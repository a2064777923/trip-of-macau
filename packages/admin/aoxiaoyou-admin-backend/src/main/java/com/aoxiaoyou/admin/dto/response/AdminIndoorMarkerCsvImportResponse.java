package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminIndoorMarkerCsvImportResponse {
    private Long batchId;
    private Long floorId;
    private Integer totalRows;
    private Integer importedRows;
    private Integer skippedRows;
    private List<AdminIndoorMarkerResponse> markers;
}
