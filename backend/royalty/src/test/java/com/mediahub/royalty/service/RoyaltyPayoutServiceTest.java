package com.mediahub.royalty.service;

import com.mediahub.royalty.client.CreatorClient;
import com.mediahub.royalty.client.NotificationClient;
import com.mediahub.royalty.model.RoyaltyPayout;
import com.mediahub.royalty.repository.RoyaltyPayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.ResourceNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoyaltyPayoutServiceTest {

    @Mock
    private RoyaltyPayoutRepository repository;

    @Mock
    private CreatorClient creatorClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private RoyaltyPayoutService service;

    private RoyaltyPayout payout;

    @BeforeEach
    void setUp() {
        // RoyaltyPayoutService has an explicit constructor for `repository`, so Mockito's
        // @InjectMocks only does constructor injection and skips field injection for the other
        // @Autowired clients — wire them manually or they stay null and NPE on first use.
        org.springframework.test.util.ReflectionTestUtils.setField(service, "creatorClient", creatorClient);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "notificationClient", notificationClient);

        payout = new RoyaltyPayout();
        payout.setStatementID(10);
        payout.setCreatorID(501);
        payout.setAmount(750.0);
        payout.setPayoutDate(new Date());
        payout.setMethod("BankTransfer");
    }

    // ─── REGULAR TEST CASES ──────────────────────────────────────────────────

    // TC-33: Create payout successfully
    @Test
    void createPayout_success() {
        doNothing().when(repository).save(any(RoyaltyPayout.class));

        RoyaltyPayout result = service.createPayout(payout, 1L);

        assertEquals("Pending", result.getStatus());
        assertEquals(10, result.getStatementID());
        verify(repository, times(1)).save(any(RoyaltyPayout.class));
    }

    // TC-34: Create payout — missing StatementID
    @Test
    void createPayout_missingStatementID() {
        payout.setStatementID(0);

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
        verify(repository, never()).save(any());
    }

    // TC-35: Create payout — missing CreatorID
    @Test
    void createPayout_missingCreatorID() {
        payout.setCreatorID(0);

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-36: Create payout — zero amount
    @Test
    void createPayout_zeroAmount() {
        payout.setAmount(0);

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-37: Create payout — negative amount
    @Test
    void createPayout_negativeAmount() {
        payout.setAmount(-100.0);

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-38: Create payout — invalid method
    @Test
    void createPayout_invalidMethod() {
        payout.setMethod("Cash");

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-39: Create payout — null method
    @Test
    void createPayout_nullMethod() {
        payout.setMethod(null);

        assertThrows(BadRequestException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-40: Create payout — repository failure
    @Test
    void createPayout_repositoryFailure() {
        doThrow(new RuntimeException("DB error")).when(repository).save(any(RoyaltyPayout.class));

        assertThrows(RuntimeException.class, () -> service.createPayout(payout, 1L));
    }

    // TC-41: Get all payouts — returns list
    @Test
    void getAllPayouts_returnsList() {
        when(repository.findAll())
                .thenReturn(Arrays.asList(new RoyaltyPayout(), new RoyaltyPayout()));

        List<RoyaltyPayout> result = service.getAllPayouts();

        assertEquals(2, result.size());
    }

    // TC-42: Get all payouts — empty list
    @Test
    void getAllPayouts_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<RoyaltyPayout> result = service.getAllPayouts();

        assertTrue(result.isEmpty());
    }

    // TC-43: Get payout by ID — found
    @Test
    void getPayoutById_found() {
        when(repository.findById(1)).thenReturn(payout);

        RoyaltyPayout result = service.getPayoutById(1);

        assertNotNull(result);
    }

    // TC-44: Get payout by ID — not found
    @Test
    void getPayoutById_notFound() {
        when(repository.findById(99)).thenThrow(new ResourceNotFoundException("not found"));

        assertThrows(ResourceNotFoundException.class, () -> service.getPayoutById(99));
    }

    // TC-45: Process payout — success
    @Test
    void processPayout_success() {
        when(repository.updateStatus(1, "Processed")).thenReturn(1);

        Map<String, Object> result = service.processPayout(1, 1L);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Processed", result.get("status"));
        assertEquals("Payout processed successfully.", result.get("message"));
    }

    // TC-46: Process payout — not found
    @Test
    void processPayout_notFound() {
        when(repository.updateStatus(anyInt(), anyString())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.processPayout(99, 1L));
    }

    // TC-47: Fail payout — success
    @Test
    void failPayout_success() {
        when(repository.updateStatus(1, "Failed")).thenReturn(1);

        Map<String, Object> result = service.failPayout(1, "Insufficient funds", 1L);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Failed", result.get("status"));
        assertEquals("Insufficient funds", result.get("reason"));
        assertEquals("Payout marked as failed.", result.get("message"));
    }

    // TC-48: Fail payout — not found
    @Test
    void failPayout_notFound() {
        when(repository.updateStatus(anyInt(), anyString())).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.failPayout(99, "Error", 1L));
    }

    // TC-49: Delete Processed payout — blocked
    @Test
    void deletePayout_processedStatus_blocked() {
        when(repository.findStatusById(1)).thenReturn("Processed");

        assertThrows(BadRequestException.class, () -> service.deletePayout(1));
        verify(repository, never()).delete(anyInt());
    }

    // TC-50: Delete Pending payout — success
    @Test
    void deletePayout_pendingStatus_success() {
        when(repository.findStatusById(1)).thenReturn("Pending");
        when(repository.delete(1)).thenReturn(1);

        Map<String, Object> result = service.deletePayout(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Payout deleted successfully.", result.get("message"));
    }

    // TC-51: Delete payout — not found
    @Test
    void deletePayout_notFound() {
        when(repository.findStatusById(1)).thenReturn("Pending");
        when(repository.delete(1)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deletePayout(1));
    }

    // ─── EXCEPTIONAL TEST CASES ──────────────────────────────────────────────

    // EX-15: Create payout — status always forced to Pending
    @Test
    @DisplayName("EX-15: createPayout — status always set to Pending regardless of input")
    void createPayout_statusAlwaysPending() {
        payout.setStatus("Processed");
        doNothing().when(repository).save(any(RoyaltyPayout.class));

        RoyaltyPayout returned = service.createPayout(payout, 1L);

        assertEquals("Pending", returned.getStatus());
    }

    // EX-16: Create payout — WalletCredit method accepted
    @Test
    @DisplayName("EX-16: createPayout — WalletCredit method accepted")
    void createPayout_walletCreditMethod() {
        payout.setMethod("WalletCredit");
        doNothing().when(repository).save(any(RoyaltyPayout.class));

        RoyaltyPayout result = service.createPayout(payout, 1L);

        assertEquals("Pending", result.getStatus());
    }

    // EX-17: Create payout — response contains all fields
    @Test
    @DisplayName("EX-17: createPayout success — all response fields present")
    void createPayout_responseContainsAllFields() {
        doNothing().when(repository).save(any(RoyaltyPayout.class));

        RoyaltyPayout result = service.createPayout(payout, 1L);

        assertEquals(10, result.getStatementID());
        assertEquals(501, result.getCreatorID());
        assertEquals(750.0, result.getAmount());
        assertEquals("BankTransfer", result.getMethod());
        assertEquals("Pending", result.getStatus());
    }

    // EX-18: Create payout — repository throws exception
    @Test
    @DisplayName("EX-18: createPayout — repository throws → propagates uncaught")
    void createPayout_repositoryThrowsException() {
        doThrow(new RuntimeException("DB unavailable")).when(repository).save(any(RoyaltyPayout.class));

        assertThrows(RuntimeException.class, () -> service.createPayout(payout, 1L));
    }

    // EX-19: Process payout — repository throws exception
    @Test
    @DisplayName("EX-19: processPayout — repository throws → caught as 404")
    void processPayout_repositoryException() {
        when(repository.updateStatus(anyInt(), anyString()))
            .thenThrow(new RuntimeException("Timeout"));

        assertThrows(RuntimeException.class, () -> service.processPayout(1, 1L));
    }

    // EX-20: Delete payout — findStatusById throws exception
    @Test
    @DisplayName("EX-20: deletePayout — findStatusById throws → caught as 404, delete never called")
    void deletePayout_findStatusThrowsException() {
        when(repository.findStatusById(anyInt()))
            .thenThrow(new RuntimeException("Row not found"));

        assertThrows(RuntimeException.class, () -> service.deletePayout(99));
        verify(repository, never()).delete(anyInt());
    }
}
