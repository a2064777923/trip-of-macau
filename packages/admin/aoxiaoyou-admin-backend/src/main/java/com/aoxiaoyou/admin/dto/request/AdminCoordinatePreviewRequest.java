package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminCoordinatePreviewRequest {
    private String sourceCoordinateSystem = "GCJ02";
    private BigDecimal latitude;
    private BigDecimal longitude;
}
