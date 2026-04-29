package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
import com.aoxiaoyou.tripofmacau.entity.Activity;
import com.aoxiaoyou.tripofmacau.entity.Badge;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.ContentAssetLink;
import com.aoxiaoyou.tripofmacau.entity.ContentRelationLink;
import com.aoxiaoyou.tripofmacau.entity.City;
import com.aoxiaoyou.tripofmacau.entity.Collectible;
import com.aoxiaoyou.tripofmacau.entity.GameReward;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.Notification;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.entity.RedeemablePrize;
import com.aoxiaoyou.tripofmacau.entity.RewardPresentation;
import com.aoxiaoyou.tripofmacau.entity.RewardPresentationStep;
import com.aoxiaoyou.tripofmacau.entity.RewardRule;
import com.aoxiaoyou.tripofmacau.entity.RewardRuleBinding;
import com.aoxiaoyou.tripofmacau.entity.Reward;
import com.aoxiaoyou.tripofmacau.entity.Stamp;
import com.aoxiaoyou.tripofmacau.entity.StoryChapter;
import com.aoxiaoyou.tripofmacau.entity.StoryChapterBlockLink;
import com.aoxiaoyou.tripofmacau.entity.StoryContentBlock;
import com.aoxiaoyou.tripofmacau.entity.StoryLine;
import com.aoxiaoyou.tripofmacau.entity.SubMap;
import com.aoxiaoyou.tripofmacau.entity.TipArticle;
import com.aoxiaoyou.tripofmacau.mapper.ActivityMapper;
import com.aoxiaoyou.tripofmacau.mapper.BadgeMapper;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.tripofmacau.mapper.ContentRelationLinkMapper;
import com.aoxiaoyou.tripofmacau.mapper.CityMapper;
import com.aoxiaoyou.tripofmacau.mapper.CollectibleMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorBuildingMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorFloorMapper;
import com.aoxiaoyou.tripofmacau.mapper.NotificationMapper;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardPresentationMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardPresentationStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardRuleMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardMapper;
import com.aoxiaoyou.tripofmacau.mapper.StampMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryChapterMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryChapterBlockLinkMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryContentBlockMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryLineMapper;
import com.aoxiaoyou.tripofmacau.mapper.SubMapMapper;
import com.aoxiaoyou.tripofmacau.mapper.TipArticleMapper;
import com.aoxiaoyou.tripofmacau.mapper.GameRewardMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogFoundationServiceImpl implements CatalogFoundationService {

    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final PoiMapper poiMapper;
    private final ActivityMapper activityMapper;
    private final CollectibleMapper collectibleMapper;
    private final BadgeMapper badgeMapper;
    private final IndoorBuildingMapper indoorBuildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final StoryChapterBlockLinkMapper storyChapterBlockLinkMapper;
    private final StoryContentBlockMapper storyContentBlockMapper;
    private final ContentRelationLinkMapper contentRelationLinkMapper;
    private final ContentAssetLinkMapper contentAssetLinkMapper;
    private final TipArticleMapper tipArticleMapper;
    private final RewardMapper rewardMapper;
    private final RedeemablePrizeMapper redeemablePrizeMapper;
    private final GameRewardMapper gameRewardMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RewardRuleBindingMapper rewardRuleBindingMapper;
    private final RewardPresentationMapper rewardPresentationMapper;
    private final RewardPresentationStepMapper rewardPresentationStepMapper;
    private final StampMapper stampMapper;
    private final NotificationMapper notificationMapper;
    private final ContentAssetMapper contentAssetMapper;

    @Override
    public List<City> listPublishedCities() {
        return cityMapper.selectList(new LambdaQueryWrapper<City>()
                .eq(City::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(City::getSortOrder)
                .orderByAsc(City::getId));
    }

    @Override
    public Optional<City> getPublishedCityByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        return Optional.ofNullable(cityMapper.selectOne(new LambdaQueryWrapper<City>()
                .eq(City::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .eq(City::getCode, code)
                .last("LIMIT 1")));
    }

    @Override
    public List<SubMap> listPublishedSubMaps(Long cityId) {
        return subMapMapper.selectList(new LambdaQueryWrapper<SubMap>()
                .eq(SubMap::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .eq(cityId != null, SubMap::getCityId, cityId)
                .orderByAsc(SubMap::getSortOrder)
                .orderByAsc(SubMap::getId));
    }

    @Override
    public Optional<SubMap> getPublishedSubMapByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        return Optional.ofNullable(subMapMapper.selectOne(new LambdaQueryWrapper<SubMap>()
                .eq(SubMap::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .eq(SubMap::getCode, code)
                .last("LIMIT 1")));
    }

    @Override
    public List<Poi> listPublishedPois(Long cityId, Long subMapId, Long storylineId, String keyword) {
        return poiMapper.selectList(new LambdaQueryWrapper<Poi>()
                .eq(Poi::getStatus, ContentStatus.PUBLISHED.getCode())
                .eq(cityId != null, Poi::getCityId, cityId)
                .eq(subMapId != null, Poi::getSubMapId, subMapId)
                .eq(storylineId != null, Poi::getStorylineId, storylineId)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(Poi::getNameZh, keyword)
                        .or()
                        .like(Poi::getNameEn, keyword)
                        .or()
                        .like(Poi::getNameZht, keyword)
                        .or()
                        .like(Poi::getNamePt, keyword)
                        .or()
                        .like(Poi::getAddressZh, keyword)
                        .or()
                        .like(Poi::getAddressEn, keyword)
                        .or()
                        .like(Poi::getAddressZht, keyword)
                        .or()
                        .like(Poi::getAddressPt, keyword))
                .orderByAsc(Poi::getSortOrder)
                .orderByAsc(Poi::getId));
    }

    @Override
    public Optional<Poi> getPublishedPoi(Long poiId) {
        return Optional.ofNullable(poiMapper.selectOne(new LambdaQueryWrapper<Poi>()
                .eq(Poi::getId, poiId)
                .eq(Poi::getStatus, ContentStatus.PUBLISHED.getCode())
                .last("LIMIT 1")));
    }

    @Override
    public List<StoryLine> listPublishedStoryLines() {
        return storyLineMapper.selectList(new LambdaQueryWrapper<StoryLine>()
                .eq(StoryLine::getStatus, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(StoryLine::getSortOrder)
                .orderByAsc(StoryLine::getId));
    }

    @Override
    public Optional<StoryLine> getPublishedStoryLine(Long storyLineId) {
        return Optional.ofNullable(storyLineMapper.selectOne(new LambdaQueryWrapper<StoryLine>()
                .eq(StoryLine::getId, storyLineId)
                .eq(StoryLine::getStatus, ContentStatus.PUBLISHED.getCode())
                .last("LIMIT 1")));
    }

    @Override
    public List<StoryChapter> listPublishedStoryChapters(Collection<Long> storylineIds) {
        if (storylineIds == null || storylineIds.isEmpty()) {
            return Collections.emptyList();
        }
        return storyChapterMapper.selectList(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getStatus, ContentStatus.PUBLISHED.getCode())
                .in(StoryChapter::getStorylineId, storylineIds)
                .orderByAsc(StoryChapter::getStorylineId)
                .orderByAsc(StoryChapter::getChapterOrder)
                .orderByAsc(StoryChapter::getSortOrder)
                .orderByAsc(StoryChapter::getId));
    }

    @Override
    public List<StoryChapterBlockLink> listPublishedStoryChapterBlockLinks(Collection<Long> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Collections.emptyList();
        }
        return storyChapterBlockLinkMapper.selectList(new LambdaQueryWrapper<StoryChapterBlockLink>()
                .in(StoryChapterBlockLink::getChapterId, chapterIds)
                .eq(StoryChapterBlockLink::getStatus, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(StoryChapterBlockLink::getChapterId)
                .orderByAsc(StoryChapterBlockLink::getSortOrder)
                .orderByAsc(StoryChapterBlockLink::getId));
    }

    @Override
    public Map<Long, StoryContentBlock> getPublishedStoryContentBlocksByIds(Collection<Long> blockIds) {
        if (blockIds == null || blockIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return storyContentBlockMapper.selectList(new LambdaQueryWrapper<StoryContentBlock>()
                        .in(StoryContentBlock::getId, blockIds)
                        .eq(StoryContentBlock::getStatus, ContentStatus.PUBLISHED.getCode()))
                .stream()
                .collect(Collectors.toMap(StoryContentBlock::getId, item -> item, (left, right) -> left));
    }

    @Override
    public List<ContentAssetLink> listPublishedContentAssetLinks(String entityType, Collection<Long> entityIds) {
        if (!StringUtils.hasText(entityType) || entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                .eq(ContentAssetLink::getEntityType, entityType)
                .in(ContentAssetLink::getEntityId, entityIds)
                .eq(ContentAssetLink::getStatus, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(ContentAssetLink::getEntityId)
                .orderByAsc(ContentAssetLink::getSortOrder)
                .orderByAsc(ContentAssetLink::getId));
    }

    @Override
    public List<ContentRelationLink> listRelationLinks(String ownerType, Collection<Long> ownerIds, String relationType) {
        if (!StringUtils.hasText(ownerType) || ownerIds == null || ownerIds.isEmpty() || !StringUtils.hasText(relationType)) {
            return Collections.emptyList();
        }
        return contentRelationLinkMapper.selectList(new LambdaQueryWrapper<ContentRelationLink>()
                .eq(ContentRelationLink::getOwnerType, ownerType)
                .in(ContentRelationLink::getOwnerId, ownerIds)
                .eq(ContentRelationLink::getRelationType, relationType)
                .orderByAsc(ContentRelationLink::getOwnerId)
                .orderByAsc(ContentRelationLink::getSortOrder)
                .orderByAsc(ContentRelationLink::getId));
    }

    @Override
    public List<TipArticle> listPublishedTipArticles(String categoryCode, String keyword) {
        return tipArticleMapper.selectList(new LambdaQueryWrapper<TipArticle>()
                .eq(TipArticle::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .eq(StringUtils.hasText(categoryCode), TipArticle::getCategoryCode, categoryCode)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(TipArticle::getTitleZh, keyword)
                        .or()
                        .like(TipArticle::getTitleEn, keyword)
                        .or()
                        .like(TipArticle::getTitleZht, keyword)
                        .or()
                        .like(TipArticle::getTitlePt, keyword)
                        .or()
                        .like(TipArticle::getSummaryZh, keyword)
                        .or()
                        .like(TipArticle::getSummaryEn, keyword)
                        .or()
                        .like(TipArticle::getSummaryZht, keyword)
                        .or()
                        .like(TipArticle::getSummaryPt, keyword))
                .orderByAsc(TipArticle::getSortOrder)
                .orderByAsc(TipArticle::getId));
    }

    @Override
    public Optional<TipArticle> getPublishedTipArticle(Long articleId) {
        return Optional.ofNullable(tipArticleMapper.selectOne(new LambdaQueryWrapper<TipArticle>()
                .eq(TipArticle::getId, articleId)
                .eq(TipArticle::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .last("LIMIT 1")));
    }

    @Override
    public List<Reward> listPublishedRewards() {
        LocalDateTime now = LocalDateTime.now();
        return rewardMapper.selectList(new LambdaQueryWrapper<Reward>()
                .eq(Reward::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper.isNull(Reward::getPublishStartAt).or().le(Reward::getPublishStartAt, now))
                .and(wrapper -> wrapper.isNull(Reward::getPublishEndAt).or().ge(Reward::getPublishEndAt, now))
                .orderByAsc(Reward::getSortOrder)
                .orderByAsc(Reward::getId));
    }

    @Override
    public List<RedeemablePrize> listPublishedRedeemablePrizes() {
        LocalDateTime now = LocalDateTime.now();
        return redeemablePrizeMapper.selectList(new LambdaQueryWrapper<RedeemablePrize>()
                .eq(RedeemablePrize::getStatus, ContentStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper.isNull(RedeemablePrize::getPublishStartAt).or().le(RedeemablePrize::getPublishStartAt, now))
                .and(wrapper -> wrapper.isNull(RedeemablePrize::getPublishEndAt).or().ge(RedeemablePrize::getPublishEndAt, now))
                .orderByAsc(RedeemablePrize::getSortOrder)
                .orderByAsc(RedeemablePrize::getId));
    }

    @Override
    public List<GameReward> listPublishedGameRewards(Boolean honorsOnly) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<GameReward> wrapper = new LambdaQueryWrapper<GameReward>()
                .eq(GameReward::getStatus, ContentStatus.PUBLISHED.getCode())
                .and(query -> query.isNull(GameReward::getPublishStartAt).or().le(GameReward::getPublishStartAt, now))
                .and(query -> query.isNull(GameReward::getPublishEndAt).or().ge(GameReward::getPublishEndAt, now))
                .orderByAsc(GameReward::getSortOrder)
                .orderByAsc(GameReward::getId);
        if (Boolean.TRUE.equals(honorsOnly)) {
            wrapper.in(GameReward::getRewardType, List.of("badge", "title"));
        }
        return gameRewardMapper.selectList(wrapper);
    }

    @Override
    public Map<Long, List<RewardRuleBinding>> getRewardRuleBindings(String ownerDomain, Collection<Long> ownerIds) {
        if (!StringUtils.hasText(ownerDomain) || ownerIds == null || ownerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                        .eq(RewardRuleBinding::getOwnerDomain, ownerDomain)
                        .in(RewardRuleBinding::getOwnerId, ownerIds)
                        .orderByAsc(RewardRuleBinding::getOwnerId)
                        .orderByAsc(RewardRuleBinding::getSortOrder)
                        .orderByAsc(RewardRuleBinding::getId))
                .stream()
                .collect(Collectors.groupingBy(RewardRuleBinding::getOwnerId, Collectors.toList()));
    }

    @Override
    public Map<Long, RewardRule> getRewardRulesByIds(Collection<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rewardRuleMapper.selectBatchIds(ruleIds).stream()
                .collect(Collectors.toMap(RewardRule::getId, item -> item, (left, right) -> left));
    }

    @Override
    public Map<Long, RewardPresentation> getRewardPresentationsByIds(Collection<Long> presentationIds) {
        if (presentationIds == null || presentationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rewardPresentationMapper.selectList(new LambdaQueryWrapper<RewardPresentation>()
                        .in(RewardPresentation::getId, presentationIds)
                        .eq(RewardPresentation::getStatus, ContentStatus.PUBLISHED.getCode()))
                .stream()
                .collect(Collectors.toMap(RewardPresentation::getId, item -> item, (left, right) -> left));
    }

    @Override
    public List<RewardPresentationStep> listRewardPresentationSteps(Collection<Long> presentationIds) {
        if (presentationIds == null || presentationIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rewardPresentationStepMapper.selectList(new LambdaQueryWrapper<RewardPresentationStep>()
                .in(RewardPresentationStep::getPresentationId, presentationIds)
                .orderByAsc(RewardPresentationStep::getPresentationId)
                .orderByAsc(RewardPresentationStep::getSortOrder)
                .orderByAsc(RewardPresentationStep::getId));
    }

    @Override
    public List<Activity> listPublishedActivities() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, ContentStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper.isNull(Activity::getPublishStartAt).or().le(Activity::getPublishStartAt, now))
                .and(wrapper -> wrapper.isNull(Activity::getPublishEndAt).or().ge(Activity::getPublishEndAt, now))
                .orderByDesc(Activity::getIsPinned)
                .orderByAsc(Activity::getSortOrder)
                .orderByAsc(Activity::getId));
    }

    @Override
    public List<Collectible> listPublishedCollectibles() {
        return collectibleMapper.selectList(new LambdaQueryWrapper<Collectible>()
                .eq(Collectible::getStatus, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(Collectible::getSortOrder)
                .orderByAsc(Collectible::getId));
    }

    @Override
    public List<Badge> listPublishedBadges() {
        return badgeMapper.selectList(new LambdaQueryWrapper<Badge>()
                .eq(Badge::getStatus, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(Badge::getId));
    }

    @Override
    public List<Stamp> listPublishedStamps() {
        return stampMapper.selectList(new LambdaQueryWrapper<Stamp>()
                .eq(Stamp::getStatusCode, ContentStatus.PUBLISHED.getCode())
                .orderByAsc(Stamp::getSortOrder)
                .orderByAsc(Stamp::getId));
    }

    @Override
    public List<Notification> listPublishedNotifications() {
        LocalDateTime now = LocalDateTime.now();
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getStatus, ContentStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper.isNull(Notification::getPublishStartAt).or().le(Notification::getPublishStartAt, now))
                .and(wrapper -> wrapper.isNull(Notification::getPublishEndAt).or().ge(Notification::getPublishEndAt, now))
                .orderByAsc(Notification::getSortOrder)
                .orderByAsc(Notification::getId));
    }

    @Override
    public Map<Long, ContentAsset> getPublishedAssetsByIds(Collection<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return contentAssetMapper.selectList(new LambdaQueryWrapper<ContentAsset>()
                        .in(ContentAsset::getId, assetIds)
                        .eq(ContentAsset::getStatus, ContentStatus.PUBLISHED.getCode()))
                .stream()
                .collect(Collectors.toMap(ContentAsset::getId, asset -> asset, (left, right) -> left));
    }

    @Override
    public Map<Long, IndoorBuilding> getPublishedIndoorBuildingsByIds(Collection<Long> buildingIds) {
        if (buildingIds == null || buildingIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return indoorBuildingMapper.selectList(new LambdaQueryWrapper<IndoorBuilding>()
                        .in(IndoorBuilding::getId, buildingIds)
                        .eq(IndoorBuilding::getStatus, ContentStatus.PUBLISHED.getCode()))
                .stream()
                .collect(Collectors.toMap(IndoorBuilding::getId, item -> item, (left, right) -> left));
    }

    @Override
    public Map<Long, IndoorFloor> getPublishedIndoorFloorsByIds(Collection<Long> floorIds) {
        if (floorIds == null || floorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return indoorFloorMapper.selectList(new LambdaQueryWrapper<IndoorFloor>()
                        .in(IndoorFloor::getId, floorIds)
                        .eq(IndoorFloor::getStatus, ContentStatus.PUBLISHED.getCode()))
                .stream()
                .collect(Collectors.toMap(IndoorFloor::getId, item -> item, (left, right) -> left));
    }
}
