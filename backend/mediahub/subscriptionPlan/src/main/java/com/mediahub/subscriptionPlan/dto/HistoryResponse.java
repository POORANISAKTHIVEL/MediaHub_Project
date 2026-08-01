package com.mediahub.subscriptionPlan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryResponse {

    private Long historyId;
    private Long subscriptionId;
    private Long userId;
    private Long fromPlanId;
    private Long toPlanId;
    private String changeType;
    private LocalDateTime changeDate;
    private String remarks;
}