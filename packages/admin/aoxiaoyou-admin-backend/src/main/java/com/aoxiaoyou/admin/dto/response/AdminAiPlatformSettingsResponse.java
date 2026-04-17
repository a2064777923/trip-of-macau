package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminAiPlatformSettingsResponse {

    private Integer inventoryFreshnessHours;
    private Integer syncHistoryLimit;
    private BigDecimal dailyCostAlertUsd;
    private BigDecimal providerFailureRateWarning;
    private Integer recentWindowHours;
    private Integer allowOperatorGlobalHistory;
}
