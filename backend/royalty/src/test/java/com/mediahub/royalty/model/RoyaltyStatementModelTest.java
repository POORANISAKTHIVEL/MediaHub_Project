package com.mediahub.royalty.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoyaltyStatementModelTest {

    private RoyaltyStatement statement;

    @BeforeEach
    void setUp() {
        statement = new RoyaltyStatement();
    }

    // MT-14: Default constructor sets status to Draft
    @Test
    @DisplayName("MT-14: Default constructor sets status to Draft")
    void defaultConstructor_setsStatusDraft() {
        assertEquals("Draft", statement.getStatus());
    }

    // MT-15: Default constructor initialises numeric fields to zero
    @Test
    @DisplayName("MT-15: Default constructor sets numeric fields to 0")
    void defaultConstructor_numericFieldsDefaultToZero() {
        assertEquals(0, statement.getStatementID());
        assertEquals(0, statement.getCreatorID());
        assertEquals(0L, statement.getTotalViews());
        assertEquals(0.0, statement.getTotalRevenue());
        assertEquals(0.0, statement.getRoyaltyAmount());
    }

    // MT-16: Default constructor leaves period null
    @Test
    @DisplayName("MT-16: Default constructor leaves period null")
    void defaultConstructor_periodIsNull() {
        assertNull(statement.getPeriod());
    }

    // MT-17: setStatementID and getStatementID
    @Test
    @DisplayName("MT-17: setStatementID / getStatementID round-trip")
    void statementID_setAndGet() {
        statement.setStatementID(101);
        assertEquals(101, statement.getStatementID());
    }

    // MT-18: setCreatorID and getCreatorID
    @Test
    @DisplayName("MT-18: setCreatorID / getCreatorID round-trip")
    void creatorID_setAndGet() {
        statement.setCreatorID(502);
        assertEquals(502, statement.getCreatorID());
    }

    // MT-19: setPeriod and getPeriod
    @Test
    @DisplayName("MT-19: setPeriod / getPeriod round-trip")
    void period_setAndGet() {
        statement.setPeriod("2025-Q2");
        assertEquals("2025-Q2", statement.getPeriod());
    }

    // MT-20: setTotalViews and getTotalViews
    @Test
    @DisplayName("MT-20: setTotalViews / getTotalViews round-trip")
    void totalViews_setAndGet() {
        statement.setTotalViews(100000L);
        assertEquals(100000L, statement.getTotalViews());
    }

    // MT-21: setTotalRevenue and getTotalRevenue
    @Test
    @DisplayName("MT-21: setTotalRevenue / getTotalRevenue round-trip")
    void totalRevenue_setAndGet() {
        statement.setTotalRevenue(5000.75);
        assertEquals(5000.75, statement.getTotalRevenue());
    }

    // MT-22: setRoyaltyAmount and getRoyaltyAmount
    @Test
    @DisplayName("MT-22: setRoyaltyAmount / getRoyaltyAmount round-trip")
    void royaltyAmount_setAndGet() {
        statement.setRoyaltyAmount(1500.0);
        assertEquals(1500.0, statement.getRoyaltyAmount());
    }

    // MT-23: setStatus and getStatus
    @Test
    @DisplayName("MT-23: setStatus / getStatus round-trip")
    void status_setAndGet() {
        statement.setStatus("Finalised");
        assertEquals("Finalised", statement.getStatus());
    }

    // MT-24: Status transitions Draft → Finalised → Paid
    @Test
    @DisplayName("MT-24: Status can be transitioned Draft → Finalised → Paid")
    void status_transitionDraftToFinalisedToPaid() {
        assertEquals("Draft", statement.getStatus());
        statement.setStatus("Finalised");
        assertEquals("Finalised", statement.getStatus());
        statement.setStatus("Paid");
        assertEquals("Paid", statement.getStatus());
    }

    // MT-25: Two separate instances are independent
    @Test
    @DisplayName("MT-25: Two RoyaltyStatement instances are independent")
    void twoInstances_areIndependent() {
        RoyaltyStatement s1 = new RoyaltyStatement();
        RoyaltyStatement s2 = new RoyaltyStatement();
        s1.setCreatorID(1);
        s2.setCreatorID(2);
        assertNotEquals(s1.getCreatorID(), s2.getCreatorID());
    }

    // MT-26: TotalRevenue can be set to zero
    @Test
    @DisplayName("MT-26: TotalRevenue accepts zero value")
    void totalRevenue_acceptsZero() {
        statement.setTotalRevenue(0.0);
        assertEquals(0.0, statement.getTotalRevenue());
    }
}
