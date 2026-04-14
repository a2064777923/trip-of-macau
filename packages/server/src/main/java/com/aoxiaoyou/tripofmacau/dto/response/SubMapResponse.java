package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SubMapResponse {
    private Long id;
    private Long cityId;
    private String cityCode;
    private String code;
    private String name;
    private String subtitle;
    private String description;
    private String sourceCoordinateSystem;
    private BigDecimal sourceCenterLat;
    private BigDecimal sourceCenterLng;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private String boundsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private String coverImageUrl;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
}
