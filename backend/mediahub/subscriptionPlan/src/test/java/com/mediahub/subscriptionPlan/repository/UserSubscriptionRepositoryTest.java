package com.mediahub.subscriptionPlan.repository;

import com.mediahub.subscriptionPlan.model.UserSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionRepositoryTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    private UserSubscription subscription1;
    private UserSubscription subscription2;

    @BeforeEach
    void setUp() {
        subscription1 = UserSubscription.builder()
                .subscriptionId(1L)
                .userId(1L)
                .planId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 31))
                .renewalType("AutoRenew")
                .status("Active")
                .build();

        subscription2 = UserSubscription.builder()
                .subscriptionId(2L)
                .userId(2L)
                .planId(2L)
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 28))
                .renewalType("Manual")
                .status("Expired")
                .build();
    }

    // ── Save Tests ────────────────────────────────────────────────

    @Test
    void save_Subscription_Success() {
        when(userSubscriptionRepository.save(any(UserSubscription.class)))
                .thenReturn(subscription1);

        UserSubscription saved = userSubscriptionRepository.save(subscription1);

        assertNotNull(saved);
        assertEquals(1L, saved.getSubscriptionId());
        assertEquals(1L, saved.getUserId());
        assertEquals("AutoRenew", saved.getRenewalType());
        assertEquals("Active", saved.getStatus());
        verify(userSubscriptionRepository, times(1)).save(subscription1);
    }

    // ── FindAll Tests ─────────────────────────────────────────────

    @Test
    void findAll_ReturnsAllSubscriptions() {
        when(userSubscriptionRepository.findAll())
                .thenReturn(Arrays.asList(subscription1, subscription2));

        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();

        assertNotNull(subscriptions);
        assertEquals(2, subscriptions.size());
        assertEquals(1L, subscriptions.get(0).getUserId());
        assertEquals(2L, subscriptions.get(1).getUserId());
        verify(userSubscriptionRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsEmptyList() {
        when(userSubscriptionRepository.findAll())
                .thenReturn(Arrays.asList());

        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();

        assertNotNull(subscriptions);
        assertEquals(0, subscriptions.size());
    }

    // ── FindById Tests ────────────────────────────────────────────

    @Test
    void findById_ReturnsSubscription_WhenExists() {
        when(userSubscriptionRepository.findById(1L))
                .thenReturn(Optional.of(subscription1));

        Optional<UserSubscription> result = userSubscriptionRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
        assertEquals("Active", result.get().getStatus());
        verify(userSubscriptionRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        when(userSubscriptionRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<UserSubscription> result = userSubscriptionRepository.findById(99L);

        assertFalse(result.isPresent());
        verify(userSubscriptionRepository, times(1)).findById(99L);
    }

    // ── FindByUserIdAndStatus Tests ───────────────────────────────

    @Test
    void findByUserIdAndStatus_ReturnsSubscription_WhenActive() {
        when(userSubscriptionRepository.findByUserIdAndStatus(1L, "Active"))
                .thenReturn(Optional.of(subscription1));

        Optional<UserSubscription> result =
                userSubscriptionRepository.findByUserIdAndStatus(1L, "Active");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
        assertEquals("Active", result.get().getStatus());
        verify(userSubscriptionRepository, times(1))
                .findByUserIdAndStatus(1L, "Active");
    }

    @Test
    void findByUserIdAndStatus_ReturnsEmpty_WhenNoActiveSubscription() {
        when(userSubscriptionRepository.findByUserIdAndStatus(2L, "Active"))
                .thenReturn(Optional.empty());

        Optional<UserSubscription> result =
                userSubscriptionRepository.findByUserIdAndStatus(2L, "Active");

        assertFalse(result.isPresent());
        verify(userSubscriptionRepository, times(1))
                .findByUserIdAndStatus(2L, "Active");
    }

    @Test
    void findByUserIdAndStatus_ReturnsExpired_WhenExpiredStatus() {
        when(userSubscriptionRepository.findByUserIdAndStatus(2L, "Expired"))
                .thenReturn(Optional.of(subscription2));

        Optional<UserSubscription> result =
                userSubscriptionRepository.findByUserIdAndStatus(2L, "Expired");

        assertTrue(result.isPresent());
        assertEquals("Expired", result.get().getStatus());
        verify(userSubscriptionRepository, times(1))
                .findByUserIdAndStatus(2L, "Expired");
    }

    // ── Count Tests ───────────────────────────────────────────────

    @Test
    void count_ReturnsCorrectCount() {
        when(userSubscriptionRepository.count())
                .thenReturn(2L);

        long count = userSubscriptionRepository.count();

        assertEquals(2L, count);
        verify(userSubscriptionRepository, times(1)).count();
    }

    @Test
    void count_ReturnsZero_WhenEmpty() {
        when(userSubscriptionRepository.count())
                .thenReturn(0L);

        long count = userSubscriptionRepository.count();

        assertEquals(0L, count);
    }

    // ── Delete Tests ──────────────────────────────────────────────

    @Test
    void deleteById_DeletesSubscription_Successfully() {
        doNothing().when(userSubscriptionRepository).deleteById(1L);

        userSubscriptionRepository.deleteById(1L);

        verify(userSubscriptionRepository, times(1)).deleteById(1L);
    }

    // ── ExistsById Tests ──────────────────────────────────────────

    @Test
    void existsById_ReturnsTrue_WhenExists() {
        when(userSubscriptionRepository.existsById(1L))
                .thenReturn(true);

        boolean exists = userSubscriptionRepository.existsById(1L);

        assertTrue(exists);
        verify(userSubscriptionRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse_WhenNotExists() {
        when(userSubscriptionRepository.existsById(99L))
                .thenReturn(false);

        boolean exists = userSubscriptionRepository.existsById(99L);

        assertFalse(exists);
        verify(userSubscriptionRepository, times(1)).existsById(99L);
    }
}