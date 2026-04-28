package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.ExperienceEventRequest;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceEventResponse;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceRuntimeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryMediaAssetResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StorylineSessionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserExplorationResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.ExperienceBinding;
import com.aoxiaoyou.tripofmacau.entity.ExperienceFlow;
import com.aoxiaoyou.tripofmacau.entity.ExperienceFlowStep;
import com.aoxiaoyou.tripofmacau.entity.ExperienceOverride;
import com.aoxiaoyou.tripofmacau.entity.ExperienceTemplate;
import com.aoxiaoyou.tripofmacau.entity.ExplorationElement;
import com.aoxiaoyou.tripofmacau.entity.UserExplorationEvent;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExplorationElementMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserExplorationEventMapper;
import com.aoxiaoyou.tripofmacau.service.PublicExperienceService;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicExperienceServiceImpl implements PublicExperienceService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String STATUS_PUBLISHED = "published";
    private static final String BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW = "default_experience_flow";
    private static final String OVERRIDE_MODE_APPEND = "append";
    private static final String OVERRIDE_MODE_DISABLE = "disable";
    private static final String OVERRIDE_MODE_REPLACE = "replace";
    private static final Map<String, Integer> EXPLORATION_WEIGHT_VALUES = Map.of(
            "tiny", 1,
            "small", 2,
            "medium", 3,
            "large", 5,
            "core", 8);

    private final StoryLineService storyLineService;
    private final ExperienceTemplateMapper templateMapper;
    private final ExperienceFlowMapper flowMapper;
    private final ExperienceFlowStepMapper stepMapper;
    private final ExperienceBindingMapper bindingMapper;
    private final ExperienceOverrideMapper overrideMapper;
    private final ExplorationElementMapper explorationElementMapper;
    private final UserExplorationEventMapper userExplorationEventMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final LocalizedContentSupport localizedContentSupport;
    private final ObjectMapper objectMapper;

    @Override
    public ExperienceRuntimeResponse.Flow getPoiExperience(Long poiId, String localeHint) {
        ExperienceFlow flow = findDefaultFlow("poi", poiId, null)
                .orElseThrow(() -> new BusinessException(4071, "POI experience flow not found"));
        return toRuntimeFlow(flow, localeHint);
    }

    @Override
    public ExperienceRuntimeResponse.StorylineRuntime getStorylineRuntime(Long storylineId, String localeHint) {
        StoryLineResponse storyline = storyLineService.getDetail(storylineId, localeHint);
        List<StoryChapterResponse> chapters = storyline.getChapters() == null ? Collections.emptyList() : storyline.getChapters();
        List<ExperienceRuntimeResponse.StoryChapterRuntime> chapterRuntimes = chapters.stream()
                .map(chapter -> buildChapterRuntime(chapter, localeHint))
                .toList();
        ExperienceRuntimeResponse.StoryModeConfig storyModeConfig = chapterRuntimes.stream()
                .map(ExperienceRuntimeResponse.StoryChapterRuntime::getStoryModeConfig)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return ExperienceRuntimeResponse.StorylineRuntime.builder()
                .storyline(storyline)
                .storyModeConfig(storyModeConfig)
                .chapters(chapterRuntimes)
                .build();
    }

    @Override
    public ExperienceEventResponse recordEvent(Long userId, ExperienceEventRequest request) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        // Preserve client_event_id idempotency before the unique key handles duplicate writes.
        String clientEventId = defaultText(request.getClientEventId(), "server-" + UUID.randomUUID());
        UserExplorationEvent existingEvent = findEventByClientEventId(userId, clientEventId);
        if (existingEvent != null) {
            return toEventResponse(existingEvent);
        }
        ExplorationElement element = resolveExplorationElement(request.getElementId(), request.getElementCode());
        UserExplorationEvent event = new UserExplorationEvent();
        event.setUserId(userId);
        event.setElementId(element == null ? request.getElementId() : element.getId());
        event.setElementCode(defaultText(element == null ? request.getElementCode() : element.getElementCode(), ""));
        event.setEventType(requireText(request.getEventType(), "eventType"));
        event.setEventSource(defaultText(request.getEventSource(), "mini_program"));
        event.setStorylineSessionId(defaultText(request.getStorylineSessionId(), ""));
        event.setClientEventId(clientEventId);
        event.setEventPayloadJson(validateJson(request.getPayloadJson(), "payloadJson"));
        event.setOccurredAt(parseDateTime(request.getOccurredAt(), LocalDateTime.now()));
        event.setCreatedAt(LocalDateTime.now());
        try {
            userExplorationEventMapper.insert(event);
            return toEventResponse(event);
        } catch (DuplicateKeyException ex) {
            UserExplorationEvent duplicateEvent = findEventByClientEventId(userId, clientEventId);
            if (duplicateEvent != null) {
                return toEventResponse(duplicateEvent);
            }
            throw ex;
        }
    }

    @Override
    public StorylineSessionResponse startStorylineSession(Long userId, Long storylineId) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        storyLineService.getDetail(storylineId, "zh-Hant");
        return StorylineSessionResponse.builder()
                .storylineId(storylineId)
                .sessionId("story-" + storylineId + "-" + UUID.randomUUID())
                .status("started")
                .build();
    }

    @Override
    public StorylineSessionResponse exitStorylineSession(Long userId, Long storylineId, String sessionId) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        return StorylineSessionResponse.builder()
                .storylineId(storylineId)
                .sessionId(sessionId)
                .status("exited")
                .build();
    }

    @Override
    public UserExplorationResponse getUserExploration(Long userId, String localeHint, String scopeType, Long scopeId) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        List<ExplorationElement> elements = selectPublishedExplorationElements(scopeType, scopeId);
        Set<Long> elementIds = elements.stream().map(ExplorationElement::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> elementCodes = elements.stream().map(ExplorationElement::getElementCode).filter(StringUtils::hasText).collect(Collectors.toCollection(LinkedHashSet::new));
        List<UserExplorationEvent> events = selectCompletedEventsForElements(userId, elementIds, elementCodes);
        Set<Long> completedIds = events.stream().map(UserExplorationEvent::getElementId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> completedCodes = events.stream().map(UserExplorationEvent::getElementCode).filter(StringUtils::hasText).collect(Collectors.toSet());
        List<UserExplorationResponse.ElementProgress> elementProgress = elements.stream()
                .map(element -> {
                    boolean completed = completedIds.contains(element.getId()) || completedCodes.contains(element.getElementCode());
                    return UserExplorationResponse.ElementProgress.builder()
                            .elementId(element.getId())
                            .elementCode(element.getElementCode())
                            .elementType(element.getElementType())
                            .title(localizedContentSupport.resolveText(localeHint, element.getTitleZh(), element.getTitleEn(), element.getTitleZht(), element.getTitlePt()))
                            .weightLevel(element.getWeightLevel())
                            .weightValue(resolveExplorationWeightValue(element.getWeightLevel(), element.getWeightValue()))
                            .completed(completed)
                            .build();
                })
                .toList();
        int availableWeight = elementProgress.stream().mapToInt(UserExplorationResponse.ElementProgress::getWeightValue).sum();
        int completedWeight = elementProgress.stream()
                .filter(UserExplorationResponse.ElementProgress::isCompleted)
                .mapToInt(UserExplorationResponse.ElementProgress::getWeightValue)
                .sum();
        double percent = availableWeight == 0 ? 0 : Math.round((completedWeight * 10000.0 / availableWeight)) / 100.0;
        return UserExplorationResponse.builder()
                .userId(userId)
                .scopeType(defaultText(scopeType, "global"))
                .scopeId(scopeId)
                .completedWeight(completedWeight)
                .availableWeight(availableWeight)
                .progressPercent(percent)
                .elements(elementProgress)
                .build();
    }

    private ExperienceRuntimeResponse.StoryChapterRuntime buildChapterRuntime(StoryChapterResponse chapter, String localeHint) {
        ExperienceRuntimeResponse.Flow inheritedFlow = findDefaultFlow(chapter.getAnchorType(), chapter.getAnchorTargetId(), chapter.getAnchorTargetCode())
                .map(flow -> toRuntimeFlow(flow, localeHint))
                .orElse(null);
        ExperienceRuntimeResponse.Flow chapterFlow = chapter.getExperienceFlowId() == null
                ? null
                : toRuntimeFlow(requireFlow(chapter.getExperienceFlowId()), localeHint);
        List<ExperienceOverride> overrides = overrideMapper.selectList(new LambdaQueryWrapper<ExperienceOverride>()
                .eq(ExperienceOverride::getOwnerType, "story_chapter")
                .eq(ExperienceOverride::getOwnerId, chapter.getId())
                .eq(ExperienceOverride::getStatus, STATUS_PUBLISHED)
                .orderByAsc(ExperienceOverride::getSortOrder)
                .orderByAsc(ExperienceOverride::getId));
        List<ExperienceRuntimeResponse.Step> compiledSteps = compileSteps(inheritedFlow, chapterFlow, overrides, localeHint);
        return ExperienceRuntimeResponse.StoryChapterRuntime.builder()
                .chapterId(chapter.getId())
                .chapterOrder(chapter.getChapterOrder())
                .anchorType(chapter.getAnchorType())
                .anchorTargetId(chapter.getAnchorTargetId())
                .anchorTargetCode(chapter.getAnchorTargetCode())
                .overridePolicy(readObjectValue(chapter.getOverridePolicy()))
                .storyModeConfig(toStoryModeConfig(chapter.getStoryModeConfig()))
                .chapter(chapter)
                .inheritedFlow(inheritedFlow)
                .chapterFlow(chapterFlow)
                .overrides(overrides.stream().map(this::toOverrideResponse).toList())
                .compiledSteps(compiledSteps)
                .build();
    }

    private List<ExperienceRuntimeResponse.Step> compileSteps(
            ExperienceRuntimeResponse.Flow inheritedFlow,
            ExperienceRuntimeResponse.Flow chapterFlow,
            List<ExperienceOverride> overrides,
            String localeHint) {
        List<ExperienceRuntimeResponse.Step> compiled = new ArrayList<>();
        if (inheritedFlow != null && inheritedFlow.getSteps() != null) {
            compiled.addAll(inheritedFlow.getSteps());
        }
        for (ExperienceOverride override : overrides) {
            applyOverride(compiled, override, localeHint);
        }
        if (chapterFlow != null && chapterFlow.getSteps() != null) {
            appendIfMissing(compiled, chapterFlow.getSteps());
        }
        return normalizeCompiledSteps(compiled);
    }

    private ExperienceRuntimeResponse.Flow toRuntimeFlow(ExperienceFlow flow, String localeHint) {
        List<ExperienceFlowStep> steps = selectPublishedFlowSteps(flow.getId());
        Map<Long, ExperienceTemplate> templatesById = loadTemplatesById(steps.stream().map(ExperienceFlowStep::getTemplateId).toList());
        Map<Long, ContentAsset> assetsById = loadAssetsById(steps.stream().map(ExperienceFlowStep::getMediaAssetId).toList());
        return ExperienceRuntimeResponse.Flow.builder()
                .id(flow.getId())
                .code(flow.getCode())
                .flowType(flow.getFlowType())
                .mode(flow.getMode())
                .name(localizedContentSupport.resolveText(localeHint, flow.getNameZh(), flow.getNameEn(), flow.getNameZht(), flow.getNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, flow.getDescriptionZh(), flow.getDescriptionEn(), flow.getDescriptionZht(), flow.getDescriptionPt()))
                .mapPolicy(readObjectJson(flow.getMapPolicyJson()))
                .advancedConfig(readObjectJson(flow.getAdvancedConfigJson()))
                .steps(steps.stream().map(step -> toRuntimeStep(step, templatesById, assetsById, localeHint)).toList())
                .build();
    }

    private ExperienceRuntimeResponse.Step toRuntimeStep(
            ExperienceFlowStep step,
            Map<Long, ExperienceTemplate> templatesById,
            Map<Long, ContentAsset> assetsById,
            String localeHint) {
        ExperienceTemplate template = step.getTemplateId() == null ? null : templatesById.get(step.getTemplateId());
        return ExperienceRuntimeResponse.Step.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .name(localizedContentSupport.resolveText(localeHint, step.getStepNameZh(), step.getStepNameEn(), step.getStepNameZht(), step.getStepNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, step.getDescriptionZh(), step.getDescriptionEn(), step.getDescriptionZht(), step.getDescriptionPt()))
                .triggerType(step.getTriggerType())
                .triggerConfig(readObjectJson(step.getTriggerConfigJson()))
                .conditionConfig(readObjectJson(step.getConditionConfigJson()))
                .effectConfig(readObjectJson(step.getEffectConfigJson()))
                .mediaAssetId(step.getMediaAssetId())
                .mediaAsset(toStoryMediaAsset(assetsById.get(step.getMediaAssetId())))
                .rewardRuleIds(readJsonValue(step.getRewardRuleIdsJson()))
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .explorationWeightValue(resolveExplorationWeightValue(step.getExplorationWeightLevel(), null))
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .template(template == null ? null : toRuntimeTemplate(template, localeHint))
                .sortOrder(step.getSortOrder())
                .build();
    }

    private ExperienceRuntimeResponse.Template toRuntimeTemplate(ExperienceTemplate template, String localeHint) {
        return ExperienceRuntimeResponse.Template.builder()
                .id(template.getId())
                .code(template.getCode())
                .templateType(template.getTemplateType())
                .category(template.getCategory())
                .name(localizedContentSupport.resolveText(localeHint, template.getNameZh(), template.getNameEn(), template.getNameZht(), template.getNamePt()))
                .summary(localizedContentSupport.resolveText(localeHint, template.getSummaryZh(), template.getSummaryEn(), template.getSummaryZht(), template.getSummaryPt()))
                .config(readObjectJson(template.getConfigJson()))
                .riskLevel(template.getRiskLevel())
                .build();
    }

    private ExperienceRuntimeResponse.OverrideRule toOverrideResponse(ExperienceOverride override) {
        return ExperienceRuntimeResponse.OverrideRule.builder()
                .id(override.getId())
                .ownerType(override.getOwnerType())
                .ownerId(override.getOwnerId())
                .targetOwnerType(override.getTargetOwnerType())
                .targetOwnerId(override.getTargetOwnerId())
                .targetStepCode(override.getTargetStepCode())
                .overrideMode(override.getOverrideMode())
                .replacementStepId(override.getReplacementStepId())
                .overrideConfig(readObjectJson(override.getOverrideConfigJson()))
                .build();
    }

    private java.util.Optional<ExperienceFlow> findDefaultFlow(String ownerType, Long ownerId, String ownerCode) {
        if (!StringUtils.hasText(ownerType)) {
            return java.util.Optional.empty();
        }
        List<ExperienceBinding> bindings = bindingMapper.selectList(new LambdaQueryWrapper<ExperienceBinding>()
                .eq(ExperienceBinding::getOwnerType, normalizeToken(ownerType))
                .eq(ownerId != null, ExperienceBinding::getOwnerId, ownerId)
                .eq(ownerId == null && StringUtils.hasText(ownerCode), ExperienceBinding::getOwnerCode, ownerCode)
                .eq(ExperienceBinding::getBindingRole, BINDING_ROLE_DEFAULT_EXPERIENCE_FLOW)
                .eq(ExperienceBinding::getStatus, STATUS_PUBLISHED)
                .orderByDesc(ExperienceBinding::getPriority)
                .orderByAsc(ExperienceBinding::getSortOrder)
                .last("LIMIT 1"));
        if (bindings.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(requireFlow(bindings.get(0).getFlowId()));
    }

    private ExplorationElement resolveExplorationElement(Long elementId, String elementCode) {
        if (elementId != null) {
            return explorationElementMapper.selectById(elementId);
        }
        if (!StringUtils.hasText(elementCode)) {
            return null;
        }
        return explorationElementMapper.selectOne(new LambdaQueryWrapper<ExplorationElement>()
                .eq(ExplorationElement::getElementCode, elementCode)
                .last("LIMIT 1"));
    }

    private ExperienceFlow requireFlow(Long flowId) {
        ExperienceFlow flow = flowMapper.selectById(flowId);
        if (flow == null || !isPublishedStatus(flow.getStatus())) {
            throw new BusinessException(4071, "Experience flow not found");
        }
        return flow;
    }

    private ExperienceFlowStep requireStep(Long stepId) {
        ExperienceFlowStep step = stepMapper.selectById(stepId);
        if (step == null || !isPublishedStatus(step.getStatus())) {
            throw new BusinessException(4073, "Experience step not found");
        }
        return step;
    }

    private Map<Long, ExperienceTemplate> loadTemplatesById(Collection<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return templateMapper.selectList(new LambdaQueryWrapper<ExperienceTemplate>()
                        .in(ExperienceTemplate::getId, normalizedIds)
                        .eq(ExperienceTemplate::getStatus, STATUS_PUBLISHED))
                .stream()
                .collect(Collectors.toMap(ExperienceTemplate::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, ContentAsset> loadAssetsById(Collection<Long> ids) {
        List<Long> normalizedIds = ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return contentAssetMapper.selectBatchIds(normalizedIds).stream()
                .collect(Collectors.toMap(ContentAsset::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private StoryMediaAssetResponse toStoryMediaAsset(ContentAsset asset) {
        if (asset == null) {
            return null;
        }
        return StoryMediaAssetResponse.builder()
                .id(asset.getId())
                .assetKind(asset.getAssetKind())
                .url(asset.getCanonicalUrl())
                .mimeType(asset.getMimeType())
                .originalFilename(asset.getOriginalFilename())
                .widthPx(asset.getWidthPx())
                .heightPx(asset.getHeightPx())
                .animationSubtype(asset.getAnimationSubtype())
                .defaultLoop(asset.getDefaultLoop())
                .defaultAutoplay(asset.getDefaultAutoplay())
                .posterAssetId(asset.getPosterAssetId())
                .fallbackAssetId(asset.getFallbackAssetId())
                .build();
    }

    private Map<String, Object> readObjectJson(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> readObjectValue(Object value) {
        if (value == null) {
            return Collections.emptyMap();
        }
        if (value instanceof Map<?, ?> mapValue) {
            LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();
            mapValue.forEach((key, mapEntryValue) -> normalized.put(String.valueOf(key), mapEntryValue));
            return normalized;
        }
        if (value instanceof String stringValue) {
            return readObjectJson(stringValue);
        }
        try {
            return objectMapper.convertValue(value, MAP_TYPE);
        } catch (IllegalArgumentException ignored) {
            return Collections.emptyMap();
        }
    }

    private Object readJsonValue(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception ignored) {
            return json;
        }
    }

    private ExperienceRuntimeResponse.StoryModeConfig toStoryModeConfig(Object value) {
        Map<String, Object> raw = readObjectValue(value);
        if (raw.isEmpty()) {
            return null;
        }
        Map<String, Object> extra = new LinkedHashMap<>(raw);
        extra.remove("schemaVersion");
        extra.remove("hideUnrelatedContent");
        extra.remove("nearbyRevealMeters");
        extra.remove("currentRouteStyle");
        extra.remove("inactiveRouteStyle");
        extra.remove("exitResetsSessionProgress");
        return ExperienceRuntimeResponse.StoryModeConfig.builder()
                .schemaVersion(readInteger(raw.get("schemaVersion")))
                .hideUnrelatedContent(readBoolean(raw.get("hideUnrelatedContent")))
                .nearbyRevealMeters(readInteger(raw.get("nearbyRevealMeters")))
                .currentRouteStyle(readString(raw.get("currentRouteStyle")))
                .inactiveRouteStyle(readString(raw.get("inactiveRouteStyle")))
                .exitResetsSessionProgress(readBoolean(raw.get("exitResetsSessionProgress")))
                .extra(extra.isEmpty() ? Collections.emptyMap() : extra)
                .build();
    }

    private String validateJson(String json, String fieldName) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            objectMapper.readTree(json);
            return json.trim();
        } catch (Exception ex) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return LocalDateTime.parse(value);
    }

    private List<ExperienceFlowStep> selectPublishedFlowSteps(Long flowId) {
        return stepMapper.selectList(new LambdaQueryWrapper<ExperienceFlowStep>()
                .eq(ExperienceFlowStep::getFlowId, flowId)
                .eq(ExperienceFlowStep::getStatus, STATUS_PUBLISHED)
                .orderByAsc(ExperienceFlowStep::getSortOrder)
                .orderByAsc(ExperienceFlowStep::getId));
    }

    private List<ExplorationElement> selectPublishedExplorationElements(String scopeType, Long scopeId) {
        String normalizedScopeType = normalizeToken(scopeType);
        return explorationElementMapper.selectList(new LambdaQueryWrapper<ExplorationElement>()
                .eq(ExplorationElement::getStatus, STATUS_PUBLISHED)
                .eq(ExplorationElement::getIncludeInExploration, true)
                .eq("city".equals(normalizedScopeType) && scopeId != null, ExplorationElement::getCityId, scopeId)
                .eq("sub_map".equals(normalizedScopeType) && scopeId != null, ExplorationElement::getSubMapId, scopeId)
                .eq("storyline".equals(normalizedScopeType) && scopeId != null, ExplorationElement::getStorylineId, scopeId)
                .eq("story_chapter".equals(normalizedScopeType) && scopeId != null, ExplorationElement::getStoryChapterId, scopeId)
                .orderByAsc(ExplorationElement::getSortOrder)
                .orderByAsc(ExplorationElement::getId));
    }

    private List<UserExplorationEvent> selectCompletedEventsForElements(Long userId, Set<Long> elementIds, Set<String> elementCodes) {
        return userExplorationEventMapper.selectList(new LambdaQueryWrapper<UserExplorationEvent>()
                .eq(UserExplorationEvent::getUserId, userId)
                .and(!elementIds.isEmpty() || !elementCodes.isEmpty(), q -> {
                    if (!elementIds.isEmpty()) {
                        q.in(UserExplorationEvent::getElementId, elementIds);
                    }
                    if (!elementCodes.isEmpty()) {
                        if (!elementIds.isEmpty()) {
                            q.or();
                        }
                        q.in(UserExplorationEvent::getElementCode, elementCodes);
                    }
                }));
    }

    private UserExplorationEvent findEventByClientEventId(Long userId, String clientEventId) {
        if (userId == null || !StringUtils.hasText(clientEventId)) {
            return null;
        }
        return userExplorationEventMapper.selectOne(new LambdaQueryWrapper<UserExplorationEvent>()
                .eq(UserExplorationEvent::getUserId, userId)
                .eq(UserExplorationEvent::getClientEventId, clientEventId)
                .last("LIMIT 1"));
    }

    private ExperienceEventResponse toEventResponse(UserExplorationEvent event) {
        return ExperienceEventResponse.builder()
                .accepted(true)
                .eventId(event.getId())
                .userId(event.getUserId())
                .elementId(event.getElementId())
                .elementCode(event.getElementCode())
                .eventType(event.getEventType())
                .storylineSessionId(event.getStorylineSessionId())
                .build();
    }

    private void applyOverride(List<ExperienceRuntimeResponse.Step> compiled, ExperienceOverride override, String localeHint) {
        String overrideMode = normalizeToken(override.getOverrideMode());
        String targetKey = StringUtils.hasText(override.getTargetStepCode()) ? override.getTargetStepCode().trim() : null;
        int targetIndex = findStepIndex(compiled, targetKey);
        if (OVERRIDE_MODE_DISABLE.equals(overrideMode)) {
            if (targetIndex >= 0) {
                compiled.remove(targetIndex);
            }
            return;
        }
        if (!OVERRIDE_MODE_REPLACE.equals(overrideMode) && !OVERRIDE_MODE_APPEND.equals(overrideMode)) {
            return;
        }
        if (override.getReplacementStepId() == null) {
            return;
        }
        ExperienceFlowStep replacementStep = requireStep(override.getReplacementStepId());
        ExperienceRuntimeResponse.Step replacement = toRuntimeStep(
                replacementStep,
                loadTemplatesById(Collections.singletonList(replacementStep.getTemplateId())),
                loadAssetsById(Collections.singletonList(replacementStep.getMediaAssetId())),
                localeHint);
        if (OVERRIDE_MODE_REPLACE.equals(overrideMode) && targetIndex >= 0) {
            compiled.set(targetIndex, copyStep(replacement, compiled.get(targetIndex).getSortOrder()));
            return;
        }
        if (OVERRIDE_MODE_APPEND.equals(overrideMode) && targetIndex >= 0) {
            compiled.add(targetIndex + 1, replacement);
            return;
        }
        compiled.add(replacement);
    }

    private void appendIfMissing(List<ExperienceRuntimeResponse.Step> compiled, List<ExperienceRuntimeResponse.Step> additionalSteps) {
        Set<String> existingKeys = compiled.stream()
                .map(this::stepKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (ExperienceRuntimeResponse.Step step : additionalSteps) {
            if (existingKeys.add(stepKey(step))) {
                compiled.add(step);
            }
        }
    }

    private List<ExperienceRuntimeResponse.Step> normalizeCompiledSteps(List<ExperienceRuntimeResponse.Step> steps) {
        if (steps.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExperienceRuntimeResponse.Step> normalized = new ArrayList<>(steps.size());
        for (int index = 0; index < steps.size(); index++) {
            normalized.add(copyStep(steps.get(index), index + 1));
        }
        return normalized;
    }

    private ExperienceRuntimeResponse.Step copyStep(ExperienceRuntimeResponse.Step step, Integer sortOrder) {
        return ExperienceRuntimeResponse.Step.builder()
                .id(step.getId())
                .flowId(step.getFlowId())
                .stepCode(step.getStepCode())
                .stepType(step.getStepType())
                .name(step.getName())
                .description(step.getDescription())
                .triggerType(step.getTriggerType())
                .triggerConfig(step.getTriggerConfig())
                .conditionConfig(step.getConditionConfig())
                .effectConfig(step.getEffectConfig())
                .mediaAssetId(step.getMediaAssetId())
                .mediaAsset(step.getMediaAsset())
                .rewardRuleIds(step.getRewardRuleIds())
                .explorationWeightLevel(step.getExplorationWeightLevel())
                .explorationWeightValue(step.getExplorationWeightValue())
                .requiredForCompletion(step.getRequiredForCompletion())
                .inheritKey(step.getInheritKey())
                .template(step.getTemplate())
                .sortOrder(sortOrder)
                .build();
    }

    private int findStepIndex(List<ExperienceRuntimeResponse.Step> compiled, String targetKey) {
        if (!StringUtils.hasText(targetKey)) {
            return -1;
        }
        for (int index = 0; index < compiled.size(); index++) {
            if (targetKey.equals(stepKey(compiled.get(index)))) {
                return index;
            }
        }
        return -1;
    }

    private String stepKey(ExperienceRuntimeResponse.Step step) {
        return StringUtils.hasText(step.getStepCode()) ? step.getStepCode() : "step-" + step.getId();
    }

    private String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4002, fieldName + " is required");
        }
        return value.trim();
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private Integer readInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
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

    private int resolveExplorationWeightValue(String weightLevel, Integer weightValue) {
        if (weightValue != null && weightValue > 0) {
            return weightValue;
        }
        return EXPLORATION_WEIGHT_VALUES.getOrDefault(normalizeToken(defaultText(weightLevel, "small")), 2);
    }

    private boolean isPublishedStatus(String status) {
        return STATUS_PUBLISHED.equalsIgnoreCase(defaultText(status, ""));
    }

    private String normalizeToken(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }
}
