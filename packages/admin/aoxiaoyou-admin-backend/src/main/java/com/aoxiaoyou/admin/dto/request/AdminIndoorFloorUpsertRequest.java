package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminIndoorFloorUpsertRequest {
    private Long indoorMapId;
    private String floorCode;
    private Integer floorNumber;
    private String floorNameZh;
    private String floorNameEn;
    private String floorNameZht;
    private String floorNamePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private Long coverAssetId;
    private Long floorPlanAssetId;
    private String tilePreviewImageUrl;
    private BigDecimal altitudeMeters;
    private BigDecimal areaSqm;
    private BigDecimal zoomMin;
    private BigDecimal zoomMax;
    private BigDecimal defaultZoom;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<AdminSpatialAssetLinkUpsertRequest> attachments = new ArrayList<>();
    private List<Long> attachmentAssetIds = new ArrayList<>();
    private Integer sortOrder;
    private String status;
    private String publishedAt;
}
