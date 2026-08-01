package com.mediahub.subscriptionPlan.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String phone;
    private String country;
}