package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BadgeResponse {
    private Long id;
    private String badgeCode;
    private String nameZh;
    private String badgeType;
    private String rarity;
    private Integer isHidden;
    private String iconUrl;
    private String imageUrl;
    private String status;
}
