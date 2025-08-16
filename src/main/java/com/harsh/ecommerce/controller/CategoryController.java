package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.CategoryDto;
import com.harsh.ecommerce.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<CategoryDto> categories = categoryService.getAllCategories(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories.getContent());
            response.put("currentPage", categories.getNumber());
            response.put("totalItems", categories.getTotalElements());
            response.put("totalPages", categories.getTotalPages());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCategories() {
        try {
            List<CategoryDto> categories = categoryService.getActiveCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories);
            response.put("count", categories.size());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDto category = categoryService.getCategoryById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getCategoryBySlug(@PathVariable String slug) {
        try {
            CategoryDto category = categoryService.getCategoryBySlug(slug);

            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CategoryDto> categories = categoryService.searchCategories(q, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("categories", categories.getContent());
            response.put("searchTerm", q);
            response.put("currentPage", categories.getNumber());
            response.put("totalItems", categories.getTotalElements());
            response.put("totalPages", categories.getTotalPages());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}