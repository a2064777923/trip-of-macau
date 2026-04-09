package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminBuildingUpsertRequest;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;

public interface AdminIndoorService {
    PageResponse<BuildingResponse> pageBuildings(long pageNum, long pageSize, String cityCode);
    BuildingResponse createBuilding(AdminBuildingUpsertRequest request);
    BuildingResponse updateBuilding(Long id, AdminBuildingUpsertRequest request);
}
