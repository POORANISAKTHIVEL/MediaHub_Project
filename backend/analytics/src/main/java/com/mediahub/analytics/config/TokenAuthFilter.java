package com.mediahub.analytics.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class TokenAuthFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);

    @Value("${jwt.secret:mediahub_iam_secret_key_2025_must_be_32_chars_min}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            return chain.filter(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String roleType = claims.get("roleType", String.class);
            if (roleType != null && !roleType.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType.toUpperCase()));
            }

            Object permissions = claims.get("permissions");
            if (permissions instanceof Collection<?> permissionCollection) {
                for (Object permission : permissionCollection) {
                    if (permission != null) {
                        authorities.add(new SimpleGrantedAuthority(permission.toString()));
                    }
                }
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } catch (JwtException e) {
            logger.warn("JWT validation failed for analytics request", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
                    .wrap("{\"status\":401,\"error\":\"INVALID_TOKEN\",\"message\":\"Invalid or expired JWT token\"}".getBytes(StandardCharsets.UTF_8))));
        }
    }
}
