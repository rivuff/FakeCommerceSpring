package com.example.FakeCommerce.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.FakeCommerce.dtos.CreateProductRequestDto;
import com.example.FakeCommerce.dtos.GetProductResponseDto;
import com.example.FakeCommerce.dtos.GetProductWithDetailsResponseDto;
import com.example.FakeCommerce.exceptions.ResourceNotFoundException;
import com.example.FakeCommerce.repositories.ProductRepository;
import com.example.FakeCommerce.schema.Category;
import com.example.FakeCommerce.schema.Product;
import com.example.FakeCommerce.services.cache.ProductRedisCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductRedisCache productRedisCache;

    public List<GetProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream()
        .map(product -> GetProductResponseDto.builder()
                        .id(product.getId())
                        .title(product.getTitle())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .image(product.getImage())
                        .rating(product.getRating())
                        .build())
                    .collect(Collectors.toList());
    }

    public GetProductResponseDto getProductById(Long id) {
        Optional<GetProductResponseDto> cachedSummary = productRedisCache.getSummary(id);
        if (cachedSummary.isPresent()) {
            return cachedSummary.get();
        }
        GetProductResponseDto response =  productRepository.findById(id)
            .map(product -> GetProductResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(product.getImage())
                .rating(product.getRating())
                .build())
            .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        productRedisCache.putSummary(id, response);
        return response;
    }

    public GetProductWithDetailsResponseDto getProductWithDetailsById(Long id) {
        List<Product> results = productRepository.findProductWithDetailsById(id);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        Product product = results.get(0);

        return GetProductWithDetailsResponseDto.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .category(product.getCategory().getName())
                    .image(product.getImage())
                    .rating(product.getRating())
                    .build();
    }

    public Product createProduct(CreateProductRequestDto requestDto) {

        Category category = categoryService.getCategoryById(
            requestDto.getCategoryId()
        );

        Product newProduct = Product.builder()
            .title(requestDto.getTitle())
            .description(requestDto.getDescription())
            .image(requestDto.getImage())
            .price(requestDto.getPrice())
            .category(category)
            .rating(requestDto.getRating())
            .build();

        return productRepository.save(newProduct); // this will save the product to the database


    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        productRepository.delete(product);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

}
