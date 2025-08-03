package com.harsh.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // This tells Spring: "This class handles HTTP requests"
@RequestMapping("/api/test")  // Base URL for all endpoints in this controller
public class TestController {

    @GetMapping("/hello")  // Handles GET requests to /api/test/hello
    public String hello() {
        return "Hello! Your Spring Boot E-Commerce API is working! 🚀";
    }

    @GetMapping("/status")  // Another endpoint: /api/test/status
    public String status() {
        return "Database connected, API running on port 8080";
    }
}
