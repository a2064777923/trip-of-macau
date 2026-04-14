package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminSubMapResponse {
    private Long id;
    private Long cityId;
    private String cityCode;
    private String cityName;
    private String code;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String subtitleZh;
    private String subtitleEn;
    private String subtitleZht;
    private String subtitlePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private Long coverAssetId;
    private String sourceCoordinateSystem;
    private BigDecimal sourceCenterLat;
    private BigDecimal sourceCenterLng;
    private BigDecimal centerLat;
    private BigDecimal centerLng;
    private String boundsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private Integer sortOrder;
    private String status;
    private LocalDateTime publishedAt;
    private List<AdminSpatialAssetLinkResponse> attachments;
}
