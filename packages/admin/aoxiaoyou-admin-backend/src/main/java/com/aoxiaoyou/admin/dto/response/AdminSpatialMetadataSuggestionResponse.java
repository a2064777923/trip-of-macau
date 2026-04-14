package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class AdminSpatialMetadataSuggestionResponse {
    private String entityType;
    private String code;
    private String countryCode;
    private String sourceCoordinateSystem;
    private BigDecimal suggestedCenterLat;
    private BigDecimal suggestedCenterLng;
    private Integer defaultZoom;
    private String note;
    private boolean amapAssisted;
}
