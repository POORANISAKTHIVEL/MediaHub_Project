package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.ContentTag;
import com.mediahub.contentcatalog.service.ContentTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/mediahub/contentCatalog/contentTag")
public class ContentTagController {

    // ✅ Logger added
    private static final Logger logger = LoggerFactory.getLogger(ContentTagController.class);

    @Autowired
    ContentTagService contentTagService;

    // ✅ ADD TAG
    @PreAuthorize("hasAuthority('content:write')")
    @PostMapping("/addTag")
    public ResponseEntity<String> addTag(@RequestBody ContentTag contentTag) {
        logger.info("Received request to add tag for contentId: {}", contentTag.getContentId());

        return ResponseEntity.status(201)
                .body(contentTagService.addTag(contentTag));
    }

    // ✅ FETCH TAGS
    @PreAuthorize("hasAuthority('content:read')")
    @GetMapping("/fetchTagsByContent/{contentId}")
    public ResponseEntity<List<ContentTag>> getTagsByContentId(@PathVariable int contentId) {
        logger.info("Received request to fetch tags for contentId: {}", contentId);

        return ResponseEntity.ok(
                contentTagService.getTagsByContentId(contentId)
        );
    }

    // ✅ REMOVE TAG
    @PreAuthorize("hasAuthority('content:delete')")
    @DeleteMapping("/removeTag/{tagId}")
    public ResponseEntity<String> removeTag(@PathVariable int tagId) {
        logger.info("Received request to remove tag with ID: {}", tagId);

        return ResponseEntity.status(200)
                .body(contentTagService.removeTag(tagId));
    }
}
