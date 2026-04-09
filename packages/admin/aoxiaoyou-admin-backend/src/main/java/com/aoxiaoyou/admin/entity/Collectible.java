package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("collectible_type")
    private String collectibleType;

    private String rarity;

    @TableField("image_url")
    private String imageUrl;

    @TableField("animation_url")
    private String animationUrl;

    @TableField("series_id")
    private Long seriesId;

    @TableField("acquisition_source")
    private String acquisitionSource;

    @TableField("bind_condition")
    private String bindCondition;

    @TableField("display_rule")
    private String displayRule;

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

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
