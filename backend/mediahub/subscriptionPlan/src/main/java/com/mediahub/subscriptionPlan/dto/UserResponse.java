package com.mediahub.subscriptionPlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;
    private String name;
    private String roles;
    private String email;
    private String phone;
    private String country;
    private String status;
}