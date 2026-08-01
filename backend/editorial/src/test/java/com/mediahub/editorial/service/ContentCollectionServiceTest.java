package com.mediahub.editorial.service;

import com.mediahub.editorial.model.ContentCollection;
import com.mediahub.editorial.repository.ContentCollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentCollectionServiceTest {

    @Mock
    private ContentCollectionRepository repository;

    @InjectMocks
    private ContentCollectionService service;

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

    // TC-01: Create collection successfully
    @Test
    void createCollection_success() {
        when(repository.save(any(ContentCollection.class))).thenReturn(collection);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(201, result.get("statusCode"));
        assertEquals("Scheduled", result.get("status"));
        assertEquals("Collection created successfully.", result.get("message"));
        verify(repository, times(1)).save(any(ContentCollection.class));
    }

    // TC-02: Null name returns 400
    @Test
    void createCollection_nullName() {
        collection.setName(null);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Name is required", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-03: Empty name returns 400
    @Test
    void createCollection_emptyName() {
        collection.setName("");

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Name is required", result.get("error"));
    }

    // TC-04: Invalid category returns 400
    @Test
    void createCollection_invalidCategory() {
        collection.setCategory("Unknown");

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Category must be Featured Trending Curated or New", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-05: Null category returns 400
    @Test
    void createCollection_nullCategory() {
        collection.setCategory(null);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Category must be Featured Trending Curated or New", result.get("error"));
    }

    // TC-06: Valid category "Trending"
    @Test
    void createCollection_categoryTrending() {
        collection.setCategory("Trending");
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(201, result.get("statusCode"));
    }

    // TC-07: Valid category "Curated"
    @Test
    void createCollection_categoryCurated() {
        collection.setCategory("Curated");
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(201, result.get("statusCode"));
    }

    // TC-08: Valid category "New"
    @Test
    void createCollection_categoryNew() {
        collection.setCategory("New");
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(201, result.get("statusCode"));
    }

    // TC-09: Empty contentIDs returns 400
    @Test
    void createCollection_emptyContentIDs() {
        collection.setContentIDs(Collections.emptyList());

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ContentIDs are required", result.get("error"));
    }

    // TC-10: Null contentIDs returns 400
    @Test
    void createCollection_nullContentIDs() {
        collection.setContentIDs(null);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ContentIDs are required", result.get("error"));
    }

    // TC-11: Null publishDate returns 400
    @Test
    void createCollection_nullPublishDate() {
        collection.setPublishDate(null);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Dates are required", result.get("error"));
    }

    // TC-12: Null expiryDate returns 400
    @Test
    void createCollection_nullExpiryDate() {
        collection.setExpiryDate(null);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Dates are required", result.get("error"));
    }

    // TC-13: Status is auto-set to Scheduled before save
    @Test
    void createCollection_statusAutoSetToScheduled() {
        collection.setStatus(null);
        when(repository.save(any())).thenReturn(collection);

        service.createCollection(collection);

        verify(repository).save(argThat(c -> "Scheduled".equals(c.getStatus())));
    }

    // TC-14: Response contains collectionID from saved entity
    @Test
    void createCollection_responseContainsCollectionID() {
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.createCollection(collection);

        assertEquals(1, result.get("collectionID"));
    }

    // TC-15: Get all collections returns populated list
    @Test
    void getAllCollections_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(collection, new ContentCollection()));

        List<ContentCollection> result = service.getAllCollections();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-16: Get all collections returns empty list
    @Test
    void getAllCollections_empty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<ContentCollection> result = service.getAllCollections();

        assertTrue(result.isEmpty());
    }

    // TC-17: Get collection by ID — found
    @Test
    void getCollectionById_found() {
        when(repository.findById(1)).thenReturn(Optional.of(collection));

        Map<String, Object> result = service.getCollectionById(1);

        assertEquals(200, result.get("statusCode"));
        assertNotNull(result.get("collection"));
        assertEquals(collection, result.get("collection"));
    }

    // TC-18: Get collection by ID — not found
    @Test
    void getCollectionById_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.getCollectionById(99);

        assertEquals(404, result.get("statusCode"));
        assertTrue(result.get("error").toString().contains("99"));
    }

    // TC-19: Update collection — success
    @Test
    void updateCollection_success() {
        when(repository.findById(1)).thenReturn(Optional.of(collection));
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.updateCollection(1, collection);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Collection updated successfully.", result.get("message"));
        assertEquals(1, result.get("collectionID"));
        verify(repository).save(any(ContentCollection.class));
    }

    // TC-20: Update collection — not found
    @Test
    void updateCollection_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.updateCollection(99, collection);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Collection not found", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-21: Expire collection — success
    @Test
    void expireCollection_success() {
        when(repository.findById(1)).thenReturn(Optional.of(collection));
        when(repository.save(any())).thenReturn(collection);

        Map<String, Object> result = service.expireCollection(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Expired", result.get("status"));
        assertEquals("Collection expired successfully.", result.get("message"));
        verify(repository).save(argThat(c -> "Expired".equals(c.getStatus())));
    }

    // TC-22: Expire collection — not found
    @Test
    void expireCollection_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.expireCollection(99);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Collection not found", result.get("error"));
    }

    // TC-23: Delete collection — Active status is blocked
    @Test
    void deleteCollection_activeBlocked() {
        collection.setStatus("Active");
        when(repository.findById(1)).thenReturn(Optional.of(collection));

        Map<String, Object> result = service.deleteCollection(1);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Cannot delete Active collection. Expire it first.", result.get("error"));
        verify(repository, never()).deleteById(anyInt());
    }

    // TC-24: Delete collection — Scheduled status succeeds
    @Test
    void deleteCollection_scheduledSuccess() {
        collection.setStatus("Scheduled");
        when(repository.findById(1)).thenReturn(Optional.of(collection));
        doNothing().when(repository).deleteById(1);

        Map<String, Object> result = service.deleteCollection(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Collection deleted successfully.", result.get("message"));
        verify(repository).deleteById(1);
    }

    // TC-25: Delete collection — Expired status succeeds
    @Test
    void deleteCollection_expiredSuccess() {
        collection.setStatus("Expired");
        when(repository.findById(1)).thenReturn(Optional.of(collection));
        doNothing().when(repository).deleteById(1);

        Map<String, Object> result = service.deleteCollection(1);

        assertEquals(200, result.get("statusCode"));
        verify(repository).deleteById(1);
    }

    // TC-26: Delete collection — not found
    @Test
    void deleteCollection_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.deleteCollection(99);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Collection not found", result.get("error"));
    }
}
