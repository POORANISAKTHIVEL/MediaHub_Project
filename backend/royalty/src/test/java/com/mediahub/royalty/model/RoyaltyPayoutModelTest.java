package com.mediahub.royalty.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class RoyaltyPayoutModelTest {

    private RoyaltyPayout payout;

    @BeforeEach
    void setUp() {
        payout = new RoyaltyPayout();
    }

    // MT-27: Default constructor sets status to Pending
    @Test
    @DisplayName("MT-27: Default constructor sets status to Pending")
    void defaultConstructor_setsStatusPending() {
        assertEquals("Pending", payout.getStatus());
    }

    // MT-28: Default constructor initialises numeric fields to zero
    @Test
    @DisplayName("MT-28: Default constructor sets numeric fields to 0")
    void defaultConstructor_numericFieldsDefaultToZero() {
        assertEquals(0, payout.getPayoutID());
        assertEquals(0, payout.getStatementID());
        assertEquals(0, payout.getCreatorID());
        assertEquals(0.0, payout.getAmount());
    }

    // MT-29: Default constructor leaves method and payoutDate null
    @Test
    @DisplayName("MT-29: Default constructor leaves method and payoutDate null")
    void defaultConstructor_nullableFieldsAreNull() {
        assertNull(payout.getMethod());
        assertNull(payout.getPayoutDate());
    }

    // MT-30: setPayoutID and getPayoutID
    @Test
    @DisplayName("MT-30: setPayoutID / getPayoutID round-trip")
    void payoutID_setAndGet() {
        payout.setPayoutID(77);
        assertEquals(77, payout.getPayoutID());
    }

    // MT-31: setStatementID and getStatementID
    @Test
    @DisplayName("MT-31: setStatementID / getStatementID round-trip")
    void statementID_setAndGet() {
        payout.setStatementID(20);
        assertEquals(20, payout.getStatementID());
    }

    // MT-32: setCreatorID and getCreatorID
    @Test
    @DisplayName("MT-32: setCreatorID / getCreatorID round-trip")
    void creatorID_setAndGet() {
        payout.setCreatorID(503);
        assertEquals(503, payout.getCreatorID());
    }

    // MT-33: setAmount and getAmount
    @Test
    @DisplayName("MT-33: setAmount / getAmount round-trip")
    void amount_setAndGet() {
        payout.setAmount(850.50);
        assertEquals(850.50, payout.getAmount());
    }

    // MT-34: setPayoutDate and getPayoutDate
    @Test
    @DisplayName("MT-34: setPayoutDate / getPayoutDate round-trip")
    void payoutDate_setAndGet() {
        Date date = new Date();
        payout.setPayoutDate(date);
        assertEquals(date, payout.getPayoutDate());
    }

    // MT-35: setMethod and getMethod — BankTransfer
    @Test
    @DisplayName("MT-35: setMethod BankTransfer / getMethod round-trip")
    void method_bankTransfer_setAndGet() {
        payout.setMethod("BankTransfer");
        assertEquals("BankTransfer", payout.getMethod());
    }

    // MT-36: setMethod and getMethod — WalletCredit
    @Test
    @DisplayName("MT-36: setMethod WalletCredit / getMethod round-trip")
    void method_walletCredit_setAndGet() {
        payout.setMethod("WalletCredit");
        assertEquals("WalletCredit", payout.getMethod());
    }

    // MT-37: setStatus and getStatus
    @Test
    @DisplayName("MT-37: setStatus / getStatus round-trip")
    void status_setAndGet() {
        payout.setStatus("Processed");
        assertEquals("Processed", payout.getStatus());
    }

    // MT-38: Status transitions Pending → Processed
    @Test
    @DisplayName("MT-38: Status transitions Pending → Processed → Failed")
    void status_canTransition() {
        assertEquals("Pending", payout.getStatus());
        payout.setStatus("Processed");
        assertEquals("Processed", payout.getStatus());
        payout.setStatus("Failed");
        assertEquals("Failed", payout.getStatus());
    }

    // MT-39: Two instances are independent
    @Test
    @DisplayName("MT-39: Two RoyaltyPayout instances are independent")
    void twoInstances_areIndependent() {
        RoyaltyPayout p1 = new RoyaltyPayout();
        RoyaltyPayout p2 = new RoyaltyPayout();
        p1.setAmount(100.0);
        p2.setAmount(200.0);
        assertNotEquals(p1.getAmount(), p2.getAmount());
    }

    // MT-40: setPayoutDate accepts null
    @Test
    @DisplayName("MT-40: setPayoutDate accepts null without throwing")
    void payoutDate_acceptsNull() {
        assertDoesNotThrow(() -> payout.setPayoutDate(null));
        assertNull(payout.getPayoutDate());
    }
}
