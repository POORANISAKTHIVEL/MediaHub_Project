package com.mediahub.licensing.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

// Spring Security filter that runs once per request to authenticate callers via their JWT.
// Extends OncePerRequestFilter so the token check executes exactly once on each incoming request.
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Core filter logic invoked for every request; decides whether to let the request through.
    // On success it populates the SecurityContext; on any failure it writes a 401 JSON error and stops.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Capture the requested URI so we can decide whether the path needs authentication.
        String path = request.getRequestURI();

        // ── Skip filter for public endpoints ──────────────────────────────────
        // Licensing endpoints with public access can be added here
        if (path.contains("/actuator") || path.contains("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Read Authorization header ─────────────────────────────────────────
        // Require an "Authorization: Bearer <token>" header; reject with 401 if it's missing or malformed.
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "UNAUTHORIZED",
                    "No token provided. Please login first.");
            return;
        }

        // Strip the "Bearer " prefix (7 chars) to get the raw JWT string.
        String token = authHeader.substring(7);

        try {
            // Validate the token signature, expiration, and issuer.
            // If valid, extract claims from the payload (userId, roleType, etc.).
            Long userId = jwtUtil.extractUserId(token);
            String roleType = jwtUtil.extractRoleType(token);

            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + (roleType != null ? roleType : "USER")));

            Object permsObj = jwtUtil.extractClaim(token, "permissions");
            if (permsObj instanceof java.util.Collection) {
                for (Object p : (java.util.Collection<?>) permsObj) {
                    if (p != null) {
                        authorities.add(new SimpleGrantedAuthority(p.toString()));
                    }
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Authentication succeeded — pass the request on to the rest of the filter chain.
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // Any parsing/validation failure (bad signature, expired, invalid issuer, etc.) results in a 401 response.
            sendError(response, 401, "TOKEN_INVALID",
                    "Token is invalid or expired. Please login again.");
        }
    }

    // Helper method to send a JSON error response.
    private void sendError(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                status, error, message, System.currentTimeMillis()));
    }
}
