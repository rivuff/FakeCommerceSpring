package com.example.FakeCommerce.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.FakeCommerce.adapters.OrderAdapter;
import com.example.FakeCommerce.dtos.CreateOrderRequestDTO;
import com.example.FakeCommerce.dtos.GetOrderResponseDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.OrderRespository;
import com.example.FakeCommerce.repositories.OrderproductsRepository;
import com.example.FakeCommerce.repositories.ProductRepository;
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.OrderProducts;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.schema.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRespository orderRespository;
    private final OrderproductsRepository orderproductsRepository;
    private final ProductRepository productRepository;
    private final OrderAdapter orderAdapter;


    public List<GetOrderResponseDto> getAllOrders() {

        List<Order> orders = orderRespository.findAll();
        return orderAdapter.mapToGetOrderResponseDtoList(orders);

    }

    public GetOrderResponseDto getOrderById(Long id) {
        Order order = orderRespository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return orderAdapter.mapToGetOrderResponseDto(order);
    }

    public void deleteOrder(Long id) {
        Order order = orderRespository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        orderRespository.delete(order);
    }


    public void createOrder(CreateOrderRequestDTO createOrderRequestDTO) {
        Order order = Order.builder()
                        .status(OrderStatus.PENDING)
                        .build();

        orderRespository.save(order);

        if(createOrderRequestDTO.getOrderItems() != null) {
            for(var itemDto : createOrderRequestDTO.getOrderItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDto.getProductId()));


                OrderProducts orderProduct = OrderProducts.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1)
                    .build();

                orderproductsRepository.save(orderProduct);
                
            }
        }


    }
}


// User -> Cart -> Adds an item -> New Order (Pending)

// User -> adds more items in the cart -> Same order will be updated

// During checkout -> Order Pending -> Success/Failure