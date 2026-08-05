package com.mediahub.editorial.controller;

import com.mediahub.editorial.client.AuditClient;
import com.mediahub.editorial.model.EditorialReview;
import com.mediahub.editorial.service.EditorialReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/MediaHub/editorial")
public class EditorialReviewController {

    @Autowired
    private EditorialReviewService service;

    @Autowired
    private AuditClient auditClient;

    private static String actorRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .findFirst()
            .orElse("UNKNOWN");
    }

    // API 1 — POST /reviews
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(
            @RequestBody EditorialReview review) {
        Map<String, Object> res = service.submitReview(review);
        int code = (int) res.remove("statusCode");
        return ResponseEntity.status(code).body(res);
    }

    // API 2 — GET /reviews
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/reviews")
    public ResponseEntity<List<EditorialReview>> getAllReviews() {
        return ResponseEntity.ok(service.getAllReviews());
    }

    // API 3 — GET /reviews/{reviewID}
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/reviews/{reviewID}")
    public ResponseEntity<Map<String, Object>> getReviewById(
            @PathVariable int reviewID) {
        Map<String, Object> res =
                service.getReviewById(reviewID);
        int code = (int) res.remove("statusCode");
        return ResponseEntity.status(code).body(res);
    }

    // API 4 — POST /reviews/{reviewID}/approve
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/reviews/{reviewID}/approve")
    public ResponseEntity<Map<String, Object>> approveReview(
            @PathVariable int reviewID,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Map<String, Object> res = service.approveReview(
                reviewID, body.get("remarks"), Long.valueOf(authentication.getName()));
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("REVIEW_APPROVED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "EditorialReview", String.valueOf(reviewID),
                "Approved review: " + body.get("remarks"), "LOW");
        }
        return ResponseEntity.status(code).body(res);
    }

    // API 5 — POST /reviews/{reviewID}/reject
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/reviews/{reviewID}/reject")
    public ResponseEntity<Map<String, Object>> rejectReview(
            @PathVariable int reviewID,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Map<String, Object> res = service.rejectReview(
                reviewID, body.get("remarks"), Long.valueOf(authentication.getName()));
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("REVIEW_REJECTED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "EditorialReview", String.valueOf(reviewID),
                "Rejected review: " + body.get("remarks"), "MEDIUM");
        }
        return ResponseEntity.status(code).body(res);
    }

    // API 6 — POST /reviews/{reviewID}/revise
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/reviews/{reviewID}/revise")
    public ResponseEntity<Map<String, Object>> requestRevision(
            @PathVariable int reviewID,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Map<String, Object> res = service.requestRevision(
                reviewID, body.get("remarks"), Long.valueOf(authentication.getName()));
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("REVIEW_REVISION_REQUESTED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "EditorialReview", String.valueOf(reviewID),
                "Requested revision: " + body.get("remarks"), "LOW");
        }
        return ResponseEntity.status(code).body(res);
    }

    // ✅ ROYALTY VALIDATION ENDPOINT — check if content has an Approved editorial review
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/validateApproval/{contentId}")
    public ResponseEntity<Boolean> validateApproval(
            @PathVariable int contentId) {
        return ResponseEntity.ok(service.validateApproval(contentId));
    }
}
