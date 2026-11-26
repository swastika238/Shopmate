package com.shopmate.product.repository;

import com.shopmate.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaSpecificationExecutor lets ProductSearchService build a query dynamically —
 * "only filter by price if a price was given", "only filter by category if one
 * was given", etc. — without writing a different @Query for every combination.
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
