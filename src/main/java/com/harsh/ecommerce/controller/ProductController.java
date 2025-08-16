package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ProductFilterDto;
import com.harsh.ecommerce.dto.ProductResponseDto;
import com.harsh.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {

        try {
            ProductFilterDto filterDto = new ProductFilterDto();
            filterDto.setSearch(search);
            filterDto.setCategoryId(categoryId);
            filterDto.setMinPrice(minPrice != null ? new java.math.BigDecimal(minPrice) : null);
            filterDto.setMaxPrice(maxPrice != null ? new java.math.BigDecimal(maxPrice) : null);
            filterDto.setInStock(inStock);
            filterDto.setFeatured(featured);
            filterDto.setSortBy(sortBy);
            filterDto.setSortDir(sortDir);
            filterDto.setPage(page);
            filterDto.setSize(size);

            Page<ProductResponseDto> products = productService.getProducts(filterDto);

            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("hasNext", products.hasNext());
            response.put("hasPrevious", products.hasPrevious());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to fetch products: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductResponseDto product = productService.getProductById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        try {
            ProductResponseDto product = productService.getProductBySlug(slug);

            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    org.springframework.data.domain.Sort.by(sortBy).descending() :
                    org.springframework.data.domain.Sort.by(sortBy).ascending();

            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(page, size, sort);

            Page<ProductResponseDto> products = productService.getProductsByCategory(categoryId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts() {
        try {
            List<ProductResponseDto> products = productService.getFeaturedProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("count", products.size());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "8") Integer size) {

        try {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(page, size);

            Page<ProductResponseDto> products = productService.getLatestProducts(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            ProductFilterDto filterDto = new ProductFilterDto();
            filterDto.setSearch(q);
            filterDto.setSortBy(sortBy);
            filterDto.setSortDir(sortDir);
            filterDto.setPage(page);
            filterDto.setSize(size);

            Page<ProductResponseDto> products = productService.getProducts(filterDto);

            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("searchTerm", q);
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
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
