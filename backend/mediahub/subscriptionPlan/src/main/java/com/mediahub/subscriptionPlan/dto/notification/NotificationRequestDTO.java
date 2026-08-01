package com.mediahub.subscriptionPlan.dto.notification;


import lombok.Data;

@Data
public class NotificationRequestDTO {

    private Long userId;

    private String message;

    private String category;
}
