package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyStatement;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.GlobalExceptionHandler;
import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.service.RoyaltyStatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RoyaltyStatementControllerTest {

    @Mock
    private RoyaltyStatementService service;

    @InjectMocks
    private RoyaltyStatementController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─── generateStatement ────────────────────────────────────────────────────

    // CT-14: POST /api/royalty-statements — 201 Created
    @Test
    @DisplayName("CT-14: POST /api/royalty-statements returns 201 on success")
    void generateStatement_returns201() throws Exception {
        RoyaltyStatement created = new RoyaltyStatement();
        created.setCreatorID(501);
        created.setPeriod("2025-Q1");
        created.setTotalViews(50000);
        created.setTotalRevenue(2000.0);
        created.setRoyaltyAmount(600.0);
        created.setStatus("Draft");
        when(service.generateStatement(any(RoyaltyStatement.class))).thenReturn(created);

        mockMvc.perform(post("/api/royalty-statements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorID\":501,\"period\":\"2025-Q1\","
                + "\"totalViews\":50000,\"totalRevenue\":2000.0,\"royaltyAmount\":600.0}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("Draft"))
            .andExpect(jsonPath("$.creatorID").value(501));
    }

    // CT-15: POST /api/royalty-statements — 400 missing CreatorID
    @Test
    @DisplayName("CT-15: POST /api/royalty-statements returns 400 when CreatorID missing")
    void generateStatement_returns400_missingCreatorID() throws Exception {
        when(service.generateStatement(any(RoyaltyStatement.class)))
            .thenThrow(new BadRequestException("CreatorID is required"));

        mockMvc.perform(post("/api/royalty-statements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"period\":\"2025-Q1\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("CreatorID is required"));
    }

    // CT-16: POST /api/royalty-statements — 400 negative revenue
    @Test
    @DisplayName("CT-16: POST /api/royalty-statements returns 400 for negative revenue")
    void generateStatement_returns400_negativeRevenue() throws Exception {
        when(service.generateStatement(any(RoyaltyStatement.class)))
            .thenThrow(new BadRequestException("TotalRevenue cannot be negative"));

        mockMvc.perform(post("/api/royalty-statements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorID\":501,\"period\":\"2025-Q1\",\"totalRevenue\":-100}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("TotalRevenue cannot be negative"));
    }

    // CT-17: POST /api/royalty-statements — 500 repository failure
    @Test
    @DisplayName("CT-17: POST /api/royalty-statements returns 500 on repository failure")
    void generateStatement_returns500() throws Exception {
        when(service.generateStatement(any(RoyaltyStatement.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/royalty-statements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorID\":501,\"period\":\"2025-Q1\",\"totalRevenue\":2000.0}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    // ─── getAllStatements ─────────────────────────────────────────────────────

    // CT-18: GET /api/royalty-statements — 200 with list
    @Test
    @DisplayName("CT-18: GET /api/royalty-statements returns 200 with list")
    void getAllStatements_returns200() throws Exception {
        when(service.getAllStatements()).thenReturn(
                Arrays.asList(new RoyaltyStatement(), new RoyaltyStatement()));

        mockMvc.perform(get("/api/royalty-statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // CT-19: GET /api/royalty-statements — 200 empty list
    @Test
    @DisplayName("CT-19: GET /api/royalty-statements returns 200 with empty list")
    void getAllStatements_returns200Empty() throws Exception {
        when(service.getAllStatements()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── getStatementById ─────────────────────────────────────────────────────

    // CT-20: GET /api/royalty-statements/{id} — 200 Found
    @Test
    @DisplayName("CT-20: GET /api/royalty-statements/{id} returns 200 when found")
    void getStatementById_returns200() throws Exception {
        RoyaltyStatement s = new RoyaltyStatement();
        when(service.getStatementById(1)).thenReturn(s);

        mockMvc.perform(get("/api/royalty-statements/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Draft"));
    }

    // CT-21: GET /api/royalty-statements/{id} — 404 Not Found
    @Test
    @DisplayName("CT-21: GET /api/royalty-statements/{id} returns 404 when not found")
    void getStatementById_returns404() throws Exception {
        when(service.getStatementById(99))
            .thenThrow(new ResourceNotFoundException("Statement not found with ID: 99"));

        mockMvc.perform(get("/api/royalty-statements/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Statement not found with ID: 99"));
    }

    // ─── finaliseStatement ────────────────────────────────────────────────────

    // CT-22: PUT /api/royalty-statements/{id}/finalise — 200 Success
    @Test
    @DisplayName("CT-22: PUT finalise returns 200 on success")
    void finaliseStatement_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("status", "Finalised");
        response.put("message", "Statement finalised successfully.");
        when(service.finaliseStatement(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-statements/1/finalise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Finalised"))
                .andExpect(jsonPath("$.message").value("Statement finalised successfully."));
    }

    // CT-23: PUT /api/royalty-statements/{id}/finalise — 400 Not Draft
    @Test
    @DisplayName("CT-23: PUT finalise returns 400 when statement not in Draft")
    void finaliseStatement_returns400() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", "Only Draft statements can be finalised.");
        when(service.finaliseStatement(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-statements/1/finalise"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only Draft statements can be finalised."));
    }

    // ─── markAsPaid ───────────────────────────────────────────────────────────

    // CT-24: PUT /api/royalty-statements/{id}/mark-paid — 200 Success
    @Test
    @DisplayName("CT-24: PUT mark-paid returns 200 on success")
    void markAsPaid_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("status", "Paid");
        response.put("message", "Statement marked as Paid successfully.");
        when(service.markAsPaid(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-statements/1/mark-paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Paid"))
                .andExpect(jsonPath("$.message").value("Statement marked as Paid successfully."));
    }

    // CT-25: PUT /api/royalty-statements/{id}/mark-paid — 400 Not Finalised
    @Test
    @DisplayName("CT-25: PUT mark-paid returns 400 when statement not Finalised")
    void markAsPaid_returns400() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", "Only Finalised statements can be marked as Paid.");
        when(service.markAsPaid(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-statements/1/mark-paid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Only Finalised statements can be marked as Paid."));
    }

    // CT-26: GET /api/royalty-statements — service called exactly once
    @Test
    @DisplayName("CT-26: GET /api/royalty-statements verifies service called once")
    void getAllStatements_serviceCalledOnce() throws Exception {
        when(service.getAllStatements()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-statements"));

        verify(service, times(1)).getAllStatements();
    }
}
