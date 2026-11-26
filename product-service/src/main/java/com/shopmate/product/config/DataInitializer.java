package com.shopmate.product.config;

import com.shopmate.product.model.Category;
import com.shopmate.product.model.Product;
import com.shopmate.product.repository.CategoryRepository;
import com.shopmate.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedCatalog(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            if (categoryRepository.count() > 0) {
                return;
            }

            Category phones = saveCategory(categoryRepository, "Smartphones", "smartphones");
            Category laptops = saveCategory(categoryRepository, "Laptops", "laptops");
            Category audio = saveCategory(categoryRepository, "Audio", "audio");
            Category accessories = saveCategory(categoryRepository, "Accessories", "accessories");

            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setName("NovaPhone X5");
                p1.setDescription("5G smartphone with long battery life and 65W fast charging");
                p1.setPrice(new BigDecimal("449.99"));
                p1.setStockQuantity(50);
                p1.setRating(new BigDecimal("4.6"));
                p1.setCategory(phones);
                p1.setFeatures(Set.of("5g", "battery life", "fast charging", "amoled"));

                Product p2 = new Product();
                p2.setName("PixelLite A15");
                p2.setDescription("Compact smartphone with great camera and all-day battery life");
                p2.setPrice(new BigDecimal("349.00"));
                p2.setStockQuantity(35);
                p2.setRating(new BigDecimal("4.4"));
                p2.setCategory(phones);
                p2.setFeatures(Set.of("battery life", "camera", "5g"));

                Product p3 = new Product();
                p3.setName("AeroBook Pro 14");
                p3.setDescription("Lightweight 14-inch developer laptop with 16GB RAM and NVMe SSD");
                p3.setPrice(new BigDecimal("1099.00"));
                p3.setStockQuantity(20);
                p3.setRating(new BigDecimal("4.8"));
                p3.setCategory(laptops);
                p3.setFeatures(Set.of("16gb ram", "ssd", "lightweight", "fast charging"));

                Product p4 = new Product();
                p4.setName("SoundPulse ANC Headphones");
                p4.setDescription("Wireless Bluetooth headphones with active noise cancellation");
                p4.setPrice(new BigDecimal("149.50"));
                p4.setStockQuantity(80);
                p4.setRating(new BigDecimal("4.5"));
                p4.setCategory(audio);
                p4.setFeatures(Set.of("wireless", "bluetooth", "noise cancellation", "battery life"));

                Product p5 = new Product();
                p5.setName("VoltCharge 65W GaN Adapter");
                p5.setDescription("Compact dual-port USB-C fast charger for laptops and phones");
                p5.setPrice(new BigDecimal("39.99"));
                p5.setStockQuantity(120);
                p5.setRating(new BigDecimal("4.7"));
                p5.setCategory(accessories);
                p5.setFeatures(Set.of("fast charging", "usb-c", "compact"));

                productRepository.saveAll(List.of(p1, p2, p3, p4, p5));
                log.info("Seeded default categories and 5 sample products");
            }
        };
    }

    private Category saveCategory(CategoryRepository repo, String name, String slug) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        return repo.save(c);
    }
}
