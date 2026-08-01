package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateHistoryRequest;
import com.mediahub.subscriptionPlan.dto.UpdateHistoryRequest;
import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import com.mediahub.subscriptionPlan.repository.SubscriptionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionHistoryServiceTest {

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @InjectMocks
    private SubscriptionHistoryService subscriptionHistoryService;

    private SubscriptionHistory history;
    private CreateHistoryRequest createRequest;
    private UpdateHistoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        history = SubscriptionHistory.builder()
                .historyId(1L).subscriptionId(1L).userId(1L)
                .fromPlanId(1L).toPlanId(2L).changeType("Upgrade")
                .changeDate(LocalDateTime.of(2025, 1, 1, 9, 0))
                .remarks("Upgraded to Premium")
                .build();

        createRequest = new CreateHistoryRequest();
        createRequest.setSubscriptionId(1L);
        createRequest.setUserId(1L);
        createRequest.setFromPlanId(1L);
        createRequest.setToPlanId(2L);
        createRequest.setChangeType("Upgrade");
        createRequest.setChangeDate(LocalDateTime.of(2025, 1, 1, 9, 0));
        createRequest.setRemarks("Upgraded to Premium");

        updateRequest = new UpdateHistoryRequest();
        updateRequest.setRemarks("Upgraded after promo offer");
        updateRequest.setChangeDate(LocalDateTime.of(2025, 4, 15, 10, 0));
    }

    @Test
    void createHistory_Success() {
        when(subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(1L, "Upgrade"))
                .thenReturn(Optional.empty());
        when(subscriptionHistoryRepository.save(any(SubscriptionHistory.class)))
                .thenReturn(history);

        Map<String, String> result = subscriptionHistoryService.createHistory(createRequest);

        assertEquals("History created successfully", result.get("message"));
        verify(subscriptionHistoryRepository, times(1)).save(any(SubscriptionHistory.class));
    }

    @Test
    void createHistory_AlreadyExists_ThrowsException() {
        when(subscriptionHistoryRepository
                .findBySubscriptionIdAndChangeType(1L, "Upgrade"))
                .thenReturn(Optional.of(history));

        assertThrows(IllegalStateException.class,
                () -> subscriptionHistoryService.createHistory(createRequest));
        verify(subscriptionHistoryRepository, never()).save(any());
    }

    @Test
    void fetchHistories_Success() {
        when(subscriptionHistoryRepository.findAll())
                .thenReturn(Arrays.asList(history));

        List<SubscriptionHistory> result = subscriptionHistoryService.fetchHistories();

        assertEquals(1, result.size());
        assertEquals("Upgrade", result.get(0).getChangeType());
    }

    @Test
    void fetchHistoryById_Success() {
        when(subscriptionHistoryRepository.findById(1L))
                .thenReturn(Optional.of(history));

        SubscriptionHistory result = subscriptionHistoryService.fetchHistoryById(1L);

        assertEquals(1L, result.getHistoryId());
        assertEquals("Upgrade", result.getChangeType());
        assertEquals("Upgraded to Premium", result.getRemarks());
    }

    @Test
    void fetchHistoryById_NotFound_ThrowsException() {
        when(subscriptionHistoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionHistoryService.fetchHistoryById(99L));
    }

    @Test
    void updateHistory_Success() {
        when(subscriptionHistoryRepository.findById(1L))
                .thenReturn(Optional.of(history));
        when(subscriptionHistoryRepository.save(any(SubscriptionHistory.class)))
                .thenReturn(history);

        Map<String, String> result = subscriptionHistoryService.updateHistory(1L, updateRequest);

        assertEquals("History updated successfully", result.get("message"));
        verify(subscriptionHistoryRepository, times(1)).save(any(SubscriptionHistory.class));
    }

    @Test
    void updateHistory_NotFound_ThrowsException() {
        when(subscriptionHistoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> subscriptionHistoryService.updateHistory(99L, updateRequest));
    }
}