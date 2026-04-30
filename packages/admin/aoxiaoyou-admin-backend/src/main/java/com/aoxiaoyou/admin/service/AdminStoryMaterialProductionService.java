package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialProductionRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialProductionResponse;

import java.util.List;

public interface AdminStoryMaterialProductionService {

    AdminStoryMaterialProductionResponse.PreflightResponse preflightPackage(
            Long packageId,
            AdminStoryMaterialProductionRequest.PreflightRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    );

    AdminStoryMaterialProductionResponse.PackageItemVersionResponse importLocalAsset(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.LocalImportRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    );

    AdminStoryMaterialProductionResponse.PackageItemVersionResponse bindFinalizedCandidate(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.AiCandidateBindRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    );

    AdminStoryMaterialProductionResponse.PromotionResult promoteItemVersion(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.PromoteRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    );

    AdminStoryMaterialProductionResponse.RollbackResult rollbackItemVersion(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.RollbackRequest request,
            Long adminUserId,
            String adminUsername,
            List<String> roles
    );

    List<AdminStoryMaterialProductionResponse.PackageItemVersionResponse> listVersions(
            Long packageId,
            Long itemId,
            AdminStoryMaterialProductionRequest.VersionHistoryQuery query
    );
}
