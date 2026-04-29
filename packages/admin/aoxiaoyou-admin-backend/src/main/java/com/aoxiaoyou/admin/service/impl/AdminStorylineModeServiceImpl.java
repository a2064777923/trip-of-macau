package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStorylineModeRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStorylineModeResponse;
import com.aoxiaoyou.admin.entity.ExperienceBinding;
import com.aoxiaoyou.admin.entity.ExperienceFlow;
import com.aoxiaoyou.admin.entity.ExperienceFlowStep;
import com.aoxiaoyou.admin.entity.ExperienceOverride;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.admin.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.service.AdminStoryChapterService;
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import com.aoxiaoyou.admin.service.AdminStorylineModeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class AdminStorylineModeServiceImpl implements AdminStorylineModeService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final List<String> AVAILABLE_ANCHOR_TYPES = List.of(
            "poi", "indoor_building", "indoor_floor", "indoor_node", "task", "overlay", "manual"
    );
    private static final List<String> AVAILABLE_OVERRIDE_MODES = List.of("inherit", "disable", "replace", "append");
    private static final Set<String> STATUSES = Set.of("draft", "published", "archived");
    private static final Set<String> WEIGHT_LEVELS = Set.of("tiny", "small", "medium", "large", "core");
    private static final String STATUS_DRAFT = "draft";
    private static final String OWNER_TYPE_STORY_CHAPTER = "story_chapter";
    private static final String BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW = "default_experience_flow";
    private static final String BINDING_ROLE_STORY_OVERRIDE_FLOW = "story_override_flow";
    private static final String FLOW_TYPE_STORY_CHAPTER = "story_chapter";
    private static final String FLOW_MODE_STORYLINE = "storyline";

    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final ExperienceFlowMapper flowMapper;
    private final ExperienceFlowStepMapper stepMapper;
    private final ExperienceBindingMapper bindingMapper;
    private final ExperienceOverrideMapper overrideMapper;
    private final AdminStoryLineService adminStoryLineService;
    private final AdminStoryChapterService adminStoryChapterService;
    private final ObjectMapper objectMapper;

    @Override
    public AdminStorylineModeResponse.Snapshot getSnapshot(Long storylineId) {
        requireStoryline(storylineId);
        return buildSnapshot(storylineId);
    }

    @Override
    @Transactional
    public AdminStorylineModeResponse.Snapshot updateModeConfig(Long storylineId, AdminStorylineModeRequest.StoryModeConfigUpsert request) {
        requireStoryline(storylineId);
        List<StoryChapter> chapters = selectChapters(storylineId);
        String storyModeConfigJson = resolveStoryModeConfigJson(request);
        for (StoryChapter chapter : chapters) {
            chapter.setStoryModeConfigJson(storyModeConfigJson);
            storyChapterMapper.updateById(chapter);
        }
        return buildSnapshot(storylineId);
    }

    @Override
    @Transactional
    public AdminStorylineModeResponse.Snapshot updateChapterAnchor(Long storylineId, Long chapterId, AdminStorylineModeRequest.ChapterAnchorUpsert request) {
        StoryChapter chapter = requireChapter(storylineId, chapterId);
        String anchorType = normalizeAllowed(request.getAnchorType(), "manual", AVAILABLE_ANCHOR_TYPES, "anchorType");
        validateAnchorShape(anchorType, request.getAnchorTargetId(), request.getAnchorTargetCode());
        chapter.setAnchorType(anchorType);
        chapter.setAnchorTargetId(request.getAnchorTargetId());
        chapter.setAnchorTargetCode(trim(request.getAnchorTargetCode()));
        if (StringUtils.hasText(request.getAnchorLabelOverride())) {
            chapter.setLocationNameZh(request.getAnchorLabelOverride().trim());
            chapter.setLocationNameZht(request.getAnchorLabelOverride().trim());
        }
        if (request.getRouteOrder() != null) {
            chapter.setChapterOrder(request.getRouteOrder());
            chapter.setSortOrder(request.getRouteOrder());
        }
        if (StringUtils.hasText(request.getRouteSegmentStyle())) {
            Map<String, Object> config = readVersionedObject(chapter.getStoryModeConfigJson(), "storyModeConfigJson", true);
            config.put("routeSegmentStyle", request.getRouteSegmentStyle().trim());
            chapter.setStoryModeConfigJson(writeJson(normalizeStoryModeConfig(config)));
        }
        storyChapterMapper.updateById(chapter);
        return buildSnapshot(storylineId);
    }

    @Override
    @Transactional
    public AdminStorylineModeResponse.Snapshot updateChapterOverridePolicy(Long storylineId, Long chapterId, AdminStorylineModeRequest.ChapterOverridePolicyUpsert request) {
        StoryChapter chapter = requireChapter(storylineId, chapterId);
        chapter.setOverridePolicyJson(resolveOverridePolicyJson(request));
        storyChapterMapper.updateById(chapter);
        return buildSnapshot(storylineId);
    }

    @Override
    @Transactional
    public AdminStorylineModeResponse.OverrideRule createOverrideStep(Long storylineId, Long chapterId, AdminStorylineModeRequest.OverrideStepUpsert request) {
        StoryChapter chapter = requireChapter(storylineId, chapterId);
        ExperienceOverride overrideRule = new ExperienceOverride();
        applyOverrideRequest(chapter, overrideRule, request);
        overrideMapper.insert(overrideRule);
        return toOverrideRule(requireOverride(overrideRule.getId()), loadStepsById(Collections.singletonList(overrideRule.getReplacementStepId())));
    }

    @Override
    @Transactional
    public AdminStorylineModeResponse.OverrideRule updateOverrideStep(Long storylineId, Long chapterId, Long overrideId, AdminStorylineModeRequest.OverrideStepUpsert request) {
        StoryChapter chapter = requireChapter(storylineId, chapterId);
        ExperienceOverride overrideRule = requireOverride(overrideId);
        if (!OWNER_TYPE_STORY_CHAPTER.equals(overrideRule.getOwnerType()) || !Objects.equals(overrideRule.getOwnerId(), chapter.getId())) {
            throw new BusinessException(4075, "Experience override not found in story chapter");
        }
        applyOverrideRequest(chapter, overrideRule, request);
        overrideMapper.updateById(overrideRule);
        return toOverrideRule(requireOverride(overrideId), loadStepsById(Collections.singletonList(overrideRule.getReplacementStepId())));
    }

    @Override
    @Transactional
    public void deleteOverrideStep(Long storylineId, Long chapterId, Long overrideId) {
        StoryChapter chapter = requireChapter(storylineId, chapterId);
        ExperienceOverride overrideRule = requireOverride(overrideId);
        if (!OWNER_TYPE_STORY_CHAPTER.equals(overrideRule.getOwnerType()) || !Objects.equals(overrideRule.getOwnerId(), chapter.getId())) {
            throw new BusinessException(4075, "Experience override not found in story chapter");
        }
        overrideRule.setDeleted(1);
        overrideMapper.updateById(overrideRule);
    }

    @Override
    public AdminStorylineModeResponse.RuntimePreview runtimePreview(Long storylineId) {
        requireStoryline(storylineId);
        AdminStorylineModeResponse.Snapshot snapshot = buildSnapshot(storylineId);
        return AdminStorylineModeResponse.RuntimePreview.builder()
                .storylineId(storylineId)
                .publicRuntimePath(snapshot.getPublicRuntimePath())
                .storyModeConfig(snapshot.getRouteStrategy())
                .chapters(snapshot.getChapterRuntimes())
                .validationFindings(snapshot.getValidationFindings())
                .build();
    }

    private AdminStorylineModeResponse.Snapshot buildSnapshot(Long storylineId) {
        AdminStoryLineDetailResponse storyline = adminStoryLineService.detail(storylineId);
        List<AdminStoryChapterResponse> chapterResponses = adminStoryChapterService.listByStoryline(storylineId);
        List<StoryChapter> chapters = selectChapters(storylineId);
        List<AdminStorylineModeResponse.ChapterRuntime> chapterRuntimes = chapters.stream()
                .map(chapter -> buildChapterRuntime(chapter, chapterResponses))
                .toList();
        List<AdminStorylineModeResponse.ValidationFinding> findings = chapterRuntimes.stream()
                .flatMap(runtime -> runtime.getValidationFindings().stream())
                .toList();
        return AdminStorylineModeResponse.Snapshot.builder()
                .storyline(storyline)
                .chapters(chapterResponses)
                .routeStrategy(toRouteStrategy(firstStoryModeConfig(chapters)))
                .chapterRuntimes(chapterRuntimes)
                .availableAnchorTypes(AVAILABLE_ANCHOR_TYPES)
                .availableOverrideModes(AVAILABLE_OVERRIDE_MODES)
                .validationFindings(findings)
                .publicRuntimePath("/api/v1/storylines/" + storylineId + "/runtime")
                .build();
    }

    private AdminStorylineModeResponse.ChapterRuntime buildChapterRuntime(
            StoryChapter chapter,
            List<AdminStoryChapterResponse> chapterResponses) {
        AdminStoryChapterResponse chapterResponse = chapterResponses.stream()
                .filter(item -> Objects.equals(item.getId(), chapter.getId()))
                .findFirst()
                .orElseGet(() -> adminStoryChapterService.detail(chapter.getStorylineId(), chapter.getId()));
        ExperienceFlow inheritedFlow = findDefaultFlow(chapter.getAnchorType(), chapter.getAnchorTargetId(), chapter.getAnchorTargetCode());
        ExperienceFlow chapterFlow = chapter.getExperienceFlowId() == null ? null : selectActiveFlow(chapter.getExperienceFlowId());
        List<ExperienceOverride> overrides = selectChapterOverrides(chapter.getId());
        Map<Long, ExperienceFlowStep> replacementSteps = loadStepsById(overrides.stream().map(ExperienceOverride::getReplacementStepId).toList());
        List<AdminStorylineModeResponse.StepSummary> compiledSteps = compileStepPreview(inheritedFlow, chapterFlow, overrides);
        List<AdminStorylineModeResponse.ValidationFinding> findings = buildValidationFindings(chapter, inheritedFlow, overrides);
        return AdminStorylineModeResponse.ChapterRuntime.builder()
                .chapter(chapterResponse)
                .anchor(AdminStorylineModeResponse.Anchor.builder()
                        .anchorType(chapter.getAnchorType())
                        .anchorTargetId(chapter.getAnchorTargetId())
                        .anchorTargetCode(chapter.getAnchorTargetCode())
                        .anchorLabel(chapterResponse.getAnchorTargetLabel())
                        .routeOrder(chapter.getChapterOrder())
                        .routeSegmentStyle(readString(readObject(chapter.getStoryModeConfigJson()).get("routeSegmentStyle")))
                        .build())
                .inheritedFlow(toFlowSummary(inheritedFlow))
                .chapterFlow(toFlowSummary(chapterFlow))
                .overrides(overrides.stream().map(override -> toOverrideRule(override, replacementSteps)).toList())
                .compiledStepPreview(compiledSteps)
                .validationFindings(findings)
                .build();
    }

    private List<AdminStorylineModeResponse.StepSummary> compileStepPreview(
            ExperienceFlow inheritedFlow,
            ExperienceFlow chapterFlow,
            List<ExperienceOverride> overrides) {
        List<AdminStorylineModeResponse.StepSummary> compiled = new ArrayList<>();
        if (inheritedFlow != null) {
            compiled.addAll(selectSteps(inheritedFlow.getId()).stream().map(step -> toStepSummary(step, "inherit")).toList());
        }
        Map<Long, ExperienceFlowStep> replacementSteps = loadStepsById(overrides.stream().map(ExperienceOverride::getReplacementStepId).toList());
        for (ExperienceOverride overrideRule : overrides) {
            applyOverridePreview(compiled, overrideRule, replacementSteps.get(overrideRule.getReplacementStepId()));
        }
        if (chapterFlow != null) {
            appendIfMissing(compiled, selectSteps(chapterFlow.getId()).stream().map(step -> toStepSummary(step, "append")).toList());
        }
        List<AdminStorylineModeResponse.StepSummary> normalized = new ArrayList<>();
        for (int index = 0; index < compiled.size(); index++) {
            AdminStorylineModeResponse.StepSummary step = compiled.get(index);
            normalized.add(copyStepSummary(step, index + 1));
        }
        return normalized;
    }

    private void applyOverridePreview(
            List<AdminStorylineModeResponse.StepSummary> compiled,
            ExperienceOverride overrideRule,
            ExperienceFlowStep replacementStep) {
        String overrideMode = normalizeCode(overrideRule.getOverrideMode(), "inherit");
        int targetIndex = findStepIndex(compiled, overrideRule.getTargetStepCode());
        if ("inherit".equals(overrideMode)) {
            return;
        }
        if ("disable".equals(overrideMode)) {
            if (targetIndex >= 0) {
                compiled.remove(targetIndex);
            }
            return;
        }
        if (replacementStep == null) {
            return;
        }
        AdminStorylineModeResponse.StepSummary replacement = toStepSummary(replacementStep, overrideMode);
        if ("replace".equals(overrideMode) && targetIndex >= 0) {
            compiled.set(targetIndex, replacement);
            return;
        }
        if ("append".equals(overrideMode) && targetIndex >= 0) {
            compiled.add(targetIndex + 1, replacement);
            return;
        }
        compiled.add(replacement);
    }

    private void applyOverrideRequest(
            StoryChapter chapter,
            ExperienceOverride overrideRule,
            AdminStorylineModeRequest.OverrideStepUpsert request) {
        String overrideMode = normalizeAllowed(request.getOverrideMode(), "inherit", AVAILABLE_OVERRIDE_MODES, "overrideMode");
        if (("disable".equals(overrideMode) || "replace".equals(overrideMode)) && !StringUtils.hasText(request.getTargetStepCode())) {
            throw new BusinessException(4002, "targetStepCode is required for disable or replace overrides");
        }
        Long replacementStepId = request.getReplacementStepId();
        if (request.getReplacementStepDraft() != null) {
            replacementStepId = upsertReplacementStep(chapter, request.getReplacementStepDraft(), request).getId();
        }
        if (("replace".equals(overrideMode) || "append".equals(overrideMode)) && replacementStepId == null) {
            throw new BusinessException(4002, "replacementStepId or replacementStepDraft is required for replace or append overrides");
        }
        if (replacementStepId != null) {
            requireStep(replacementStepId);
        }
        String anchorType = normalizeAllowed(chapter.getAnchorType(), "manual", AVAILABLE_ANCHOR_TYPES, "anchorType");
        overrideRule.setOwnerType(OWNER_TYPE_STORY_CHAPTER);
        overrideRule.setOwnerId(chapter.getId());
        overrideRule.setTargetOwnerType(anchorType);
        overrideRule.setTargetOwnerId(chapter.getAnchorTargetId());
        overrideRule.setTargetStepCode(trim(request.getTargetStepCode()));
        overrideRule.setOverrideMode(overrideMode);
        overrideRule.setReplacementStepId(replacementStepId);
        overrideRule.setOverrideConfigJson(resolveOverrideConfigJson(request));
        overrideRule.setStatus(normalizeAllowed(request.getStatus(), STATUS_DRAFT, new ArrayList<>(STATUSES), "status"));
        overrideRule.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        overrideRule.setDeleted(0);
    }

    private ExperienceFlowStep upsertReplacementStep(
            StoryChapter chapter,
            AdminStorylineModeRequest.ReplacementStepDraft draft,
            AdminStorylineModeRequest.OverrideStepUpsert overrideRequest) {
        ExperienceFlow flow = ensureChapterFlow(chapter);
        ExperienceFlowStep step = draft.getStepId() == null ? new ExperienceFlowStep() : requireStep(draft.getStepId());
        if (step.getId() != null && !Objects.equals(step.getFlowId(), flow.getId())) {
            throw new BusinessException(4073, "Replacement step is not in this story chapter flow");
        }
        step.setFlowId(flow.getId());
        step.setStepCode(defaultCode(draft.getStepCode(), "story_step_" + System.currentTimeMillis()));
        step.setStepType(defaultCode(draft.getStepType(), "custom"));
        step.setTemplateId(null);
        step.setStepNameZh(defaultText(draft.getStepNameZh(), "故事專屬步驟"));
        step.setStepNameZht(defaultText(draft.getStepNameZht(), defaultText(draft.getStepNameZh(), "故事專屬步驟")));
        step.setStepNameEn(trim(draft.getStepNameEn()));
        step.setStepNamePt(trim(draft.getStepNamePt()));
        step.setDescriptionZh(trim(draft.getDescriptionZh()));
        step.setDescriptionZht(defaultText(draft.getDescriptionZht(), trim(draft.getDescriptionZh())));
        step.setDescriptionEn(trim(draft.getDescriptionEn()));
        step.setDescriptionPt(trim(draft.getDescriptionPt()));
        step.setTriggerType(defaultCode(draft.getTriggerType(), "story_mode_enter"));
        step.setTriggerConfigJson(normalizeVersionedJson(draft.getTriggerConfigJson(), "triggerConfigJson", true));
        step.setConditionConfigJson(normalizeVersionedJson(draft.getConditionConfigJson(), "conditionConfigJson", true));
        step.setEffectConfigJson(resolveStepEffectJson(draft, overrideRequest));
        step.setMediaAssetId(draft.getMediaAssetId() == null ? overrideRequest.getMediaAssetId() : draft.getMediaAssetId());
        step.setRewardRuleIdsJson(writeRewardRuleIds(draft.getRewardRuleIds() == null ? overrideRequest.getRewardRuleIds() : draft.getRewardRuleIds()));
        step.setExplorationWeightLevel(normalizeAllowed(
                defaultText(draft.getExplorationWeightLevel(), overrideRequest.getExplorationWeightLevel()),
                "small",
                new ArrayList<>(WEIGHT_LEVELS),
                "explorationWeightLevel"));
        step.setRequiredForCompletion(Boolean.TRUE.equals(draft.getRequiredForCompletion()));
        step.setInheritKey(trim(draft.getInheritKey()));
        step.setStatus(normalizeAllowed(defaultText(draft.getStatus(), overrideRequest.getStatus()), STATUS_DRAFT, new ArrayList<>(STATUSES), "status"));
        step.setSortOrder(draft.getSortOrder() == null ? defaultNumber(overrideRequest.getSortOrder()) : draft.getSortOrder());
        step.setDeleted(0);
        if (step.getId() == null) {
            stepMapper.insert(step);
            return requireStep(step.getId());
        }
        stepMapper.updateById(step);
        return requireStep(step.getId());
    }

    private ExperienceFlow ensureChapterFlow(StoryChapter chapter) {
        ExperienceFlow flow = chapter.getExperienceFlowId() == null ? null : selectActiveFlow(chapter.getExperienceFlowId());
        if (flow == null) {
            flow = flowMapper.selectOne(activeFlowQuery().eq(ExperienceFlow::getCode, chapterFlowCode(chapter)));
        }
        boolean creating = flow == null;
        if (creating) {
            flow = new ExperienceFlow();
            flow.setCode(chapterFlowCode(chapter));
        }
        flow.setFlowType(FLOW_TYPE_STORY_CHAPTER);
        flow.setMode(FLOW_MODE_STORYLINE);
        flow.setNameZh(defaultText(chapter.getTitleZh(), "故事章節") + "覆寫流程");
        flow.setNameZht(defaultText(chapter.getTitleZht(), defaultText(chapter.getTitleZh(), "故事章節")) + "覆寫流程");
        flow.setNameEn(defaultText(chapter.getTitleEn(), "Story chapter override flow"));
        flow.setNamePt(trim(chapter.getTitlePt()));
        flow.setDescriptionZh("故事線模式下用於替換或追加錨點預設體驗的章節專屬流程。");
        flow.setDescriptionZht("故事線模式下用於替換或追加錨點預設體驗的章節專屬流程。");
        flow.setMapPolicyJson(writeJson(versionedMap(Map.of("source", "storyline_mode_workbench"))));
        flow.setAdvancedConfigJson(writeJson(versionedMap(Map.of("chapterId", chapter.getId()))));
        flow.setStatus(STATUS_DRAFT);
        flow.setSortOrder(defaultNumber(chapter.getSortOrder()));
        flow.setPublishedAt(null);
        flow.setDeleted(0);
        if (creating) {
            flowMapper.insert(flow);
        } else {
            flowMapper.updateById(flow);
        }
        chapter.setExperienceFlowId(flow.getId());
        storyChapterMapper.updateById(chapter);
        ensureStoryOverrideBinding(chapter, flow);
        return requireFlow(flow.getId());
    }

    private void ensureStoryOverrideBinding(StoryChapter chapter, ExperienceFlow flow) {
        ExperienceBinding binding = bindingMapper.selectOne(activeBindingQuery()
                .eq(ExperienceBinding::getOwnerType, OWNER_TYPE_STORY_CHAPTER)
                .eq(ExperienceBinding::getOwnerId, chapter.getId())
                .eq(ExperienceBinding::getBindingRole, BINDING_ROLE_STORY_OVERRIDE_FLOW)
                .eq(ExperienceBinding::getFlowId, flow.getId())
                .last("LIMIT 1"));
        if (binding == null) {
            binding = new ExperienceBinding();
        }
        binding.setOwnerType(OWNER_TYPE_STORY_CHAPTER);
        binding.setOwnerId(chapter.getId());
        binding.setOwnerCode("story_chapter_" + chapter.getId());
        binding.setBindingRole(BINDING_ROLE_STORY_OVERRIDE_FLOW);
        binding.setFlowId(flow.getId());
        binding.setPriority(20);
        binding.setInheritPolicy("override");
        binding.setStatus(flow.getStatus());
        binding.setSortOrder(defaultNumber(chapter.getSortOrder()));
        binding.setDeleted(0);
        if (binding.getId() == null) {
            bindingMapper.insert(binding);
        } else {
            bindingMapper.updateById(binding);
        }
    }

    private String resolveStoryModeConfigJson(AdminStorylineModeRequest.StoryModeConfigUpsert request) {
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled())) {
            Map<String, Object> advanced = readVersionedObject(request.getAdvancedStoryModeConfigJson(), "storyModeConfigJson", false);
            return writeJson(normalizeStoryModeConfig(advanced));
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("hideUnrelatedContent", Boolean.TRUE.equals(request.getHideUnrelatedContent()));
        values.put("nearbyRevealEnabled", Boolean.TRUE.equals(request.getNearbyRevealEnabled()));
        putValue(values, "nearbyRevealRadiusMeters", request.getNearbyRevealRadiusMeters());
        putText(values, "currentRouteHighlight", request.getCurrentRouteHighlight());
        putText(values, "inactiveRouteStyle", request.getInactiveRouteStyle());
        values.put("clearTemporaryProgressOnExit", Boolean.TRUE.equals(request.getClearTemporaryProgressOnExit()));
        values.put("preservePermanentEvents", !Boolean.FALSE.equals(request.getPreservePermanentEvents()));
        putText(values, "branchSourceType", request.getBranchSourceType());
        putText(values, "branchInsertPosition", request.getBranchInsertPosition());
        values.put("branchSkippable", Boolean.TRUE.equals(request.getBranchSkippable()));
        values.put("branchAffectsStoryProgress", Boolean.TRUE.equals(request.getBranchAffectsStoryProgress()));
        putList(values, "manualBranchPoiIds", request.getManualBranchPoiIds());
        return writeJson(normalizeStoryModeConfig(versionedMap(values)));
    }

    private Map<String, Object> normalizeStoryModeConfig(Map<String, Object> raw) {
        Map<String, Object> values = new LinkedHashMap<>(raw == null ? Collections.emptyMap() : raw);
        values.put("schemaVersion", readInteger(values.get("schemaVersion"), 1));
        Integer radius = readInteger(firstNonNull(values.get("nearbyRevealRadiusMeters"), values.get("nearbyRevealMeters")), null);
        if (radius != null) {
            values.put("nearbyRevealRadiusMeters", radius);
            values.put("nearbyRevealMeters", radius);
        }
        String routeStyle = readString(firstNonNull(values.get("currentRouteHighlight"), values.get("currentRouteStyle")));
        if (StringUtils.hasText(routeStyle)) {
            values.put("currentRouteHighlight", routeStyle);
            values.put("currentRouteStyle", routeStyle);
        }
        Boolean reset = readBoolean(firstNonNull(values.get("clearTemporaryProgressOnExit"), values.get("exitResetsSessionProgress")));
        if (reset != null) {
            values.put("clearTemporaryProgressOnExit", reset);
            values.put("exitResetsSessionProgress", reset);
        }
        values.putIfAbsent("hideUnrelatedContent", true);
        values.putIfAbsent("nearbyRevealEnabled", false);
        values.putIfAbsent("inactiveRouteStyle", "muted");
        values.putIfAbsent("preservePermanentEvents", true);
        return values;
    }

    private String resolveOverridePolicyJson(AdminStorylineModeRequest.ChapterOverridePolicyUpsert request) {
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled())) {
            return writeJson(readVersionedObject(request.getAdvancedOverridePolicyJson(), "overridePolicyJson", false));
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("inheritDefaultFlow", !Boolean.FALSE.equals(request.getInheritDefaultFlow()));
        values.put("disableDefaultArrivalMedia", Boolean.TRUE.equals(request.getDisableDefaultArrivalMedia()));
        values.put("appendStorySpecificRewards", Boolean.TRUE.equals(request.getAppendStorySpecificRewards()));
        values.put("supportedModes", AVAILABLE_OVERRIDE_MODES);
        return writeJson(versionedMap(values));
    }

    private String resolveOverrideConfigJson(AdminStorylineModeRequest.OverrideStepUpsert request) {
        if (Boolean.TRUE.equals(request.getAdvancedJsonEnabled())) {
            return writeJson(readVersionedObject(request.getAdvancedOverrideConfigJson(), "overrideConfigJson", false));
        }
        Map<String, Object> values = new LinkedHashMap<>();
        putText(values, "effectPreset", request.getEffectPreset());
        putValue(values, "mediaAssetId", request.getMediaAssetId());
        putList(values, "rewardRuleIds", request.getRewardRuleIds());
        putList(values, "pickupCodes", normalizeCodeList(request.getPickupCodes()));
        putText(values, "challengeCode", request.getChallengeCode());
        putText(values, "explorationWeightLevel", request.getExplorationWeightLevel());
        return writeJson(versionedMap(values));
    }

    private String resolveStepEffectJson(
            AdminStorylineModeRequest.ReplacementStepDraft draft,
            AdminStorylineModeRequest.OverrideStepUpsert overrideRequest) {
        if (StringUtils.hasText(draft.getEffectConfigJson())) {
            return normalizeVersionedJson(draft.getEffectConfigJson(), "effectConfigJson", false);
        }
        Map<String, Object> values = new LinkedHashMap<>();
        putText(values, "effectPreset", overrideRequest.getEffectPreset());
        putValue(values, "mediaAssetId", overrideRequest.getMediaAssetId());
        putList(values, "rewardRuleIds", overrideRequest.getRewardRuleIds());
        putList(values, "pickupCodes", normalizeCodeList(overrideRequest.getPickupCodes()));
        putText(values, "challengeCode", overrideRequest.getChallengeCode());
        return writeJson(versionedMap(values));
    }

    private AdminStorylineModeResponse.RouteStrategy toRouteStrategy(String storyModeConfigJson) {
        Map<String, Object> values = normalizeStoryModeConfig(readVersionedObject(storyModeConfigJson, "storyModeConfigJson", true));
        Map<String, Object> extra = new LinkedHashMap<>(values);
        List<String> known = List.of(
                "schemaVersion", "hideUnrelatedContent", "nearbyRevealEnabled", "nearbyRevealRadiusMeters", "nearbyRevealMeters",
                "currentRouteHighlight", "currentRouteStyle", "inactiveRouteStyle", "clearTemporaryProgressOnExit",
                "exitResetsSessionProgress", "preservePermanentEvents", "branchSourceType", "branchInsertPosition",
                "branchSkippable", "branchAffectsStoryProgress", "manualBranchPoiIds"
        );
        known.forEach(extra::remove);
        return AdminStorylineModeResponse.RouteStrategy.builder()
                .schemaVersion(readInteger(values.get("schemaVersion"), 1))
                .hideUnrelatedContent(readBoolean(values.get("hideUnrelatedContent")))
                .nearbyRevealEnabled(readBoolean(values.get("nearbyRevealEnabled")))
                .nearbyRevealRadiusMeters(readInteger(values.get("nearbyRevealRadiusMeters"), null))
                .nearbyRevealMeters(readInteger(values.get("nearbyRevealMeters"), null))
                .currentRouteHighlight(readString(values.get("currentRouteHighlight")))
                .currentRouteStyle(readString(values.get("currentRouteStyle")))
                .inactiveRouteStyle(readString(values.get("inactiveRouteStyle")))
                .clearTemporaryProgressOnExit(readBoolean(values.get("clearTemporaryProgressOnExit")))
                .exitResetsSessionProgress(readBoolean(values.get("exitResetsSessionProgress")))
                .preservePermanentEvents(readBoolean(values.get("preservePermanentEvents")))
                .branchSourceType(readString(values.get("branchSourceType")))
                .branchInsertPosition(readString(values.get("branchInsertPosition")))
                .branchSkippable(readBoolean(values.get("branchSkippable")))
                .branchAffectsStoryProgress(readBoolean(values.get("branchAffectsStoryProgress")))
                .manualBranchPoiIds(readLongList(values.get("manualBranchPoiIds")))
                .extra(extra)
                .build();
    }

    private AdminStorylineModeResponse.FlowSummary toFlowSummary(ExperienceFlow flow) {
        if (flow == null) {
            return null;
        }
        return AdminStorylineModeResponse.FlowSummary.builder()
                .id(flow.getId())
                .code(flow.getCode())
                .flowType(flow.getFlowType())
                .mode(flow.getMode())
                .nameZh(flow.getNameZh())
                .nameZht(flow.getNameZht())
                .descriptionZh(flow.getDescriptionZh())
                .descriptionZht(flow.getDescriptionZht())
                .status(flow.getStatus())
                .sortOrder(flow.getSortOrder())
                .publishedAt(flow.getPublishedAt())
                .steps(selectSteps(flow.getId()).stream().map(step -> toStepSummary(step, null)).toList())
                .build();
    }

    private AdminStorylineModeResponse.OverrideRule toOverrideRule(ExperienceOverride overrideRule, Map<Long, ExperienceFlowStep> replacementSteps) {
        ExperienceFlowStep replacementStep = overrideRule.getReplacementStepId() == null ? null : replacementSteps.get(overrideRule.getReplacementStepId());
        return AdminStorylineModeResponse.OverrideRule.builder()
                .id(overrideRule.getId())
                .ownerType(overrideRule.getOwnerType())
                .ownerId(overrideRule.getOwnerId())
                .targetOwnerType(overrideRule.getTargetOwnerType())
                .targetOwnerId(overrideRule.getTargetOwnerId())
                .targetStepCode(overrideRule.getTargetStepCode())
                .overrideMode(overrideRule.getOverrideMode())
                .replacementStepId(overrideRule.getReplacementStepId())
                .replacementStep(replacementStep == null ? null : toStepSummary(replacementStep, overrideRule.getOverrideMode()))
                .overrideConfigJson(overrideRule.getOverrideConfigJson())
                .status(overrideRule.getStatus())
                .sortOrder(overrideRule.getSortOrder())
                .createdAt(overrideRule.getCreatedAt())
                .updatedAt(overrideRule.getUpdatedAt())
                .build();
    }

    private AdminStorylineModeResponse.StepSummary toStepSummary(ExperienceFlowStep step, String overrideMode) {
        return AdminStorylineModeResponse.StepSummary.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .stepNameZh(step.getStepNameZh())
                .stepNameZht(step.getStepNameZht())
                .triggerType(step.getTriggerType())
                .mediaAssetId(step.getMediaAssetId())
                .rewardRuleIdsJson(step.getRewardRuleIdsJson())
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .status(step.getStatus())
                .sortOrder(step.getSortOrder())
                .overrideMode(overrideMode)
                .build();
    }

    private AdminStorylineModeResponse.StepSummary copyStepSummary(AdminStorylineModeResponse.StepSummary step, Integer sortOrder) {
        return AdminStorylineModeResponse.StepSummary.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .stepNameZh(step.getStepNameZh())
                .stepNameZht(step.getStepNameZht())
                .triggerType(step.getTriggerType())
                .mediaAssetId(step.getMediaAssetId())
                .rewardRuleIdsJson(step.getRewardRuleIdsJson())
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .status(step.getStatus())
                .sortOrder(sortOrder)
                .overrideMode(step.getOverrideMode())
                .build();
    }

    private List<AdminStorylineModeResponse.ValidationFinding> buildValidationFindings(
            StoryChapter chapter,
            ExperienceFlow inheritedFlow,
            List<ExperienceOverride> overrides) {
        List<AdminStorylineModeResponse.ValidationFinding> findings = new ArrayList<>();
        if (!StringUtils.hasText(chapter.getAnchorType()) || "manual".equals(chapter.getAnchorType())) {
            findings.add(finding("info", "manual_anchor", "章節使用手動錨點", "手動錨點不會自動繼承 POI 或室內預設流程。", chapter, null, null));
        } else if (inheritedFlow == null) {
            findings.add(finding("warning", "missing_inherited_flow", "錨點未找到預設體驗流程", "公開故事 runtime 仍可輸出章節專屬流程，但無法繼承該錨點原有 walk-in 體驗。", chapter, null, null));
        }
        for (ExperienceOverride overrideRule : overrides) {
            String mode = normalizeCode(overrideRule.getOverrideMode(), "inherit");
            if (("disable".equals(mode) || "replace".equals(mode)) && !StringUtils.hasText(overrideRule.getTargetStepCode())) {
                findings.add(finding("error", "override_missing_target", "覆寫缺少目標步驟", "停用或替換繼承步驟必須指定 targetStepCode。", chapter, null, overrideRule.getTargetStepCode()));
            }
            if (("replace".equals(mode) || "append".equals(mode)) && overrideRule.getReplacementStepId() == null) {
                findings.add(finding("error", "override_missing_replacement", "覆寫缺少替換步驟", "替換或追加模式必須綁定 replacementStepId。", chapter, null, overrideRule.getTargetStepCode()));
            }
        }
        return findings;
    }

    private AdminStorylineModeResponse.ValidationFinding finding(
            String severity,
            String findingType,
            String title,
            String description,
            StoryChapter chapter,
            Long stepId,
            String stepCode) {
        return AdminStorylineModeResponse.ValidationFinding.builder()
                .severity(severity)
                .findingType(findingType)
                .title(title)
                .description(description)
                .chapterId(chapter == null ? null : chapter.getId())
                .stepId(stepId)
                .stepCode(stepCode)
                .build();
    }

    private StoryLine requireStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
        return storyLine;
    }

    private StoryChapter requireChapter(Long storylineId, Long chapterId) {
        requireStoryline(storylineId);
        StoryChapter chapter = storyChapterMapper.selectById(chapterId);
        if (chapter == null || !Objects.equals(chapter.getStorylineId(), storylineId)) {
            throw new BusinessException(4044, "Story chapter not found");
        }
        return chapter;
    }

    private ExperienceFlow requireFlow(Long flowId) {
        ExperienceFlow flow = selectActiveFlow(flowId);
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

    private ExperienceOverride requireOverride(Long overrideId) {
        ExperienceOverride overrideRule = overrideMapper.selectOne(activeOverrideQuery().eq(ExperienceOverride::getId, overrideId));
        if (overrideRule == null) {
            throw new BusinessException(4075, "Experience override not found");
        }
        return overrideRule;
    }

    private ExperienceFlow selectActiveFlow(Long flowId) {
        if (flowId == null) {
            return null;
        }
        return flowMapper.selectOne(activeFlowQuery().eq(ExperienceFlow::getId, flowId));
    }

    private ExperienceFlow findDefaultFlow(String ownerType, Long ownerId, String ownerCode) {
        if (!StringUtils.hasText(ownerType)) {
            return null;
        }
        ExperienceBinding binding = bindingMapper.selectOne(activeBindingQuery()
                .eq(ExperienceBinding::getOwnerType, normalizeCode(ownerType, ""))
                .eq(ownerId != null, ExperienceBinding::getOwnerId, ownerId)
                .eq(ownerId == null && StringUtils.hasText(ownerCode), ExperienceBinding::getOwnerCode, ownerCode)
                .eq(ExperienceBinding::getBindingRole, BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW)
                .orderByDesc(ExperienceBinding::getPriority)
                .orderByAsc(ExperienceBinding::getSortOrder)
                .orderByAsc(ExperienceBinding::getId)
                .last("LIMIT 1"));
        return binding == null ? null : selectActiveFlow(binding.getFlowId());
    }

    private List<StoryChapter> selectChapters(Long storylineId) {
        return storyChapterMapper.selectList(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getStorylineId, storylineId)
                .orderByAsc(StoryChapter::getChapterOrder)
                .orderByAsc(StoryChapter::getId));
    }

    private List<ExperienceFlowStep> selectSteps(Long flowId) {
        if (flowId == null) {
            return Collections.emptyList();
        }
        return stepMapper.selectList(activeStepQuery()
                .eq(ExperienceFlowStep::getFlowId, flowId)
                .orderByAsc(ExperienceFlowStep::getSortOrder)
                .orderByAsc(ExperienceFlowStep::getId));
    }

    private List<ExperienceOverride> selectChapterOverrides(Long chapterId) {
        return overrideMapper.selectList(activeOverrideQuery()
                .eq(ExperienceOverride::getOwnerType, OWNER_TYPE_STORY_CHAPTER)
                .eq(ExperienceOverride::getOwnerId, chapterId)
                .orderByAsc(ExperienceOverride::getSortOrder)
                .orderByAsc(ExperienceOverride::getId));
    }

    private Map<Long, ExperienceFlowStep> loadStepsById(List<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return stepMapper.selectList(activeStepQuery().in(ExperienceFlowStep::getId, normalizedIds)).stream()
                .collect(Collectors.toMap(ExperienceFlowStep::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
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

    private String firstStoryModeConfig(List<StoryChapter> chapters) {
        return chapters.stream()
                .map(StoryChapter::getStoryModeConfigJson)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> readObject(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> readVersionedObject(String json, String fieldName, boolean emptyAllowed) {
        if (!StringUtils.hasText(json)) {
            if (emptyAllowed) {
                return versionedMap(Collections.emptyMap());
            }
            throw new BusinessException(4002, fieldName + " must be a JSON object with schemaVersion");
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (!node.isObject() || !node.path("schemaVersion").canConvertToInt() || node.path("schemaVersion").asInt() <= 0) {
                throw new BusinessException(4002, fieldName + " must be a JSON object with schemaVersion");
            }
            return objectMapper.convertValue(node, MAP_TYPE);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private String normalizeVersionedJson(String json, String fieldName, boolean emptyAllowed) {
        if (!StringUtils.hasText(json) && emptyAllowed) {
            return null;
        }
        return writeJson(readVersionedObject(json, fieldName, false));
    }

    private String writeRewardRuleIds(List<Long> rewardRuleIds) {
        if (rewardRuleIds == null || rewardRuleIds.isEmpty()) {
            return null;
        }
        return writeJson(rewardRuleIds.stream().filter(Objects::nonNull).distinct().toList());
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(4002, "JSON serialization failed");
        }
    }

    private Map<String, Object> versionedMap(Map<String, Object> values) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schemaVersion", 1);
        if (values != null) {
            result.putAll(values);
        }
        return result;
    }

    private void validateAnchorShape(String anchorType, Long anchorTargetId, String anchorTargetCode) {
        if ("manual".equals(anchorType)) {
            return;
        }
        if ("task".equals(anchorType) || "overlay".equals(anchorType)) {
            if (anchorTargetId == null && !StringUtils.hasText(anchorTargetCode)) {
                throw new BusinessException(4002, "Anchor target id or code is required");
            }
            return;
        }
        if (anchorTargetId == null) {
            throw new BusinessException(4002, "anchorTargetId is required for " + anchorType);
        }
    }

    private String normalizeAllowed(String value, String fallback, List<String> allowed, String fieldName) {
        String normalized = normalizeCode(value, fallback);
        if (!allowed.contains(normalized)) {
            throw new BusinessException(4002, fieldName + " must use Phase 30 canonical vocabulary");
        }
        return normalized;
    }

    private String normalizeCode(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT).replace('-', '_') : fallback;
    }

    private String defaultCode(String value, String fallback) {
        return normalizeCode(value, fallback);
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

    private void putList(Map<String, Object> values, String key, List<?> valuesToPut) {
        if (valuesToPut != null && !valuesToPut.isEmpty()) {
            values.put(key, valuesToPut);
        }
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

    private Object firstNonNull(Object first, Object second) {
        return first == null ? second : first;
    }

    private Integer readInteger(Object value, Integer fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private Boolean readBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            return Boolean.parseBoolean(stringValue.trim());
        }
        return null;
    }

    private String readString(Object value) {
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            return stringValue.trim();
        }
        return value == null ? null : String.valueOf(value);
    }

    private List<Long> readLongList(Object value) {
        if (!(value instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(item -> {
                    if (item instanceof Number number) {
                        return number.longValue();
                    }
                    if (item instanceof String stringValue && StringUtils.hasText(stringValue)) {
                        try {
                            return Long.parseLong(stringValue.trim());
                        } catch (NumberFormatException ignored) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void appendIfMissing(
            List<AdminStorylineModeResponse.StepSummary> compiled,
            List<AdminStorylineModeResponse.StepSummary> additionalSteps) {
        Set<String> existingKeys = compiled.stream().map(this::stepKey).collect(Collectors.toCollection(LinkedHashSet::new));
        for (AdminStorylineModeResponse.StepSummary step : additionalSteps) {
            if (existingKeys.add(stepKey(step))) {
                compiled.add(step);
            }
        }
    }

    private int findStepIndex(List<AdminStorylineModeResponse.StepSummary> compiled, String stepCode) {
        if (!StringUtils.hasText(stepCode)) {
            return -1;
        }
        for (int index = 0; index < compiled.size(); index++) {
            if (stepCode.trim().equals(stepKey(compiled.get(index)))) {
                return index;
            }
        }
        return -1;
    }

    private String stepKey(AdminStorylineModeResponse.StepSummary step) {
        return StringUtils.hasText(step.getStepCode()) ? step.getStepCode() : "step-" + step.getId();
    }

    private String chapterFlowCode(StoryChapter chapter) {
        return "story_chapter_" + chapter.getId() + "_flow";
    }
}
