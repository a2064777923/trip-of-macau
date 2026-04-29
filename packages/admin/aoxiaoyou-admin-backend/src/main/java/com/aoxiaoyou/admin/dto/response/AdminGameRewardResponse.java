package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminGameRewardResponse {
    private Long id;
    private String code;
    private String rewardType;
    private String rarity;
    private Integer stackable;
    private Integer maxOwned;
    private Integer canEquip;
    private Integer canConsume;
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
    private Long coverAssetId;
    private Long iconAssetId;
    private Long animationAssetId;
    private String rewardConfigJson;
    private Long presentationId;
    private AdminRewardPresentationSummaryResponse presentation;
    private List<Long> ruleIds;
    private List<AdminRewardRuleLinkResponse> linkedRules;
    private List<Long> storylineBindings;
    private List<Long> cityBindings;
    private List<Long> subMapBindings;
    private List<Long> indoorBuildingBindings;
    private List<Long> indoorFloorBindings;
    private List<Long> attachmentAssetIds;
    private String status;
    private Integer sortOrder;
    private LocalDateTime publishStartAt;
    private LocalDateTime publishEndAt;
    private LocalDateTime createdAt;
}
