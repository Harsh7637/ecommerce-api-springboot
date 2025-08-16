package com.harsh.ecommerce.service;

import com.harsh.ecommerce.dto.ProductCreateDto;
import com.harsh.ecommerce.dto.ProductFilterDto;
import com.harsh.ecommerce.dto.ProductResponseDto;
import com.harsh.ecommerce.entity.Category;
import com.harsh.ecommerce.entity.Product;
import com.harsh.ecommerce.exception.CategoryNotFoundException;
import com.harsh.ecommerce.exception.InsufficientStockException;
import com.harsh.ecommerce.exception.ProductNotFoundException;
import com.harsh.ecommerce.repository.CategoryRepository;
import com.harsh.ecommerce.repository.ProductRepository;
import com.harsh.ecommerce.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public ProductResponseDto createProduct(ProductCreateDto productDto) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDto.getCategoryId()));

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setCategory(category);
        product.setSku(productDto.getSku());
        product.setImages(productDto.getImages());
        product.setWeight(productDto.getWeight());
        product.setDimensions(productDto.getDimensions());
        product.setIsActive(productDto.getIsActive());
        product.setIsFeatured(productDto.getIsFeatured());
        product.setSortOrder(productDto.getSortOrder());

        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with slug: " + slug));
        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductResponseDto::new);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(ProductFilterDto filterDto) {
        Sort sort = createSort(filterDto.getSortBy(), filterDto.getSortDir());
        Pageable pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize(), sort);

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (filterDto.getSearch() != null && !filterDto.getSearch().trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasNameOrDescriptionLike(filterDto.getSearch()));
        }

        if (filterDto.getCategoryId() != null) {
            spec = spec.and(ProductSpecification.hasCategoryId(filterDto.getCategoryId()));
        }

        if (filterDto.getMinPrice() != null) {
            spec = spec.and(ProductSpecification.hasPriceGreaterThanOrEqual(filterDto.getMinPrice()));
        }

        if (filterDto.getMaxPrice() != null) {
            spec = spec.and(ProductSpecification.hasPriceLessThanOrEqual(filterDto.getMaxPrice()));
        }

        if (filterDto.getInStock() != null && filterDto.getInStock()) {
            spec = spec.and(ProductSpecification.hasStockGreaterThan(0));
        }

        if (filterDto.getFeatured() != null && filterDto.getFeatured()) {
            spec = spec.and(ProductSpecification.isFeatured());
        }

        spec = spec.and(ProductSpecification.isActive());

        return productRepository.findAll(spec, pageable)
                .map(ProductResponseDto::new);
    }

    public ProductResponseDto updateProduct(Long id, ProductCreateDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + productDto.getCategoryId()));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setStockQuantity(productDto.getStockQuantity());
        existingProduct.setCategory(category);
        existingProduct.setSku(productDto.getSku());
        existingProduct.setImages(productDto.getImages());
        existingProduct.setWeight(productDto.getWeight());
        existingProduct.setDimensions(productDto.getDimensions());
        existingProduct.setIsActive(productDto.getIsActive());
        existingProduct.setIsFeatured(productDto.getIsFeatured());
        existingProduct.setSortOrder(productDto.getSortOrder());

        Product updatedProduct = productRepository.save(existingProduct);
        return new ProductResponseDto(updatedProduct);
    }

    // ✅ NEW METHOD: Update product image
    public ProductResponseDto updateProductImage(Long id, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        List<String> currentImages = product.getImages();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
        }
        currentImages.add(imageUrl);

        product.setImages(currentImages);
        Product updatedProduct = productRepository.save(product);
        return new ProductResponseDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(ProductResponseDto::new);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getLatestProducts(Pageable pageable) {
        return productRepository.findLatestProducts(pageable)
                .map(ProductResponseDto::new);
    }

    public void updateStock(Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    public void reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " +
                    product.getStockQuantity() + ", Required: " + quantity);
        }

        product.reduceStock(quantity);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts()
                .stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }

    @Transactional(readOnly = true)
    public long getTotalActiveProducts() {
        return productRepository.countActiveProducts();
    }

    @Transactional(readOnly = true)
    public BigDecimal getAveragePrice() {
        return productRepository.getAveragePrice();
    }

    @Transactional(readOnly = true)
    public Long getTotalStock() {
        return productRepository.getTotalStock();
    }
}
