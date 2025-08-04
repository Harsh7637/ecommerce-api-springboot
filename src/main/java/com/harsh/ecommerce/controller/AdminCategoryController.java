package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.CategoryDto;
import com.harsh.ecommerce.service.CategoryService;
import com.harsh.ecommerce.service.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto category = categoryService.createCategory(categoryDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category created successfully");
            response.put("category", category);
            response.put("success", true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto category = categoryService.updateCategory(id, categoryDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category updated successfully");
            response.put("category", category);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Long id) {
        try {
            CategoryDto category = categoryService.toggleCategoryStatus(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Category status updated successfully");
            response.put("category", category);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getCategoryAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalCategories", categoryService.getActiveCategoryCount());
            List<CategoryDto> categoriesByProductCount = categoryService.getCategoriesByProductCount();
            analytics.put("categoriesByProductCount", categoriesByProductCount);
            Map<String, Object> response = new HashMap<>();
            response.put("analytics", analytics);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadCategoryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please upload a valid image file"));
            }
            String imageUrl = cloudinaryService.uploadCategoryImage(file);
            CategoryDto updatedCategory = categoryService.updateCategoryImage(id, imageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("category", updatedCategory);
            response.put("imageUrl", imageUrl);
            response.put("success", true);
            response.put("message", "Category image uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadCategoryImageStandalone(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please upload a valid image file"));
            }
            String imageUrl = cloudinaryService.uploadCategoryImage(file);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<?> deleteCategoryImage(@RequestParam("imageUrl") String imageUrl) {
        try {
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Image deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to delete image: " + e.getMessage()));
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
