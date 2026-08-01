package com.mediahub.subscriptionPlan.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateHistoryRequest {
    private Long subscriptionId;
    private Long userId;
    private Long fromPlanId;
    private Long toPlanId;
    private String changeType;
    private LocalDateTime changeDate;
    private String remarks;
}