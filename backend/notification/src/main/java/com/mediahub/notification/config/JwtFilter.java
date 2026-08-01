package com.mediahub.notification.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String token = authorization.substring(7);
                Claims claims = jwtUtil.validateToken(token);

                String username = claims.getSubject();
                Collection<SimpleGrantedAuthority> authorities = buildAuthorities(claims);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                // allow Spring Security to handle authentication failures
            }
        }

        filterChain.doFilter(request, response);
    }

    private Collection<SimpleGrantedAuthority> buildAuthorities(Claims claims) {
        List<String> permissions = claims.get("permissions", List.class);
        String roleType = claims.get("roleType", String.class);
        List<String> roles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> permissionAuthorities = permissions == null ? List.of()
                : permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        List<SimpleGrantedAuthority> roleAuthorities = Stream.concat(
                roles == null ? Stream.<String>empty() : roles.stream(),
                roleType == null ? Stream.<String>empty() : Stream.of(roleType)
            )
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        return Stream.concat(permissionAuthorities.stream(), roleAuthorities.stream())
                .collect(Collectors.toList());
    }
}

