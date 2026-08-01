package com.mediahub.editorial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.editorial.model.ContentCollection;
import com.mediahub.editorial.service.ContentCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContentCollectionController.class)
public class ContentCollectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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

    // TC-01: POST /collections — 201 Created
    @Test
    void createCollection_returns201() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 201);
        resp.put("message", "Collection created successfully.");
        resp.put("status", "Scheduled");
        when(service.createCollection(any(ContentCollection.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collection)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Collection created successfully."))
                .andExpect(jsonPath("$.status").value("Scheduled"));
    }

    // TC-02: POST /collections — 400 Bad Request (validation error)
    @Test
    void createCollection_returns400() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 400);
        resp.put("error", "Name is required");
        when(service.createCollection(any(ContentCollection.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/collections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collection)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name is required"));
    }

    // TC-03: GET /collections — 200 with list
    @Test
    void getAllCollections_returns200() throws Exception {
        when(service.getAllCollections())
                .thenReturn(Arrays.asList(collection, new ContentCollection()));

        mockMvc.perform(get("/MediaHub/editorial/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // TC-04: GET /collections — 200 with empty list
    @Test
    void getAllCollections_returnsEmptyList() throws Exception {
        when(service.getAllCollections()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/MediaHub/editorial/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // TC-05: GET /collections/{id} — 200 Found
    @Test
    void getCollectionById_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("collection", collection);
        when(service.getCollectionById(1)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/collections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").exists());
    }

    // TC-06: GET /collections/{id} — 404 Not Found
    @Test
    void getCollectionById_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Collection not found with ID: 99");
        when(service.getCollectionById(99)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/collections/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Collection not found with ID: 99"));
    }

    // TC-07: PUT /collections/{id} — 200 Updated
    @Test
    void updateCollection_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("collectionID", 1);
        resp.put("message", "Collection updated successfully.");
        when(service.updateCollection(eq(1), any(ContentCollection.class))).thenReturn(resp);

        mockMvc.perform(put("/MediaHub/editorial/collections/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collection)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Collection updated successfully."));
    }

    // TC-08: PUT /collections/{id} — 404 Not Found
    @Test
    void updateCollection_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Collection not found");
        when(service.updateCollection(eq(99), any(ContentCollection.class))).thenReturn(resp);

        mockMvc.perform(put("/MediaHub/editorial/collections/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(collection)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Collection not found"));
    }

    // TC-09: POST /collections/{id}/expire — 200 Expired
    @Test
    void expireCollection_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("status", "Expired");
        resp.put("message", "Collection expired successfully.");
        when(service.expireCollection(1)).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/collections/1/expire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Expired"))
                .andExpect(jsonPath("$.message").value("Collection expired successfully."));
    }

    // TC-10: POST /collections/{id}/expire — 404 Not Found
    @Test
    void expireCollection_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Collection not found");
        when(service.expireCollection(99)).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/collections/99/expire"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Collection not found"));
    }

    // TC-11: DELETE /collections/{id} — 200 Deleted
    @Test
    void deleteCollection_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("message", "Collection deleted successfully.");
        when(service.deleteCollection(1)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/collections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Collection deleted successfully."));
    }

    // TC-12: DELETE /collections/{id} — 400 Active blocked
    @Test
    void deleteCollection_activeBlocked_returns400() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 400);
        resp.put("error", "Cannot delete Active collection. Expire it first.");
        when(service.deleteCollection(1)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/collections/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Cannot delete Active collection. Expire it first."));
    }

    // TC-13: DELETE /collections/{id} — 404 Not Found
    @Test
    void deleteCollection_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Collection not found");
        when(service.deleteCollection(99)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/collections/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Collection not found"));
    }
}
