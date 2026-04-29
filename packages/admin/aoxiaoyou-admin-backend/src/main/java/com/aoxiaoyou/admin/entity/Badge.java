package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("badges")
public class Badge extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("badge_code")
    private String badgeCode;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("icon_asset_id")
    private Long iconAssetId;

    @TableField("animation_asset_id")
    private Long animationAssetId;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("badge_type")
    private String badgeType;

    private String rarity;

    @TableField("is_hidden")
    private Integer isHidden;

    @TableField("is_limited_time")
    private Integer isLimitedTime;

    @TableField("limited_start")
    private LocalDateTime limitedStart;

    @TableField("limited_end")
    private LocalDateTime limitedEnd;

    @TableField("image_url")
    private String imageUrl;

    @TableField("animation_unlock")
    private String animationUnlock;

    @TableField("popup_preset_code")
    private String popupPresetCode;

    @TableField("popup_config_json")
    private String popupConfigJson;

    @TableField("display_preset_code")
    private String displayPresetCode;

    @TableField("display_config_json")
    private String displayConfigJson;

    @TableField("trigger_preset_code")
    private String triggerPresetCode;

    @TableField("trigger_config_json")
    private String triggerConfigJson;

    @TableField("example_content_zh")
    private String exampleContentZh;

    @TableField("example_content_en")
    private String exampleContentEn;

    @TableField("example_content_zht")
    private String exampleContentZht;

    @TableField("example_content_pt")
    private String exampleContentPt;

    private String status;
}
