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
import com.aoxiaoyou.admin.entity.ExplorationElement;
import com.aoxiaoyou.admin.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.admin.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.admin.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.admin.mapper.ExplorationElementMapper;
import com.aoxiaoyou.admin.service.AdminExperienceOrchestrationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminExperienceOrchestrationServiceImpl implements AdminExperienceOrchestrationService {

    private static final List<String> VERSIONED_JSON_FIELD_ORDER = List.of(
            "configJson",
            "schemaJson",
            "mapPolicyJson",
            "advancedConfigJson",
            "triggerConfigJson",
            "conditionConfigJson",
            "effectConfigJson",
            "overrideConfigJson",
            "metadataJson"
    );
    private static final Set<String> VERSIONED_JSON_FIELDS = Set.copyOf(VERSIONED_JSON_FIELD_ORDER);
    private static final List<String> TEMPLATE_TYPES = List.of(
            "presentation",
            "effect",
            "trigger_effect",
            "gameplay",
            "display_condition",
            "trigger_condition",
            "task_gameplay",
            "reward_presentation"
    );
    private static final List<String> FLOW_TYPES = List.of(
            "default_poi",
            "default_indoor_building",
            "default_indoor_floor",
            "default_indoor_node",
            "default_task",
            "default_marker",
            "default_overlay",
            "default_activity",
            "story_chapter_override",
            "manual_target"
    );
    private static final List<String> FLOW_MODES = List.of("walk_in", "story_mode", "manual");
    private static final List<String> OWNER_TYPES = List.of(
            "poi",
            "indoor_building",
            "indoor_floor",
            "indoor_node",
            "story_chapter",
            "task",
            "marker",
            "overlay",
            "activity",
            "manual_target"
    );
    private static final List<String> BINDING_ROLES = List.of("default_experience_flow", "story_override_flow");
    private static final List<String> INHERIT_POLICIES = List.of("inherit", "override");
    private static final List<String> OVERRIDE_MODES = List.of("inherit", "disable", "replace", "append");
    private static final List<String> TRIGGER_TYPES = List.of(
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
    private static final List<String> WEIGHT_LEVELS = List.of("tiny", "small", "medium", "large", "core");
    private static final List<String> STATUSES = List.of("draft", "published", "archived");
    private static final List<String> RISK_LEVELS = List.of("low", "normal", "high", "critical");
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final Map<String, Integer> WEIGHT_LEVEL_VALUES = Map.of(
            "tiny", 1,
            "small", 2,
            "medium", 3,
            "large", 5,
            "core", 8
    );
    private static final Map<String, String> TEMPLATE_TYPE_LABELS_ZH = Map.ofEntries(
            Map.entry("presentation", "展示呈現"),
            Map.entry("effect", "效果演出"),
            Map.entry("trigger_effect", "觸發效果"),
            Map.entry("gameplay", "互動玩法"),
            Map.entry("display_condition", "顯示條件"),
            Map.entry("trigger_condition", "觸發條件"),
            Map.entry("task_gameplay", "任務玩法"),
            Map.entry("reward_presentation", "獎勵演出")
    );
    private static final Map<String, String> FLOW_TYPE_LABELS_ZH = Map.ofEntries(
            Map.entry("default_poi", "地點預設流程"),
            Map.entry("default_indoor_building", "建築預設流程"),
            Map.entry("default_indoor_floor", "樓層預設流程"),
            Map.entry("default_indoor_node", "節點預設流程"),
            Map.entry("default_task", "任務預設流程"),
            Map.entry("default_marker", "標記預設流程"),
            Map.entry("default_overlay", "疊加物預設流程"),
            Map.entry("default_activity", "活動預設流程"),
            Map.entry("story_chapter_override", "故事章節覆寫流程"),
            Map.entry("manual_target", "手動目標流程")
    );
    private static final Map<String, String> FLOW_MODE_LABELS_ZH = Map.of(
            "walk_in", "自然抵達",
            "story_mode", "故事模式",
            "manual", "手動觸發"
    );
    private static final Map<String, String> OWNER_TYPE_LABELS_ZH = Map.ofEntries(
            Map.entry("poi", "地點"),
            Map.entry("indoor_building", "室內建築"),
            Map.entry("indoor_floor", "室內樓層"),
            Map.entry("indoor_node", "室內節點"),
            Map.entry("story_chapter", "故事章節"),
            Map.entry("task", "任務"),
            Map.entry("marker", "標記"),
            Map.entry("overlay", "疊加物"),
            Map.entry("activity", "活動"),
            Map.entry("manual_target", "手動錨點")
    );
    private static final Map<String, String> BINDING_ROLE_LABELS_ZH = Map.of(
            "default_experience_flow", "錨點預設體驗流程",
            "story_override_flow", "章節覆寫流程"
    );
    private static final Map<String, String> INHERIT_POLICY_LABELS_ZH = Map.of(
            "inherit", "沿用錨點預設",
            "override", "由章節覆寫接管"
    );
    private static final Map<String, String> OVERRIDE_MODE_LABELS_ZH = Map.of(
            "inherit", "沿用",
            "disable", "停用",
            "replace", "替換",
            "append", "附加"
    );
    private static final Map<String, String> OVERRIDE_MODE_GUIDANCE_ZH = Map.ofEntries(
            Map.entry("inherit", "保留錨點預設步驟，不額外插入新內容。"),
            Map.entry("disable", "停用繼承步驟，但仍保留其他流程上下文。"),
            Map.entry("replace", "以 replacementStepId 指向的新步驟取代既有步驟。"),
            Map.entry("append", "在既有步驟後附加 replacementStepId 指向的新步驟。")
    );
    private static final Map<String, String> TRIGGER_TYPE_LABELS_ZH = Map.ofEntries(
            Map.entry("manual", "手動"),
            Map.entry("tap", "點擊"),
            Map.entry("tap_action", "點擊按鈕"),
            Map.entry("proximity", "接近範圍"),
            Map.entry("media_finished", "媒體播放完成"),
            Map.entry("dwell", "停留時長"),
            Map.entry("story_mode_enter", "進入故事模式"),
            Map.entry("tap_sequence", "依序點擊"),
            Map.entry("mixed", "混合條件"),
            Map.entry("compound", "複合條件"),
            Map.entry("content_complete", "內容完成"),
            Map.entry("task_complete", "任務完成"),
            Map.entry("pickup_complete", "拾取完成")
    );
    private static final Map<String, String> WEIGHT_LEVEL_LABELS_ZH = Map.of(
            "tiny", "極少量",
            "small", "少量",
            "medium", "中量",
            "large", "大量",
            "core", "核心"
    );
    private static final Map<String, String> WEIGHT_LEVEL_GUIDANCE_ZH = Map.ofEntries(
            Map.entry("tiny", "只適用於提示型探索訊號，通常不作為章節主目標。"),
            Map.entry("small", "輕量探索進度，適合普通抵達與一般互動。"),
            Map.entry("medium", "中量探索進度，適合任務釋放與關鍵提示。"),
            Map.entry("large", "大量探索進度，適合支線收集與高價值事件。"),
            Map.entry("core", "核心探索進度，適合章節完成與關鍵隱藏挑戰。")
    );
    private static final Map<String, String> STATUS_LABELS_ZH = Map.of(
            "draft", "草稿",
            "published", "已發布",
            "archived", "已封存"
    );

    private final ExperienceTemplateMapper templateMapper;
    private final ExperienceFlowMapper flowMapper;
    private final ExperienceFlowStepMapper stepMapper;
    private final ExperienceBindingMapper bindingMapper;
    private final ExperienceOverrideMapper overrideMapper;
    private final ExplorationElementMapper explorationElementMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<AdminExperienceResponse.Template> pageTemplates(long pageNum, long pageSize, String keyword, String templateType, String status) {
        Page<ExperienceTemplate> page = templateMapper.selectPage(new Page<>(pageNum, pageSize),
                activeTemplateQuery()
                        .eq(StringUtils.hasText(templateType), ExperienceTemplate::getTemplateType, normalizeCode(templateType))
                        .eq(StringUtils.hasText(status), ExperienceTemplate::getStatus, normalizeCode(status))
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(ExperienceTemplate::getCode, keyword)
                                .or().like(ExperienceTemplate::getNameZh, keyword)
                                .or().like(ExperienceTemplate::getNameZht, keyword)
                                .or().like(ExperienceTemplate::getNameEn, keyword))
                        .orderByAsc(ExperienceTemplate::getSortOrder)
                        .orderByDesc(ExperienceTemplate::getUpdatedAt));
        Page<AdminExperienceResponse.Template> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toTemplateResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminExperienceResponse.Template createTemplate(AdminExperienceRequest.TemplateUpsert request) {
        ExperienceTemplate template = new ExperienceTemplate();
        applyTemplate(template, request);
        templateMapper.insert(template);
        return toTemplateResponse(requireTemplate(template.getId()));
    }

    @Override
    public AdminExperienceResponse.Template updateTemplate(Long templateId, AdminExperienceRequest.TemplateUpsert request) {
        ExperienceTemplate template = requireTemplate(templateId);
        applyTemplate(template, request);
        templateMapper.updateById(template);
        return toTemplateResponse(requireTemplate(templateId));
    }

    @Override
    public void deleteTemplate(Long templateId) {
        ExperienceTemplate template = requireTemplate(templateId);
        Long usageCount = stepMapper.selectCount(activeStepQuery().eq(ExperienceFlowStep::getTemplateId, templateId));
        if (usageCount != null && usageCount > 0) {
            throw new BusinessException(4071, "Experience template is still used by flow steps");
        }
        template.setDeleted(1);
        templateMapper.updateById(template);
    }

    @Override
    public PageResponse<AdminExperienceResponse.Flow> pageFlows(long pageNum, long pageSize, String keyword, String flowType, String status) {
        Page<ExperienceFlow> page = flowMapper.selectPage(new Page<>(pageNum, pageSize),
                activeFlowQuery()
                        .eq(StringUtils.hasText(flowType), ExperienceFlow::getFlowType, normalizeCode(flowType))
                        .eq(StringUtils.hasText(status), ExperienceFlow::getStatus, normalizeCode(status))
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(ExperienceFlow::getCode, keyword)
                                .or().like(ExperienceFlow::getNameZh, keyword)
                                .or().like(ExperienceFlow::getNameZht, keyword)
                                .or().like(ExperienceFlow::getNameEn, keyword))
                        .orderByAsc(ExperienceFlow::getSortOrder)
                        .orderByDesc(ExperienceFlow::getUpdatedAt));
        Page<AdminExperienceResponse.Flow> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(flow -> toFlowResponse(flow, false)).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminExperienceResponse.Flow getFlow(Long flowId) {
        return toFlowResponse(requireFlow(flowId), true);
    }

    @Override
    public AdminExperienceResponse.Flow createFlow(AdminExperienceRequest.FlowUpsert request) {
        ExperienceFlow flow = new ExperienceFlow();
        applyFlow(flow, request);
        flowMapper.insert(flow);
        return toFlowResponse(requireFlow(flow.getId()), true);
    }

    @Override
    public AdminExperienceResponse.Flow updateFlow(Long flowId, AdminExperienceRequest.FlowUpsert request) {
        ExperienceFlow flow = requireFlow(flowId);
        applyFlow(flow, request);
        flowMapper.updateById(flow);
        return toFlowResponse(requireFlow(flowId), true);
    }

    @Override
    public void deleteFlow(Long flowId) {
        ExperienceFlow flow = requireFlow(flowId);
        Long bindingCount = bindingMapper.selectCount(activeBindingQuery().eq(ExperienceBinding::getFlowId, flowId));
        if (bindingCount != null && bindingCount > 0) {
            throw new BusinessException(4072, "Experience flow is still bound to runtime owners");
        }
        flow.setDeleted(1);
        flowMapper.updateById(flow);
    }

    @Override
    public AdminExperienceResponse.Step createStep(Long flowId, AdminExperienceRequest.StepUpsert request) {
        requireFlow(flowId);
        ExperienceFlowStep step = new ExperienceFlowStep();
        step.setFlowId(flowId);
        applyStep(step, request);
        stepMapper.insert(step);
        return toStepResponse(requireStep(step.getId()), loadTemplatesById(Collections.singletonList(step.getTemplateId())));
    }

    @Override
    public AdminExperienceResponse.Step updateStep(Long flowId, Long stepId, AdminExperienceRequest.StepUpsert request) {
        requireFlow(flowId);
        ExperienceFlowStep step = requireStep(stepId);
        if (!Objects.equals(step.getFlowId(), flowId)) {
            throw new BusinessException(4073, "Experience step not found in flow");
        }
        applyStep(step, request);
        stepMapper.updateById(step);
        return toStepResponse(requireStep(stepId), loadTemplatesById(Collections.singletonList(step.getTemplateId())));
    }

    @Override
    public void deleteStep(Long flowId, Long stepId) {
        requireFlow(flowId);
        ExperienceFlowStep step = requireStep(stepId);
        if (!Objects.equals(step.getFlowId(), flowId)) {
            throw new BusinessException(4073, "Experience step not found in flow");
        }
        step.setDeleted(1);
        stepMapper.updateById(step);
    }

    @Override
    public PageResponse<AdminExperienceResponse.Binding> pageBindings(long pageNum, long pageSize, String ownerType, Long ownerId, String ownerCode) {
        Page<ExperienceBinding> page = bindingMapper.selectPage(new Page<>(pageNum, pageSize),
                activeBindingQuery()
                        .eq(StringUtils.hasText(ownerType), ExperienceBinding::getOwnerType, normalizeCode(ownerType))
                        .eq(ownerId != null, ExperienceBinding::getOwnerId, ownerId)
                        .eq(StringUtils.hasText(ownerCode), ExperienceBinding::getOwnerCode, trim(ownerCode))
                        .orderByAsc(ExperienceBinding::getPriority)
                        .orderByAsc(ExperienceBinding::getSortOrder));
        Map<Long, ExperienceFlow> flowsById = loadFlowsById(page.getRecords().stream().map(ExperienceBinding::getFlowId).toList());
        Page<AdminExperienceResponse.Binding> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(binding -> toBindingResponse(binding, flowsById)).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminExperienceResponse.Binding createBinding(AdminExperienceRequest.BindingUpsert request) {
        ExperienceBinding binding = new ExperienceBinding();
        applyBinding(binding, request);
        bindingMapper.insert(binding);
        return toBindingResponse(requireBinding(binding.getId()), loadFlowsById(List.of(binding.getFlowId())));
    }

    @Override
    public AdminExperienceResponse.Binding updateBinding(Long bindingId, AdminExperienceRequest.BindingUpsert request) {
        ExperienceBinding binding = requireBinding(bindingId);
        applyBinding(binding, request);
        bindingMapper.updateById(binding);
        return toBindingResponse(requireBinding(bindingId), loadFlowsById(List.of(binding.getFlowId())));
    }

    @Override
    public void deleteBinding(Long bindingId) {
        ExperienceBinding binding = requireBinding(bindingId);
        binding.setDeleted(1);
        bindingMapper.updateById(binding);
    }

    @Override
    public PageResponse<AdminExperienceResponse.OverrideRule> pageOverrides(long pageNum, long pageSize, String ownerType, Long ownerId) {
        Page<ExperienceOverride> page = overrideMapper.selectPage(new Page<>(pageNum, pageSize),
                activeOverrideQuery()
                        .eq(StringUtils.hasText(ownerType), ExperienceOverride::getOwnerType, normalizeCode(ownerType))
                        .eq(ownerId != null, ExperienceOverride::getOwnerId, ownerId)
                        .orderByAsc(ExperienceOverride::getSortOrder)
                        .orderByDesc(ExperienceOverride::getUpdatedAt));
        Page<AdminExperienceResponse.OverrideRule> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toOverrideResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminExperienceResponse.OverrideRule createOverride(AdminExperienceRequest.OverrideUpsert request) {
        ExperienceOverride overrideRule = new ExperienceOverride();
        applyOverride(overrideRule, request);
        overrideMapper.insert(overrideRule);
        return toOverrideResponse(requireOverride(overrideRule.getId()));
    }

    @Override
    public AdminExperienceResponse.OverrideRule updateOverride(Long overrideId, AdminExperienceRequest.OverrideUpsert request) {
        ExperienceOverride overrideRule = requireOverride(overrideId);
        applyOverride(overrideRule, request);
        overrideMapper.updateById(overrideRule);
        return toOverrideResponse(requireOverride(overrideId));
    }

    @Override
    public void deleteOverride(Long overrideId) {
        ExperienceOverride overrideRule = requireOverride(overrideId);
        overrideRule.setDeleted(1);
        overrideMapper.updateById(overrideRule);
    }

    @Override
    public PageResponse<AdminExperienceResponse.ExplorationElement> pageExplorationElements(
            long pageNum,
            long pageSize,
            String keyword,
            String ownerType,
            Long ownerId,
            Long cityId,
            Long subMapId,
            Long storylineId,
            String status) {
        Page<ExplorationElement> page = explorationElementMapper.selectPage(new Page<>(pageNum, pageSize),
                activeExplorationElementQuery()
                        .eq(StringUtils.hasText(ownerType), ExplorationElement::getOwnerType, normalizeCode(ownerType))
                        .eq(ownerId != null, ExplorationElement::getOwnerId, ownerId)
                        .eq(cityId != null, ExplorationElement::getCityId, cityId)
                        .eq(subMapId != null, ExplorationElement::getSubMapId, subMapId)
                        .eq(storylineId != null, ExplorationElement::getStorylineId, storylineId)
                        .eq(StringUtils.hasText(status), ExplorationElement::getStatus, normalizeCode(status))
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(ExplorationElement::getElementCode, keyword)
                                .or().like(ExplorationElement::getTitleZh, keyword)
                                .or().like(ExplorationElement::getTitleZht, keyword))
                        .orderByAsc(ExplorationElement::getSortOrder)
                        .orderByDesc(ExplorationElement::getUpdatedAt));
        Page<AdminExperienceResponse.ExplorationElement> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toExplorationElementResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminExperienceResponse.ExplorationElement createExplorationElement(AdminExperienceRequest.ExplorationElementUpsert request) {
        ExplorationElement element = new ExplorationElement();
        applyExplorationElement(element, request);
        explorationElementMapper.insert(element);
        return toExplorationElementResponse(requireExplorationElement(element.getId()));
    }

    @Override
    public AdminExperienceResponse.ExplorationElement updateExplorationElement(Long elementId, AdminExperienceRequest.ExplorationElementUpsert request) {
        ExplorationElement element = requireExplorationElement(elementId);
        applyExplorationElement(element, request);
        explorationElementMapper.updateById(element);
        return toExplorationElementResponse(requireExplorationElement(elementId));
    }

    @Override
    public void deleteExplorationElement(Long elementId) {
        ExplorationElement element = requireExplorationElement(elementId);
        element.setDeleted(1);
        explorationElementMapper.updateById(element);
    }

    @Override
    public AdminExperienceResponse.GovernanceOverview getGovernanceOverview() {
        List<ExperienceTemplate> templates = templateMapper.selectList(activeTemplateQuery());
        List<ExperienceFlow> flows = flowMapper.selectList(activeFlowQuery());
        List<ExperienceBinding> bindings = bindingMapper.selectList(activeBindingQuery());
        List<ExperienceOverride> overrides = overrideMapper.selectList(activeOverrideQuery());
        List<ExplorationElement> elements = explorationElementMapper.selectList(activeExplorationElementQuery());
        List<ExperienceFlowStep> steps = stepMapper.selectList(activeStepQuery());

        List<AdminExperienceResponse.GovernanceFinding> findings = buildGovernanceFindings(bindings, steps, overrides, elements);
        return AdminExperienceResponse.GovernanceOverview.builder()
                .templateCount(templates.size())
                .flowCount(flows.size())
                .bindingCount(bindings.size())
                .overrideCount(overrides.size())
                .explorationElementCount(elements.size())
                .highRiskTemplateCount(templates.stream().filter(template -> "high".equalsIgnoreCase(template.getRiskLevel())).count())
                .contractVocabulary(buildContractVocabulary())
                .operatorHints(buildOperatorHints())
                .findings(findings)
                .build();
    }

    private void applyTemplate(ExperienceTemplate template, AdminExperienceRequest.TemplateUpsert request) {
        String templateType = requireAllowedCode(request.getTemplateType(), "templateType", TEMPLATE_TYPES);
        template.setCode(resolveCode(request.getCode(), templateType, template.getId(), "tpl"));
        template.setTemplateType(templateType);
        template.setCategory(defaultCode(request.getCategory(), "general"));
        template.setNameZh(trim(request.getNameZh()));
        template.setNameEn(trim(request.getNameEn()));
        template.setNameZht(trim(request.getNameZht()));
        template.setNamePt(trim(request.getNamePt()));
        template.setSummaryZh(trim(request.getSummaryZh()));
        template.setSummaryEn(trim(request.getSummaryEn()));
        template.setSummaryZht(trim(request.getSummaryZht()));
        template.setSummaryPt(trim(request.getSummaryPt()));
        template.setConfigJson(validateVersionedJson(request.getConfigJson(), "configJson"));
        template.setSchemaJson(validateVersionedJson(request.getSchemaJson(), "schemaJson"));
        template.setRiskLevel(defaultAllowedCode(request.getRiskLevel(), "riskLevel", "normal", RISK_LEVELS));
        template.setStatus(defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES));
        template.setSortOrder(defaultNumber(request.getSortOrder()));
        template.setDeleted(0);
    }

    private void applyFlow(ExperienceFlow flow, AdminExperienceRequest.FlowUpsert request) {
        String flowType = defaultAllowedCode(request.getFlowType(), "flowType", "default_poi", FLOW_TYPES);
        String flowMode = defaultAllowedCode(request.getMode(), "mode", "walk_in", FLOW_MODES);
        String status = defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES);
        LocalDateTime publishedAt = parseDateTime(request.getPublishedAt());
        flow.setCode(resolveCode(request.getCode(), flowType, flow.getId(), "flow"));
        flow.setFlowType(flowType);
        flow.setMode(flowMode);
        flow.setNameZh(trim(request.getNameZh()));
        flow.setNameEn(trim(request.getNameEn()));
        flow.setNameZht(trim(request.getNameZht()));
        flow.setNamePt(trim(request.getNamePt()));
        flow.setDescriptionZh(trim(request.getDescriptionZh()));
        flow.setDescriptionEn(trim(request.getDescriptionEn()));
        flow.setDescriptionZht(trim(request.getDescriptionZht()));
        flow.setDescriptionPt(trim(request.getDescriptionPt()));
        flow.setMapPolicyJson(validateVersionedJson(request.getMapPolicyJson(), "mapPolicyJson"));
        flow.setAdvancedConfigJson(validateVersionedJson(request.getAdvancedConfigJson(), "advancedConfigJson"));
        flow.setStatus(status);
        flow.setSortOrder(defaultNumber(request.getSortOrder()));
        flow.setPublishedAt(STATUS_PUBLISHED.equals(status) ? (publishedAt == null ? LocalDateTime.now() : publishedAt) : null);
        flow.setDeleted(0);
    }

    private void applyStep(ExperienceFlowStep step, AdminExperienceRequest.StepUpsert request) {
        if (request.getTemplateId() != null) {
            requireTemplate(request.getTemplateId());
        }
        String stepType = requireCode(request.getStepType(), "stepType");
        String triggerType = defaultAllowedCode(request.getTriggerType(), "triggerType", "manual", TRIGGER_TYPES);
        String explorationWeightLevel = defaultAllowedCode(request.getExplorationWeightLevel(), "explorationWeightLevel", "small", WEIGHT_LEVELS);
        step.setStepCode(resolveCode(request.getStepCode(), stepType, step.getId(), "step"));
        step.setStepType(stepType);
        step.setTemplateId(request.getTemplateId());
        step.setStepNameZh(trim(request.getStepNameZh()));
        step.setStepNameEn(trim(request.getStepNameEn()));
        step.setStepNameZht(trim(request.getStepNameZht()));
        step.setStepNamePt(trim(request.getStepNamePt()));
        step.setDescriptionZh(trim(request.getDescriptionZh()));
        step.setDescriptionEn(trim(request.getDescriptionEn()));
        step.setDescriptionZht(trim(request.getDescriptionZht()));
        step.setDescriptionPt(trim(request.getDescriptionPt()));
        step.setTriggerType(triggerType);
        step.setTriggerConfigJson(validateVersionedJson(request.getTriggerConfigJson(), "triggerConfigJson"));
        step.setConditionConfigJson(validateVersionedJson(request.getConditionConfigJson(), "conditionConfigJson"));
        step.setEffectConfigJson(validateVersionedJson(request.getEffectConfigJson(), "effectConfigJson"));
        step.setMediaAssetId(request.getMediaAssetId());
        step.setRewardRuleIdsJson(validateAnyJson(request.getRewardRuleIdsJson(), "rewardRuleIdsJson"));
        step.setExplorationWeightLevel(explorationWeightLevel);
        step.setRequiredForCompletion(Boolean.TRUE.equals(request.getRequiredForCompletion()));
        step.setInheritKey(defaultText(request.getInheritKey(), ""));
        step.setStatus(defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES));
        step.setSortOrder(defaultNumber(request.getSortOrder()));
        step.setDeleted(0);
    }

    private void applyBinding(ExperienceBinding binding, AdminExperienceRequest.BindingUpsert request) {
        ExperienceFlow flow = requireFlow(request.getFlowId());
        String ownerType = requireAllowedCode(request.getOwnerType(), "ownerType", OWNER_TYPES);
        String bindingRole = defaultAllowedCode(request.getBindingRole(), "bindingRole", "default_experience_flow", BINDING_ROLES);
        validateOwnerReference(ownerType, request.getOwnerId(), request.getOwnerCode());
        if ("story_override_flow".equals(bindingRole) && !"story_chapter".equals(ownerType)) {
            throw new BusinessException(4002, "story_override_flow bindings must target story_chapter owners");
        }
        binding.setOwnerType(ownerType);
        binding.setOwnerId(request.getOwnerId());
        binding.setOwnerCode(defaultText(request.getOwnerCode(), ""));
        binding.setBindingRole(bindingRole);
        binding.setFlowId(flow.getId());
        binding.setPriority(request.getPriority() == null ? 0 : request.getPriority());
        binding.setInheritPolicy(defaultAllowedCode(request.getInheritPolicy(), "inheritPolicy", "inherit", INHERIT_POLICIES));
        binding.setStatus(defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES));
        binding.setSortOrder(defaultNumber(request.getSortOrder()));
        binding.setDeleted(0);
    }

    private void applyOverride(ExperienceOverride overrideRule, AdminExperienceRequest.OverrideUpsert request) {
        String ownerType = requireAllowedCode(request.getOwnerType(), "ownerType", OWNER_TYPES);
        String targetOwnerType = optionalAllowedCode(request.getTargetOwnerType(), "targetOwnerType", OWNER_TYPES);
        String overrideMode = defaultAllowedCode(request.getOverrideMode(), "overrideMode", "inherit", OVERRIDE_MODES);
        if (request.getReplacementStepId() != null) {
            requireStep(request.getReplacementStepId());
        }
        validateOwnerReference(ownerType, request.getOwnerId(), null);
        validateOverrideSemantics(overrideMode, request.getTargetStepCode(), request.getReplacementStepId());
        overrideRule.setOwnerType(ownerType);
        overrideRule.setOwnerId(request.getOwnerId());
        overrideRule.setTargetOwnerType(targetOwnerType == null ? "" : targetOwnerType);
        overrideRule.setTargetOwnerId(request.getTargetOwnerId());
        overrideRule.setTargetStepCode(defaultText(request.getTargetStepCode(), ""));
        overrideRule.setOverrideMode(overrideMode);
        overrideRule.setReplacementStepId(request.getReplacementStepId());
        overrideRule.setOverrideConfigJson(validateVersionedJson(request.getOverrideConfigJson(), "overrideConfigJson"));
        overrideRule.setStatus(defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES));
        overrideRule.setSortOrder(defaultNumber(request.getSortOrder()));
        overrideRule.setDeleted(0);
    }

    private void applyExplorationElement(ExplorationElement element, AdminExperienceRequest.ExplorationElementUpsert request) {
        String ownerType = requireAllowedCode(request.getOwnerType(), "ownerType", OWNER_TYPES);
        String weightLevel = defaultAllowedCode(request.getWeightLevel(), "weightLevel", "small", WEIGHT_LEVELS);
        int derivedWeightValue = weightValue(weightLevel);
        if (request.getWeightValue() != null && request.getWeightValue() != derivedWeightValue) {
            throw new BusinessException(4002, "weightValue must follow the canonical semantic weight mapping for weightLevel");
        }
        element.setElementCode(resolveCode(request.getElementCode(), request.getElementType(), element.getId(), "explore"));
        element.setElementType(requireCode(request.getElementType(), "elementType"));
        element.setOwnerType(ownerType);
        element.setOwnerId(request.getOwnerId());
        element.setOwnerCode(defaultText(request.getOwnerCode(), ""));
        validateOwnerReference(ownerType, request.getOwnerId(), request.getOwnerCode());
        element.setCityId(request.getCityId());
        element.setSubMapId(request.getSubMapId());
        element.setStorylineId(request.getStorylineId());
        element.setStoryChapterId(request.getStoryChapterId());
        element.setTitleZh(trim(request.getTitleZh()));
        element.setTitleEn(trim(request.getTitleEn()));
        element.setTitleZht(trim(request.getTitleZht()));
        element.setTitlePt(trim(request.getTitlePt()));
        element.setWeightLevel(weightLevel);
        element.setWeightValue(derivedWeightValue);
        element.setIncludeInExploration(!Boolean.FALSE.equals(request.getIncludeInExploration()));
        element.setMetadataJson(validateVersionedJson(request.getMetadataJson(), "metadataJson"));
        element.setStatus(defaultAllowedCode(request.getStatus(), "status", STATUS_DRAFT, STATUSES));
        element.setSortOrder(defaultNumber(request.getSortOrder()));
        element.setDeleted(0);
    }

    private AdminExperienceResponse.Template toTemplateResponse(ExperienceTemplate template) {
        Long usageCount = template.getId() == null ? 0 : stepMapper.selectCount(activeStepQuery().eq(ExperienceFlowStep::getTemplateId, template.getId()));
        return AdminExperienceResponse.Template.builder()
                .id(template.getId())
                .code(template.getCode())
                .templateType(template.getTemplateType())
                .templateTypeLabelZh(labelFor(template.getTemplateType(), TEMPLATE_TYPE_LABELS_ZH))
                .templateTypeGuidance("使用可重用模板描述展示、條件、玩法與獎勵演出。")
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

    private AdminExperienceResponse.Flow toFlowResponse(ExperienceFlow flow, boolean includeChildren) {
        List<ExperienceFlowStep> steps = includeChildren
                ? stepMapper.selectList(activeStepQuery().eq(ExperienceFlowStep::getFlowId, flow.getId()).orderByAsc(ExperienceFlowStep::getSortOrder).orderByAsc(ExperienceFlowStep::getId))
                : Collections.emptyList();
        Map<Long, ExperienceTemplate> templatesById = loadTemplatesById(steps.stream().map(ExperienceFlowStep::getTemplateId).toList());
        List<Long> stepIds = steps.stream().map(ExperienceFlowStep::getId).filter(Objects::nonNull).toList();
        List<ExperienceBinding> bindings = includeChildren
                ? bindingMapper.selectList(activeBindingQuery().eq(ExperienceBinding::getFlowId, flow.getId()).orderByAsc(ExperienceBinding::getPriority).orderByAsc(ExperienceBinding::getSortOrder))
                : Collections.emptyList();
        List<ExperienceOverride> overrides = includeChildren && !stepIds.isEmpty()
                ? overrideMapper.selectList(activeOverrideQuery().in(ExperienceOverride::getReplacementStepId, stepIds).orderByAsc(ExperienceOverride::getSortOrder))
                : Collections.emptyList();
        Map<Long, ExperienceFlow> flowsById = Map.of(flow.getId(), flow);
        return AdminExperienceResponse.Flow.builder()
                .id(flow.getId())
                .code(flow.getCode())
                .flowType(flow.getFlowType())
                .flowTypeLabelZh(labelFor(flow.getFlowType(), FLOW_TYPE_LABELS_ZH))
                .mode(flow.getMode())
                .modeLabelZh(labelFor(flow.getMode(), FLOW_MODE_LABELS_ZH))
                .nameZh(flow.getNameZh())
                .nameEn(flow.getNameEn())
                .nameZht(flow.getNameZht())
                .namePt(flow.getNamePt())
                .descriptionZh(flow.getDescriptionZh())
                .descriptionEn(flow.getDescriptionEn())
                .descriptionZht(flow.getDescriptionZht())
                .descriptionPt(flow.getDescriptionPt())
                .mapPolicyJson(flow.getMapPolicyJson())
                .advancedConfigJson(flow.getAdvancedConfigJson())
                .status(flow.getStatus())
                .sortOrder(flow.getSortOrder())
                .publishedAt(flow.getPublishedAt())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .steps(steps.stream().map(step -> toStepResponse(step, templatesById)).toList())
                .bindings(bindings.stream().map(binding -> toBindingResponse(binding, flowsById)).toList())
                .overrides(overrides.stream().map(this::toOverrideResponse).toList())
                .build();
    }

    private AdminExperienceResponse.Step toStepResponse(ExperienceFlowStep step, Map<Long, ExperienceTemplate> templatesById) {
        ExperienceTemplate template = step.getTemplateId() == null ? null : templatesById.get(step.getTemplateId());
        return AdminExperienceResponse.Step.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .templateId(step.getTemplateId())
                .template(template == null ? null : toTemplateResponse(template))
                .stepNameZh(step.getStepNameZh())
                .stepNameEn(step.getStepNameEn())
                .stepNameZht(step.getStepNameZht())
                .stepNamePt(step.getStepNamePt())
                .descriptionZh(step.getDescriptionZh())
                .descriptionEn(step.getDescriptionEn())
                .descriptionZht(step.getDescriptionZht())
                .descriptionPt(step.getDescriptionPt())
                .triggerType(step.getTriggerType())
                .triggerTypeLabelZh(labelFor(step.getTriggerType(), TRIGGER_TYPE_LABELS_ZH))
                .triggerConfigJson(step.getTriggerConfigJson())
                .conditionConfigJson(step.getConditionConfigJson())
                .effectConfigJson(step.getEffectConfigJson())
                .mediaAssetId(step.getMediaAssetId())
                .rewardRuleIdsJson(step.getRewardRuleIdsJson())
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .explorationWeightLabelZh(labelFor(step.getExplorationWeightLevel(), WEIGHT_LEVEL_LABELS_ZH))
                .explorationWeightValue(WEIGHT_LEVEL_VALUES.getOrDefault(step.getExplorationWeightLevel(), weightValue(step.getExplorationWeightLevel())))
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .status(step.getStatus())
                .sortOrder(step.getSortOrder())
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .build();
    }

    private AdminExperienceResponse.Binding toBindingResponse(ExperienceBinding binding, Map<Long, ExperienceFlow> flowsById) {
        ExperienceFlow flow = flowsById.get(binding.getFlowId());
        return AdminExperienceResponse.Binding.builder()
                .id(binding.getId())
                .ownerType(binding.getOwnerType())
                .ownerId(binding.getOwnerId())
                .ownerCode(binding.getOwnerCode())
                .bindingRole(binding.getBindingRole())
                .bindingRoleLabelZh(labelFor(binding.getBindingRole(), BINDING_ROLE_LABELS_ZH))
                .flowId(binding.getFlowId())
                .flowName(flow == null ? null : flow.getNameZh())
                .priority(binding.getPriority())
                .inheritPolicy(binding.getInheritPolicy())
                .status(binding.getStatus())
                .sortOrder(binding.getSortOrder())
                .createdAt(binding.getCreatedAt())
                .updatedAt(binding.getUpdatedAt())
                .build();
    }

    private AdminExperienceResponse.OverrideRule toOverrideResponse(ExperienceOverride overrideRule) {
        return AdminExperienceResponse.OverrideRule.builder()
                .id(overrideRule.getId())
                .ownerType(overrideRule.getOwnerType())
                .ownerId(overrideRule.getOwnerId())
                .targetOwnerType(overrideRule.getTargetOwnerType())
                .targetOwnerId(overrideRule.getTargetOwnerId())
                .targetStepCode(overrideRule.getTargetStepCode())
                .overrideMode(overrideRule.getOverrideMode())
                .overrideModeLabelZh(labelFor(overrideRule.getOverrideMode(), OVERRIDE_MODE_LABELS_ZH))
                .replacementStepId(overrideRule.getReplacementStepId())
                .overrideConfigJson(overrideRule.getOverrideConfigJson())
                .requiresTargetStepCode(requiresTargetStepCode(overrideRule.getOverrideMode()))
                .requiresReplacementStep(requiresReplacementStep(overrideRule.getOverrideMode()))
                .semanticsHint(OVERRIDE_MODE_GUIDANCE_ZH.getOrDefault(overrideRule.getOverrideMode(), "使用繼承、停用、替換或附加語義編排章節覆寫。"))
                .status(overrideRule.getStatus())
                .sortOrder(overrideRule.getSortOrder())
                .createdAt(overrideRule.getCreatedAt())
                .updatedAt(overrideRule.getUpdatedAt())
                .build();
    }

    private AdminExperienceResponse.ExplorationElement toExplorationElementResponse(ExplorationElement element) {
        return AdminExperienceResponse.ExplorationElement.builder()
                .id(element.getId())
                .elementCode(element.getElementCode())
                .elementType(element.getElementType())
                .ownerType(element.getOwnerType())
                .ownerId(element.getOwnerId())
                .ownerCode(element.getOwnerCode())
                .cityId(element.getCityId())
                .subMapId(element.getSubMapId())
                .storylineId(element.getStorylineId())
                .storyChapterId(element.getStoryChapterId())
                .titleZh(element.getTitleZh())
                .titleEn(element.getTitleEn())
                .titleZht(element.getTitleZht())
                .titlePt(element.getTitlePt())
                .weightLevel(element.getWeightLevel())
                .weightLabelZh(labelFor(element.getWeightLevel(), WEIGHT_LEVEL_LABELS_ZH))
                .weightValue(element.getWeightValue())
                .includeInExploration(element.getIncludeInExploration())
                .metadataJson(element.getMetadataJson())
                .weightGuidance(WEIGHT_LEVEL_GUIDANCE_ZH.getOrDefault(element.getWeightLevel(), "探索權重使用語義等級而不是固定百分比。"))
                .status(element.getStatus())
                .sortOrder(element.getSortOrder())
                .createdAt(element.getCreatedAt())
                .updatedAt(element.getUpdatedAt())
                .build();
    }

    private List<AdminExperienceResponse.GovernanceFinding> buildGovernanceFindings(
            List<ExperienceBinding> bindings,
            List<ExperienceFlowStep> steps,
            List<ExperienceOverride> overrides,
            List<ExplorationElement> elements) {
        Map<Long, List<ExperienceBinding>> bindingsByFlow = bindings.stream()
                .collect(Collectors.groupingBy(ExperienceBinding::getFlowId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<ExperienceFlowStep>> stepsByFlow = steps.stream()
                .collect(Collectors.groupingBy(ExperienceFlowStep::getFlowId, LinkedHashMap::new, Collectors.toList()));
        List<AdminExperienceResponse.GovernanceFinding> findings = new java.util.ArrayList<>();
        bindingsByFlow.forEach((flowId, flowBindings) -> {
            List<ExperienceFlowStep> flowSteps = stepsByFlow.getOrDefault(flowId, Collections.emptyList());
            long fullscreenCount = flowSteps.stream()
                    .filter(step -> containsIgnoreCase(step.getEffectConfigJson(), "fullscreen"))
                    .count();
            if (fullscreenCount > 1) {
                for (ExperienceBinding binding : flowBindings) {
                    findings.add(AdminExperienceResponse.GovernanceFinding.builder()
                            .severity("warning")
                            .findingType("fullscreen_overlap")
                            .title("同一流程存在多個全屏效果")
                            .description("同一地點或章節流程內有多個全屏播放效果，發布前需要確認觸發條件不會重疊。")
                            .ownerType(binding.getOwnerType())
                            .ownerId(binding.getOwnerId())
                            .flowId(flowId)
                            .build());
                }
            }
        });
        overrides.stream()
                .filter(override -> "disable".equalsIgnoreCase(override.getOverrideMode()))
                .filter(override -> !StringUtils.hasText(override.getTargetStepCode()))
                .forEach(override -> findings.add(AdminExperienceResponse.GovernanceFinding.builder()
                        .severity("error")
                        .findingType("invalid_override")
                        .title("覆寫關閉缺少目標步驟")
                        .description("關閉繼承步驟時必須指定 targetStepCode，否則 runtime 無法知道要關閉哪個效果。")
                        .ownerType(override.getOwnerType())
                        .ownerId(override.getOwnerId())
                        .build()));
        overrides.stream()
                .filter(override -> requiresReplacementStep(override.getOverrideMode()))
                .filter(override -> override.getReplacementStepId() == null)
                .forEach(override -> findings.add(AdminExperienceResponse.GovernanceFinding.builder()
                        .severity("error")
                        .findingType("override_missing_replacement")
                        .title("覆寫語義缺少替換步驟")
                        .description("replace 或 append 覆寫必須提供 replacementStepId，否則無法在 runtime 正確編排新步驟。")
                        .ownerType(override.getOwnerType())
                        .ownerId(override.getOwnerId())
                        .build()));
        bindings.stream()
                .filter(binding -> "story_override_flow".equalsIgnoreCase(binding.getBindingRole()))
                .filter(binding -> !"story_chapter".equalsIgnoreCase(binding.getOwnerType()))
                .forEach(binding -> findings.add(AdminExperienceResponse.GovernanceFinding.builder()
                        .severity("error")
                        .findingType("invalid_binding_role")
                        .title("章節覆寫流程綁定到錯誤對象")
                        .description("story_override_flow 只能綁定到 story_chapter，否則故事章節覆寫語義會漂移。")
                        .ownerType(binding.getOwnerType())
                        .ownerId(binding.getOwnerId())
                        .flowId(binding.getFlowId())
                        .build()));
        elements.stream()
                .filter(element -> element.getWeightValue() != null)
                .filter(element -> !Objects.equals(element.getWeightValue(), weightValue(element.getWeightLevel())))
                .forEach(element -> findings.add(AdminExperienceResponse.GovernanceFinding.builder()
                        .severity("warning")
                        .findingType("weight_value_drift")
                        .title("探索權重數值與語義等級不一致")
                        .description("探索元素應以 weightLevel 為主，weightValue 只應反映系統映射值，避免出現固定百分比語義。")
                        .ownerType(element.getOwnerType())
                        .ownerId(element.getOwnerId())
                        .build()));
        return findings;
    }

    private AdminExperienceResponse.ContractVocabulary buildContractVocabulary() {
        return AdminExperienceResponse.ContractVocabulary.builder()
                .templateTypes(buildVocabularyOptions(TEMPLATE_TYPES, TEMPLATE_TYPE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .flowTypes(buildVocabularyOptions(FLOW_TYPES, FLOW_TYPE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .flowModes(buildVocabularyOptions(FLOW_MODES, FLOW_MODE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .ownerTypes(buildVocabularyOptions(OWNER_TYPES, OWNER_TYPE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .bindingRoles(buildVocabularyOptions(BINDING_ROLES, BINDING_ROLE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .inheritPolicies(buildVocabularyOptions(INHERIT_POLICIES, INHERIT_POLICY_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .overrideModes(buildVocabularyOptions(OVERRIDE_MODES, OVERRIDE_MODE_LABELS_ZH, OVERRIDE_MODE_GUIDANCE_ZH, Collections.emptyMap()))
                .triggerTypes(buildVocabularyOptions(TRIGGER_TYPES, TRIGGER_TYPE_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .weightLevels(buildVocabularyOptions(WEIGHT_LEVELS, WEIGHT_LEVEL_LABELS_ZH, WEIGHT_LEVEL_GUIDANCE_ZH, WEIGHT_LEVEL_VALUES))
                .statuses(buildVocabularyOptions(STATUSES, STATUS_LABELS_ZH, Collections.emptyMap(), Collections.emptyMap()))
                .build();
    }

    private List<AdminExperienceResponse.OperatorHint> buildOperatorHints() {
        return List.of(
                AdminExperienceResponse.OperatorHint.builder()
                        .fieldName("versionedJson")
                        .title("版本化 JSON 必須帶 schemaVersion")
                        .description(String.join(", ", VERSIONED_JSON_FIELD_ORDER) + " 都必須保存為 {schemaVersion: n, ...} 物件，避免未版本化 payload 直接入庫。")
                        .build(),
                AdminExperienceResponse.OperatorHint.builder()
                        .fieldName("overrideMode")
                        .title("覆寫語義必須使用四種固定模式")
                        .description("inherit / disable / replace / append 對應沿用、停用、替換、附加。replace 與 append 必須指定 replacementStepId。")
                        .build(),
                AdminExperienceResponse.OperatorHint.builder()
                        .fieldName("default_experience_flow")
                        .title("錨點預設流程是跨表面的基準")
                        .description("default_experience_flow 代表 POI 或空間錨點的自然體驗，story_override_flow 只應由 story_chapter 接管。")
                        .build(),
                AdminExperienceResponse.OperatorHint.builder()
                        .fieldName("weightLevel")
                        .title("探索權重使用語義等級")
                        .description("tiny / small / medium / large / core 由系統映射為 1 / 2 / 3 / 5 / 8，不直接讓運營輸入固定百分比。")
                        .build()
        );
    }

    private List<AdminExperienceResponse.VocabularyOption> buildVocabularyOptions(
            List<String> codes,
            Map<String, String> labelsZh,
            Map<String, String> guidanceByCode,
            Map<String, Integer> numericValues) {
        return codes.stream()
                .map(code -> AdminExperienceResponse.VocabularyOption.builder()
                        .code(code)
                        .labelZh(labelFor(code, labelsZh))
                        .guidance(guidanceByCode.get(code))
                        .numericValue(numericValues.get(code))
                        .build())
                .toList();
    }

    private ExperienceTemplate requireTemplate(Long templateId) {
        ExperienceTemplate template = templateMapper.selectOne(activeTemplateQuery().eq(ExperienceTemplate::getId, templateId));
        if (template == null) {
            throw new BusinessException(4070, "Experience template not found");
        }
        return template;
    }

    private ExperienceFlow requireFlow(Long flowId) {
        ExperienceFlow flow = flowMapper.selectOne(activeFlowQuery().eq(ExperienceFlow::getId, flowId));
        if (flow == null) {
            throw new BusinessException(4071, "Experience flow not found");
        }
        return flow;
    }

    private ExperienceFlowStep requireStep(Long stepId) {
        ExperienceFlowStep step = stepMapper.selectOne(activeStepQuery().eq(ExperienceFlowStep::getId, stepId));
        if (step == null) {
            throw new BusinessException(4073, "Experience step not found");
        }
        return step;
    }

    private ExperienceBinding requireBinding(Long bindingId) {
        ExperienceBinding binding = bindingMapper.selectOne(activeBindingQuery().eq(ExperienceBinding::getId, bindingId));
        if (binding == null) {
            throw new BusinessException(4074, "Experience binding not found");
        }
        return binding;
    }

    private ExperienceOverride requireOverride(Long overrideId) {
        ExperienceOverride overrideRule = overrideMapper.selectOne(activeOverrideQuery().eq(ExperienceOverride::getId, overrideId));
        if (overrideRule == null) {
            throw new BusinessException(4075, "Experience override not found");
        }
        return overrideRule;
    }

    private ExplorationElement requireExplorationElement(Long elementId) {
        ExplorationElement element = explorationElementMapper.selectOne(activeExplorationElementQuery().eq(ExplorationElement::getId, elementId));
        if (element == null) {
            throw new BusinessException(4076, "Exploration element not found");
        }
        return element;
    }

    private Map<Long, ExperienceTemplate> loadTemplatesById(List<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return templateMapper.selectList(activeTemplateQuery().in(ExperienceTemplate::getId, normalizedIds)).stream()
                .collect(Collectors.toMap(ExperienceTemplate::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, ExperienceFlow> loadFlowsById(List<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return flowMapper.selectList(activeFlowQuery().in(ExperienceFlow::getId, normalizedIds)).stream()
                .collect(Collectors.toMap(ExperienceFlow::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
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

    private LambdaQueryWrapper<ExplorationElement> activeExplorationElementQuery() {
        return new LambdaQueryWrapper<ExplorationElement>().eq(ExplorationElement::getDeleted, 0);
    }

    private String validateVersionedJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        JsonNode node = readJson(value, fieldName);
        if (!VERSIONED_JSON_FIELDS.contains(fieldName)) {
            return value.trim();
        }
        JsonNode schemaVersionNode = node.path("schemaVersion");
        if (!node.isObject() || schemaVersionNode.isMissingNode() || !schemaVersionNode.canConvertToInt() || schemaVersionNode.asInt() <= 0) {
            throw new BusinessException(4002, fieldName + " must be a JSON object with schemaVersion");
        }
        return value.trim();
    }

    private String validateAnyJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        readJson(value, fieldName);
        return value.trim();
    }

    private JsonNode readJson(String value, String fieldName) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception ex) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private String resolveCode(String candidate, String type, Long id, String prefix) {
        if (StringUtils.hasText(candidate)) {
            return normalizeCode(candidate);
        }
        String suffix = id == null ? String.valueOf(System.currentTimeMillis()) : String.valueOf(id);
        return prefix + "_" + defaultCode(type, "general").replace('-', '_') + "_" + suffix;
    }

    private String requireCode(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4002, fieldName + " is required");
        }
        return normalizeCode(value);
    }

    private String requireAllowedCode(String value, String fieldName, List<String> allowedCodes) {
        String normalized = requireCode(value, fieldName);
        if (!allowedCodes.contains(normalized)) {
            throw new BusinessException(4002, fieldName + " must use the canonical experience vocabulary");
        }
        return normalized;
    }

    private String optionalAllowedCode(String value, String fieldName, List<String> allowedCodes) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return requireAllowedCode(value, fieldName, allowedCodes);
    }

    private String defaultAllowedCode(String value, String fieldName, String fallback, List<String> allowedCodes) {
        String normalized = StringUtils.hasText(value) ? normalizeCode(value) : fallback;
        if (!allowedCodes.contains(normalized)) {
            throw new BusinessException(4002, fieldName + " must use the canonical experience vocabulary");
        }
        return normalized;
    }

    private String defaultCode(String value, String fallback) {
        return StringUtils.hasText(value) ? normalizeCode(value) : fallback;
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return StringUtils.hasText(value) && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private void validateOwnerReference(String ownerType, Long ownerId, String ownerCode) {
        if ("manual_target".equals(ownerType)) {
            if (!StringUtils.hasText(ownerCode)) {
                throw new BusinessException(4002, "manual_target owners must provide ownerCode");
            }
            return;
        }
        if (ownerId == null && !StringUtils.hasText(ownerCode)) {
            throw new BusinessException(4002, "ownerId or ownerCode is required for non-manual experience owners");
        }
    }

    private void validateOverrideSemantics(String overrideMode, String targetStepCode, Long replacementStepId) {
        if (requiresTargetStepCode(overrideMode) && !StringUtils.hasText(targetStepCode)) {
            throw new BusinessException(4002, "overrideMode requires targetStepCode");
        }
        if (requiresReplacementStep(overrideMode) && replacementStepId == null) {
            throw new BusinessException(4002, "overrideMode requires replacementStepId");
        }
    }

    private boolean requiresTargetStepCode(String overrideMode) {
        return "disable".equalsIgnoreCase(overrideMode)
                || "replace".equalsIgnoreCase(overrideMode)
                || "append".equalsIgnoreCase(overrideMode);
    }

    private boolean requiresReplacementStep(String overrideMode) {
        return "replace".equalsIgnoreCase(overrideMode)
                || "append".equalsIgnoreCase(overrideMode);
    }

    private String labelFor(String code, Map<String, String> labels) {
        return StringUtils.hasText(code) ? labels.getOrDefault(code, code) : null;
    }

    private int weightValue(String weightLevel) {
        String normalized = StringUtils.hasText(weightLevel) ? normalizeCode(weightLevel) : "small";
        return WEIGHT_LEVEL_VALUES.getOrDefault(normalized, 2);
    }
}
