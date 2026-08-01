package com.mediahub.contentcatalog.repository;

import com.mediahub.contentcatalog.entity.Creator;
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
public class CreatorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CreatorRepository creatorRepository;

    private Creator creator;

    @BeforeEach
    void setUp() {
        creator = new Creator();
        creator.setUserId(1L);
        creator.setDisplayName("Priya Creates");
        creator.setGenre("Drama");
        creator.setCountry("India");
        creator.setRoyaltyTier("Gold");
        creator.setBankAccountRef("BANK-001");
        creator.setStatus("Active");
        entityManager.persist(creator);
        entityManager.flush();
    }

    @Test
    void testSaveCreator_Positive() {
        Creator newCreator = new Creator();
        newCreator.setUserId(2L);
        newCreator.setDisplayName("Arjun Studios");
        newCreator.setGenre("Action");
        newCreator.setCountry("India");
        newCreator.setRoyaltyTier("Silver");
        newCreator.setBankAccountRef("BANK-002");
        newCreator.setStatus("Active");
        Creator saved = creatorRepository.save(newCreator);
        assertNotNull(saved);
        assertNotNull(saved.getCreatorId());
        assertEquals("Arjun Studios", saved.getDisplayName());
    }

    @Test
    void testSaveCreator_Negative_EmptyDisplayName() {
        Creator newCreator = new Creator();
        newCreator.setUserId(3L);
        newCreator.setDisplayName("Test Creator");
        newCreator.setStatus("Active");
        Creator saved = creatorRepository.save(newCreator);
        assertNotNull(saved);
        assertNotNull(saved.getDisplayName());
        assertEquals("Test Creator", saved.getDisplayName());
    }

    @Test
    void testFindAllCreators_Positive() {
        List<Creator> creators = creatorRepository.findAll();
        assertNotNull(creators);
        assertFalse(creators.isEmpty());
        assertEquals(1, creators.size());
    }

    @Test
    void testFindAllCreators_Negative_EmptyRepository() {
        creatorRepository.deleteAll();
        List<Creator> creators = creatorRepository.findAll();
        assertNotNull(creators);
        assertTrue(creators.isEmpty());
    }

    @Test
    void testFindById_Positive() {
        Optional<Creator> found = creatorRepository.findById(creator.getCreatorId());
        assertTrue(found.isPresent());
        assertEquals("Priya Creates", found.get().getDisplayName());
    }

    @Test
    void testFindById_Negative_NotFound() {
        Optional<Creator> found = creatorRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateCreator_Positive() {
        creator.setDisplayName("Priya Studios");
        creator.setRoyaltyTier("Platinum");
        Creator updated = creatorRepository.save(creator);
        assertEquals("Priya Studios", updated.getDisplayName());
        assertEquals("Platinum", updated.getRoyaltyTier());
    }

    @Test
    void testUpdateCreator_Negative_WrongId() {
        Optional<Creator> found = creatorRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateCreatorStatus_Positive() {
        creator.setStatus("Suspended");
        Creator updated = creatorRepository.save(creator);
        assertEquals("Suspended", updated.getStatus());
    }

    @Test
    void testUpdateCreatorStatus_Negative_InvalidStatus() {
        creator.setStatus("InvalidStatus");
        Creator updated = creatorRepository.save(creator);
        assertEquals("InvalidStatus", updated.getStatus());
    }

    @Test
    void testDeleteCreator_Positive() {
        creatorRepository.deleteById(creator.getCreatorId());
        Optional<Creator> found = creatorRepository.findById(creator.getCreatorId());
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteCreator_Negative_NotFound() {
        int sizeBefore = creatorRepository.findAll().size();
        creatorRepository.deleteById(999);
        int sizeAfter = creatorRepository.findAll().size();
        assertEquals(sizeBefore, sizeAfter);
    }

    @Test
    void testExistsById_Positive() {
        boolean exists = creatorRepository.existsById(creator.getCreatorId());
        assertTrue(exists);
    }

    @Test
    void testExistsById_Negative() {
        boolean exists = creatorRepository.existsById(999);
        assertFalse(exists);
    }

    @Test
    void testCountCreators() {
        long count = creatorRepository.count();
        assertEquals(1, count);
    }
}