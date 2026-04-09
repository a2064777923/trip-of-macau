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
@TableName("map_tile_configs")
public class MapTileConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("map_id")
    private String mapId;

    private String style;

    @TableField("cdn_base")
    private String cdnBase;

    @TableField("control_points_url")
    private String controlPointsUrl;

    @TableField("pois_url")
    private String poisUrl;

    @TableField("zoom_min")
    private Integer zoomMin;

    @TableField("zoom_max")
    private Integer zoomMax;

    @TableField("center_lat")
    private BigDecimal centerLat;

    @TableField("center_lng")
    private BigDecimal centerLng;

    @TableField("default_zoom")
    private Integer defaultZoom;

    private String status;

    private String version;
}
