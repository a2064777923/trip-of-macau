package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminIndoorFloorResponse {
    private Long id;
    private Long buildingId;
    private Long indoorMapId;
    private String floorCode;
    private Integer floorNumber;
    private String floorNameZh;
    private String floorNameEn;
    private String floorNameZht;
    private String floorNamePt;
    private String descriptionZh;
    private String descriptionEn;
    private String descriptionZht;
    private String descriptionPt;
    private Long coverAssetId;
    private Long floorPlanAssetId;
    private String floorPlanUrl;
    private String tileSourceType;
    private Long tileSourceAssetId;
    private String tileSourceFilename;
    private String tilePreviewImageUrl;
    private String tileRootUrl;
    private String tileManifestJson;
    private String tileZoomDerivationJson;
    private Integer imageWidthPx;
    private Integer imageHeightPx;
    private Integer tileSizePx;
    private Integer gridCols;
    private Integer gridRows;
    private Integer tileLevelCount;
    private Integer tileEntryCount;
    private String importStatus;
    private String importNote;
    private LocalDateTime importedAt;
    private BigDecimal altitudeMeters;
    private BigDecimal areaSqm;
    private BigDecimal zoomMin;
    private BigDecimal zoomMax;
    private BigDecimal defaultZoom;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<AdminSpatialAssetLinkResponse> attachments;
    private List<Long> attachmentAssetIds;
    private Integer markerCount;
    private Integer sortOrder;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AdminIndoorMarkerResponse> markers;
}
