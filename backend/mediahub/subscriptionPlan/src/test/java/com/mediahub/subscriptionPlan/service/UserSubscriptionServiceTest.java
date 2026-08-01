package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateSubscriptionRequest;
import com.mediahub.subscriptionPlan.dto.UpdateSubscriptionRequest;
import com.mediahub.subscriptionPlan.model.UserSubscription;
import com.mediahub.subscriptionPlan.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks
    private UserSubscriptionService userSubscriptionService;

    private UserSubscription subscription;
    private CreateSubscriptionRequest createRequest;
    private UpdateSubscriptionRequest updateRequest;

    @BeforeEach
    void setUp() {
        subscription = UserSubscription.builder()
                .subscriptionId(1L).userId(1L).planId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .renewalType("AutoRenew").status("Active")
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
    void createSubscription_Success() {
        when(userSubscriptionRepository.findByUserIdAndStatus(1L, "Active"))
                .thenReturn(Optional.empty());
        when(userSubscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(subscription);

        Map<String, String> result = userSubscriptionService.createSubscription(createRequest);

        assertEquals("Subscription created successfully", result.get("message"));
        verify(userSubscriptionRepository, times(1)).save(any(UserSubscription.class));
    }

    @Test
    void createSubscription_ActiveAlreadyExists_ThrowsException() {
        when(userSubscriptionRepository.findByUserIdAndStatus(1L, "Active"))
                .thenReturn(Optional.of(subscription));

        assertThrows(IllegalStateException.class,
                () -> userSubscriptionService.createSubscription(createRequest));
        verify(userSubscriptionRepository, never()).save(any());
    }

    @Test
    void fetchSubscriptions_Success() {
        when(userSubscriptionRepository.findAll())
                .thenReturn(Arrays.asList(subscription));

        List<UserSubscription> result = userSubscriptionService.fetchSubscriptions();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void fetchSubscriptionById_Success() {
        when(userSubscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));

        UserSubscription result = userSubscriptionService.fetchSubscriptionById(1L);

        assertEquals(1L, result.getSubscriptionId());
        assertEquals("AutoRenew", result.getRenewalType());
        assertEquals("Active", result.getStatus());
    }

    @Test
    void fetchSubscriptionById_NotFound_ThrowsException() {
        when(userSubscriptionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userSubscriptionService.fetchSubscriptionById(99L));
    }

    @Test
    void updateSubscription_Success() {
        when(userSubscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription));
        when(userSubscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(subscription);

        Map<String, String> result = userSubscriptionService.updateSubscription(1L, updateRequest);

        assertEquals("Subscription updated successfully", result.get("message"));
        verify(userSubscriptionRepository, times(1)).save(any(UserSubscription.class));
    }

    @Test
    void updateSubscription_NotFound_ThrowsException() {
        when(userSubscriptionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userSubscriptionService.updateSubscription(99L, updateRequest));
    }
}