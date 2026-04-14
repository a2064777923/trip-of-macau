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
@TableName("pois")
public class Poi extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    @TableField("sub_map_id")
    private Long subMapId;

    @TableField("storyline_id")
    private Long storylineId;

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

    @TableField("address_zh")
    private String addressZh;

    @TableField("address_en")
    private String addressEn;

    @TableField("address_zht")
    private String addressZht;

    @TableField("address_pt")
    private String addressPt;

    @TableField("source_coordinate_system")
    private String sourceCoordinateSystem;

    @TableField("source_latitude")
    private BigDecimal sourceLatitude;

    @TableField("source_longitude")
    private BigDecimal sourceLongitude;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @TableField("trigger_radius")
    private Integer triggerRadius;

    @TableField("manual_checkin_radius")
    private Integer manualCheckinRadius;

    @TableField("stay_seconds")
    private Integer staySeconds;

    @TableField("category_code")
    private String categoryCode;

    private String difficulty;

    @TableField("district_zh")
    private String districtZh;

    @TableField("district_en")
    private String districtEn;

    @TableField("district_zht")
    private String districtZht;

    @TableField("district_pt")
    private String districtPt;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("map_icon_asset_id")
    private Long mapIconAssetId;

    @TableField("audio_asset_id")
    private Long audioAssetId;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("intro_title_zh")
    private String introTitleZh;

    @TableField("intro_title_en")
    private String introTitleEn;

    @TableField("intro_title_zht")
    private String introTitleZht;

    @TableField("intro_title_pt")
    private String introTitlePt;

    @TableField("intro_summary_zh")
    private String introSummaryZh;

    @TableField("intro_summary_en")
    private String introSummaryEn;

    @TableField("intro_summary_zht")
    private String introSummaryZht;

    @TableField("intro_summary_pt")
    private String introSummaryPt;

    @TableField("popup_config_json")
    private String popupConfigJson;

    @TableField("display_config_json")
    private String displayConfigJson;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
