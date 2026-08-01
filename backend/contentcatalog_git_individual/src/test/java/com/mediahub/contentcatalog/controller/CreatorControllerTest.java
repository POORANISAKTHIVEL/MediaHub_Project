package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.Creator;
import com.mediahub.contentcatalog.service.CreatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatorControllerTest {

    @Mock
    private CreatorService creatorService;

    @InjectMocks
    private CreatorController creatorController;

    private Creator creator;

    @BeforeEach
    void setUp() {
        creator = new Creator();
        creator.setCreatorId(1);
        creator.setUserId(1L);
        creator.setDisplayName("Priya Creates");
        creator.setGenre("Drama");
        creator.setCountry("India");
        creator.setRoyaltyTier("Gold");
        creator.setBankAccountRef("BANK-001");
        creator.setStatus("PendingReview");
    }

    @Test
    void testCreateCreator_Positive() {
        when(creatorService.createCreator(any(Creator.class)))
                .thenReturn("Creator created successfully");
        ResponseEntity<String> response = creatorController.createCreator(creator);
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Creator created successfully", response.getBody());
        verify(creatorService, times(1)).createCreator(any(Creator.class));
    }

    @Test
    void testCreateCreator_Negative() {
        when(creatorService.createCreator(any(Creator.class)))
                .thenReturn("Creator created successfully");
        Creator emptyCreator = new Creator();
        ResponseEntity<String> response = creatorController.createCreator(emptyCreator);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void testGetAllCreators_Positive() {
        List<Creator> creators = Arrays.asList(creator);
        when(creatorService.getAllCreators()).thenReturn(creators);
        ResponseEntity<List<Creator>> response = creatorController.getAllCreators();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(creatorService, times(1)).getAllCreators();
    }

    @Test
    void testGetAllCreators_Negative_EmptyList() {
        when(creatorService.getAllCreators()).thenReturn(Arrays.asList());
        ResponseEntity<List<Creator>> response = creatorController.getAllCreators();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testGetCreatorById_Positive() {
        when(creatorService.getCreatorById(1)).thenReturn(creator);
        ResponseEntity<Creator> response = creatorController.getCreatorById(1);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Priya Creates", response.getBody().getDisplayName());
        verify(creatorService, times(1)).getCreatorById(1);
    }

    @Test
    void testGetCreatorById_Negative_NotFound() {
        when(creatorService.getCreatorById(999)).thenReturn(null);
        ResponseEntity<Creator> response = creatorController.getCreatorById(999);
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateCreator_Positive() {
        when(creatorService.updateCreator(eq(1), any(Creator.class)))
                .thenReturn("Creator updated successfully");
        ResponseEntity<String> response = creatorController.updateCreator(1, creator);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Creator updated successfully", response.getBody());
        verify(creatorService, times(1)).updateCreator(eq(1), any(Creator.class));
    }

    @Test
    void testUpdateCreator_Negative_NotFound() {
        when(creatorService.updateCreator(eq(999), any(Creator.class)))
                .thenReturn("Creator not found");
        ResponseEntity<String> response = creatorController.updateCreator(999, creator);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Creator not found", response.getBody());
    }

    @Test
    void testUpdateCreatorStatus_Positive() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "Active");
        when(creatorService.updateCreatorStatus(1, "Active"))
                .thenReturn("Status updated successfully");
        ResponseEntity<String> response = creatorController.updateCreatorStatus(1, body);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Status updated successfully", response.getBody());
    }

    @Test
    void testUpdateCreatorStatus_Negative_NotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "Active");
        when(creatorService.updateCreatorStatus(999, "Active"))
                .thenReturn("Creator not found");
        ResponseEntity<String> response = creatorController.updateCreatorStatus(999, body);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Creator not found", response.getBody());
    }
}