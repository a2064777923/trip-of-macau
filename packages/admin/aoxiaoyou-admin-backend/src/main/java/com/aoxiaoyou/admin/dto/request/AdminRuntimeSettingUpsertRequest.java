package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRuntimeSettingUpsertRequest {

    @NotBlank(message = "settingGroup is required")
    private String settingGroup;

    @NotBlank(message = "settingKey is required")
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
    private String publishedAt;
}
