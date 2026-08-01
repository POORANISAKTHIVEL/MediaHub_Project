package com.mediahub.subscriptionPlan.repository;

import com.mediahub.subscriptionPlan.model.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
    Optional<SubscriptionHistory> findBySubscriptionIdAndChangeType(Long subscriptionId, String changeType);
}