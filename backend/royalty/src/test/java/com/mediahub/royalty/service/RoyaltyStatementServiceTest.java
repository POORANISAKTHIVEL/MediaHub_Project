package com.mediahub.royalty.service;

import com.mediahub.royalty.model.RoyaltyStatement;
import com.mediahub.royalty.repository.RoyaltyStatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.mediahub.royalty.exception.BadRequestException;
import com.mediahub.royalty.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class RoyaltyStatementServiceTest {

    @Mock
    private RoyaltyStatementRepository repository;

    @InjectMocks
    private RoyaltyStatementService service;

    private RoyaltyStatement statement;

    @BeforeEach
    void setUp() {
        statement = new RoyaltyStatement();
        statement.setCreatorID(501);
        statement.setPeriod("2025-Q1");
        statement.setTotalViews(50000L);
        statement.setTotalRevenue(2000.0);
        statement.setRoyaltyAmount(600.0);
    }

    // ─── REGULAR TEST CASES ──────────────────────────────────────────────────

    // TC-19: Generate statement successfully
    @Test
    void generateStatement_success() {
        doNothing().when(repository).save(any(RoyaltyStatement.class));

        RoyaltyStatement result = service.generateStatement(statement);

        assertEquals("Draft", result.getStatus());
        verify(repository, times(1)).save(any(RoyaltyStatement.class));
    }

    // TC-20: Generate statement — missing CreatorID
    @Test
    void generateStatement_missingCreatorID() {
        statement.setCreatorID(0);

        assertThrows(BadRequestException.class, () -> service.generateStatement(statement));
        verify(repository, never()).save(any());
    }

    // TC-21: Generate statement — null period
    @Test
    void generateStatement_nullPeriod() {
        statement.setPeriod(null);

        assertThrows(BadRequestException.class, () -> service.generateStatement(statement));
    }

    // TC-22: Generate statement — empty period
    @Test
    void generateStatement_emptyPeriod() {
        statement.setPeriod("");

        assertThrows(BadRequestException.class, () -> service.generateStatement(statement));
    }

    // TC-23: Generate statement — negative totalRevenue
    @Test
    void generateStatement_negativeTotalRevenue() {
        statement.setTotalRevenue(-50.0);

        assertThrows(BadRequestException.class, () -> service.generateStatement(statement));
    }

    // TC-24: Generate statement — repository failure
    @Test
    void generateStatement_repositoryFailure() {
        doThrow(new RuntimeException("DB error")).when(repository).save(any(RoyaltyStatement.class));

        assertThrows(RuntimeException.class, () -> service.generateStatement(statement));
    }

    // TC-25: Get all statements — returns list
    @Test
    void getAllStatements_returnsList() {
        when(repository.findAll())
                .thenReturn(Arrays.asList(new RoyaltyStatement(), new RoyaltyStatement()));

        List<RoyaltyStatement> result = service.getAllStatements();

        assertEquals(2, result.size());
    }

    // TC-26: Get all statements — empty list
    @Test
    void getAllStatements_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<RoyaltyStatement> result = service.getAllStatements();

        assertTrue(result.isEmpty());
    }

    // TC-27: Get statement by ID — found
    @Test
    void getStatementById_found() {
        when(repository.findById(1)).thenReturn(statement);

        RoyaltyStatement result = service.getStatementById(1);

        assertNotNull(result);
    }

    // TC-28: Get statement by ID — not found
    @Test
    void getStatementById_notFound() {
        when(repository.findById(99)).thenThrow(new ResourceNotFoundException("not found"));

        assertThrows(ResourceNotFoundException.class, () -> service.getStatementById(99));
    }

    // TC-29: Finalise Draft statement — success
    @Test
    void finaliseStatement_draftStatus_success() {
        when(repository.findStatusById(1)).thenReturn("Draft");
        when(repository.updateStatus(1, "Finalised")).thenReturn(1);

        Map<String, Object> result = service.finaliseStatement(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Finalised", result.get("status"));
        assertEquals("Statement finalised successfully.", result.get("message"));
    }

    // TC-30: Finalise non-Draft statement — blocked
    @Test
    void finaliseStatement_nonDraft_blocked() {
        when(repository.findStatusById(1)).thenReturn("Finalised");

        assertThrows(BadRequestException.class, () -> service.finaliseStatement(1));
        verify(repository, never()).updateStatus(anyInt(), anyString());
    }

    // TC-31: Mark as Paid — Finalised statement — success
    @Test
    void markAsPaid_finalisedStatus_success() {
        when(repository.findStatusById(1)).thenReturn("Finalised");
        when(repository.updateStatus(1, "Paid")).thenReturn(1);

        Map<String, Object> result = service.markAsPaid(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Paid", result.get("status"));
        assertEquals("Statement marked as Paid successfully.", result.get("message"));
    }

    // TC-32: Mark as Paid — non-Finalised statement — blocked
    @Test
    void markAsPaid_nonFinalised_blocked() {
        when(repository.findStatusById(1)).thenReturn("Draft");

        assertThrows(BadRequestException.class, () -> service.markAsPaid(1));
        verify(repository, never()).updateStatus(anyInt(), anyString());
    }

    // ─── EXCEPTIONAL TEST CASES ──────────────────────────────────────────────

    // EX-09: Generate statement — status always forced to Draft
    @Test
    @DisplayName("EX-09: generateStatement — status always set to Draft regardless of input")
    void generateStatement_statusAlwaysDraft() {
        statement.setStatus("Finalised");
        doNothing().when(repository).save(any(RoyaltyStatement.class));

        RoyaltyStatement returned = service.generateStatement(statement);

        assertEquals("Draft", returned.getStatus());
    }

    // EX-10: Generate statement — zero totalRevenue (boundary, valid)
    @Test
    @DisplayName("EX-10: generateStatement — zero totalRevenue is accepted")
    void generateStatement_zeroTotalRevenue_isValid() {
        statement.setTotalRevenue(0.0);
        doNothing().when(repository).save(any(RoyaltyStatement.class));

        RoyaltyStatement result = service.generateStatement(statement);

        assertEquals("Draft", result.getStatus());
    }

    // EX-11: Generate statement — response contains all fields
    @Test
    @DisplayName("EX-11: generateStatement success — all response fields present")
    void generateStatement_responseContainsAllFields() {
        doNothing().when(repository).save(any(RoyaltyStatement.class));

        RoyaltyStatement result = service.generateStatement(statement);

        assertEquals(501, result.getCreatorID());
        assertEquals("2025-Q1", result.getPeriod());
        assertEquals(2000.0, result.getTotalRevenue());
        assertEquals("Draft", result.getStatus());
    }

    // EX-12: Generate statement — repository throws exception
    @Test
    @DisplayName("EX-12: generateStatement — repository throws → propagates uncaught")
    void generateStatement_repositoryThrowsException() {
        doThrow(new RuntimeException("DB unavailable")).when(repository).save(any(RoyaltyStatement.class));

        assertThrows(RuntimeException.class, () -> service.generateStatement(statement));
    }

    // EX-13: Finalise statement — findStatusById throws exception
    @Test
    @DisplayName("EX-13: finaliseStatement — findStatusById throws → caught as 400")
    void finaliseStatement_findStatusThrows() {
        when(repository.findStatusById(anyInt()))
            .thenThrow(new com.mediahub.royalty.exception.ResourceNotFoundException("Row not found"));

        assertThrows(com.mediahub.royalty.exception.ResourceNotFoundException.class,
                () -> service.finaliseStatement(99));
        verify(repository, never()).updateStatus(anyInt(), anyString());
    }

    // EX-14: Mark as Paid — findStatusById throws exception
    @Test
    @DisplayName("EX-14: markAsPaid — findStatusById throws → caught as 400")
    void markAsPaid_findStatusThrows() {
        when(repository.findStatusById(anyInt()))
            .thenThrow(new com.mediahub.royalty.exception.ResourceNotFoundException("Row not found"));

        assertThrows(com.mediahub.royalty.exception.ResourceNotFoundException.class,
                () -> service.markAsPaid(99));
        verify(repository, never()).updateStatus(anyInt(), anyString());
    }
}
