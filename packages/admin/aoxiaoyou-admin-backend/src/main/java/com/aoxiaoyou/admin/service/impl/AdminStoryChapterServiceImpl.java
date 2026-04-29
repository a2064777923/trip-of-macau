package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryChapterUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterContentBlockLinkResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryContentBlockResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterResponse;
import com.aoxiaoyou.admin.entity.Activity;
import com.aoxiaoyou.admin.entity.Badge;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.ContentAssetLink;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNode;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryChapterBlockLink;
import com.aoxiaoyou.admin.entity.StoryContentBlock;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.ActivityMapper;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterBlockLinkMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.StoryContentBlockMapper;
import com.aoxiaoyou.admin.service.AdminStoryChapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStoryChapterServiceImpl implements AdminStoryChapterService {

    private final StoryChapterMapper storyChapterMapper;
    private final StoryLineMapper storyLineMapper;
    private final PoiMapper poiMapper;
    private final BuildingMapper buildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final IndoorNodeMapper indoorNodeMapper;
    private final CollectibleMapper collectibleMapper;
    private final BadgeMapper badgeMapper;
    private final RewardMapper rewardMapper;
    private final ActivityMapper activityMapper;
    private final StoryContentBlockMapper storyContentBlockMapper;
    private final StoryChapterBlockLinkMapper storyChapterBlockLinkMapper;
    private final ContentAssetLinkMapper contentAssetLinkMapper;
    private final ExperienceFlowMapper experienceFlowMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<AdminStoryChapterResponse> page(Long storylineId, long pageNum, long pageSize) {
        verifyStoryline(storylineId);
        Page<StoryChapter> page = storyChapterMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryChapter>()
                        .eq(StoryChapter::getStorylineId, storylineId)
                        .orderByAsc(StoryChapter::getChapterOrder)
                        .orderByAsc(StoryChapter::getId));
        Page<AdminStoryChapterResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public List<AdminStoryChapterResponse> listByStoryline(Long storylineId) {
        verifyStoryline(storylineId);
        return storyChapterMapper.selectList(new LambdaQueryWrapper<StoryChapter>()
                        .eq(StoryChapter::getStorylineId, storylineId)
                        .orderByAsc(StoryChapter::getChapterOrder)
                        .orderByAsc(StoryChapter::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AdminStoryChapterResponse detail(Long storylineId, Long chapterId) {
        verifyStoryline(storylineId);
        StoryChapter chapter = requireChapter(chapterId);
        if (!Objects.equals(chapter.getStorylineId(), storylineId)) {
            throw new BusinessException(4044, "Story chapter not found");
        }
        return toResponse(chapter);
    }

    @Override
    public AdminStoryChapterResponse create(AdminStoryChapterUpsertRequest.Upsert request) {
        verifyStoryline(request.getStorylineId());
        StoryChapter chapter = new StoryChapter();
        applyRequest(chapter, request);
        storyChapterMapper.insert(chapter);
        syncContentBlocks(chapter.getId(), request.getContentBlocks());
        return toResponse(requireChapter(chapter.getId()));
    }

    @Override
    public AdminStoryChapterResponse update(Long chapterId, AdminStoryChapterUpsertRequest.Upsert request) {
        verifyStoryline(request.getStorylineId());
        StoryChapter chapter = requireChapter(chapterId);
        applyRequest(chapter, request);
        storyChapterMapper.updateById(chapter);
        syncContentBlocks(chapterId, request.getContentBlocks());
        return toResponse(requireChapter(chapterId));
    }

    @Override
    public void delete(Long chapterId) {
        requireChapter(chapterId);
        storyChapterBlockLinkMapper.delete(new LambdaQueryWrapper<StoryChapterBlockLink>()
                .eq(StoryChapterBlockLink::getChapterId, chapterId));
        storyChapterMapper.deleteById(chapterId);
    }

    private void applyRequest(StoryChapter chapter, AdminStoryChapterUpsertRequest.Upsert request) {
        chapter.setStorylineId(request.getStorylineId());
        chapter.setChapterOrder(request.getChapterOrder());
        chapter.setTitleZh(request.getTitleZh());
        chapter.setTitleEn(request.getTitleEn());
        chapter.setTitleZht(request.getTitleZht());
        chapter.setTitlePt(request.getTitlePt());
        chapter.setSummaryZh(request.getSummaryZh());
        chapter.setSummaryEn(request.getSummaryEn());
        chapter.setSummaryZht(request.getSummaryZht());
        chapter.setSummaryPt(request.getSummaryPt());
        chapter.setDetailZh(request.getDetailZh());
        chapter.setDetailEn(request.getDetailEn());
        chapter.setDetailZht(request.getDetailZht());
        chapter.setDetailPt(request.getDetailPt());
        chapter.setAchievementZh(request.getAchievementZh());
        chapter.setAchievementEn(request.getAchievementEn());
        chapter.setAchievementZht(request.getAchievementZht());
        chapter.setAchievementPt(request.getAchievementPt());
        chapter.setCollectibleZh(request.getCollectibleZh());
        chapter.setCollectibleEn(request.getCollectibleEn());
        chapter.setCollectibleZht(request.getCollectibleZht());
        chapter.setCollectiblePt(request.getCollectiblePt());
        chapter.setLocationNameZh(request.getLocationNameZh());
        chapter.setLocationNameEn(request.getLocationNameEn());
        chapter.setLocationNameZht(request.getLocationNameZht());
        chapter.setLocationNamePt(request.getLocationNamePt());
        chapter.setMediaAssetId(request.getMediaAssetId());
        validateExperienceFlow(request.getExperienceFlowId());
        chapter.setExperienceFlowId(request.getExperienceFlowId());
        chapter.setOverridePolicyJson(normalizeVersionedJson(request.getOverridePolicyJson(), "overridePolicyJson"));
        chapter.setStoryModeConfigJson(normalizeVersionedJson(request.getStoryModeConfigJson(), "storyModeConfigJson"));
        chapter.setAnchorType(normalizeAnchorType(request.getAnchorType()));
        chapter.setAnchorTargetId(request.getAnchorTargetId());
        chapter.setAnchorTargetCode(trimToNull(request.getAnchorTargetCode()));
        chapter.setUnlockType(StringUtils.hasText(request.getUnlockType()) ? request.getUnlockType() : "sequence");
        chapter.setUnlockParamJson(normalizeJson(request.getUnlockParamJson(), "unlockParamJson"));
        chapter.setPrerequisiteJson(normalizeJson(request.getPrerequisiteJson(), "prerequisiteJson"));
        chapter.setCompletionJson(normalizeJson(request.getCompletionJson(), "completionJson"));
        chapter.setRewardJson(normalizeJson(request.getRewardJson(), "rewardJson"));
        validateAnchorTarget(chapter.getAnchorType(), chapter.getAnchorTargetId(), chapter.getAnchorTargetCode());
        validateContentBlocks(request.getContentBlocks());
        chapter.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        chapter.setSortOrder(request.getSortOrder() == null ? request.getChapterOrder() : request.getSortOrder());
        chapter.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private StoryChapter requireChapter(Long chapterId) {
        StoryChapter chapter = storyChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4044, "Story chapter not found");
        }
        return chapter;
    }

    private StoryLine verifyStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
        return storyLine;
    }

    private AdminStoryChapterResponse toResponse(StoryChapter chapter) {
        List<AdminStoryChapterContentBlockLinkResponse> contentBlocks = loadContentBlocks(chapter.getId());
        return AdminStoryChapterResponse.builder()
                .id(chapter.getId())
                .storylineId(chapter.getStorylineId())
                .chapterOrder(chapter.getChapterOrder())
                .titleZh(chapter.getTitleZh())
                .titleEn(chapter.getTitleEn())
                .titleZht(chapter.getTitleZht())
                .titlePt(chapter.getTitlePt())
                .summaryZh(chapter.getSummaryZh())
                .summaryEn(chapter.getSummaryEn())
                .summaryZht(chapter.getSummaryZht())
                .summaryPt(chapter.getSummaryPt())
                .detailZh(chapter.getDetailZh())
                .detailEn(chapter.getDetailEn())
                .detailZht(chapter.getDetailZht())
                .detailPt(chapter.getDetailPt())
                .achievementZh(chapter.getAchievementZh())
                .achievementEn(chapter.getAchievementEn())
                .achievementZht(chapter.getAchievementZht())
                .achievementPt(chapter.getAchievementPt())
                .collectibleZh(chapter.getCollectibleZh())
                .collectibleEn(chapter.getCollectibleEn())
                .collectibleZht(chapter.getCollectibleZht())
                .collectiblePt(chapter.getCollectiblePt())
                .locationNameZh(chapter.getLocationNameZh())
                .locationNameEn(chapter.getLocationNameEn())
                .locationNameZht(chapter.getLocationNameZht())
                .locationNamePt(chapter.getLocationNamePt())
                .mediaAssetId(chapter.getMediaAssetId())
                .experienceFlowId(chapter.getExperienceFlowId())
                .overridePolicyJson(chapter.getOverridePolicyJson())
                .storyModeConfigJson(chapter.getStoryModeConfigJson())
                .anchorType(chapter.getAnchorType())
                .anchorTargetId(chapter.getAnchorTargetId())
                .anchorTargetCode(chapter.getAnchorTargetCode())
                .anchorTargetLabel(resolveAnchorLabel(chapter))
                .unlockType(chapter.getUnlockType())
                .unlockParamJson(chapter.getUnlockParamJson())
                .prerequisiteJson(chapter.getPrerequisiteJson())
                .completionJson(chapter.getCompletionJson())
                .rewardJson(chapter.getRewardJson())
                .status(chapter.getStatus())
                .sortOrder(chapter.getSortOrder())
                .publishedAt(chapter.getPublishedAt())
                .createdAt(chapter.getCreatedAt() == null ? null : chapter.getCreatedAt().toString())
                .updatedAt(chapter.getUpdatedAt() == null ? null : chapter.getUpdatedAt().toString())
                .contentBlocks(contentBlocks)
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private String normalizeAnchorType(String anchorType) {
        return StringUtils.hasText(anchorType) ? anchorType.trim() : "manual";
    }

    private String normalizeJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            objectMapper.readTree(value);
            return value.trim();
        } catch (Exception exception) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private String normalizeVersionedJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            var node = objectMapper.readTree(value);
            if (!node.isObject() || !node.has("schemaVersion")) {
                throw new BusinessException(4002, fieldName + " must be a JSON object with schemaVersion");
            }
            return value.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
    }

    private void validateExperienceFlow(Long flowId) {
        if (flowId == null) {
            return;
        }
        if (experienceFlowMapper.selectById(flowId) == null) {
            throw new BusinessException(4071, "Experience flow not found");
        }
    }

    private void validateContentBlocks(List<AdminStoryChapterUpsertRequest.ContentBlockLinkUpsert> contentBlocks) {
        if (contentBlocks == null || contentBlocks.isEmpty()) {
            return;
        }
        for (AdminStoryChapterUpsertRequest.ContentBlockLinkUpsert contentBlock : contentBlocks) {
            if (contentBlock == null || contentBlock.getBlockId() == null) {
                throw new BusinessException(4002, "Each content block link must contain blockId");
            }
            if (storyContentBlockMapper.selectById(contentBlock.getBlockId()) == null) {
                throw new BusinessException(4045, "Story content block not found");
            }
            normalizeJson(contentBlock.getOverrideTitleJson(), "contentBlocks.overrideTitleJson");
            normalizeJson(contentBlock.getOverrideSummaryJson(), "contentBlocks.overrideSummaryJson");
            normalizeJson(contentBlock.getOverrideBodyJson(), "contentBlocks.overrideBodyJson");
            normalizeJson(contentBlock.getDisplayConditionJson(), "contentBlocks.displayConditionJson");
            normalizeJson(contentBlock.getOverrideConfigJson(), "contentBlocks.overrideConfigJson");
        }
    }

    private void syncContentBlocks(Long chapterId, List<AdminStoryChapterUpsertRequest.ContentBlockLinkUpsert> contentBlocks) {
        storyChapterBlockLinkMapper.delete(new LambdaQueryWrapper<StoryChapterBlockLink>()
                .eq(StoryChapterBlockLink::getChapterId, chapterId));
        if (contentBlocks == null || contentBlocks.isEmpty()) {
            return;
        }
        for (int index = 0; index < contentBlocks.size(); index++) {
            AdminStoryChapterUpsertRequest.ContentBlockLinkUpsert request = contentBlocks.get(index);
            if (request == null || request.getBlockId() == null) {
                continue;
            }
            StoryChapterBlockLink link = new StoryChapterBlockLink();
            link.setChapterId(chapterId);
            link.setBlockId(request.getBlockId());
            link.setOverrideTitleJson(trimToNull(request.getOverrideTitleJson()));
            link.setOverrideSummaryJson(trimToNull(request.getOverrideSummaryJson()));
            link.setOverrideBodyJson(trimToNull(request.getOverrideBodyJson()));
            link.setDisplayConditionJson(trimToNull(request.getDisplayConditionJson()));
            link.setOverrideConfigJson(trimToNull(request.getOverrideConfigJson()));
            link.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "draft");
            link.setSortOrder(request.getSortOrder() == null ? index : request.getSortOrder());
            storyChapterBlockLinkMapper.insert(link);
        }
    }

    private List<AdminStoryChapterContentBlockLinkResponse> loadContentBlocks(Long chapterId) {
        List<StoryChapterBlockLink> links = storyChapterBlockLinkMapper.selectList(new LambdaQueryWrapper<StoryChapterBlockLink>()
                .eq(StoryChapterBlockLink::getChapterId, chapterId)
                .orderByAsc(StoryChapterBlockLink::getSortOrder)
                .orderByAsc(StoryChapterBlockLink::getId));
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, StoryContentBlock> blocksById = storyContentBlockMapper.selectBatchIds(
                        links.stream().map(StoryChapterBlockLink::getBlockId).filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(StoryContentBlock::getId, item -> item, (left, right) -> left));
        Map<Long, List<Long>> attachmentAssetIdsByBlock = contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                        .eq(ContentAssetLink::getEntityType, "story_content_block")
                        .in(ContentAssetLink::getEntityId, blocksById.keySet())
                        .orderByAsc(ContentAssetLink::getSortOrder)
                        .orderByAsc(ContentAssetLink::getId))
                .stream()
                .collect(Collectors.groupingBy(
                        ContentAssetLink::getEntityId,
                        Collectors.mapping(ContentAssetLink::getAssetId, Collectors.toList())));

        return links.stream()
                .map(link -> {
                    StoryContentBlock block = blocksById.get(link.getBlockId());
                    if (block == null) {
                        return null;
                    }
                    AdminStoryContentBlockResponse blockResponse = AdminStoryContentBlockResponse.builder()
                            .id(block.getId())
                            .code(block.getCode())
                            .blockType(block.getBlockType())
                            .titleZh(block.getTitleZh())
                            .titleEn(block.getTitleEn())
                            .titleZht(block.getTitleZht())
                            .titlePt(block.getTitlePt())
                            .summaryZh(block.getSummaryZh())
                            .summaryEn(block.getSummaryEn())
                            .summaryZht(block.getSummaryZht())
                            .summaryPt(block.getSummaryPt())
                            .bodyZh(block.getBodyZh())
                            .bodyEn(block.getBodyEn())
                            .bodyZht(block.getBodyZht())
                            .bodyPt(block.getBodyPt())
                            .primaryAssetId(block.getPrimaryAssetId())
                            .attachmentAssetIds(attachmentAssetIdsByBlock.getOrDefault(block.getId(), Collections.emptyList()))
                            .stylePreset(block.getStylePreset())
                            .displayMode(block.getDisplayMode())
                            .visibilityJson(block.getVisibilityJson())
                            .configJson(block.getConfigJson())
                            .status(block.getStatus())
                            .sortOrder(block.getSortOrder())
                            .publishedAt(block.getPublishedAt())
                            .createdAt(block.getCreatedAt())
                            .updatedAt(block.getUpdatedAt())
                            .build();
                    return AdminStoryChapterContentBlockLinkResponse.builder()
                            .id(link.getId())
                            .chapterId(link.getChapterId())
                            .blockId(link.getBlockId())
                            .overrideTitleJson(link.getOverrideTitleJson())
                            .overrideSummaryJson(link.getOverrideSummaryJson())
                            .overrideBodyJson(link.getOverrideBodyJson())
                            .displayConditionJson(link.getDisplayConditionJson())
                            .overrideConfigJson(link.getOverrideConfigJson())
                            .status(link.getStatus())
                            .sortOrder(link.getSortOrder())
                            .block(blockResponse)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateAnchorTarget(String anchorType, Long anchorTargetId, String anchorTargetCode) {
        switch (anchorType) {
            case "manual":
                return;
            case "poi":
                requireTarget(anchorTargetId, poiMapper.selectById(anchorTargetId), "POI anchor target not found");
                return;
            case "indoor_building":
                requireTarget(anchorTargetId, buildingMapper.selectById(anchorTargetId), "Indoor building anchor target not found");
                return;
            case "indoor_floor":
                requireTarget(anchorTargetId, indoorFloorMapper.selectById(anchorTargetId), "Indoor floor anchor target not found");
                return;
            case "indoor_node":
                requireTarget(anchorTargetId, indoorNodeMapper.selectById(anchorTargetId), "Indoor node anchor target not found");
                return;
            case "collectible":
                requireTarget(anchorTargetId, collectibleMapper.selectById(anchorTargetId), "Collectible anchor target not found");
                return;
            case "badge":
                requireTarget(anchorTargetId, badgeMapper.selectById(anchorTargetId), "Badge anchor target not found");
                return;
            case "reward":
                requireTarget(anchorTargetId, rewardMapper.selectById(anchorTargetId), "Reward anchor target not found");
                return;
            case "activity":
                requireTarget(anchorTargetId, activityMapper.selectById(anchorTargetId), "Activity anchor target not found");
                return;
            case "overlay":
            case "task":
                if (anchorTargetId == null && !StringUtils.hasText(anchorTargetCode)) {
                    throw new BusinessException(4002, "Anchor target id or code is required");
                }
                return;
            case "marker":
                if (anchorTargetId == null && !StringUtils.hasText(anchorTargetCode)) {
                    throw new BusinessException(4002, "Anchor target id or code is required");
                }
                return;
            default:
                throw new BusinessException(4002, "Unsupported anchorType");
        }
    }

    private void requireTarget(Long targetId, Object entity, String message) {
        if (targetId == null || entity == null) {
            throw new BusinessException(4002, message);
        }
    }

    private String resolveAnchorLabel(StoryChapter chapter) {
        if (chapter.getAnchorTargetId() == null) {
            return trimToNull(chapter.getAnchorTargetCode());
        }
        return switch (normalizeAnchorType(chapter.getAnchorType())) {
            case "poi" -> resolvePoiName(chapter.getAnchorTargetId());
            case "indoor_building" -> resolveBuildingName(chapter.getAnchorTargetId(), chapter.getAnchorTargetCode());
            case "indoor_floor" -> resolveIndoorFloorName(chapter.getAnchorTargetId(), chapter.getAnchorTargetCode());
            case "indoor_node" -> resolveIndoorNodeName(chapter.getAnchorTargetId(), chapter.getAnchorTargetCode());
            case "collectible" -> resolveCollectibleName(chapter.getAnchorTargetId());
            case "badge" -> resolveBadgeName(chapter.getAnchorTargetId());
            case "reward" -> resolveRewardName(chapter.getAnchorTargetId());
            case "activity" -> resolveActivityName(chapter.getAnchorTargetId());
            default -> trimToNull(chapter.getAnchorTargetCode());
        };
    }

    private String resolvePoiName(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        return poi == null ? null : firstText(poi.getNameZht(), poi.getNameZh(), poi.getCode(), null);
    }

    private String resolveBuildingName(Long buildingId, String fallbackCode) {
        Building building = buildingMapper.selectById(buildingId);
        return building == null ? trimToNull(fallbackCode) : firstText(building.getNameZht(), building.getNameZh(), building.getBuildingCode(), fallbackCode);
    }

    private String resolveIndoorFloorName(Long floorId, String fallbackCode) {
        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
        return floor == null ? trimToNull(fallbackCode) : firstText(floor.getFloorNameZht(), floor.getFloorNameZh(), floor.getFloorCode(), fallbackCode);
    }

    private String resolveIndoorNodeName(Long nodeId, String fallbackCode) {
        IndoorNode node = indoorNodeMapper.selectById(nodeId);
        return node == null ? trimToNull(fallbackCode) : firstText(node.getNodeNameZht(), node.getNodeNameZh(), node.getMarkerCode(), fallbackCode);
    }

    private String resolveCollectibleName(Long collectibleId) {
        Collectible collectible = collectibleMapper.selectById(collectibleId);
        return collectible == null ? null : collectible.getNameZh();
    }

    private String resolveBadgeName(Long badgeId) {
        Badge badge = badgeMapper.selectById(badgeId);
        return badge == null ? null : badge.getNameZh();
    }

    private String resolveRewardName(Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        return reward == null ? null : reward.getNameZh();
    }

    private String resolveActivityName(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        return activity == null ? null : activity.getTitle();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String first, String second, String third, String fallback) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        if (StringUtils.hasText(second)) {
            return second.trim();
        }
        if (StringUtils.hasText(third)) {
            return third.trim();
        }
        return trimToNull(fallback);
    }
}
