package com.mediahub.contentcatalog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.allowed.issuers}")
    private String allowedIssuers;

    private SecretKey signingKey;
    private List<String> issuers;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuers = List.of(allowedIssuers.split(","));
    }

    public Claims validateToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String issuer = claims.getIssuer();
        if (issuer != null && issuers.stream().noneMatch(allowed -> allowed.equalsIgnoreCase(issuer))) {
            throw new JwtException("Invalid token issuer");
        }

        return claims;
    }
}
