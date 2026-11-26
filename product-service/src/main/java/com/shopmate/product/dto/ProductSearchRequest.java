package com.shopmate.product.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is what chatbot-service (via ml-service's parsed intent) and the
 * React catalog's filter sidebar both send to POST /products/search.
 * Every field is optional — only non-null fields get applied as filters.
 */
public class ProductSearchRequest {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String category;
    private List<String> features;
    private String keyword;

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
