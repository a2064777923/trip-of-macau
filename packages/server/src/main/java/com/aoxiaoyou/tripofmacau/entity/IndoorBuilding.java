package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("buildings")
public class IndoorBuilding {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    @TableField("city_code")
    private String cityCode;

    @TableField("sub_map_id")
    private Long subMapId;

    @TableField("binding_mode")
    private String bindingMode;

    @TableField("building_code")
    private String buildingCode;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

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

    private BigDecimal lat;

    private BigDecimal lng;

    @TableField("total_floors")
    private Integer totalFloors;

    @TableField("basement_floors")
    private Integer basementFloors;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("popup_config_json")
    private String popupConfigJson;

    @TableField("display_config_json")
    private String displayConfigJson;

    @TableField("poi_id")
    private Long poiId;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
