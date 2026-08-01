package com.mediahub.subscriptionPlan.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String roles;
    private String email;
    private String phone;
    private String country;
}