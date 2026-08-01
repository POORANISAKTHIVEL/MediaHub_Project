package com.mediahub.contentcatalog.service;

import com.mediahub.contentcatalog.entity.ContentTag;
import com.mediahub.contentcatalog.repository.ContentTagRepository;
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
public class ContentTagServiceTest {

    @Mock
    private ContentTagRepository contentTagRepository;

    @InjectMocks
    private ContentTagService contentTagService;

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
        when(contentTagRepository.save(any(ContentTag.class))).thenReturn(contentTag);
        String result = contentTagService.addTag(contentTag);
        assertEquals("Tag added successfully", result);
        verify(contentTagRepository, times(1)).save(any(ContentTag.class));
    }

    @Test
    void testAddTag_Negative_NullTag() {
        ContentTag nullTag = new ContentTag();
        when(contentTagRepository.save(any(ContentTag.class))).thenReturn(nullTag);
        String result = contentTagService.addTag(nullTag);
        assertEquals("Tag added successfully", result);
    }

    @Test
    void testGetTagsByContentId_Positive() {
        List<ContentTag> tags = Arrays.asList(contentTag);
        when(contentTagRepository.findByContentId(1)).thenReturn(tags);
        List<ContentTag> result = contentTagService.getTagsByContentId(1);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drama", result.get(0).getTagName());
        verify(contentTagRepository, times(1)).findByContentId(1);
    }

    @Test
    void testGetTagsByContentId_Negative_EmptyList() {
        when(contentTagRepository.findByContentId(999)).thenReturn(Arrays.asList());
        List<ContentTag> result = contentTagService.getTagsByContentId(999);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testRemoveTag_Positive() {
        when(contentTagRepository.findById(1)).thenReturn(Optional.of(contentTag));
        doNothing().when(contentTagRepository).deleteById(1);
        String result = contentTagService.removeTag(1);
        assertEquals("Tag removed successfully", result);
        verify(contentTagRepository, times(1)).deleteById(1);
    }

    @Test
    void testRemoveTag_Negative_NotFound() {
        when(contentTagRepository.findById(999)).thenReturn(Optional.empty());
        String result = contentTagService.removeTag(999);
        assertEquals("Tag not found", result);
        verify(contentTagRepository, never()).deleteById(999);
    }
}