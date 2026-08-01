package com.mediahub.editorial;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Value("${jwt.secret}")
    private String secret;

    private byte[] keyBytes;

    @BeforeEach
    void setup() {
        keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    private String buildToken(long userId, String roleType, List<String> perms) {
        var b = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("roleType", roleType)
                .claim("permissions", perms)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000));
        return b.signWith(Keys.hmacShaKeyFor(keyBytes)).compact();
    }

    @Test
    void subscriberCannotCreateCollection() throws Exception {
        String token = buildToken(200L, "subscriber", List.of("content:read"));
        mvc.perform(post("/MediaHub/editorial/collections")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void creatorCanCreateCollection_but_subscriberCannot() throws Exception {
        String token = buildToken(201L, "creator", List.of("content:read","content:write"));
        var result = mvc.perform(post("/MediaHub/editorial/collections")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"x\"}"))
            .andReturn();
        int status = result.getResponse().getStatus();
        // creator should NOT receive 403
        org.junit.jupiter.api.Assertions.assertNotEquals(403, status);
    }

    @Test
    void editorialCanApproveReview() throws Exception {
        String token = buildToken(202L, "editorial", List.of("content:read","content:publish","content:delete"));
        var result = mvc.perform(post("/MediaHub/editorial/reviews/1/approve")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"remarks\":\"ok\"}"))
            .andReturn();
        int status = result.getResponse().getStatus();
        // editorial should NOT receive 403
        org.junit.jupiter.api.Assertions.assertNotEquals(403, status);
    }

    @Test
    void creatorCannotApproveReview() throws Exception {
        String token = buildToken(203L, "creator", List.of("content:read","content:write"));
        mvc.perform(post("/MediaHub/editorial/reviews/1/approve")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"remarks\":\"ok\"}"))
                .andExpect(status().isForbidden());
    }
}
