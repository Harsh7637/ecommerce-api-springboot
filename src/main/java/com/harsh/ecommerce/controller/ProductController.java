package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.ApiResponse;
import com.harsh.ecommerce.dto.ProductFilterDto;
import com.harsh.ecommerce.dto.ProductResponseDto;
import com.harsh.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "🌐 Public - Products", description = "Public product browsing and search")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(
            summary = "Get all products with filtering",
            description = "Retrieve a paginated list of products with advanced filtering options including search, category, price range, and sorting"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                    {
                        "products": [],
                        "currentPage": 0,
                        "totalItems": 50,
                        "totalPages": 5,
                        "hasNext": true,
                        "hasPrevious": false,
                        "success": true
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter parameters",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> getProducts(
            @Parameter(description = "Search keyword for product name or description", example = "smartphone")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by category ID", example = "1")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Minimum price filter", example = "100.00")
            @RequestParam(required = false) String minPrice,

            @Parameter(description = "Maximum price filter", example = "1000.00")
            @RequestParam(required = false) String maxPrice,

            @Parameter(description = "Filter by stock availability", example = "true")
            @RequestParam(required = false) Boolean inStock,

            @Parameter(description = "Filter by featured products", example = "true")
            @RequestParam(required = false) Boolean featured,

            @Parameter(description = "Sort field", example = "price", schema = @Schema(allowableValues = {"name", "price", "createdAt", "stockQuantity"}))
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction", example = "desc", schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "asc") String sortDir,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Number of items per page", example = "12")
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
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve detailed information about a specific product using its unique identifier"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Product Details",
                                    value = """
                    {
                        "product": {
                            "id": 1,
                            "name": "Google Nest Hub Max",
                            "description": "AI-powered smart display",
                            "price": 1299.99,
                            "stockQuantity": 25,
                            "categoryName": "AI Technology"
                        },
                        "success": true
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable Long id) {
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
    @Operation(
            summary = "Get product by slug",
            description = "Retrieve product information using its URL-friendly slug identifier"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Product found successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    public ResponseEntity<?> getProductBySlug(
            @Parameter(description = "Product slug", example = "google-nest-hub-max", required = true)
            @PathVariable String slug) {
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
    @Operation(
            summary = "Get products by category",
            description = "Retrieve all products belonging to a specific category with pagination"
    )
    public ResponseEntity<?> getProductsByCategory(
            @Parameter(description = "Category ID", example = "1", required = true)
            @PathVariable Long categoryId,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size", example = "12")
            @RequestParam(defaultValue = "12") Integer size,

            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction", example = "asc")
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
    @Operation(
            summary = "Get featured products",
            description = "Retrieve a list of products marked as featured"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Featured products retrieved successfully"
            )
    })
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
    @Operation(
            summary = "Get latest products",
            description = "Retrieve the most recently added products with pagination"
    )
    public ResponseEntity<?> getLatestProducts(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size", example = "8")
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
    @Operation(
            summary = "Search products",
            description = "Search for products using keywords with advanced filtering and sorting options"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Search Results",
                                    value = """
                    {
                        "products": [],
                        "searchTerm": "smart speaker",
                        "currentPage": 0,
                        "totalItems": 15,
                        "totalPages": 2,
                        "success": true
                    }
                    """
                            )
                    )
            )
    })
    public ResponseEntity<?> searchProducts(
            @Parameter(description = "Search query", example = "smart speaker", required = true)
            @RequestParam String q,

            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Page size", example = "12")
            @RequestParam(defaultValue = "12") Integer size,

            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Sort direction", example = "asc")
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