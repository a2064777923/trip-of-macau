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
@TableName("sub_maps")
public class SubMap extends BaseEntity {

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

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("source_coordinate_system")
    private String sourceCoordinateSystem;

    @TableField("source_center_lat")
    private BigDecimal sourceCenterLat;

    @TableField("source_center_lng")
    private BigDecimal sourceCenterLng;

    @TableField("center_lat")
    private BigDecimal centerLat;

    @TableField("center_lng")
    private BigDecimal centerLng;

    @TableField("bounds_json")
    private String boundsJson;

    @TableField("popup_config_json")
    private String popupConfigJson;

    @TableField("display_config_json")
    private String displayConfigJson;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
