package com.mediahub.contentcatalog.config;

import com.mediahub.contentcatalog.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (!token.isBlank()) {
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

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Validated JWT bearer token; authentication set for subject {}", claims.getSubject());
                } catch (JwtException e) {
                    logger.error("JWT validation failure", e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
