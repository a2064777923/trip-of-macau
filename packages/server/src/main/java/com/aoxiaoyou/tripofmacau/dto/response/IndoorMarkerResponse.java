package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class IndoorMarkerResponse {
    private Long id;
    private String markerCode;
    private String nodeType;
    private String name;
    private String description;
    private BigDecimal relativeX;
    private BigDecimal relativeY;
    private Long relatedPoiId;
    private String iconUrl;
    private String animationUrl;
    private String linkedEntityType;
    private Long linkedEntityId;
    private String tagsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private String metadataJson;
    private Integer sortOrder;
    private String status;
}
