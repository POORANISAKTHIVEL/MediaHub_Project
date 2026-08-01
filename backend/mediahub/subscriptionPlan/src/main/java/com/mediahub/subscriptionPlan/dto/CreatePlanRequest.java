package com.mediahub.subscriptionPlan.dto;

import lombok.Data;

@Data
public class CreatePlanRequest {
    private String name;
    private Double price;
    private String billingCycle;
    private String contentAccessLevel;
    private Integer maxDevices;
    private Integer downloadAllowed;
}