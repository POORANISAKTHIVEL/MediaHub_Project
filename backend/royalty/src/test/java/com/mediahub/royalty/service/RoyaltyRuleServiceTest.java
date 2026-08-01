package com.mediahub.royalty.service;

import com.mediahub.royalty.model.RoyaltyRule;
import com.mediahub.royalty.repository.RoyaltyRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RoyaltyRuleServiceTest {

    @Mock
    private RoyaltyRuleRepository repository;

    @InjectMocks
    private RoyaltyRuleService service;

    private RoyaltyRule rule;

    @BeforeEach
    void setUp() {
        rule = new RoyaltyRule();
        rule.setCreatorTier("Gold");
        rule.setRevenueSharePercent(30.0);
        rule.setMinimumPayoutThreshold(100.0);
        rule.setPayoutFrequency("Monthly");
        rule.setEffectiveDate(new Date());
    }

    // ─── REGULAR TEST CASES ──────────────────────────────────────────────────

    // TC-01: Create rule successfully
    @Test
    void createRule_success() {
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule result = service.createRule(rule);

        assertEquals("Active", result.getStatus());
        verify(repository, times(1)).save(any(RoyaltyRule.class));
    }

    // TC-02: Create rule with missing CreatorTier
    @Test
    void createRule_missingCreatorTier() {
        rule.setCreatorTier(null);

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
        verify(repository, never()).save(any());
    }

    // TC-03: Create rule with empty CreatorTier
    @Test
    void createRule_emptyCreatorTier() {
        rule.setCreatorTier("");

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-04: Create rule with invalid RevenueSharePercent (0)
    @Test
    void createRule_zeroRevenueSharePercent() {
        rule.setRevenueSharePercent(0);

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-05: Create rule with RevenueSharePercent > 100
    @Test
    void createRule_revenueSharePercentAbove100() {
        rule.setRevenueSharePercent(101);

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-06: Create rule with invalid PayoutFrequency
    @Test
    void createRule_invalidPayoutFrequency() {
        rule.setPayoutFrequency("Weekly");

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-07: Create rule with null PayoutFrequency
    @Test
    void createRule_nullPayoutFrequency() {
        rule.setPayoutFrequency(null);

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-08: Create rule with missing EffectiveDate
    @Test
    void createRule_missingEffectiveDate() {
        rule.setEffectiveDate(null);

        assertThrows(BadRequestException.class, () -> service.createRule(rule));
    }

    // TC-09: Create rule — repository failure
    @Test
    void createRule_repositoryFailure() {
        doThrow(new RuntimeException("DB error")).when(repository).save(any(RoyaltyRule.class));

        assertThrows(RuntimeException.class, () -> service.createRule(rule));
    }

    // TC-10: Get all rules — returns list
    @Test
    void getAllRules_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(new RoyaltyRule(), new RoyaltyRule()));

        List<RoyaltyRule> result = service.getAllRules();

        assertEquals(2, result.size());
    }

    // TC-11: Get all rules — empty list
    @Test
    void getAllRules_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<RoyaltyRule> result = service.getAllRules();

        assertTrue(result.isEmpty());
    }

    // TC-12: Get rule by ID — found
    @Test
    void getRuleById_found() {
        when(repository.findById(1)).thenReturn(rule);

        RoyaltyRule result = service.getRuleById(1);

        assertNotNull(result);
    }

    // TC-13: Get rule by ID — not found
    @Test
    void getRuleById_notFound() {
        when(repository.findById(99)).thenThrow(new ResourceNotFoundException("not found"));

        assertThrows(ResourceNotFoundException.class, () -> service.getRuleById(99));
    }

    // TC-14: Deactivate rule — success
    @Test
    void deactivateRule_success() {
        when(repository.updateStatus(1, "Inactive")).thenReturn(1);

        Map<String, Object> result = service.deactivateRule(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Inactive", result.get("status"));
        assertEquals("Royalty rule deactivated successfully.", result.get("message"));
    }

    // TC-15: Deactivate rule — not found
    @Test
    void deactivateRule_notFound() {
        when(repository.updateStatus(anyInt(), anyString())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deactivateRule(99));
    }

    // TC-16: Delete Active rule — blocked
    @Test
    void deleteRule_activeStatus_blocked() {
        when(repository.findStatusById(1)).thenReturn("Active");

        assertThrows(BadRequestException.class, () -> service.deleteRule(1));
        verify(repository, never()).delete(anyInt());
    }

    // TC-17: Delete Inactive rule — success
    @Test
    void deleteRule_inactiveStatus_success() {
        when(repository.findStatusById(1)).thenReturn("Inactive");
        when(repository.delete(1)).thenReturn(1);

        Map<String, Object> result = service.deleteRule(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Royalty rule deleted successfully.", result.get("message"));
    }

    // TC-18: Delete rule — not found
    @Test
    void deleteRule_notFound() {
        when(repository.findStatusById(1)).thenReturn("Inactive");
        when(repository.delete(1)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteRule(1));
    }

    // ─── EXCEPTIONAL TEST CASES ──────────────────────────────────────────────

    // EX-01: Create rule — status always forced to Active
    @Test
    @DisplayName("EX-01: createRule — status always set to Active regardless of input")
    void createRule_statusAlwaysActive() {
        rule.setStatus("Inactive");
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule returned = service.createRule(rule);

        assertEquals("Active", returned.getStatus());
    }

    // EX-02: Create rule — Quarterly frequency accepted
    @Test
    @DisplayName("EX-02: createRule — Quarterly PayoutFrequency accepted")
    void createRule_quarterlyFrequency() {
        rule.setPayoutFrequency("Quarterly");
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule result = service.createRule(rule);

        assertEquals("Active", result.getStatus());
    }

    // EX-03: Create rule — response contains all required fields
    @Test
    @DisplayName("EX-03: createRule success — all response fields present")
    void createRule_responseContainsAllFields() {
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule result = service.createRule(rule);

        assertEquals("Gold", result.getCreatorTier());
        assertEquals(30.0, result.getRevenueSharePercent());
        assertEquals("Active", result.getStatus());
    }

    // EX-04: Create rule — repository throws exception
    @Test
    @DisplayName("EX-04: createRule — repository throws → propagates uncaught")
    void createRule_repositoryThrowsException() {
        doThrow(new RuntimeException("DB unavailable")).when(repository).save(any(RoyaltyRule.class));

        assertThrows(RuntimeException.class, () -> service.createRule(rule));
    }

    // EX-05: Deactivate rule — repository throws exception
    @Test
    @DisplayName("EX-05: deactivateRule — repository throws → caught as 404")
    void deactivateRule_repositoryException() {
        when(repository.updateStatus(anyInt(), anyString()))
            .thenThrow(new RuntimeException("Timeout"));

        assertThrows(RuntimeException.class, () -> service.deactivateRule(1));
    }

    // EX-06: Delete rule — findStatusById throws exception
    @Test
    @DisplayName("EX-06: deleteRule — findStatusById throws → caught as 404, delete never called")
    void deleteRule_findStatusThrowsException() {
        when(repository.findStatusById(anyInt()))
            .thenThrow(new com.mediahub.royalty.exception.ResourceNotFoundException("Row not found"));

        assertThrows(com.mediahub.royalty.exception.ResourceNotFoundException.class,
                () -> service.deleteRule(99));
        verify(repository, never()).delete(anyInt());
    }

    // EX-07: Create rule — exact boundary RevenueSharePercent = 100 (valid)
    @Test
    @DisplayName("EX-07: createRule — RevenueSharePercent exactly 100 is accepted")
    void createRule_revenueSharePercent100_isValid() {
        rule.setRevenueSharePercent(100.0);
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule result = service.createRule(rule);

        assertEquals("Active", result.getStatus());
    }

    // EX-08: Create rule — exact boundary RevenueSharePercent = 1 (valid)
    @Test
    @DisplayName("EX-08: createRule — RevenueSharePercent exactly 1 is accepted")
    void createRule_revenueSharePercent1_isValid() {
        rule.setRevenueSharePercent(1.0);
        doNothing().when(repository).save(any(RoyaltyRule.class));

        RoyaltyRule result = service.createRule(rule);

        assertEquals("Active", result.getStatus());
    }
}
