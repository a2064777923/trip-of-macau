package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TriggerLogResponse {

    private Long id;
    private Long userId;
    private Long poiId;
    private String triggerType;
    private BigDecimal distance;
    private BigDecimal gpsAccuracy;
    private Boolean wifiUsed;
    private LocalDateTime createdAt;
}
