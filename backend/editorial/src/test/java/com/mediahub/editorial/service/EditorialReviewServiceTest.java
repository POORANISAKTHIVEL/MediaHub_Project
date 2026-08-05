package com.mediahub.editorial.service;

import com.mediahub.editorial.model.EditorialReview;
import com.mediahub.editorial.repository.EditorialReviewRepository;
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
public class EditorialReviewServiceTest {

    @Mock
    private EditorialReviewRepository repository;

    @InjectMocks
    private EditorialReviewService service;

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

    // TC-01: Submit review successfully
    @Test
    void submitReview_success() {
        when(repository.save(any(EditorialReview.class))).thenReturn(review);

        Map<String, Object> result = service.submitReview(review);

        assertEquals(201, result.get("statusCode"));
        assertEquals("Pending", result.get("status"));
        assertEquals("Content submitted for review successfully", result.get("message"));
        verify(repository, times(1)).save(any(EditorialReview.class));
    }

    // TC-02: ContentID is zero returns 400
    @Test
    void submitReview_zeroContentID() {
        review.setContentID(0);

        Map<String, Object> result = service.submitReview(review);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ContentID is required", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-03: ReviewerID is zero returns 400
    @Test
    void submitReview_zeroReviewerID() {
        review.setReviewerID(0);

        Map<String, Object> result = service.submitReview(review);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ReviewerID is required", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-04: Null submissionDate gets auto-set
    @Test
    void submitReview_nullSubmissionDateAutoSet() {
        review.setSubmissionDate(null);
        when(repository.save(any(EditorialReview.class))).thenReturn(review);

        service.submitReview(review);

        verify(repository).save(argThat(r -> r.getSubmissionDate() != null));
    }

    // TC-05: Status is auto-set to Pending before save
    @Test
    void submitReview_statusAutoSetToPending() {
        review.setStatus(null);
        when(repository.save(any(EditorialReview.class))).thenReturn(review);

        service.submitReview(review);

        verify(repository).save(argThat(r -> "Pending".equals(r.getStatus())));
    }

    // TC-06: Response contains reviewID from saved entity
    @Test
    void submitReview_responseContainsReviewID() {
        when(repository.save(any())).thenReturn(review);

        Map<String, Object> result = service.submitReview(review);

        assertEquals(1, result.get("reviewID"));
        assertEquals(101, result.get("contentID"));
        assertEquals(201, result.get("reviewerID"));
    }

    // TC-07: Get all reviews returns list
    @Test
    void getAllReviews_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(review, new EditorialReview()));

        List<EditorialReview> result = service.getAllReviews();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-08: Get all reviews returns empty list
    @Test
    void getAllReviews_empty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<EditorialReview> result = service.getAllReviews();

        assertTrue(result.isEmpty());
    }

    // TC-09: Get review by ID — found
    @Test
    void getReviewById_found() {
        when(repository.findById(1)).thenReturn(Optional.of(review));

        Map<String, Object> result = service.getReviewById(1);

        assertEquals(200, result.get("statusCode"));
        assertNotNull(result.get("review"));
        assertEquals(review, result.get("review"));
    }

    // TC-10: Get review by ID — not found
    @Test
    void getReviewById_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.getReviewById(99);

        assertEquals(404, result.get("statusCode"));
        assertTrue(result.get("error").toString().contains("99"));
    }

    // TC-11: Approve review — success
    @Test
    void approveReview_success() {
        when(repository.findById(1)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        Map<String, Object> result = service.approveReview(1, "Good work", 1L);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Approved", result.get("decision"));
        assertEquals("Completed", result.get("status"));
        assertEquals("Content approved successfully.", result.get("message"));
        verify(repository).save(argThat(r ->
            "Approved".equals(r.getDecision()) && "Completed".equals(r.getStatus())));
    }

    // TC-12: Approve review — not found
    @Test
    void approveReview_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.approveReview(99, "Remarks", 1L);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Review not found", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-13: Reject review — success
    @Test
    void rejectReview_success() {
        when(repository.findById(1)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        Map<String, Object> result = service.rejectReview(1, "Not acceptable", 1L);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Rejected", result.get("decision"));
        assertEquals("Completed", result.get("status"));
        assertEquals("Content rejected. Creator notified.", result.get("message"));
        verify(repository).save(argThat(r ->
            "Rejected".equals(r.getDecision()) && "Completed".equals(r.getStatus())));
    }

    // TC-14: Reject review — not found
    @Test
    void rejectReview_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.rejectReview(99, "Remarks", 1L);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Review not found", result.get("error"));
    }

    // TC-15: Request revision — success
    @Test
    void requestRevision_success() {
        when(repository.findById(1)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        Map<String, Object> result = service.requestRevision(1, "Needs changes", 1L);

        assertEquals(200, result.get("statusCode"));
        assertEquals("RevisionRequired", result.get("decision"));
        assertEquals("Pending", result.get("status"));
        assertEquals("Revision requested. Creator notified.", result.get("message"));
        verify(repository).save(argThat(r ->
            "RevisionRequired".equals(r.getDecision()) && "Pending".equals(r.getStatus())));
    }

    // TC-16: Request revision — not found
    @Test
    void requestRevision_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.requestRevision(99, "Remarks", 1L);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Review not found", result.get("error"));
    }

    // TC-17: Approve review sets reviewDate
    @Test
    void approveReview_setsReviewDate() {
        when(repository.findById(1)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        service.approveReview(1, "OK", 1L);

        verify(repository).save(argThat(r -> r.getReviewDate() != null));
    }

    // TC-18: Reject review sets remarks correctly
    @Test
    void rejectReview_setsRemarks() {
        when(repository.findById(1)).thenReturn(Optional.of(review));
        when(repository.save(any())).thenReturn(review);

        service.rejectReview(1, "Plagiarism detected", 1L);

        verify(repository).save(argThat(r -> "Plagiarism detected".equals(r.getRemarks())));
    }
}
