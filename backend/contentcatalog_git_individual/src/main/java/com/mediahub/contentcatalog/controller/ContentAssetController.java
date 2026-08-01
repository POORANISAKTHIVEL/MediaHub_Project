package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.ContentAsset;
import com.mediahub.contentcatalog.service.ContentAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mediahub/contentCatalog/contentAsset")
public class ContentAssetController {

    // ✅ Logger added
    private static final Logger logger = LoggerFactory.getLogger(ContentAssetController.class);

    @Autowired
    ContentAssetService contentAssetService;

    // ✅ CREATE CONTENT
    @PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/createContent")
    public ResponseEntity<String> createContent(
            @RequestBody ContentAsset contentAsset) {

        logger.info("Received request to create content");

        return ResponseEntity.status(201)
                .body(contentAssetService.createContent(contentAsset));
    }

    // ✅ GET ALL CONTENT
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchContents")
    public ResponseEntity<List<ContentAsset>> getAllContents() {

        logger.info("Received request to fetch all content assets");

        return ResponseEntity.ok(
                contentAssetService.getAllContents());
    }

    // ✅ GET CONTENT BY ID
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchContentById/{contentId}")
    public ResponseEntity<ContentAsset> getContentById(
            @PathVariable int contentId) {

        logger.info(
                "Received request to fetch content with ID: {}",
                contentId);

        return ResponseEntity.ok(
                contentAssetService.getContentById(contentId));
    }

    // ✅ UPDATE CONTENT
    @PreAuthorize("hasAuthority('content:write')")
    @PutMapping("/updateContent/{contentId}")
    public ResponseEntity<String> updateContent(
            @PathVariable int contentId,
            @RequestBody ContentAsset contentAsset) {

        logger.info(
                "Received request to update content with ID: {}",
                contentId);

        return ResponseEntity.ok(
                contentAssetService.updateContent(
                        contentId,
                        contentAsset));
    }

    // ✅ UPDATE CONTENT STATUS
    @PreAuthorize("hasAuthority('content:write')")
    @PutMapping("/updateContentStatus/{contentId}")
    public ResponseEntity<String> updateContentStatus(
            @PathVariable int contentId,
            @RequestBody Map<String, String> body) {

        logger.info(
                "Received request to update content status for ID: {}",
                contentId);

        return ResponseEntity.ok(
                contentAssetService.updateContentStatus(
                        contentId,
                        body.get("status")));
    }

    // ✅ SUBSCRIPTION VALIDATION + CONTENT ACCESS
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/accessContent/{userId}/{contentId}")
    public ResponseEntity<Map<String, Object>> accessContent(
            @PathVariable Long userId,
            @PathVariable int contentId) {

        logger.info(
                "Received access request for user {} and content {}",
                userId,
                contentId);

        return ResponseEntity.ok(
                contentAssetService.accessContent(
                        userId,
                        contentId));
    }

    // ✅ DELETE CONTENT
    @PreAuthorize("hasAuthority('content:delete')")
    @DeleteMapping("/deleteContent/{contentId}")
    public ResponseEntity<String> deleteContent(
            @PathVariable int contentId) {

        logger.info(
                "Received request to delete content with ID: {}",
                contentId);

        return ResponseEntity.status(200)
                .body(contentAssetService.deleteContent(contentId));
    }
 // ✅ ROYALTY INTEGRATION — fetch content IDs by creator
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchByCreator/{creatorId}")
    public ResponseEntity<List<ContentAsset>> getContentsByCreator(
            @PathVariable int creatorId) {

        logger.info(
                "Received request to fetch content for creatorId: {}",
                creatorId);

        return ResponseEntity.ok(
                contentAssetService.getContentsByCreator(creatorId));
    }

 // ✅ VALIDATE CONTENT FOR LICENSING MODULE
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/validateContent/{contentId}")
    public ResponseEntity<Boolean> validateContent(
            @PathVariable int contentId) {

        ContentAsset content =
                contentAssetService.getContentById(contentId);

        return ResponseEntity.ok(
                content != null);
    }
}