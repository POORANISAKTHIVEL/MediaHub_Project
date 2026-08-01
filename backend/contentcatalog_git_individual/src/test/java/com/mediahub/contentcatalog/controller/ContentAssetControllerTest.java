package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.ContentAsset;
import com.mediahub.contentcatalog.service.ContentAssetService;
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
public class ContentAssetControllerTest {

    @Mock
    private ContentAssetService contentAssetService;

    @InjectMocks
    private ContentAssetController contentAssetController;

    private ContentAsset contentAsset;

    @BeforeEach
    void setUp() {
        contentAsset = new ContentAsset();
        contentAsset.setContentId(1);
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
    }

    @Test
    void testCreateContent_Positive() {
        when(contentAssetService.createContent(any(ContentAsset.class)))
                .thenReturn("Content created successfully");
        ResponseEntity<String> response = contentAssetController.createContent(contentAsset);
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Content created successfully", response.getBody());
        verify(contentAssetService, times(1)).createContent(any(ContentAsset.class));
    }

    @Test
    void testCreateContent_Negative() {
        when(contentAssetService.createContent(any(ContentAsset.class)))
                .thenReturn("Content created successfully");
        ContentAsset emptyContent = new ContentAsset();
        ResponseEntity<String> response = contentAssetController.createContent(emptyContent);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void testGetAllContents_Positive() {
        List<ContentAsset> contents = Arrays.asList(contentAsset);
        when(contentAssetService.getAllContents()).thenReturn(contents);
        ResponseEntity<List<ContentAsset>> response = contentAssetController.getAllContents();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(contentAssetService, times(1)).getAllContents();
    }

    @Test
    void testGetAllContents_Negative_EmptyList() {
        when(contentAssetService.getAllContents()).thenReturn(Arrays.asList());
        ResponseEntity<List<ContentAsset>> response = contentAssetController.getAllContents();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testGetContentById_Positive() {
        when(contentAssetService.getContentById(1)).thenReturn(contentAsset);
        ResponseEntity<ContentAsset> response = contentAssetController.getContentById(1);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("My First Short Film", response.getBody().getTitle());
        verify(contentAssetService, times(1)).getContentById(1);
    }

    @Test
    void testGetContentById_Negative_NotFound() {
        when(contentAssetService.getContentById(999)).thenReturn(null);
        ResponseEntity<ContentAsset> response = contentAssetController.getContentById(999);
        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateContent_Positive() {
        when(contentAssetService.updateContent(eq(1), any(ContentAsset.class)))
                .thenReturn("Content updated successfully");
        ResponseEntity<String> response = contentAssetController.updateContent(1, contentAsset);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content updated successfully", response.getBody());
        verify(contentAssetService, times(1)).updateContent(eq(1), any(ContentAsset.class));
    }

    @Test
    void testUpdateContent_Negative_NotFound() {
        when(contentAssetService.updateContent(eq(999), any(ContentAsset.class)))
                .thenReturn("Content not found");
        ResponseEntity<String> response = contentAssetController.updateContent(999, contentAsset);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content not found", response.getBody());
    }

    @Test
    void testUpdateContentStatus_Positive() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "UnderReview");
        when(contentAssetService.updateContentStatus(1, "UnderReview"))
                .thenReturn("Status updated successfully");
        ResponseEntity<String> response = contentAssetController.updateContentStatus(1, body);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Status updated successfully", response.getBody());
    }

    @Test
    void testUpdateContentStatus_Negative_NotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "UnderReview");
        when(contentAssetService.updateContentStatus(999, "UnderReview"))
                .thenReturn("Content not found");
        ResponseEntity<String> response = contentAssetController.updateContentStatus(999, body);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content not found", response.getBody());
    }

    @Test
    void testDeleteContent_Positive() {
        when(contentAssetService.deleteContent(1))
                .thenReturn("Content deleted successfully");
        ResponseEntity<String> response = contentAssetController.deleteContent(1);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content deleted successfully", response.getBody());
        verify(contentAssetService, times(1)).deleteContent(1);
    }

    @Test
    void testDeleteContent_Negative_NotDraft() {
        when(contentAssetService.deleteContent(1))
                .thenReturn("Content can only be deleted when status is Draft");
        ResponseEntity<String> response = contentAssetController.deleteContent(1);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content can only be deleted when status is Draft", response.getBody());
    }

    @Test
    void testDeleteContent_Negative_NotFound() {
        when(contentAssetService.deleteContent(999))
                .thenReturn("Content not found");
        ResponseEntity<String> response = contentAssetController.deleteContent(999);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Content not found", response.getBody());
    }
}