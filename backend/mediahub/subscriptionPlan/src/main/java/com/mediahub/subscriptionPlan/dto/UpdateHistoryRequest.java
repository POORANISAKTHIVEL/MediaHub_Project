package com.mediahub.subscriptionPlan.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateHistoryRequest {
    private String remarks;
    private LocalDateTime changeDate;
}