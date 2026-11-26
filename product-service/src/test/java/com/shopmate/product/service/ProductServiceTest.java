package com.shopmate.product.service;

import com.shopmate.product.dto.ProductRequest;
import com.shopmate.product.dto.ProductResponse;
import com.shopmate.product.exception.ResourceNotFoundException;
import com.shopmate.product.model.Category;
import com.shopmate.product.model.Product;
import com.shopmate.product.repository.CategoryRepository;
import com.shopmate.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        productService = new ProductService(productRepository, categoryRepository);
    }

    @Test
    void reduceStock_sufficientStock_decrementsQuantity() {
        Category cat = new Category();
        cat.setSlug("smartphones");
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setPrice(new BigDecimal("299.00"));
        product.setStockQuantity(10);
        product.setCategory(cat);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse res = productService.reduceStock(1L, 3);
        assertEquals(7, res.getStockQuantity());
        assertEquals(7, res.getStock());
    }

    @Test
    void reduceStock_insufficientStock_throwsIllegalState() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setStockQuantity(1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class, () -> productService.reduceStock(1L, 5));
    }

    @Test
    void getProductById_missing_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void createProduct_missingCategory_throwsNotFound() {
        ProductRequest req = new ProductRequest();
        req.setName("Test");
        req.setPrice(BigDecimal.TEN);
        req.setStockQuantity(5);
        req.setCategorySlug("unknown");

        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(req));
    }
}
