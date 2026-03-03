package com.example.FakeCommerce.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.FakeCommerce.dtos.CreateReviewRequestDto;
import com.example.FakeCommerce.repositories.OrderRepository;
import com.example.FakeCommerce.repositories.ProductRepository;
import com.example.FakeCommerce.repositories.ReviewRepository;
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.Product;
import com.example.FakeCommerce.schema.Review;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;


    public List<Review> getAllReviews(){
        return reviewRepository.findAll();
    }

    public Review creatReview(CreateReviewRequestDto requestDto){
        Order order = orderRepository.findById(requestDto.getOrderId())
        .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(requestDto.getProductId())
        .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = Review.builder()
                        .comment(requestDto.getComment())
                        .order(order)
                        .product(product)
                        .rating(requestDto.getRating())
                        .build();
        
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id){
        reviewRepository.deleteById(id);
    }

    public Review getReviewById(Long id){
        return reviewRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    public List<Review> getReviewsByProductId(Long productId){
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getReviewsByOrderId(Long orderId){
        return reviewRepository.findByOrderId(orderId);
    }

    
        
}
