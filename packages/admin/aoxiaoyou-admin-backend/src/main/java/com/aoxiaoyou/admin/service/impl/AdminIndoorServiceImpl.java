package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminBuildingUpsertRequest;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.service.AdminIndoorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminIndoorServiceImpl implements AdminIndoorService {

    private final BuildingMapper buildingMapper;

    @Override
    public PageResponse<BuildingResponse> pageBuildings(long pageNum, long pageSize, String cityCode) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(cityCode)) {
            wrapper.eq(Building::getCityCode, cityCode);
        }
        wrapper.orderByDesc(Building::getId);

        Page<Building> page = buildingMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<BuildingResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapBuilding).toList());
        return PageResponse.of(result);
    }

    @Override
    public BuildingResponse createBuilding(AdminBuildingUpsertRequest request) {
        validateRequest(request, true);

        Building building = new Building();
        applyRequest(building, request, true);
        buildingMapper.insert(building);
        return mapBuilding(building);
    }

    @Override
    public BuildingResponse updateBuilding(Long id, AdminBuildingUpsertRequest request) {
        Building existing = buildingMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(4040, "building not found");
        }

        validateRequest(request, false);
        applyRequest(existing, request, false);
        buildingMapper.updateById(existing);
        return mapBuilding(existing);
    }

    private void validateRequest(AdminBuildingUpsertRequest request, boolean create) {
        if (request == null) {
            throw new BusinessException(4001, "request body is required");
        }
        if (create && !StringUtils.hasText(request.getBuildingCode())) {
            throw new BusinessException(4001, "buildingCode is required");
        }
        if (create && !StringUtils.hasText(request.getNameZh())) {
            throw new BusinessException(4001, "nameZh is required");
        }
        if (create && !StringUtils.hasText(request.getCityCode())) {
            throw new BusinessException(4001, "cityCode is required");
        }
        if (request.getTotalFloors() != null && request.getTotalFloors() < 1) {
            throw new BusinessException(4001, "totalFloors must be greater than 0");
        }
        if (request.getBasementFloors() != null && request.getBasementFloors() < 0) {
            throw new BusinessException(4001, "basementFloors must be at least 0");
        }
    }

    private void applyRequest(Building building, AdminBuildingUpsertRequest request, boolean create) {
        if (StringUtils.hasText(request.getBuildingCode())) {
            building.setBuildingCode(request.getBuildingCode().trim());
        }
        if (StringUtils.hasText(request.getNameZh())) {
            building.setNameZh(request.getNameZh().trim());
        }
        if (request.getAddressZh() != null) {
            building.setAddressZh(request.getAddressZh().trim());
        }
        if (StringUtils.hasText(request.getCityCode())) {
            building.setCityCode(request.getCityCode().trim());
        } else if (create && !StringUtils.hasText(building.getCityCode())) {
            building.setCityCode("macau");
        }
        if (request.getLat() != null) {
            building.setLat(request.getLat());
        }
        if (request.getLng() != null) {
            building.setLng(request.getLng());
        }
        if (request.getTotalFloors() != null) {
            building.setTotalFloors(request.getTotalFloors());
        } else if (create && building.getTotalFloors() == null) {
            building.setTotalFloors(1);
        }
        if (request.getBasementFloors() != null) {
            building.setBasementFloors(request.getBasementFloors());
        } else if (create && building.getBasementFloors() == null) {
            building.setBasementFloors(0);
        }
        if (request.getCoverImageUrl() != null) {
            building.setCoverImageUrl(request.getCoverImageUrl().trim());
        }
        if (request.getDescriptionZh() != null) {
            building.setDescriptionZh(request.getDescriptionZh().trim());
        }
        if (request.getPoiId() != null) {
            building.setPoiId(request.getPoiId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            building.setStatus(request.getStatus().trim());
        } else if (create && !StringUtils.hasText(building.getStatus())) {
            building.setStatus("1");
        }
    }

    private BuildingResponse mapBuilding(Building building) {
        return BuildingResponse.builder()
                .id(building.getId())
                .buildingCode(building.getBuildingCode())
                .nameZh(building.getNameZh())
                .addressZh(building.getAddressZh())
                .cityCode(building.getCityCode())
                .lat(building.getLat())
                .lng(building.getLng())
                .totalFloors(building.getTotalFloors())
                .coverImageUrl(building.getCoverImageUrl())
                .status(building.getStatus())
                .build();
    }
}
