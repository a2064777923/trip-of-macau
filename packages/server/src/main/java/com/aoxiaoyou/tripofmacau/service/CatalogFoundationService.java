package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.ContentAssetLink;
import com.aoxiaoyou.tripofmacau.entity.ContentRelationLink;
import com.aoxiaoyou.tripofmacau.entity.Activity;
import com.aoxiaoyou.tripofmacau.entity.Badge;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogFoundationService {

    List<City> listPublishedCities();

    Optional<City> getPublishedCityByCode(String code);

    List<SubMap> listPublishedSubMaps(Long cityId);

    Optional<SubMap> getPublishedSubMapByCode(String code);

    List<Poi> listPublishedPois(Long cityId, Long subMapId, Long storylineId, String keyword);

    Optional<Poi> getPublishedPoi(Long poiId);

    List<StoryLine> listPublishedStoryLines();

    Optional<StoryLine> getPublishedStoryLine(Long storyLineId);

    List<StoryChapter> listPublishedStoryChapters(Collection<Long> storylineIds);

    List<StoryChapterBlockLink> listPublishedStoryChapterBlockLinks(Collection<Long> chapterIds);

    Map<Long, StoryContentBlock> getPublishedStoryContentBlocksByIds(Collection<Long> blockIds);

    List<ContentAssetLink> listPublishedContentAssetLinks(String entityType, Collection<Long> entityIds);

    List<ContentRelationLink> listRelationLinks(String ownerType, Collection<Long> ownerIds, String relationType);

    List<TipArticle> listPublishedTipArticles(String categoryCode, String keyword);

    Optional<TipArticle> getPublishedTipArticle(Long articleId);

    List<Reward> listPublishedRewards();

    List<RedeemablePrize> listPublishedRedeemablePrizes();

    List<GameReward> listPublishedGameRewards(Boolean honorsOnly);

    Map<Long, List<RewardRuleBinding>> getRewardRuleBindings(String ownerDomain, Collection<Long> ownerIds);

    Map<Long, RewardRule> getRewardRulesByIds(Collection<Long> ruleIds);

    Map<Long, RewardPresentation> getRewardPresentationsByIds(Collection<Long> presentationIds);

    List<RewardPresentationStep> listRewardPresentationSteps(Collection<Long> presentationIds);

    List<Activity> listPublishedActivities();

    List<Collectible> listPublishedCollectibles();

    List<Badge> listPublishedBadges();

    List<Stamp> listPublishedStamps();

    List<Notification> listPublishedNotifications();

    Map<Long, ContentAsset> getPublishedAssetsByIds(Collection<Long> assetIds);

    Map<Long, IndoorBuilding> getPublishedIndoorBuildingsByIds(Collection<Long> buildingIds);

    Map<Long, IndoorFloor> getPublishedIndoorFloorsByIds(Collection<Long> floorIds);
}
