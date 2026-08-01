package com.mediahub.contentcatalog.repository;

import com.mediahub.contentcatalog.entity.ContentTag;
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
public class ContentTagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContentTagRepository contentTagRepository;

    private ContentTag contentTag;

    @BeforeEach
    void setUp() {
        contentTag = new ContentTag();
        contentTag.setContentId(1);
        contentTag.setTagName("Drama");
        contentTag.setTagCategory("Genre");
        entityManager.persist(contentTag);
        entityManager.flush();
    }

    @Test
    void testSaveTag_Positive() {
        ContentTag newTag = new ContentTag();
        newTag.setContentId(1);
        newTag.setTagName("Emotional");
        newTag.setTagCategory("Mood");
        ContentTag saved = contentTagRepository.save(newTag);
        assertNotNull(saved);
        assertNotNull(saved.getTagId());
        assertEquals("Emotional", saved.getTagName());
    }

    @Test
    void testSaveTag_Negative_NullTagName() {
        ContentTag newTag = new ContentTag();
        newTag.setContentId(1);
        newTag.setTagName("Horror");
        newTag.setTagCategory("Genre");
        ContentTag saved = contentTagRepository.save(newTag);
        assertNotNull(saved);
        assertNotNull(saved.getTagName());
        assertEquals("Horror", saved.getTagName());
    }

    @Test
    void testFindAllTags_Positive() {
        List<ContentTag> tags = contentTagRepository.findAll();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertEquals(1, tags.size());
    }

    @Test
    void testFindAllTags_Negative_EmptyRepository() {
        contentTagRepository.deleteAll();
        List<ContentTag> tags = contentTagRepository.findAll();
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testFindById_Positive() {
        Optional<ContentTag> found = contentTagRepository.findById(contentTag.getTagId());
        assertTrue(found.isPresent());
        assertEquals("Drama", found.get().getTagName());
    }

    @Test
    void testFindById_Negative_NotFound() {
        Optional<ContentTag> found = contentTagRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByContentId_Positive() {
        List<ContentTag> tags = contentTagRepository.findByContentId(1);
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertEquals("Drama", tags.get(0).getTagName());
    }

    @Test
    void testFindByContentId_Negative_NotFound() {
        List<ContentTag> tags = contentTagRepository.findByContentId(999);
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testFindByContentId_MultipleTags() {
        ContentTag tag2 = new ContentTag();
        tag2.setContentId(1);
        tag2.setTagName("Emotional");
        tag2.setTagCategory("Mood");
        entityManager.persist(tag2);
        entityManager.flush();
        List<ContentTag> tags = contentTagRepository.findByContentId(1);
        assertEquals(2, tags.size());
    }

    @Test
    void testUpdateTag_Positive() {
        contentTag.setTagName("Comedy");
        ContentTag updated = contentTagRepository.save(contentTag);
        assertEquals("Comedy", updated.getTagName());
    }

    @Test
    void testUpdateTag_Negative_WrongId() {
        Optional<ContentTag> found = contentTagRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteTag_Positive() {
        contentTagRepository.deleteById(contentTag.getTagId());
        Optional<ContentTag> found = contentTagRepository.findById(contentTag.getTagId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteTag_Negative_NotFound() {
        int sizeBefore = contentTagRepository.findAll().size();
        contentTagRepository.deleteById(999);
        int sizeAfter = contentTagRepository.findAll().size();
        assertEquals(sizeBefore, sizeAfter);
    }

    @Test
    void testExistsById_Positive() {
        boolean exists = contentTagRepository.existsById(contentTag.getTagId());
        assertTrue(exists);
    }

    @Test
    void testExistsById_Negative() {
        boolean exists = contentTagRepository.existsById(999);
        assertFalse(exists);
    }

    @Test
    void testCountTags() {
        long count = contentTagRepository.count();
        assertEquals(1, count);
    }
}