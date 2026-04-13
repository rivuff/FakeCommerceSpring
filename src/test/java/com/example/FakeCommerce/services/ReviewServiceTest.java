package com.example.FakeCommerce.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.example.FakeCommerce.adapters.ReviewAdapter;
import com.example.FakeCommerce.dtos.GetReviewResponseDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.ReviewRepository;
import com.example.FakeCommerce.schema.Order;
import com.example.FakeCommerce.schema.OrderStatus;
import com.example.FakeCommerce.schema.Product;
import com.example.FakeCommerce.schema.Review;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewAdapter reviewAdapter;

    @InjectMocks
    private ReviewService reviewService;

    private Review buildReview(Long id, String comment, BigDecimal rating, Product product, Order order) {
        Review review = Review.builder()
                .comment(comment)
                .rating(rating)
                .product(product)
                .order(order)
                .build();
        review.setId(id);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    private GetReviewResponseDto buildReviewDto(Long id, String comment, BigDecimal rating,
                                                 Long productId, Long orderId) {
        return GetReviewResponseDto.builder()
                .id(id)
                .comment(comment)
                .rating(rating)
                .productId(productId)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Product buildProduct(Long id) {
        Product product = Product.builder()
                .title("Product " + id)
                .price(BigDecimal.valueOf(100))
                .rating(BigDecimal.valueOf(4.0))
                .build();
        product.setId(id);
        return product;
    }

    private Order buildOrder(Long id) {
        Order order = Order.builder().status(OrderStatus.DELIVERED).build();
        order.setId(id);
        return order;
    }

    // --- getAllReviews ---

    @Test
    void getAllReviews_returnsMappedDtoList() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review r1 = buildReview(1L, "Great", BigDecimal.valueOf(5), product, order);
        Review r2 = buildReview(2L, "Good", BigDecimal.valueOf(4), product, order);
        List<Review> reviews = List.of(r1, r2);

        List<GetReviewResponseDto> dtos = List.of(
                buildReviewDto(1L, "Great", BigDecimal.valueOf(5), 10L, 1L),
                buildReviewDto(2L, "Good", BigDecimal.valueOf(4), 10L, 1L));

        when(reviewRepository.findAll()).thenReturn(reviews);
        when(reviewAdapter.mapToGetReviewResponseDtoList(reviews)).thenReturn(dtos);

        // act.
        List<GetReviewResponseDto> result = reviewService.getAllReviews();

        // assert.
        assertEquals(2, result.size());
        assertEquals("Great", result.get(0).getComment());
        assertEquals("Good", result.get(1).getComment());
    }

    @Test
    void getAllReviews_whenEmpty_returnsEmptyList() {
        // arrange.
        when(reviewRepository.findAll()).thenReturn(Collections.emptyList());
        when(reviewAdapter.mapToGetReviewResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // act.
        List<GetReviewResponseDto> result = reviewService.getAllReviews();

        // assert.
        assertTrue(result.isEmpty());
    }

    // --- getReviewById ---

    @Test
    void getReviewById_whenFound_returnsMappedDto() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review review = buildReview(1L, "Excellent", BigDecimal.valueOf(5), product, order);
        GetReviewResponseDto dto = buildReviewDto(1L, "Excellent", BigDecimal.valueOf(5), 10L, 1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewAdapter.mapToGetReviewResponseDto(review)).thenReturn(dto);

        // act.
        GetReviewResponseDto result = reviewService.getReviewById(1L);

        // assert.
        assertEquals(1L, result.getId());
        assertEquals("Excellent", result.getComment());
        assertEquals(BigDecimal.valueOf(5), result.getRating());
    }

    @Test
    void getReviewById_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(99L));
    }

    // --- getReviewsByProductId ---

    @Test
    void getReviewsByProductId_returnsMappedDtoList() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review review = buildReview(1L, "Nice product", BigDecimal.valueOf(4), product, order);
        List<Review> reviews = List.of(review);

        List<GetReviewResponseDto> dtos = List.of(
                buildReviewDto(1L, "Nice product", BigDecimal.valueOf(4), 10L, 1L));

        when(reviewRepository.findByProductId(10L)).thenReturn(reviews);
        when(reviewAdapter.mapToGetReviewResponseDtoList(reviews)).thenReturn(dtos);

        // act.
        List<GetReviewResponseDto> result = reviewService.getReviewsByProductId(10L);

        // assert.
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getProductId());
    }

    @Test
    void getReviewsByProductId_whenNone_returnsEmptyList() {
        // arrange.
        when(reviewRepository.findByProductId(99L)).thenReturn(Collections.emptyList());
        when(reviewAdapter.mapToGetReviewResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // act.
        List<GetReviewResponseDto> result = reviewService.getReviewsByProductId(99L);

        // assert.
        assertTrue(result.isEmpty());
    }

    // --- getReviewsByOrderId ---

    @Test
    void getReviewsByOrderId_returnsMappedDtoList() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review review = buildReview(1L, "Fast delivery", BigDecimal.valueOf(5), product, order);
        List<Review> reviews = List.of(review);

        List<GetReviewResponseDto> dtos = List.of(
                buildReviewDto(1L, "Fast delivery", BigDecimal.valueOf(5), 10L, 1L));

        when(reviewRepository.findByOrderId(1L)).thenReturn(reviews);
        when(reviewAdapter.mapToGetReviewResponseDtoList(reviews)).thenReturn(dtos);

        // act.
        List<GetReviewResponseDto> result = reviewService.getReviewsByOrderId(1L);

        // assert.
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrderId());
    }

    @Test
    void getReviewsByOrderId_whenNone_returnsEmptyList() {
        // arrange.
        when(reviewRepository.findByOrderId(99L)).thenReturn(Collections.emptyList());
        when(reviewAdapter.mapToGetReviewResponseDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // act.
        List<GetReviewResponseDto> result = reviewService.getReviewsByOrderId(99L);

        // assert.
        assertTrue(result.isEmpty());
    }

    // --- createReview ---

    @Test
    void createReview_savesAndReturnsReview() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review review = buildReview(null, "Amazing!", BigDecimal.valueOf(5), product, order);

        Review savedReview = buildReview(1L, "Amazing!", BigDecimal.valueOf(5), product, order);
        when(reviewRepository.save(review)).thenReturn(savedReview);

        // act.
        Review result = reviewService.createReview(review);

        // assert.
        assertEquals(1L, result.getId());
        assertEquals("Amazing!", result.getComment());
        assertEquals(BigDecimal.valueOf(5), result.getRating());
    }

    // --- deleteReview ---

    @Test
    void deleteReview_whenFound_deletesSuccessfully() {
        // arrange.
        Product product = buildProduct(10L);
        Order order = buildOrder(1L);
        Review review = buildReview(1L, "Good", BigDecimal.valueOf(4), product, order);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // act.
        reviewService.deleteReview(1L);

        // assert.
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_whenNotFound_throwsResourceNotFoundException() {
        // arrange.
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        // act and assert.
        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(99L));
    }
}
