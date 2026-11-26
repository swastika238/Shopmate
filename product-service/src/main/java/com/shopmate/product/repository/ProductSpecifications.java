package com.shopmate.product.repository;

import com.shopmate.product.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

/**
 * Builds the WHERE clause piece by piece, in plain Java, instead of one giant
 * @Query string. Each method returns null when its filter wasn't requested —
 * Specification.where(...).and(...) silently skips null specs, so calling
 * search with only maxPrice set still produces a clean, correct SQL query.
 */
public class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        if (minPrice == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> inCategory(String categorySlug) {
        if (categorySlug == null || categorySlug.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("category").get("slug")), categorySlug.toLowerCase());
    }

    /**
     * Matches products that have AT LEAST ONE of the requested features.
     */
    public static Specification<Product> hasAnyFeature(List<String> features) {
        if (features == null || features.isEmpty()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            return root.join("features").in(features);
        };
    }

    public static Specification<Product> nameOrDescriptionContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }
}
