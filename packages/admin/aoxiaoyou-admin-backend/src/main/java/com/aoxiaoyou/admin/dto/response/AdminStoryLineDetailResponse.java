package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminStoryLineDetailResponse {
    private Long storylineId;
    private Long cityId;
    private String cityName;
    private String code;
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
    private Integer totalChapters;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
