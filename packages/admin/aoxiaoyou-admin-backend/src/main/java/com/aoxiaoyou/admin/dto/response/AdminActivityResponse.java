package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminActivityResponse {

    private Long id;
    private String code;
    private String activityType;
    private String title;
    private String description;
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
    private LocalDateTime signupStartAt;
    private LocalDateTime signupEndAt;
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private Integer isPinned;
    private Long coverAssetId;
    private Long heroAssetId;
    private Integer participationCount;
    private String status;
    private Integer sortOrder;
    private List<Long> cityBindings;
    private List<Long> subMapBindings;
    private List<Long> storylineBindings;
    private List<Long> attachmentAssetIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
