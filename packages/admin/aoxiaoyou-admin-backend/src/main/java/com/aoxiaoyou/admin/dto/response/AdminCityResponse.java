package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminCityResponse {
    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String countryCode;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private Integer defaultZoom;
    private String unlockType;
    private String coverImageUrl;
    private String bannerUrl;
    private String descriptionZh;
    private Integer sortOrder;
    private String status;
}
