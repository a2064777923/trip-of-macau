package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminMapTileResponse {

    private Long id;
    private String mapId;
    private String style;
    private String cdnBase;
    private String controlPointsUrl;
    private String poisUrl;
    private Integer zoomMin;
    private Integer zoomMax;
    private Integer defaultZoom;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private String version;
    private String status;
    private LocalDateTime updatedAt;
}

