package com.mediahub.subscriptionPlan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.subscriptionPlan.dto.CreateHistoryRequest;
import com.mediahub.subscriptionPlan.dto.UpdateHistoryRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import com.mediahub.subscriptionPlan.service.SubscriptionHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionHistoryService subscriptionHistoryService;

    @InjectMocks
    private SubscriptionHistoryController subscriptionHistoryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SubscriptionHistory history;
    private CreateHistoryRequest createRequest;
    private UpdateHistoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subscriptionHistoryController)
                .build();

        history = SubscriptionHistory.builder()
                .historyId(1L)
                .subscriptionId(1L)
                .userId(1L)
                .fromPlanId(1L)
                .toPlanId(2L)
                .changeType("Upgrade")
                .changeDate(LocalDateTime.of(2025, 1, 1, 9, 0))
                .remarks("Upgraded to Premium")
                .build();

        createRequest = new CreateHistoryRequest();
        createRequest.setSubscriptionId(1L);
        createRequest.setUserId(1L);
        createRequest.setFromPlanId(1L);
        createRequest.setToPlanId(2L);
        createRequest.setChangeType("Upgrade");
        createRequest.setChangeDate(LocalDateTime.of(2025, 1, 1, 9, 0));
        createRequest.setRemarks("Upgraded to Premium");

        updateRequest = new UpdateHistoryRequest();
        updateRequest.setRemarks("Upgraded after promo offer");
        updateRequest.setChangeDate(LocalDateTime.of(2025, 4, 15, 10, 0));
    }

    @Test
    void createHistory_Returns201() throws Exception {
        when(subscriptionHistoryService.createHistory(any(CreateHistoryRequest.class)))
                .thenReturn(Map.of("message", "History created successfully"));

        // Use raw JSON string to avoid LocalDateTime serialization issue
        String json = "{"
                + "\"subscriptionId\": 1,"
                + "\"userId\": 1,"
                + "\"fromPlanId\": 1,"
                + "\"toPlanId\": 2,"
                + "\"changeType\": \"Upgrade\","
                + "\"changeDate\": \"2025-01-01T09:00:00\","
                + "\"remarks\": \"Upgraded to Premium\""
                + "}";

        mockMvc.perform(post("/mediaHub/subscriptionPlan/subscriptionhistory/createHistory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("History created successfully"));
    }

    @Test
    void createHistory_AlreadyExists_Returns409() throws Exception {
        when(subscriptionHistoryService.createHistory(any(CreateHistoryRequest.class)))
                .thenThrow(new IllegalStateException("History already exists"));

        String json = "{"
                + "\"subscriptionId\": 1,"
                + "\"userId\": 1,"
                + "\"fromPlanId\": 1,"
                + "\"toPlanId\": 2,"
                + "\"changeType\": \"Upgrade\","
                + "\"changeDate\": \"2025-01-01T09:00:00\","
                + "\"remarks\": \"Upgraded to Premium\""
                + "}";

        mockMvc.perform(post("/mediaHub/subscriptionPlan/subscriptionhistory/createHistory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("History already exists"));
    }

    @Test
    void fetchHistories_Returns200() throws Exception {
        when(subscriptionHistoryService.fetchHistories())
                .thenReturn(Arrays.asList(history));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/subscriptionhistory/fetchHistories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeType").value("Upgrade"))
                .andExpect(jsonPath("$[0].remarks").value("Upgraded to Premium"));
    }

    @Test
    void fetchHistoryById_Returns200() throws Exception {
        when(subscriptionHistoryService.fetchHistoryById(1L))
                .thenReturn(history);

        mockMvc.perform(get("/mediaHub/subscriptionPlan/subscriptionhistory/fetchHistory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historyId").value(1))
                .andExpect(jsonPath("$.changeType").value("Upgrade"));
    }

    @Test
    void fetchHistoryById_NotFound_Returns404() throws Exception {
        when(subscriptionHistoryService.fetchHistoryById(99L))
                .thenThrow(new RuntimeException("History not found"));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/subscriptionhistory/fetchHistory/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("History not found"));
    }

    @Test
    void updateHistory_Returns200() throws Exception {
        when(subscriptionHistoryService.updateHistory(eq(1L), any(UpdateHistoryRequest.class)))
                .thenReturn(Map.of("message", "History updated successfully"));

        String json = "{"
                + "\"remarks\": \"Upgraded after promo offer\","
                + "\"changeDate\": \"2025-04-15T10:00:00\""
                + "}";

        mockMvc.perform(put("/mediaHub/subscriptionPlan/subscriptionhistory/updateHistory/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("History updated successfully"));
    }

    @Test
    void updateHistory_NotFound_Returns404() throws Exception {
        when(subscriptionHistoryService.updateHistory(eq(99L), any(UpdateHistoryRequest.class)))
                .thenThrow(new RuntimeException("History not found"));

        String json = "{"
                + "\"remarks\": \"Upgraded after promo offer\","
                + "\"changeDate\": \"2025-04-15T10:00:00\""
                + "}";

        mockMvc.perform(put("/mediaHub/subscriptionPlan/subscriptionhistory/updateHistory/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("History not found"));
    }
}