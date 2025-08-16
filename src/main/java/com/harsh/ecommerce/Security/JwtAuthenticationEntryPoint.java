package com.harsh.ecommerce.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication required to access this resource");
        errorResponse.put("path", request.getRequestURI());

        String errorDetail = getErrorDetail(authException);
        errorResponse.put("detail", errorDetail);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private String getErrorDetail(AuthenticationException authException) {
        String message = authException.getMessage();

        if (message.contains("JWT")) {
            return "Invalid or expired JWT token";
        } else if (message.contains("expired")) {
            return "JWT token has expired";
        } else if (message.contains("malformed")) {
            return "Malformed JWT token";
        } else if (message.contains("signature")) {
            return "JWT signature verification failed";
        } else {
            return "Please provide a valid JWT token in the Authorization header";
        }
    }
}
