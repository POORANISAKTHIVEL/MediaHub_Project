package com.mediahub.subscriptionPlan.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private Long userId;

    private Long fromPlanId;

    @Column(nullable = false)
    private Long toPlanId;

    @Column(nullable = false)
    private String changeType;

    @Column(nullable = false)
    private LocalDateTime changeDate;

    private String remarks;
}