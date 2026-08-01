package com.mediahub.subscriptionPlan.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateSubscriptionRequest {
    private Long userId;
    private Long planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String renewalType;
}