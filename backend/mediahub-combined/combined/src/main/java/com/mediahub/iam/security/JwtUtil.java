package com.mediahub.iam.security;

import com.mediahub.iam.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

// Spring component that centralizes JWT creation and validation logic.
// Marked @Component so it can be injected wherever tokens are issued (login) or verified (JwtFilter).
@Component
public class JwtUtil {

    // The secret signing key, injected from the "jwt.secret" application property.
    @Value("${jwt.secret}")
    private String secret;

    // The token's lifetime in milliseconds, injected from the "jwt.expiry" application property.
    @Value("${jwt.expiry}")
    private long expiry;

    // Builds the HMAC-SHA signing key from the configured secret.
    // The same key is used to both sign newly issued tokens and verify incoming ones.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Creates a signed JWT for the given user, embedding identity/role claims into the payload.
    // Sets the issued-at and expiration timestamps, signs with the secret key, and returns the compact token string.
    public String generateToken(User user, java.util.List<String> permissions) {
        JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(user.getUserId()))
            .claim("userId",    user.getUserId())
            .claim("roleId",    user.getRole().getRoleId())
            .claim("roleType",  user.getRole().getRoleType())
            .claim("email",     user.getEmail())
            .claim("country",   user.getCountry())
            .claim("isRevoked", user.getIsRevoked())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiry));

        if (permissions != null) {
            builder.claim("permissions", permissions);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    // Backwards-compatible overload (if callers don't have permissions list)
    public String generateToken(User user) {
        return generateToken(user, null);
    }

    // Verifies a token's signature and parses it, returning its Claims (payload) if valid.
    // Throws a JwtException for any invalid/expired/tampered token, which callers handle as a 401.
    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Convenience helper: validates the token and pulls out the "userId" claim.
    public Long extractUserId(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    // Convenience helper: validates the token and pulls out the "roleType" claim.
    public String extractRoleType(String token) {
        return validateToken(token).get("roleType", String.class);
    }
}