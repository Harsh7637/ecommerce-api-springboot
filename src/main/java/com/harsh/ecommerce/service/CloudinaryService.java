package com.harsh.ecommerce.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        // Validate that properties are loaded
        if (cloudName == null || cloudName.trim().isEmpty()) {
            throw new RuntimeException("Cloudinary cloud-name is not configured");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("Cloudinary api-key is not configured");
        }
        if (apiSecret == null || apiSecret.trim().isEmpty()) {
            throw new RuntimeException("Cloudinary api-secret is not configured");
        }

        // Initialize Cloudinary
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty or null");
        }

        if (cloudinary == null) {
            throw new RuntimeException("Cloudinary is not initialized");
        }

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    public String uploadProductImage(MultipartFile file) {
        return uploadImage(file, "ecommerce/products");
    }

    public String uploadCategoryImage(MultipartFile file) {
        return uploadImage(file, "ecommerce/categories");
    }

    public void deleteImage(String publicId) {
        if (cloudinary == null) {
            throw new RuntimeException("Cloudinary is not initialized");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        String[] parts = imageUrl.split("/");
        if (parts.length < 2) {
            return null;
        }

        String filename = parts[parts.length - 1];
        return filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
    }
}