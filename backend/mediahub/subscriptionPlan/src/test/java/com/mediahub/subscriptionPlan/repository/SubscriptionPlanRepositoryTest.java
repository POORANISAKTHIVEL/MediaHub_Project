package com.mediahub.subscriptionPlan.repository;

import com.mediahub.subscriptionPlan.model.SubscriptionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanRepositoryTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    private SubscriptionPlan plan1;
    private SubscriptionPlan plan2;

    @BeforeEach
    void setUp() {
        plan1 = SubscriptionPlan.builder()
                .planId(1L)
                .name("Premium")
                .price(14.99)
                .billingCycle("Monthly")
                .contentAccessLevel("4K HDR")
                .maxDevices(5)
                .downloadAllowed(1)
                .status("Active")
                .build();

        plan2 = SubscriptionPlan.builder()
                .planId(2L)
                .name("Standard")
                .price(8.99)
                .billingCycle("Monthly")
                .contentAccessLevel("HD")
                .maxDevices(3)
                .downloadAllowed(1)
                .status("Active")
                .build();
    }

    // ── Save Tests ────────────────────────────────────────────────

    @Test
    void save_Plan_Success() {
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class)))
                .thenReturn(plan1);

        SubscriptionPlan saved = subscriptionPlanRepository.save(plan1);

        assertNotNull(saved);
        assertEquals(1L, saved.getPlanId());
        assertEquals("Premium", saved.getName());
        assertEquals(14.99, saved.getPrice());
        verify(subscriptionPlanRepository, times(1)).save(plan1);
    }

    // ── FindAll Tests ─────────────────────────────────────────────

    @Test
    void findAll_ReturnsAllPlans() {
        when(subscriptionPlanRepository.findAll())
                .thenReturn(Arrays.asList(plan1, plan2));

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        assertNotNull(plans);
        assertEquals(2, plans.size());
        assertEquals("Premium", plans.get(0).getName());
        assertEquals("Standard", plans.get(1).getName());
        verify(subscriptionPlanRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsEmptyList() {
        when(subscriptionPlanRepository.findAll())
                .thenReturn(Arrays.asList());

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        assertNotNull(plans);
        assertEquals(0, plans.size());
    }

    // ── FindById Tests ────────────────────────────────────────────

    @Test
    void findById_Returnsplan_WhenExists() {
        when(subscriptionPlanRepository.findById(1L))
                .thenReturn(Optional.of(plan1));

        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Premium", result.get().getName());
        assertEquals(14.99, result.get().getPrice());
        verify(subscriptionPlanRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        when(subscriptionPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findById(99L);

        assertFalse(result.isPresent());
        verify(subscriptionPlanRepository, times(1)).findById(99L);
    }

    // ── FindByName Tests ──────────────────────────────────────────

    @Test
    void findByName_ReturnsPlan_WhenExists() {
        when(subscriptionPlanRepository.findByName("Premium"))
                .thenReturn(Optional.of(plan1));

        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findByName("Premium");

        assertTrue(result.isPresent());
        assertEquals("Premium", result.get().getName());
        assertEquals(1L, result.get().getPlanId());
        verify(subscriptionPlanRepository, times(1)).findByName("Premium");
    }

    @Test
    void findByName_ReturnsEmpty_WhenNotExists() {
        when(subscriptionPlanRepository.findByName("Unknown"))
                .thenReturn(Optional.empty());

        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findByName("Unknown");

        assertFalse(result.isPresent());
        verify(subscriptionPlanRepository, times(1)).findByName("Unknown");
    }

    // ── Count Tests ───────────────────────────────────────────────

    @Test
    void count_ReturnsCorrectCount() {
        when(subscriptionPlanRepository.count())
                .thenReturn(2L);

        long count = subscriptionPlanRepository.count();

        assertEquals(2L, count);
        verify(subscriptionPlanRepository, times(1)).count();
    }

    @Test
    void count_ReturnsZero_WhenEmpty() {
        when(subscriptionPlanRepository.count())
                .thenReturn(0L);

        long count = subscriptionPlanRepository.count();

        assertEquals(0L, count);
    }

    // ── Delete Tests ──────────────────────────────────────────────

    @Test
    void deleteById_DeletesPlan_Successfully() {
        doNothing().when(subscriptionPlanRepository).deleteById(1L);

        subscriptionPlanRepository.deleteById(1L);

        verify(subscriptionPlanRepository, times(1)).deleteById(1L);
    }

    // ── ExistsById Tests ──────────────────────────────────────────

    @Test
    void existsById_ReturnsTrue_WhenExists() {
        when(subscriptionPlanRepository.existsById(1L))
                .thenReturn(true);

        boolean exists = subscriptionPlanRepository.existsById(1L);

        assertTrue(exists);
        verify(subscriptionPlanRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse_WhenNotExists() {
        when(subscriptionPlanRepository.existsById(99L))
                .thenReturn(false);

        boolean exists = subscriptionPlanRepository.existsById(99L);

        assertFalse(exists);
        verify(subscriptionPlanRepository, times(1)).existsById(99L);
    }
}