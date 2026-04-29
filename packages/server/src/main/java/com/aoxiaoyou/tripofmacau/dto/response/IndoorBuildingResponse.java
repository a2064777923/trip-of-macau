package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class IndoorBuildingResponse {
    private Long id;
    private String buildingCode;
    private String bindingMode;
    private Long cityId;
    private String cityCode;
    private Long subMapId;
    private Long poiId;
    private String name;
    private String address;
    private String description;
    private String coverImageUrl;
    private String popupConfigJson;
    private String displayConfigJson;
    private String sourceCoordinateSystem;
    private BigDecimal sourceLatitude;
    private BigDecimal sourceLongitude;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalFloors;
    private Integer basementFloors;
    private List<IndoorFloorResponse> floors;
}
