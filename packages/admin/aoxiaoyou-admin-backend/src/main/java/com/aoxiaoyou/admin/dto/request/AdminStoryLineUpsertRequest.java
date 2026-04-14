package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminStoryLineUpsertRequest {

    @Data
    public static class Upsert {
        private Long cityId;

        @NotBlank(message = "code is required")
        private String code;

        @NotBlank(message = "nameZh is required")
        private String nameZh;

        private String nameEn;
        private String nameZht;
        private String namePt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private Integer estimatedMinutes;
        private String difficulty;
        private Long coverAssetId;
        private Long bannerAssetId;
        private String rewardBadgeZh;
        private String rewardBadgeEn;
        private String rewardBadgeZht;
        private String rewardBadgePt;
        private String status;
        private Integer sortOrder;
        private String publishedAt;
    }
}
