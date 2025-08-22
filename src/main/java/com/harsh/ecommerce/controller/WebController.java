package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.service.CategoryService;
import com.harsh.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@CrossOrigin(origins = "*")
@Hidden // Hide this controller from Swagger documentation
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Web", description = "Endpoints for server-side rendered pages")
public class WebController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String home(Model model) {
        try {
            model.addAttribute("categories", categoryService.getAllCategories());
        } catch (Exception e) {
            log.error("Error loading home page", e);
        }
        return "index"; // directly returns index.html
    }

    @GetMapping("/products")
    public String products(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) Long categoryId) {
        try {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("currentSearch", search);
            model.addAttribute("currentCategoryId", categoryId);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            log.error("Error loading products page", e);
        }
        return "products"; // directly returns products.html
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("productId", id);
        } catch (Exception e) {
            log.error("Error loading product detail page for ID: " + id, e);
            return "redirect:/products";
        }
        return "product-detail"; // directly returns product-detail.html
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login"; // directly returns login.html
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register"; // directly returns register.html
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        return "cart"; // directly returns cart.html
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        return "wishlist"; // directly returns wishlist.html
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        return "checkout"; // directly returns checkout.html
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        return "orders"; // directly returns orders.html
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        return "profile"; // directly returns profile.html
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        return "admin-dashboard"; // directly returns admin-dashboard.html
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        return "admin-products"; // directly returns admin-products.html
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        return "admin-orders"; // directly returns admin-orders.html
    }

    @GetMapping("/admin/categories")
    public String adminCategories(Model model) {
        return "admin-categories"; // directly returns admin-categories.html
    }
}
