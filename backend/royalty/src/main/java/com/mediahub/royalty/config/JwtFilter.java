package com.mediahub.royalty.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validateToken(token);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String roleType = claims.get("roleType", String.class);
            if (roleType != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType.toUpperCase()));
            }

            Object perms = claims.get("permissions");
            if (perms instanceof java.util.Collection) {
                for (Object p : (java.util.Collection<?>) perms) {
                    if (p != null) {
                        authorities.add(new SimpleGrantedAuthority(p.toString()));
                    }
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("JWT valid for user {}", claims.getSubject());
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            logger.error("Invalid JWT token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"TOKEN_INVALID\",\"message\":\"Token is invalid or expired\"}");
        }
    }
}
