package com.mediahub.notification.service;

import com.mediahub.notification.dto.request.NotificationRequestDTO;
import com.mediahub.notification.dto.response.NotificationResponseDTO;
import com.mediahub.notification.entity.Notification;
import com.mediahub.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    private Notification existing;

    @BeforeEach
    void setUp() {
        existing = new Notification();
        existing.setNotificationId(100L);
        existing.setUserId(1L);
        existing.setMessage("Existing notification");
        existing.setCategory(Notification.Category.CONTENT);
        existing.setStatus(Notification.Status.UNREAD);
        existing.setCreatedDate(LocalDateTime.now());
    }

    // ---------- createNotification ----------

    @Test
    void createNotification_savesAndReturnsResponseWithUnreadStatus() {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setUserId(1L);
        request.setMessage("New notification");
        request.setCategory(Notification.Category.SUBSCRIPTION);

        // repository.save returns the entity it is given (with an id)
        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification toSave = invocation.getArgument(0);
                    toSave.setNotificationId(200L);
                    return toSave;
                });

        NotificationResponseDTO response = service.createNotification(request);

        assertThat(response).isNotNull();
        assertThat(response.getNotificationId()).isEqualTo(200L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("New notification");
        assertThat(response.getCategory())
                .isEqualTo(Notification.Category.SUBSCRIPTION);
        // Business rule: every new notification starts as UNREAD
        assertThat(response.getStatus())
                .isEqualTo(Notification.Status.UNREAD);
        assertThat(response.getCreatedDate()).isNotNull();
        verify(repository).save(any(Notification.class));
    }

    // ---------- getAllNotifications ----------

    @Test
    void getAllNotifications_returnsMappedListForUser() {
        when(repository.findByUserId(1L))
                .thenReturn(List.of(existing));

        List<NotificationResponseDTO> result =
                service.getAllNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotificationId()).isEqualTo(100L);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        verify(repository).findByUserId(1L);
    }

    @Test
    void getAllNotifications_throwsExceptionWhenNoneFound() {
        when(repository.findByUserId(42L))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->
                service.getAllNotifications(42L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("42");

        verify(repository).findByUserId(42L);
 }

    // ---------- getUnreadNotifications ----------

    @Test
    void getUnreadNotifications_returnsOnlyUnreadForUser() {
        when(repository.findByUserIdAndStatus(
                1L, Notification.Status.UNREAD))
                .thenReturn(List.of(existing));

        List<NotificationResponseDTO> result =
                service.getUnreadNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus())
                .isEqualTo(Notification.Status.UNREAD);
        verify(repository).findByUserIdAndStatus(
                1L, Notification.Status.UNREAD);
    }

  @Test
  void getUnreadNotifications_throwsExceptionWhenNoneUnread() {
        when(repository.findByUserIdAndStatus(
                1L, Notification.Status.UNREAD))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() ->
                service.getUnreadNotifications(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("1");

        verify(repository).findByUserIdAndStatus(
                1L, Notification.Status.UNREAD);
  }

    // ---------- updateNotification ----------

    @Test
    void updateNotification_updatesStatusWhenNotificationExists() {
        when(repository.findById(100L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponseDTO response =
                service.updateNotification(100L, "read");

        assertThat(response.getStatus())
                .isEqualTo(Notification.Status.READ);
        verify(repository).findById(100L);
        verify(repository).save(existing);
    }

    @Test
    void updateNotification_throwsWhenNotificationNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateNotification(999L, "read"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void updateNotification_throwsWhenNotificationIsDismissed() {
        existing.setStatus(Notification.Status.DISMISSED);
        when(repository.findById(100L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.updateNotification(100L, "read"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Dismissed");

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void updateNotification_throwsForInvalidStatusValue() {
        when(repository.findById(100L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                service.updateNotification(100L, "NOT_A_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any(Notification.class));
    }
}
