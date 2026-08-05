package com.mediahub.editorial.controller;

import com.mediahub.editorial.client.AuditClient;
import com.mediahub.editorial.model.ContentCollection;
import com.mediahub.editorial.service.ContentCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/MediaHub/editorial")
public class ContentCollectionController {

    @Autowired
    private ContentCollectionService service;

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

    // API 7 — POST /collections
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
    @PostMapping("/collections")
    public ResponseEntity<Map<String, Object>> createCollection(
            @RequestBody ContentCollection collection,
            Authentication authentication) {
        Map<String, Object> res =
                service.createCollection(collection);
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("COLLECTION_CREATED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "ContentCollection", String.valueOf(collection.getCollectionID()),
                "Created collection: " + collection.getName(), "LOW");
        }
        return ResponseEntity.status(code).body(res);
    }

    // API 8 — GET /collections
        @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:read')")
        @GetMapping("/collections")
        public ResponseEntity<List<ContentCollection>>
                        getAllCollections() {
        return ResponseEntity.ok(service.getAllCollections());
    }

    // API 9 — GET /collections/{collectionID}
        @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('content:read')")
        @GetMapping("/collections/{collectionID}")
        public ResponseEntity<Map<String, Object>> getCollectionById(
            @PathVariable int collectionID) {
        Map<String, Object> res =
                service.getCollectionById(collectionID);
        int code = (int) res.remove("statusCode");
        return ResponseEntity.status(code).body(res);
    }

    // API 10 — PUT /collections/{collectionID}
        @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
        @PutMapping("/collections/{collectionID}")
        public ResponseEntity<Map<String, Object>> updateCollection(
            @PathVariable int collectionID,
            @RequestBody ContentCollection collection) {
        Map<String, Object> res =
                service.updateCollection(collectionID, collection);
        int code = (int) res.remove("statusCode");
        return ResponseEntity.status(code).body(res);
    }

    // API 11 — POST /collections/{collectionID}/expire
        @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
        @PostMapping("/collections/{collectionID}/expire")
        public ResponseEntity<Map<String, Object>> expireCollection(
            @PathVariable int collectionID,
            Authentication authentication) {
        Map<String, Object> res =
                service.expireCollection(collectionID);
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("COLLECTION_EXPIRED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "ContentCollection", String.valueOf(collectionID),
                "Expired collection", "LOW");
        }
        return ResponseEntity.status(code).body(res);
    }

    // API 12 — DELETE /collections/{collectionID}
        @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('editorial:manage')")
        @DeleteMapping("/collections/{collectionID}")
        public ResponseEntity<Map<String, Object>> deleteCollection(
            @PathVariable int collectionID,
            Authentication authentication) {
        Map<String, Object> res =
                service.deleteCollection(collectionID);
        int code = (int) res.remove("statusCode");
        if (code >= 200 && code < 300) {
            auditClient.log("COLLECTION_DELETED", "EDITORIAL", Long.valueOf(authentication.getName()),
                actorRole(authentication), "ContentCollection", String.valueOf(collectionID),
                "Deleted collection", "MEDIUM");
        }
        return ResponseEntity.status(code).body(res);
    }
}
