package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminBadgeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminCollectibleUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.entity.Badge;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminCollectibleService;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminCollectibleServiceImpl implements AdminCollectibleService {

    private static final String OWNER_TYPE_COLLECTIBLE = "collectible";
    private static final String OWNER_TYPE_BADGE = "badge";
    private static final String OWNER_TYPE_REWARD = "reward";
    private static final String RELATION_STORYLINE = "storyline_binding";
    private static final String RELATION_CITY = "city_binding";
    private static final String RELATION_SUB_MAP = "sub_map_binding";
    private static final String RELATION_INDOOR_BUILDING = "indoor_building_binding";
    private static final String RELATION_INDOOR_FLOOR = "indoor_floor_binding";
    private static final String RELATION_ATTACHMENT = "attachment_asset";

    private final CollectibleMapper collectibleMapper;
    private final BadgeMapper badgeMapper;
    private final RewardMapper rewardMapper;
    private final StoryLineMapper storyLineMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final BuildingMapper buildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final AdminContentRelationService adminContentRelationService;

    @Override
    public PageResponse<CollectibleResponse> pageCollectibles(long pageNum, long pageSize, String keyword, String rarity) {
        LambdaQueryWrapper<Collectible> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalized = keyword.trim();
            wrapper.and(query -> query.like(Collectible::getCollectibleCode, normalized)
                    .or()
                    .like(Collectible::getNameZh, normalized));
        }
        if (StringUtils.hasText(rarity)) {
            wrapper.eq(Collectible::getRarity, rarity.trim());
        }
        wrapper.orderByAsc(Collectible::getSortOrder).orderByDesc(Collectible::getId);
        Page<Collectible> page = collectibleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<CollectibleResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapCollectible).toList());
        return PageResponse.of(result);
    }

    @Override
    public CollectibleResponse getCollectible(Long collectibleId) {
        return mapCollectible(requireCollectible(collectibleId));
    }

    @Override
    public CollectibleResponse createCollectible(AdminCollectibleUpsertRequest request) {
        validateCollectibleRequest(request);
        Collectible collectible = new Collectible();
        applyCollectibleRequest(collectible, request);
        collectibleMapper.insert(collectible);
        syncBindings(OWNER_TYPE_COLLECTIBLE, collectible.getId(), request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapCollectible(requireCollectible(collectible.getId()));
    }

    @Override
    public CollectibleResponse updateCollectible(Long collectibleId, AdminCollectibleUpsertRequest request) {
        Collectible collectible = requireCollectible(collectibleId);
        validateCollectibleRequest(request);
        applyCollectibleRequest(collectible, request);
        collectibleMapper.updateById(collectible);
        syncBindings(OWNER_TYPE_COLLECTIBLE, collectibleId, request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapCollectible(requireCollectible(collectibleId));
    }

    @Override
    public void deleteCollectible(Long collectibleId) {
        requireCollectible(collectibleId);
        collectibleMapper.deleteById(collectibleId);
    }

    @Override
    public PageResponse<BadgeResponse> pageBadges(long pageNum, long pageSize) {
        LambdaQueryWrapper<Badge> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Badge::getId);
        Page<Badge> page = badgeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<BadgeResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapBadge).toList());
        return PageResponse.of(result);
    }

    @Override
    public BadgeResponse getBadge(Long badgeId) {
        return mapBadge(requireBadge(badgeId));
    }

    @Override
    public BadgeResponse createBadge(AdminBadgeUpsertRequest request) {
        validateBadgeRequest(request);
        Badge badge = new Badge();
        applyBadgeRequest(badge, request);
        badgeMapper.insert(badge);
        syncBindings(OWNER_TYPE_BADGE, badge.getId(), request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapBadge(requireBadge(badge.getId()));
    }

    @Override
    public BadgeResponse updateBadge(Long badgeId, AdminBadgeUpsertRequest request) {
        Badge badge = requireBadge(badgeId);
        validateBadgeRequest(request);
        applyBadgeRequest(badge, request);
        badgeMapper.updateById(badge);
        syncBindings(OWNER_TYPE_BADGE, badgeId, request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapBadge(requireBadge(badgeId));
    }

    @Override
    public void deleteBadge(Long badgeId) {
        requireBadge(badgeId);
        badgeMapper.deleteById(badgeId);
    }

    @Override
    public PageResponse<AdminRewardResponse> pageRewards(long pageNum, long pageSize, String status) {
        Page<Reward> page = rewardMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Reward>()
                        .eq(StringUtils.hasText(status), Reward::getStatus, status)
                        .orderByAsc(Reward::getSortOrder)
                        .orderByAsc(Reward::getId));
        Page<AdminRewardResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapReward).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request) {
        validateRewardRequest(request);
        Reward reward = new Reward();
        applyRewardRequest(reward, request);
        rewardMapper.insert(reward);
        syncBindings(OWNER_TYPE_REWARD, reward.getId(), request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapReward(requireReward(reward.getId()));
    }

    @Override
    public AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request) {
        Reward reward = requireReward(rewardId);
        validateRewardRequest(request);
        applyRewardRequest(reward, request);
        rewardMapper.updateById(reward);
        syncBindings(OWNER_TYPE_REWARD, rewardId, request.getStorylineBindings(), request.getCityBindings(),
                request.getSubMapBindings(), request.getIndoorBuildingBindings(), request.getIndoorFloorBindings(), request.getAttachmentAssetIds());
        return mapReward(requireReward(rewardId));
    }

    @Override
    public void deleteReward(Long rewardId) {
        requireReward(rewardId);
        rewardMapper.deleteById(rewardId);
    }

    private void applyCollectibleRequest(Collectible collectible, AdminCollectibleUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "collectible request is required");
        }
        collectible.setCollectibleCode(requireText(request.getCollectibleCode(), "collectibleCode is required"));
        collectible.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        collectible.setNameEn(trimToNull(request.getNameEn()));
        collectible.setNameZht(trimToNull(request.getNameZht()));
        collectible.setNamePt(trimToNull(request.getNamePt()));
        collectible.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        collectible.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        collectible.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        collectible.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        collectible.setCollectibleType(defaultText(request.getCollectibleType(), "item"));
        collectible.setRarity(defaultText(request.getRarity(), "common"));
        collectible.setCoverAssetId(request.getCoverAssetId());
        collectible.setIconAssetId(request.getIconAssetId());
        collectible.setAnimationAssetId(request.getAnimationAssetId());
        collectible.setImageUrl(trimToNull(request.getImageUrl()));
        collectible.setAnimationUrl(trimToNull(request.getAnimationUrl()));
        collectible.setSeriesId(request.getSeriesId());
        collectible.setAcquisitionSource(trimToNull(request.getAcquisitionSource()));
        collectible.setPopupPresetCode(trimToNull(request.getPopupPresetCode()));
        collectible.setPopupConfigJson(trimToNull(request.getPopupConfigJson()));
        collectible.setDisplayPresetCode(trimToNull(request.getDisplayPresetCode()));
        collectible.setDisplayConfigJson(trimToNull(request.getDisplayConfigJson()));
        collectible.setTriggerPresetCode(trimToNull(request.getTriggerPresetCode()));
        collectible.setTriggerConfigJson(trimToNull(request.getTriggerConfigJson()));
        collectible.setExampleContentZh(trimToNull(request.getExampleContentZh()));
        collectible.setExampleContentEn(trimToNull(request.getExampleContentEn()));
        collectible.setExampleContentZht(trimToNull(request.getExampleContentZht()));
        collectible.setExampleContentPt(trimToNull(request.getExampleContentPt()));
        collectible.setIsRepeatable(defaultNumber(request.getIsRepeatable(), 0));
        collectible.setIsLimited(defaultNumber(request.getIsLimited(), 0));
        collectible.setMaxOwnership(defaultNumber(request.getMaxOwnership(), 1));
        collectible.setStatus(normalizeStatus(request.getStatus(), "draft"));
        collectible.setSortOrder(defaultNumber(request.getSortOrder(), 0));
    }

    private void applyBadgeRequest(Badge badge, AdminBadgeUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "badge request is required");
        }
        badge.setBadgeCode(requireText(request.getBadgeCode(), "badgeCode is required"));
        badge.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        badge.setNameEn(trimToNull(request.getNameEn()));
        badge.setNameZht(trimToNull(request.getNameZht()));
        badge.setNamePt(trimToNull(request.getNamePt()));
        badge.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        badge.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        badge.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        badge.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        badge.setBadgeType(defaultText(request.getBadgeType(), "special"));
        badge.setRarity(defaultText(request.getRarity(), "common"));
        badge.setIsHidden(defaultNumber(request.getIsHidden(), 0));
        badge.setCoverAssetId(request.getCoverAssetId());
        badge.setIconAssetId(request.getIconAssetId());
        badge.setAnimationAssetId(request.getAnimationAssetId());
        badge.setIconUrl(trimToNull(request.getIconUrl()));
        badge.setImageUrl(trimToNull(request.getImageUrl()));
        badge.setAnimationUnlock(trimToNull(request.getAnimationUnlock()));
        badge.setPopupPresetCode(trimToNull(request.getPopupPresetCode()));
        badge.setPopupConfigJson(trimToNull(request.getPopupConfigJson()));
        badge.setDisplayPresetCode(trimToNull(request.getDisplayPresetCode()));
        badge.setDisplayConfigJson(trimToNull(request.getDisplayConfigJson()));
        badge.setTriggerPresetCode(trimToNull(request.getTriggerPresetCode()));
        badge.setTriggerConfigJson(trimToNull(request.getTriggerConfigJson()));
        badge.setExampleContentZh(trimToNull(request.getExampleContentZh()));
        badge.setExampleContentEn(trimToNull(request.getExampleContentEn()));
        badge.setExampleContentZht(trimToNull(request.getExampleContentZht()));
        badge.setExampleContentPt(trimToNull(request.getExampleContentPt()));
        badge.setStatus(normalizeStatus(request.getStatus(), "draft"));
    }

    private void applyRewardRequest(Reward reward, AdminRewardUpsertRequest.Upsert request) {
        if (request == null) {
            throw new BusinessException(4001, "reward request is required");
        }
        reward.setCode(requireText(request.getCode(), "code is required"));
        reward.setNameZh(requireText(request.getNameZh(), "nameZh is required"));
        reward.setNameEn(trimToNull(request.getNameEn()));
        reward.setNameZht(trimToNull(request.getNameZht()));
        reward.setNamePt(trimToNull(request.getNamePt()));
        reward.setSubtitleZh(trimToNull(request.getSubtitleZh()));
        reward.setSubtitleEn(trimToNull(request.getSubtitleEn()));
        reward.setSubtitleZht(trimToNull(request.getSubtitleZht()));
        reward.setSubtitlePt(trimToNull(request.getSubtitlePt()));
        reward.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        reward.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        reward.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        reward.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        reward.setHighlightZh(trimToNull(request.getHighlightZh()));
        reward.setHighlightEn(trimToNull(request.getHighlightEn()));
        reward.setHighlightZht(trimToNull(request.getHighlightZht()));
        reward.setHighlightPt(trimToNull(request.getHighlightPt()));
        reward.setStampCost(defaultNumber(request.getStampCost(), 0));
        reward.setInventoryTotal(defaultNumber(request.getInventoryTotal(), 0));
        reward.setInventoryRedeemed(defaultNumber(request.getInventoryRedeemed(), 0));
        reward.setCoverAssetId(request.getCoverAssetId());
        reward.setPopupPresetCode(trimToNull(request.getPopupPresetCode()));
        reward.setPopupConfigJson(trimToNull(request.getPopupConfigJson()));
        reward.setDisplayPresetCode(trimToNull(request.getDisplayPresetCode()));
        reward.setDisplayConfigJson(trimToNull(request.getDisplayConfigJson()));
        reward.setTriggerPresetCode(trimToNull(request.getTriggerPresetCode()));
        reward.setTriggerConfigJson(trimToNull(request.getTriggerConfigJson()));
        reward.setExampleContentZh(trimToNull(request.getExampleContentZh()));
        reward.setExampleContentEn(trimToNull(request.getExampleContentEn()));
        reward.setExampleContentZht(trimToNull(request.getExampleContentZht()));
        reward.setExampleContentPt(trimToNull(request.getExampleContentPt()));
        reward.setStatus(normalizeStatus(request.getStatus(), "draft"));
        reward.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        reward.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        reward.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private void syncBindings(
            String ownerType,
            Long ownerId,
            List<Long> storylineBindings,
            List<Long> cityBindings,
            List<Long> subMapBindings,
            List<Long> indoorBuildingBindings,
            List<Long> indoorFloorBindings,
            List<Long> attachmentAssetIds
    ) {
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_STORYLINE, "storyline", storylineBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_CITY, "city", cityBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_SUB_MAP, "sub_map", subMapBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_INDOOR_BUILDING, "indoor_building", indoorBuildingBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_INDOOR_FLOOR, "indoor_floor", indoorFloorBindings);
        adminContentRelationService.syncTargetIds(ownerType, ownerId, RELATION_ATTACHMENT, "asset", attachmentAssetIds);
    }

    private CollectibleResponse mapCollectible(Collectible collectible) {
        return CollectibleResponse.builder()
                .id(collectible.getId())
                .collectibleCode(collectible.getCollectibleCode())
                .nameZh(collectible.getNameZh())
                .nameEn(collectible.getNameEn())
                .nameZht(collectible.getNameZht())
                .namePt(collectible.getNamePt())
                .descriptionZh(collectible.getDescriptionZh())
                .descriptionEn(collectible.getDescriptionEn())
                .descriptionZht(collectible.getDescriptionZht())
                .descriptionPt(collectible.getDescriptionPt())
                .collectibleType(collectible.getCollectibleType())
                .rarity(collectible.getRarity())
                .coverAssetId(collectible.getCoverAssetId())
                .iconAssetId(collectible.getIconAssetId())
                .animationAssetId(collectible.getAnimationAssetId())
                .imageUrl(collectible.getImageUrl())
                .animationUrl(collectible.getAnimationUrl())
                .seriesId(collectible.getSeriesId())
                .acquisitionSource(collectible.getAcquisitionSource())
                .popupPresetCode(collectible.getPopupPresetCode())
                .popupConfigJson(collectible.getPopupConfigJson())
                .displayPresetCode(collectible.getDisplayPresetCode())
                .displayConfigJson(collectible.getDisplayConfigJson())
                .triggerPresetCode(collectible.getTriggerPresetCode())
                .triggerConfigJson(collectible.getTriggerConfigJson())
                .exampleContentZh(collectible.getExampleContentZh())
                .exampleContentEn(collectible.getExampleContentEn())
                .exampleContentZht(collectible.getExampleContentZht())
                .exampleContentPt(collectible.getExampleContentPt())
                .isRepeatable(collectible.getIsRepeatable())
                .isLimited(collectible.getIsLimited())
                .maxOwnership(collectible.getMaxOwnership())
                .status(collectible.getStatus())
                .sortOrder(collectible.getSortOrder())
                .storylineBindings(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_STORYLINE, "storyline"))
                .cityBindings(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_CITY, "city"))
                .subMapBindings(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_SUB_MAP, "sub_map"))
                .indoorBuildingBindings(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_INDOOR_BUILDING, "indoor_building"))
                .indoorFloorBindings(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_INDOOR_FLOOR, "indoor_floor"))
                .attachmentAssetIds(bindingIds(OWNER_TYPE_COLLECTIBLE, collectible.getId(), RELATION_ATTACHMENT, "asset"))
                .build();
    }

    private BadgeResponse mapBadge(Badge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .badgeCode(badge.getBadgeCode())
                .nameZh(badge.getNameZh())
                .nameEn(badge.getNameEn())
                .nameZht(badge.getNameZht())
                .namePt(badge.getNamePt())
                .descriptionZh(badge.getDescriptionZh())
                .descriptionEn(badge.getDescriptionEn())
                .descriptionZht(badge.getDescriptionZht())
                .descriptionPt(badge.getDescriptionPt())
                .badgeType(badge.getBadgeType())
                .rarity(badge.getRarity())
                .isHidden(badge.getIsHidden())
                .coverAssetId(badge.getCoverAssetId())
                .iconAssetId(badge.getIconAssetId())
                .animationAssetId(badge.getAnimationAssetId())
                .iconUrl(badge.getIconUrl())
                .imageUrl(badge.getImageUrl())
                .animationUnlock(badge.getAnimationUnlock())
                .popupPresetCode(badge.getPopupPresetCode())
                .popupConfigJson(badge.getPopupConfigJson())
                .displayPresetCode(badge.getDisplayPresetCode())
                .displayConfigJson(badge.getDisplayConfigJson())
                .triggerPresetCode(badge.getTriggerPresetCode())
                .triggerConfigJson(badge.getTriggerConfigJson())
                .exampleContentZh(badge.getExampleContentZh())
                .exampleContentEn(badge.getExampleContentEn())
                .exampleContentZht(badge.getExampleContentZht())
                .exampleContentPt(badge.getExampleContentPt())
                .status(badge.getStatus())
                .storylineBindings(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_STORYLINE, "storyline"))
                .cityBindings(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_CITY, "city"))
                .subMapBindings(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_SUB_MAP, "sub_map"))
                .indoorBuildingBindings(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_INDOOR_BUILDING, "indoor_building"))
                .indoorFloorBindings(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_INDOOR_FLOOR, "indoor_floor"))
                .attachmentAssetIds(bindingIds(OWNER_TYPE_BADGE, badge.getId(), RELATION_ATTACHMENT, "asset"))
                .build();
    }

    private AdminRewardResponse mapReward(Reward reward) {
        int total = reward.getInventoryTotal() == null ? 0 : reward.getInventoryTotal();
        int redeemed = reward.getInventoryRedeemed() == null ? 0 : reward.getInventoryRedeemed();
        return AdminRewardResponse.builder()
                .id(reward.getId())
                .code(reward.getCode())
                .nameZh(reward.getNameZh())
                .nameEn(reward.getNameEn())
                .nameZht(reward.getNameZht())
                .namePt(reward.getNamePt())
                .subtitleZh(reward.getSubtitleZh())
                .subtitleEn(reward.getSubtitleEn())
                .subtitleZht(reward.getSubtitleZht())
                .subtitlePt(reward.getSubtitlePt())
                .descriptionZh(reward.getDescriptionZh())
                .descriptionEn(reward.getDescriptionEn())
                .descriptionZht(reward.getDescriptionZht())
                .descriptionPt(reward.getDescriptionPt())
                .highlightZh(reward.getHighlightZh())
                .highlightEn(reward.getHighlightEn())
                .highlightZht(reward.getHighlightZht())
                .highlightPt(reward.getHighlightPt())
                .stampCost(reward.getStampCost())
                .inventoryTotal(total)
                .inventoryRedeemed(redeemed)
                .inventoryRemaining(Math.max(total - redeemed, 0))
                .coverAssetId(reward.getCoverAssetId())
                .popupPresetCode(reward.getPopupPresetCode())
                .popupConfigJson(reward.getPopupConfigJson())
                .displayPresetCode(reward.getDisplayPresetCode())
                .displayConfigJson(reward.getDisplayConfigJson())
                .triggerPresetCode(reward.getTriggerPresetCode())
                .triggerConfigJson(reward.getTriggerConfigJson())
                .exampleContentZh(reward.getExampleContentZh())
                .exampleContentEn(reward.getExampleContentEn())
                .exampleContentZht(reward.getExampleContentZht())
                .exampleContentPt(reward.getExampleContentPt())
                .status(reward.getStatus())
                .sortOrder(reward.getSortOrder())
                .publishStartAt(reward.getPublishStartAt())
                .publishEndAt(reward.getPublishEndAt())
                .createdAt(reward.getCreatedAt())
                .storylineBindings(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_STORYLINE, "storyline"))
                .cityBindings(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_CITY, "city"))
                .subMapBindings(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_SUB_MAP, "sub_map"))
                .indoorBuildingBindings(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_INDOOR_BUILDING, "indoor_building"))
                .indoorFloorBindings(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_INDOOR_FLOOR, "indoor_floor"))
                .attachmentAssetIds(bindingIds(OWNER_TYPE_REWARD, reward.getId(), RELATION_ATTACHMENT, "asset"))
                .build();
    }

    private void validateCollectibleRequest(AdminCollectibleUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "collectible request is required");
        }
        validateBindings(
                request.getStorylineBindings(),
                request.getCityBindings(),
                request.getSubMapBindings(),
                request.getIndoorBuildingBindings(),
                request.getIndoorFloorBindings(),
                request.getAttachmentAssetIds());
    }

    private void validateBadgeRequest(AdminBadgeUpsertRequest request) {
        if (request == null) {
            throw new BusinessException(4001, "badge request is required");
        }
        validateBindings(
                request.getStorylineBindings(),
                request.getCityBindings(),
                request.getSubMapBindings(),
                request.getIndoorBuildingBindings(),
                request.getIndoorFloorBindings(),
                request.getAttachmentAssetIds());
    }

    private void validateRewardRequest(AdminRewardUpsertRequest.Upsert request) {
        if (request == null) {
            throw new BusinessException(4001, "reward request is required");
        }
        validateBindings(
                request.getStorylineBindings(),
                request.getCityBindings(),
                request.getSubMapBindings(),
                request.getIndoorBuildingBindings(),
                request.getIndoorFloorBindings(),
                request.getAttachmentAssetIds());
    }

    private void validateBindings(
            List<Long> storylineBindings,
            List<Long> cityBindings,
            List<Long> subMapBindings,
            List<Long> indoorBuildingBindings,
            List<Long> indoorFloorBindings,
            List<Long> attachmentAssetIds
    ) {
        List<Long> normalizedStorylines = normalizeIds(storylineBindings);
        List<Long> normalizedCities = normalizeIds(cityBindings);
        List<Long> normalizedSubMaps = normalizeIds(subMapBindings);
        List<Long> normalizedBuildings = normalizeIds(indoorBuildingBindings);
        List<Long> normalizedFloors = normalizeIds(indoorFloorBindings);
        List<Long> normalizedAssets = normalizeIds(attachmentAssetIds);

        validateIdsExist(normalizedStorylines, "storyline", id -> storyLineMapper.selectById(id) != null);
        validateIdsExist(normalizedCities, "city", id -> cityMapper.selectById(id) != null);
        validateIdsExist(normalizedSubMaps, "sub_map", id -> subMapMapper.selectById(id) != null);
        validateIdsExist(normalizedBuildings, "indoor_building", id -> buildingMapper.selectById(id) != null);
        validateIdsExist(normalizedFloors, "indoor_floor", id -> indoorFloorMapper.selectById(id) != null);
        validateIdsExist(normalizedAssets, "asset", id -> contentAssetMapper.selectById(id) != null);

        if (!normalizedFloors.isEmpty() && !normalizedBuildings.isEmpty()) {
            List<Long> invalidFloorIds = normalizedFloors.stream()
                    .filter(floorId -> {
                        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
                        return floor == null || floor.getBuildingId() == null || !normalizedBuildings.contains(floor.getBuildingId());
                    })
                    .toList();
            if (!invalidFloorIds.isEmpty()) {
                throw new BusinessException(4002, "indoor_floor bindings must belong to the selected indoor_building bindings");
            }
        }
    }

    private void validateIdsExist(List<Long> ids, String targetType, java.util.function.Predicate<Long> existsPredicate) {
        for (Long id : ids) {
            if (!existsPredicate.test(id)) {
                throw new BusinessException(4002, targetType + " relation target not found");
            }
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<Long> bindingIds(String ownerType, Long ownerId, String relationType, String targetType) {
        return adminContentRelationService.listTargetIds(ownerType, ownerId, relationType, targetType);
    }

    private Collectible requireCollectible(Long collectibleId) {
        Collectible collectible = collectibleMapper.selectById(collectibleId);
        if (collectible == null) {
            throw new BusinessException(4046, "Collectible not found");
        }
        return collectible;
    }

    private Badge requireBadge(Long badgeId) {
        Badge badge = badgeMapper.selectById(badgeId);
        if (badge == null) {
            throw new BusinessException(4047, "Badge not found");
        }
        return badge;
    }

    private Reward requireReward(Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4045, "Reward not found");
        }
        return reward;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(4001, message);
        }
        return value.trim();
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Integer defaultNumber(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String normalizeStatus(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String normalized = value.trim();
        if ("1".equals(normalized) || "active".equalsIgnoreCase(normalized)) {
            return "published";
        }
        if ("0".equals(normalized) || "inactive".equalsIgnoreCase(normalized)) {
            return "archived";
        }
        return normalized;
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }
}
