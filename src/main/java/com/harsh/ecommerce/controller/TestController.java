package com.harsh.ecommerce.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Hidden // This annotation hides the entire controller from Swagger documentation
@Tag(name = "Test", description = "Endpoints for testing API functionality")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! Your Spring Boot E-Commerce API is working! �";
    }

    @GetMapping("/status")
    public String status() {
        return "Database connected, API running on port 8080";
    }
}
