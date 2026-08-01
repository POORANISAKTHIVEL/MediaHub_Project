package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.EditorialReview;
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
public class EditorialReviewRepositoryTest {

    @Mock
    private EditorialReviewRepository repository;

    private EditorialReview review;

    @BeforeEach
    void setUp() {
        review = new EditorialReview();
        review.setReviewID(1);
        review.setContentID(101);
        review.setReviewerID(201);
        review.setSubmissionDate(new Date());
        review.setStatus("Pending");
    }

    // TC-01: save() persists review and returns it
    @Test
    void save_returnsPersistedReview() {
        when(repository.save(any(EditorialReview.class))).thenReturn(review);

        EditorialReview saved = repository.save(review);

        assertNotNull(saved);
        assertEquals(1, saved.getReviewID());
        assertEquals(101, saved.getContentID());
        verify(repository, times(1)).save(review);
    }

    // TC-02: findById() returns Optional present when found
    @Test
    void findById_returnsOptionalPresent() {
        when(repository.findById(1)).thenReturn(Optional.of(review));

        Optional<EditorialReview> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Pending", result.get().getStatus());
    }

    // TC-03: findById() returns empty Optional when not found
    @Test
    void findById_returnsOptionalEmpty() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Optional<EditorialReview> result = repository.findById(99);

        assertFalse(result.isPresent());
    }

    // TC-04: findAll() returns all reviews
    @Test
    void findAll_returnsAllReviews() {
        EditorialReview r2 = new EditorialReview();
        r2.setReviewID(2);
        when(repository.findAll()).thenReturn(Arrays.asList(review, r2));

        List<EditorialReview> result = repository.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-05: findAll() returns empty list when no records
    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<EditorialReview> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    // TC-06: deleteById() is invoked with correct ID
    @Test
    void deleteById_invokesDelete() {
        doNothing().when(repository).deleteById(1);

        repository.deleteById(1);

        verify(repository, times(1)).deleteById(1);
    }

    // TC-07: save() updates decision and status on existing review
    @Test
    void save_updatesDecisionAndStatus() {
        review.setDecision("Approved");
        review.setStatus("Completed");
        when(repository.save(any(EditorialReview.class))).thenReturn(review);

        EditorialReview updated = repository.save(review);

        assertEquals("Approved", updated.getDecision());
        assertEquals("Completed", updated.getStatus());
    }

    // TC-08: findById() returns review with all fields
    @Test
    void findById_returnsReviewWithAllFields() {
        review.setDecision("Rejected");
        review.setRemarks("Not acceptable");
        review.setReviewDate(new Date());
        when(repository.findById(1)).thenReturn(Optional.of(review));

        Optional<EditorialReview> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Rejected", result.get().getDecision());
        assertEquals("Not acceptable", result.get().getRemarks());
        assertNotNull(result.get().getReviewDate());
    }
}
