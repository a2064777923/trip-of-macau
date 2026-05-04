package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.entity.GameReward;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.RewardRedemption;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.UserGameRewardGrant;
import com.aoxiaoyou.admin.entity.UserProgressOperationAudit;
import com.aoxiaoyou.admin.mapper.AdminUserProgressReadMapper;
import com.aoxiaoyou.admin.mapper.GameRewardMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.RewardRedemptionMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationEventAdminMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationStateAdminMapper;
import com.aoxiaoyou.admin.mapper.UserGameRewardGrantMapper;
import com.aoxiaoyou.admin.mapper.UserProgressOperationAuditMapper;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.AdminUserProgressRepairService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
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
    public static final String VOID_DUPLICATE_EVENT = "VOID_DUPLICATE_EVENT";
    public static final String RESEND_REWARD = "RESEND_REWARD";
    public static final String ANNOTATE_ISSUE = "ANNOTATE_ISSUE";

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
    private final RewardMapper rewardMapper;
    private final GameRewardMapper gameRewardMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RewardRedemptionMapper rewardRedemptionMapper;
    private final UserGameRewardGrantMapper userGameRewardGrantMapper;
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

        return new OperationResult(RECOMPUTE_SCOPE, "confirmed", writtenRows, 0, 0, "進度已重新計算並寫入狀態快取", resultSummary);
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
                request.rewardId(),
                request.gameRewardId(),
                request.ruleId(),
                request.sourceEventId(),
                request.annotationText(),
                request.issueSeverity(),
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
                request.rewardId(),
                request.gameRewardId(),
                request.ruleId(),
                request.sourceEventId(),
                request.annotationText(),
                request.issueSeverity(),
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
        } else if (VOID_DUPLICATE_EVENT.equals(actionType)) {
            mutatedRows = eventMapper.markDuplicate(
                    request.targetEventId(),
                    request.duplicateOfEventId(),
                    toJson(buildRepairNote(actionType, preview.response().previewSummary(), request.reason()))
            );
            resultSummary.put("voided", true);
        } else if (RESEND_REWARD.equals(actionType)) {
            ResendApplyResult resendResult = applyResendReward(target, request, preview.response().previewSummary());
            mutatedRows = 0;
            resultSummary.putAll(resendResult.resultSummary());
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
            return new OperationResult(
                    actionType,
                    resendResult.status(),
                    resendResult.writtenRows(),
                    0,
                    0,
                    resendResult.operationMessage(),
                    resultSummary
            );
        } else if (ANNOTATE_ISSUE.equals(actionType)) {
            mutatedRows = 0;
            resultSummary.put("annotationText", defaultText(request.annotationText()));
            resultSummary.put("issueSeverity", normalizeIssueSeverity(request.issueSeverity()));
            resultSummary.put("annotated", true);
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

        return new OperationResult(actionType, operationStatus(actionType), 0, mutatedRows, 0, operationMessage(actionType), resultSummary);
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
            Long rewardId,
            Long gameRewardId,
            Long ruleId,
            Long sourceEventId,
            String annotationText,
            String issueSeverity,
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
        } else if (MARK_DUPLICATE_CLIENT_EVENT.equals(actionType) || VOID_DUPLICATE_EVENT.equals(actionType)) {
            UserExplorationEventAdminMapper.EventRecord event = requireOwnedEvent(targetEventId, target.userId());
            UserExplorationEventAdminMapper.EventRecord canonical = requireOwnedEvent(duplicateOfEventId, target.userId());
            if (Objects.equals(event.getId(), canonical.getId())) {
                throw new BusinessException(4004, "Duplicate target must differ from canonical event");
            }
            previewSummary.put("beforeDuplicateMarked", event.getDuplicateMarked());
            previewSummary.put("beforeDuplicateOfEventId", event.getDuplicateOfEventId());
            previewSummary.put("duplicateOfEventId", canonical.getId());
            previewSummary.put("clientEventId", event.getClientEventId());
            previewSummary.put("deletedEventRows", 0);
            previewSummary.put("voided", VOID_DUPLICATE_EVENT.equals(actionType));
        } else if (RESEND_REWARD.equals(actionType)) {
            if (rewardId == null && gameRewardId == null) {
                throw new BusinessException(4004, "Reward or game reward target must be provided");
            }
            if (sourceEventId == null && targetEventId == null && ruleId == null) {
                throw new BusinessException(4004, "Reward resend must include source event, target event, or rule");
            }
            Reward reward = rewardId == null ? null : requireReward(rewardId);
            GameReward gameReward = gameRewardId == null ? null : requireGameReward(gameRewardId);
            if (ruleId != null && rewardRuleMapper.selectById(ruleId) == null) {
                throw new BusinessException(4044, "Reward rule not found");
            }
            boolean alreadyGranted = rewardId != null
                    ? findRewardRedemption(target.userId(), rewardId, ruleId, firstNonNull(sourceEventId, targetEventId)) != null
                    : findGameRewardGrant(target.userId(), gameRewardId, ruleId, firstNonNull(sourceEventId, targetEventId)) != null;
            previewSummary.put("rewardId", rewardId);
            previewSummary.put("gameRewardId", gameRewardId);
            previewSummary.put("ruleId", ruleId);
            previewSummary.put("sourceEventId", sourceEventId);
            previewSummary.put("targetRewardType", rewardId != null ? "redeemable_reward" : "game_reward");
            previewSummary.put("targetRewardName", reward != null ? localizedRewardName(reward) : localizedGameRewardName(gameReward));
            previewSummary.put("alreadyGranted", alreadyGranted);
            previewSummary.put("operatorMessage", alreadyGranted
                    ? "旅客已擁有此補發結果，確認後不會重複發放"
                    : "可補發，確認後會寫入旅客獎勵狀態與審計");
        } else if (ANNOTATE_ISSUE.equals(actionType)) {
            if (!StringUtils.hasText(annotationText)) {
                throw new BusinessException(4004, "Annotation text is required");
            }
            previewSummary.put("annotationText", annotationText.trim());
            previewSummary.put("issueSeverity", normalizeIssueSeverity(issueSeverity));
            previewSummary.put("sourceEventId", sourceEventId);
            previewSummary.put("rewardId", rewardId);
            previewSummary.put("gameRewardId", gameRewardId);
            previewSummary.put("ruleId", ruleId);
            previewSummary.put("operatorMessage", "確認後只會寫入審計與系統操作紀錄，不會改動旅客狀態");
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

    private Reward requireReward(Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4044, "Reward not found");
        }
        return reward;
    }

    private GameReward requireGameReward(Long gameRewardId) {
        GameReward gameReward = gameRewardMapper.selectById(gameRewardId);
        if (gameReward == null) {
            throw new BusinessException(4044, "Game reward not found");
        }
        return gameReward;
    }

    private RewardRedemption findRewardRedemption(Long userId, Long rewardId, Long ruleId, Long sourceEventId) {
        return rewardRedemptionMapper.selectOne(new LambdaQueryWrapper<RewardRedemption>()
                .eq(RewardRedemption::getUserId, userId)
                .eq(RewardRedemption::getRewardId, rewardId)
                .eq(RewardRedemption::getDeleted, 0)
                .eq(ruleId != null, RewardRedemption::getSourceRuleId, ruleId)
                .eq(sourceEventId != null, RewardRedemption::getSourceEventId, sourceEventId)
                .last("LIMIT 1"));
    }

    private UserGameRewardGrant findGameRewardGrant(Long userId, Long gameRewardId, Long ruleId, Long sourceEventId) {
        return userGameRewardGrantMapper.selectOne(new LambdaQueryWrapper<UserGameRewardGrant>()
                .eq(UserGameRewardGrant::getUserId, userId)
                .eq(UserGameRewardGrant::getGameRewardId, gameRewardId)
                .eq(ruleId != null, UserGameRewardGrant::getRuleId, ruleId)
                .eq(sourceEventId != null, UserGameRewardGrant::getSourceEventId, sourceEventId)
                .last("LIMIT 1"));
    }

    private ResendApplyResult applyResendReward(
            ScopeTarget target,
            RepairApplyRequest request,
            Map<String, Object> previewSummary) {
        Long effectiveSourceEventId = firstNonNull(request.sourceEventId(), request.targetEventId());
        Long sourceSessionId = null;
        if (effectiveSourceEventId != null) {
            UserExplorationEventAdminMapper.EventRecord event = requireOwnedEvent(effectiveSourceEventId, target.userId());
            sourceSessionId = null;
            previewSummary.put("sourceEventType", event.getEventType());
        }
        boolean alreadyGranted = Boolean.TRUE.equals(previewSummary.get("alreadyGranted"));
        if (alreadyGranted) {
            Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, null, null);
            result.put("alreadyGranted", true);
            result.put("grantPersistenceMode", request.rewardId() != null ? "reward_redemptions" : "user_game_reward_grants");
            result.put("writtenStateRows", 0);
            result.put("resendRecordedAt", LocalDateTime.now());
            return new ResendApplyResult("already_present", 0, "旅客已擁有此補發結果，未重複發放", result);
        }

        if (request.rewardId() != null) {
            Reward reward = requireReward(request.rewardId());
            String idempotencyKey = rewardIdempotencyKey(
                    target.userId(),
                    request.rewardId(),
                    request.ruleId(),
                    effectiveSourceEventId,
                    sourceSessionId);
            RewardRedemption existing = selectRewardRedemptionByIdempotency(idempotencyKey);
            if (existing != null) {
                Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "reward_redemptions", existing.getId());
                result.put("alreadyGranted", true);
                result.put("writtenStateRows", 0);
                result.put("resendRecordedAt", existing.getCreatedAt());
                return new ResendApplyResult("already_present", 0, "旅客已擁有此補發結果，未重複發放", result);
            }
            RewardRedemption redemption = new RewardRedemption();
            redemption.setUserId(target.userId());
            redemption.setRewardId(reward.getId());
            redemption.setRedemptionStatus("created");
            redemption.setStampCostSnapshot(reward.getStampCost() == null ? 0 : reward.getStampCost());
            redemption.setQrCode("SUPPORT-" + target.userId() + "-" + reward.getId());
            redemption.setRedeemedAt(LocalDateTime.now());
            redemption.setExpiresAt(null);
            redemption.setSourceEventId(effectiveSourceEventId);
            redemption.setSourceRuleId(request.ruleId());
            redemption.setSourceSessionId(sourceSessionId);
            redemption.setIdempotencyKey(idempotencyKey);
            redemption.setDeleted(0);
            try {
                rewardRedemptionMapper.insert(redemption);
            } catch (DuplicateKeyException ex) {
                existing = selectRewardRedemptionByIdempotency(idempotencyKey);
                Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "reward_redemptions", existing == null ? null : existing.getId());
                result.put("alreadyGranted", true);
                result.put("writtenStateRows", 0);
                result.put("resendRecordedAt", LocalDateTime.now());
                return new ResendApplyResult("already_present", 0, "旅客已擁有此補發結果，未重複發放", result);
            }
            Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "reward_redemptions", redemption.getId());
            result.put("writtenStateRows", 1);
            result.put("resendRecordedAt", LocalDateTime.now());
            return new ResendApplyResult("resent", 1, "已補發兌換獎勵並寫入旅客狀態", result);
        }

        GameReward gameReward = requireGameReward(request.gameRewardId());
        String idempotencyKey = gameRewardIdempotencyKey(
                target.userId(),
                gameReward.getId(),
                request.ruleId(),
                effectiveSourceEventId,
                sourceSessionId);
        UserGameRewardGrant existing = selectGameRewardGrantByIdempotency(idempotencyKey);
        if (existing != null) {
            Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "user_game_reward_grants", existing.getId());
            result.put("alreadyGranted", true);
            result.put("writtenStateRows", 0);
            result.put("resendRecordedAt", existing.getGrantedAt());
            return new ResendApplyResult("already_present", 0, "旅客已擁有此補發結果，未重複發放", result);
        }
        UserGameRewardGrant grant = new UserGameRewardGrant();
        grant.setUserId(target.userId());
        grant.setGameRewardId(gameReward.getId());
        grant.setRuleId(request.ruleId());
        grant.setSourceEventId(effectiveSourceEventId);
        grant.setSourceSessionId(sourceSessionId);
        grant.setGrantStatus("granted");
        grant.setGrantReason(defaultText(request.reason()));
        grant.setGrantedBy(request.operator() == null ? null : request.operator().operatorId());
        grant.setGrantedAt(LocalDateTime.now());
        grant.setIdempotencyKey(idempotencyKey);
        try {
            userGameRewardGrantMapper.insert(grant);
        } catch (DuplicateKeyException ex) {
            existing = selectGameRewardGrantByIdempotency(idempotencyKey);
            Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "user_game_reward_grants", existing == null ? null : existing.getId());
            result.put("alreadyGranted", true);
            result.put("writtenStateRows", 0);
            result.put("resendRecordedAt", LocalDateTime.now());
            return new ResendApplyResult("already_present", 0, "旅客已擁有此補發結果，未重複發放", result);
        }
        Map<String, Object> result = resendResultBase(request, effectiveSourceEventId, "user_game_reward_grants", grant.getId());
        result.put("writtenStateRows", 1);
        result.put("resendRecordedAt", grant.getGrantedAt());
        return new ResendApplyResult("resent", 1, "已補發遊戲內獎勵並寫入旅客狀態", result);
    }

    private Map<String, Object> resendResultBase(
            RepairApplyRequest request,
            Long effectiveSourceEventId,
            String grantPersistenceMode,
            Long grantRowId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rewardId", request.rewardId());
        result.put("gameRewardId", request.gameRewardId());
        result.put("ruleId", request.ruleId());
        result.put("sourceEventId", effectiveSourceEventId);
        result.put("grantPersistenceMode", grantPersistenceMode);
        result.put("grantRowId", grantRowId);
        result.put("mutatedEventRows", 0);
        result.put("deletedEventRows", 0);
        return result;
    }

    private RewardRedemption selectRewardRedemptionByIdempotency(String idempotencyKey) {
        return rewardRedemptionMapper.selectOne(new LambdaQueryWrapper<RewardRedemption>()
                .eq(RewardRedemption::getIdempotencyKey, idempotencyKey)
                .last("LIMIT 1"));
    }

    private UserGameRewardGrant selectGameRewardGrantByIdempotency(String idempotencyKey) {
        return userGameRewardGrantMapper.selectOne(new LambdaQueryWrapper<UserGameRewardGrant>()
                .eq(UserGameRewardGrant::getIdempotencyKey, idempotencyKey)
                .last("LIMIT 1"));
    }

    private String rewardIdempotencyKey(Long userId, Long rewardId, Long ruleId, Long sourceEventId, Long sourceSessionId) {
        return "resend:reward:" + userId + ":" + rewardId + ":rule:" + nullToZero(ruleId)
                + ":event:" + nullToZero(sourceEventId) + ":session:" + nullToZero(sourceSessionId);
    }

    private String gameRewardIdempotencyKey(Long userId, Long gameRewardId, Long ruleId, Long sourceEventId, Long sourceSessionId) {
        return "resend:game_reward:" + userId + ":" + gameRewardId + ":rule:" + nullToZero(ruleId)
                + ":event:" + nullToZero(sourceEventId) + ":session:" + nullToZero(sourceSessionId);
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
        String normalized = StringUtils.hasText(actionType) ? actionType.trim().toUpperCase(Locale.ROOT) : "";
        if ("VOID_DUPLICATE".equals(normalized)) {
            return VOID_DUPLICATE_EVENT;
        }
        return normalized;
    }

    private String normalizeIssueSeverity(String issueSeverity) {
        String normalized = StringUtils.hasText(issueSeverity) ? issueSeverity.trim().toLowerCase(Locale.ROOT) : "info";
        return switch (normalized) {
            case "warning", "critical" -> normalized;
            default -> "info";
        };
    }

    private String operationStatus(String actionType) {
        if (ANNOTATE_ISSUE.equals(actionType)) {
            return "annotated";
        }
        if (VOID_DUPLICATE_EVENT.equals(actionType)) {
            return "voided";
        }
        return "confirmed";
    }

    private String operationMessage(String actionType) {
        if (ANNOTATE_ISSUE.equals(actionType)) {
            return "已新增支援註記，不會改動旅客狀態";
        }
        if (VOID_DUPLICATE_EVENT.equals(actionType)) {
            return "已標記重複事件，未刪除原始紀錄";
        }
        if (MARK_DUPLICATE_CLIENT_EVENT.equals(actionType)) {
            return "已標記重複事件";
        }
        if (LINK_ORPHAN_EVENT.equals(actionType)) {
            return "已重新連結探索事件";
        }
        return "操作已完成";
    }

    private String localizedRewardName(Reward reward) {
        if (reward == null) {
            return "";
        }
        if (StringUtils.hasText(reward.getNameZht())) {
            return reward.getNameZht().trim();
        }
        if (StringUtils.hasText(reward.getNameZh())) {
            return reward.getNameZh().trim();
        }
        if (StringUtils.hasText(reward.getNameEn())) {
            return reward.getNameEn().trim();
        }
        return defaultText(reward.getCode());
    }

    private String localizedGameRewardName(GameReward reward) {
        if (reward == null) {
            return "";
        }
        if (StringUtils.hasText(reward.getNameZht())) {
            return reward.getNameZht().trim();
        }
        if (StringUtils.hasText(reward.getNameZh())) {
            return reward.getNameZh().trim();
        }
        if (StringUtils.hasText(reward.getNameEn())) {
            return reward.getNameEn().trim();
        }
        return defaultText(reward.getCode());
    }

    private Long firstNonNull(Long first, Long second) {
        return first != null ? first : second;
    }

    private long nullToZero(Long value) {
        return value == null ? 0L : value;
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

    private record ResendApplyResult(
            String status,
            int writtenRows,
            String operationMessage,
            Map<String, Object> resultSummary) {
    }
}
