package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscoverCardResponse {

    private String id;
    private String title;
    private String subtitle;
    private String description;
    private String tag;
    private String icon;
    private String type;
    private String district;
    private String actionText;
    private String coverColor;
    private String actionUrl;
    private String sourceType;
    private Long sourceId;
}
