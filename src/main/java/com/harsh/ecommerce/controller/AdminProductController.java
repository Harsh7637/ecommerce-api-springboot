package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ProductCreateDto;
import com.harsh.ecommerce.dto.ProductFilterDto;
import com.harsh.ecommerce.dto.ProductResponseDto;
import com.harsh.ecommerce.service.CloudinaryService;
import com.harsh.ecommerce.service.ProductService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")

public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductCreateDto productDto) {
        try {
            ProductResponseDto product = productService.createProduct(productDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product created successfully");
            response.put("product", product);
            response.put("success", true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProductsForAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ProductResponseDto> products;
            if (search != null || categoryId != null || minPrice != null || maxPrice != null) {
                ProductFilterDto filterDto = new ProductFilterDto();
                filterDto.setSearch(search);
                filterDto.setCategoryId(categoryId);
                filterDto.setMinPrice(minPrice != null ? new BigDecimal(minPrice) : null);
                filterDto.setMaxPrice(maxPrice != null ? new BigDecimal(maxPrice) : null);
                filterDto.setFeatured(isFeatured);
                filterDto.setSortBy(sortBy);
                filterDto.setSortDir(sortDir);
                filterDto.setPage(page);
                filterDto.setSize(size);
                products = productService.getProducts(filterDto);
            } else {
                products = productService.getAllProducts(pageable);
            }
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
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductCreateDto productDto) {
        try {
            ProductResponseDto product = productService.updateProduct(id, productDto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product updated successfully");
            response.put("product", product);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product deleted successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/bulk-update-status")
    public ResponseEntity<?> bulkUpdateStatus(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");
            Boolean isActive = (Boolean) request.get("isActive");
            int updatedCount = 0;
            for (Long productId : productIds) {
                try {
                    ProductResponseDto product = productService.getProductById(productId);
                    ProductCreateDto updateDto = createUpdateDto(product);
                    updateDto.setIsActive(isActive);
                    productService.updateProduct(productId, updateDto);
                    updatedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to update product " + productId + ": " + e.getMessage());
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk update completed");
            response.put("updatedCount", updatedCount);
            response.put("totalRequested", productIds.size());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/bulk-update-featured")
    public ResponseEntity<?> bulkUpdateFeatured(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");
            Boolean isFeatured = (Boolean) request.get("isFeatured");
            int updatedCount = 0;
            for (Long productId : productIds) {
                try {
                    ProductResponseDto product = productService.getProductById(productId);
                    ProductCreateDto updateDto = createUpdateDto(product);
                    updateDto.setIsFeatured(isFeatured);
                    productService.updateProduct(productId, updateDto);
                    updatedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to update product " + productId + ": " + e.getMessage());
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk featured update completed");
            response.put("updatedCount", updatedCount);
            response.put("totalRequested", productIds.size());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id,
                                         @RequestBody Map<String, Integer> request) {
        try {
            Integer newStock = request.get("stockQuantity");
            if (newStock == null || newStock < 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid stock quantity"));
            }
            productService.updateStock(id, newStock);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stock updated successfully");
            response.put("newStock", newStock);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts(@RequestParam(defaultValue = "5") Integer threshold) {
        try {
            List<ProductResponseDto> products = productService.getLowStockProducts(threshold);
            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("threshold", threshold);
            response.put("count", products.size());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<?> getOutOfStockProducts() {
        try {
            List<ProductResponseDto> products = productService.getOutOfStockProducts();
            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("count", products.size());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getProductAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("totalProducts", productService.getTotalActiveProducts());
            analytics.put("averagePrice", productService.getAveragePrice());
            analytics.put("totalStock", productService.getTotalStock());
            analytics.put("lowStockCount", productService.getLowStockProducts(5).size());
            analytics.put("outOfStockCount", productService.getOutOfStockProducts().size());
            Map<String, Object> response = new HashMap<>();
            response.put("analytics", analytics);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadProductImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please upload a valid image file"));
            }
            String imageUrl = cloudinaryService.uploadProductImage(file);
            ProductResponseDto updatedProduct = productService.updateProductImage(id, imageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("product", updatedProduct);
            response.put("imageUrl", imageUrl);
            response.put("success", true);
            response.put("message", "Product image uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImageStandalone(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }
            if (!isImageFile(file)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please upload a valid image file"));
            }
            String imageUrl = cloudinaryService.uploadProductImage(file);
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
    public ResponseEntity<?> deleteProductImage(@RequestParam("imageUrl") String imageUrl) {
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

    private ProductCreateDto createUpdateDto(ProductResponseDto product) {
        ProductCreateDto dto = new ProductCreateDto();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategoryId());
        dto.setSku(product.getSku());
        dto.setImages(product.getImages());
        dto.setWeight(product.getWeight());
        dto.setDimensions(product.getDimensions());
        dto.setIsActive(product.getIsActive());
        dto.setIsFeatured(product.getIsFeatured());
        dto.setSortOrder(product.getSortOrder());
        return dto;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
