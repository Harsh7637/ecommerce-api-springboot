package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
@Tag(name = "📁 File Management", description = "File upload operations")
public class FileUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload/product-image")
    @Operation(summary = "Upload a product image", description = "Uploads an image file to be used for a product. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
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
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/category-image")
    @Operation(summary = "Upload a category image", description = "Uploads an image file to be used for a category. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadCategoryImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
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
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete-image")
    @Operation(summary = "Delete an image", description = "Deletes an image from cloud storage using its URL. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image URL or delete failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "URL of the image to delete", required = true, example = "http://example.com/image.jpg")
            @RequestParam("imageUrl") String imageUrl) {
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