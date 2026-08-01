package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyRule;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.GlobalExceptionHandler;
import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.service.RoyaltyRuleService;
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
public class RoyaltyRuleControllerTest {

    @Mock
    private RoyaltyRuleService service;

    @InjectMocks
    private RoyaltyRuleController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─── createRule ───────────────────────────────────────────────────────────

    // CT-01: POST /api/royalty-rules — 201 Created
    @Test
    @DisplayName("CT-01: POST /api/royalty-rules returns 201 on success")
    void createRule_returns201() throws Exception {
        RoyaltyRule created = new RoyaltyRule();
        created.setCreatorTier("Gold");
        created.setRevenueSharePercent(30.0);
        created.setPayoutFrequency("Monthly");
        created.setStatus("Active");
        when(service.createRule(any(RoyaltyRule.class))).thenReturn(created);

        mockMvc.perform(post("/api/royalty-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorTier\":\"Gold\",\"revenueSharePercent\":30.0,"
                + "\"payoutFrequency\":\"Monthly\",\"effectiveDate\":\"2025-01-01\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("Active"))
            .andExpect(jsonPath("$.creatorTier").value("Gold"));
    }

    // CT-02: POST /api/royalty-rules — 400 Bad Request (validation fails)
    @Test
    @DisplayName("CT-02: POST /api/royalty-rules returns 400 when validation fails")
    void createRule_returns400() throws Exception {
        when(service.createRule(any(RoyaltyRule.class)))
            .thenThrow(new BadRequestException("CreatorTier is required"));

        mockMvc.perform(post("/api/royalty-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"revenueSharePercent\":30.0}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("CreatorTier is required"));
    }

    // CT-03: POST /api/royalty-rules — 500 Internal Server Error
    @Test
    @DisplayName("CT-03: POST /api/royalty-rules returns 500 on repository failure")
    void createRule_returns500() throws Exception {
        when(service.createRule(any(RoyaltyRule.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/royalty-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorTier\":\"Gold\",\"revenueSharePercent\":30.0,"
                + "\"payoutFrequency\":\"Monthly\",\"effectiveDate\":\"2025-01-01\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    // ─── getAllRules ──────────────────────────────────────────────────────────

    // CT-04: GET /api/royalty-rules — 200 with list
    @Test
    @DisplayName("CT-04: GET /api/royalty-rules returns 200 with list")
    void getAllRules_returns200() throws Exception {
        when(service.getAllRules()).thenReturn(Arrays.asList(new RoyaltyRule(), new RoyaltyRule()));

        mockMvc.perform(get("/api/royalty-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // CT-05: GET /api/royalty-rules — 200 with empty list
    @Test
    @DisplayName("CT-05: GET /api/royalty-rules returns 200 with empty list")
    void getAllRules_returns200EmptyList() throws Exception {
        when(service.getAllRules()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── getRuleById ──────────────────────────────────────────────────────────

    // CT-06: GET /api/royalty-rules/{id} — 200 Found
    @Test
    @DisplayName("CT-06: GET /api/royalty-rules/{id} returns 200 when found")
    void getRuleById_returns200() throws Exception {
        RoyaltyRule r = new RoyaltyRule();
        when(service.getRuleById(1)).thenReturn(r);

        mockMvc.perform(get("/api/royalty-rules/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Active"));
    }

    // CT-07: GET /api/royalty-rules/{id} — 404 Not Found
    @Test
    @DisplayName("CT-07: GET /api/royalty-rules/{id} returns 404 when not found")
    void getRuleById_returns404() throws Exception {
        when(service.getRuleById(99))
            .thenThrow(new ResourceNotFoundException("Royalty rule not found with ID: 99"));

        mockMvc.perform(get("/api/royalty-rules/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Royalty rule not found with ID: 99"));
    }

    // ─── deactivateRule ───────────────────────────────────────────────────────

    // CT-08: PUT /api/royalty-rules/{id}/deactivate — 200 Success
    @Test
    @DisplayName("CT-08: PUT /api/royalty-rules/{id}/deactivate returns 200")
    void deactivateRule_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("status", "Inactive");
        response.put("message", "Royalty rule deactivated successfully.");
        when(service.deactivateRule(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-rules/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Inactive"))
                .andExpect(jsonPath("$.message").value("Royalty rule deactivated successfully."));
    }

    // CT-09: PUT /api/royalty-rules/{id}/deactivate — 404 Not Found
    @Test
    @DisplayName("CT-09: PUT /api/royalty-rules/{id}/deactivate returns 404 when not found")
    void deactivateRule_returns404() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 404);
        response.put("error", "Royalty rule not found");
        when(service.deactivateRule(99)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-rules/99/deactivate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Royalty rule not found"));
    }

    // ─── deleteRule ───────────────────────────────────────────────────────────

    // CT-10: DELETE /api/royalty-rules/{id} — 200 Deleted
    @Test
    @DisplayName("CT-10: DELETE /api/royalty-rules/{id} returns 200 on success")
    void deleteRule_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("message", "Royalty rule deleted successfully.");
        when(service.deleteRule(1)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Royalty rule deleted successfully."));
    }

    // CT-11: DELETE /api/royalty-rules/{id} — 400 Active rule blocked
    @Test
    @DisplayName("CT-11: DELETE /api/royalty-rules/{id} returns 400 for Active rule")
    void deleteRule_returns400_activeRule() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", "Cannot delete Active royalty rule. Deactivate it first.");
        when(service.deleteRule(1)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-rules/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Cannot delete Active royalty rule. Deactivate it first."));
    }

    // CT-12: DELETE /api/royalty-rules/{id} — 404 Not Found
    @Test
    @DisplayName("CT-12: DELETE /api/royalty-rules/{id} returns 404 when not found")
    void deleteRule_returns404() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 404);
        response.put("error", "Royalty rule not found");
        when(service.deleteRule(99)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-rules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Royalty rule not found"));
    }

    // CT-13: GET /api/royalty-rules — service called exactly once
    @Test
    @DisplayName("CT-13: GET /api/royalty-rules verifies service called once")
    void getAllRules_serviceCalledOnce() throws Exception {
        when(service.getAllRules()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-rules"));

        verify(service, times(1)).getAllRules();
    }
}
