package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryMaterialQaRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryMaterialQaResponse;

import java.util.List;

public interface AdminStoryMaterialQaService {

    AdminStoryMaterialQaResponse.QaOverview overview(
            Long packageId,
            AdminStoryMaterialQaRequest.QaItemQuery query
    );

    PageResponse<AdminStoryMaterialQaResponse.QaItem> pageItems(
            Long packageId,
            AdminStoryMaterialQaRequest.QaItemQuery query
    );

    AdminStoryMaterialQaResponse.QaDetail detail(Long packageId, Long itemId);

    AdminStoryMaterialQaResponse.QaActionResult reject(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.QaActionRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    );

    AdminStoryMaterialQaResponse.QaActionResult approve(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.QaActionRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    );

    AdminStoryMaterialQaResponse.QaActionResult replace(
            Long packageId,
            Long itemId,
            AdminStoryMaterialQaRequest.ReplaceRequest request,
            Long adminId,
            String adminName,
            List<String> roles
    );

    AdminStoryMaterialQaResponse.ConsistencyReport runConsistencyCheck(
            Long packageId,
            AdminStoryMaterialQaRequest.ConsistencyCheckRequest request
    );
}
