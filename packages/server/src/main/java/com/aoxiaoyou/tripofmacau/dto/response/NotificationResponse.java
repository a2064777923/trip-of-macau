package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String code;
    private String title;
    private String content;
    private String notificationType;
    private String targetScope;
    private String actionUrl;
    private String coverImageUrl;
    private Integer sortOrder;
    private LocalDateTime publishedAt;
}
