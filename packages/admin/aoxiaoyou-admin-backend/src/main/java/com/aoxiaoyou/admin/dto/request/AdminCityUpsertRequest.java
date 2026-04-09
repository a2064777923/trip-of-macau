package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class AdminCityUpsertRequest {
    private Upsert upsert = new Upsert();

    @Data
    public static class Upsert {
        private String code;
        private String nameZh;
        private String nameEn;
        private String nameZht;
        private String countryCode = "MO";
        private Double centerLat;
        private Double centerLng;
        private Integer defaultZoom = 14;
        private String unlockType = "auto";
        private String coverImageUrl;
        private String bannerUrl;
        private String descriptionZh;
        private Integer sortOrder;
    }
}
