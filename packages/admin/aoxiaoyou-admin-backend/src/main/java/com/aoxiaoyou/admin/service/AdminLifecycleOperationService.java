package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminLifecycleOperationRequest;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleOperationResponse;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleTargetResponse;

import java.util.List;

public interface AdminLifecycleOperationService {

    List<AdminLifecycleTargetResponse.TargetType> listTargetTypes();

    List<AdminLifecycleTargetResponse.StatusOption> listStatuses();

    PageResponse<AdminLifecycleTargetResponse.TargetSummary> pageTargets(AdminLifecycleOperationRequest.TargetQuery query);

    AdminLifecycleOperationResponse.Preview preview(AdminLifecycleOperationRequest.Preview request);

    AdminLifecycleOperationResponse.Summary createOperation(AdminLifecycleOperationRequest.CreateOperation request, Long adminUserId, String adminUsername);

    AdminLifecycleOperationResponse.Summary applyOperation(Long operationId, AdminLifecycleOperationRequest.ApplyOperation request, Long adminUserId, String adminUsername);

    AdminLifecycleOperationResponse.Summary cancelOperation(Long operationId, AdminLifecycleOperationRequest.CancelOperation request, Long adminUserId, String adminUsername);

    List<AdminLifecycleOperationResponse.Summary> runDueOperations(Long adminUserId, String adminUsername);

    PageResponse<AdminLifecycleOperationResponse.Summary> pageOperations(long pageNum, long pageSize, String targetType, String action, String operationStatus, String keyword);

    AdminLifecycleOperationResponse.Detail getOperation(Long operationId);
}
