package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminActivityUpsertRequest {

    @Data
    public static class Upsert {
        @NotBlank(message = "code is required")
        private String code;

        private String activityType;

        @NotBlank(message = "titleZh is required")
        private String titleZh;

        private String titleEn;
        private String titleZht;
        private String titlePt;
        private String summaryZh;
        private String summaryEn;
        private String summaryZht;
        private String summaryPt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private String htmlZh;
        private String htmlEn;
        private String htmlZht;
        private String htmlPt;
        private String venueNameZh;
        private String venueNameEn;
        private String venueNameZht;
        private String venueNamePt;
        private String addressZh;
        private String addressEn;
        private String addressZht;
        private String addressPt;
        private String organizerName;
        private String organizerContact;
        private String organizerWebsite;
        private Integer signupCapacity;
        private BigDecimal signupFeeAmount;
        private String signupStartAt;
        private String signupEndAt;
        private String publishStartAt;
        private String publishEndAt;
        private Integer isPinned;
        private Long coverAssetId;
        private Long heroAssetId;
        private Integer participationCount;
        private String status;
        private Integer sortOrder;
        private List<Long> cityBindings = new ArrayList<>();
        private List<Long> subMapBindings = new ArrayList<>();
        private List<Long> storylineBindings = new ArrayList<>();
        private List<Long> attachmentAssetIds = new ArrayList<>();
    }
}
