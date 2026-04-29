package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminBuildingUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorFloorUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerCsvConfirmRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminIndoorBuildingDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorFloorResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvImportResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvPreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorNodeResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleValidationResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorTilePreviewResponse;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminIndoorService {
    PageResponse<BuildingResponse> pageBuildings(long pageNum, long pageSize, String keyword, Long cityId, Long subMapId, Long poiId, String status);
    AdminIndoorBuildingDetailResponse getBuildingDetail(Long id);
    AdminIndoorBuildingDetailResponse createBuilding(AdminBuildingUpsertRequest request);
    AdminIndoorBuildingDetailResponse updateBuilding(Long id, AdminBuildingUpsertRequest request);
    PageResponse<AdminIndoorFloorResponse> pageFloors(Long buildingId, long pageNum, long pageSize, String status);
    AdminIndoorFloorResponse getFloorDetail(Long floorId);
    AdminIndoorFloorResponse createFloor(Long buildingId, AdminIndoorFloorUpsertRequest request);
    AdminIndoorFloorResponse updateFloor(Long floorId, AdminIndoorFloorUpsertRequest request);
    void deleteFloor(Long floorId);
    AdminIndoorTilePreviewResponse previewFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx);
    AdminIndoorFloorResponse importFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId);
    AdminIndoorFloorResponse importFloorPlanImage(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId);
    List<AdminIndoorNodeResponse> listFloorNodes(Long floorId, String status);
    AdminIndoorNodeResponse createNode(Long floorId, AdminIndoorNodeUpsertRequest request);
    AdminIndoorNodeResponse updateNode(Long nodeId, AdminIndoorNodeUpsertRequest request);
    void deleteNode(Long nodeId);
    AdminIndoorRuleValidationResponse validateRuleGraph(Long floorId, Long nodeId, AdminIndoorNodeUpsertRequest request);
    List<AdminIndoorMarkerResponse> listFloorMarkers(Long floorId, String status);
    AdminIndoorMarkerResponse createMarker(Long floorId, AdminIndoorMarkerUpsertRequest request);
    AdminIndoorMarkerResponse updateMarker(Long markerId, AdminIndoorMarkerUpsertRequest request);
    void deleteMarker(Long markerId);
    AdminIndoorMarkerCsvPreviewResponse previewMarkerCsv(Long floorId, MultipartFile file);
    AdminIndoorMarkerCsvImportResponse confirmMarkerCsv(Long floorId, AdminIndoorMarkerCsvConfirmRequest request, Long adminUserId);
}
