package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.entity.UserProgressOperationAudit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminUserProgressRepairService {

    OperationPreview previewRecompute(RecomputePreviewRequest request);

    OperationResult confirmRecompute(RecomputeConfirmRequest request);

    OperationPreview previewRepair(RepairPreviewRequest request);

    OperationResult applyRepair(RepairApplyRequest request);

    List<UserProgressOperationAudit> listAudits(AuditQuery query);

    record OperatorContext(Long operatorId, String operatorName, String requestIp) {
    }

    record ScopeTarget(Long userId, String scopeType, Long scopeId, Long storylineId) {
    }

    record RecomputePreviewRequest(OperatorContext operator, ScopeTarget target, String reason) {
    }

    record RecomputeConfirmRequest(
            OperatorContext operator,
            ScopeTarget target,
            String reason,
            String previewToken,
            String confirmText) {
    }

    record RepairPreviewRequest(
            OperatorContext operator,
            ScopeTarget target,
            String actionType,
            Long targetEventId,
            Long replacementElementId,
            String replacementElementCode,
            Long duplicateOfEventId,
            String reason,
            LocalDateTime windowStart,
            LocalDateTime windowEnd) {
    }

    record RepairApplyRequest(
            OperatorContext operator,
            ScopeTarget target,
            String actionType,
            Long targetEventId,
            Long replacementElementId,
            String replacementElementCode,
            Long duplicateOfEventId,
            String reason,
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            String previewToken,
            String confirmText) {
    }

    record AuditQuery(Long targetUserId, String actionType, Integer limit) {
    }

    record OperationPreview(
            String actionType,
            String requiredConfirmText,
            String confirmationToken,
            int affectedUserCount,
            int affectedScopeCount,
            int matchingEventCount,
            int availableElementCount,
            int completedElementCount,
            Map<String, Object> previewSummary) {
    }

    record OperationResult(
            String actionType,
            String status,
            int writtenStateRows,
            int mutatedEventRows,
            int deletedEventRows,
            Map<String, Object> resultSummary) {
    }
}
