package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminCityUpsertRequest {

    private Upsert upsert = new Upsert();

    @Data
    public static class Upsert {
        private String code;
        private String nameZh;
        private String nameEn;
        private String nameZht;
        private String namePt;
        private String subtitleZh;
        private String subtitleEn;
        private String subtitleZht;
        private String subtitlePt;
        private String countryCode = "MO";
        private String customCountryName;
        private String sourceCoordinateSystem = "GCJ02";
        private Double sourceCenterLat;
        private Double sourceCenterLng;
        private Double centerLat;
        private Double centerLng;
        private Integer defaultZoom = 13;
        private String unlockType = "default";
        private String unlockConditionJson;
        private Long coverAssetId;
        private Long bannerAssetId;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private String popupConfigJson;
        private String displayConfigJson;
        private List<AdminSpatialAssetLinkUpsertRequest> attachments = new ArrayList<>();
        private Integer sortOrder = 0;
        private String status = "draft";
        private String publishedAt;
    }
}
