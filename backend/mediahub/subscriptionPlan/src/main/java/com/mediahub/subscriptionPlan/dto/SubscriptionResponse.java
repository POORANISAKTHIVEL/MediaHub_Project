package com.mediahub.subscriptionPlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;
    private Long userId;
    private Long planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String renewalType;
    private String status;
}