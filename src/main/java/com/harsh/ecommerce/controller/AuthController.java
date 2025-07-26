package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.*;
import com.harsh.ecommerce.entity.Role;
import com.harsh.ecommerce.entity.User;
import com.harsh.ecommerce.repository.UserRepository;
import com.harsh.ecommerce.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use!"));
            }

            User user = new User(
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getEmail(),
                    passwordEncoder.encode(registerRequest.getPassword())
            );

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(
                    "User registered successfully! You can now log in.",
                    savedUser.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String jwt = jwtUtil.generateToken(userDetails);

            JwtResponse jwtResponse = new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Login successful!",
                    jwtResponse
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or password!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Admin email already exists!"));
            }

            User admin = new User(
                    registerRequest.getFirstName(),
                    registerRequest.getLastName(),
                    registerRequest.getEmail(),
                    passwordEncoder.encode(registerRequest.getPassword()),
                    Role.ADMIN
            );

            User savedAdmin = userRepository.save(admin);

            return ResponseEntity.ok(ApiResponse.success(
                    "Admin user created successfully!",
                    savedAdmin.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Admin creation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtUtil.validateToken(token);

            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok(ApiResponse.success(
                        "Token is valid",
                        username
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token validation failed: " + e.getMessage()));
        }
    }
}
