package com.mediahub.iam.security;

import com.mediahub.iam.entity.User;
import com.mediahub.iam.repository.UserRepository;
import io.jsonwebtoken.Claims;
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

    // Collaborators: JwtUtil validates/parses the token, UserRepository loads the user to verify session state.
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // Constructor injection of the two dependencies (wired up where the filter is registered).
    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
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
        // if (path.contains("/auth/register") ||
        //     path.contains("/auth/login")    ||
        //     path.contains("/auth/refreshToken")) {
        //     filterChain.doFilter(request, response);
        //     return;
        // }
        // Auth endpoints (login/register/refresh) are public, so bypass token checks and continue the chain.
        if (path.contains("/auth/")) {
           filterChain.doFilter(request, response);
           return;
              }

        // Allow actuator health endpoints without a JWT header.
        if (path.equals("/actuator/health") || path.startsWith("/actuator/health/") || path.equals("/actuator/info")) {
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
            // Validate/parse the token and pull the userId claim out of it.
            Claims claims = jwtUtil.validateToken(token);
            Long userId = claims.get("userId", Long.class);

            // Load the user referenced by the token (null if no such user exists).
            User user = userRepository.findById(userId).orElse(null);

            // Reject if the token points to a user that no longer exists.
            if (user == null) {
                sendError(response, 401, "UNAUTHORIZED",
                    "User not found.");
                return;
            }
            // Reject if the user's session has been explicitly revoked (e.g. logout/admin action).
            if (user.getIsRevoked()) {
                sendError(response, 401, "TOKEN_REVOKED",
                    "Session revoked. Please login again.");
                return;
            }
            // Reject if there is no stored token hash, meaning the user has no active session.
            if (user.getTokenHash() == null) {
                sendError(response, 401, "NO_ACTIVE_SESSION",
                    "No active session. Please login.");
                return;
            }

            // ── Set authentication in Spring Security context ─────────────────
            // Build authorities: include ROLE_<roleType> and any permission strings embedded in the token.
            String roleType = claims.get("roleType", String.class);
            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            if (roleType != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType.toUpperCase()));
            }

            Object permsObj = claims.get("permissions");
            if (permsObj instanceof java.util.Collection) {
                for (Object p : (java.util.Collection<?>) permsObj) {
                    if (p != null) authorities.add(new SimpleGrantedAuthority(p.toString()));
                }
            }

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            // Authentication succeeded — pass the request on to the rest of the filter chain.
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // Any parsing/validation failure (bad signature, expired, etc.) results in a 401 response.
            sendError(response, 401, "TOKEN_INVALID",
                "Token is invalid or expired. Please login again.");
        }
    }

    // Helper that writes a standardized JSON error body with the given HTTP status, error code, and message.
    // Used by all the rejection paths above to keep the unauthorized responses consistent.
    private void sendError(HttpServletResponse res,
                           int status, String code, String msg)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write(
            "{\"status\":\"error\",\"code\":\"" + code +
            "\",\"message\":\"" + msg + "\"}");
    }
}