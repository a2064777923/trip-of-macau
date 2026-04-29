package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRecomputeConfirmRequest;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRecomputePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRepairRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressAuditEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressOperationPreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressOperationResultResponse;
import com.aoxiaoyou.admin.entity.UserProgressOperationAudit;
import com.aoxiaoyou.admin.service.AdminUserProgressRepairService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Tag(name = "Traveler Progress Operations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/users")
public class AdminTravelerProgressOpsController {

    private static final String EXTERNAL_RECOMPUTE_CONFIRM_TEXT = "RECOMPUTE";
    private static final String EXTERNAL_REPAIR_CONFIRM_TEXT = "REPAIR";
    private static final int MAX_AUDIT_PAGE_SIZE = 100;
    private static final int MAX_AUDIT_FETCH_LIMIT = 200;
    private static final Set<String> INTERNAL_REPAIR_ACTION_TYPES = Set.of(
            "LINK_ORPHAN_EVENT",
            "MARK_DUPLICATE_CLIENT_EVENT"
    );
    private static final TypeReference<Map<String, Object>> SUMMARY_TYPE = new TypeReference<>() {
    };

    private final AdminUserProgressRepairService adminUserProgressRepairService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Preview traveler progress recompute")
    @PostMapping("/{userId}/progress-ops/recompute-preview")
    public ApiResponse<AdminUserProgressOperationPreviewResponse> previewRecompute(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRecomputePreviewRequest request,
            HttpServletRequest httpRequest) {
        Long scopedUserId = resolveScopedUserId(userId, request.getUserId());
        AdminUserProgressRepairService.ScopeTarget target = buildScopeTarget(
                scopedUserId,
                request.getScopeType(),
                request.getScopeId(),
                request.getStorylineId()
        );
        AdminUserProgressRepairService.OperationPreview preview = adminUserProgressRepairService.previewRecompute(
                new AdminUserProgressRepairService.RecomputePreviewRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        request.getReason()
                )
        );
        return ApiResponse.success(toPreviewResponse(
                preview,
                scopedUserId,
                target.scopeType(),
                target.scopeId(),
                target.storylineId(),
                request.getFrom(),
                request.getTo(),
                EXTERNAL_RECOMPUTE_CONFIRM_TEXT
        ));
    }

    @Operation(summary = "Confirm traveler progress recompute")
    @PostMapping("/{userId}/progress-ops/recompute-confirm")
    public ApiResponse<AdminUserProgressOperationResultResponse> confirmRecompute(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRecomputeConfirmRequest request,
            HttpServletRequest httpRequest) {
        requireConfirmationText(request.getConfirmationText(), EXTERNAL_RECOMPUTE_CONFIRM_TEXT);
        Long scopedUserId = resolveScopedUserId(userId, request.getUserId());
        AdminUserProgressRepairService.ScopeTarget target = buildScopeTarget(
                scopedUserId,
                request.getScopeType(),
                request.getScopeId(),
                request.getStorylineId()
        );
        String previewToken = requirePreviewToken(request.getPreviewHash(), request.getConfirmationToken());
        AdminUserProgressRepairService.OperationPreview preview = adminUserProgressRepairService.previewRecompute(
                new AdminUserProgressRepairService.RecomputePreviewRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        request.getReason()
                )
        );
        AdminUserProgressRepairService.OperationResult result = adminUserProgressRepairService.confirmRecompute(
                new AdminUserProgressRepairService.RecomputeConfirmRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        request.getReason(),
                        previewToken,
                        preview.requiredConfirmText()
                )
        );
        return ApiResponse.success(toResultResponse(
                result,
                scopedUserId,
                target.scopeType(),
                target.scopeId(),
                target.storylineId(),
                request.getFrom(),
                request.getTo(),
                EXTERNAL_RECOMPUTE_CONFIRM_TEXT,
                previewToken
        ));
    }

    @Operation(summary = "Preview traveler progress repair")
    @PostMapping("/{userId}/progress-ops/repair-preview")
    public ApiResponse<AdminUserProgressOperationPreviewResponse> previewRepair(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRepairRequest request,
            HttpServletRequest httpRequest) {
        Long scopedUserId = resolveScopedUserId(userId, request.getUserId());
        AdminUserProgressRepairService.ScopeTarget target = buildScopeTarget(
                scopedUserId,
                request.getScopeType(),
                request.getScopeId(),
                request.getStorylineId()
        );
        AdminUserProgressRepairService.OperationPreview preview = adminUserProgressRepairService.previewRepair(
                new AdminUserProgressRepairService.RepairPreviewRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        request.getActionType(),
                        request.getTargetEventId(),
                        request.getReplacementElementId(),
                        request.getReplacementElementCode(),
                        request.getDuplicateOfEventId(),
                        request.getReason(),
                        request.getFrom(),
                        request.getTo()
                )
        );
        return ApiResponse.success(toPreviewResponse(
                preview,
                scopedUserId,
                target.scopeType(),
                target.scopeId(),
                target.storylineId(),
                request.getFrom(),
                request.getTo(),
                EXTERNAL_REPAIR_CONFIRM_TEXT
        ));
    }

    @Operation(summary = "Apply traveler progress repair")
    @PostMapping("/{userId}/progress-ops/repair-apply")
    public ApiResponse<AdminUserProgressOperationResultResponse> applyRepair(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRepairRequest request,
            HttpServletRequest httpRequest) {
        requireConfirmationText(request.getConfirmationText(), EXTERNAL_REPAIR_CONFIRM_TEXT);
        Long scopedUserId = resolveScopedUserId(userId, request.getUserId());
        AdminUserProgressRepairService.ScopeTarget target = buildScopeTarget(
                scopedUserId,
                request.getScopeType(),
                request.getScopeId(),
                request.getStorylineId()
        );
        String previewToken = requirePreviewToken(request.getPreviewHash(), request.getConfirmationToken());
        AdminUserProgressRepairService.OperationPreview preview = adminUserProgressRepairService.previewRepair(
                new AdminUserProgressRepairService.RepairPreviewRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        request.getActionType(),
                        request.getTargetEventId(),
                        request.getReplacementElementId(),
                        request.getReplacementElementCode(),
                        request.getDuplicateOfEventId(),
                        request.getReason(),
                        request.getFrom(),
                        request.getTo()
                )
        );
        AdminUserProgressRepairService.OperationResult result = adminUserProgressRepairService.applyRepair(
                new AdminUserProgressRepairService.RepairApplyRequest(
                        buildOperatorContext(httpRequest),
                        target,
                        preview.actionType(),
                        request.getTargetEventId(),
                        request.getReplacementElementId(),
                        request.getReplacementElementCode(),
                        request.getDuplicateOfEventId(),
                        request.getReason(),
                        request.getFrom(),
                        request.getTo(),
                        previewToken,
                        preview.requiredConfirmText()
                )
        );
        return ApiResponse.success(toResultResponse(
                result,
                scopedUserId,
                target.scopeType(),
                target.scopeId(),
                target.storylineId(),
                request.getFrom(),
                request.getTo(),
                EXTERNAL_REPAIR_CONFIRM_TEXT,
                previewToken
        ));
    }

    @Operation(summary = "List traveler progress operation audits")
    @GetMapping("/{userId}/progress-ops/audits")
    public ApiResponse<PageResponse<AdminUserProgressAuditEntryResponse>> listAudits(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) List<String> actionTypes,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        long safePageNum = Math.max(1, pageNum);
        long safePageSize = Math.max(1, Math.min(pageSize, MAX_AUDIT_PAGE_SIZE));
        Set<String> normalizedActionTypes = normalizeAuditActionTypes(actionTypes);
        int fetchLimit = computeAuditFetchLimit(safePageNum, safePageSize, normalizedActionTypes.size());

        List<UserProgressOperationAudit> audits = new ArrayList<>();
        if (normalizedActionTypes.isEmpty()) {
            audits.addAll(adminUserProgressRepairService.listAudits(
                    new AdminUserProgressRepairService.AuditQuery(userId, null, fetchLimit)
            ));
        } else {
            for (String actionType : normalizedActionTypes) {
                audits.addAll(adminUserProgressRepairService.listAudits(
                        new AdminUserProgressRepairService.AuditQuery(userId, actionType, fetchLimit)
                ));
            }
        }

        List<UserProgressOperationAudit> filteredAudits = deduplicateAudits(audits).stream()
                .filter(audit -> matchesScopeType(audit, scopeType))
                .filter(audit -> matchesFrom(audit, from))
                .filter(audit -> matchesTo(audit, to))
                .sorted(Comparator.comparing(
                        UserProgressOperationAudit::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();

        long total = filteredAudits.size();
        long totalPages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;
        int startIndex = (int) Math.min((safePageNum - 1) * safePageSize, total);
        int endIndex = (int) Math.min(startIndex + safePageSize, total);
        List<AdminUserProgressAuditEntryResponse> pageItems = filteredAudits.subList(startIndex, endIndex).stream()
                .map(this::toAuditEntryResponse)
                .toList();

        return ApiResponse.success(PageResponse.<AdminUserProgressAuditEntryResponse>builder()
                .pageNum(safePageNum)
                .pageSize(safePageSize)
                .total(total)
                .totalPages(totalPages)
                .list(pageItems)
                .build());
    }

    private AdminUserProgressRepairService.OperatorContext buildOperatorContext(HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("adminUserId");
        String operatorName = (String) request.getAttribute("adminUsername");
        return new AdminUserProgressRepairService.OperatorContext(
                operatorId,
                defaultText(operatorName),
                getClientIp(request)
        );
    }

    private AdminUserProgressRepairService.ScopeTarget buildScopeTarget(
            Long userId,
            String scopeType,
            Long scopeId,
            Long storylineId) {
        return new AdminUserProgressRepairService.ScopeTarget(
                userId,
                StringUtils.hasText(scopeType) ? scopeType.trim() : "global",
                scopeId,
                storylineId
        );
    }

    private AdminUserProgressOperationPreviewResponse toPreviewResponse(
            AdminUserProgressRepairService.OperationPreview preview,
            Long userId,
            String scopeType,
            Long scopeId,
            Long storylineId,
            LocalDateTime from,
            LocalDateTime to,
            String confirmationText) {
        return AdminUserProgressOperationPreviewResponse.builder()
                .userId(userId)
                .scopeType(scopeType)
                .scopeId(scopeId)
                .storylineId(storylineId)
                .from(from)
                .to(to)
                .actionType(preview.actionType())
                .confirmationText(confirmationText)
                .previewHash(preview.confirmationToken())
                .confirmationToken(preview.confirmationToken())
                .affectedUserCount(preview.affectedUserCount())
                .affectedScopeCount(preview.affectedScopeCount())
                .matchingEventCount(preview.matchingEventCount())
                .availableElementCount(preview.availableElementCount())
                .completedElementCount(preview.completedElementCount())
                .previewSummary(preview.previewSummary())
                .build();
    }

    private AdminUserProgressOperationResultResponse toResultResponse(
            AdminUserProgressRepairService.OperationResult result,
            Long userId,
            String scopeType,
            Long scopeId,
            Long storylineId,
            LocalDateTime from,
            LocalDateTime to,
            String confirmationText,
            String previewToken) {
        return AdminUserProgressOperationResultResponse.builder()
                .userId(userId)
                .scopeType(scopeType)
                .scopeId(scopeId)
                .storylineId(storylineId)
                .from(from)
                .to(to)
                .actionType(result.actionType())
                .confirmationText(confirmationText)
                .previewHash(previewToken)
                .confirmationToken(previewToken)
                .status(result.status())
                .writtenStateRows(result.writtenStateRows())
                .mutatedEventRows(result.mutatedEventRows())
                .deletedEventRows(result.deletedEventRows())
                .resultSummary(result.resultSummary())
                .build();
    }

    private AdminUserProgressAuditEntryResponse toAuditEntryResponse(UserProgressOperationAudit audit) {
        return AdminUserProgressAuditEntryResponse.builder()
                .id(audit.getId())
                .userId(audit.getTargetUserId())
                .scopeType(audit.getScopeType())
                .scopeId(audit.getScopeId())
                .storylineId(audit.getStorylineId())
                .actionType(audit.getActionType())
                .operatorId(audit.getOperatorId())
                .operatorName(audit.getOperatorName())
                .reason(audit.getReason())
                .requestIp(audit.getRequestIp())
                .previewSummary(parseSummary(audit.getPreviewSummaryJson()))
                .resultSummary(parseSummary(audit.getResultSummaryJson()))
                .timestamp(audit.getCreatedAt())
                .build();
    }

    private Map<String, Object> parseSummary(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, SUMMARY_TYPE);
        } catch (JsonProcessingException ex) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("raw", json);
            return fallback;
        }
    }

    private Set<String> normalizeAuditActionTypes(List<String> actionTypes) {
        if (actionTypes == null || actionTypes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String actionType : actionTypes) {
            if (!StringUtils.hasText(actionType)) {
                continue;
            }
            for (String token : actionType.split(",")) {
                String normalizedToken = token.trim().toUpperCase(Locale.ROOT);
                if (!StringUtils.hasText(normalizedToken)) {
                    continue;
                }
                if (Objects.equals(normalizedToken, EXTERNAL_RECOMPUTE_CONFIRM_TEXT)) {
                    normalized.add("RECOMPUTE_SCOPE");
                } else if (Objects.equals(normalizedToken, EXTERNAL_REPAIR_CONFIRM_TEXT)) {
                    normalized.addAll(INTERNAL_REPAIR_ACTION_TYPES);
                } else {
                    normalized.add(normalizedToken);
                }
            }
        }
        return normalized;
    }

    private List<UserProgressOperationAudit> deduplicateAudits(List<UserProgressOperationAudit> audits) {
        Map<Long, UserProgressOperationAudit> deduplicated = new LinkedHashMap<>();
        for (UserProgressOperationAudit audit : audits) {
            if (audit.getId() != null) {
                deduplicated.putIfAbsent(audit.getId(), audit);
            }
        }
        return new ArrayList<>(deduplicated.values());
    }

    private boolean matchesScopeType(UserProgressOperationAudit audit, String scopeType) {
        if (!StringUtils.hasText(scopeType)) {
            return true;
        }
        return Objects.equals(defaultText(audit.getScopeType()).toLowerCase(Locale.ROOT), scopeType.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesFrom(UserProgressOperationAudit audit, LocalDateTime from) {
        return from == null || (audit.getCreatedAt() != null && !audit.getCreatedAt().isBefore(from));
    }

    private boolean matchesTo(UserProgressOperationAudit audit, LocalDateTime to) {
        return to == null || (audit.getCreatedAt() != null && !audit.getCreatedAt().isAfter(to));
    }

    private int computeAuditFetchLimit(long pageNum, long pageSize, int actionTypeCount) {
        long multiplier = Math.max(1, actionTypeCount);
        long desired = Math.max(pageSize * multiplier * pageNum, pageSize * 3);
        return (int) Math.min(MAX_AUDIT_FETCH_LIMIT, Math.max(pageSize, desired));
    }

    private Long resolveScopedUserId(Long pathUserId, Long requestUserId) {
        if (requestUserId != null && !Objects.equals(pathUserId, requestUserId)) {
            throw new BusinessException(4004, "Path userId must match request userId");
        }
        return pathUserId;
    }

    private void requireConfirmationText(String actual, String expected) {
        if (!StringUtils.hasText(actual) || !Objects.equals(actual.trim().toUpperCase(Locale.ROOT), expected)) {
            throw new BusinessException(4004, "Explicit confirmation text is required");
        }
    }

    private String requirePreviewToken(String previewHash, String confirmationToken) {
        if (StringUtils.hasText(previewHash)) {
            return previewHash.trim();
        }
        if (StringUtils.hasText(confirmationToken)) {
            return confirmationToken.trim();
        }
        throw new BusinessException(4004, "Preview hash is required");
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return defaultText(ip);
    }
}
