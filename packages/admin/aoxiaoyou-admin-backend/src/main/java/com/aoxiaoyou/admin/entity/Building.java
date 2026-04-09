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
@TableName("buildings")
public class Building extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_code")
    private String cityCode;

    @TableField("building_code")
    private String buildingCode;

    @TableField("name_zh")
    private String nameZh;

    @TableField("address_zh")
    private String addressZh;

    private BigDecimal lat;

    private BigDecimal lng;

    @TableField("total_floors")
    private Integer totalFloors;

    @TableField("basement_floors")
    private Integer basementFloors;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("poi_id")
    private Long poiId;

    private String status;
}
