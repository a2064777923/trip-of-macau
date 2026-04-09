package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BuildingResponse {
    private Long id;
    private String buildingCode;
    private String nameZh;
    private String addressZh;
    private String cityCode;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer totalFloors;
    private String coverImageUrl;
    private String status;
}
