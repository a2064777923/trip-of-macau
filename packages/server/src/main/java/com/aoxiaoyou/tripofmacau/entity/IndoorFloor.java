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
@TableName("indoor_floors")
public class IndoorFloor {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("building_id")
    private Long buildingId;

    @TableField("indoor_map_id")
    private Long indoorMapId;

    @TableField("floor_code")
    private String floorCode;

    @TableField("floor_number")
    private Integer floorNumber;

    @TableField("floor_name_zh")
    private String floorNameZh;

    @TableField("floor_name_en")
    private String floorNameEn;

    @TableField("floor_name_zht")
    private String floorNameZht;

    @TableField("floor_name_pt")
    private String floorNamePt;

    @TableField("floor_plan_url")
    private String floorPlanUrl;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("floor_plan_asset_id")
    private Long floorPlanAssetId;

    @TableField("tile_source_type")
    private String tileSourceType;

    @TableField("tile_source_asset_id")
    private Long tileSourceAssetId;

    @TableField("tile_source_filename")
    private String tileSourceFilename;

    @TableField("tile_preview_image_url")
    private String tilePreviewImageUrl;

    @TableField("tile_root_url")
    private String tileRootUrl;

    @TableField("tile_manifest_json")
    private String tileManifestJson;

    @TableField("tile_zoom_derivation_json")
    private String tileZoomDerivationJson;

    @TableField("image_width_px")
    private Integer imageWidthPx;

    @TableField("image_height_px")
    private Integer imageHeightPx;

    @TableField("tile_size_px")
    private Integer tileSizePx;

    @TableField("grid_cols")
    private Integer gridCols;

    @TableField("grid_rows")
    private Integer gridRows;

    @TableField("tile_level_count")
    private Integer tileLevelCount;

    @TableField("tile_entry_count")
    private Integer tileEntryCount;

    @TableField("import_status")
    private String importStatus;

    @TableField("import_note")
    private String importNote;

    @TableField("imported_at")
    private LocalDateTime importedAt;

    @TableField("altitude_meters")
    private BigDecimal altitudeMeters;

    @TableField("area_sqm")
    private BigDecimal areaSqm;

    @TableField("zoom_min")
    private BigDecimal zoomMin;

    @TableField("zoom_max")
    private BigDecimal zoomMax;

    @TableField("default_zoom")
    private BigDecimal defaultZoom;

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
