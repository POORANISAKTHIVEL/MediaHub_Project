package com.mediahub.royalty.repository;

import com.mediahub.royalty.model.RoyaltyStatement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class RoyaltyStatementRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RoyaltyStatementRepository repository;

    private RoyaltyStatement statement;

    @BeforeEach
    void setUp() {
        statement = new RoyaltyStatement();
        statement.setStatementID(1);
        statement.setCreatorID(501);
        statement.setPeriod("2025-Q1");
        statement.setTotalViews(50000L);
        statement.setTotalRevenue(2000.0);
        statement.setRoyaltyAmount(600.0);
        statement.setStatus("Draft");
    }

    @Test
    @DisplayName("RT-15: save() calls persist and returns 1")
    void save_success() {
        doNothing().when(entityManager).persist(any(RoyaltyStatement.class));

        // save is void now; just verify persist/merge is invoked
        repository.save(statement);

        verify(entityManager, times(1)).persist(any(RoyaltyStatement.class));
    }

    @Test
    @DisplayName("RT-16: save() returns 0 when persist throws")
    void save_failure() {
        doThrow(new RuntimeException("DB error"))
                .when(entityManager).persist(any(RoyaltyStatement.class));

        assertThrows(RuntimeException.class, () -> repository.save(statement));
    }

    @Test
    @DisplayName("RT-17: findAll() returns list of statements")
    void findAll_returnsList() {
        TypedQuery<RoyaltyStatement> query = mock(TypedQuery.class);
        List<RoyaltyStatement> mockList = Arrays.asList(new RoyaltyStatement(), new RoyaltyStatement());

        when(entityManager.createQuery(anyString(), eq(RoyaltyStatement.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockList);

        List<RoyaltyStatement> result = repository.findAll();

        assertEquals(2, result.size());
        verify(entityManager).createQuery(anyString(), eq(RoyaltyStatement.class));
        verify(query).getResultList();
    }

    @Test
    @DisplayName("RT-18: findAll() returns empty list when no records")
    void findAll_returnsEmptyList() {
        TypedQuery<RoyaltyStatement> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(RoyaltyStatement.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<RoyaltyStatement> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("RT-19: findById() returns matching statement")
    void findById_returnsStatement() {
        when(entityManager.find(RoyaltyStatement.class, 1)).thenReturn(statement);

        RoyaltyStatement result = repository.findById(1);

        assertNotNull(result);
        assertEquals("2025-Q1", result.getPeriod());
        assertEquals(501, result.getCreatorID());
    }

    @Test
    @DisplayName("RT-20: findById() throws when no record found")
    void findById_throwsWhenNotFound() {
        when(entityManager.find(RoyaltyStatement.class, 99)).thenReturn(null);

        assertThrows(com.mediahub.royalty.exception.ResourceNotFoundException.class,
                () -> repository.findById(99));
    }

    @Test
    @DisplayName("RT-21: updateStatus() returns 1 on success")
    void updateStatus_success() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("status"), eq("Finalised"))).thenReturn(query);
        when(query.setParameter(eq("statementID"), eq(1))).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        int result = repository.updateStatus(1, "Finalised");

        assertEquals(1, result);
    }

    @Test
    @DisplayName("RT-22: updateStatus() returns 0 when no row updated")
    void updateStatus_notFound() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = repository.updateStatus(99, "Paid");

        assertEquals(0, result);
    }

    @Test
    @DisplayName("RT-23: findStatusById() returns correct status string")
    void findStatusById_returnsStatus() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("statementID"), eq(1))).thenReturn(query);
        when(query.getSingleResult()).thenReturn("Draft");

        String status = repository.findStatusById(1);

        assertEquals("Draft", status);
    }

    @Test
    @DisplayName("RT-24: findStatusById() throws when statement does not exist")
    void findStatusById_throwsWhenNotFound() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("statementID"), eq(99))).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(com.mediahub.royalty.exception.ResourceNotFoundException.class,
                () -> repository.findStatusById(99));
    }

    @Test
    @DisplayName("RT-25: save() returns 0 when persist throws")
    void save_jpaThrowsException() {
        doThrow(new RuntimeException("DB connection lost"))
                .when(entityManager).persist(any(RoyaltyStatement.class));

        assertThrows(RuntimeException.class, () -> repository.save(statement));
    }

    @Test
    @DisplayName("RT-26: findAll() propagates exception from createQuery")
    void findAll_jpaThrowsException() {
        when(entityManager.createQuery(anyString(), eq(RoyaltyStatement.class)))
                .thenThrow(new RuntimeException("Query failed"));

        assertThrows(RuntimeException.class, () -> repository.findAll());
    }
}
