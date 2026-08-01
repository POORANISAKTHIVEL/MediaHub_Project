package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreatePlanRequest;
import com.mediahub.subscriptionPlan.dto.UpdatePlanRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionPlan;
import com.mediahub.subscriptionPlan.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @InjectMocks
    private SubscriptionPlanService subscriptionPlanService;

    private SubscriptionPlan plan;
    private CreatePlanRequest createRequest;
    private UpdatePlanRequest updateRequest;

    @BeforeEach
    void setUp() {
        plan = SubscriptionPlan.builder()
                .planId(1L).name("Premium").price(14.99)
                .billingCycle("Monthly").contentAccessLevel("4K HDR")
                .maxDevices(5).downloadAllowed(1).status("Active")
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
        updateRequest.setMaxDevices(6);
        updateRequest.setDownloadAllowed(1);
    }

    @Test
    void createPlan_Success() {
        when(subscriptionPlanRepository.findByName("Premium"))
                .thenReturn(Optional.empty());
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class)))
                .thenReturn(plan);

        Map<String, String> result = subscriptionPlanService.createPlan(createRequest);

        assertEquals("Plan created successfully", result.get("message"));
        verify(subscriptionPlanRepository, times(1)).save(any(SubscriptionPlan.class));
    }

    @Test
    void createPlan_AlreadyExists_ThrowsException() {
        when(subscriptionPlanRepository.findByName("Premium"))
                .thenReturn(Optional.of(plan));

        assertThrows(IllegalStateException.class,
                () -> subscriptionPlanService.createPlan(createRequest));
        verify(subscriptionPlanRepository, never()).save(any());
    }

    @Test
    void fetchPlans_Success() {
        when(subscriptionPlanRepository.findAll())
                .thenReturn(Arrays.asList(plan));

        List<SubscriptionPlan> result = subscriptionPlanService.fetchPlans();

        assertEquals(1, result.size());
        assertEquals("Premium", result.get(0).getName());
    }

    @Test
    void fetchPlanById_Success() {
        when(subscriptionPlanRepository.findById(1L))
                .thenReturn(Optional.of(plan));

        SubscriptionPlan result = subscriptionPlanService.fetchPlanById(1L);

        assertEquals(1L, result.getPlanId());
        assertEquals("Premium", result.getName());
        assertEquals(14.99, result.getPrice());
    }

    @Test
    void fetchPlanById_NotFound_ThrowsException() {
        when(subscriptionPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionPlanService.fetchPlanById(99L));
    }

    @Test
    void updatePlan_Success() {
        when(subscriptionPlanRepository.findById(1L))
                .thenReturn(Optional.of(plan));
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class)))
                .thenReturn(plan);

        Map<String, String> result = subscriptionPlanService.updatePlan(1L, updateRequest);

        assertEquals("Plan updated successfully", result.get("message"));
        verify(subscriptionPlanRepository, times(1)).save(any(SubscriptionPlan.class));
    }

    @Test
    void updatePlan_NotFound_ThrowsException() {
        when(subscriptionPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionPlanService.updatePlan(99L, updateRequest));
    }
}