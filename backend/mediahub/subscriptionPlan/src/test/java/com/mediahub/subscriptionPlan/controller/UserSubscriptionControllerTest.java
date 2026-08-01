package com.mediahub.subscriptionPlan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.subscriptionPlan.dto.CreateSubscriptionRequest;
import com.mediahub.subscriptionPlan.dto.UpdateSubscriptionRequest;
import com.mediahub.subscriptionPlan.model.UserSubscription;
import com.mediahub.subscriptionPlan.service.UserSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserSubscriptionService userSubscriptionService;

    @InjectMocks
    private UserSubscriptionController userSubscriptionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private UserSubscription subscription;
    private CreateSubscriptionRequest createRequest;
    private UpdateSubscriptionRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userSubscriptionController)
                .build();

        subscription = UserSubscription.builder()
                .subscriptionId(1L)
                .userId(1L)
                .planId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .renewalType("AutoRenew")
                .status("Active")
                .build();

        createRequest = new CreateSubscriptionRequest();
        createRequest.setUserId(1L);
        createRequest.setPlanId(1L);
        createRequest.setStartDate(LocalDate.of(2025, 1, 1));
        createRequest.setEndDate(LocalDate.of(2025, 1, 31));
        createRequest.setRenewalType("AutoRenew");

        updateRequest = new UpdateSubscriptionRequest();
        updateRequest.setPlanId(2L);
        updateRequest.setEndDate(LocalDate.of(2025, 6, 30));
        updateRequest.setRenewalType("Manual");
    }

    @Test
    void createSubscription_Returns201() throws Exception {
        when(userSubscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenReturn(Map.of("message", "Subscription created successfully"));

        // Use raw JSON string instead of objectMapper to avoid LocalDate issue
        String json = "{"
                + "\"userId\": 1,"
                + "\"planId\": 1,"
                + "\"startDate\": \"2025-01-01\","
                + "\"endDate\": \"2025-01-31\","
                + "\"renewalType\": \"AutoRenew\""
                + "}";

        mockMvc.perform(post("/mediaHub/subscriptionPlan/usersubscriptions/createSubscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Subscription created successfully"));
    }

    @Test
    void createSubscription_ActiveExists_Returns409() throws Exception {
        when(userSubscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenThrow(new IllegalStateException("Active subscription already exists"));

        String json = "{"
                + "\"userId\": 1,"
                + "\"planId\": 1,"
                + "\"startDate\": \"2025-01-01\","
                + "\"endDate\": \"2025-01-31\","
                + "\"renewalType\": \"AutoRenew\""
                + "}";

        mockMvc.perform(post("/mediaHub/subscriptionPlan/usersubscriptions/createSubscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Active subscription already exists"));
    }

    @Test
    void fetchSubscriptions_Returns200() throws Exception {
        when(userSubscriptionService.fetchSubscriptions())
                .thenReturn(Arrays.asList(subscription));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/usersubscriptions/fetchSubscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].status").value("Active"));
    }

    @Test
    void fetchSubscriptionById_Returns200() throws Exception {
        when(userSubscriptionService.fetchSubscriptionById(1L))
                .thenReturn(subscription);

        mockMvc.perform(get("/mediaHub/subscriptionPlan/usersubscriptions/fetchSubscription/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(1))
                .andExpect(jsonPath("$.renewalType").value("AutoRenew"));
    }

    @Test
    void fetchSubscriptionById_NotFound_Returns404() throws Exception {
        when(userSubscriptionService.fetchSubscriptionById(99L))
                .thenThrow(new RuntimeException("Subscription not found"));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/usersubscriptions/fetchSubscription/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription not found"));
    }

    @Test
    void updateSubscription_Returns200() throws Exception {
        when(userSubscriptionService.updateSubscription(eq(1L), any(UpdateSubscriptionRequest.class)))
                .thenReturn(Map.of("message", "Subscription updated successfully"));

        String json = "{"
                + "\"planId\": 2,"
                + "\"endDate\": \"2025-06-30\","
                + "\"renewalType\": \"Manual\""
                + "}";

        mockMvc.perform(put("/mediaHub/subscriptionPlan/usersubscriptions/updateSubscription/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subscription updated successfully"));
    }

    @Test
    void updateSubscription_NotFound_Returns404() throws Exception {
        when(userSubscriptionService.updateSubscription(eq(99L), any(UpdateSubscriptionRequest.class)))
                .thenThrow(new RuntimeException("Subscription not found"));

        String json = "{"
                + "\"planId\": 2,"
                + "\"endDate\": \"2025-06-30\","
                + "\"renewalType\": \"Manual\""
                + "}";

        mockMvc.perform(put("/mediaHub/subscriptionPlan/usersubscriptions/updateSubscription/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription not found"));
    }
}