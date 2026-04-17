package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminAiPlatformSettingsUpsertRequest {

    private Integer inventoryFreshnessHours;

    private Integer syncHistoryLimit;

    private BigDecimal dailyCostAlertUsd;

    private BigDecimal providerFailureRateWarning;

    private Integer recentWindowHours;

    private Integer allowOperatorGlobalHistory;
}
