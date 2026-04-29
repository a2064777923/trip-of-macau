package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collectibles")
public class Collectible extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("collectible_code")
    private String collectibleCode;

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

    @TableField("collectible_type")
    private String collectibleType;

    private String rarity;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("icon_asset_id")
    private Long iconAssetId;

    @TableField("animation_asset_id")
    private Long animationAssetId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("animation_url")
    private String animationUrl;

    @TableField("acquisition_source")
    private String acquisitionSource;

    @TableField("series_id")
    private Long seriesId;

    @TableField("bind_condition")
    private String bindCondition;

    @TableField("display_rule")
    private String displayRule;

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

    @TableField("is_repeatable")
    private Integer isRepeatable;

    @TableField("is_limited")
    private Integer isLimited;

    @TableField("limited_start")
    private LocalDateTime limitedStart;

    @TableField("limited_end")
    private LocalDateTime limitedEnd;

    @TableField("cross_city")
    private Integer crossCity;

    @TableField("max_ownership")
    private Integer maxOwnership;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;
}
