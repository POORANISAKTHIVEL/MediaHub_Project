package com.mediahub.subscriptionPlan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.subscriptionPlan.dto.CreatePlanRequest;
import com.mediahub.subscriptionPlan.dto.UpdatePlanRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionPlan;
import com.mediahub.subscriptionPlan.service.SubscriptionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    @InjectMocks
    private SubscriptionPlanController subscriptionPlanController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SubscriptionPlan plan;
    private CreatePlanRequest createRequest;
    private UpdatePlanRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(subscriptionPlanController)
                .build();

        plan = SubscriptionPlan.builder()
                .planId(1L)
                .name("Premium")
                .price(14.99)
                .billingCycle("Monthly")
                .contentAccessLevel("4K HDR")
                .maxDevices(5)
                .downloadAllowed(1)
                .status("Active")
                .build();

        createRequest = new CreatePlanRequest();
        createRequest.setName("Premium");
        createRequest.setPrice(14.99);
        createRequest.setBillingCycle("Monthly");
        createRequest.setContentAccessLevel("4K HDR");
        createRequest.setMaxDevices(5);
        createRequest.setDownloadAllowed(1);

        updateRequest = new UpdatePlanRequest();
        updateRequest.setPrice(19.99);
        updateRequest.setBillingCycle("Quarterly");
    }

    @Test
    void createPlan_Returns201() throws Exception {
        when(subscriptionPlanService.createPlan(any(CreatePlanRequest.class)))
                .thenReturn(Map.of("message", "Plan created successfully"));

        mockMvc.perform(post("/mediaHub/subscriptionPlan/plans/createPlan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Plan created successfully"));
    }

    @Test
    void createPlan_DuplicatePlan_Returns409() throws Exception {
        when(subscriptionPlanService.createPlan(any(CreatePlanRequest.class)))
                .thenThrow(new IllegalStateException("Plan already exists"));

        mockMvc.perform(post("/mediaHub/subscriptionPlan/plans/createPlan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Plan already exists"));
    }

    @Test
    void fetchPlans_Returns200() throws Exception {
        when(subscriptionPlanService.fetchPlans())
                .thenReturn(Arrays.asList(plan));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/plans/fetchPlans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Premium"))
                .andExpect(jsonPath("$[0].price").value(14.99));
    }

    @Test
    void fetchPlanById_Returns200() throws Exception {
        when(subscriptionPlanService.fetchPlanById(1L))
                .thenReturn(plan);

        mockMvc.perform(get("/mediaHub/subscriptionPlan/plans/fetchPlan/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(1))
                .andExpect(jsonPath("$.name").value("Premium"));
    }

    @Test
    void fetchPlanById_NotFound_Returns404() throws Exception {
        when(subscriptionPlanService.fetchPlanById(99L))
                .thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/plans/fetchPlan/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plan not found"));
    }

    @Test
    void updatePlan_Returns200() throws Exception {
        when(subscriptionPlanService.updatePlan(eq(1L), any(UpdatePlanRequest.class)))
                .thenReturn(Map.of("message", "Plan updated successfully"));

        mockMvc.perform(put("/mediaHub/subscriptionPlan/plans/updatePlan/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plan updated successfully"));
    }

    @Test
    void updatePlan_NotFound_Returns404() throws Exception {
        when(subscriptionPlanService.updatePlan(eq(99L), any(UpdatePlanRequest.class)))
                .thenThrow(new RuntimeException("Plan not found"));

        mockMvc.perform(put("/mediaHub/subscriptionPlan/plans/updatePlan/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plan not found"));
    }
}