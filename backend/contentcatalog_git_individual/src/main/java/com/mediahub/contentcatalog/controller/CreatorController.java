package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.Creator;
import com.mediahub.contentcatalog.service.CreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mediahub/contentCatalog/creator")
public class CreatorController {

    // ✅ Logger added
    private static final Logger logger = LoggerFactory.getLogger(CreatorController.class);

    @Autowired
    CreatorService creatorService;

    // ✅ CREATE
    @PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/createCreator")
    public ResponseEntity<String> createCreator(@RequestBody Creator creator) {
        logger.info("Received request to create creator");

        if (creator == null) {
            logger.error("createCreator called with null body");
            return ResponseEntity.badRequest().body("Request body is required");
        }

        if (creator.getUserId() == null || creator.getDisplayName() == null) {
            logger.error("createCreator missing required fields: userId or displayName");
            return ResponseEntity.badRequest().body("userId and displayName are required");
        }

        try {
            String result = creatorService.createCreator(creator);
            return ResponseEntity.status(201).body(result);
        } catch (IllegalArgumentException iae) {
            logger.error("Validation error creating creator", iae);
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (IllegalStateException ise) {
            logger.error("Conflict creating creator", ise);
            return ResponseEntity.status(409).body(ise.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating creator", e);
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // ✅ GET ALL
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchCreators")
    public ResponseEntity<List<Creator>> getAllCreators() {
        logger.info("Received request to fetch all creators");

        return ResponseEntity.ok(creatorService.getAllCreators());
    }

    // ✅ GET BY ID
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchCreatorById/{creatorId}")
    public ResponseEntity<Creator> getCreatorById(@PathVariable int creatorId) {
        logger.info("Received request to fetch creator with ID: {}", creatorId);

        return ResponseEntity.ok(creatorService.getCreatorById(creatorId));
    }

    // ✅ UPDATE CREATOR
    @PreAuthorize("hasAuthority('content:write')")
    @PutMapping("/updateCreator/{creatorId}")
    public ResponseEntity<String> updateCreator(@PathVariable int creatorId,
                                                @RequestBody Creator creator) {

        logger.info("Received request to update creator with ID: {}", creatorId);

        return ResponseEntity.ok(creatorService.updateCreator(creatorId, creator));
    }

    // ✅ UPDATE STATUS
    // Lifecycle transitions (PendingReview -> Active -> Suspended) are an admin-only decision,
    // unlike profile edits above which the creator's own content:write also covers.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateCreatorStatus/{creatorId}")
    public ResponseEntity<String> updateCreatorStatus(@PathVariable int creatorId,
                                                      @RequestBody Map<String, String> body) {

        logger.info("Received request to update creator status for ID: {}", creatorId);

        return ResponseEntity.ok(
                creatorService.updateCreatorStatus(creatorId, body.get("status"))
        );
    }
 // ✅ VALIDATE CREATOR FOR ROYALTY MODULE
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/validateCreator/{creatorId}")
    public ResponseEntity<Boolean> validateCreator(
            @PathVariable int creatorId) {

        Creator creator =
                creatorService.getCreatorById(creatorId);

        return ResponseEntity.ok(
                creator != null);
    }
}