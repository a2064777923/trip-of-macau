package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class IndoorFloorResponse {
    private Long id;
    private String floorCode;
    private Integer floorNumber;
    private String name;
    private String description;
    private String coverImageUrl;
    private String floorPlanUrl;
    private String tileSourceType;
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
    private BigDecimal altitudeMeters;
    private BigDecimal areaSqm;
    private BigDecimal zoomMin;
    private BigDecimal zoomMax;
    private BigDecimal defaultZoom;
    private String popupConfigJson;
    private String displayConfigJson;
    private List<IndoorMarkerResponse> markers;
}
