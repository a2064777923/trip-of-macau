package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialPackageRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialPackageResponse;

public interface AdminStoryMaterialPackageService {

    PageResponse<AdminStoryMaterialPackageResponse.PackageSummary> page(AdminStoryMaterialPackageRequest.PackageQuery query);

    AdminStoryMaterialPackageResponse.PackageDetail detail(Long packageId);

    AdminStoryMaterialPackageResponse.PackageDetail create(AdminStoryMaterialPackageRequest.PackageUpsert request, Long adminUserId, String adminUsername);

    AdminStoryMaterialPackageResponse.PackageDetail update(Long packageId, AdminStoryMaterialPackageRequest.PackageUpsert request);

    AdminStoryMaterialPackageResponse.PackageItem addItem(Long packageId, AdminStoryMaterialPackageRequest.ItemUpsert request);

    AdminStoryMaterialPackageResponse.PackageItem updateItem(Long packageId, Long itemId, AdminStoryMaterialPackageRequest.ItemUpsert request);

    void deleteItem(Long packageId, Long itemId);

    void delete(Long packageId);
}
