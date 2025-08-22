package com.harsh.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;

    @Value("${app.openapi.prod-url:https://your-production-url.com}")
    private String prodUrl;

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("harsh@ecommerce.com");
        contact.setName("Harsh");
        contact.setUrl("https://github.com/harsh");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("E-Commerce API Documentation")
                .version("1.0.0")
                .contact(contact)
                .description("Comprehensive E-Commerce REST API with organized endpoints:\n\n" +
                        "## 📱 **Public APIs** (No Authentication Required)\n" +
                        "• Browse products, categories, and reviews\n" +
                        "• Search and filter functionality\n" +
                        "• Product details and featured items\n\n" +
                        "## 🔐 **Authentication APIs**\n" +
                        "• User registration and login\n" +
                        "• JWT token management\n" +
                        "• Password management\n\n" +
                        "## 👤 **User APIs** (Authentication Required)\n" +
                        "• Shopping cart management\n" +
                        "• Order processing and history\n" +
                        "• Wishlist functionality\n" +
                        "• Payment processing\n" +
                        "• Product reviews\n\n" +
                        "## 👨‍💼 **Admin APIs** (Admin Role Required)\n" +
                        "• Product and category management\n" +
                        "• Order administration\n" +
                        "• User management\n" +
                        "• Analytics and reporting\n" +
                        "• Payment administration\n\n" +
                        "Built with Spring Boot 3.x, PostgreSQL, Redis, Stripe, and JWT security.")
                .termsOfService("https://your-terms-of-service.com")
                .license(mitLicense);

        // JWT Security Scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter JWT Bearer token in format: Bearer <token>");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("Bearer Authentication", securityScheme))
                .tags(List.of(
                        new Tag().name("🌐 Public - Products").description("Public product browsing and search"),
                        new Tag().name("🌐 Public - Categories").description("Public category browsing"),
                        new Tag().name("🌐 Public - Reviews").description("Public product reviews"),
                        new Tag().name("🔐 Authentication").description("User registration, login, and token management"),
                        new Tag().name("👤 User - Cart").description("Shopping cart operations (requires authentication)"),
                        new Tag().name("👤 User - Orders").description("Order management (requires authentication)"),
                        new Tag().name("👤 User - Payments").description("Payment processing (requires authentication)"),
                        new Tag().name("👤 User - Wishlist").description("Wishlist management (requires authentication)"),
                        new Tag().name("👤 User - Profile").description("User profile management (requires authentication)"),
                        new Tag().name("👨‍💼 Admin - Products").description("Product management (admin only)"),
                        new Tag().name("👨‍💼 Admin - Categories").description("Category management (admin only)"),
                        new Tag().name("👨‍💼 Admin - Orders").description("Order administration (admin only)"),
                        new Tag().name("👨‍💼 Admin - Users").description("User management (admin only)"),
                        new Tag().name("👨‍💼 Admin - Payments").description("Payment administration (admin only)"),
                        new Tag().name("👨‍💼 Admin - Reviews").description("Review moderation (admin only)"),
                        new Tag().name("📁 File Management").description("File upload operations"),
                        new Tag().name("🔗 Webhooks").description("External service webhooks (internal use)")
                ));
    }
}