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
import com.aoxiaoyou.tripofmacau.entity.UserStorylineSession;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExplorationElementMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserExplorationEventMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserStorylineSessionMapper;
import com.aoxiaoyou.tripofmacau.service.PublicExperienceService;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
    private static final String OVERRIDE_MODE_INHERIT = "inherit";
    private static final String OVERRIDE_MODE_REPLACE = "replace";
    private static final String SCOPE_GLOBAL = "global";
    private static final String SESSION_STATUS_EXITED = "exited";
    private static final String SESSION_STATUS_STARTED = "started";
    private static final String OWNER_TYPE_EXPERIENCE_FLOW_STEP = "experience_flow_step";
    private static final String OWNER_TYPE_COLLECTIBLE = "collectible";
    private static final String OWNER_TYPE_REWARD = "reward";
    private static final String OWNER_TYPE_CONTENT_ASSET = "content_asset";
    private static final String DEFAULT_SESSION_LOCALE = "zh-Hant";
    private static final String EMPTY_JSON_OBJECT = "{}";
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
    private UserStorylineSessionMapper userStorylineSessionMapper;

    @Autowired
    void setUserStorylineSessionMapper(UserStorylineSessionMapper userStorylineSessionMapper) {
        this.userStorylineSessionMapper = userStorylineSessionMapper;
    }

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
            updateStorylineSessionAfterEvent(userId, request, event, element);
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
        StoryLineResponse storyline = storyLineService.getDetail(storylineId, DEFAULT_SESSION_LOCALE);
        UserStorylineSession activeSession = findActiveStorylineSession(userId, storylineId);
        if (activeSession != null) {
            return toSessionResponse(activeSession);
        }
        LocalDateTime now = LocalDateTime.now();
        UserStorylineSession session = new UserStorylineSession();
        session.setSessionId("story-" + storylineId + "-" + UUID.randomUUID());
        session.setUserId(userId);
        session.setStorylineId(storylineId);
        session.setCurrentChapterId(resolveInitialChapterId(storyline));
        session.setStatus(SESSION_STATUS_STARTED);
        session.setStartedAt(now);
        session.setEventCount(0);
        session.setTemporaryStepStateJson(EMPTY_JSON_OBJECT);
        session.setExitClearedTemporaryState(false);
        requireUserStorylineSessionMapper().insert(session);
        return toSessionResponse(session);
    }

    @Override
    public StorylineSessionResponse exitStorylineSession(Long userId, Long storylineId, String sessionId) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        UserStorylineSession session = findStorylineSession(userId, storylineId, sessionId);
        if (session == null) {
            throw new BusinessException(4044, "Storyline session not found");
        }
        UserStorylineSession exitedSession = copyStorylineSession(session);
        exitedSession.setStatus(SESSION_STATUS_EXITED);
        exitedSession.setExitedAt(LocalDateTime.now());
        exitedSession.setTemporaryStepStateJson(EMPTY_JSON_OBJECT);
        exitedSession.setExitClearedTemporaryState(true);
        requireUserStorylineSessionMapper().updateById(exitedSession);
        return toSessionResponse(exitedSession);
    }

    @Override
    public UserExplorationResponse getUserExploration(Long userId, String localeHint, String scopeType, Long scopeId) {
        if (userId == null) {
            throw new BusinessException(4010, "Unauthorized");
        }
        String normalizedScopeType = normalizeScopeType(scopeType);
        List<ExplorationElement> activeElements = selectActiveExplorationElements(normalizedScopeType, scopeId);
        Set<Long> activeElementIds = activeElements.stream()
                .map(ExplorationElement::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> activeElementCodes = activeElements.stream()
                .map(ExplorationElement::getElementCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<UserExplorationEvent> activeEvents = selectCompletedEventsForElements(userId, activeElementIds, activeElementCodes);
        List<UserExplorationEvent> allEvents = mergeCompletionEvents(activeEvents, selectUserEvents(userId));
        Map<String, UserExplorationEvent> completionEventsByKey = indexCompletionEvents(allEvents);

        List<UserExplorationResponse.ElementProgress> activeProgress = activeElements.stream()
                .map(element -> toElementProgress(element, completionEventsByKey, localeHint, true))
                .toList();
        List<UserExplorationResponse.ElementProgress> retiredProgress = loadRetiredCompletedElements(activeElements, completionEventsByKey, normalizedScopeType, scopeId)
                .stream()
                .map(element -> toElementProgress(element, completionEventsByKey, localeHint, false))
                .toList();
        List<UserExplorationResponse.ElementProgress> elementProgress = Stream.concat(activeProgress.stream(), retiredProgress.stream()).toList();

        int availableWeight = activeProgress.stream().mapToInt(UserExplorationResponse.ElementProgress::getWeightValue).sum();
        int completedWeight = activeProgress.stream()
                .filter(UserExplorationResponse.ElementProgress::isCompleted)
                .mapToInt(UserExplorationResponse.ElementProgress::getWeightValue)
                .sum();
        int completedElementCount = (int) activeProgress.stream()
                .filter(UserExplorationResponse.ElementProgress::isCompleted)
                .count();
        double percent = availableWeight == 0 ? 0 : Math.round((completedWeight * 10000.0 / availableWeight)) / 100.0;
        return UserExplorationResponse.builder()
                .userId(userId)
                .scopeType(normalizedScopeType)
                .scopeId(scopeId)
                .completedWeight(completedWeight)
                .availableWeight(availableWeight)
                .completedElementCount(completedElementCount)
                .availableElementCount(activeProgress.size())
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
        List.of(
                "schemaVersion",
                "hideUnrelatedContent",
                "nearbyRevealEnabled",
                "nearbyRevealRadiusMeters",
                "nearbyRevealMeters",
                "currentRouteHighlight",
                "currentRouteStyle",
                "inactiveRouteStyle",
                "clearTemporaryProgressOnExit",
                "exitResetsSessionProgress",
                "preservePermanentEvents",
                "branchSourceType",
                "branchInsertPosition",
                "branchSkippable",
                "branchAffectsStoryProgress",
                "manualBranchPoiIds"
        ).forEach(extra::remove);
        Integer nearbyRevealRadiusMeters = readInteger(firstNonNull(raw.get("nearbyRevealRadiusMeters"), raw.get("nearbyRevealMeters")));
        String currentRouteHighlight = readString(firstNonNull(raw.get("currentRouteHighlight"), raw.get("currentRouteStyle")));
        Boolean clearTemporaryProgressOnExit = readBoolean(firstNonNull(raw.get("clearTemporaryProgressOnExit"), raw.get("exitResetsSessionProgress")));
        return ExperienceRuntimeResponse.StoryModeConfig.builder()
                .schemaVersion(readInteger(raw.get("schemaVersion")))
                .hideUnrelatedContent(readBoolean(raw.get("hideUnrelatedContent")))
                .nearbyRevealEnabled(readBoolean(raw.get("nearbyRevealEnabled")))
                .nearbyRevealRadiusMeters(nearbyRevealRadiusMeters)
                .nearbyRevealMeters(nearbyRevealRadiusMeters)
                .currentRouteHighlight(currentRouteHighlight)
                .currentRouteStyle(currentRouteHighlight)
                .inactiveRouteStyle(readString(raw.get("inactiveRouteStyle")))
                .clearTemporaryProgressOnExit(clearTemporaryProgressOnExit)
                .exitResetsSessionProgress(clearTemporaryProgressOnExit)
                .preservePermanentEvents(readBoolean(raw.get("preservePermanentEvents")))
                .branchSourceType(readString(raw.get("branchSourceType")))
                .branchInsertPosition(readString(raw.get("branchInsertPosition")))
                .branchSkippable(readBoolean(raw.get("branchSkippable")))
                .branchAffectsStoryProgress(readBoolean(raw.get("branchAffectsStoryProgress")))
                .manualBranchPoiIds(readLongList(raw.get("manualBranchPoiIds")))
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

    private List<ExplorationElement> selectActiveExplorationElements(String scopeType, Long scopeId) {
        LambdaQueryWrapper<ExplorationElement> query = new LambdaQueryWrapper<ExplorationElement>()
                .eq(ExplorationElement::getStatus, STATUS_PUBLISHED)
                .eq(ExplorationElement::getIncludeInExploration, true);
        applyScopeFilter(query, scopeType, scopeId);
        return explorationElementMapper.selectList(query
                .orderByAsc(ExplorationElement::getSortOrder)
                .orderByAsc(ExplorationElement::getId));
    }

    private List<UserExplorationEvent> selectCompletedEventsForElements(Long userId, Set<Long> elementIds, Set<String> elementCodes) {
        if (elementIds.isEmpty() && elementCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return userExplorationEventMapper.selectList(new LambdaQueryWrapper<UserExplorationEvent>()
                .eq(UserExplorationEvent::getUserId, userId)
                .and(q -> {
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

    private List<UserExplorationEvent> selectUserEvents(Long userId) {
        return userExplorationEventMapper.selectList(new LambdaQueryWrapper<UserExplorationEvent>()
                .eq(UserExplorationEvent::getUserId, userId)
                .orderByAsc(UserExplorationEvent::getOccurredAt)
                .orderByAsc(UserExplorationEvent::getId));
    }

    private void applyScopeFilter(LambdaQueryWrapper<ExplorationElement> query, String scopeType, Long scopeId) {
        if (scopeId == null || SCOPE_GLOBAL.equals(scopeType)) {
            return;
        }
        switch (scopeType) {
            case "city" -> query.eq(ExplorationElement::getCityId, scopeId);
            case "sub_map" -> query.eq(ExplorationElement::getSubMapId, scopeId);
            case "poi" -> query.eq(ExplorationElement::getPoiId, scopeId);
            case "indoor_building" -> query.eq(ExplorationElement::getIndoorBuildingId, scopeId);
            case "indoor_floor" -> query.eq(ExplorationElement::getIndoorFloorId, scopeId);
            case "storyline" -> query.eq(ExplorationElement::getStorylineId, scopeId);
            case "story_chapter" -> query.eq(ExplorationElement::getStoryChapterId, scopeId);
            case "task" -> applyOwnerScopeFilter(query, OWNER_TYPE_EXPERIENCE_FLOW_STEP, scopeId);
            case "collectible" -> applyOwnerScopeFilter(query, OWNER_TYPE_COLLECTIBLE, scopeId);
            case "reward" -> applyOwnerScopeFilter(query, OWNER_TYPE_REWARD, scopeId);
            case "media" -> applyOwnerScopeFilter(query, OWNER_TYPE_CONTENT_ASSET, scopeId);
            default -> {
            }
        }
    }

    private void applyOwnerScopeFilter(LambdaQueryWrapper<ExplorationElement> query, String ownerType, Long scopeId) {
        query.eq(ExplorationElement::getOwnerType, ownerType)
                .eq(ExplorationElement::getOwnerId, scopeId);
    }

    private List<ExplorationElement> selectExplorationElementsByIdsOrCodes(Set<Long> elementIds, Set<String> elementCodes) {
        if (elementIds.isEmpty() && elementCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return explorationElementMapper.selectList(new LambdaQueryWrapper<ExplorationElement>()
                .and(query -> {
                    if (!elementIds.isEmpty()) {
                        query.in(ExplorationElement::getId, elementIds);
                    }
                    if (!elementCodes.isEmpty()) {
                        if (!elementIds.isEmpty()) {
                            query.or();
                        }
                        query.in(ExplorationElement::getElementCode, elementCodes);
                    }
                })
                .orderByAsc(ExplorationElement::getSortOrder)
                .orderByAsc(ExplorationElement::getId));
    }

    private List<ExplorationElement> loadRetiredCompletedElements(
            List<ExplorationElement> activeElements,
            Map<String, UserExplorationEvent> completionEventsByKey,
            String scopeType,
            Long scopeId) {
        if (completionEventsByKey.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> activeKeys = activeElements.stream()
                .flatMap(element -> streamElementKeys(element))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> retiredIds = new LinkedHashSet<>();
        Set<String> retiredCodes = new LinkedHashSet<>();
        new LinkedHashSet<>(completionEventsByKey.values()).forEach(event -> {
            boolean matchesActive = activeKeys.contains(eventIdentityKey(event.getElementId(), null))
                    || activeKeys.contains(eventIdentityKey(null, event.getElementCode()));
            if (!matchesActive) {
                if (event.getElementId() != null) {
                    retiredIds.add(event.getElementId());
                }
                if (StringUtils.hasText(event.getElementCode())) {
                    retiredCodes.add(event.getElementCode().trim());
                }
            }
        });
        if (retiredIds.isEmpty() && retiredCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return selectExplorationElementsByIdsOrCodes(retiredIds, retiredCodes).stream()
                .filter(element -> !activeKeys.contains(elementIdentityKey(element)))
                .filter(element -> matchesScope(element, scopeType, scopeId))
                .filter(element -> completionEventFor(element, completionEventsByKey) != null)
                .filter(element -> !isActiveElement(element))
                .toList();
    }

    private UserExplorationResponse.ElementProgress toElementProgress(
            ExplorationElement element,
            Map<String, UserExplorationEvent> completionEventsByKey,
            String localeHint,
            boolean includedInCurrentPercentage) {
        UserExplorationEvent sourceEvent = completionEventFor(element, completionEventsByKey);
        return UserExplorationResponse.ElementProgress.builder()
                .elementId(element.getId())
                .elementCode(element.getElementCode())
                .elementType(element.getElementType())
                .title(localizedContentSupport.resolveText(localeHint, element.getTitleZh(), element.getTitleEn(), element.getTitleZht(), element.getTitlePt()))
                .weightLevel(element.getWeightLevel())
                .weightValue(resolveExplorationWeightValue(element.getWeightLevel(), element.getWeightValue()))
                .completed(sourceEvent != null)
                .includedInCurrentPercentage(includedInCurrentPercentage)
                .sourceEventId(sourceEvent == null ? null : sourceEvent.getId())
                .eventOccurredAt(sourceEvent == null ? null : sourceEvent.getOccurredAt())
                .build();
    }

    private Map<String, UserExplorationEvent> indexCompletionEvents(List<UserExplorationEvent> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, UserExplorationEvent> indexed = new LinkedHashMap<>();
        events.stream()
                .filter(Objects::nonNull)
                .sorted((left, right) -> {
                    LocalDateTime leftTime = left.getOccurredAt() == null ? LocalDateTime.MIN : left.getOccurredAt();
                    LocalDateTime rightTime = right.getOccurredAt() == null ? LocalDateTime.MIN : right.getOccurredAt();
                    int timeCompare = leftTime.compareTo(rightTime);
                    if (timeCompare != 0) {
                        return timeCompare;
                    }
                    long leftId = left.getId() == null ? Long.MAX_VALUE : left.getId();
                    long rightId = right.getId() == null ? Long.MAX_VALUE : right.getId();
                    return Long.compare(leftId, rightId);
                })
                .forEach(event -> {
                    String idKey = eventIdentityKey(event.getElementId(), null);
                    if (idKey != null) {
                        indexed.putIfAbsent(idKey, event);
                    }
                    String codeKey = eventIdentityKey(null, event.getElementCode());
                    if (codeKey != null) {
                        indexed.putIfAbsent(codeKey, event);
                    }
                });
        return indexed;
    }

    private List<UserExplorationEvent> mergeCompletionEvents(List<UserExplorationEvent> first, List<UserExplorationEvent> second) {
        LinkedHashMap<Long, UserExplorationEvent> merged = new LinkedHashMap<>();
        Stream.of(first, second)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .forEach(event -> {
                    Long eventId = event.getId();
                    if (eventId != null) {
                        merged.putIfAbsent(eventId, event);
                    } else {
                        merged.put((long) merged.size() + 1, event);
                    }
                });
        return new ArrayList<>(merged.values());
    }

    private UserExplorationEvent completionEventFor(ExplorationElement element, Map<String, UserExplorationEvent> completionEventsByKey) {
        UserExplorationEvent byId = completionEventsByKey.get(eventIdentityKey(element.getId(), null));
        if (byId != null) {
            return byId;
        }
        return completionEventsByKey.get(eventIdentityKey(null, element.getElementCode()));
    }

    private boolean matchesScope(ExplorationElement element, String scopeType, Long scopeId) {
        if (scopeId == null || SCOPE_GLOBAL.equals(scopeType)) {
            return true;
        }
        return switch (scopeType) {
            case "city" -> Objects.equals(element.getCityId(), scopeId);
            case "sub_map" -> Objects.equals(element.getSubMapId(), scopeId);
            case "poi" -> Objects.equals(element.getPoiId(), scopeId);
            case "indoor_building" -> Objects.equals(element.getIndoorBuildingId(), scopeId);
            case "indoor_floor" -> Objects.equals(element.getIndoorFloorId(), scopeId);
            case "storyline" -> Objects.equals(element.getStorylineId(), scopeId);
            case "story_chapter" -> Objects.equals(element.getStoryChapterId(), scopeId);
            case "task" -> matchesOwnerScope(element, OWNER_TYPE_EXPERIENCE_FLOW_STEP, scopeId);
            case "collectible" -> matchesOwnerScope(element, OWNER_TYPE_COLLECTIBLE, scopeId);
            case "reward" -> matchesOwnerScope(element, OWNER_TYPE_REWARD, scopeId);
            case "media" -> matchesOwnerScope(element, OWNER_TYPE_CONTENT_ASSET, scopeId);
            default -> false;
        };
    }

    private boolean matchesOwnerScope(ExplorationElement element, String ownerType, Long scopeId) {
        return ownerType.equals(normalizeToken(element.getOwnerType())) && Objects.equals(element.getOwnerId(), scopeId);
    }

    private boolean isActiveElement(ExplorationElement element) {
        return isPublishedStatus(element.getStatus()) && Boolean.TRUE.equals(element.getIncludeInExploration());
    }

    private String normalizeScopeType(String scopeType) {
        String normalized = normalizeToken(scopeType);
        return StringUtils.hasText(normalized) ? normalized : SCOPE_GLOBAL;
    }

    private Stream<String> streamElementKeys(ExplorationElement element) {
        return Stream.of(
                eventIdentityKey(element.getId(), null),
                eventIdentityKey(null, element.getElementCode()),
                elementIdentityKey(element)
        ).filter(StringUtils::hasText);
    }

    private String elementIdentityKey(ExplorationElement element) {
        if (element.getId() != null) {
            return eventIdentityKey(element.getId(), null);
        }
        return eventIdentityKey(null, element.getElementCode());
    }

    private String eventIdentityKey(Long elementId, String elementCode) {
        if (elementId != null) {
            return "id:" + elementId;
        }
        if (StringUtils.hasText(elementCode)) {
            return "code:" + elementCode.trim();
        }
        return null;
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
        if (OVERRIDE_MODE_INHERIT.equals(overrideMode)) {
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

    private void updateStorylineSessionAfterEvent(
            Long userId,
            ExperienceEventRequest request,
            UserExplorationEvent event,
            ExplorationElement element) {
        String sessionId = event.getStorylineSessionId();
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        UserStorylineSession session = findStorylineSession(userId, null, sessionId);
        if (session == null || SESSION_STATUS_EXITED.equals(normalizeToken(session.getStatus()))) {
            return;
        }
        UserStorylineSession updatedSession = copyStorylineSession(session);
        updatedSession.setStatus(SESSION_STATUS_STARTED);
        updatedSession.setCurrentChapterId(resolveCurrentChapterId(session, element, request.getPayloadJson()));
        updatedSession.setLastEventAt(event.getOccurredAt());
        updatedSession.setEventCount((session.getEventCount() == null ? 0 : session.getEventCount()) + 1);
        updatedSession.setTemporaryStepStateJson(buildTemporaryStepStateJson(request, event, updatedSession.getCurrentChapterId()));
        updatedSession.setExitClearedTemporaryState(false);
        requireUserStorylineSessionMapper().updateById(updatedSession);
    }

    private UserStorylineSession findActiveStorylineSession(Long userId, Long storylineId) {
        return requireUserStorylineSessionMapper().selectOne(new LambdaQueryWrapper<UserStorylineSession>()
                .eq(UserStorylineSession::getUserId, userId)
                .eq(UserStorylineSession::getStorylineId, storylineId)
                .eq(UserStorylineSession::getStatus, SESSION_STATUS_STARTED)
                .orderByDesc(UserStorylineSession::getStartedAt)
                .last("LIMIT 1"));
    }

    private UserStorylineSession findStorylineSession(Long userId, Long storylineId, String sessionId) {
        if (userId == null || !StringUtils.hasText(sessionId)) {
            return null;
        }
        LambdaQueryWrapper<UserStorylineSession> query = new LambdaQueryWrapper<UserStorylineSession>()
                .eq(UserStorylineSession::getSessionId, sessionId.trim())
                .eq(UserStorylineSession::getUserId, userId);
        if (storylineId != null) {
            query.eq(UserStorylineSession::getStorylineId, storylineId);
        }
        return requireUserStorylineSessionMapper().selectOne(query.last("LIMIT 1"));
    }

    private UserStorylineSession copyStorylineSession(UserStorylineSession source) {
        UserStorylineSession copy = new UserStorylineSession();
        copy.setSessionId(source.getSessionId());
        copy.setUserId(source.getUserId());
        copy.setStorylineId(source.getStorylineId());
        copy.setCurrentChapterId(source.getCurrentChapterId());
        copy.setStatus(source.getStatus());
        copy.setStartedAt(source.getStartedAt());
        copy.setLastEventAt(source.getLastEventAt());
        copy.setExitedAt(source.getExitedAt());
        copy.setEventCount(source.getEventCount());
        copy.setTemporaryStepStateJson(source.getTemporaryStepStateJson());
        copy.setExitClearedTemporaryState(source.getExitClearedTemporaryState());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }

    private StorylineSessionResponse toSessionResponse(UserStorylineSession session) {
        return StorylineSessionResponse.builder()
                .storylineId(session.getStorylineId())
                .sessionId(session.getSessionId())
                .currentChapterId(session.getCurrentChapterId())
                .status(defaultText(session.getStatus(), SESSION_STATUS_STARTED))
                .startedAt(session.getStartedAt())
                .lastEventAt(session.getLastEventAt())
                .exitedAt(session.getExitedAt())
                .eventCount(session.getEventCount() == null ? 0 : session.getEventCount())
                .exitClearedTemporaryState(Boolean.TRUE.equals(session.getExitClearedTemporaryState()))
                .build();
    }

    private Long resolveInitialChapterId(StoryLineResponse storyline) {
        if (storyline == null || storyline.getChapters() == null || storyline.getChapters().isEmpty()) {
            return null;
        }
        return storyline.getChapters().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        chapter -> chapter.getChapterOrder() == null ? Integer.MAX_VALUE : chapter.getChapterOrder()))
                .map(StoryChapterResponse::getId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Long resolveCurrentChapterId(UserStorylineSession session, ExplorationElement element, String payloadJson) {
        Long payloadChapterId = extractChapterId(payloadJson);
        if (payloadChapterId != null) {
            return payloadChapterId;
        }
        if (element != null && element.getStoryChapterId() != null) {
            return element.getStoryChapterId();
        }
        return session == null ? null : session.getCurrentChapterId();
    }

    private Long extractChapterId(String payloadJson) {
        Map<String, Object> payload = readObjectMap(payloadJson);
        for (String key : List.of("currentChapterId", "storyChapterId", "chapterId")) {
            Long chapterId = readLongValue(payload.get(key));
            if (chapterId != null) {
                return chapterId;
            }
        }
        return null;
    }

    private String buildTemporaryStepStateJson(
            ExperienceEventRequest request,
            UserExplorationEvent event,
            Long currentChapterId) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("lastEventType", request.getEventType());
        state.put("lastElementId", firstNonNull(event.getElementId(), request.getElementId()));
        state.put("lastElementCode", firstNonNull(event.getElementCode(), request.getElementCode()));
        if (currentChapterId != null) {
            state.put("currentChapterId", currentChapterId);
        }
        Map<String, Object> payload = readObjectMap(request.getPayloadJson());
        if (!payload.isEmpty()) {
            state.put("lastPayload", payload);
        }
        return writeObjectJson(state);
    }

    private Map<String, Object> readObjectMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String writeObjectJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyMap() : value);
        } catch (Exception ex) {
            return EMPTY_JSON_OBJECT;
        }
    }

    private UserStorylineSessionMapper requireUserStorylineSessionMapper() {
        if (userStorylineSessionMapper == null) {
            throw new IllegalStateException("UserStorylineSessionMapper not configured");
        }
        return userStorylineSessionMapper;
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

    private Long readLongValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
            try {
                return Long.parseLong(stringValue.trim());
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

    private Object firstNonNull(Object first, Object second) {
        return first == null ? second : first;
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
