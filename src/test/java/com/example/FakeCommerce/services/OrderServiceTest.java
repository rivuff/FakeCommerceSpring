package com.example.FakeCommerce.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.FakeCommerce.adapters.OrderAdapter;
import com.example.FakeCommerce.dtos.CreateOrderRequestDTO;
import com.example.FakeCommerce.dtos.GetOrderResponseDto;
import com.example.FakeCommerce.dtos.GetOrderSummaryResponseDto;
import com.example.FakeCommerce.dtos.OrderItemAction;
import com.example.FakeCommerce.dtos.OrderItemActionDto;
import com.example.FakeCommerce.dtos.OrderItemRequestDto;
import com.example.FakeCommerce.dtos.OrderItemResponseDto;
import com.example.FakeCommerce.dtos.UpdateOrderRequestDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.OrderRespository;
import com.example.FakeCommerce.repositories.OrderproductsRepository;
import com.example.FakeCommerce.repositories.ProductRepository;
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.OrderProducts;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.schema.Product;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRespository orderRespository;

    @Mock
    private OrderproductsRepository orderproductsRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderAdapter orderAdapter;

    @InjectMocks
    private OrderService orderService;

    private Order buildOrder(Long id, OrderStatus status) {
        Order order = Order.builder().status(status).build();
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    private Product buildProduct(Long id, String title, BigDecimal price) {
        Product product = Product.builder()
                .title(title)
                .price(price)
                .rating(BigDecimal.valueOf(4.0))
                .build();
        product.setId(id);
        return product;
    }

    private GetOrderResponseDto buildOrderResponseDto(Long id, OrderStatus status) {
        return GetOrderResponseDto.builder()
                .id(id)
                .status(status)
                .items(Collections.emptyList())
                .build();
    }

    // --- getAllOrders ---

    @Test
    void getAllOrders_returnsMappedDtoList() {
        // arrange.
        List<Order> orders = List.of(
                buildOrder(1L, OrderStatus.PENDING),
                buildOrder(2L, OrderStatus.SHIPPED));
        List<GetOrderResponseDto> dtos = List.of(
                buildOrderResponseDto(1L, OrderStatus.PENDING),
                buildOrderResponseDto(2L, OrderStatus.SHIPPED));
        when(orderRespository.findAll()).thenReturn(orders);
        when(orderAdapter.mapToGetOrderResponseDtoList(orders)).thenReturn(dtos);

        // act.
        List<GetOrderResponseDto> result = orderService.getAllOrders();

        // assert.
        assertEquals(2, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        assertEquals(OrderStatus.SHIPPED, result.get(1).getStatus());
    }

    @Test
    void getAllOrders_whenEmpty_returnsEmptyList() {
        // arrange.
        when(orderRespository.findAll()).thenReturn(Collections.emptyList());
        when(orderAdapter.mapToGetOrderResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // act.
        List<GetOrderResponseDto> result = orderService.getAllOrders();

        // assert.
        assertEquals(0, result.size());
    }

    // --- getOrderById ---

    @Test
    void getOrderById_whenFound_returnsDto() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        GetOrderResponseDto dto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(dto);

        // act.
        GetOrderResponseDto result = orderService.getOrderById(1L);

        // assert.
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
    }

    @Test
    void getOrderById_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(orderRespository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    // --- deleteOrder ---

    @Test
    void deleteOrder_whenFound_deletesSuccessfully() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));

        // act.
        orderService.deleteOrder(1L);

        // assert.
        verify(orderRespository).delete(order);
    }

    @Test
    void deleteOrder_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(orderRespository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(99L));
    }

    // --- createOrder ---

    @Test
    void createOrder_withOrderItems_createsOrderAndSavesItems() {
        // arrange.
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderItemRequestDto itemDto = OrderItemRequestDto.builder().productId(10L).quantity(2).build();
        CreateOrderRequestDTO requestDTO = CreateOrderRequestDTO.builder()
                .orderItems(List.of(itemDto))
                .build();

        Order savedOrder = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(any(Order.class))).thenReturn(expectedDto);

        // act.
        GetOrderResponseDto result = orderService.createOrder(requestDTO);

        // assert.
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderproductsRepository).saveAll(anyList());
    }

    @Test
    void createOrder_withNullOrderItems_createsOrderOnly() {
        // arrange.
        CreateOrderRequestDTO requestDTO = CreateOrderRequestDTO.builder().orderItems(null).build();
        Order savedOrder = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.save(any(Order.class))).thenReturn(savedOrder);

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(any(Order.class))).thenReturn(expectedDto);

        // act.
        GetOrderResponseDto result = orderService.createOrder(requestDTO);

        // assert.
        assertEquals(1L, result.getId());
        verify(orderproductsRepository, never()).saveAll(anyList());
    }

    @Test
    void createOrder_productNotFound_throwsResourceNotFoundException() {
        // arrange.
        OrderItemRequestDto itemDto = OrderItemRequestDto.builder().productId(99L).quantity(1).build();
        CreateOrderRequestDTO requestDTO = CreateOrderRequestDTO.builder()
                .orderItems(List.of(itemDto))
                .build();

        Order savedOrder = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.findAllById(List.of(99L))).thenReturn(Collections.emptyList());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(requestDTO));
    }

    @Test
    void createOrder_withNullQuantity_defaultsToOne() {
        // arrange.
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderItemRequestDto itemDto = OrderItemRequestDto.builder().productId(10L).quantity(null).build();
        CreateOrderRequestDTO requestDTO = CreateOrderRequestDTO.builder()
                .orderItems(List.of(itemDto))
                .build();

        Order savedOrder = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(any(Order.class))).thenReturn(expectedDto);

        // act.
        GetOrderResponseDto result = orderService.createOrder(requestDTO);

        // assert.
        assertEquals(1L, result.getId());
        verify(orderproductsRepository).saveAll(anyList());
    }

    // --- updateOrder ---

    @Test
    void updateOrder_statusOnly_updatesStatusAndSaves() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));

        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .status(OrderStatus.SHIPPED)
                .orderItems(null)
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.SHIPPED);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        GetOrderResponseDto result = orderService.updateOrder(1L, dto);

        // assert.
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        verify(orderRespository).save(order);
    }

    @Test
    void updateOrder_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(orderRespository.findById(99L)).thenReturn(Optional.empty());
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder().status(OrderStatus.SHIPPED).build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(99L, dto));
    }

    @Test
    void updateOrder_addNewItem_savesNewOrderProduct() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(Collections.emptyList());

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).quantity(3).action(OrderItemAction.ADD).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        GetOrderResponseDto result = orderService.updateOrder(1L, dto);

        // assert.
        assertEquals(1L, result.getId());
        verify(orderproductsRepository).saveAll(anyList());
    }

    @Test
    void updateOrder_addExistingItem_incrementsQuantity() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderProducts existing = OrderProducts.builder()
                .order(order).product(product).quantity(2).build();
        existing.setId(100L);

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(existing));

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).quantity(3).action(OrderItemAction.ADD).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        orderService.updateOrder(1L, dto);

        // assert: quantity should be 2 + 3 = 5
        assertEquals(5, existing.getQuantity());
        verify(orderproductsRepository).saveAll(anyList());
    }

    @Test
    void updateOrder_removeItem_deletesOrderProduct() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderProducts existing = OrderProducts.builder()
                .order(order).product(product).quantity(2).build();
        existing.setId(100L);

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(existing));

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.REMOVE).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        orderService.updateOrder(1L, dto);

        // assert.
        verify(orderproductsRepository).deleteAll(anyList());
    }

    @Test
    void updateOrder_removeNonExistingItem_throwsResourceNotFoundException() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(Collections.emptyList());

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.REMOVE).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, dto));
    }

    @Test
    void updateOrder_incrementItem_incrementsQuantityByOne() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderProducts existing = OrderProducts.builder()
                .order(order).product(product).quantity(3).build();
        existing.setId(100L);

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(existing));

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.INCREMENT).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        orderService.updateOrder(1L, dto);

        // assert: 3 + 1 = 4
        assertEquals(4, existing.getQuantity());
        verify(orderproductsRepository).saveAll(anyList());
    }

    @Test
    void updateOrder_incrementNonExistingItem_throwsResourceNotFoundException() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(Collections.emptyList());

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.INCREMENT).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, dto));
    }

    @Test
    void updateOrder_decrementItem_quantityAboveOne_decrementsQuantity() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderProducts existing = OrderProducts.builder()
                .order(order).product(product).quantity(5).build();
        existing.setId(100L);

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(existing));

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.DECREMENT).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        orderService.updateOrder(1L, dto);

        // assert: 5 - 1 = 4
        assertEquals(4, existing.getQuantity());
        verify(orderproductsRepository).saveAll(anyList());
    }

    @Test
    void updateOrder_decrementItem_quantityIsOne_removesItem() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));
        OrderProducts existing = OrderProducts.builder()
                .order(order).product(product).quantity(1).build();
        existing.setId(100L);

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(existing));

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.DECREMENT).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        GetOrderResponseDto expectedDto = buildOrderResponseDto(1L, OrderStatus.PENDING);
        when(orderAdapter.mapToGetOrderResponseDto(order)).thenReturn(expectedDto);

        // act.
        orderService.updateOrder(1L, dto);

        // assert.
        verify(orderproductsRepository).deleteAll(anyList());
    }

    @Test
    void updateOrder_decrementNonExistingItem_throwsResourceNotFoundException() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product product = buildProduct(10L, "Phone", BigDecimal.valueOf(999));

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(Collections.emptyList());

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(10L).action(OrderItemAction.DECREMENT).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, dto));
    }

    @Test
    void updateOrder_productNotFound_throwsResourceNotFoundException() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findAllById(List.of(99L))).thenReturn(Collections.emptyList());

        OrderItemActionDto actionDto = OrderItemActionDto.builder()
                .productId(99L).action(OrderItemAction.ADD).quantity(1).build();
        UpdateOrderRequestDto dto = UpdateOrderRequestDto.builder()
                .orderItems(List.of(actionDto))
                .build();

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, dto));
    }

    // --- getOrderSummary ---

    @Test
    void getOrderSummary_whenFound_returnsComputedSummary() {
        // arrange.
        Order order = buildOrder(1L, OrderStatus.PENDING);
        Product p1 = buildProduct(10L, "Phone", BigDecimal.valueOf(100));
        Product p2 = buildProduct(20L, "Case", BigDecimal.valueOf(25));

        OrderProducts op1 = OrderProducts.builder().order(order).product(p1).quantity(2).build();
        OrderProducts op2 = OrderProducts.builder().order(order).product(p2).quantity(4).build();

        when(orderRespository.findById(1L)).thenReturn(Optional.of(order));
        when(orderproductsRepository.findByOrderWithProduct(order)).thenReturn(List.of(op1, op2));

        List<OrderItemResponseDto> itemDtos = List.of(
                OrderItemResponseDto.builder().productId(10L).quantity(2).subTotal(BigDecimal.valueOf(200)).build(),
                OrderItemResponseDto.builder().productId(20L).quantity(4).subTotal(BigDecimal.valueOf(100)).build());
        when(orderAdapter.mapToOrderItemResponseDto(List.of(op1, op2))).thenReturn(itemDtos);

        // act.
        GetOrderSummaryResponseDto result = orderService.getOrderSummary(1L);

        // assert.
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(6, result.getTotalItems());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(result.getTotalPrice()));
        assertEquals(2, result.getItems().size());
    }

    @Test
    void getOrderSummary_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(orderRespository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderSummary(99L));
    }
}
