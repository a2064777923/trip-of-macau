package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("indoor_nodes")
public class IndoorNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("building_id")
    private Long buildingId;

    @TableField("floor_id")
    private Long floorId;

    @TableField("marker_code")
    private String markerCode;

    @TableField("node_type")
    private String nodeType;

    @TableField("presentation_mode")
    private String presentationMode;

    @TableField("overlay_type")
    private String overlayType;

    @TableField("node_name_zh")
    private String nodeNameZh;

    @TableField("node_name_en")
    private String nodeNameEn;

    @TableField("node_name_zht")
    private String nodeNameZht;

    @TableField("node_name_pt")
    private String nodeNamePt;

    @TableField("position_x")
    private BigDecimal positionX;

    @TableField("position_y")
    private BigDecimal positionY;

    @TableField("relative_x")
    private BigDecimal relativeX;

    @TableField("relative_y")
    private BigDecimal relativeY;

    @TableField("related_poi_id")
    private Long relatedPoiId;

    @TableField("icon")
    private String icon;

    @TableField("icon_asset_id")
    private Long iconAssetId;

    @TableField("animation_asset_id")
    private Long animationAssetId;

    @TableField("linked_entity_type")
    private String linkedEntityType;

    @TableField("linked_entity_id")
    private Long linkedEntityId;

    @TableField("tags")
    private String tags;

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

    @TableField("overlay_geometry_json")
    private String overlayGeometryJson;

    @TableField("inherit_linked_entity_rules")
    private Boolean inheritLinkedEntityRules;

    @TableField("runtime_support_level")
    private String runtimeSupportLevel;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("import_batch_id")
    private Long importBatchId;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
