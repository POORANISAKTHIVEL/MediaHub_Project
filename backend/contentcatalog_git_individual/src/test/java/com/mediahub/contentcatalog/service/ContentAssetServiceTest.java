package com.mediahub.contentcatalog.service;

import com.mediahub.contentcatalog.entity.ContentAsset;
import com.mediahub.contentcatalog.repository.ContentAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContentAssetServiceTest {

    @Mock
    private ContentAssetRepository contentAssetRepository;

    @InjectMocks
    private ContentAssetService contentAssetService;

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
        when(contentAssetRepository.save(any(ContentAsset.class))).thenReturn(contentAsset);
        String result = contentAssetService.createContent(contentAsset);
        assertEquals("Content created successfully", result);
        verify(contentAssetRepository, times(1)).save(any(ContentAsset.class));
    }

    @Test
    void testCreateContent_Negative_NullContent() {
        ContentAsset nullContent = new ContentAsset();
        when(contentAssetRepository.save(any(ContentAsset.class))).thenReturn(nullContent);
        String result = contentAssetService.createContent(nullContent);
        assertEquals("Content created successfully", result);
    }

    @Test
    void testGetAllContents_Positive() {
        List<ContentAsset> contents = Arrays.asList(contentAsset);
        when(contentAssetRepository.findAll()).thenReturn(contents);
        List<ContentAsset> result = contentAssetService.getAllContents();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("My First Short Film", result.get(0).getTitle());
        verify(contentAssetRepository, times(1)).findAll();
    }

    @Test
    void testGetAllContents_Negative_EmptyList() {
        when(contentAssetRepository.findAll()).thenReturn(Arrays.asList());
        List<ContentAsset> result = contentAssetService.getAllContents();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetContentById_Positive() {
        when(contentAssetRepository.findById(1)).thenReturn(Optional.of(contentAsset));
        ContentAsset result = contentAssetService.getContentById(1);
        assertNotNull(result);
        assertEquals("My First Short Film", result.getTitle());
        verify(contentAssetRepository, times(1)).findById(1);
    }

    @Test
    void testGetContentById_Negative_NotFound() {
        when(contentAssetRepository.findById(999)).thenReturn(Optional.empty());
        ContentAsset result = contentAssetService.getContentById(999);
        assertNull(result);
    }

    @Test
    void testUpdateContent_Positive() {
        ContentAsset updatedContent = new ContentAsset();
        updatedContent.setTitle("Updated Film");
        updatedContent.setGenre("Thriller");
        updatedContent.setLanguage("English");
        updatedContent.setSynopsis("New synopsis");
        updatedContent.setFilePath("/content/videos/updated.mp4");
        updatedContent.setThumbnailPath("/content/thumbs/updated.jpg");
        when(contentAssetRepository.findById(1)).thenReturn(Optional.of(contentAsset));
        when(contentAssetRepository.save(any(ContentAsset.class))).thenReturn(contentAsset);
        String result = contentAssetService.updateContent(1, updatedContent);
        assertEquals("Content updated successfully", result);
        verify(contentAssetRepository, times(1)).save(any(ContentAsset.class));
    }

    @Test
    void testUpdateContent_Negative_NotFound() {
        ContentAsset updatedContent = new ContentAsset();
        when(contentAssetRepository.findById(999)).thenReturn(Optional.empty());
        String result = contentAssetService.updateContent(999, updatedContent);
        assertEquals("Content not found", result);
        verify(contentAssetRepository, never()).save(any(ContentAsset.class));
    }

    @Test
    void testUpdateContentStatus_Positive() {
        when(contentAssetRepository.findById(1)).thenReturn(Optional.of(contentAsset));
        when(contentAssetRepository.save(any(ContentAsset.class))).thenReturn(contentAsset);
        String result = contentAssetService.updateContentStatus(1, "UnderReview");
        assertEquals("Status updated successfully", result);
        verify(contentAssetRepository, times(1)).save(any(ContentAsset.class));
    }

    @Test
    void testUpdateContentStatus_Negative_NotFound() {
        when(contentAssetRepository.findById(999)).thenReturn(Optional.empty());
        String result = contentAssetService.updateContentStatus(999, "UnderReview");
        assertEquals("Content not found", result);
        verify(contentAssetRepository, never()).save(any(ContentAsset.class));
    }

    @Test
    void testDeleteContent_Positive() {
        contentAsset.setStatus("Draft");
        when(contentAssetRepository.findById(1)).thenReturn(Optional.of(contentAsset));
        doNothing().when(contentAssetRepository).deleteById(1);
        String result = contentAssetService.deleteContent(1);
        assertEquals("Content deleted successfully", result);
        verify(contentAssetRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteContent_Negative_NotDraft() {
        contentAsset.setStatus("Published");
        when(contentAssetRepository.findById(1)).thenReturn(Optional.of(contentAsset));
        String result = contentAssetService.deleteContent(1);
        assertEquals("Content can only be deleted when status is Draft", result);
        verify(contentAssetRepository, never()).deleteById(1);
    }

    @Test
    void testDeleteContent_Negative_NotFound() {
        when(contentAssetRepository.findById(999)).thenReturn(Optional.empty());
        String result = contentAssetService.deleteContent(999);
        assertEquals("Content not found", result);
        verify(contentAssetRepository, never()).deleteById(999);
    }
}