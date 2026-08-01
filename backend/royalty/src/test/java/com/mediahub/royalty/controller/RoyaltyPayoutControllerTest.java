package com.mediahub.royalty.controller;

import com.mediahub.royalty.model.RoyaltyPayout;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.GlobalExceptionHandler;
import com.mediahub.royalty.exception.ResourceNotFoundException;
import com.mediahub.royalty.service.RoyaltyPayoutService;
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
public class RoyaltyPayoutControllerTest {

    @Mock
    private RoyaltyPayoutService service;

    @InjectMocks
    private RoyaltyPayoutController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─── createPayout ─────────────────────────────────────────────────────────

    // CT-27: POST /api/royalty-payouts — 201 Created
    @Test
    @DisplayName("CT-27: POST /api/royalty-payouts returns 201 on success")
    void createPayout_returns201() throws Exception {
        RoyaltyPayout created = new RoyaltyPayout();
        created.setStatementID(10);
        created.setCreatorID(501);
        created.setAmount(750.0);
        created.setMethod("BankTransfer");
        created.setStatus("Pending");
        when(service.createPayout(any(RoyaltyPayout.class))).thenReturn(created);

        mockMvc.perform(post("/api/royalty-payouts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"statementID\":10,\"creatorID\":501,"
                + "\"amount\":750.0,\"method\":\"BankTransfer\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("Pending"))
            .andExpect(jsonPath("$.creatorID").value(501));
    }

    // CT-28: POST /api/royalty-payouts — 400 missing StatementID
    @Test
    @DisplayName("CT-28: POST /api/royalty-payouts returns 400 when StatementID missing")
    void createPayout_returns400_missingStatementID() throws Exception {
        when(service.createPayout(any(RoyaltyPayout.class)))
            .thenThrow(new BadRequestException("StatementID is required"));

        mockMvc.perform(post("/api/royalty-payouts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creatorID\":501,\"amount\":750.0,\"method\":\"BankTransfer\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("StatementID is required"));
    }

    // CT-29: POST /api/royalty-payouts — 400 invalid method
    @Test
    @DisplayName("CT-29: POST /api/royalty-payouts returns 400 for invalid method")
    void createPayout_returns400_invalidMethod() throws Exception {
        when(service.createPayout(any(RoyaltyPayout.class)))
            .thenThrow(new BadRequestException("Method must be BankTransfer or WalletCredit"));

        mockMvc.perform(post("/api/royalty-payouts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"statementID\":10,\"creatorID\":501,\"amount\":750.0,\"method\":\"Cash\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Method must be BankTransfer or WalletCredit"));
    }

    // CT-30: POST /api/royalty-payouts — 400 zero amount
    @Test
    @DisplayName("CT-30: POST /api/royalty-payouts returns 400 for zero amount")
    void createPayout_returns400_zeroAmount() throws Exception {
        when(service.createPayout(any(RoyaltyPayout.class)))
            .thenThrow(new BadRequestException("Amount must be greater than zero"));

        mockMvc.perform(post("/api/royalty-payouts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"statementID\":10,\"creatorID\":501,\"amount\":0,\"method\":\"BankTransfer\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));
    }

    // CT-31: POST /api/royalty-payouts — 500 repository failure
    @Test
    @DisplayName("CT-31: POST /api/royalty-payouts returns 500 on repository failure")
    void createPayout_returns500() throws Exception {
        when(service.createPayout(any(RoyaltyPayout.class))).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/api/royalty-payouts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"statementID\":10,\"creatorID\":501,\"amount\":750.0,\"method\":\"BankTransfer\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    // ─── getAllPayouts ────────────────────────────────────────────────────────

    // CT-32: GET /api/royalty-payouts — 200 with list
    @Test
    @DisplayName("CT-32: GET /api/royalty-payouts returns 200 with list")
    void getAllPayouts_returns200() throws Exception {
        when(service.getAllPayouts()).thenReturn(
                Arrays.asList(new RoyaltyPayout(), new RoyaltyPayout()));

        mockMvc.perform(get("/api/royalty-payouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // CT-33: GET /api/royalty-payouts — 200 empty list
    @Test
    @DisplayName("CT-33: GET /api/royalty-payouts returns 200 empty list")
    void getAllPayouts_returns200Empty() throws Exception {
        when(service.getAllPayouts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-payouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── getPayoutById ────────────────────────────────────────────────────────

    // CT-34: GET /api/royalty-payouts/{id} — 200 Found
    @Test
    @DisplayName("CT-34: GET /api/royalty-payouts/{id} returns 200 when found")
    void getPayoutById_returns200() throws Exception {
        RoyaltyPayout p = new RoyaltyPayout();
        when(service.getPayoutById(1)).thenReturn(p);

        mockMvc.perform(get("/api/royalty-payouts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Pending"));
    }

    // CT-35: GET /api/royalty-payouts/{id} — 404 Not Found
    @Test
    @DisplayName("CT-35: GET /api/royalty-payouts/{id} returns 404 when not found")
    void getPayoutById_returns404() throws Exception {
        when(service.getPayoutById(99))
            .thenThrow(new ResourceNotFoundException("Payout not found with ID: 99"));

        mockMvc.perform(get("/api/royalty-payouts/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Payout not found with ID: 99"));
    }

    // ─── processPayout ────────────────────────────────────────────────────────

    // CT-36: PUT /api/royalty-payouts/{id}/process — 200 Success
    @Test
    @DisplayName("CT-36: PUT /process returns 200 on success")
    void processPayout_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("status", "Processed");
        response.put("message", "Payout processed successfully.");
        when(service.processPayout(1)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-payouts/1/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Processed"))
                .andExpect(jsonPath("$.message").value("Payout processed successfully."));
    }

    // CT-37: PUT /api/royalty-payouts/{id}/process — 404 Not Found
    @Test
    @DisplayName("CT-37: PUT /process returns 404 when not found")
    void processPayout_returns404() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 404);
        response.put("error", "Payout not found");
        when(service.processPayout(99)).thenReturn(response);

        mockMvc.perform(put("/api/royalty-payouts/99/process"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payout not found"));
    }

    // ─── failPayout ───────────────────────────────────────────────────────────

    // CT-38: PUT /api/royalty-payouts/{id}/fail — 200 Success
    @Test
    @DisplayName("CT-38: PUT /fail returns 200 on success")
    void failPayout_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("status", "Failed");
        response.put("reason", "Insufficient funds");
        response.put("message", "Payout marked as failed.");
        when(service.failPayout(eq(1), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/royalty-payouts/1/fail")
                .param("reason", "Insufficient funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Failed"))
                .andExpect(jsonPath("$.reason").value("Insufficient funds"));
    }

    // CT-39: PUT /api/royalty-payouts/{id}/fail — 404 Not Found
    @Test
    @DisplayName("CT-39: PUT /fail returns 404 when not found")
    void failPayout_returns404() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 404);
        response.put("error", "Payout not found");
        when(service.failPayout(eq(99), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/royalty-payouts/99/fail")
                .param("reason", "Error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payout not found"));
    }

    // ─── deletePayout ─────────────────────────────────────────────────────────

    // CT-40: DELETE /api/royalty-payouts/{id} — 200 Deleted
    @Test
    @DisplayName("CT-40: DELETE /api/royalty-payouts/{id} returns 200 on success")
    void deletePayout_returns200() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("message", "Payout deleted successfully.");
        when(service.deletePayout(1)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-payouts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payout deleted successfully."));
    }

    // CT-41: DELETE /api/royalty-payouts/{id} — 400 Processed blocked
    @Test
    @DisplayName("CT-41: DELETE returns 400 when payout is Processed")
    void deletePayout_returns400_processed() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 400);
        response.put("error", "Cannot delete Processed payout.");
        when(service.deletePayout(1)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-payouts/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete Processed payout."));
    }

    // CT-42: DELETE /api/royalty-payouts/{id} — 404 Not Found
    @Test
    @DisplayName("CT-42: DELETE returns 404 when payout not found")
    void deletePayout_returns404() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 404);
        response.put("error", "Payout not found");
        when(service.deletePayout(99)).thenReturn(response);

        mockMvc.perform(delete("/api/royalty-payouts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payout not found"));
    }

    // CT-43: GET /api/royalty-payouts — service called exactly once
    @Test
    @DisplayName("CT-43: GET /api/royalty-payouts verifies service called once")
    void getAllPayouts_serviceCalledOnce() throws Exception {
        when(service.getAllPayouts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/royalty-payouts"));

        verify(service, times(1)).getAllPayouts();
    }
}
