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
@TableName("storylines")
public class StoryLine extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    private String code;

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

    @TableField("estimated_minutes")
    private Integer estimatedMinutes;

    private String difficulty;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("banner_asset_id")
    private Long bannerAssetId;

    @TableField("reward_badge_zh")
    private String rewardBadgeZh;

    @TableField("reward_badge_en")
    private String rewardBadgeEn;

    @TableField("reward_badge_zht")
    private String rewardBadgeZht;

    @TableField("reward_badge_pt")
    private String rewardBadgePt;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
