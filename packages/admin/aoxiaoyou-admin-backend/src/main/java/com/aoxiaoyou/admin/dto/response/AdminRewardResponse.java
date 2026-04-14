package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminRewardResponse {
    private Long id;
    private String code;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String subtitleZh;
    private String subtitleEn;
    private String subtitleZht;
    private String subtitlePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String highlightZh;
    private String highlightEn;
    private String highlightZht;
    private String highlightPt;
    private Integer stampCost;
    private Integer inventoryTotal;
    private Integer inventoryRedeemed;
    private Integer inventoryRemaining;
    private Long coverAssetId;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private LocalDateTime createdAt;
}
