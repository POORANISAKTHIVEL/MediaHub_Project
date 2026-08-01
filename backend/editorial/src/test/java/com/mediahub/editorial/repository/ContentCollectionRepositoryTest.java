package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.ContentCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentCollectionRepositoryTest {

    @Mock
    private ContentCollectionRepository repository;

    private ContentCollection collection;

    @BeforeEach
    void setUp() {
        collection = new ContentCollection();
        collection.setCollectionID(1);
        collection.setName("Tech Highlights");
        collection.setCategory("Featured");
        collection.setContentIDs(Arrays.asList(1, 2, 3));
        collection.setPublishDate(new Date());
        collection.setExpiryDate(new Date(System.currentTimeMillis() + 86400000L));
        collection.setStatus("Scheduled");
    }

    // TC-01: save() persists entity and returns it
    @Test
    void save_returnsPersistedEntity() {
        when(repository.save(any(ContentCollection.class))).thenReturn(collection);

        ContentCollection saved = repository.save(collection);

        assertNotNull(saved);
        assertEquals(1, saved.getCollectionID());
        assertEquals("Tech Highlights", saved.getName());
        verify(repository, times(1)).save(collection);
    }

    // TC-02: findById() returns Optional with entity when found
    @Test
    void findById_returnsOptionalPresent() {
        when(repository.findById(1)).thenReturn(Optional.of(collection));

        Optional<ContentCollection> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Featured", result.get().getCategory());
    }

    // TC-03: findById() returns empty Optional when not found
    @Test
    void findById_returnsOptionalEmpty() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Optional<ContentCollection> result = repository.findById(99);

        assertFalse(result.isPresent());
    }

    // TC-04: findAll() returns all collections
    @Test
    void findAll_returnsAllCollections() {
        ContentCollection c2 = new ContentCollection();
        c2.setCollectionID(2);
        when(repository.findAll()).thenReturn(Arrays.asList(collection, c2));

        List<ContentCollection> result = repository.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-05: findAll() returns empty list when no records
    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<ContentCollection> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    // TC-06: deleteById() is invoked with correct ID
    @Test
    void deleteById_invokesDelete() {
        doNothing().when(repository).deleteById(1);

        repository.deleteById(1);

        verify(repository, times(1)).deleteById(1);
    }

    // TC-07: save() updates existing entity (same ID)
    @Test
    void save_updatesExistingEntity() {
        collection.setStatus("Expired");
        when(repository.save(any(ContentCollection.class))).thenReturn(collection);

        ContentCollection updated = repository.save(collection);

        assertEquals("Expired", updated.getStatus());
    }

    // TC-08: findById() returns entity with correct contentIDs
    @Test
    void findById_returnsEntityWithContentIDs() {
        when(repository.findById(1)).thenReturn(Optional.of(collection));

        Optional<ContentCollection> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(Arrays.asList(1, 2, 3), result.get().getContentIDs());
    }
}
