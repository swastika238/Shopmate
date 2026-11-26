package com.shopmate.product.dto;

import com.shopmate.product.model.Product;

import java.math.BigDecimal;
import java.util.Set;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private BigDecimal rating;
    private String category;
    private Set<String> features;

    public static ProductResponse from(Product p) {
        ProductResponse r = new ProductResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.price = p.getPrice();
        r.stockQuantity = p.getStockQuantity();
        r.imageUrl = p.getImageUrl();
        r.rating = p.getRating();
        r.category = p.getCategory() != null ? p.getCategory().getSlug() : null;
        r.features = p.getFeatures();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public int getStock() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getRating() { return rating; }
    public String getCategory() { return category; }
    public Set<String> getFeatures() { return features; }
}
