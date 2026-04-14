package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminRuntimeSettingResponse {
    private Long id;
    private String settingGroup;
    private String settingKey;
    private String localeCode;
    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String valueJson;
    private String valueText;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private Long assetId;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
}
