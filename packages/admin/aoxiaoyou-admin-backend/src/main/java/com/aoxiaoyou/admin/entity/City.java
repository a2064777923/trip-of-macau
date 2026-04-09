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
@TableName("cities")
public class City extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("country_code")
    private String countryCode;

    @TableField("center_lat")
    private BigDecimal centerLat;

    @TableField("center_lng")
    private BigDecimal centerLng;

    @TableField("bounds_geojson")
    private String boundsGeojson;

    @TableField("default_zoom")
    private Integer defaultZoom;

    @TableField("unlock_type")
    private String unlockType;

    @TableField("unlock_condition")
    private String unlockCondition;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("banner_url")
    private String bannerUrl;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
