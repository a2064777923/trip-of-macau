package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminNotificationUpsertRequest {

    @NotBlank(message = "code is required")
    private String code;

    private String titleZh;
    private String titleEn;
    private String titleZht;
    private String titlePt;
    private String contentZh;
    private String contentEn;
    private String contentZht;
    private String contentPt;
    private String notificationType;
    private String targetScope;
    private Long coverAssetId;
    private String actionUrl;
    private String status;
    private Integer sortOrder;
    private String publishStartAt;
    private String publishEndAt;
}
