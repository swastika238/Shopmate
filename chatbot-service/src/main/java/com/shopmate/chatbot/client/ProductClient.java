package com.shopmate.chatbot.client;

import com.shopmate.chatbot.dto.ProductSearchRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductClient {

    @GetMapping("/api/products")
    List<ProductSummary> getAllProducts();

    @PostMapping("/api/products/search")
    List<ProductSummary> searchProducts(@RequestBody ProductSearchRequest request);

    @GetMapping("/api/categories")
    List<CategorySummary> getCategories();
}
