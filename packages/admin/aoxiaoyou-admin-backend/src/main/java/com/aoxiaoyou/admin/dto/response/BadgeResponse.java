package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BadgeResponse {
    private Long id;
    private String badgeCode;
    private String nameZh;
    private String nameEn;
    private String nameZht;
    private String namePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private String badgeType;
    private String rarity;
    private Integer isHidden;
    private Long coverAssetId;
    private Long iconAssetId;
    private Long animationAssetId;
    private String iconUrl;
    private String imageUrl;
    private String animationUnlock;
    private String popupPresetCode;
    private String popupConfigJson;
    private String displayPresetCode;
    private String displayConfigJson;
    private String triggerPresetCode;
    private String triggerConfigJson;
    private String exampleContentZh;
    private String exampleContentEn;
    private String exampleContentZht;
    private String exampleContentPt;
    private String status;
    private List<Long> storylineBindings;
    private List<Long> cityBindings;
    private List<Long> subMapBindings;
    private List<Long> indoorBuildingBindings;
    private List<Long> indoorFloorBindings;
    private List<Long> attachmentAssetIds;
}
