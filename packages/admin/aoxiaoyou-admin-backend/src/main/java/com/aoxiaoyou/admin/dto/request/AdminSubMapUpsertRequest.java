package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminSubMapUpsertRequest {
    private Long cityId;
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
    private String sourceCoordinateSystem = "GCJ02";
    private Double sourceCenterLat;
    private Double sourceCenterLng;
    private Double centerLat;
    private Double centerLng;
    private String boundsJson;
    private String popupConfigJson;
    private String displayConfigJson;
    private Integer sortOrder = 0;
    private String status = "draft";
    private String publishedAt;
    private List<AdminSpatialAssetLinkUpsertRequest> attachments = new ArrayList<>();
}
