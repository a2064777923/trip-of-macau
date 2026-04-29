package com.aoxiaoyou.tripofmacau.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndoorRuntimeInteractionRequest {

    @NotNull
    private Long floorId;

    @NotNull
    private Long nodeId;

    @NotNull
    private Long behaviorId;

    private String triggerId;

    @NotBlank
    private String eventType;

    private String eventTimestamp;

    private BigDecimal relativeX;

    private BigDecimal relativeY;

    private Long dwellMs;

    private String clientSessionId;

    private String locale;
}
