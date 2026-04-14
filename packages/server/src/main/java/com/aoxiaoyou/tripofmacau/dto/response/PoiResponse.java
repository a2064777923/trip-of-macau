package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PoiResponse {

    private Long id;
    private Long cityId;
    private String cityCode;
    private Long subMapId;
    private String subMapCode;
    private String subMapName;
    private Long storylineId;
    private String storylineCode;
    private String storylineName;
    private String code;
    private String name;
    private String subtitle;
    private String address;
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer triggerRadius;
    private Integer manualCheckinRadius;
    private Integer staySeconds;
    private String categoryCode;
    private String difficulty;
    private String district;
    private String description;
    private String introTitle;
    private String introSummary;
    private String coverImageUrl;
    private String mapIconUrl;
    private String audioUrl;
    private String popupConfigJson;
    private String displayConfigJson;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
}
