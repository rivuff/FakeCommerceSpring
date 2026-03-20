package com.example.FakeCommerce.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.FakeCommerce.adapters.ReviewAdapter;
import com.example.FakeCommerce.dtos.GetReviewResponseDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.ReviewRepository;
import com.example.FakeCommerce.schema.Review;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewAdapter reviewAdapter;

    public List<GetReviewResponseDto> getAllReviews() {
        return reviewAdapter.mapToGetReviewResponseDtoList(reviewRepository.findAll());
    }

    public GetReviewResponseDto getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewAdapter::mapToGetReviewResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found"));
    }

    public List<GetReviewResponseDto> getReviewsByProductId(Long productId) {
        return reviewAdapter.mapToGetReviewResponseDtoList(reviewRepository.findByProductId(productId));
    }

    public List<GetReviewResponseDto> getReviewsByOrderId(Long orderId) {
        return reviewAdapter.mapToGetReviewResponseDtoList(reviewRepository.findByOrderId(orderId));
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found"));
        reviewRepository.delete(review);
    }
}
