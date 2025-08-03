    package com.harsh.ecommerce.controller;

    import com.harsh.ecommerce.dto.UserRegistrationDto;
    import com.harsh.ecommerce.dto.UserResponseDto;
    import com.harsh.ecommerce.entity.Role;
    import com.harsh.ecommerce.service.UserService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/users")
    @CrossOrigin(origins = "*")
    public class UserController {

        @Autowired
        private UserService userService;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getAllUsers(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "createdAt") String sortBy,
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getUserById(@PathVariable Long id) {
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> searchUsers(
                @RequestParam String q,
                @RequestParam(defaultValue = "0") int page,
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getUsersByRole(
                @PathVariable Role role,
                @RequestParam(defaultValue = "0") int page,
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRegistrationDto updateDto) {
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> activateUser(@PathVariable Long id) {
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getInactiveUsers(@RequestParam(defaultValue = "30") int days) {
            List<UserResponseDto> inactiveUsers = userService.getInactiveUsers(days);

            Map<String, Object> response = new HashMap<>();
            response.put("inactiveUsers", inactiveUsers);
            response.put("daysInactive", days);
            response.put("count", inactiveUsers.size());
            response.put("success", true);

            return ResponseEntity.ok(response);
        }
    }