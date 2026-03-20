package com.example.FakeCommerce.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FakeCommerce.dtos.CreateOrderRequestDTO;
import com.example.FakeCommerce.dtos.GetOrderResponseDto;
import com.example.FakeCommerce.dtos.GetOrderSummaryResponseDto;
import com.example.FakeCommerce.dtos.UpdateOrderRequestDto;
import com.example.FakeCommerce.services.OrderService;
import com.example.FakeCommerce.utils.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetOrderResponseDto>>> getAllOrders() {
        List<GetOrderResponseDto> orders = orderService.getAllOrders();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(orders, "Orders fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GetOrderResponseDto>> createOrder(@RequestBody CreateOrderRequestDTO createOrderRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    ApiResponse.success(
                        orderService.createOrder(createOrderRequestDTO), 
                        "Order created successfully")
                    );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(null, "Order deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOrderResponseDto>> getOrderById(@PathVariable Long id) {
        GetOrderResponseDto order = orderService.getOrderById(id);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(order, "Order fetched successfully"));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOrderResponseDto>> updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequestDto updateOrderRequestDto) {
        return ResponseEntity
               .status(HttpStatus.OK)
               .body(
                    ApiResponse.success(orderService.updateOrder(id, updateOrderRequestDto), 
                    "Order updated successfully")
               );
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<GetOrderSummaryResponseDto>> getOrderSummary(@PathVariable Long id) {
        return ResponseEntity
               .status(HttpStatus.OK)
               .body(
                    ApiResponse.success(orderService.getOrderSummary(id), 
                    "Order summary fetched successfully")
               );
    }

}
