package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CityResponse {

    private Long id;
    private String code;
    private String name;
    private String subtitle;
    private String description;
    private String countryCode;
    private String sourceCoordinateSystem;
    private BigDecimal sourceCenterLat;
    private BigDecimal sourceCenterLng;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private Integer defaultZoom;
    private String unlockType;
    private String coverImageUrl;
    private String bannerImageUrl;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<SubMapResponse> subMaps;
    private Integer sortOrder;
}
