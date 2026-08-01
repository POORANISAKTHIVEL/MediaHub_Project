package com.mediahub.royalty.repository;

import com.mediahub.royalty.model.RoyaltyRule;
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
public class RoyaltyRuleRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RoyaltyRuleRepository repository;

    private RoyaltyRule rule;

    @BeforeEach
    void setUp() {
        rule = new RoyaltyRule();
        rule.setRuleID(1);
        rule.setCreatorTier("Gold");
        rule.setRevenueSharePercent(30.0);
        rule.setMinimumPayoutThreshold(100.0);
        rule.setPayoutFrequency("Monthly");
        rule.setEffectiveDate(new Date());
        rule.setStatus("Active");
    }

    @Test
    @DisplayName("RT-01: save() calls persist and returns 1")
    void save_success() {
        doNothing().when(entityManager).persist(any(RoyaltyRule.class));
        repository.save(rule);

        verify(entityManager, times(1)).persist(any(RoyaltyRule.class));
    }

    @Test
    @DisplayName("RT-02: save() returns 0 when persist throws")
    void save_failure() {
        doThrow(new RuntimeException("DB error"))
                .when(entityManager).persist(any(RoyaltyRule.class));
        assertThrows(RuntimeException.class, () -> repository.save(rule));
    }

    @Test
    @DisplayName("RT-03: findAll() returns list of rules")
    void findAll_returnsList() {
        TypedQuery<RoyaltyRule> query = mock(TypedQuery.class);
        List<RoyaltyRule> mockList = Arrays.asList(new RoyaltyRule(), new RoyaltyRule());

        when(entityManager.createQuery(anyString(), eq(RoyaltyRule.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(mockList);

        List<RoyaltyRule> result = repository.findAll();

        assertEquals(2, result.size());
        verify(entityManager).createQuery(anyString(), eq(RoyaltyRule.class));
        verify(query).getResultList();
    }

    @Test
    @DisplayName("RT-04: findAll() returns empty list when no records")
    void findAll_returnsEmptyList() {
        TypedQuery<RoyaltyRule> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(RoyaltyRule.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<RoyaltyRule> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("RT-05: findById() returns matching rule")
    void findById_returnsRule() {
        when(entityManager.find(RoyaltyRule.class, 1)).thenReturn(rule);

        RoyaltyRule result = repository.findById(1);

        assertNotNull(result);
        assertEquals("Gold", result.getCreatorTier());
    }

    @Test
    @DisplayName("RT-06: findById() throws when no record found")
    void findById_throwsWhenNotFound() {
        when(entityManager.find(RoyaltyRule.class, 99)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> repository.findById(99));
    }

    @Test
    @DisplayName("RT-07: updateStatus() returns 1 on success")
    void updateStatus_success() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("status"), eq("Inactive"))).thenReturn(query);
        when(query.setParameter(eq("ruleID"), eq(1))).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        int result = repository.updateStatus(1, "Inactive");

        assertEquals(1, result);
    }

    @Test
    @DisplayName("RT-08: updateStatus() returns 0 when no row updated")
    void updateStatus_notFound() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = repository.updateStatus(99, "Inactive");

        assertEquals(0, result);
    }

    @Test
    @DisplayName("RT-09: delete() returns 1 on success")
    void delete_success() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("ruleID"), eq(1))).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        int result = repository.delete(1);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("RT-10: delete() returns 0 when no row does not exist")
    void delete_notFound() {
        Query query = mock(Query.class);

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = repository.delete(99);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("RT-11: findStatusById() returns correct status")
    void findStatusById_returnsStatus() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("ruleID"), eq(1))).thenReturn(query);
        when(query.getSingleResult()).thenReturn("Active");

        String status = repository.findStatusById(1);

        assertEquals("Active", status);
    }

    @Test
    @DisplayName("RT-12: findStatusById() throws when rule does not exist")
    void findStatusById_throwsWhenNotFound() {
        TypedQuery<String> query = mock(TypedQuery.class);

        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(eq("ruleID"), eq(99))).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(RuntimeException.class, () -> repository.findStatusById(99));
    }

    @Test
    @DisplayName("RT-13: save() returns 0 when persist throws")
    void save_jpaThrowsException() {
        doThrow(new RuntimeException("DB connection lost"))
                .when(entityManager).persist(any(RoyaltyRule.class));
        assertThrows(RuntimeException.class, () -> repository.save(rule));
    }

    @Test
    @DisplayName("RT-14: findAll() propagates exception from createQuery")
    void findAll_jpaThrowsException() {
        when(entityManager.createQuery(anyString(), eq(RoyaltyRule.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> repository.findAll());
    }
}
