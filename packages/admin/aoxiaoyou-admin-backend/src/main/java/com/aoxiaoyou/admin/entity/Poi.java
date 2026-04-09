package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pois")
public class Poi extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    private String subtitle;

    @TableField("region_code")
    private String regionCode;

    @TableField("poi_type")
    private String poiType;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String address;

    @TableField("category_id")
    private Long categoryId;

    @TableField("trigger_radius")
    private Integer triggerRadius;

    @TableField("check_in_method")
    private String checkInMethod;

    private String importance;

    @TableField("story_line_id")
    private Long storyLineId;

    @TableField("stamp_type")
    private String stampType;

    private String description;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("image_urls")
    private String imageUrls;

    @TableField("audio_guide_url")
    private String audioGuideUrl;

    @TableField("video_url")
    private String videoUrl;

    @TableField("ar_content_url")
    private String arContentUrl;

    private String tags;

    private String difficulty;

    @TableField("open_time")
    private String openTime;

    @TableField("suggested_visit_minutes")
    private Integer suggestedVisitMinutes;

    private String status;

    @TableField("check_in_count")
    private Long checkInCount;

    @TableField("favorite_count")
    private Long favoriteCount;
}

