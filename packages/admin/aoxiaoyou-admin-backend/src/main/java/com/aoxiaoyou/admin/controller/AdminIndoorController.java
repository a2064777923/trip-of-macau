package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
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
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleConflictResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleGovernanceDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleGovernanceItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleStatusUpdateResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleValidationResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorTilePreviewResponse;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;
import com.aoxiaoyou.admin.service.AdminIndoorService;
import com.aoxiaoyou.admin.service.impl.IndoorRuleGovernanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/map/indoor")
@RequiredArgsConstructor
public class AdminIndoorController {

    private final AdminIndoorService indoorService;
    private final IndoorRuleGovernanceService indoorRuleGovernanceService;

    @GetMapping("/buildings")
    public ApiResponse<PageResponse<BuildingResponse>> pageBuildings(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long subMapId,
            @RequestParam(required = false) Long poiId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(indoorService.pageBuildings(pageNum, pageSize, keyword, cityId, subMapId, poiId, status));
    }

    @GetMapping("/buildings/{id}")
    public ApiResponse<AdminIndoorBuildingDetailResponse> getBuildingDetail(@PathVariable Long id) {
        return ApiResponse.success(indoorService.getBuildingDetail(id));
    }

    @PostMapping("/buildings")
    public ApiResponse<AdminIndoorBuildingDetailResponse> createBuilding(@Valid @RequestBody AdminBuildingUpsertRequest request) {
        return ApiResponse.success(indoorService.createBuilding(request));
    }

    @PutMapping("/buildings/{id}")
    public ApiResponse<AdminIndoorBuildingDetailResponse> updateBuilding(@PathVariable Long id, @Valid @RequestBody AdminBuildingUpsertRequest request) {
        return ApiResponse.success(indoorService.updateBuilding(id, request));
    }

    @GetMapping("/buildings/{buildingId}/floors")
    public ApiResponse<PageResponse<AdminIndoorFloorResponse>> pageFloors(
            @PathVariable Long buildingId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(indoorService.pageFloors(buildingId, pageNum, pageSize, status));
    }

    @GetMapping("/floors/{floorId}")
    public ApiResponse<AdminIndoorFloorResponse> getFloorDetail(@PathVariable Long floorId) {
        return ApiResponse.success(indoorService.getFloorDetail(floorId));
    }

    @PostMapping("/buildings/{buildingId}/floors")
    public ApiResponse<AdminIndoorFloorResponse> createFloor(
            @PathVariable Long buildingId,
            @Valid @RequestBody AdminIndoorFloorUpsertRequest request) {
        return ApiResponse.success(indoorService.createFloor(buildingId, request));
    }

    @PutMapping("/floors/{floorId}")
    public ApiResponse<AdminIndoorFloorResponse> updateFloor(
            @PathVariable Long floorId,
            @Valid @RequestBody AdminIndoorFloorUpsertRequest request) {
        return ApiResponse.success(indoorService.updateFloor(floorId, request));
    }

    @DeleteMapping("/floors/{floorId}")
    public ApiResponse<Boolean> deleteFloor(@PathVariable Long floorId) {
        indoorService.deleteFloor(floorId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @PostMapping(value = "/floors/{floorId}/tile-import/zip-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminIndoorTilePreviewResponse> previewFloorTileZip(
            @PathVariable Long floorId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Integer tileSizePx) {
        return ApiResponse.success(indoorService.previewFloorTileZip(floorId, file, tileSizePx));
    }

    @PostMapping(value = "/floors/{floorId}/tile-import/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminIndoorFloorResponse> importFloorTileZip(
            @PathVariable Long floorId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Integer tileSizePx,
            jakarta.servlet.http.HttpServletRequest request) {
        return ApiResponse.success(indoorService.importFloorTileZip(
                floorId,
                file,
                tileSizePx,
                (Long) request.getAttribute("adminUserId")
        ));
    }

    @PostMapping(value = "/floors/{floorId}/tile-import/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminIndoorFloorResponse> importFloorImage(
            @PathVariable Long floorId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Integer tileSizePx,
            jakarta.servlet.http.HttpServletRequest request) {
        return ApiResponse.success(indoorService.importFloorPlanImage(
                floorId,
                file,
                tileSizePx,
                (Long) request.getAttribute("adminUserId")
        ));
    }

    @GetMapping("/floors/{floorId}/nodes")
    public ApiResponse<List<AdminIndoorNodeResponse>> listFloorNodes(
            @PathVariable Long floorId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(indoorService.listFloorNodes(floorId, status));
    }

    @PostMapping("/floors/{floorId}/nodes")
    public ApiResponse<AdminIndoorNodeResponse> createNode(
            @PathVariable Long floorId,
            @RequestBody AdminIndoorNodeUpsertRequest request) {
        return ApiResponse.success(indoorService.createNode(floorId, request));
    }

    @PutMapping("/nodes/{nodeId}")
    public ApiResponse<AdminIndoorNodeResponse> updateNode(
            @PathVariable Long nodeId,
            @RequestBody AdminIndoorNodeUpsertRequest request) {
        return ApiResponse.success(indoorService.updateNode(nodeId, request));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<Boolean> deleteNode(@PathVariable Long nodeId) {
        indoorService.deleteNode(nodeId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @PostMapping("/nodes/validate-rule-graph")
    public ApiResponse<AdminIndoorRuleValidationResponse> validateRuleGraph(
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long nodeId,
            @RequestBody AdminIndoorNodeUpsertRequest request) {
        return ApiResponse.success(indoorService.validateRuleGraph(floorId, nodeId, request));
    }

    @GetMapping("/rules/overview")
    public ApiResponse<List<AdminIndoorRuleGovernanceItemResponse>> listRuleOverview(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long relatedPoiId,
            @RequestParam(required = false) String linkedEntityType,
            @RequestParam(required = false) Long linkedEntityId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String runtimeSupportLevel,
            @RequestParam(required = false) Boolean conflictOnly,
            @RequestParam(required = false) Boolean enabledOnly) {
        return ApiResponse.success(indoorRuleGovernanceService.listOverview(
                keyword,
                cityId,
                buildingId,
                floorId,
                relatedPoiId,
                linkedEntityType,
                linkedEntityId,
                status,
                runtimeSupportLevel,
                conflictOnly,
                enabledOnly
        ));
    }

    @GetMapping("/rules/conflicts")
    public ApiResponse<List<AdminIndoorRuleConflictResponse>> listRuleConflicts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long relatedPoiId,
            @RequestParam(required = false) String linkedEntityType,
            @RequestParam(required = false) Long linkedEntityId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String runtimeSupportLevel,
            @RequestParam(required = false) Boolean conflictOnly,
            @RequestParam(required = false) Boolean enabledOnly) {
        return ApiResponse.success(indoorRuleGovernanceService.listConflicts(
                keyword,
                cityId,
                buildingId,
                floorId,
                relatedPoiId,
                linkedEntityType,
                linkedEntityId,
                status,
                runtimeSupportLevel,
                conflictOnly,
                enabledOnly
        ));
    }

    @GetMapping("/rules/behaviors/{behaviorId}")
    public ApiResponse<AdminIndoorRuleGovernanceDetailResponse> getRuleBehaviorDetail(@PathVariable Long behaviorId) {
        return ApiResponse.success(indoorRuleGovernanceService.getBehaviorDetail(behaviorId));
    }

    @PatchMapping("/rules/behaviors/{behaviorId}/status")
    public ApiResponse<AdminIndoorRuleStatusUpdateResponse> updateRuleBehaviorStatus(
            @PathVariable Long behaviorId,
            @RequestBody Map<String, String> request) {
        return ApiResponse.success(indoorRuleGovernanceService.updateBehaviorStatus(behaviorId, request.get("status")));
    }

    @GetMapping("/floors/{floorId}/markers")
    public ApiResponse<List<AdminIndoorMarkerResponse>> listFloorMarkers(
            @PathVariable Long floorId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(indoorService.listFloorMarkers(floorId, status));
    }

    @PostMapping("/floors/{floorId}/markers")
    public ApiResponse<AdminIndoorMarkerResponse> createMarker(
            @PathVariable Long floorId,
            @RequestBody AdminIndoorMarkerUpsertRequest request) {
        return ApiResponse.success(indoorService.createMarker(floorId, request));
    }

    @PutMapping("/markers/{markerId}")
    public ApiResponse<AdminIndoorMarkerResponse> updateMarker(
            @PathVariable Long markerId,
            @RequestBody AdminIndoorMarkerUpsertRequest request) {
        return ApiResponse.success(indoorService.updateMarker(markerId, request));
    }

    @DeleteMapping("/markers/{markerId}")
    public ApiResponse<Boolean> deleteMarker(@PathVariable Long markerId) {
        indoorService.deleteMarker(markerId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @PostMapping(value = "/floors/{floorId}/markers/csv-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminIndoorMarkerCsvPreviewResponse> previewMarkerCsv(
            @PathVariable Long floorId,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success(indoorService.previewMarkerCsv(floorId, file));
    }

    @PostMapping("/floors/{floorId}/markers/csv-confirm")
    public ApiResponse<AdminIndoorMarkerCsvImportResponse> confirmMarkerCsv(
            @PathVariable Long floorId,
            @RequestBody AdminIndoorMarkerCsvConfirmRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ApiResponse.success(indoorService.confirmMarkerCsv(
                floorId,
                request,
                (Long) httpRequest.getAttribute("adminUserId")
        ));
    }
}
