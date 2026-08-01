package com.mediahub.royalty.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class RoyaltyRuleModelTest {

    private RoyaltyRule rule;

    @BeforeEach
    void setUp() {
        rule = new RoyaltyRule();
    }

    // ─── CONSTRUCTOR TESTS ────────────────────────────────────────────────────

    // MT-01: Default constructor sets status to Active
    @Test
    @DisplayName("MT-01: Default constructor sets status to Active")
    void defaultConstructor_setsStatusActive() {
        assertEquals("Active", rule.getStatus());
    }

    // MT-02: Default constructor initialises numeric fields to zero
    @Test
    @DisplayName("MT-02: Default constructor sets numeric fields to 0")
    void defaultConstructor_numericFieldsDefaultToZero() {
        assertEquals(0, rule.getRuleID());
        assertEquals(0.0, rule.getRevenueSharePercent());
        assertEquals(0.0, rule.getMinimumPayoutThreshold());
    }

    // MT-03: Default constructor leaves string fields null (except status)
    @Test
    @DisplayName("MT-03: Default constructor leaves string fields null except status")
    void defaultConstructor_stringFieldsNullExceptStatus() {
        assertNull(rule.getCreatorTier());
        assertNull(rule.getPayoutFrequency());
        assertNull(rule.getEffectiveDate());
    }

    // ─── SETTER / GETTER TESTS ────────────────────────────────────────────────

    // MT-04: setRuleID and getRuleID
    @Test
    @DisplayName("MT-04: setRuleID / getRuleID round-trip")
    void ruleID_setAndGet() {
        rule.setRuleID(42);
        assertEquals(42, rule.getRuleID());
    }

    // MT-05: setCreatorTier and getCreatorTier
    @Test
    @DisplayName("MT-05: setCreatorTier / getCreatorTier round-trip")
    void creatorTier_setAndGet() {
        rule.setCreatorTier("Gold");
        assertEquals("Gold", rule.getCreatorTier());
    }

    // MT-06: setRevenueSharePercent and getRevenueSharePercent
    @Test
    @DisplayName("MT-06: setRevenueSharePercent / getRevenueSharePercent round-trip")
    void revenueSharePercent_setAndGet() {
        rule.setRevenueSharePercent(35.5);
        assertEquals(35.5, rule.getRevenueSharePercent());
    }

    // MT-07: setMinimumPayoutThreshold and getMinimumPayoutThreshold
    @Test
    @DisplayName("MT-07: setMinimumPayoutThreshold / getMinimumPayoutThreshold round-trip")
    void minimumPayoutThreshold_setAndGet() {
        rule.setMinimumPayoutThreshold(200.0);
        assertEquals(200.0, rule.getMinimumPayoutThreshold());
    }

    // MT-08: setPayoutFrequency and getPayoutFrequency
    @Test
    @DisplayName("MT-08: setPayoutFrequency / getPayoutFrequency round-trip")
    void payoutFrequency_setAndGet() {
        rule.setPayoutFrequency("Quarterly");
        assertEquals("Quarterly", rule.getPayoutFrequency());
    }

    // MT-09: setEffectiveDate and getEffectiveDate
    @Test
    @DisplayName("MT-09: setEffectiveDate / getEffectiveDate round-trip")
    void effectiveDate_setAndGet() {
        Date date = new Date();
        rule.setEffectiveDate(date);
        assertEquals(date, rule.getEffectiveDate());
    }

    // MT-10: setStatus and getStatus
    @Test
    @DisplayName("MT-10: setStatus / getStatus round-trip")
    void status_setAndGet() {
        rule.setStatus("Inactive");
        assertEquals("Inactive", rule.getStatus());
    }

    // MT-11: Status can be overridden after construction
    @Test
    @DisplayName("MT-11: Status can be changed after default construction")
    void status_canBeOverridden() {
        assertEquals("Active", rule.getStatus());
        rule.setStatus("Inactive");
        assertEquals("Inactive", rule.getStatus());
    }

    // MT-12: Two separate instances are independent
    @Test
    @DisplayName("MT-12: Two RoyaltyRule instances are independent")
    void twoInstances_areIndependent() {
        RoyaltyRule r1 = new RoyaltyRule();
        RoyaltyRule r2 = new RoyaltyRule();
        r1.setCreatorTier("Gold");
        r2.setCreatorTier("Silver");
        assertNotEquals(r1.getCreatorTier(), r2.getCreatorTier());
    }

    // MT-13: setEffectiveDate accepts null
    @Test
    @DisplayName("MT-13: setEffectiveDate accepts null without throwing")
    void effectiveDate_acceptsNull() {
        assertDoesNotThrow(() -> rule.setEffectiveDate(null));
        assertNull(rule.getEffectiveDate());
    }
}
