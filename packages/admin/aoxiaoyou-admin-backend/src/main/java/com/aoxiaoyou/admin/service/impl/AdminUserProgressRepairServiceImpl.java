package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.UserProgressOperationAudit;
import com.aoxiaoyou.admin.mapper.AdminUserProgressReadMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationEventAdminMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationStateAdminMapper;
import com.aoxiaoyou.admin.mapper.UserProgressOperationAuditMapper;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.AdminUserProgressRepairService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserProgressRepairServiceImpl implements AdminUserProgressRepairService {

    public static final String RECOMPUTE_SCOPE = "RECOMPUTE_SCOPE";
    public static final String LINK_ORPHAN_EVENT = "LINK_ORPHAN_EVENT";
    public static final String MARK_DUPLICATE_CLIENT_EVENT = "MARK_DUPLICATE_CLIENT_EVENT";

    private static final Set<String> ALLOWED_SCOPE_TYPES = Set.of(
            "global",
            "city",
            "sub_map",
            "poi",
            "indoor_building",
            "indoor_floor",
            "storyline",
            "story_chapter",
            "task",
            "collectible",
            "reward",
            "media"
    );

    private final AdminUserProgressCalculatorService calculatorService;
    private final AdminUserProgressReadMapper readMapper;
    private final UserExplorationStateAdminMapper stateMapper;
    private final UserExplorationEventAdminMapper eventMapper;
    private final UserProgressOperationAuditMapper auditMapper;
    private final SysOperationLogMapper sysOperationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public OperationPreview previewRecompute(RecomputePreviewRequest request) {
        ScopeTarget target = requireScopedTarget(request == null ? null : request.target());
        PreviewComputation preview = buildRecomputePreview(target, request.reason());
        return preview.response();
    }

    @Override
    public OperationResult confirmRecompute(RecomputeConfirmRequest request) {
        ScopeTarget target = requireScopedTarget(request == null ? null : request.target());
        PreviewComputation preview = buildRecomputePreview(target, request.reason());
        validateConfirmation(preview.response(), request.previewToken(), request.confirmText());

        String scopeType = normalizeScopeType(target.scopeType());
        AdminUserProgressSummaryResponse summary = calculatorService.calculateSummary(
                target.userId(),
                scopeType,
                target.scopeId(),
                false
        );
        LocalDateTime now = LocalDateTime.now();
        stateMapper.deleteScopeState(target.userId(), scopeType, target.scopeId());
        int writtenRows = stateMapper.upsertScopeState(new UserExplorationStateAdminMapper.ScopeStateUpsert(
                target.userId(),
                scopeType,
                target.scopeId(),
                summary.getCompletedWeight(),
                summary.getAvailableWeight(),
                summary.getProgressPercent(),
                now,
                now,
                now
        ));

        Map<String, Object> resultSummary = new LinkedHashMap<>();
        resultSummary.put("targetUserId", target.userId());
        resultSummary.put("scopeType", scopeType);
        resultSummary.put("scopeId", target.scopeId());
        resultSummary.put("storylineId", target.storylineId());
        resultSummary.put("completedWeight", summary.getCompletedWeight());
        resultSummary.put("availableWeight", summary.getAvailableWeight());
        resultSummary.put("progressPercent", summary.getProgressPercent());
        resultSummary.put("writtenStateRows", writtenRows);
        resultSummary.put("computedAt", now);

        writeAudit(
                request.operator(),
                target,
                RECOMPUTE_SCOPE,
                preview.response(),
                resultSummary,
                request.reason(),
                request.previewToken()
        );
        writeSystemLog(request.operator(), RECOMPUTE_SCOPE, request.reason(), preview.previewSummaryJson(), toJson(resultSummary));

        return new OperationResult(RECOMPUTE_SCOPE, "confirmed", writtenRows, 0, 0, resultSummary);
    }

    @Override
    public OperationPreview previewRepair(RepairPreviewRequest request) {
        ScopeTarget target = requireScopedTarget(request == null ? null : request.target());
        PreviewComputation preview = buildRepairPreview(
                target,
                request.actionType(),
                request.targetEventId(),
                request.replacementElementId(),
                request.replacementElementCode(),
                request.duplicateOfEventId(),
                request.reason()
        );
        return preview.response();
    }

    @Override
    public OperationResult applyRepair(RepairApplyRequest request) {
        ScopeTarget target = requireScopedTarget(request == null ? null : request.target());
        PreviewComputation preview = buildRepairPreview(
                target,
                request.actionType(),
                request.targetEventId(),
                request.replacementElementId(),
                request.replacementElementCode(),
                request.duplicateOfEventId(),
                request.reason()
        );
        validateConfirmation(preview.response(), request.previewToken(), request.confirmText());

        String actionType = normalizeActionType(request.actionType());
        int mutatedRows;
        Map<String, Object> resultSummary = new LinkedHashMap<>(preview.response().previewSummary());
        if (LINK_ORPHAN_EVENT.equals(actionType)) {
            mutatedRows = eventMapper.updateEventLink(
                    request.targetEventId(),
                    request.replacementElementId(),
                    defaultText(request.replacementElementCode()),
                    toJson(buildRepairNote(actionType, preview.response().previewSummary(), request.reason()))
            );
        } else if (MARK_DUPLICATE_CLIENT_EVENT.equals(actionType)) {
            mutatedRows = eventMapper.markDuplicate(
                    request.targetEventId(),
                    request.duplicateOfEventId(),
                    toJson(buildRepairNote(actionType, preview.response().previewSummary(), request.reason()))
            );
        } else {
            throw new BusinessException(4004, "Unsupported repair action");
        }
        resultSummary.put("mutatedEventRows", mutatedRows);
        resultSummary.put("deletedEventRows", 0);
        resultSummary.put("appliedAt", LocalDateTime.now());

        writeAudit(
                request.operator(),
                target,
                actionType,
                preview.response(),
                resultSummary,
                request.reason(),
                request.previewToken()
        );
        writeSystemLog(request.operator(), actionType, request.reason(), preview.previewSummaryJson(), toJson(resultSummary));

        return new OperationResult(actionType, "confirmed", 0, mutatedRows, 0, resultSummary);
    }

    @Override
    public List<UserProgressOperationAudit> listAudits(AuditQuery query) {
        int limit = query == null || query.limit() == null ? 20 : Math.max(1, Math.min(query.limit(), 200));
        String actionType = query == null ? "" : normalizeActionType(query.actionType());
        return auditMapper.selectList(new LambdaQueryWrapper<UserProgressOperationAudit>()
                .eq(query != null && query.targetUserId() != null, UserProgressOperationAudit::getTargetUserId, query.targetUserId())
                .eq(StringUtils.hasText(actionType), UserProgressOperationAudit::getActionType, actionType)
                .orderByDesc(UserProgressOperationAudit::getCreatedAt)
                .last("LIMIT " + limit));
    }

    private PreviewComputation buildRecomputePreview(ScopeTarget target, String reason) {
        String scopeType = normalizeScopeType(target.scopeType());
        List<AdminUserProgressReadMapper.ProgressElementRow> elements = readMapper.selectScopeElements(scopeType, target.scopeId(), true);
        List<AdminUserProgressReadMapper.ProgressEventRow> events = readMapper.selectUserEvents(target.userId());
        Set<String> elementKeys = new LinkedHashSet<>();
        elements.forEach(element -> {
            if (element.getElementId() != null) {
                elementKeys.add("id:" + element.getElementId());
            }
            if (StringUtils.hasText(element.getElementCode())) {
                elementKeys.add("code:" + element.getElementCode().trim());
            }
        });
        int completedElementCount = 0;
        int matchingEventCount = 0;
        Set<String> completedElementKeys = new LinkedHashSet<>();
        for (AdminUserProgressReadMapper.ProgressEventRow event : events) {
            String idKey = event.getElementId() == null ? null : "id:" + event.getElementId();
            String codeKey = StringUtils.hasText(event.getElementCode()) ? "code:" + event.getElementCode().trim() : null;
            if ((idKey != null && elementKeys.contains(idKey)) || (codeKey != null && elementKeys.contains(codeKey))) {
                matchingEventCount++;
                if (idKey != null) {
                    completedElementKeys.add(idKey);
                } else if (codeKey != null) {
                    completedElementKeys.add(codeKey);
                }
            }
        }
        completedElementCount = completedElementKeys.size();
        Map<String, Object> previewSummary = new LinkedHashMap<>();
        previewSummary.put("targetUserId", target.userId());
        previewSummary.put("scopeType", scopeType);
        previewSummary.put("scopeId", target.scopeId());
        previewSummary.put("storylineId", target.storylineId());
        previewSummary.put("availableElementCount", elements.size());
        previewSummary.put("completedElementCount", completedElementCount);
        previewSummary.put("matchingEventCount", matchingEventCount);
        previewSummary.put("reason", defaultText(reason));
        String previewJson = toJson(previewSummary);
        return new PreviewComputation(new OperationPreview(
                RECOMPUTE_SCOPE,
                RECOMPUTE_SCOPE,
                confirmationToken(RECOMPUTE_SCOPE, target, previewJson, reason),
                1,
                1,
                matchingEventCount,
                elements.size(),
                completedElementCount,
                previewSummary
        ), previewJson);
    }

    private PreviewComputation buildRepairPreview(
            ScopeTarget target,
            String requestedActionType,
            Long targetEventId,
            Long replacementElementId,
            String replacementElementCode,
            Long duplicateOfEventId,
            String reason) {
        String actionType = normalizeActionType(requestedActionType);
        Map<String, Object> previewSummary = new LinkedHashMap<>();
        previewSummary.put("targetUserId", target.userId());
        previewSummary.put("scopeType", normalizeScopeType(target.scopeType()));
        previewSummary.put("scopeId", target.scopeId());
        previewSummary.put("storylineId", target.storylineId());
        previewSummary.put("targetEventId", targetEventId);
        previewSummary.put("reason", defaultText(reason));

        if (LINK_ORPHAN_EVENT.equals(actionType)) {
            UserExplorationEventAdminMapper.EventRecord event = requireOwnedEvent(targetEventId, target.userId());
            if (!StringUtils.hasText(replacementElementCode) && replacementElementId == null) {
                throw new BusinessException(4004, "Replacement element must be provided");
            }
            previewSummary.put("beforeElementId", event.getElementId());
            previewSummary.put("beforeElementCode", event.getElementCode());
            previewSummary.put("afterElementId", replacementElementId);
            previewSummary.put("afterElementCode", defaultText(replacementElementCode));
        } else if (MARK_DUPLICATE_CLIENT_EVENT.equals(actionType)) {
            UserExplorationEventAdminMapper.EventRecord event = requireOwnedEvent(targetEventId, target.userId());
            UserExplorationEventAdminMapper.EventRecord canonical = requireOwnedEvent(duplicateOfEventId, target.userId());
            if (Objects.equals(event.getId(), canonical.getId())) {
                throw new BusinessException(4004, "Duplicate target must differ from canonical event");
            }
            previewSummary.put("beforeDuplicateMarked", event.getDuplicateMarked());
            previewSummary.put("beforeDuplicateOfEventId", event.getDuplicateOfEventId());
            previewSummary.put("duplicateOfEventId", canonical.getId());
            previewSummary.put("clientEventId", event.getClientEventId());
        } else {
            throw new BusinessException(4004, "Unsupported repair action");
        }

        String previewJson = toJson(previewSummary);
        return new PreviewComputation(new OperationPreview(
                actionType,
                actionType,
                confirmationToken(actionType, target, previewJson, reason),
                1,
                1,
                1,
                0,
                0,
                previewSummary
        ), previewJson);
    }

    private ScopeTarget requireScopedTarget(ScopeTarget target) {
        if (target == null || target.userId() == null) {
            throw new BusinessException(4004, "Repair and recompute requests must stay scoped to a target user");
        }
        String scopeType = normalizeScopeType(target.scopeType());
        if (!ALLOWED_SCOPE_TYPES.contains(scopeType)) {
            throw new BusinessException(4004, "Unsupported scope type");
        }
        if (!"global".equals(scopeType) && target.scopeId() == null && target.storylineId() == null) {
            throw new BusinessException(4004, "Non-global operations must stay scoped by scopeId or storyline");
        }
        return new ScopeTarget(target.userId(), scopeType, target.scopeId(), target.storylineId());
    }

    private UserExplorationEventAdminMapper.EventRecord requireOwnedEvent(Long eventId, Long targetUserId) {
        if (eventId == null) {
            throw new BusinessException(4004, "Target event is required");
        }
        UserExplorationEventAdminMapper.EventRecord event = eventMapper.selectEventById(eventId);
        if (event == null) {
            throw new BusinessException(4044, "Exploration event not found");
        }
        if (!Objects.equals(event.getUserId(), targetUserId)) {
            throw new BusinessException(4004, "Repair request cannot cross user boundaries");
        }
        return event;
    }

    private void validateConfirmation(OperationPreview preview, String previewToken, String confirmText) {
        if (!StringUtils.hasText(previewToken) || !Objects.equals(preview.confirmationToken(), previewToken.trim())) {
            throw new BusinessException(4004, "Preview token mismatch");
        }
        if (!StringUtils.hasText(confirmText) || !Objects.equals(preview.requiredConfirmText(), confirmText.trim())) {
            throw new BusinessException(4004, "Explicit confirm text is required");
        }
    }

    private void writeAudit(
            OperatorContext operator,
            ScopeTarget target,
            String actionType,
            OperationPreview preview,
            Map<String, Object> resultSummary,
            String reason,
            String previewToken) {
        UserProgressOperationAudit audit = new UserProgressOperationAudit();
        audit.setOperatorId(operator == null ? null : operator.operatorId());
        audit.setOperatorName(operator == null ? "" : defaultText(operator.operatorName()));
        audit.setTargetUserId(target.userId());
        audit.setScopeType(target.scopeType());
        audit.setScopeId(target.scopeId());
        audit.setStorylineId(target.storylineId());
        audit.setActionType(actionType);
        audit.setPreviewTokenHash(tokenHash(previewToken));
        audit.setPreviewSummaryJson(toJson(preview.previewSummary()));
        audit.setResultSummaryJson(toJson(resultSummary));
        audit.setReason(defaultText(reason));
        audit.setRequestIp(operator == null ? "" : defaultText(operator.requestIp()));
        auditMapper.insert(audit);
    }

    private void writeSystemLog(
            OperatorContext operator,
            String actionType,
            String reason,
            String requestParams,
            String responseData) {
        SysOperationLog log = new SysOperationLog();
        log.setOpenid("");
        log.setAdminId(operator == null ? null : operator.operatorId());
        log.setAdminUsername(operator == null ? "" : defaultText(operator.operatorName()));
        log.setModule("USER_PROGRESS");
        log.setOperation(actionType);
        log.setRequestMethod("POST");
        log.setRequestUrl("/api/admin/v1/traveler-progress/ops");
        log.setRequestParams(StringUtils.hasText(reason) ? reason.trim() : requestParams);
        log.setResponseData(responseData);
        log.setIp(operator == null ? "" : defaultText(operator.requestIp()));
        sysOperationLogMapper.insert(log);
    }

    private Map<String, Object> buildRepairNote(String actionType, Map<String, Object> previewSummary, String reason) {
        Map<String, Object> note = new LinkedHashMap<>(previewSummary);
        note.put("actionType", actionType);
        note.put("reason", defaultText(reason));
        note.put("repairedAt", LocalDateTime.now());
        return note;
    }

    private String normalizeScopeType(String scopeType) {
        String normalized = StringUtils.hasText(scopeType) ? scopeType.trim().toLowerCase(Locale.ROOT) : "global";
        return ALLOWED_SCOPE_TYPES.contains(normalized) ? normalized : normalized;
    }

    private String normalizeActionType(String actionType) {
        return StringUtils.hasText(actionType) ? actionType.trim().toUpperCase(Locale.ROOT) : "";
    }

    private String confirmationToken(String actionType, ScopeTarget target, String previewJson, String reason) {
        return tokenHash(actionType + "|" + target.userId() + "|" + target.scopeType() + "|" + target.scopeId()
                + "|" + target.storylineId() + "|" + defaultText(reason) + "|" + previewJson);
    }

    private String tokenHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(defaultText(input).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException(5003, "Unable to hash preview token");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5003, "Failed to serialize repair audit payload");
        }
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private record PreviewComputation(OperationPreview response, String previewSummaryJson) {
    }
}
