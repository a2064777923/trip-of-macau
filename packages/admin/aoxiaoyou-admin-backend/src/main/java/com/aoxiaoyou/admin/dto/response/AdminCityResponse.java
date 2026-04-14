package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminCityResponse {
    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String subtitleZh;
    private String subtitleEn;
    private String subtitleZht;
    private String subtitlePt;
    private String countryCode;
    private String customCountryName;
    private String sourceCoordinateSystem;
    private BigDecimal sourceCenterLat;
    private BigDecimal sourceCenterLng;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private Integer defaultZoom;
    private String unlockType;
    private String unlockConditionJson;
    private Long coverAssetId;
    private Long bannerAssetId;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<AdminSubMapResponse> subMaps;
    private List<AdminSpatialAssetLinkResponse> attachments;
    private Integer sortOrder;
    private String status;
    private LocalDateTime publishedAt;
}
