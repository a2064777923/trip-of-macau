package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminNotificationResponse {
    private Long id;
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
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private LocalDateTime updatedAt;
}
