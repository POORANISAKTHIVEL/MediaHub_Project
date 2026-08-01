package com.mediahub.subscriptionPlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    private Long planId;
    private String name;
    private Double price;
    private String billingCycle;
    private String contentAccessLevel;
    private Integer maxDevices;
    private Integer downloadAllowed;
    private String status;
}