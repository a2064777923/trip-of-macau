package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;
import com.aoxiaoyou.admin.entity.ExperienceBinding;
import com.aoxiaoyou.admin.entity.ExperienceFlow;
import com.aoxiaoyou.admin.entity.ExperienceFlowStep;
import com.aoxiaoyou.admin.entity.ExperienceOverride;
import com.aoxiaoyou.admin.entity.ExperienceTemplate;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.admin.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.admin.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.service.AdminExperienceGovernanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminExperienceGovernanceServiceImpl implements AdminExperienceGovernanceService {

    private final ExperienceTemplateMapper templateMapper;
    private final ExperienceFlowMapper flowMapper;
    private final ExperienceFlowStepMapper stepMapper;
    private final ExperienceBindingMapper bindingMapper;
    private final ExperienceOverrideMapper overrideMapper;
    private final IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RewardRuleBindingMapper rewardRuleBindingMapper;

    @Override
    public PageResponse<AdminExperienceResponse.GovernanceItem> pageGovernanceItems(AdminExperienceRequest.GovernanceQuery query) {
        AdminExperienceRequest.GovernanceQuery safeQuery = query == null ? new AdminExperienceRequest.GovernanceQuery() : query;
        List<AdminExperienceResponse.GovernanceItem> items = applyFilters(buildGovernanceItems(), safeQuery);
        List<AdminExperienceResponse.GovernanceFinding> conflicts = detectConflicts(items);
        Map<String, Long> conflictsByItemKey = conflicts.stream()
                .filter(finding -> StringUtils.hasText(finding.getItemKey()))
                .collect(Collectors.groupingBy(AdminExperienceResponse.GovernanceFinding::getItemKey, LinkedHashMap::new, Collectors.counting()));
        List<AdminExperienceResponse.GovernanceItem> enriched = items.stream()
                .map(item -> copyItemWithConflictCount(item, conflictsByItemKey.getOrDefault(item.getItemKey(), 0L)))
                .filter(item -> !Boolean.TRUE.equals(safeQuery.getConflictOnly()) || item.getConflictCount() > 0)
                .sorted(Comparator.comparing(AdminExperienceResponse.GovernanceItem::getSourceDomain, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AdminExperienceResponse.GovernanceItem::getOwnerType, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AdminExperienceResponse.GovernanceItem::getItemKey, Comparator.nullsLast(String::compareTo)))
                .toList();
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1 ? 1 : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1 ? 20 : Math.min(safeQuery.getPageSize(), 100);
        int fromIndex = (int) Math.min((pageNum - 1) * pageSize, enriched.size());
        int toIndex = (int) Math.min(fromIndex + pageSize, enriched.size());
        return PageResponse.<AdminExperienceResponse.GovernanceItem>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(enriched.size())
                .totalPages((long) Math.ceil(enriched.size() / (double) pageSize))
                .list(enriched.subList(fromIndex, toIndex))
                .build();
    }

    @Override
    public AdminExperienceResponse.GovernanceDetail getGovernanceDetail(String itemKey) {
        AdminExperienceResponse.GovernanceItem item = buildGovernanceItems().stream()
                .filter(candidate -> Objects.equals(candidate.getItemKey(), itemKey))
                .findFirst()
                .orElseThrow(() -> new BusinessException(4077, "Governance item not found"));
        List<AdminExperienceResponse.GovernanceFinding> conflicts = detectConflicts(buildGovernanceItems()).stream()
                .filter(conflict -> Objects.equals(conflict.getItemKey(), itemKey))
                .toList();
        return AdminExperienceResponse.GovernanceDetail.builder()
                .item(copyItemWithConflictCount(item, (long) conflicts.size()))
                .usageRefs(buildUsageRefs(item))
                .conflicts(conflicts)
                .rawSummary("source=" + item.getSourceDomain() + "; owner=" + item.getOwnerType() + "; template=" + item.getTemplateCode())
                .build();
    }

    @Override
    public List<AdminExperienceResponse.GovernanceFinding> checkGovernanceConflicts(AdminExperienceRequest.GovernanceQuery query) {
        return detectConflicts(applyFilters(buildGovernanceItems(), query == null ? new AdminExperienceRequest.GovernanceQuery() : query));
    }

    private List<AdminExperienceResponse.GovernanceItem> buildGovernanceItems() {
        List<ExperienceTemplate> templates = templateMapper.selectList(activeTemplateQuery());
        List<ExperienceFlow> flows = flowMapper.selectList(activeFlowQuery());
        List<ExperienceFlowStep> steps = stepMapper.selectList(activeStepQuery());
        List<ExperienceBinding> bindings = bindingMapper.selectList(activeBindingQuery());
        List<ExperienceOverride> overrides = overrideMapper.selectList(activeOverrideQuery());
        List<IndoorNodeBehavior> indoorBehaviors = indoorNodeBehaviorMapper.selectList(null);
        List<RewardRule> rewardRules = rewardRuleMapper.selectList(null);
        List<RewardRuleBinding> rewardRuleBindings = rewardRuleBindingMapper.selectList(null);

        Map<Long, ExperienceTemplate> templatesById = templates.stream()
                .collect(Collectors.toMap(ExperienceTemplate::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<String, ExperienceTemplate> templatesByCode = templates.stream()
                .filter(template -> StringUtils.hasText(template.getCode()))
                .collect(Collectors.toMap(template -> normalize(template.getCode()), Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ExperienceFlow> flowsById = flows.stream()
                .collect(Collectors.toMap(ExperienceFlow::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ExperienceBinding>> bindingsByFlowId = bindings.stream()
                .collect(Collectors.groupingBy(ExperienceBinding::getFlowId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, RewardRule> rewardRulesById = rewardRules.stream()
                .collect(Collectors.toMap(RewardRule::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<AdminExperienceResponse.GovernanceItem> items = new ArrayList<>();
        for (ExperienceFlowStep step : steps) {
            ExperienceFlow flow = flowsById.get(step.getFlowId());
            if (flow == null) {
                continue;
            }
            ExperienceTemplate template = step.getTemplateId() == null ? null : templatesById.get(step.getTemplateId());
            List<ExperienceBinding> stepBindings = bindingsByFlowId.getOrDefault(flow.getId(), List.of());
            if (stepBindings.isEmpty()) {
                items.add(stepItem(flow, step, template, null));
            } else {
                for (ExperienceBinding binding : stepBindings) {
                    items.add(stepItem(flow, step, template, binding));
                }
            }
        }
        for (ExperienceOverride override : overrides) {
            items.add(overrideItem(override));
        }
        for (IndoorNodeBehavior behavior : indoorBehaviors) {
            addIndoorTemplateItem(items, behavior, behavior.getTriggerTemplateCode(), "trigger_condition", templatesByCode);
            addIndoorTemplateItem(items, behavior, behavior.getEffectTemplateCode(), "trigger_effect", templatesByCode);
        }
        for (RewardRuleBinding binding : rewardRuleBindings) {
            RewardRule rule = rewardRulesById.get(binding.getRuleId());
            if (rule != null) {
                items.add(rewardItem(rule, binding));
            }
        }
        return items;
    }

    private AdminExperienceResponse.GovernanceItem stepItem(ExperienceFlow flow, ExperienceFlowStep step, ExperienceTemplate template, ExperienceBinding binding) {
        String ownerType = binding == null ? flow.getFlowType() : binding.getOwnerType();
        Long ownerId = binding == null ? flow.getId() : binding.getOwnerId();
        String ownerCode = binding == null ? flow.getCode() : binding.getOwnerCode();
        boolean storyOverride = "story_chapter".equals(ownerType) || "story_chapter_override".equals(flow.getFlowType());
        return AdminExperienceResponse.GovernanceItem.builder()
                .itemKey(itemKey("experience_step", ownerType, ownerId, ownerCode, flow.getId(), step.getId(), step.getStepCode()))
                .sourceDomain("experience_step")
                .ownerType(ownerType)
                .ownerId(ownerId)
                .ownerCode(ownerCode)
                .ownerName(binding == null ? flow.getNameZh() : binding.getOwnerCode())
                .poiId("poi".equals(ownerType) ? ownerId : null)
                .indoorBuildingId("indoor_building".equals(ownerType) ? ownerId : null)
                .storyChapterId("story_chapter".equals(ownerType) ? ownerId : null)
                .templateId(template == null ? null : template.getId())
                .templateCode(template == null ? null : template.getCode())
                .templateNameZh(template == null ? null : template.getNameZh())
                .templateType(template == null ? step.getStepType() : template.getTemplateType())
                .flowId(flow.getId())
                .flowCode(flow.getCode())
                .stepId(step.getId())
                .stepCode(step.getStepCode())
                .triggerType(step.getTriggerType())
                .effectFamily(resolveEffectFamily(step.getEffectConfigJson(), template == null ? null : template.getCategory(), step.getStepType()))
                .rewardType(resolveRewardType(step.getRewardRuleIdsJson(), step.getEffectConfigJson()))
                .status(step.getStatus())
                .riskLevel(template == null ? "normal" : template.getRiskLevel())
                .storyOverride(storyOverride)
                .conflictCount(0L)
                .build();
    }

    private AdminExperienceResponse.GovernanceItem overrideItem(ExperienceOverride override) {
        return AdminExperienceResponse.GovernanceItem.builder()
                .itemKey(itemKey("story_override", override.getOwnerType(), override.getOwnerId(), null, null, override.getId(), override.getTargetStepCode()))
                .sourceDomain("story_override")
                .ownerType(override.getOwnerType())
                .ownerId(override.getOwnerId())
                .storyChapterId("story_chapter".equals(override.getOwnerType()) ? override.getOwnerId() : null)
                .templateNameZh("章節覆寫：" + override.getOverrideMode())
                .templateType("story_override")
                .stepId(override.getReplacementStepId())
                .stepCode(override.getTargetStepCode())
                .triggerType("story_mode_enter")
                .effectFamily(override.getOverrideMode())
                .status(override.getStatus())
                .riskLevel("disable".equals(override.getOverrideMode()) ? "high" : "normal")
                .storyOverride(true)
                .conflictCount(0L)
                .build();
    }

    private void addIndoorTemplateItem(List<AdminExperienceResponse.GovernanceItem> items, IndoorNodeBehavior behavior, String templateCode, String fallbackType, Map<String, ExperienceTemplate> templatesByCode) {
        if (!StringUtils.hasText(templateCode)) {
            return;
        }
        ExperienceTemplate template = templatesByCode.get(normalize(templateCode));
        items.add(AdminExperienceResponse.GovernanceItem.builder()
                .itemKey(itemKey("indoor_behavior", "indoor_node", behavior.getNodeId(), behavior.getBehaviorCode(), null, behavior.getId(), templateCode))
                .sourceDomain("indoor_behavior")
                .ownerType("indoor_node")
                .ownerId(behavior.getNodeId())
                .ownerCode(behavior.getBehaviorCode())
                .ownerName(behavior.getBehaviorNameZht() == null ? behavior.getBehaviorNameZh() : behavior.getBehaviorNameZht())
                .templateId(template == null ? null : template.getId())
                .templateCode(templateCode)
                .templateNameZh(template == null ? templateCode : template.getNameZh())
                .templateType(template == null ? fallbackType : template.getTemplateType())
                .stepId(behavior.getId())
                .stepCode(behavior.getBehaviorCode())
                .triggerType(resolveBehaviorTrigger(behavior))
                .effectFamily(resolveEffectFamily(behavior.getEffectRulesJson(), template == null ? null : template.getCategory(), behavior.getEffectTemplateCode()))
                .rewardType(resolveRewardType(behavior.getEffectRulesJson(), behavior.getTriggerRulesJson()))
                .status(behavior.getStatus())
                .riskLevel(template == null ? "normal" : template.getRiskLevel())
                .storyOverride(false)
                .conflictCount(0L)
                .build());
    }

    private AdminExperienceResponse.GovernanceItem rewardItem(RewardRule rule, RewardRuleBinding binding) {
        return AdminExperienceResponse.GovernanceItem.builder()
                .itemKey(itemKey("reward_rule", binding.getOwnerDomain(), binding.getOwnerId(), binding.getOwnerCode(), null, rule.getId(), rule.getCode()))
                .sourceDomain("reward_rule")
                .ownerType(binding.getOwnerDomain())
                .ownerId(binding.getOwnerId())
                .ownerCode(binding.getOwnerCode())
                .ownerName(rule.getNameZht() == null ? rule.getNameZh() : rule.getNameZht())
                .templateNameZh(rule.getNameZh())
                .templateType("reward_rule")
                .stepId(rule.getId())
                .stepCode(rule.getCode())
                .triggerType("task_complete")
                .effectFamily("grant_reward")
                .rewardType(rule.getCode())
                .status(rule.getStatus())
                .riskLevel("high")
                .storyOverride("story_chapter".equals(binding.getOwnerDomain()))
                .conflictCount(0L)
                .build();
    }

    private List<AdminExperienceResponse.GovernanceItem> applyFilters(List<AdminExperienceResponse.GovernanceItem> items, AdminExperienceRequest.GovernanceQuery query) {
        return items.stream()
                .filter(item -> !StringUtils.hasText(query.getKeyword()) || contains(item.getItemKey(), query.getKeyword()) || contains(item.getOwnerName(), query.getKeyword()) || contains(item.getTemplateNameZh(), query.getKeyword()) || contains(item.getTemplateCode(), query.getKeyword()))
                .filter(item -> query.getPoiId() == null || Objects.equals(item.getPoiId(), query.getPoiId()) || ("poi".equals(item.getOwnerType()) && Objects.equals(item.getOwnerId(), query.getPoiId())))
                .filter(item -> query.getIndoorBuildingId() == null || Objects.equals(item.getIndoorBuildingId(), query.getIndoorBuildingId()) || ("indoor_building".equals(item.getOwnerType()) && Objects.equals(item.getOwnerId(), query.getIndoorBuildingId())))
                .filter(item -> query.getStoryChapterId() == null || Objects.equals(item.getStoryChapterId(), query.getStoryChapterId()) || ("story_chapter".equals(item.getOwnerType()) && Objects.equals(item.getOwnerId(), query.getStoryChapterId())))
                .filter(item -> !StringUtils.hasText(query.getOwnerType()) || Objects.equals(item.getOwnerType(), normalize(query.getOwnerType())))
                .filter(item -> !StringUtils.hasText(query.getTemplateType()) || Objects.equals(item.getTemplateType(), normalize(query.getTemplateType())))
                .filter(item -> !StringUtils.hasText(query.getTriggerType()) || Objects.equals(item.getTriggerType(), normalize(query.getTriggerType())))
                .filter(item -> !StringUtils.hasText(query.getEffectFamily()) || Objects.equals(item.getEffectFamily(), normalize(query.getEffectFamily())))
                .filter(item -> !StringUtils.hasText(query.getRewardType()) || Objects.equals(item.getRewardType(), normalize(query.getRewardType())))
                .filter(item -> !StringUtils.hasText(query.getStatus()) || Objects.equals(item.getStatus(), normalize(query.getStatus())))
                .filter(item -> !Boolean.TRUE.equals(query.getStoryOverrideOnly()) || Boolean.TRUE.equals(item.getStoryOverride()))
                .filter(item -> !Boolean.TRUE.equals(query.getHighRiskOnly()) || isHighRisk(item.getRiskLevel()))
                .toList();
    }

    private List<AdminExperienceResponse.GovernanceFinding> detectConflicts(List<AdminExperienceResponse.GovernanceItem> items) {
        List<AdminExperienceResponse.GovernanceFinding> findings = new ArrayList<>();
        addOverlappingFullscreenFindings(items, findings);
        addDuplicateRewardFindings(items, findings);
        addRequiredStepDisabledFindings(items, findings);
        addSharedPickupPolicyFindings(items, findings);
        addHighRiskTemplateWithoutGuardFindings(items, findings);
        return findings;
    }

    private void addOverlappingFullscreenFindings(List<AdminExperienceResponse.GovernanceItem> items, List<AdminExperienceResponse.GovernanceFinding> findings) {
        grouped(items.stream()
                .filter(item -> "published".equals(item.getStatus()))
                .filter(item -> "fullscreen_media".equals(item.getEffectFamily()))
                .collect(Collectors.groupingBy(item -> ownerScope(item) + ":" + nullSafe(item.getTriggerType()))))
                .values().stream()
                .filter(group -> group.size() > 1)
                .forEach(group -> group.forEach(item -> findings.add(finding("warning", "overlapping_fullscreen_effect", "同一主體存在重疊全屏效果", "同一地點或章節在相同觸發條件下有多個全屏媒體效果，發布前需要調整前置條件或冷卻策略。", item, null))));
    }

    private void addDuplicateRewardFindings(List<AdminExperienceResponse.GovernanceItem> items, List<AdminExperienceResponse.GovernanceFinding> findings) {
        grouped(items.stream()
                .filter(item -> "published".equals(item.getStatus()))
                .filter(item -> StringUtils.hasText(item.getRewardType()))
                .collect(Collectors.groupingBy(item -> ownerScope(item) + ":" + item.getRewardType())))
                .values().stream()
                .filter(group -> group.size() > 1)
                .forEach(group -> group.forEach(item -> findings.add(finding("error", "duplicate_reward_grant", "同一獎勵可能被重複發放", "同一主體範圍內存在多條規則發放相同獎勵或收集物，需確認是否互斥或改成同一條規則。", item, item.getStepId()))));
    }

    private void addRequiredStepDisabledFindings(List<AdminExperienceResponse.GovernanceItem> items, List<AdminExperienceResponse.GovernanceFinding> findings) {
        items.stream()
                .filter(item -> "story_override".equals(item.getSourceDomain()))
                .filter(item -> "published".equals(item.getStatus()))
                .filter(item -> "disable".equals(item.getEffectFamily()))
                .forEach(item -> findings.add(finding("error", "required_step_disabled", "章節覆寫關閉了繼承步驟", "章節覆寫以 disable 關閉繼承步驟，需確認已有替代主線效果或完成條件，避免 runtime 缺失必要步驟。", item, null)));
    }

    private void addSharedPickupPolicyFindings(List<AdminExperienceResponse.GovernanceItem> items, List<AdminExperienceResponse.GovernanceFinding> findings) {
        grouped(items.stream()
                .filter(item -> StringUtils.hasText(item.getRewardType()))
                .filter(item -> item.getRewardType().contains("pickup") || item.getRewardType().contains("collect"))
                .collect(Collectors.groupingBy(AdminExperienceResponse.GovernanceItem::getRewardType)))
                .values().stream()
                .filter(group -> group.stream().map(AdminExperienceResponse.GovernanceItem::getEffectFamily).distinct().count() > 1)
                .forEach(group -> group.forEach(item -> findings.add(finding("warning", "shared_pickup_policy_mismatch", "同一拾取物被不同策略引用", "同一拾取物或收集物在多處使用不同效果策略，需確認主線、支線與獎勵規則是否一致。", item, null))));
    }

    private void addHighRiskTemplateWithoutGuardFindings(List<AdminExperienceResponse.GovernanceItem> items, List<AdminExperienceResponse.GovernanceFinding> findings) {
        Map<Long, Long> usageByTemplate = items.stream()
                .filter(item -> item.getTemplateId() != null)
                .collect(Collectors.groupingBy(AdminExperienceResponse.GovernanceItem::getTemplateId, Collectors.counting()));
        items.stream()
                .filter(item -> "published".equals(item.getStatus()))
                .filter(item -> isHighRisk(item.getRiskLevel()))
                .filter(item -> item.getTemplateId() != null && usageByTemplate.getOrDefault(item.getTemplateId(), 0L) <= 1)
                .forEach(item -> findings.add(finding("info", "high_risk_template_without_usage_guard", "高風險模板缺少足夠使用保護", "高風險模板已發布但目前使用處過少或缺少治理確認，建議補充 schema guard、冷卻或互斥條件。", item, null)));
    }

    private Map<String, List<AdminExperienceResponse.GovernanceItem>> grouped(Map<String, List<AdminExperienceResponse.GovernanceItem>> groups) {
        return groups;
    }

    private AdminExperienceResponse.GovernanceFinding finding(String severity, String findingType, String title, String description, AdminExperienceResponse.GovernanceItem item, Long rewardRuleId) {
        return AdminExperienceResponse.GovernanceFinding.builder()
                .severity(severity)
                .findingType(findingType)
                .title(title)
                .description(description)
                .sourceDomain(item.getSourceDomain())
                .ownerType(item.getOwnerType())
                .ownerId(item.getOwnerId())
                .flowId(item.getFlowId())
                .stepId(item.getStepId())
                .templateId(item.getTemplateId())
                .rewardRuleId(rewardRuleId)
                .itemKey(item.getItemKey())
                .build();
    }

    private List<AdminExperienceResponse.GovernanceUsageRef> buildUsageRefs(AdminExperienceResponse.GovernanceItem item) {
        return List.of(AdminExperienceResponse.GovernanceUsageRef.builder()
                .sourceDomain(item.getSourceDomain())
                .relationType(Boolean.TRUE.equals(item.getStoryOverride()) ? "story_override_target" : "default_experience_flow")
                .ownerType(item.getOwnerType())
                .ownerId(item.getOwnerId())
                .ownerName(item.getOwnerName())
                .flowId(item.getFlowId())
                .stepId(item.getStepId())
                .rewardRuleId("reward_rule".equals(item.getSourceDomain()) ? item.getStepId() : null)
                .indoorNodeId("indoor_node".equals(item.getOwnerType()) ? item.getOwnerId() : null)
                .description(item.getTemplateNameZh())
                .build());
    }

    private AdminExperienceResponse.GovernanceItem copyItemWithConflictCount(AdminExperienceResponse.GovernanceItem item, Long conflictCount) {
        return AdminExperienceResponse.GovernanceItem.builder()
                .itemKey(item.getItemKey())
                .sourceDomain(item.getSourceDomain())
                .ownerType(item.getOwnerType())
                .ownerId(item.getOwnerId())
                .ownerCode(item.getOwnerCode())
                .ownerName(item.getOwnerName())
                .cityId(item.getCityId())
                .subMapId(item.getSubMapId())
                .poiId(item.getPoiId())
                .indoorBuildingId(item.getIndoorBuildingId())
                .storylineId(item.getStorylineId())
                .storyChapterId(item.getStoryChapterId())
                .templateId(item.getTemplateId())
                .templateCode(item.getTemplateCode())
                .templateNameZh(item.getTemplateNameZh())
                .templateType(item.getTemplateType())
                .flowId(item.getFlowId())
                .flowCode(item.getFlowCode())
                .stepId(item.getStepId())
                .stepCode(item.getStepCode())
                .triggerType(item.getTriggerType())
                .effectFamily(item.getEffectFamily())
                .rewardType(item.getRewardType())
                .status(item.getStatus())
                .riskLevel(item.getRiskLevel())
                .storyOverride(item.getStoryOverride())
                .conflictCount(conflictCount)
                .build();
    }

    private String resolveBehaviorTrigger(IndoorNodeBehavior behavior) {
        if (contains(behavior.getTriggerRulesJson(), "proximity")) {
            return "proximity";
        }
        if (contains(behavior.getTriggerRulesJson(), "dwell")) {
            return "dwell";
        }
        if (contains(behavior.getTriggerTemplateCode(), "sequence")) {
            return "tap_sequence";
        }
        return "tap";
    }

    private String resolveEffectFamily(String payload, String category, String fallback) {
        String joined = (nullSafe(payload) + " " + nullSafe(category) + " " + nullSafe(fallback)).toLowerCase(Locale.ROOT);
        if (joined.contains("fullscreen")) return "fullscreen_media";
        if (joined.contains("lottie")) return "lottie_overlay";
        if (joined.contains("grant_collectible") || joined.contains("pickup")) return "grant_collectible";
        if (joined.contains("grant_badge") || joined.contains("grant_title")) return "grant_badge_title";
        if (joined.contains("grant_reward") || joined.contains("reward")) return "grant_game_reward";
        if (joined.contains("route")) return "route_guidance";
        if (joined.contains("modal") || joined.contains("popup")) return "rich_popup";
        return StringUtils.hasText(category) ? normalize(category) : normalize(fallback);
    }

    private String resolveRewardType(String rewardPayload, String effectPayload) {
        String joined = (nullSafe(rewardPayload) + " " + nullSafe(effectPayload)).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(joined.trim())) {
            return null;
        }
        if (joined.contains("badge") || joined.contains("title")) return "badge_title";
        if (joined.contains("collectible") || joined.contains("pickup")) return "collectible_pickup";
        if (joined.contains("coin")) return "coin";
        if (joined.contains("reward")) return "game_reward";
        return joined.length() > 48 ? joined.substring(0, 48) : joined;
    }

    private String itemKey(String sourceDomain, String ownerType, Long ownerId, String ownerCode, Long flowId, Long stepId, String stepCode) {
        return sourceDomain + ":" + nullSafe(ownerType) + ":" + (ownerId == null ? nullSafe(ownerCode) : ownerId) + ":" + (flowId == null ? "-" : flowId) + ":" + (stepId == null ? nullSafe(stepCode) : stepId);
    }

    private String ownerScope(AdminExperienceResponse.GovernanceItem item) {
        return nullSafe(item.getOwnerType()) + ":" + (item.getOwnerId() == null ? nullSafe(item.getOwnerCode()) : item.getOwnerId());
    }

    private boolean isHighRisk(String riskLevel) {
        return "high".equals(riskLevel) || "critical".equals(riskLevel);
    }

    private boolean contains(String value, String keyword) {
        return StringUtils.hasText(value) && StringUtils.hasText(keyword) && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private LambdaQueryWrapper<ExperienceTemplate> activeTemplateQuery() {
        return new LambdaQueryWrapper<ExperienceTemplate>().eq(ExperienceTemplate::getDeleted, 0);
    }

    private LambdaQueryWrapper<ExperienceFlow> activeFlowQuery() {
        return new LambdaQueryWrapper<ExperienceFlow>().eq(ExperienceFlow::getDeleted, 0);
    }

    private LambdaQueryWrapper<ExperienceFlowStep> activeStepQuery() {
        return new LambdaQueryWrapper<ExperienceFlowStep>().eq(ExperienceFlowStep::getDeleted, 0);
    }

    private LambdaQueryWrapper<ExperienceBinding> activeBindingQuery() {
        return new LambdaQueryWrapper<ExperienceBinding>().eq(ExperienceBinding::getDeleted, 0);
    }

    private LambdaQueryWrapper<ExperienceOverride> activeOverrideQuery() {
        return new LambdaQueryWrapper<ExperienceOverride>().eq(ExperienceOverride::getDeleted, 0);
    }
}
