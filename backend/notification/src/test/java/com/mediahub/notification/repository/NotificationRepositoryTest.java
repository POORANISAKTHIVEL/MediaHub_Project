package com.mediahub.notification.repository;

import com.mediahub.notification.entity.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository repository;

    private Notification buildNotification(Long userId,
                                           Notification.Status status,
                                           Notification.Category category) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage("Test message for user " + userId);
        notification.setCategory(category);
        notification.setStatus(status);
        notification.setCreatedDate(LocalDateTime.now());
        return notification;
    }

    // ---------- findByUserId ----------

    @Test
    void findByUserId_returnsOnlyMatchingUsersNotifications() {
        // Use a userId unlikely to collide with pre-existing data, and a
        // unique marker so the assertions do not depend on table state
        // (this test runs against the real MySQL DB - see class header).
        Long userId = 9001L;
        Notification a = buildNotification(userId,
                Notification.Status.UNREAD, Notification.Category.CONTENT);
        a.setMessage("marker-A");
        Notification b = buildNotification(userId,
                Notification.Status.READ, Notification.Category.ROYALTY);
        b.setMessage("marker-B");
        entityManager.persist(a);
        entityManager.persist(b);
        // A different user's notification must NOT be returned.
        entityManager.persist(buildNotification(9002L,
                Notification.Status.UNREAD, Notification.Category.LICENSE));
        entityManager.flush();

        List<Notification> result = repository.findByUserId(userId);

        // Every returned row belongs to the queried user...
        assertThat(result)
                .isNotEmpty()
                .extracting(Notification::getUserId)
                .containsOnly(userId);
        // ...and both inserted rows are present.
        assertThat(result)
                .extracting(Notification::getMessage)
                .contains("marker-A", "marker-B");
    }

    @Test
    void findByUserId_returnsEmptyListWhenNoNotificationsExist() {
        entityManager.persist(buildNotification(1L,
                Notification.Status.UNREAD, Notification.Category.CONTENT));
        entityManager.flush();

        List<Notification> result = repository.findByUserId(99L);

        assertThat(result).isEmpty();
    }

    // ---------- findByUserIdAndStatus ----------

    @Test
    void findByUserIdAndStatus_returnsOnlyMatchingStatus() {
        entityManager.persist(buildNotification(5L,
                Notification.Status.UNREAD, Notification.Category.CONTENT));
        entityManager.persist(buildNotification(5L,
                Notification.Status.UNREAD, Notification.Category.EDITORIAL));
        entityManager.persist(buildNotification(5L,
                Notification.Status.READ, Notification.Category.SUBSCRIPTION));
        entityManager.flush();

        List<Notification> result = repository.findByUserIdAndStatus(
                5L, Notification.Status.UNREAD);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Notification::getStatus)
                .containsOnly(Notification.Status.UNREAD);
    }

    @Test
    void findByUserIdAndStatus_returnsEmptyListWhenStatusDoesNotMatch() {
        entityManager.persist(buildNotification(7L,
                Notification.Status.READ, Notification.Category.CONTENT));
        entityManager.flush();

        List<Notification> result = repository.findByUserIdAndStatus(
                7L, Notification.Status.DISMISSED);

        assertThat(result).isEmpty();
    }

    // ---------- save (persistence sanity) ----------

    @Test
    void save_persistsNotificationAndGeneratesId() {
        Notification saved = repository.save(buildNotification(10L,
                Notification.Status.UNREAD, Notification.Category.CONTENT));

        assertThat(saved.getNotificationId()).isNotNull();
        assertThat(repository.findById(saved.getNotificationId()))
                .isPresent();
    }
}
