package com.mediahub.contentcatalog.controller;

import com.mediahub.contentcatalog.entity.ContentTag;
import com.mediahub.contentcatalog.service.ContentTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentTagControllerTest {

    @Mock
    private ContentTagService contentTagService;

    @InjectMocks
    private ContentTagController contentTagController;

    private ContentTag contentTag;

    @BeforeEach
    void setUp() {
        contentTag = new ContentTag();
        contentTag.setTagId(1);
        contentTag.setContentId(1);
        contentTag.setTagName("Drama");
        contentTag.setTagCategory("Genre");
    }

    @Test
    void testAddTag_Positive() {
        when(contentTagService.addTag(any(ContentTag.class)))
                .thenReturn("Tag added successfully");
        ResponseEntity<String> response = contentTagController.addTag(contentTag);
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Tag added successfully", response.getBody());
        verify(contentTagService, times(1)).addTag(any(ContentTag.class));
    }

    @Test
    void testAddTag_Negative() {
        when(contentTagService.addTag(any(ContentTag.class)))
                .thenReturn("Tag added successfully");
        ContentTag emptyTag = new ContentTag();
        ResponseEntity<String> response = contentTagController.addTag(emptyTag);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void testGetTagsByContentId_Positive() {
        List<ContentTag> tags = Arrays.asList(contentTag);
        when(contentTagService.getTagsByContentId(1)).thenReturn(tags);
        ResponseEntity<List<ContentTag>> response = contentTagController.getTagsByContentId(1);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Drama", response.getBody().get(0).getTagName());
        verify(contentTagService, times(1)).getTagsByContentId(1);
    }

    @Test
    void testGetTagsByContentId_Negative_EmptyList() {
        when(contentTagService.getTagsByContentId(999)).thenReturn(Arrays.asList());
        ResponseEntity<List<ContentTag>> response = contentTagController.getTagsByContentId(999);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testRemoveTag_Positive() {
        when(contentTagService.removeTag(1))
                .thenReturn("Tag removed successfully");
        ResponseEntity<String> response = contentTagController.removeTag(1);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Tag removed successfully", response.getBody());
        verify(contentTagService, times(1)).removeTag(1);
    }

    @Test
    void testRemoveTag_Negative_NotFound() {
        when(contentTagService.removeTag(999))
                .thenReturn("Tag not found");
        ResponseEntity<String> response = contentTagController.removeTag(999);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Tag not found", response.getBody());
    }
}