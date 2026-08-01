package com.mediahub.subscriptionPlan.repository;

import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionHistoryRepositoryTest {

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    private SubscriptionHistory history1;
    private SubscriptionHistory history2;

    @BeforeEach
    void setUp() {
        history1 = SubscriptionHistory.builder()
                .historyId(1L)
                .subscriptionId(1L)
                .userId(1L)
                .fromPlanId(1L)
                .toPlanId(2L)
                .changeType("Upgrade")
                .changeDate(LocalDateTime.of(2025, 1, 1, 9, 0))
                .remarks("Upgraded to Premium")
                .build();

        history2 = SubscriptionHistory.builder()
                .historyId(2L)
                .subscriptionId(2L)
                .userId(2L)
                .fromPlanId(2L)
                .toPlanId(1L)
                .changeType("Downgrade")
                .changeDate(LocalDateTime.of(2025, 2, 1, 10, 0))
                .remarks("Downgraded to Standard")
                .build();
    }

    // ── Save Tests ────────────────────────────────────────────────

    @Test
    void save_History_Success() {
        when(subscriptionHistoryRepository.save(any(SubscriptionHistory.class)))
                .thenReturn(history1);

        SubscriptionHistory saved = subscriptionHistoryRepository.save(history1);

        assertNotNull(saved);
        assertEquals(1L, saved.getHistoryId());
        assertEquals("Upgrade", saved.getChangeType());
        assertEquals("Upgraded to Premium", saved.getRemarks());
        verify(subscriptionHistoryRepository, times(1)).save(history1);
    }

    // ── FindAll Tests ─────────────────────────────────────────────

    @Test
    void findAll_ReturnsAllHistories() {
        when(subscriptionHistoryRepository.findAll())
                .thenReturn(Arrays.asList(history1, history2));

        List<SubscriptionHistory> histories = subscriptionHistoryRepository.findAll();

        assertNotNull(histories);
        assertEquals(2, histories.size());
        assertEquals("Upgrade", histories.get(0).getChangeType());
        assertEquals("Downgrade", histories.get(1).getChangeType());
        verify(subscriptionHistoryRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsEmptyList() {
        when(subscriptionHistoryRepository.findAll())
                .thenReturn(Arrays.asList());

        List<SubscriptionHistory> histories = subscriptionHistoryRepository.findAll();

        assertNotNull(histories);
        assertEquals(0, histories.size());
    }

    // ── FindById Tests ────────────────────────────────────────────

    @Test
    void findById_ReturnsHistory_WhenExists() {
        when(subscriptionHistoryRepository.findById(1L))
                .thenReturn(Optional.of(history1));

        Optional<SubscriptionHistory> result = subscriptionHistoryRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Upgrade", result.get().getChangeType());
        assertEquals(1L, result.get().getUserId());
        verify(subscriptionHistoryRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        when(subscriptionHistoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<SubscriptionHistory> result = subscriptionHistoryRepository.findById(99L);

        assertFalse(result.isPresent());
        verify(subscriptionHistoryRepository, times(1)).findById(99L);
    }

    // ── FindBySubscriptionIdAndChangeType Tests ───────────────────

    @Test
    void findBySubscriptionIdAndChangeType_ReturnsHistory_WhenExists() {
        when(subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(1L, "Upgrade"))
                .thenReturn(Optional.of(history1));

        Optional<SubscriptionHistory> result =
                subscriptionHistoryRepository.findBySubscriptionIdAndChangeType(1L, "Upgrade");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSubscriptionId());
        assertEquals("Upgrade", result.get().getChangeType());
        verify(subscriptionHistoryRepository, times(1))
                .findBySubscriptionIdAndChangeType(1L, "Upgrade");
    }

    @Test
    void findBySubscriptionIdAndChangeType_ReturnsEmpty_WhenNotExists() {
        when(subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(99L, "Upgrade"))
                .thenReturn(Optional.empty());

        Optional<SubscriptionHistory> result =
                subscriptionHistoryRepository.findBySubscriptionIdAndChangeType(99L, "Upgrade");

        assertFalse(result.isPresent());
        verify(subscriptionHistoryRepository, times(1))
                .findBySubscriptionIdAndChangeType(99L, "Upgrade");
    }

    @Test
    void findBySubscriptionIdAndChangeType_Downgrade_ReturnsHistory() {
        when(subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(2L, "Downgrade"))
                .thenReturn(Optional.of(history2));

        Optional<SubscriptionHistory> result =
                subscriptionHistoryRepository.findBySubscriptionIdAndChangeType(2L, "Downgrade");

        assertTrue(result.isPresent());
        assertEquals("Downgrade", result.get().getChangeType());
        assertEquals(2L, result.get().getSubscriptionId());
        verify(subscriptionHistoryRepository, times(1))
                .findBySubscriptionIdAndChangeType(2L, "Downgrade");
    }

    // ── Count Tests ───────────────────────────────────────────────

    @Test
    void count_ReturnsCorrectCount() {
        when(subscriptionHistoryRepository.count())
                .thenReturn(2L);

        long count = subscriptionHistoryRepository.count();

        assertEquals(2L, count);
        verify(subscriptionHistoryRepository, times(1)).count();
    }

    @Test
    void count_ReturnsZero_WhenEmpty() {
        when(subscriptionHistoryRepository.count())
                .thenReturn(0L);

        long count = subscriptionHistoryRepository.count();

        assertEquals(0L, count);
    }

    // ── Delete Tests ──────────────────────────────────────────────

    @Test
    void deleteById_DeletesHistory_Successfully() {
        doNothing().when(subscriptionHistoryRepository).deleteById(1L);

        subscriptionHistoryRepository.deleteById(1L);

        verify(subscriptionHistoryRepository, times(1)).deleteById(1L);
    }

    // ── ExistsById Tests ──────────────────────────────────────────

    @Test
    void existsById_ReturnsTrue_WhenExists() {
        when(subscriptionHistoryRepository.existsById(1L))
                .thenReturn(true);

        boolean exists = subscriptionHistoryRepository.existsById(1L);

        assertTrue(exists);
        verify(subscriptionHistoryRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse_WhenNotExists() {
        when(subscriptionHistoryRepository.existsById(99L))
                .thenReturn(false);

        boolean exists = subscriptionHistoryRepository.existsById(99L);

        assertFalse(exists);
        verify(subscriptionHistoryRepository, times(1)).existsById(99L);
    }
}