package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.CategoryDto;
import com.harsh.ecommerce.entity.Category;
import com.harsh.ecommerce.exception.CategoryNotFoundException;
import com.harsh.ecommerce.exception.DuplicateCategoryException;
import com.harsh.ecommerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Create Category
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new DuplicateCategoryException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        Category category = categoryDto.toEntity();
        Category savedCategory = categoryRepository.save(category);
        return new CategoryDto(savedCategory);
    }

    // Get Category by ID
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
        return new CategoryDto(category);
    }

    // Get Category by Slug
    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with slug: " + slug));
        return new CategoryDto(category);
    }

    // Get All Categories
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDto::new);
    }

    // Get Active Categories
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    // Search Categories
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategories(String searchTerm, Pageable pageable) {
        return categoryRepository.searchCategories(searchTerm, pageable)
                .map(CategoryDto::new);
    }

    // Update Category
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        // Check for duplicate name (excluding current category)
        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new DuplicateCategoryException("Category with name '" + categoryDto.getName() + "' already exists");
        }

        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        existingCategory.setImageUrl(categoryDto.getImageUrl());
        existingCategory.setIsActive(categoryDto.getIsActive());
        existingCategory.setSortOrder(categoryDto.getSortOrder());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return new CategoryDto(updatedCategory);
    }

    // Delete Category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with existing products. Move or delete products first.");
        }

        categoryRepository.delete(category);
    }

    // Toggle Category Status
    public CategoryDto toggleCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));

        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);
        return new CategoryDto(updatedCategory);
    }

    // Get Categories by Product Count
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByProductCount() {
        return categoryRepository.findCategoriesByProductCount()
                .stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    // Category Statistics
    @Transactional(readOnly = true)
    public long getActiveCategoryCount() {
        return categoryRepository.countActiveCategories();
    }
}
