package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.CategoryDto;
import com.harsh.ecommerce.service.CategoryService;
import com.harsh.ecommerce.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "👨‍💼 Admin - Categories", description = "Category management (admin only)")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
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
    @Operation(summary = "Get all categories for admin", description = "Retrieves a paginated list of all categories. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<?> getAllCategories(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field", example = "sortOrder", schema = @Schema(allowableValues = {"name", "sortOrder", "createdAt"}))
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @Parameter(description = "Sort direction", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
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
    @Operation(summary = "Get category by ID", description = "Retrieves a single category by its ID. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> getCategoryById(@Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
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
    @Operation(summary = "Update a category", description = "Updates an existing category's details by ID. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> updateCategory(@Parameter(description = "Category ID", example = "1") @PathVariable Long id, @Valid @RequestBody CategoryDto categoryDto) {
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
    @Operation(summary = "Delete a category", description = "Deletes a category by ID. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> deleteCategory(@Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
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
    @Operation(summary = "Toggle category status", description = "Activates or deactivates a category. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> toggleCategoryStatus(@Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
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
    @Operation(summary = "Get category analytics", description = "Retrieves analytics for categories, including total count and product counts. Admin only.")
    @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
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
    @Operation(summary = "Upload category image", description = "Uploads an image for a specific category and updates its image URL. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<?> uploadCategoryImage(@Parameter(description = "Category ID", example = "1") @PathVariable Long id, @RequestParam("file") MultipartFile file) {
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
    @Operation(summary = "Upload image standalone", description = "Uploads an image without associating it with a specific category. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed")
    })
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
    @Operation(summary = "Delete category image", description = "Deletes an image from the cloud storage using its URL. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image URL or delete failed")
    })
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