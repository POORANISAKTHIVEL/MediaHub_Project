package com.mediahub.contentcatalog.service;

import com.mediahub.contentcatalog.entity.Creator;
import com.mediahub.contentcatalog.repository.CreatorRepository;
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
public class CreatorServiceTest {

    @Mock
    private CreatorRepository creatorRepository;

    @InjectMocks
    private CreatorService creatorService;

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
        when(creatorRepository.save(any(Creator.class))).thenReturn(creator);
        String result = creatorService.createCreator(creator);
        assertEquals("Creator created successfully", result);
        verify(creatorRepository, times(1)).save(any(Creator.class));
    }

    @Test
    void testCreateCreator_Negative_NullCreator() {
        Creator nullCreator = new Creator();
        when(creatorRepository.save(any(Creator.class))).thenReturn(nullCreator);
        String result = creatorService.createCreator(nullCreator);
        assertEquals("Creator created successfully", result);
    }

    @Test
    void testGetAllCreators_Positive() {
        List<Creator> creators = Arrays.asList(creator);
        when(creatorRepository.findAll()).thenReturn(creators);
        List<Creator> result = creatorService.getAllCreators();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Priya Creates", result.get(0).getDisplayName());
        verify(creatorRepository, times(1)).findAll();
    }

    @Test
    void testGetAllCreators_Negative_EmptyList() {
        when(creatorRepository.findAll()).thenReturn(Arrays.asList());
        List<Creator> result = creatorService.getAllCreators();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCreatorById_Positive() {
        when(creatorRepository.findById(1)).thenReturn(Optional.of(creator));
        Creator result = creatorService.getCreatorById(1);
        assertNotNull(result);
        assertEquals("Priya Creates", result.getDisplayName());
        verify(creatorRepository, times(1)).findById(1);
    }

    @Test
    void testGetCreatorById_Negative_NotFound() {
        when(creatorRepository.findById(999)).thenReturn(Optional.empty());
        Creator result = creatorService.getCreatorById(999);
        assertNull(result);
    }

    @Test
    void testUpdateCreator_Positive() {
        Creator updatedCreator = new Creator();
        updatedCreator.setDisplayName("Priya Studios");
        updatedCreator.setGenre("Thriller");
        updatedCreator.setCountry("India");
        updatedCreator.setRoyaltyTier("Platinum");
        updatedCreator.setBankAccountRef("BANK-099");
        when(creatorRepository.findById(1)).thenReturn(Optional.of(creator));
        when(creatorRepository.save(any(Creator.class))).thenReturn(creator);
        String result = creatorService.updateCreator(1, updatedCreator);
        assertEquals("Creator updated successfully", result);
        verify(creatorRepository, times(1)).save(any(Creator.class));
    }

    @Test
    void testUpdateCreator_Negative_NotFound() {
        Creator updatedCreator = new Creator();
        when(creatorRepository.findById(999)).thenReturn(Optional.empty());
        String result = creatorService.updateCreator(999, updatedCreator);
        assertEquals("Creator not found", result);
        verify(creatorRepository, never()).save(any(Creator.class));
    }

    @Test
    void testUpdateCreatorStatus_Positive() {
        when(creatorRepository.findById(1)).thenReturn(Optional.of(creator));
        when(creatorRepository.save(any(Creator.class))).thenReturn(creator);
        String result = creatorService.updateCreatorStatus(1, "Active");
        assertEquals("Status updated successfully", result);
        verify(creatorRepository, times(1)).save(any(Creator.class));
    }

    @Test
    void testUpdateCreatorStatus_Negative_NotFound() {
        when(creatorRepository.findById(999)).thenReturn(Optional.empty());
        String result = creatorService.updateCreatorStatus(999, "Active");
        assertEquals("Creator not found", result);
        verify(creatorRepository, never()).save(any(Creator.class));
    }
}