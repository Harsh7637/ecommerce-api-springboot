package com.harsh.ecommerce.Security;

import com.harsh.ecommerce.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    // List of paths that don't require JWT authentication
    private final List<String> publicPaths = Arrays.asList(
            // Auth endpoints
            "/api/auth/login",
            "/api/auth/register",
            "/api/webhooks/",

            // Public API endpoints
            "/api/products",
            "/api/categories",
            "/api/test",

            // Frontend HTML pages (served by WebController)
            "/",
            "/home",
            "/login",
            "/register",
            "/products",
            "/product/",

            // Static resources
            "/css/",
            "/js/",
            "/images/",
            "/webjars/",

            // Swagger
            "/swagger-ui",
            "/v3/api-docs",

            // H2 Console
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        logger.info("=== JWT Filter Processing: {} {} ===", request.getMethod(), path);

        // ✅ Skip JWT check for public paths
        if (shouldSkipJwtValidation(path)) {
            logger.info("✅ Skipping JWT validation for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("🔒 JWT validation required for path: {}", path);

        try {
            String jwt = parseJwt(request);
            logger.info("JWT Token extracted: {}", jwt != null ? "YES (length: " + jwt.length() + ")" : "NO");

            if (jwt != null) {
                logger.info("Validating JWT token...");
                boolean isValidToken = jwtUtil.validateToken(jwt);
                logger.info("JWT token valid: {}", isValidToken);

                if (isValidToken) {
                    String username = jwtUtil.getUsernameFromToken(jwt);
                    logger.info("Username extracted from token: {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        logger.info("Loading user details for: {}", username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        logger.info("User details loaded successfully: {}", userDetails.getUsername());

                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("✅ Authentication set successfully for user: {}", username);
                        } else {
                            logger.warn("❌ Token validation with UserDetails failed for user: {}", username);
                        }
                    } else if (username == null) {
                        logger.warn("❌ Username is null from token");
                    } else {
                        logger.info("Authentication already exists in SecurityContext");
                    }
                } else {
                    logger.warn("❌ JWT token validation failed");
                }
            } else {
                logger.warn("❌ No JWT token found in request for protected path: {}", path);
            }
        } catch (Exception e) {
            logger.error("❌ Error in JWT authentication: {}", e.getMessage(), e);
        }

        // Check final authentication status
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        logger.info("Final authentication status: {}", isAuthenticated ? "AUTHENTICATED" : "NOT AUTHENTICATED");

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path should skip JWT validation
     */
    private boolean shouldSkipJwtValidation(String path) {
        return publicPaths.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/")) {
                return path.startsWith(publicPath);
            } else {
                return path.equals(publicPath) || path.startsWith(publicPath + "/");
            }
        });
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.info("Authorization header present: {}", headerAuth != null);

        if (headerAuth != null) {
            logger.info("Authorization header value: {}", headerAuth.startsWith("Bearer ") ? "Bearer [REDACTED]" : headerAuth);
        }

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.info("Extracted token length: {}", token.length());
            return token;
        }

        logger.warn("No valid Authorization header found");
        return null;
    }
}