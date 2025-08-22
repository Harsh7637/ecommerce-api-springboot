package com.harsh.ecommerce.controller;

import com.harsh.ecommerce.dto.PasswordChangeDto;
import com.harsh.ecommerce.dto.UserProfileUpdateDto;
import com.harsh.ecommerce.dto.UserRegistrationDto;
import com.harsh.ecommerce.dto.UserResponseDto;
import com.harsh.ecommerce.entity.Role;
import com.harsh.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "👤 User - Profile", description = "User profile management (requires authentication)")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile details of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponseDto user = userService.getUserByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Updates the profile details for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCurrentUserProfile(
            @RequestBody UserProfileUpdateDto updateDto,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponseDto user = userService.getUserByEmail(email);
            UserResponseDto updatedUser = userService.updateUserProfile(user.getId(), updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/profile/password")
    @Operation(summary = "Change password", description = "Allows the authenticated user to change their password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid old password or other error")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeDto passwordChangeDto,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponseDto user = userService.getUserByEmail(email);
            userService.changePassword(user.getId(), passwordChangeDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin)", description = "Retrieves a paginated list of all registered users. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponseDto> users = userService.getAllUsers(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (Admin)", description = "Retrieves a single user by their ID. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        try {
            UserResponseDto user = userService.getUserById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search users (Admin)", description = "Searches for users by name or email. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchUsers(
            @Parameter(description = "Search query", example = "john doe")
            @RequestParam String q,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.searchUsers(q, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        response.put("searchTerm", q);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role (Admin)", description = "Retrieves a paginated list of users filtered by their role. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(
            @Parameter(description = "User role", example = "ADMIN", schema = @Schema(implementation = Role.class))
            @PathVariable Role role,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.getUsersByRole(role, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("users", users.getContent());
        response.put("role", role);
        response.put("currentPage", users.getNumber());
        response.put("totalItems", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details by ID (Admin)", description = "Updates a user's details. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@Parameter(description = "User ID", example = "1") @PathVariable Long id, @RequestBody UserRegistrationDto updateDto) {
        try {
            UserResponseDto updatedUser = userService.updateUser(id, updateDto);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            response.put("user", updatedUser);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role by ID (Admin)", description = "Changes a user's role. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid role or other error"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@Parameter(description = "User ID", example = "1") @PathVariable Long id, @Parameter(description = "New role", example = "ADMIN", schema = @Schema(implementation = Role.class)) @RequestParam Role role) {
        try {
            UserResponseDto updatedUser = userService.updateUserRole(id, role);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            response.put("user", updatedUser);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user (Admin)", description = "Marks a user as inactive. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "User is already deactivated or other error"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        try {
            userService.deactivateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deactivated successfully");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate a user (Admin)", description = "Activates an inactive user. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "User is already active or other error"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        try {
            userService.activateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User activated successfully");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics (Admin)", description = "Retrieves various statistics about the user base. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("totalAdmins", userService.getUserCountByRole(Role.ADMIN));
        stats.put("totalRegularUsers", userService.getUserCountByRole(Role.USER));
        stats.put("newUsersThisWeek", userService.getNewUsersCount(LocalDateTime.now().minusDays(7)));
        stats.put("newUsersThisMonth", userService.getNewUsersCount(LocalDateTime.now().minusDays(30)));

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", stats);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users (Admin)", description = "Retrieves a list of users who have been inactive for a specified number of days. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inactive users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getInactiveUsers(@Parameter(description = "Number of days for inactivity", example = "30") @RequestParam(defaultValue = "30") int days) {
        List<UserResponseDto> inactiveUsers = userService.getInactiveUsers(days);

        Map<String, Object> response = new HashMap<>();
        response.put("inactiveUsers", inactiveUsers);
        response.put("daysInactive", days);
        response.put("count", inactiveUsers.size());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}