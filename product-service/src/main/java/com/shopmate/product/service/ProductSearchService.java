package com.shopmate.product.service;

import com.shopmate.product.dto.ProductResponse;
import com.shopmate.product.dto.ProductSearchRequest;
import com.shopmate.product.model.Product;
import com.shopmate.product.repository.ProductRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shopmate.product.repository.ProductSpecifications.*;

/**
 * This is the endpoint the chatbot calls. chatbot-service sends the parsed
 * output of a query like "I need a phone under 500 with good battery life"
 * (minPrice/maxPrice/category/features extracted by ml-service) straight into
 * this request shape — this service doesn't know or care that NLP was involved,
 * it just applies whichever filters are present.
 */
@Service
public class ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(ProductSearchRequest request) {
        Specification<Product> spec = Specification.where(hasMinPrice(request.getMinPrice()))
                .and(hasMaxPrice(request.getMaxPrice()))
                .and(inCategory(request.getCategory()))
                .and(hasAnyFeature(request.getFeatures()))
                .and(nameOrDescriptionContains(request.getKeyword()));

        return productRepository.findAll(spec).stream()
                .map(ProductResponse::from)
                .toList();
    }
}
