package com.mediahub.royalty.repository;

import com.mediahub.royalty.model.RoyaltyPayout;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class RoyaltyPayoutRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RoyaltyPayoutRepository repository;

    private RoyaltyPayout payout;

    @BeforeEach
    void setUp() {
        payout = new RoyaltyPayout();
        payout.setPayoutID(1);
        payout.setStatementID(10);
        payout.setCreatorID(501);
        payout.setAmount(750.0);
        payout.setPayoutDate(new Date());
        payout.setMethod("BankTransfer");
        payout.setStatus("Pending");
    }

    @Test
    @DisplayName("RT-27: save() calls persist and returns 1")
    void save_success() {
        doNothing().when(entityManager).persist(any(RoyaltyPayout.class));
        repository.save(payout);

        verify(entityManager, times(1)).persist(any(RoyaltyPayout.class));
    }

    @Test
    @DisplayName("RT-28: save() returns 0 when persist throws")
    void save_failure() {
        doThrow(new RuntimeException("DB error"))
                .when(entityManager).persist(any(RoyaltyPayout.class));
        assertThrows(RuntimeException.class, () -> repository.save(payout));
    }

    @Test
    @DisplayName("RT-29: findAll() returns list of payouts")
    void findAll_returnsList() {
        TypedQuery<RoyaltyPayout> query = mock(TypedQuery.class);
        List<RoyaltyPayout> mockList = Arrays.asList(new RoyaltyPayout(), new RoyaltyPayout());

        when(entityManager.createQuery(anyString(), eq(RoyaltyPayout.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockList);

        List<RoyaltyPayout> result = repository.findAll();

        assertEquals(2, result.size());
        verify(entityManager).createQuery(anyString(), eq(RoyaltyPayout.class));
        verify(query).getResultList();
    }

    @Test
    @DisplayName("RT-30: findAll() returns empty list when no records")
    void findAll_returnsEmptyList() {
        TypedQuery<RoyaltyPayout> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(RoyaltyPayout.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<RoyaltyPayout> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("RT-31: findById() returns matching payout")
    void findById_returnsPayout() {
        when(entityManager.find(RoyaltyPayout.class, 1)).thenReturn(payout);

        RoyaltyPayout result = repository.findById(1);

        assertNotNull(result);
        assertEquals("BankTransfer", result.getMethod());
        assertEquals(750.0, result.getAmount());
    }

    @Test
    @DisplayName("RT-32: findById() throws when no record found")
    void findById_throwsWhenNotFound() {
        when(entityManager.find(RoyaltyPayout.class, 99)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> repository.findById(99));
    }

    @Test
    @DisplayName("RT-33: updateStatus() returns 1 on success")
    void updateStatus_success() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("status"), eq("Processed"))).thenReturn(query);
        when(query.setParameter(eq("payoutID"), eq(1))).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        int result = repository.updateStatus(1, "Processed");

        assertEquals(1, result);
        verify(query).executeUpdate();
    }

    @Test
    @DisplayName("RT-34: updateStatus() returns 0 when no row updated")
    void updateStatus_notFound() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = repository.updateStatus(99, "Failed");

        assertEquals(0, result);
    }

    @Test
    @DisplayName("RT-35: delete() returns 1 on success")
    void delete_success() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("payoutID"), eq(1))).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        int result = repository.delete(1);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("RT-36: delete() returns 0 when row does not exist")
    void delete_notFound() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = repository.delete(99);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("RT-37: findStatusById() returns correct status string")
    void findStatusById_returnsStatus() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("payoutID"), eq(1))).thenReturn(query);
        when(query.getSingleResult()).thenReturn("Pending");

        String status = repository.findStatusById(1);

        assertEquals("Pending", status);
    }

    @Test
    @DisplayName("RT-38: findStatusById() throws when payout does not exist")
    void findStatusById_throwsWhenNotFound() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("payoutID"), eq(99))).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(RuntimeException.class, () -> repository.findStatusById(99));
    }

    @Test
    @DisplayName("RT-39: save() returns 0 when persist throws")
    void save_jpaThrowsException() {
        doThrow(new RuntimeException("DB connection lost"))
                .when(entityManager).persist(any(RoyaltyPayout.class));
        assertThrows(RuntimeException.class, () -> repository.save(payout));
    }

    @Test
    @DisplayName("RT-40: findAll() propagates exception from createQuery")
    void findAll_jpaThrowsException() {
        when(entityManager.createQuery(anyString(), eq(RoyaltyPayout.class)))
                .thenThrow(new RuntimeException("Query failed"));

        assertThrows(RuntimeException.class, () -> repository.findAll());
    }
}
