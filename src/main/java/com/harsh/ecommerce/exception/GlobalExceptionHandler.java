package com.harsh.ecommerce.exception;

import com.harsh.ecommerce.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ✅ NEW: Handle Spring Security 6.x Authorization Denied Exception
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDenied(AuthorizationDeniedException ex, WebRequest request) {
        logger.warn("Authorization denied: {} for request: {}", ex.getMessage(), request.getDescription(false));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access denied. You don't have permission to access this resource.");
        response.put("success", false);
        response.put("error", "FORBIDDEN");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ✅ NEW: Handle Legacy Spring Security Access Denied Exception (for backward compatibility)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {} for request: {}", ex.getMessage(), request.getDescription(false));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access denied. You don't have permission to access this resource.");
        response.put("success", false);
        response.put("error", "FORBIDDEN");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ✅ NEW: Handle Authentication Exception (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {} for request: {}", ex.getMessage(), request.getDescription(false));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentication failed. Please log in.");
        response.put("success", false);
        response.put("error", "UNAUTHORIZED");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ✅ EXISTING: Your original handlers below
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("success", false);
        response.put("error", "USER_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("success", false);
        response.put("error", "EMAIL_ALREADY_EXISTS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> handleInvalidPasswordException(InvalidPasswordException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("success", false);
        response.put("error", "INVALID_PASSWORD");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("success", false);
        response.put("error", "VALIDATION_ERROR");

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentNotFoundException(PaymentNotFoundException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handlePaymentProcessingException(PaymentProcessingException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ✅ UPDATED: Enhanced generic exception handler with logging
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected exception: {} for request: {}", ex.getMessage(), request.getDescription(false), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "An unexpected error occurred");
        response.put("success", false);
        response.put("error", "INTERNAL_SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}