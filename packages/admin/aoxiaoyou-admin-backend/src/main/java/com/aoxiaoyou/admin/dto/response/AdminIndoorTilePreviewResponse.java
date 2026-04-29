package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminIndoorTilePreviewResponse {
    private Long floorId;
    private String sourceType;
    private String sourceFilename;
    private Integer imageWidthPx;
    private Integer imageHeightPx;
    private Integer tileSizePx;
    private Integer gridCols;
    private Integer gridRows;
    private Integer tileLevelCount;
    private Integer tileEntryCount;
    private BigDecimal zoomMin;
    private BigDecimal defaultZoom;
    private BigDecimal zoomMax;
    private String derivationJson;
    private String manifestJson;
    private List<String> notes;
}
