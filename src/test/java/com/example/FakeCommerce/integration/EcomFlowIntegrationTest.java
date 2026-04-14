package com.example.FakeCommerce.integration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.FakeCommerce.dtos.CreateCategoryRequestDto;
import com.example.FakeCommerce.dtos.CreateOrderRequestDTO;
import com.example.FakeCommerce.dtos.CreateProductRequestDto;
import com.example.FakeCommerce.dtos.OrderItemRequestDto;
import com.example.FakeCommerce.dtos.UpdateOrderRequestDto;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.services.cache.ProductRedisCache;
import com.jayway.jsonpath.JsonPath;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude="+
            "org.springframeframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.DataRedisReactiveAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.redis.DataRedisRepositoriesAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true"
    }
)
@AutoConfigureMockMvc
public class EcomFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductRedisCache productRedisCache;

    @BeforeEach
    void setUp() {
        when(productRedisCache.getSummary(anyLong())).thenReturn(Optional.empty());
    }

    private Long extractId(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.data.id")).longValue();
    }

    @Test
    void testFullEcommerceFlow() throws Exception {

        // ── 1. Create a category ──

        String categoryJson = objectMapper.writeValueAsString(
            CreateCategoryRequestDto.builder().name("Electronics").build()
        );

        MvcResult categoryResult = mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Electronics"))
            .andReturn();

        Long categoryId = extractId(categoryResult);

        // ── 2. Verify category retrieval ──

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Electronics"));

        // ── 3. Create 3 products ──

        Long productAId = createProduct("Laptop", "High-end laptop", "laptop.jpg",
            new BigDecimal("999.99"), categoryId, new BigDecimal("4.5"));

        Long productBId = createProduct("Phone", "Flagship phone", "phone.jpg",
            new BigDecimal("499.99"), categoryId, new BigDecimal("4.2"));

        Long productCId = createProduct("Tablet", "Portable tablet", "tablet.jpg",
            new BigDecimal("299.99"), categoryId, new BigDecimal("4.0"));

        // ── 4. Verify each product by ID and the full list ──

        mockMvc.perform(get("/api/v1/products/{id}", productAId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Laptop"));

        mockMvc.perform(get("/api/v1/products/{id}", productBId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Phone"));

        mockMvc.perform(get("/api/v1/products/{id}", productCId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("Tablet"));

        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(3));

        // ── 5. Create an order with all 3 products (different quantities) ──

        CreateOrderRequestDTO orderRequest = CreateOrderRequestDTO.builder()
            .orderItems(List.of(
                OrderItemRequestDto.builder().productId(productAId).quantity(2).build(),
                OrderItemRequestDto.builder().productId(productBId).quantity(1).build(),
                OrderItemRequestDto.builder().productId(productCId).quantity(3).build()
            ))
            .build();

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.items.length()").value(3))
            .andReturn();

        Long orderId = extractId(orderResult);

        // ── 6. Verify order by ID ──

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.items.length()").value(3));

        // ── 7. Fetch order summary and verify totals ──
        // totalItems = 2 + 1 + 3 = 6
        // totalPrice = (999.99 * 2) + (499.99 * 1) + (299.99 * 3) = 1999.98 + 499.99 + 899.97 = 3399.94

        mockMvc.perform(get("/api/v1/orders/{id}/summary", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.totalItems").value(6))
            .andExpect(jsonPath("$.data.totalPrice").value(3399.94));

        // ── 8. Update order status to SHIPPED ──

        UpdateOrderRequestDto updateRequest = UpdateOrderRequestDto.builder()
            .status(OrderStatus.SHIPPED)
            .build();

        mockMvc.perform(put("/api/v1/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        // ── 9. Fetch summary again — status updated, totals unchanged ──

        mockMvc.perform(get("/api/v1/orders/{id}/summary", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SHIPPED"))
            .andExpect(jsonPath("$.data.totalItems").value(6))
            .andExpect(jsonPath("$.data.totalPrice").value(3399.94));

        // ── 10. Delete the order and verify it's gone ──

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));

        // ── 11. Delete each product and verify ──

        for (Long productId : List.of(productAId, productBId, productCId)) {
            mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        }

        // ── 12. Delete the category and verify ──

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    private Long createProduct(String title, String description, String image,
                               BigDecimal price, Long categoryId, BigDecimal rating) throws Exception {
        CreateProductRequestDto dto = CreateProductRequestDto.builder()
            .title(title)
            .description(description)
            .image(image)
            .price(price)
            .categoryId(categoryId)
            .rating(rating)
            .build();

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        return extractId(result);
    }
}
