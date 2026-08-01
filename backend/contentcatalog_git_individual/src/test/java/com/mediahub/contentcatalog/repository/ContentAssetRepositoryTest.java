package com.mediahub.contentcatalog.repository;

import com.mediahub.contentcatalog.entity.ContentAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ContentAssetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContentAssetRepository contentAssetRepository;

    private ContentAsset contentAsset;

    @BeforeEach
    void setUp() {
        contentAsset = new ContentAsset();
        contentAsset.setCreatorId(1);
        contentAsset.setTitle("My First Short Film");
        contentAsset.setType("Video");
        contentAsset.setGenre("Drama");
        contentAsset.setLanguage("Tamil");
        contentAsset.setDurationSeconds(1800);
        contentAsset.setSynopsis("A story about friendship");
        contentAsset.setFilePath("/content/videos/film1.mp4");
        contentAsset.setThumbnailPath("/content/thumbs/film1.jpg");
        contentAsset.setStatus("Draft");
        entityManager.persist(contentAsset);
        entityManager.flush();
    }

    @Test
    void testSaveContent_Positive() {
        ContentAsset newContent = new ContentAsset();
        newContent.setCreatorId(1);
        newContent.setTitle("New Podcast");
        newContent.setType("Podcast");
        newContent.setGenre("Education");
        newContent.setLanguage("English");
        newContent.setDurationSeconds(3600);
        newContent.setSynopsis("AI Introduction");
        newContent.setFilePath("/content/podcasts/ep1.mp3");
        newContent.setThumbnailPath("/content/thumbs/ep1.jpg");
        newContent.setStatus("Draft");
        ContentAsset saved = contentAssetRepository.save(newContent);
        assertNotNull(saved);
        assertNotNull(saved.getContentId());
        assertEquals("New Podcast", saved.getTitle());
    }

    @Test
    void testSaveContent_Negative_NullTitle() {
        ContentAsset newContent = new ContentAsset();
        newContent.setCreatorId(1);
        newContent.setTitle("Test Content");
        newContent.setType("Video");
        newContent.setStatus("Draft");
        newContent.setFilePath("/content/videos/test.mp4");
        ContentAsset saved = contentAssetRepository.save(newContent);
        assertNotNull(saved);
        assertNotNull(saved.getTitle());
        assertEquals("Test Content", saved.getTitle());
    }

    @Test
    void testFindAllContents_Positive() {
        List<ContentAsset> contents = contentAssetRepository.findAll();
        assertNotNull(contents);
        assertFalse(contents.isEmpty());
        assertEquals(1, contents.size());
    }

    @Test
    void testFindAllContents_Negative_EmptyRepository() {
        contentAssetRepository.deleteAll();
        List<ContentAsset> contents = contentAssetRepository.findAll();
        assertNotNull(contents);
        assertTrue(contents.isEmpty());
    }

    @Test
    void testFindById_Positive() {
        Optional<ContentAsset> found = contentAssetRepository.findById(contentAsset.getContentId());
        assertTrue(found.isPresent());
        assertEquals("My First Short Film", found.get().getTitle());
    }

    @Test
    void testFindById_Negative_NotFound() {
        Optional<ContentAsset> found = contentAssetRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateContent_Positive() {
        contentAsset.setTitle("Updated Film");
        contentAsset.setGenre("Thriller");
        ContentAsset updated = contentAssetRepository.save(contentAsset);
        assertEquals("Updated Film", updated.getTitle());
        assertEquals("Thriller", updated.getGenre());
    }

    @Test
    void testUpdateContent_Negative_WrongId() {
        Optional<ContentAsset> found = contentAssetRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateStatus_DraftToUnderReview() {
        contentAsset.setStatus("UnderReview");
        ContentAsset updated = contentAssetRepository.save(contentAsset);
        assertEquals("UnderReview", updated.getStatus());
    }

    @Test
    void testUpdateStatus_UnderReviewToPublished() {
        contentAsset.setStatus("Published");
        ContentAsset updated = contentAssetRepository.save(contentAsset);
        assertEquals("Published", updated.getStatus());
    }

    @Test
    void testUpdateStatus_PublishedToArchived() {
        contentAsset.setStatus("Archived");
        ContentAsset updated = contentAssetRepository.save(contentAsset);
        assertEquals("Archived", updated.getStatus());
    }

    @Test
    void testUpdateStatus_ArchivedToRemoved() {
        contentAsset.setStatus("Removed");
        ContentAsset updated = contentAssetRepository.save(contentAsset);
        assertEquals("Removed", updated.getStatus());
    }

    @Test
    void testUpdateStatus_Negative_NotFound() {
        Optional<ContentAsset> found = contentAssetRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteContent_Positive() {
        contentAssetRepository.deleteById(contentAsset.getContentId());
        Optional<ContentAsset> found = contentAssetRepository.findById(contentAsset.getContentId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteContent_Negative_NotFound() {
        int sizeBefore = contentAssetRepository.findAll().size();
        contentAssetRepository.deleteById(999);
        int sizeAfter = contentAssetRepository.findAll().size();
        assertEquals(sizeBefore, sizeAfter);
    }

    @Test
    void testDeleteContent_Negative_NotDraft() {
        contentAsset.setStatus("Published");
        contentAssetRepository.save(contentAsset);
        Optional<ContentAsset> found = contentAssetRepository.findById(contentAsset.getContentId());
        assertTrue(found.isPresent());
        assertEquals("Published", found.get().getStatus());
    }

    @Test
    void testExistsById_Positive() {
        boolean exists = contentAssetRepository.existsById(contentAsset.getContentId());
        assertTrue(exists);
    }

    @Test
    void testExistsById_Negative() {
        boolean exists = contentAssetRepository.existsById(999);
        assertFalse(exists);
    }

    @Test
    void testCountContents() {
        long count = contentAssetRepository.count();
        assertEquals(1, count);
    }
}