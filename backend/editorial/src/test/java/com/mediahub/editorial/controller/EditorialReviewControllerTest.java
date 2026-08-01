package com.mediahub.editorial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.editorial.model.EditorialReview;
import com.mediahub.editorial.service.EditorialReviewService;
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

@WebMvcTest(EditorialReviewController.class)
public class EditorialReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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

    // TC-01: POST /reviews — 201 Submitted
    @Test
    void submitReview_returns201() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 201);
        resp.put("reviewID", 1);
        resp.put("status", "Pending");
        resp.put("message", "Content submitted for review successfully");
        when(service.submitReview(any(EditorialReview.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Pending"))
                .andExpect(jsonPath("$.message")
                        .value("Content submitted for review successfully"));
    }

    // TC-02: POST /reviews — 400 Bad Request
    @Test
    void submitReview_returns400() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 400);
        resp.put("error", "ContentID is required");
        when(service.submitReview(any(EditorialReview.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ContentID is required"));
    }

    // TC-03: GET /reviews — 200 with list
    @Test
    void getAllReviews_returns200() throws Exception {
        when(service.getAllReviews())
                .thenReturn(Arrays.asList(review, new EditorialReview()));

        mockMvc.perform(get("/MediaHub/editorial/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // TC-04: GET /reviews — 200 empty list
    @Test
    void getAllReviews_returnsEmptyList() throws Exception {
        when(service.getAllReviews()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/MediaHub/editorial/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // TC-05: GET /reviews/{reviewID} — 200 Found
    @Test
    void getReviewById_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("review", review);
        when(service.getReviewById(1)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").exists());
    }

    // TC-06: GET /reviews/{reviewID} — 404 Not Found
    @Test
    void getReviewById_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Review not found with ID: 99");
        when(service.getReviewById(99)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/reviews/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Review not found with ID: 99"));
    }

    // TC-07: POST /reviews/{reviewID}/approve — 200 Approved
    @Test
    void approveReview_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("decision", "Approved");
        resp.put("status", "Completed");
        resp.put("message", "Content approved successfully.");
        when(service.approveReview(eq(1), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Well written");
        mockMvc.perform(post("/MediaHub/editorial/reviews/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("Approved"))
                .andExpect(jsonPath("$.status").value("Completed"))
                .andExpect(jsonPath("$.message").value("Content approved successfully."));
    }

    // TC-08: POST /reviews/{reviewID}/approve — 404 Not Found
    @Test
    void approveReview_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Review not found");
        when(service.approveReview(eq(99), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Remarks");
        mockMvc.perform(post("/MediaHub/editorial/reviews/99/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Review not found"));
    }

    // TC-09: POST /reviews/{reviewID}/reject — 200 Rejected
    @Test
    void rejectReview_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("decision", "Rejected");
        resp.put("status", "Completed");
        resp.put("message", "Content rejected. Creator notified.");
        when(service.rejectReview(eq(1), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Not acceptable");
        mockMvc.perform(post("/MediaHub/editorial/reviews/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("Rejected"))
                .andExpect(jsonPath("$.message").value("Content rejected. Creator notified."));
    }

    // TC-10: POST /reviews/{reviewID}/reject — 404 Not Found
    @Test
    void rejectReview_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Review not found");
        when(service.rejectReview(eq(99), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Remarks");
        mockMvc.perform(post("/MediaHub/editorial/reviews/99/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // TC-11: POST /reviews/{reviewID}/revise — 200 Revision Requested
    @Test
    void requestRevision_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("decision", "RevisionRequired");
        resp.put("status", "Pending");
        resp.put("message", "Revision requested. Creator notified.");
        when(service.requestRevision(eq(1), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Needs changes");
        mockMvc.perform(post("/MediaHub/editorial/reviews/1/revise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("RevisionRequired"))
                .andExpect(jsonPath("$.status").value("Pending"))
                .andExpect(jsonPath("$.message")
                        .value("Revision requested. Creator notified."));
    }

    // TC-12: POST /reviews/{reviewID}/revise — 404 Not Found
    @Test
    void requestRevision_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Review not found");
        when(service.requestRevision(eq(99), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("remarks", "Remarks");
        mockMvc.perform(post("/MediaHub/editorial/reviews/99/revise")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Review not found"));
    }
}
