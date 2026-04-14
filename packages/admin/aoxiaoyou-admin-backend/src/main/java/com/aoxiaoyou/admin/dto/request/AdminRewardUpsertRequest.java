package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminRewardUpsertRequest {

    @Data
    public static class Upsert {
        @NotBlank(message = "code is required")
        private String code;

        @NotBlank(message = "nameZh is required")
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
        private String highlightZh;
        private String highlightEn;
        private String highlightZht;
        private String highlightPt;
        private Integer stampCost;
        private Integer inventoryTotal;
        private Integer inventoryRedeemed;
        private Long coverAssetId;
        private String status;
        private Integer sortOrder;
        private String publishStartAt;
        private String publishEndAt;
    }
}
