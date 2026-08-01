package com.mediahub.licensing.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

// Spring component that centralizes JWT creation and validation logic.
// Marked @Component so it can be injected wherever tokens are verified.
@Component
public class JwtUtil {

    // The secret signing key, injected from the "jwt.secret" application property.
    @Value("${jwt.secret}")
    private String secret;

    // The allowed issuers (IAM and Gateway), injected from the "jwt.allowed.issuers" property.
    @Value("${jwt.allowed.issuers}")
    private String allowedIssuers;

    // Builds the HMAC-SHA signing key from the configured secret.
    // The same key is used to verify incoming tokens.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Verifies a token's signature and parses it, returning its Claims (payload) if valid.
    // Throws a JwtException for any invalid/expired/tampered token.
    public Claims validateToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        // Validate issuer if present in token
        String issuer = claims.getIssuer();
        if (issuer != null) {
            validateIssuer(issuer);
        }
        
        return claims;
    }

    // Validates that the token issuer is in the list of allowed issuers
    private void validateIssuer(String issuer) {
        String[] issuers = allowedIssuers.split(",");
        for (String allowedIssuer : issuers) {
            if (allowedIssuer.trim().equals(issuer)) {
                return;
            }
        }
        throw new JwtException("Invalid issuer: " + issuer);
    }

    // Convenience helper: validates the token and pulls out the "userId" claim.
    public Long extractUserId(String token) {
        return validateToken(token).get("userId", Long.class);
    }

    // Convenience helper: validates the token and pulls out the "roleType" claim.
    public String extractRoleType(String token) {
        return validateToken(token).get("roleType", String.class);
    }

    // Extract any claim by key
    public Object extractClaim(String token, String claimKey) {
        return validateToken(token).get(claimKey);
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
