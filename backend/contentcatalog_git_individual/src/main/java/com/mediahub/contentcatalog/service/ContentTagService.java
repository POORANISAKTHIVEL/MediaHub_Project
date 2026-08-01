package com.mediahub.contentcatalog.service;

import com.mediahub.contentcatalog.entity.ContentTag;
import com.mediahub.contentcatalog.repository.ContentTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ContentTagService {

    // ✅ Logger added
    private static final Logger logger = LoggerFactory.getLogger(ContentTagService.class);

    @Autowired
    ContentTagRepository contentTagRepository;

    // ✅ ADD TAG
    public String addTag(ContentTag contentTag) {
        logger.info("Adding tag '{}' for contentId: {}", 
                contentTag.getTagName(), contentTag.getContentId());

        contentTagRepository.save(contentTag);

        logger.info("Tag added successfully for contentId: {}", contentTag.getContentId());
        return "Tag added successfully";
    }

    // ✅ GET TAGS
    public List<ContentTag> getTagsByContentId(int contentId) {
        logger.info("Fetching tags for contentId: {}", contentId);

        List<ContentTag> tags = contentTagRepository.findByContentId(contentId);

        if (tags.isEmpty()) {
            logger.warn("No tags found for contentId: {}", contentId);
        }

        return tags;
    }

    // ✅ REMOVE TAG
    public String removeTag(int tagId) {
        logger.info("Attempting to remove tag with ID: {}", tagId);

        ContentTag existing = contentTagRepository.findById(tagId).orElse(null);

        if (existing == null) {
            logger.error("Tag not found with ID: {}", tagId);
            return "Tag not found";
        }

        contentTagRepository.deleteById(tagId);

        logger.info("Tag removed successfully with ID: {}", tagId);
        return "Tag removed successfully";
    }
}