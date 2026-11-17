package com.shopmate.order.client;

/**
 * Placeholder for later. When you add Resilience4j / Spring Cloud CircuitBreaker,
 * annotate this with @Component and reference it with
 * @FeignClient(..., fallback = ProductClientFallback.class).
 * It is intentionally NOT a Spring bean yet (two ProductClient beans would clash).
 */
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProduct(Long id) {
        throw new IllegalStateException("Product service is currently unavailable");
    }

    @Override
    public void reduceStock(Long id, int quantity) {
        throw new IllegalStateException("Product service is currently unavailable");
    }
}
