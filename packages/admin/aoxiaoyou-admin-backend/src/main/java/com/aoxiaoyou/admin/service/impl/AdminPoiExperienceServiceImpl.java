package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminPoiExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiExperienceResponse;
import com.aoxiaoyou.admin.entity.ExperienceBinding;
import com.aoxiaoyou.admin.entity.ExperienceFlow;
import com.aoxiaoyou.admin.entity.ExperienceFlowStep;
import com.aoxiaoyou.admin.entity.ExperienceTemplate;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.admin.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.service.AdminPoiExperienceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPoiExperienceServiceImpl implements AdminPoiExperienceService {

    private static final String OWNER_TYPE_POI = "poi";
    private static final String BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW = "default_experience_flow";
    private static final String FLOW_TYPE_DEFAULT_POI = "default_poi";
    private static final String FLOW_MODE_WALK_IN = "walk_in";
    private static final String INHERIT_POLICY_INHERIT = "inherit";
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final Set<String> STATUSES = Set.of("draft", "published", "archived");
    private static final Set<String> STEP_TYPES = Set.of(
            "intro_modal",
            "route_guidance",
            "proximity_media",
            "checkin_task",
            "pickup",
            "hidden_challenge",
            "reward_grant",
            "custom"
    );
    private static final Set<String> TRIGGER_TYPES = Set.of(
            "manual",
            "tap",
            "tap_action",
            "proximity",
            "media_finished",
            "dwell",
            "story_mode_enter",
            "tap_sequence",
            "mixed",
            "compound",
            "content_complete",
            "task_complete",
            "pickup_complete"
    );
    private static final Set<String> WEIGHT_LEVELS = Set.of("tiny", "small", "medium", "large", "core");
    private static final Set<String> TEMPLATE_TYPES = Set.of(
            "presentation",
            "effect",
            "trigger_effect",
            "gameplay",
            "display_condition",
            "trigger_condition",
            "task_gameplay",
            "reward_presentation"
    );
    private static final Set<String> RISK_LEVELS = Set.of("low", "normal", "high", "critical");
    private static final Map<String, Integer> WEIGHT_LEVEL_VALUES = Map.of(
            "tiny", 1,
            "small", 2,
            "medium", 3,
            "large", 5,
            "core", 8
    );

    private final PoiMapper poiMapper;
    private final ExperienceFlowMapper flowMapper;
    private final ExperienceFlowStepMapper stepMapper;
    private final ExperienceBindingMapper bindingMapper;
    private final ExperienceTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AdminPoiExperienceResponse.Snapshot getDefaultExperience(Long poiId) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, null);
        ExperienceBinding binding = ensureDefaultBinding(poi, flow);
        return buildSnapshot(poi, flow, binding);
    }

    @Override
    @Transactional
    public AdminPoiExperienceResponse.Snapshot upsertDefaultFlow(Long poiId, AdminPoiExperienceRequest.FlowUpsert request) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, request);
        ExperienceBinding binding = ensureDefaultBinding(poi, flow);
        return buildSnapshot(poi, flow, binding);
    }

    @Override
    @Transactional
    public AdminPoiExperienceResponse.Step createStep(Long poiId, AdminPoiExperienceRequest.StepStructuredUpsert request) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, null);
        ensureDefaultBinding(poi, flow);
        ExperienceFlowStep step = new ExperienceFlowStep();
        step.setFlowId(flow.getId());
        applyStructuredStep(step, request);
        stepMapper.insert(step);
        return toStepResponse(requireStep(step.getId()), loadTemplatesById(Collections.singletonList(step.getTemplateId())));
    }

    @Override
    @Transactional
    public AdminPoiExperienceResponse.Step updateStep(Long poiId, Long stepId, AdminPoiExperienceRequest.StepStructuredUpsert request) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, null);
        ensureDefaultBinding(poi, flow);
        ExperienceFlowStep step = requireStepInFlow(flow, stepId);
        applyStructuredStep(step, request);
        stepMapper.updateById(step);
        return toStepResponse(requireStep(stepId), loadTemplatesById(Collections.singletonList(step.getTemplateId())));
    }

    @Override
    @Transactional
    public void deleteStep(Long poiId, Long stepId) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, null);
        ExperienceFlowStep step = requireStepInFlow(flow, stepId);
        step.setDeleted(1);
        stepMapper.updateById(step);
    }

    @Override
    @Transactional
    public AdminExperienceResponse.Template saveStepAsTemplate(
            Long poiId,
            Long stepId,
            AdminPoiExperienceRequest.SaveTemplateRequest request) {
        Poi poi = requirePoi(poiId);
        ExperienceFlow flow = loadOrCreateDefaultFlow(poi, null);
        ExperienceFlowStep step = requireStepInFlow(flow, stepId);

        String templateCode = defaultCode(request.getCode(), "tpl_poi_" + step.getStepCode());
        ExperienceTemplate template = templateMapper.selectOne(activeTemplateQuery().eq(ExperienceTemplate::getCode, templateCode));
        if (template == null) {
            template = new ExperienceTemplate();
        }
        template.setCode(templateCode);
        template.setTemplateType(defaultAllowedCode(request.getTemplateType(), deriveTemplateType(step.getStepType()), TEMPLATE_TYPES, "templateType"));
        template.setCategory(defaultCode(request.getCategory(), "poi_default_experience"));
        template.setNameZh(defaultText(request.getNameZh(), step.getStepNameZh()));
        template.setNameZht(defaultText(request.getNameZht(), step.getStepNameZht()));
        template.setNameEn(trim(request.getNameEn()));
        template.setNamePt(trim(request.getNamePt()));
        template.setSummaryZh(defaultText(request.getSummaryZh(), step.getDescriptionZh()));
        template.setSummaryZht(defaultText(request.getSummaryZht(), step.getDescriptionZht()));
        template.setSummaryEn(trim(request.getSummaryEn()));
        template.setSummaryPt(trim(request.getSummaryPt()));
        template.setConfigJson(writeTemplateConfig(step));
        template.setSchemaJson(writeJson(versionedMap(Map.of("templateKind", "poi_step", "source", "poi_default_flow"))));
        template.setRiskLevel(defaultAllowedCode(request.getRiskLevel(), "normal", RISK_LEVELS, "riskLevel"));
        template.setStatus(defaultAllowedCode(request.getStatus(), STATUS_DRAFT, STATUSES, "status"));
        template.setSortOrder(defaultNumber(request.getSortOrder()));
        template.setDeleted(0);
        if (template.getId() == null) {
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
        }

        step.setTemplateId(template.getId());
        stepMapper.updateById(step);
        return toTemplateResponse(requireTemplate(template.getId()));
    }

    private ExperienceFlow loadOrCreateDefaultFlow(Poi poi, AdminPoiExperienceRequest.FlowUpsert request) {
        ExperienceFlow flow = null;
        ExperienceBinding binding = findDefaultBinding(poi.getId());
        if (binding != null) {
            flow = selectActiveFlow(binding.getFlowId());
        }
        if (flow == null) {
            flow = flowMapper.selectOne(activeFlowQuery().eq(ExperienceFlow::getCode, defaultFlowCode(poi.getId())));
        }

        boolean creating = flow == null;
        if (creating) {
            flow = new ExperienceFlow();
        }

        if (creating || request != null) {
            applyDefaultFlow(flow, poi, request);
            if (creating) {
                flowMapper.insert(flow);
            } else {
                flowMapper.updateById(flow);
            }
            flow = selectActiveFlow(flow.getId());
        }
        return flow;
    }

    private void applyDefaultFlow(ExperienceFlow flow, Poi poi, AdminPoiExperienceRequest.FlowUpsert request) {
        String status = request == null
                ? defaultAllowedCode(flow.getStatus(), STATUS_DRAFT, STATUSES, "status")
                : defaultAllowedCode(request.getStatus(), defaultText(flow.getStatus(), STATUS_DRAFT), STATUSES, "status");
        LocalDateTime publishedAt = request == null ? flow.getPublishedAt() : parseDateTime(request.getPublishedAt());
        flow.setCode(request == null ? defaultText(flow.getCode(), defaultFlowCode(poi.getId())) : defaultCode(request.getCode(), defaultFlowCode(poi.getId())));
        flow.setFlowType(FLOW_TYPE_DEFAULT_POI);
        flow.setMode(FLOW_MODE_WALK_IN);
        flow.setNameZh(request == null ? defaultText(flow.getNameZh(), defaultPoiFlowName(poi)) : defaultText(request.getNameZh(), defaultPoiFlowName(poi)));
        flow.setNameZht(request == null ? defaultText(flow.getNameZht(), defaultPoiFlowName(poi)) : defaultText(request.getNameZht(), defaultPoiFlowName(poi)));
        flow.setNameEn(request == null ? trim(flow.getNameEn()) : trim(request.getNameEn()));
        flow.setNamePt(request == null ? trim(flow.getNamePt()) : trim(request.getNamePt()));
        flow.setDescriptionZh(request == null ? defaultText(flow.getDescriptionZh(), defaultPoiFlowDescription(poi)) : trim(request.getDescriptionZh()));
        flow.setDescriptionZht(request == null ? defaultText(flow.getDescriptionZht(), defaultPoiFlowDescription(poi)) : trim(request.getDescriptionZht()));
        flow.setDescriptionEn(request == null ? trim(flow.getDescriptionEn()) : trim(request.getDescriptionEn()));
        flow.setDescriptionPt(request == null ? trim(flow.getDescriptionPt()) : trim(request.getDescriptionPt()));
        flow.setMapPolicyJson(request == null ? validateVersionedJson(flow.getMapPolicyJson(), "mapPolicyJson") : validateVersionedJson(request.getMapPolicyJson(), "mapPolicyJson"));
        flow.setAdvancedConfigJson(request == null ? validateVersionedJson(flow.getAdvancedConfigJson(), "advancedConfigJson") : validateVersionedJson(request.getAdvancedConfigJson(), "advancedConfigJson"));
        flow.setStatus(status);
        flow.setSortOrder(request == null ? defaultNumber(flow.getSortOrder()) : defaultNumber(request.getSortOrder()));
        flow.setPublishedAt(STATUS_PUBLISHED.equals(status) ? (publishedAt == null ? LocalDateTime.now() : publishedAt) : null);
        flow.setDeleted(0);
    }

    private ExperienceBinding ensureDefaultBinding(Poi poi, ExperienceFlow flow) {
        List<ExperienceBinding> bindings = bindingMapper.selectList(activeBindingQuery()
                .eq(ExperienceBinding::getOwnerType, OWNER_TYPE_POI)
                .eq(ExperienceBinding::getOwnerId, poi.getId())
                .eq(ExperienceBinding::getBindingRole, BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW)
                .orderByAsc(ExperienceBinding::getPriority)
                .orderByAsc(ExperienceBinding::getSortOrder)
                .orderByAsc(ExperienceBinding::getId));

        ExperienceBinding binding = bindings.isEmpty() ? new ExperienceBinding() : bindings.get(0);
        binding.setOwnerType(OWNER_TYPE_POI);
        binding.setOwnerId(poi.getId());
        binding.setOwnerCode(defaultText(poi.getCode(), ""));
        binding.setBindingRole(BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW);
        binding.setFlowId(flow.getId());
        binding.setPriority(0);
        binding.setInheritPolicy(INHERIT_POLICY_INHERIT);
        binding.setStatus(defaultAllowedCode(flow.getStatus(), STATUS_DRAFT, STATUSES, "status"));
        binding.setSortOrder(0);
        binding.setDeleted(0);
        if (binding.getId() == null) {
            bindingMapper.insert(binding);
        } else {
            bindingMapper.updateById(binding);
        }

        for (int i = 1; i < bindings.size(); i += 1) {
            ExperienceBinding duplicate = bindings.get(i);
            duplicate.setDeleted(1);
            bindingMapper.updateById(duplicate);
        }
        return requireBinding(binding.getId());
    }

    private void applyStructuredStep(ExperienceFlowStep step, AdminPoiExperienceRequest.StepStructuredUpsert request) {
        if (request.getTemplateId() != null) {
            requireTemplate(request.getTemplateId());
        }
        String stepType = requireAllowedCode(request.getStepType(), "custom", STEP_TYPES, "stepType");
        String triggerType = defaultAllowedCode(request.getTriggerType(), defaultTriggerType(stepType), TRIGGER_TYPES, "triggerType");
        String weightLevel = defaultAllowedCode(request.getExplorationWeightLevel(), defaultWeightLevel(stepType), WEIGHT_LEVELS, "explorationWeightLevel");
        String status = defaultAllowedCode(request.getStatus(), STATUS_DRAFT, STATUSES, "status");

        step.setStepCode(defaultCode(request.getStepCode(), fallbackStepCode(stepType, step.getId())));
        step.setStepType(stepType);
        step.setTemplateId(request.getTemplateId());
        step.setStepNameZh(defaultText(request.getStepNameZh(), defaultStepName(stepType)));
        step.setStepNameZht(defaultText(request.getStepNameZht(), defaultText(request.getStepNameZh(), defaultStepName(stepType))));
        step.setStepNameEn(trim(request.getStepNameEn()));
        step.setStepNamePt(trim(request.getStepNamePt()));
        step.setDescriptionZh(trim(request.getDescriptionZh()));
        step.setDescriptionZht(defaultText(request.getDescriptionZht(), trim(request.getDescriptionZh())));
        step.setDescriptionEn(trim(request.getDescriptionEn()));
        step.setDescriptionPt(trim(request.getDescriptionPt()));
        step.setTriggerType(triggerType);
        step.setTriggerConfigJson(resolveTriggerConfigJson(request, triggerType));
        step.setConditionConfigJson(resolveConditionConfigJson(request));
        step.setEffectConfigJson(resolveEffectConfigJson(request));
        step.setMediaAssetId(request.getMediaAssetId());
        step.setRewardRuleIdsJson(writeRewardRuleIds(request.getRewardRuleIds()));
        step.setExplorationWeightLevel(weightLevel);
        step.setRequiredForCompletion(Boolean.TRUE.equals(request.getRequiredForCompletion()));
        step.setInheritKey(defaultText(request.getAfterStepCode(), ""));
        step.setStatus(status);
        step.setSortOrder(defaultNumber(request.getSortOrder()));
        step.setDeleted(0);
    }

    private String resolveTriggerConfigJson(AdminPoiExperienceRequest.StepStructuredUpsert request, String triggerType) {
        validateAdvancedJsonFlag(request);
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled()) && StringUtils.hasText(request.getAdvancedTriggerConfigJson())) {
            return validateVersionedJson(request.getAdvancedTriggerConfigJson(), "triggerConfigJson");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        putText(values, "preset", request.getTriggerPreset());
        values.put("triggerType", triggerType);
        putValue(values, "radiusMeters", request.getTriggerRadiusMeters());
        putValue(values, "dwellSeconds", request.getDwellSeconds());
        putText(values, "tapActionCode", request.getTapActionCode());
        putText(values, "afterStepCode", request.getAfterStepCode());
        return writeJson(versionedMap(values));
    }

    private String resolveConditionConfigJson(AdminPoiExperienceRequest.StepStructuredUpsert request) {
        validateAdvancedJsonFlag(request);
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled()) && StringUtils.hasText(request.getAdvancedConditionConfigJson())) {
            return validateVersionedJson(request.getAdvancedConditionConfigJson(), "conditionConfigJson");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        putText(values, "preset", request.getConditionPreset());
        putValue(values, "oncePerUser", Boolean.TRUE.equals(request.getOncePerUser()));
        putText(values, "timeWindowStart", request.getTimeWindowStart());
        putText(values, "timeWindowEnd", request.getTimeWindowEnd());
        putList(values, "requiredItemCodes", normalizeCodeList(request.getRequiredItemCodes()));
        putList(values, "requiredBadgeCodes", normalizeCodeList(request.getRequiredBadgeCodes()));
        return writeJson(versionedMap(values));
    }

    private String resolveEffectConfigJson(AdminPoiExperienceRequest.StepStructuredUpsert request) {
        validateAdvancedJsonFlag(request);
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled()) && StringUtils.hasText(request.getAdvancedEffectConfigJson())) {
            return validateVersionedJson(request.getAdvancedEffectConfigJson(), "effectConfigJson");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        putText(values, "preset", request.getEffectPreset());
        putText(values, "modalTitle", request.getModalTitle());
        putText(values, "modalBody", request.getModalBody());
        putText(values, "primaryActionLabel", request.getPrimaryActionLabel());
        putList(values, "routeCardTypes", normalizeCodeList(request.getRouteCardTypes()));
        putList(values, "taskCodes", normalizeCodeList(request.getTaskCodes()));
        putList(values, "pickupCodes", normalizeCodeList(request.getPickupCodes()));
        putValue(values, "rewardRuleIds", request.getRewardRuleIds());
        putText(values, "rewardSummary", request.getRewardSummary());
        putValue(values, "mediaAssetId", request.getMediaAssetId());
        putValue(values, "fullScreenMediaAssetId", request.getFullScreenMediaAssetId());
        putValue(values, "audioAssetId", request.getAudioAssetId());
        return writeJson(versionedMap(values));
    }

    private void validateAdvancedJsonFlag(AdminPoiExperienceRequest.StepStructuredUpsert request) {
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled())) {
            return;
        }
        if (StringUtils.hasText(request.getAdvancedTriggerConfigJson())
                || StringUtils.hasText(request.getAdvancedConditionConfigJson())
                || StringUtils.hasText(request.getAdvancedEffectConfigJson())) {
            throw new BusinessException(4002, "advanced JSON fields require advancedJsonEnabled=true");
        }
    }

    private AdminPoiExperienceResponse.Snapshot buildSnapshot(Poi poi, ExperienceFlow flow, ExperienceBinding binding) {
        List<ExperienceFlowStep> steps = stepMapper.selectList(activeStepQuery()
                .eq(ExperienceFlowStep::getFlowId, flow.getId())
                .orderByAsc(ExperienceFlowStep::getSortOrder)
                .orderByAsc(ExperienceFlowStep::getId));
        Map<Long, ExperienceTemplate> templatesById = loadTemplatesById(steps.stream().map(ExperienceFlowStep::getTemplateId).toList());
        List<AdminPoiExperienceResponse.Step> stepResponses = steps.stream()
                .map(step -> toStepResponse(step, templatesById))
                .toList();
        return AdminPoiExperienceResponse.Snapshot.builder()
                .poi(toPoiSummary(poi))
                .flow(toFlowResponse(flow))
                .binding(toBindingResponse(binding))
                .steps(stepResponses)
                .templates(loadAvailableTemplates())
                .validationFindings(buildValidationFindings(flow, binding, stepResponses))
                .publicRuntimePath("/api/v1/experience/poi/" + poi.getId())
                .build();
    }

    private List<AdminPoiExperienceResponse.ValidationFinding> buildValidationFindings(
            ExperienceFlow flow,
            ExperienceBinding binding,
            List<AdminPoiExperienceResponse.Step> steps) {
        List<AdminPoiExperienceResponse.ValidationFinding> findings = new ArrayList<>();
        if (steps.isEmpty()) {
            findings.add(finding("warning", "empty_flow", "尚未配置體驗步驟", "此 POI 已有預設流程，但還沒有任何步驟，前台只能讀到空流程。", null));
        }
        if (!STATUS_PUBLISHED.equals(flow.getStatus())) {
            findings.add(finding("info", "flow_not_published", "流程尚未發布", "草稿流程可在後台編輯，但公開 runtime 只會消費已發布流程。", null));
        }
        if (!STATUS_PUBLISHED.equals(binding.getStatus())) {
            findings.add(finding("info", "binding_not_published", "綁定尚未發布", "若要讓小程序公開讀取，流程與 POI 綁定都需要發布。", null));
        }
        for (AdminPoiExperienceResponse.Step step : steps) {
            if ("proximity_media".equals(step.getStepType()) && step.getMediaAssetId() == null && !contains(step.getEffectConfigJson(), "fullScreenMediaAssetId")) {
                findings.add(finding("warning", "missing_proximity_media", "抵達媒體缺少資源", "抵達播放類步驟應配置主媒體、全屏媒體或音訊資源。", step));
            }
            if ("reward_grant".equals(step.getStepType()) && !StringUtils.hasText(step.getRewardRuleIdsJson()) && !contains(step.getEffectConfigJson(), "rewardSummary")) {
                findings.add(finding("warning", "missing_reward", "獎勵步驟缺少獎勵規則", "完成與獎勵步驟應配置 rewardRuleIds 或可讀的獎勵摘要。", step));
            }
            if (STATUS_PUBLISHED.equals(flow.getStatus()) && !STATUS_PUBLISHED.equals(step.getStatus())) {
                findings.add(finding("info", "step_not_published", "存在未發布步驟", "公開 runtime 可能不會輸出此步驟。", step));
            }
        }
        return findings;
    }

    private AdminPoiExperienceResponse.ValidationFinding finding(
            String severity,
            String findingType,
            String title,
            String description,
            AdminPoiExperienceResponse.Step step) {
        return AdminPoiExperienceResponse.ValidationFinding.builder()
                .severity(severity)
                .findingType(findingType)
                .title(title)
                .description(description)
                .stepId(step == null ? null : step.getId())
                .stepCode(step == null ? null : step.getStepCode())
                .build();
    }

    private AdminPoiExperienceResponse.PoiSummary toPoiSummary(Poi poi) {
        return AdminPoiExperienceResponse.PoiSummary.builder()
                .poiId(poi.getId())
                .cityId(poi.getCityId())
                .subMapId(poi.getSubMapId())
                .code(poi.getCode())
                .nameZh(poi.getNameZh())
                .nameZht(poi.getNameZht())
                .nameEn(poi.getNameEn())
                .namePt(poi.getNamePt())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .triggerRadius(poi.getTriggerRadius())
                .manualCheckinRadius(poi.getManualCheckinRadius())
                .staySeconds(poi.getStaySeconds())
                .coverAssetId(poi.getCoverAssetId())
                .mapIconAssetId(poi.getMapIconAssetId())
                .audioAssetId(poi.getAudioAssetId())
                .status(poi.getStatus())
                .build();
    }

    private AdminPoiExperienceResponse.Flow toFlowResponse(ExperienceFlow flow) {
        return AdminPoiExperienceResponse.Flow.builder()
                .id(flow.getId())
                .code(flow.getCode())
                .flowType(flow.getFlowType())
                .mode(flow.getMode())
                .nameZh(flow.getNameZh())
                .nameZht(flow.getNameZht())
                .nameEn(flow.getNameEn())
                .namePt(flow.getNamePt())
                .descriptionZh(flow.getDescriptionZh())
                .descriptionZht(flow.getDescriptionZht())
                .descriptionEn(flow.getDescriptionEn())
                .descriptionPt(flow.getDescriptionPt())
                .mapPolicyJson(flow.getMapPolicyJson())
                .advancedConfigJson(flow.getAdvancedConfigJson())
                .status(flow.getStatus())
                .sortOrder(flow.getSortOrder())
                .publishedAt(flow.getPublishedAt())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .build();
    }

    private AdminPoiExperienceResponse.Binding toBindingResponse(ExperienceBinding binding) {
        return AdminPoiExperienceResponse.Binding.builder()
                .id(binding.getId())
                .ownerType(binding.getOwnerType())
                .ownerId(binding.getOwnerId())
                .ownerCode(binding.getOwnerCode())
                .bindingRole(binding.getBindingRole())
                .flowId(binding.getFlowId())
                .priority(binding.getPriority())
                .inheritPolicy(binding.getInheritPolicy())
                .status(binding.getStatus())
                .sortOrder(binding.getSortOrder())
                .createdAt(binding.getCreatedAt())
                .updatedAt(binding.getUpdatedAt())
                .build();
    }

    private AdminPoiExperienceResponse.Step toStepResponse(ExperienceFlowStep step, Map<Long, ExperienceTemplate> templatesById) {
        ExperienceTemplate template = step.getTemplateId() == null ? null : templatesById.get(step.getTemplateId());
        return AdminPoiExperienceResponse.Step.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .templateId(step.getTemplateId())
                .template(template == null ? null : toTemplateResponse(template))
                .stepNameZh(step.getStepNameZh())
                .stepNameZht(step.getStepNameZht())
                .stepNameEn(step.getStepNameEn())
                .stepNamePt(step.getStepNamePt())
                .descriptionZh(step.getDescriptionZh())
                .descriptionZht(step.getDescriptionZht())
                .descriptionEn(step.getDescriptionEn())
                .descriptionPt(step.getDescriptionPt())
                .triggerType(step.getTriggerType())
                .triggerConfigJson(step.getTriggerConfigJson())
                .conditionConfigJson(step.getConditionConfigJson())
                .effectConfigJson(step.getEffectConfigJson())
                .mediaAssetId(step.getMediaAssetId())
                .rewardRuleIdsJson(step.getRewardRuleIdsJson())
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .status(step.getStatus())
                .sortOrder(step.getSortOrder())
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .build();
    }

    private AdminExperienceResponse.Template toTemplateResponse(ExperienceTemplate template) {
        Long usageCount = template.getId() == null ? 0 : stepMapper.selectCount(activeStepQuery().eq(ExperienceFlowStep::getTemplateId, template.getId()));
        return AdminExperienceResponse.Template.builder()
                .id(template.getId())
                .code(template.getCode())
                .templateType(template.getTemplateType())
                .category(template.getCategory())
                .nameZh(template.getNameZh())
                .nameEn(template.getNameEn())
                .nameZht(template.getNameZht())
                .namePt(template.getNamePt())
                .summaryZh(template.getSummaryZh())
                .summaryEn(template.getSummaryEn())
                .summaryZht(template.getSummaryZht())
                .summaryPt(template.getSummaryPt())
                .configJson(template.getConfigJson())
                .schemaJson(template.getSchemaJson())
                .riskLevel(template.getRiskLevel())
                .status(template.getStatus())
                .sortOrder(template.getSortOrder())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .usageCount(usageCount == null ? 0 : usageCount)
                .build();
    }

    private List<AdminExperienceResponse.Template> loadAvailableTemplates() {
        return templateMapper.selectList(activeTemplateQuery()
                        .orderByAsc(ExperienceTemplate::getCategory)
                        .orderByAsc(ExperienceTemplate::getSortOrder)
                        .orderByDesc(ExperienceTemplate::getUpdatedAt))
                .stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    private Map<Long, ExperienceTemplate> loadTemplatesById(List<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return templateMapper.selectList(activeTemplateQuery().in(ExperienceTemplate::getId, normalizedIds)).stream()
                .collect(Collectors.toMap(ExperienceTemplate::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private ExperienceFlowStep requireStepInFlow(ExperienceFlow flow, Long stepId) {
        ExperienceFlowStep step = requireStep(stepId);
        if (!Objects.equals(step.getFlowId(), flow.getId())) {
            throw new BusinessException(4073, "Experience step not found in POI default flow");
        }
        return step;
    }

    private ExperienceFlowStep requireStep(Long stepId) {
        ExperienceFlowStep step = stepMapper.selectOne(activeStepQuery().eq(ExperienceFlowStep::getId, stepId));
        if (step == null) {
            throw new BusinessException(4073, "Experience step not found");
        }
        return step;
    }

    private ExperienceTemplate requireTemplate(Long templateId) {
        ExperienceTemplate template = templateMapper.selectOne(activeTemplateQuery().eq(ExperienceTemplate::getId, templateId));
        if (template == null) {
            throw new BusinessException(4070, "Experience template not found");
        }
        return template;
    }

    private ExperienceBinding requireBinding(Long bindingId) {
        ExperienceBinding binding = bindingMapper.selectOne(activeBindingQuery().eq(ExperienceBinding::getId, bindingId));
        if (binding == null) {
            throw new BusinessException(4074, "Experience binding not found");
        }
        return binding;
    }

    private Poi requirePoi(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "POI not found");
        }
        return poi;
    }

    private ExperienceFlow selectActiveFlow(Long flowId) {
        if (flowId == null) {
            return null;
        }
        return flowMapper.selectOne(activeFlowQuery().eq(ExperienceFlow::getId, flowId));
    }

    private ExperienceBinding findDefaultBinding(Long poiId) {
        return bindingMapper.selectOne(activeBindingQuery()
                .eq(ExperienceBinding::getOwnerType, OWNER_TYPE_POI)
                .eq(ExperienceBinding::getOwnerId, poiId)
                .eq(ExperienceBinding::getBindingRole, BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW)
                .orderByAsc(ExperienceBinding::getPriority)
                .orderByAsc(ExperienceBinding::getSortOrder)
                .orderByAsc(ExperienceBinding::getId)
                .last("LIMIT 1"));
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

    private LambdaQueryWrapper<ExperienceTemplate> activeTemplateQuery() {
        return new LambdaQueryWrapper<ExperienceTemplate>().eq(ExperienceTemplate::getDeleted, 0);
    }

    private String validateVersionedJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        JsonNode node = readJson(value, fieldName);
        JsonNode schemaVersion = node.path("schemaVersion");
        if (!node.isObject() || schemaVersion.isMissingNode() || !schemaVersion.canConvertToInt() || schemaVersion.asInt() <= 0) {
            throw new BusinessException(4002, fieldName + " must be a JSON object with schemaVersion");
        }
        return value.trim();
    }

    private JsonNode readJson(String value, String fieldName) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception ex) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private String writeTemplateConfig(ExperienceFlowStep step) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("source", "poi_default_flow_step");
        values.put("sourceStepId", step.getId());
        values.put("stepCode", step.getStepCode());
        values.put("stepType", step.getStepType());
        putJson(values, "triggerConfig", step.getTriggerConfigJson());
        putJson(values, "conditionConfig", step.getConditionConfigJson());
        putJson(values, "effectConfig", step.getEffectConfigJson());
        putJson(values, "rewardRuleIds", step.getRewardRuleIdsJson());
        return writeJson(versionedMap(values));
    }

    private String writeRewardRuleIds(List<Long> rewardRuleIds) {
        if (rewardRuleIds == null || rewardRuleIds.isEmpty()) {
            return null;
        }
        return writeJson(rewardRuleIds.stream().filter(Objects::nonNull).distinct().toList());
    }

    private Map<String, Object> versionedMap(Map<String, Object> values) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schemaVersion", 1);
        result.putAll(values);
        return result;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(4002, "JSON serialization failed");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private String requireAllowedCode(String value, String fallback, Set<String> allowedCodes, String fieldName) {
        String normalized = defaultCode(value, fallback);
        if (!allowedCodes.contains(normalized)) {
            throw new BusinessException(4002, fieldName + " must use the canonical experience vocabulary");
        }
        return normalized;
    }

    private String defaultAllowedCode(String value, String fallback, Set<String> allowedCodes, String fieldName) {
        String normalized = defaultCode(value, fallback);
        if (!allowedCodes.contains(normalized)) {
            throw new BusinessException(4002, fieldName + " must use the canonical experience vocabulary");
        }
        return normalized;
    }

    private String defaultCode(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT).replace('-', '_') : fallback;
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private List<String> normalizeCodeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT).replace('-', '_'))
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private void putText(Map<String, Object> values, String key, String value) {
        if (StringUtils.hasText(value)) {
            values.put(key, value.trim());
        }
    }

    private void putValue(Map<String, Object> values, String key, Object value) {
        if (value != null) {
            values.put(key, value);
        }
    }

    private void putList(Map<String, Object> values, String key, List<?> value) {
        if (value != null && !value.isEmpty()) {
            values.put(key, value);
        }
    }

    private void putJson(Map<String, Object> values, String key, String json) {
        if (StringUtils.hasText(json)) {
            values.put(key, readJson(json, key));
        }
    }

    private String defaultFlowCode(Long poiId) {
        return "poi_" + poiId + "_default_walk_in";
    }

    private String defaultPoiFlowName(Poi poi) {
        return defaultText(poi.getNameZht(), defaultText(poi.getNameZh(), defaultText(poi.getCode(), "POI"))) + "預設地點體驗";
    }

    private String defaultPoiFlowDescription(Poi poi) {
        return "自然 walk-in 與點擊前往時使用的 POI 預設體驗流程。POI=" + defaultText(poi.getCode(), String.valueOf(poi.getId()));
    }

    private String fallbackStepCode(String stepType, Long stepId) {
        return "step_" + stepType + "_" + (stepId == null ? System.currentTimeMillis() : stepId);
    }

    private String defaultStepName(String stepType) {
        return switch (stepType) {
            case "intro_modal" -> "點擊介紹彈窗";
            case "route_guidance" -> "前往探索與路線建議";
            case "proximity_media" -> "抵達範圍全屏媒體";
            case "checkin_task" -> "打卡任務釋放";
            case "pickup" -> "支線拾取物";
            case "hidden_challenge" -> "隱藏停留成就";
            case "reward_grant" -> "完成獎勵與稱號";
            default -> "自定義體驗步驟";
        };
    }

    private String defaultTriggerType(String stepType) {
        return switch (stepType) {
            case "intro_modal" -> "tap";
            case "route_guidance" -> "tap_action";
            case "proximity_media" -> "proximity";
            case "checkin_task" -> "media_finished";
            case "pickup" -> "tap";
            case "hidden_challenge" -> "dwell";
            case "reward_grant" -> "task_complete";
            default -> "manual";
        };
    }

    private String defaultWeightLevel(String stepType) {
        return switch (stepType) {
            case "intro_modal", "route_guidance" -> "tiny";
            case "proximity_media" -> "small";
            case "checkin_task" -> "medium";
            case "pickup" -> "large";
            case "hidden_challenge", "reward_grant" -> "core";
            default -> "small";
        };
    }

    private String deriveTemplateType(String stepType) {
        return switch (stepType) {
            case "intro_modal", "route_guidance", "proximity_media" -> "presentation";
            case "checkin_task", "hidden_challenge" -> "task_gameplay";
            case "pickup" -> "trigger_effect";
            case "reward_grant" -> "reward_presentation";
            default -> "trigger_effect";
        };
    }

    private boolean contains(String value, String needle) {
        return StringUtils.hasText(value) && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    @SuppressWarnings("unused")
    private int weightValue(String weightLevel) {
        return WEIGHT_LEVEL_VALUES.getOrDefault(defaultCode(weightLevel, "small"), 2);
    }
}
