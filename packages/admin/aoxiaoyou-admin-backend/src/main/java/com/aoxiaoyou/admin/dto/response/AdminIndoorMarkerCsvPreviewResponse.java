package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminIndoorMarkerCsvPreviewResponse {
    private Long floorId;
    private String sourceFilename;
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private List<Row> rows;

    @Data
    @Builder
    public static class Row {
        private Integer rowNumber;
        private String markerCode;
        private String nodeType;
        private String nodeNameZh;
        private String nodeNameEn;
        private String nodeNameZht;
        private String nodeNamePt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private BigDecimal relativeX;
        private BigDecimal relativeY;
        private Long relatedPoiId;
        private Long iconAssetId;
        private Long animationAssetId;
        private String linkedEntityType;
        private Long linkedEntityId;
        private String tagsJson;
        private String popupConfigJson;
        private String displayConfigJson;
        private String metadataJson;
        private Integer sortOrder;
        private String status;
        private String presentationMode;
        private String appearancePresetCode;
        private String triggerTemplateCode;
        private String effectTemplateCode;
        private String inheritMode;
        private boolean valid;
        private List<String> errors;
    }
}
