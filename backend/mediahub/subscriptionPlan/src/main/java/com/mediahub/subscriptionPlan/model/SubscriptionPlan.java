package com.mediahub.subscriptionPlan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String billingCycle;

    private String contentAccessLevel;
    private Integer maxDevices;
    private Integer downloadAllowed;

    @Column(nullable = false)
    @Builder.Default
    private String status = "Active";
}