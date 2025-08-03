package com.harsh.ecommerce.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class CloudinaryDebugController {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @GetMapping("/cloudinary-config")
    public ResponseEntity<?> checkCloudinaryConfig() {
        Map<String, Object> response = new HashMap<>();

        response.put("cloudName", cloudName != null ? cloudName : "NOT SET");
        response.put("apiKey", apiKey != null ? apiKey : "NOT SET");
        response.put("apiSecret", apiSecret != null ? (apiSecret.substring(0, 4) + "****") : "NOT SET");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}