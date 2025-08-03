package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class SimpleCloudinaryTestController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/cloudinary-upload")
    public ResponseEntity<?> testCloudinaryUpload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            String imageUrl = cloudinaryService.uploadProductImage(file);

            response.put("success", true);
            response.put("message", "Image uploaded successfully");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }
}