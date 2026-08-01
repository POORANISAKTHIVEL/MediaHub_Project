package com.mediahub.subscriptionPlan.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSubscriptionRequest {

    private Long planId;
    private LocalDate endDate;
    private String renewalType;
}
