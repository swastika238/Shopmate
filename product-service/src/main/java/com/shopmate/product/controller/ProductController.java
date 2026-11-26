package com.shopmate.product.controller;

import com.shopmate.product.dto.ProductRequest;
import com.shopmate.product.dto.ProductResponse;
import com.shopmate.product.dto.ProductSearchRequest;
import com.shopmate.product.service.ProductSearchService;
import com.shopmate.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/products", "/api/products"})
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    public ProductController(ProductService productService, ProductSearchService productSearchService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
    }

    // --- Public browsing endpoints ---

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/search")
    public List<ProductResponse> search(@RequestBody ProductSearchRequest request) {
        return productSearchService.search(request);
    }

    // --- Admin endpoints ---

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // --- Internal inventory endpoints (called by order-service) ---

    @PutMapping("/{id}/reduce-stock")
    public ProductResponse reduceStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        return productService.reduceStock(id, quantity);
    }

    @PutMapping("/{id}/restore-stock")
    public ProductResponse restoreStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        return productService.restoreStock(id, quantity);
    }
}
