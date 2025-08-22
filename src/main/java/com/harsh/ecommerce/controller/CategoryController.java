package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.CategoryDto;
import com.harsh.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "🌐 Public - Categories", description = "Public category browsing")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a paginated list of all categories.")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> getAllCategories(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Field to sort by", example = "sortOrder", schema = @Schema(allowableValues = {"name", "sortOrder", "createdAt"}))
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @Parameter(description = "Sort direction", example = "asc", schema = @Schema(allowableValues = {"asc", "desc"}))
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
    @Operation(summary = "Get all active categories", description = "Retrieve a list of categories that are currently active.")
    @ApiResponse(responseCode = "200", description = "Active categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class)))
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
    @Operation(summary = "Get category by ID", description = "Retrieve a single category by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Category found successfully",
            content = @Content(schema = @Schema(implementation = Map.class)))
    @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> getCategoryById(@Parameter(description = "Category ID", example = "1", required = true) @PathVariable Long id) {
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
    @Operation(summary = "Get category by slug", description = "Retrieve a single category by its URL-friendly slug.")
    @ApiResponse(responseCode = "200", description = "Category found successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<?> getCategoryBySlug(@Parameter(description = "Category slug", example = "electronics", required = true) @PathVariable String slug) {
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
    @Operation(summary = "Search for categories", description = "Search for categories by name or description.")
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<?> searchCategories(
            @Parameter(description = "Search query for category name or description", example = "gadgets", required = true)
            @RequestParam String q,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of items per page", example = "10")
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