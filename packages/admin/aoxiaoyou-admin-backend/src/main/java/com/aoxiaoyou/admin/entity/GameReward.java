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
@TableName("game_rewards")
public class GameReward extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("legacy_source_type")
    private String legacySourceType;

    @TableField("legacy_source_id")
    private Long legacySourceId;

    @TableField("reward_type")
    private String rewardType;

    private String rarity;

    private Integer stackable;

    @TableField("max_owned")
    private Integer maxOwned;

    @TableField("can_equip")
    private Integer canEquip;

    @TableField("can_consume")
    private Integer canConsume;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("subtitle_zh")
    private String subtitleZh;

    @TableField("subtitle_en")
    private String subtitleEn;

    @TableField("subtitle_zht")
    private String subtitleZht;

    @TableField("subtitle_pt")
    private String subtitlePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("highlight_zh")
    private String highlightZh;

    @TableField("highlight_en")
    private String highlightEn;

    @TableField("highlight_zht")
    private String highlightZht;

    @TableField("highlight_pt")
    private String highlightPt;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("icon_asset_id")
    private Long iconAssetId;

    @TableField("animation_asset_id")
    private Long animationAssetId;

    @TableField("reward_config_json")
    private String rewardConfigJson;

    @TableField("presentation_id")
    private Long presentationId;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("publish_start_at")
    private LocalDateTime publishStartAt;

    @TableField("publish_end_at")
    private LocalDateTime publishEndAt;
}
