package com.shopmate.product.service;

import com.shopmate.product.dto.CategoryRequest;
import com.shopmate.product.dto.CategoryResponse;
import com.shopmate.product.model.Category;
import com.shopmate.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String normalizedSlug = request.getSlug().trim().toLowerCase();
        if (categoryRepository.findBySlug(normalizedSlug).isPresent()) {
            throw new IllegalArgumentException("Category slug already exists: " + normalizedSlug);
        }
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(normalizedSlug);
        return CategoryResponse.from(categoryRepository.save(category));
    }
}
