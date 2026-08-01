package com.mediahub.subscriptionPlan.dto;

import lombok.Data;

@Data
public class UpdatePlanRequest {

    private Double price;
    private String billingCycle;
    private Integer maxDevices;
    private Integer downloadAllowed;
}