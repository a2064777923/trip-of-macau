package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.ActivityResponse;
import com.aoxiaoyou.tripofmacau.dto.response.BadgeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.CityResponse;
import com.aoxiaoyou.tripofmacau.dto.response.CatalogRelationBindingResponse;
import com.aoxiaoyou.tripofmacau.dto.response.CollectibleResponse;
import com.aoxiaoyou.tripofmacau.dto.response.DiscoverCardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.GameRewardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.NotificationResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RedeemablePrizeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardPresentationResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardPresentationStepResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardRuleSummaryResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StampResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterConditionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterEffectResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterUnlockResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryContentBlockResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryMediaAssetResponse;
import com.aoxiaoyou.tripofmacau.dto.response.SubMapResponse;
import com.aoxiaoyou.tripofmacau.dto.response.TipArticleResponse;
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
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicCatalogServiceImpl implements PublicCatalogService {

    private final CatalogFoundationService catalogFoundationService;
    private final RuntimeSettingsService runtimeSettingsService;
    private final LocalizedContentSupport localizedContentSupport;
    private final ObjectMapper objectMapper;

    @Override
    public List<CityResponse> listCities(String localeHint) {
        List<City> cities = catalogFoundationService.listPublishedCities();
        List<SubMap> subMaps = catalogFoundationService.listPublishedSubMaps(null);
        Map<Long, List<SubMap>> subMapsByCity = subMaps.stream()
                .collect(Collectors.groupingBy(SubMap::getCityId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.concat(
                        cities.stream().flatMap(city -> Stream.of(city.getCoverAssetId(), city.getBannerAssetId())),
                        subMaps.stream().map(SubMap::getCoverAssetId))
                .filter(Objects::nonNull)
                .toList());
        return cities.stream()
                .map(city -> toCityResponse(city, subMapsByCity.getOrDefault(city.getId(), Collections.emptyList()), assets, localeHint))
                .toList();
    }

    @Override
    public List<SubMapResponse> listSubMaps(String localeHint, String cityCode) {
        Long cityId = null;
        if (StringUtils.hasText(cityCode)) {
            cityId = catalogFoundationService.getPublishedCityByCode(cityCode)
                    .map(City::getId)
                    .orElse(-1L);
        }
        List<SubMap> subMaps = catalogFoundationService.listPublishedSubMaps(cityId);
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(subMaps.stream()
                .map(SubMap::getCoverAssetId)
                .filter(Objects::nonNull)
                .toList());
        return subMaps.stream()
                .map(subMap -> toSubMapResponse(subMap, citiesById, assets, localeHint))
                .toList();
    }

    @Override
    public List<PoiResponse> listPois(String localeHint, String cityCode, String subMapCode, Long storylineId, String keyword) {
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        Long cityId = null;
        if (StringUtils.hasText(cityCode)) {
            cityId = catalogFoundationService.getPublishedCityByCode(cityCode)
                    .map(City::getId)
                    .orElse(-1L);
        }

        Long subMapId = null;
        if (StringUtils.hasText(subMapCode)) {
            subMapId = catalogFoundationService.getPublishedSubMapByCode(subMapCode)
                    .map(SubMap::getId)
                    .orElse(-1L);
        }

        List<Poi> pois = catalogFoundationService.listPublishedPois(cityId, subMapId, storylineId, keyword);
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(pois.stream()
                .flatMap(poi -> Stream.of(poi.getCoverAssetId(), poi.getMapIconAssetId(), poi.getAudioAssetId()))
                .filter(Objects::nonNull)
                .toList());

        return pois.stream()
                .map(poi -> toPoiResponse(poi, citiesById, subMapsById, storyLinesById, assets, localeHint))
                .toList();
    }

    @Override
    public PoiResponse getPoi(Long poiId, String localeHint) {
        Poi poi = catalogFoundationService.getPublishedPoi(poiId)
                .orElseThrow(() -> new BusinessException(4041, "POI not found"));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(nonNullIds(poi.getCoverAssetId(), poi.getMapIconAssetId(), poi.getAudioAssetId()));
        return toPoiResponse(poi, citiesById, subMapsById, storyLinesById, assets, localeHint);
    }

    @Override
    public List<StoryLineResponse> listStoryLines(String localeHint) {
        return buildStorylineResponses(catalogFoundationService.listPublishedStoryLines(), localeHint);
    }

    @Override
    public StoryLineResponse getStoryLine(Long storyLineId, String localeHint) {
        StoryLine storyLine = catalogFoundationService.getPublishedStoryLine(storyLineId)
                .orElseThrow(() -> new BusinessException(4042, "Storyline not found"));
        return buildStorylineResponses(List.of(storyLine), localeHint).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(4042, "Storyline not found"));
    }

    @Override
    public List<TipArticleResponse> listTipArticles(String localeHint, String categoryCode, String keyword) {
        List<TipArticle> articles = catalogFoundationService.listPublishedTipArticles(categoryCode, keyword);
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(articles.stream()
                .map(TipArticle::getCoverAssetId)
                .filter(Objects::nonNull)
                .toList());
        return articles.stream()
                .map(article -> toTipArticleResponse(article, citiesById, assets, localeHint))
                .toList();
    }

    @Override
    public TipArticleResponse getTipArticle(Long articleId, String localeHint) {
        TipArticle article = catalogFoundationService.getPublishedTipArticle(articleId)
                .orElseThrow(() -> new BusinessException(4043, "Tip article not found"));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(nonNullIds(article.getCoverAssetId()));
        return toTipArticleResponse(article, citiesById, assets, localeHint);
    }

    @Override
    public List<RewardResponse> listRewards(String localeHint) {
        List<Reward> rewards = catalogFoundationService.listPublishedRewards();
        List<Long> rewardIds = rewards.stream().map(Reward::getId).toList();
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> storylineBindingsByReward = groupRelationLinks("reward", rewardIds, "storyline_binding", "storyline");
        Map<Long, List<ContentRelationLink>> cityBindingsByReward = groupRelationLinks("reward", rewardIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindingsByReward = groupRelationLinks("reward", rewardIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> indoorBuildingBindingsByReward = groupRelationLinks("reward", rewardIds, "indoor_building_binding", "indoor_building");
        Map<Long, List<ContentRelationLink>> indoorFloorBindingsByReward = groupRelationLinks("reward", rewardIds, "indoor_floor_binding", "indoor_floor");
        Map<Long, List<ContentRelationLink>> attachmentBindingsByReward = groupRelationLinks("reward", rewardIds, "attachment_asset", "asset");
        Map<Long, IndoorBuilding> indoorBuildingsById = catalogFoundationService.getPublishedIndoorBuildingsByIds(flattenTargetIds(indoorBuildingBindingsByReward));
        Map<Long, IndoorFloor> indoorFloorsById = catalogFoundationService.getPublishedIndoorFloorsByIds(flattenTargetIds(indoorFloorBindingsByReward));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.concat(
                        rewards.stream().map(Reward::getCoverAssetId),
                        attachmentBindingsByReward.values().stream()
                                .flatMap(List::stream)
                                .map(ContentRelationLink::getTargetId))
                .filter(Objects::nonNull)
                .toList());
        return rewards.stream()
                .map(reward -> RewardResponse.builder()
                        .id(reward.getId())
                        .code(reward.getCode())
                        .name(localizedContentSupport.resolveText(localeHint, reward.getNameZh(), reward.getNameEn(), reward.getNameZht(), reward.getNamePt()))
                        .subtitle(localizedContentSupport.resolveText(localeHint, reward.getSubtitleZh(), reward.getSubtitleEn(), reward.getSubtitleZht(), reward.getSubtitlePt()))
                        .description(localizedContentSupport.resolveText(localeHint, reward.getDescriptionZh(), reward.getDescriptionEn(), reward.getDescriptionZht(), reward.getDescriptionPt()))
                        .highlight(localizedContentSupport.resolveText(localeHint, reward.getHighlightZh(), reward.getHighlightEn(), reward.getHighlightZht(), reward.getHighlightPt()))
                        .stampCost(reward.getStampCost())
                        .inventoryTotal(reward.getInventoryTotal())
                        .inventoryRedeemed(reward.getInventoryRedeemed())
                        .availableInventory(Math.max(0, safeInt(reward.getInventoryTotal()) - safeInt(reward.getInventoryRedeemed())))
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, reward.getCoverAssetId()))
                        .popupPresetCode(reward.getPopupPresetCode())
                        .popupConfigJson(reward.getPopupConfigJson())
                        .displayPresetCode(reward.getDisplayPresetCode())
                        .displayConfigJson(reward.getDisplayConfigJson())
                        .triggerPresetCode(reward.getTriggerPresetCode())
                        .triggerConfigJson(reward.getTriggerConfigJson())
                        .exampleContent(localizedContentSupport.resolveText(localeHint, reward.getExampleContentZh(), reward.getExampleContentEn(), reward.getExampleContentZht(), reward.getExampleContentPt()))
                        .relatedStorylines(toStorylineBindings(storylineBindingsByReward.get(reward.getId()), storyLinesById, localeHint))
                        .relatedCities(toCityBindings(cityBindingsByReward.get(reward.getId()), citiesById, null, localeHint))
                        .relatedSubMaps(toSubMapBindings(subMapBindingsByReward.get(reward.getId()), subMapsById, localeHint))
                        .relatedIndoorBuildings(toIndoorBuildingBindings(indoorBuildingBindingsByReward.get(reward.getId()), indoorBuildingsById, localeHint))
                        .relatedIndoorFloors(toIndoorFloorBindings(indoorFloorBindingsByReward.get(reward.getId()), indoorFloorsById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindingsByReward.get(reward.getId()), assets))
                        .sortOrder(reward.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<RedeemablePrizeResponse> listRedeemablePrizes(String localeHint) {
        List<RedeemablePrize> prizes = catalogFoundationService.listPublishedRedeemablePrizes();
        if (prizes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> prizeIds = prizes.stream().map(RedeemablePrize::getId).toList();
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> storylineBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "storyline_binding", "storyline");
        Map<Long, List<ContentRelationLink>> cityBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> indoorBuildingBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "indoor_building_binding", "indoor_building");
        Map<Long, List<ContentRelationLink>> indoorFloorBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "indoor_floor_binding", "indoor_floor");
        Map<Long, List<ContentRelationLink>> attachmentBindingsByPrize = groupRelationLinks("redeemable_prize", prizeIds, "attachment_asset", "asset");
        Map<Long, IndoorBuilding> indoorBuildingsById = catalogFoundationService.getPublishedIndoorBuildingsByIds(flattenTargetIds(indoorBuildingBindingsByPrize));
        Map<Long, IndoorFloor> indoorFloorsById = catalogFoundationService.getPublishedIndoorFloorsByIds(flattenTargetIds(indoorFloorBindingsByPrize));
        Map<Long, List<RewardRuleBinding>> ruleBindingsByPrize = catalogFoundationService.getRewardRuleBindings("redeemable_prize", prizeIds);
        Map<Long, RewardRule> rewardRulesById = catalogFoundationService.getRewardRulesByIds(ruleBindingsByPrize.values().stream()
                .flatMap(List::stream)
                .map(RewardRuleBinding::getRuleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Map<Long, RewardPresentation> presentationsById = catalogFoundationService.getRewardPresentationsByIds(
                prizes.stream().map(RedeemablePrize::getPresentationId).filter(Objects::nonNull).distinct().toList()
        );
        Map<Long, List<RewardPresentationStep>> stepsByPresentationId = catalogFoundationService.listRewardPresentationSteps(presentationsById.keySet())
                .stream()
                .collect(Collectors.groupingBy(RewardPresentationStep::getPresentationId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.of(
                        prizes.stream().map(RedeemablePrize::getCoverAssetId),
                        attachmentBindingsByPrize.values().stream().flatMap(List::stream).map(ContentRelationLink::getTargetId),
                        presentationAssetIds(presentationsById.values(), stepsByPresentationId).stream())
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .toList());

        return prizes.stream()
                .map(prize -> RedeemablePrizeResponse.builder()
                        .id(prize.getId())
                        .code(prize.getCode())
                        .prizeType(prize.getPrizeType())
                        .fulfillmentMode(prize.getFulfillmentMode())
                        .name(localizedContentSupport.resolveText(localeHint, prize.getNameZh(), prize.getNameEn(), prize.getNameZht(), prize.getNamePt()))
                        .subtitle(localizedContentSupport.resolveText(localeHint, prize.getSubtitleZh(), prize.getSubtitleEn(), prize.getSubtitleZht(), prize.getSubtitlePt()))
                        .description(localizedContentSupport.resolveText(localeHint, prize.getDescriptionZh(), prize.getDescriptionEn(), prize.getDescriptionZht(), prize.getDescriptionPt()))
                        .highlight(localizedContentSupport.resolveText(localeHint, prize.getHighlightZh(), prize.getHighlightEn(), prize.getHighlightZht(), prize.getHighlightPt()))
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, prize.getCoverAssetId()))
                        .stampCost(prize.getStampCost())
                        .inventoryTotal(prize.getInventoryTotal())
                        .inventoryRedeemed(prize.getInventoryRedeemed())
                        .availableInventory(Math.max(0, safeInt(prize.getInventoryTotal()) - safeInt(prize.getInventoryRedeemed())))
                        .stockPolicyJson(prize.getStockPolicyJson())
                        .fulfillmentConfigJson(prize.getFulfillmentConfigJson())
                        .presentationId(prize.getPresentationId())
                        .presentation(toRewardPresentationResponse(presentationsById.get(prize.getPresentationId()), stepsByPresentationId, assets, localeHint))
                        .ruleSummaries(toRewardRuleSummaries(ruleBindingsByPrize.get(prize.getId()), rewardRulesById, localeHint))
                        .relatedStorylines(toStorylineBindings(storylineBindingsByPrize.get(prize.getId()), storyLinesById, localeHint))
                        .relatedCities(toCityBindings(cityBindingsByPrize.get(prize.getId()), citiesById, null, localeHint))
                        .relatedSubMaps(toSubMapBindings(subMapBindingsByPrize.get(prize.getId()), subMapsById, localeHint))
                        .relatedIndoorBuildings(toIndoorBuildingBindings(indoorBuildingBindingsByPrize.get(prize.getId()), indoorBuildingsById, localeHint))
                        .relatedIndoorFloors(toIndoorFloorBindings(indoorFloorBindingsByPrize.get(prize.getId()), indoorFloorsById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindingsByPrize.get(prize.getId()), assets))
                        .sortOrder(prize.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<GameRewardResponse> listGameRewards(String localeHint, Boolean honorsOnly) {
        List<GameReward> rewards = catalogFoundationService.listPublishedGameRewards(honorsOnly);
        if (rewards.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> rewardIds = rewards.stream().map(GameReward::getId).toList();
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> storylineBindingsByReward = groupRelationLinks("game_reward", rewardIds, "storyline_binding", "storyline");
        Map<Long, List<ContentRelationLink>> cityBindingsByReward = groupRelationLinks("game_reward", rewardIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindingsByReward = groupRelationLinks("game_reward", rewardIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> indoorBuildingBindingsByReward = groupRelationLinks("game_reward", rewardIds, "indoor_building_binding", "indoor_building");
        Map<Long, List<ContentRelationLink>> indoorFloorBindingsByReward = groupRelationLinks("game_reward", rewardIds, "indoor_floor_binding", "indoor_floor");
        Map<Long, List<ContentRelationLink>> attachmentBindingsByReward = groupRelationLinks("game_reward", rewardIds, "attachment_asset", "asset");
        Map<Long, IndoorBuilding> indoorBuildingsById = catalogFoundationService.getPublishedIndoorBuildingsByIds(flattenTargetIds(indoorBuildingBindingsByReward));
        Map<Long, IndoorFloor> indoorFloorsById = catalogFoundationService.getPublishedIndoorFloorsByIds(flattenTargetIds(indoorFloorBindingsByReward));
        Map<Long, List<RewardRuleBinding>> ruleBindingsByReward = catalogFoundationService.getRewardRuleBindings("game_reward", rewardIds);
        Map<Long, RewardRule> rewardRulesById = catalogFoundationService.getRewardRulesByIds(ruleBindingsByReward.values().stream()
                .flatMap(List::stream)
                .map(RewardRuleBinding::getRuleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Map<Long, RewardPresentation> presentationsById = catalogFoundationService.getRewardPresentationsByIds(
                rewards.stream().map(GameReward::getPresentationId).filter(Objects::nonNull).distinct().toList()
        );
        Map<Long, List<RewardPresentationStep>> stepsByPresentationId = catalogFoundationService.listRewardPresentationSteps(presentationsById.keySet())
                .stream()
                .collect(Collectors.groupingBy(RewardPresentationStep::getPresentationId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.of(
                        rewards.stream().flatMap(item -> Stream.of(item.getCoverAssetId(), item.getIconAssetId(), item.getAnimationAssetId())),
                        attachmentBindingsByReward.values().stream().flatMap(List::stream).map(ContentRelationLink::getTargetId),
                        presentationAssetIds(presentationsById.values(), stepsByPresentationId).stream())
                .flatMap(Function.identity())
                .filter(Objects::nonNull)
                .toList());

        return rewards.stream()
                .map(reward -> GameRewardResponse.builder()
                        .id(reward.getId())
                        .code(reward.getCode())
                        .rewardType(reward.getRewardType())
                        .rarity(reward.getRarity())
                        .stackable(reward.getStackable())
                        .maxOwned(reward.getMaxOwned())
                        .canEquip(reward.getCanEquip())
                        .canConsume(reward.getCanConsume())
                        .name(localizedContentSupport.resolveText(localeHint, reward.getNameZh(), reward.getNameEn(), reward.getNameZht(), reward.getNamePt()))
                        .subtitle(localizedContentSupport.resolveText(localeHint, reward.getSubtitleZh(), reward.getSubtitleEn(), reward.getSubtitleZht(), reward.getSubtitlePt()))
                        .description(localizedContentSupport.resolveText(localeHint, reward.getDescriptionZh(), reward.getDescriptionEn(), reward.getDescriptionZht(), reward.getDescriptionPt()))
                        .highlight(localizedContentSupport.resolveText(localeHint, reward.getHighlightZh(), reward.getHighlightEn(), reward.getHighlightZht(), reward.getHighlightPt()))
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, reward.getCoverAssetId()))
                        .iconUrl(localizedContentSupport.resolveAssetUrl(assets, reward.getIconAssetId()))
                        .animationUrl(localizedContentSupport.resolveAssetUrl(assets, reward.getAnimationAssetId()))
                        .rewardConfigJson(reward.getRewardConfigJson())
                        .presentationId(reward.getPresentationId())
                        .presentation(toRewardPresentationResponse(presentationsById.get(reward.getPresentationId()), stepsByPresentationId, assets, localeHint))
                        .ruleSummaries(toRewardRuleSummaries(ruleBindingsByReward.get(reward.getId()), rewardRulesById, localeHint))
                        .relatedStorylines(toStorylineBindings(storylineBindingsByReward.get(reward.getId()), storyLinesById, localeHint))
                        .relatedCities(toCityBindings(cityBindingsByReward.get(reward.getId()), citiesById, null, localeHint))
                        .relatedSubMaps(toSubMapBindings(subMapBindingsByReward.get(reward.getId()), subMapsById, localeHint))
                        .relatedIndoorBuildings(toIndoorBuildingBindings(indoorBuildingBindingsByReward.get(reward.getId()), indoorBuildingsById, localeHint))
                        .relatedIndoorFloors(toIndoorFloorBindings(indoorFloorBindingsByReward.get(reward.getId()), indoorFloorsById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindingsByReward.get(reward.getId()), assets))
                        .sortOrder(reward.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public RewardPresentationResponse getRewardPresentation(Long presentationId, String localeHint) {
        RewardPresentation presentation = catalogFoundationService.getRewardPresentationsByIds(List.of(presentationId)).get(presentationId);
        if (presentation == null) {
            throw new BusinessException(4045, "Reward presentation not found");
        }
        Map<Long, List<RewardPresentationStep>> stepsByPresentationId = catalogFoundationService.listRewardPresentationSteps(List.of(presentationId))
                .stream()
                .collect(Collectors.groupingBy(RewardPresentationStep::getPresentationId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(
                presentationAssetIds(List.of(presentation), stepsByPresentationId)
        );
        return toRewardPresentationResponse(presentation, stepsByPresentationId, assets, localeHint);
    }

    @Override
    public List<ActivityResponse> listActivities(String localeHint) {
        List<Activity> activities = catalogFoundationService.listPublishedActivities();
        if (activities.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> cityBindingsByActivity = catalogFoundationService
                .listRelationLinks("activity", activities.stream().map(Activity::getId).toList(), "city_binding")
                .stream()
                .filter(link -> "city".equals(link.getTargetType()))
                .collect(Collectors.groupingBy(ContentRelationLink::getOwnerId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<ContentRelationLink>> subMapBindingsByActivity = catalogFoundationService
                .listRelationLinks("activity", activities.stream().map(Activity::getId).toList(), "sub_map_binding")
                .stream()
                .filter(link -> "sub_map".equals(link.getTargetType()))
                .collect(Collectors.groupingBy(ContentRelationLink::getOwnerId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<ContentRelationLink>> storylineBindingsByActivity = catalogFoundationService
                .listRelationLinks("activity", activities.stream().map(Activity::getId).toList(), "storyline_binding")
                .stream()
                .filter(link -> "storyline".equals(link.getTargetType()))
                .collect(Collectors.groupingBy(ContentRelationLink::getOwnerId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<ContentRelationLink>> attachmentBindingsByActivity = catalogFoundationService
                .listRelationLinks("activity", activities.stream().map(Activity::getId).toList(), "attachment_asset")
                .stream()
                .filter(link -> "asset".equals(link.getTargetType()))
                .collect(Collectors.groupingBy(ContentRelationLink::getOwnerId, LinkedHashMap::new, Collectors.toList()));

        List<Long> assetIds = activities.stream()
                .flatMap(activity -> Stream.concat(
                        nonNullIds(activity.getCoverAssetId(), activity.getHeroAssetId()).stream(),
                        attachmentBindingsByActivity.getOrDefault(activity.getId(), Collections.emptyList()).stream()
                                .map(ContentRelationLink::getTargetId)
                                .filter(Objects::nonNull)))
                .toList();
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(assetIds);

        return activities.stream()
                .map(activity -> ActivityResponse.builder()
                        .id(activity.getId())
                        .code(activity.getCode())
                        .activityType(activity.getActivityType())
                        .title(localizedContentSupport.resolveText(localeHint, activity.getTitleZh(), activity.getTitleEn(), activity.getTitleZht(), activity.getTitlePt()))
                        .summary(localizedContentSupport.resolveText(localeHint, activity.getSummaryZh(), activity.getSummaryEn(), activity.getSummaryZht(), activity.getSummaryPt()))
                        .description(localizedContentSupport.resolveText(localeHint, activity.getDescriptionZh(), activity.getDescriptionEn(), activity.getDescriptionZht(), activity.getDescriptionPt()))
                        .htmlContent(localizedContentSupport.resolveText(localeHint, activity.getHtmlZh(), activity.getHtmlEn(), activity.getHtmlZht(), activity.getHtmlPt()))
                        .venueName(localizedContentSupport.resolveText(localeHint, activity.getVenueNameZh(), activity.getVenueNameEn(), activity.getVenueNameZht(), activity.getVenueNamePt()))
                        .address(localizedContentSupport.resolveText(localeHint, activity.getAddressZh(), activity.getAddressEn(), activity.getAddressZht(), activity.getAddressPt()))
                        .organizerName(activity.getOrganizerName())
                        .organizerContact(activity.getOrganizerContact())
                        .organizerWebsite(activity.getOrganizerWebsite())
                        .signupCapacity(activity.getSignupCapacity())
                        .signupFeeAmount(activity.getSignupFeeAmount())
                        .signupStartAt(activity.getSignupStartAt())
                        .signupEndAt(activity.getSignupEndAt())
                        .publishStartAt(activity.getPublishStartAt())
                        .publishEndAt(activity.getPublishEndAt())
                        .isPinned(activity.getIsPinned())
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, activity.getCoverAssetId()))
                        .heroImageUrl(localizedContentSupport.resolveAssetUrl(assets, activity.getHeroAssetId()))
                        .cityBindings(toCityBindings(cityBindingsByActivity.get(activity.getId()), citiesById, null, localeHint))
                        .subMapBindings(toSubMapBindings(subMapBindingsByActivity.get(activity.getId()), subMapsById, localeHint))
                        .storylineBindings(toStorylineBindings(storylineBindingsByActivity.get(activity.getId()), storyLinesById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindingsByActivity.get(activity.getId()), assets))
                        .participationCount(activity.getParticipationCount())
                        .sortOrder(activity.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<CollectibleResponse> listCollectibles(String localeHint) {
        List<Collectible> collectibles = catalogFoundationService.listPublishedCollectibles();
        if (collectibles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> collectibleIds = collectibles.stream().map(Collectible::getId).toList();
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> storylineBindings = groupRelationLinks("collectible", collectibleIds, "storyline_binding", "storyline");
        Map<Long, List<ContentRelationLink>> cityBindings = groupRelationLinks("collectible", collectibleIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindings = groupRelationLinks("collectible", collectibleIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> indoorBuildingBindings = groupRelationLinks("collectible", collectibleIds, "indoor_building_binding", "indoor_building");
        Map<Long, List<ContentRelationLink>> indoorFloorBindings = groupRelationLinks("collectible", collectibleIds, "indoor_floor_binding", "indoor_floor");
        Map<Long, List<ContentRelationLink>> attachmentBindings = groupRelationLinks("collectible", collectibleIds, "attachment_asset", "asset");
        Map<Long, IndoorBuilding> indoorBuildingsById = catalogFoundationService.getPublishedIndoorBuildingsByIds(flattenTargetIds(indoorBuildingBindings));
        Map<Long, IndoorFloor> indoorFloorsById = catalogFoundationService.getPublishedIndoorFloorsByIds(flattenTargetIds(indoorFloorBindings));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.concat(
                        collectibles.stream()
                                .flatMap(item -> nonNullIds(item.getCoverAssetId(), item.getIconAssetId(), item.getAnimationAssetId()).stream()),
                        attachmentBindings.values().stream()
                                .flatMap(List::stream)
                                .map(ContentRelationLink::getTargetId))
                .filter(Objects::nonNull)
                .toList());
        return collectibles.stream()
                .map(item -> CollectibleResponse.builder()
                        .id(item.getId())
                        .code(item.getCollectibleCode())
                        .name(localizedContentSupport.resolveText(localeHint, item.getNameZh(), item.getNameEn(), item.getNameZht(), item.getNamePt()))
                        .description(localizedContentSupport.resolveText(localeHint, item.getDescriptionZh(), item.getDescriptionEn(), item.getDescriptionZht(), item.getDescriptionPt()))
                        .collectibleType(item.getCollectibleType())
                        .rarity(item.getRarity())
                        .acquisitionSource(item.getAcquisitionSource())
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, item.getCoverAssetId()))
                        .iconImageUrl(localizedContentSupport.resolveAssetUrl(assets, item.getIconAssetId()))
                        .animationUrl(localizedContentSupport.resolveAssetUrl(assets, item.getAnimationAssetId()))
                        .popupPresetCode(item.getPopupPresetCode())
                        .popupConfigJson(item.getPopupConfigJson())
                        .displayPresetCode(item.getDisplayPresetCode())
                        .displayConfigJson(item.getDisplayConfigJson())
                        .triggerPresetCode(item.getTriggerPresetCode())
                        .triggerConfigJson(item.getTriggerConfigJson())
                        .exampleContent(localizedContentSupport.resolveText(localeHint, item.getExampleContentZh(), item.getExampleContentEn(), item.getExampleContentZht(), item.getExampleContentPt()))
                        .relatedStorylines(toStorylineBindings(storylineBindings.get(item.getId()), storyLinesById, localeHint))
                        .relatedCities(toCityBindings(cityBindings.get(item.getId()), citiesById, null, localeHint))
                        .relatedSubMaps(toSubMapBindings(subMapBindings.get(item.getId()), subMapsById, localeHint))
                        .relatedIndoorBuildings(toIndoorBuildingBindings(indoorBuildingBindings.get(item.getId()), indoorBuildingsById, localeHint))
                        .relatedIndoorFloors(toIndoorFloorBindings(indoorFloorBindings.get(item.getId()), indoorFloorsById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindings.get(item.getId()), assets))
                        .sortOrder(item.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<BadgeResponse> listBadges(String localeHint) {
        List<Badge> badges = catalogFoundationService.listPublishedBadges();
        if (badges.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> badgeIds = badges.stream().map(Badge::getId).toList();
        Map<Long, StoryLine> storyLinesById = catalogFoundationService.listPublishedStoryLines().stream()
                .collect(Collectors.toMap(StoryLine::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, List<ContentRelationLink>> storylineBindings = groupRelationLinks("badge", badgeIds, "storyline_binding", "storyline");
        Map<Long, List<ContentRelationLink>> cityBindings = groupRelationLinks("badge", badgeIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindings = groupRelationLinks("badge", badgeIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> indoorBuildingBindings = groupRelationLinks("badge", badgeIds, "indoor_building_binding", "indoor_building");
        Map<Long, List<ContentRelationLink>> indoorFloorBindings = groupRelationLinks("badge", badgeIds, "indoor_floor_binding", "indoor_floor");
        Map<Long, List<ContentRelationLink>> attachmentBindings = groupRelationLinks("badge", badgeIds, "attachment_asset", "asset");
        Map<Long, IndoorBuilding> indoorBuildingsById = catalogFoundationService.getPublishedIndoorBuildingsByIds(flattenTargetIds(indoorBuildingBindings));
        Map<Long, IndoorFloor> indoorFloorsById = catalogFoundationService.getPublishedIndoorFloorsByIds(flattenTargetIds(indoorFloorBindings));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(Stream.concat(
                        badges.stream()
                                .flatMap(item -> nonNullIds(item.getCoverAssetId(), item.getIconAssetId(), item.getAnimationAssetId()).stream()),
                        attachmentBindings.values().stream()
                                .flatMap(List::stream)
                                .map(ContentRelationLink::getTargetId))
                .filter(Objects::nonNull)
                .toList());
        return badges.stream()
                .map(item -> BadgeResponse.builder()
                        .id(item.getId())
                        .code(item.getBadgeCode())
                        .name(localizedContentSupport.resolveText(localeHint, item.getNameZh(), item.getNameEn(), item.getNameZht(), item.getNamePt()))
                        .description(localizedContentSupport.resolveText(localeHint, item.getDescriptionZh(), item.getDescriptionEn(), item.getDescriptionZht(), item.getDescriptionPt()))
                        .badgeType(item.getBadgeType())
                        .rarity(item.getRarity())
                        .hidden(safeInt(item.getIsHidden()) > 0)
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, item.getCoverAssetId()))
                        .iconImageUrl(localizedContentSupport.resolveAssetUrl(assets, item.getIconAssetId()))
                        .animationUrl(localizedContentSupport.resolveAssetUrl(assets, item.getAnimationAssetId()))
                        .popupPresetCode(item.getPopupPresetCode())
                        .popupConfigJson(item.getPopupConfigJson())
                        .displayPresetCode(item.getDisplayPresetCode())
                        .displayConfigJson(item.getDisplayConfigJson())
                        .triggerPresetCode(item.getTriggerPresetCode())
                        .triggerConfigJson(item.getTriggerConfigJson())
                        .exampleContent(localizedContentSupport.resolveText(localeHint, item.getExampleContentZh(), item.getExampleContentEn(), item.getExampleContentZht(), item.getExampleContentPt()))
                        .relatedStorylines(toStorylineBindings(storylineBindings.get(item.getId()), storyLinesById, localeHint))
                        .relatedCities(toCityBindings(cityBindings.get(item.getId()), citiesById, null, localeHint))
                        .relatedSubMaps(toSubMapBindings(subMapBindings.get(item.getId()), subMapsById, localeHint))
                        .relatedIndoorBuildings(toIndoorBuildingBindings(indoorBuildingBindings.get(item.getId()), indoorBuildingsById, localeHint))
                        .relatedIndoorFloors(toIndoorFloorBindings(indoorFloorBindings.get(item.getId()), indoorFloorsById, localeHint))
                        .attachmentAssetUrls(toAssetUrls(attachmentBindings.get(item.getId()), assets))
                        .build())
                .toList();
    }

    @Override
    public List<StampResponse> listStamps(String localeHint) {
        List<Stamp> stamps = catalogFoundationService.listPublishedStamps();
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(stamps.stream()
                .map(Stamp::getIconAssetId)
                .filter(Objects::nonNull)
                .toList());
        return stamps.stream()
                .map(stamp -> StampResponse.builder()
                        .id(stamp.getId())
                        .code(stamp.getCode())
                        .name(localizedContentSupport.resolveText(localeHint, stamp.getNameZh(), stamp.getNameEn(), stamp.getNameZht(), stamp.getNamePt()))
                        .description(localizedContentSupport.resolveText(localeHint, stamp.getDescriptionZh(), stamp.getDescriptionEn(), stamp.getDescriptionZht(), stamp.getDescriptionPt()))
                        .stampType(stamp.getStampType())
                        .rarity(stamp.getRarity())
                        .iconImageUrl(localizedContentSupport.resolveAssetUrl(assets, stamp.getIconAssetId()))
                        .relatedPoiId(stamp.getRelatedPoiId())
                        .relatedStorylineId(stamp.getRelatedStorylineId())
                        .sortOrder(stamp.getSortOrder())
                        .build())
                .toList();
    }

    @Override
    public List<NotificationResponse> listNotifications(String localeHint) {
        List<Notification> notifications = catalogFoundationService.listPublishedNotifications();
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(notifications.stream()
                .map(Notification::getCoverAssetId)
                .filter(Objects::nonNull)
                .toList());
        return notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .code(notification.getCode())
                        .title(localizedContentSupport.resolveText(localeHint, notification.getTitleZh(), notification.getTitleEn(), notification.getTitleZht(), notification.getTitlePt()))
                        .content(localizedContentSupport.resolveText(localeHint, notification.getContentZh(), notification.getContentEn(), notification.getContentZht(), notification.getContentPt()))
                        .notificationType(notification.getNotificationType())
                        .targetScope(notification.getTargetScope())
                        .actionUrl(notification.getActionUrl())
                        .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, notification.getCoverAssetId()))
                        .sortOrder(notification.getSortOrder())
                        .publishedAt(notification.getPublishStartAt() != null ? notification.getPublishStartAt() : notification.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<DiscoverCardResponse> listDiscoverCards(String localeHint) {
        RuntimeGroupResponse runtimeGroup = runtimeSettingsService.getRuntimeSettingsByGroup("discover", localeHint);
        List<DiscoverCardResponse> curatedCards = localizedContentSupport.parseListOfMaps(runtimeGroup.getSettings().get("curated_cards")).stream()
                .map(this::toConfiguredDiscoverCard)
                .filter(Objects::nonNull)
                .toList();
        if (!curatedCards.isEmpty()) {
            return curatedCards;
        }

        List<String> preferredTypes = localizedContentSupport.orderedDistinct(localizedContentSupport.parseListOfMaps(runtimeGroup.getSettings().get("featured_cards")).stream()
                .map(item -> Objects.toString(item.get("cardType"), ""))
                .toList());
        if (preferredTypes.isEmpty()) {
            preferredTypes = List.of("activity", "merchant", "checkin");
        }

        Map<String, DiscoverCardResponse> cardsByType = new LinkedHashMap<>();
        cardsByType.put("activity", buildActivityCard(localeHint));
        cardsByType.put("merchant", buildMerchantCard(localeHint));
        cardsByType.put("checkin", buildCheckinCard(localeHint));

        List<DiscoverCardResponse> ordered = new ArrayList<>();
        for (String type : preferredTypes) {
            DiscoverCardResponse card = cardsByType.get(type);
            if (card != null) {
                ordered.add(card);
            }
        }
        cardsByType.values().stream()
                .filter(Objects::nonNull)
                .filter(card -> ordered.stream().noneMatch(existing -> existing.getId().equals(card.getId())))
                .forEach(ordered::add);
        return ordered;
    }

    private List<StoryLineResponse> buildStorylineResponses(Collection<StoryLine> storyLines, String localeHint) {
        if (storyLines == null || storyLines.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> storylineIds = storyLines.stream()
                .map(StoryLine::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, SubMap> subMapsById = catalogFoundationService.listPublishedSubMaps(null).stream()
                .collect(Collectors.toMap(SubMap::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        List<StoryChapter> chapters = catalogFoundationService.listPublishedStoryChapters(storylineIds);
        Map<Long, List<StoryChapter>> chaptersByStoryline = chapters.stream()
                .collect(Collectors.groupingBy(StoryChapter::getStorylineId, LinkedHashMap::new, Collectors.toList()));

        Map<Long, List<ContentRelationLink>> cityBindingLinks = groupRelationLinks("storyline", storylineIds, "city_binding", "city");
        Map<Long, List<ContentRelationLink>> subMapBindingLinks = groupRelationLinks("storyline", storylineIds, "sub_map_binding", "sub_map");
        Map<Long, List<ContentRelationLink>> attachmentLinksByStoryline = groupRelationLinks("storyline", storylineIds, "attachment_asset", "asset");

        List<Long> chapterIds = chapters.stream()
                .map(StoryChapter::getId)
                .filter(Objects::nonNull)
                .toList();
        List<StoryChapterBlockLink> chapterBlockLinks = catalogFoundationService.listPublishedStoryChapterBlockLinks(chapterIds);
        Map<Long, List<StoryChapterBlockLink>> chapterBlockLinksByChapter = chapterBlockLinks.stream()
                .collect(Collectors.groupingBy(StoryChapterBlockLink::getChapterId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, StoryContentBlock> contentBlocksById = catalogFoundationService.getPublishedStoryContentBlocksByIds(
                chapterBlockLinks.stream()
                        .map(StoryChapterBlockLink::getBlockId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList());
        Map<Long, List<ContentAssetLink>> blockAssetLinksByBlock = catalogFoundationService.listPublishedContentAssetLinks(
                        "story_content_block",
                        contentBlocksById.keySet())
                .stream()
                .collect(Collectors.groupingBy(ContentAssetLink::getEntityId, LinkedHashMap::new, Collectors.toList()));

        LinkedHashSet<Long> assetIds = new LinkedHashSet<>();
        storyLines.forEach(storyLine -> assetIds.addAll(nonNullIds(storyLine.getCoverAssetId(), storyLine.getBannerAssetId())));
        attachmentLinksByStoryline.values().forEach(links -> links.stream()
                .map(ContentRelationLink::getTargetId)
                .filter(Objects::nonNull)
                .forEach(assetIds::add));
        chapters.forEach(chapter -> assetIds.addAll(nonNullIds(chapter.getMediaAssetId())));
        contentBlocksById.values().forEach(block -> assetIds.addAll(nonNullIds(block.getPrimaryAssetId())));
        blockAssetLinksByBlock.values().forEach(links -> links.stream()
                .map(ContentAssetLink::getAssetId)
                .filter(Objects::nonNull)
                .forEach(assetIds::add));
        Map<Long, ContentAsset> assets = getPublishedAssetsWithFallbacks(assetIds);

        return storyLines.stream()
                .sorted(Comparator.comparing(StoryLine::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(StoryLine::getId))
                .map(storyLine -> {
                    List<CatalogRelationBindingResponse> cityBindings = toCityBindings(
                            cityBindingLinks.get(storyLine.getId()),
                            citiesById,
                            storyLine.getCityId(),
                            localeHint);
                    List<CatalogRelationBindingResponse> subMapBindings = toSubMapBindings(
                            subMapBindingLinks.get(storyLine.getId()),
                            subMapsById,
                            localeHint);
                    CatalogRelationBindingResponse primaryCityBinding = cityBindings.isEmpty() ? null : cityBindings.get(0);
                    List<StoryChapterResponse> chapterResponses = chaptersByStoryline
                            .getOrDefault(storyLine.getId(), Collections.emptyList())
                            .stream()
                            .map(chapter -> toStoryChapterResponse(
                                    chapter,
                                    chapterBlockLinksByChapter,
                                    contentBlocksById,
                                    blockAssetLinksByBlock,
                                    assets,
                                    localeHint))
                            .toList();
                    return StoryLineResponse.builder()
                            .id(storyLine.getId())
                            .cityId(primaryCityBinding == null ? storyLine.getCityId() : primaryCityBinding.getId())
                            .cityCode(primaryCityBinding == null ? "" : primaryCityBinding.getCode())
                            .cityBindings(cityBindings)
                            .subMapBindings(subMapBindings)
                            .code(storyLine.getCode())
                            .name(localizedContentSupport.resolveText(localeHint, storyLine.getNameZh(), storyLine.getNameEn(), storyLine.getNameZht(), storyLine.getNamePt()))
                            .nameEn(localizedContentSupport.firstNonBlank(storyLine.getNameEn(), storyLine.getNamePt(), storyLine.getNameZh(), storyLine.getNameZht()))
                            .description(localizedContentSupport.resolveText(localeHint, storyLine.getDescriptionZh(), storyLine.getDescriptionEn(), storyLine.getDescriptionZht(), storyLine.getDescriptionPt()))
                            .estimatedMinutes(storyLine.getEstimatedMinutes())
                            .difficulty(storyLine.getDifficulty())
                            .rewardBadge(localizedContentSupport.resolveText(localeHint, storyLine.getRewardBadgeZh(), storyLine.getRewardBadgeEn(), storyLine.getRewardBadgeZht(), storyLine.getRewardBadgePt()))
                            .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, storyLine.getCoverAssetId()))
                            .bannerImageUrl(localizedContentSupport.resolveAssetUrl(assets, storyLine.getBannerAssetId()))
                            .attachmentAssets(toStoryMediaAssetsFromRelationLinks(attachmentLinksByStoryline.get(storyLine.getId()), assets))
                            .totalChapters(chapterResponses.size())
                            .sortOrder(storyLine.getSortOrder())
                            .chapters(chapterResponses)
                            .build();
                })
                .toList();
    }

    private StoryChapterResponse toStoryChapterResponse(
            StoryChapter chapter,
            Map<Long, List<StoryChapterBlockLink>> chapterBlockLinksByChapter,
            Map<Long, StoryContentBlock> contentBlocksById,
            Map<Long, List<ContentAssetLink>> blockAssetLinksByBlock,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        StoryMediaAssetResponse primaryMediaAsset = toStoryMediaAssetResponse(chapter.getMediaAssetId(), assets);
        return StoryChapterResponse.builder()
                .id(chapter.getId())
                .chapterOrder(chapter.getChapterOrder())
                .title(localizedContentSupport.resolveText(localeHint, chapter.getTitleZh(), chapter.getTitleEn(), chapter.getTitleZht(), chapter.getTitlePt()))
                .summary(localizedContentSupport.resolveText(localeHint, chapter.getSummaryZh(), chapter.getSummaryEn(), chapter.getSummaryZht(), chapter.getSummaryPt()))
                .detail(localizedContentSupport.resolveText(localeHint, chapter.getDetailZh(), chapter.getDetailEn(), chapter.getDetailZht(), chapter.getDetailPt()))
                .achievement(localizedContentSupport.resolveText(localeHint, chapter.getAchievementZh(), chapter.getAchievementEn(), chapter.getAchievementZht(), chapter.getAchievementPt()))
                .collectible(localizedContentSupport.resolveText(localeHint, chapter.getCollectibleZh(), chapter.getCollectibleEn(), chapter.getCollectibleZht(), chapter.getCollectiblePt()))
                .locationName(localizedContentSupport.resolveText(localeHint, chapter.getLocationNameZh(), chapter.getLocationNameEn(), chapter.getLocationNameZht(), chapter.getLocationNamePt()))
                .anchorType(chapter.getAnchorType())
                .anchorTargetId(chapter.getAnchorTargetId())
                .anchorTargetCode(chapter.getAnchorTargetCode())
                .unlockType(chapter.getUnlockType())
                .mediaUrl(primaryMediaAsset == null ? null : primaryMediaAsset.getUrl())
                .experienceFlowId(chapter.getExperienceFlowId())
                .overridePolicy(readObjectJson(chapter.getOverridePolicyJson()))
                .storyModeConfig(readObjectJson(chapter.getStoryModeConfigJson()))
                .primaryMediaUrl(primaryMediaAsset == null ? null : primaryMediaAsset.getUrl())
                .primaryMediaAsset(primaryMediaAsset)
                .unlock(buildUnlockResponse(chapter))
                .prerequisite(buildConditionResponse(chapter.getPrerequisiteJson(), null))
                .completion(buildConditionResponse(chapter.getCompletionJson(), null))
                .effect(buildEffectResponse(chapter.getRewardJson()))
                .contentBlocks(buildChapterContentBlocks(chapter, chapterBlockLinksByChapter, contentBlocksById, blockAssetLinksByBlock, assets, localeHint))
                .prerequisiteJson(chapter.getPrerequisiteJson())
                .completionJson(chapter.getCompletionJson())
                .rewardJson(chapter.getRewardJson())
                .sortOrder(chapter.getSortOrder())
                .build();
    }

    private List<StoryContentBlockResponse> buildChapterContentBlocks(
            StoryChapter chapter,
            Map<Long, List<StoryChapterBlockLink>> chapterBlockLinksByChapter,
            Map<Long, StoryContentBlock> contentBlocksById,
            Map<Long, List<ContentAssetLink>> blockAssetLinksByBlock,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        List<StoryChapterBlockLink> links = chapterBlockLinksByChapter.getOrDefault(chapter.getId(), Collections.emptyList());
        if (!links.isEmpty()) {
            return links.stream()
                    .map(link -> toStoryContentBlockResponse(link, contentBlocksById.get(link.getBlockId()), blockAssetLinksByBlock, assets, localeHint))
                    .filter(Objects::nonNull)
                    .toList();
        }

        StoryMediaAssetResponse primaryMediaAsset = toStoryMediaAssetResponse(chapter.getMediaAssetId(), assets);
        if (primaryMediaAsset == null) {
            return Collections.emptyList();
        }
        return List.of(StoryContentBlockResponse.builder()
                .id(chapter.getId() == null ? null : -chapter.getId())
                .code("legacy-media-" + chapter.getId())
                .blockType(inferLegacyBlockType(primaryMediaAsset))
                .title(localizedContentSupport.resolveText(localeHint, chapter.getTitleZh(), chapter.getTitleEn(), chapter.getTitleZht(), chapter.getTitlePt()))
                .summary(localizedContentSupport.resolveText(localeHint, chapter.getSummaryZh(), chapter.getSummaryEn(), chapter.getSummaryZht(), chapter.getSummaryPt()))
                .body(localizedContentSupport.resolveText(localeHint, chapter.getDetailZh(), chapter.getDetailEn(), chapter.getDetailZht(), chapter.getDetailPt()))
                .stylePreset("chapter-legacy")
                .displayMode("default")
                .sortOrder(0)
                .primaryAsset(primaryMediaAsset)
                .attachmentAssets(Collections.emptyList())
                .build());
    }

    private StoryContentBlockResponse toStoryContentBlockResponse(
            StoryChapterBlockLink link,
            StoryContentBlock block,
            Map<Long, List<ContentAssetLink>> blockAssetLinksByBlock,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        if (link == null || block == null) {
            return null;
        }
        String defaultTitle = localizedContentSupport.resolveText(localeHint, block.getTitleZh(), block.getTitleEn(), block.getTitleZht(), block.getTitlePt());
        String defaultSummary = localizedContentSupport.resolveText(localeHint, block.getSummaryZh(), block.getSummaryEn(), block.getSummaryZht(), block.getSummaryPt());
        String defaultBody = localizedContentSupport.resolveText(localeHint, block.getBodyZh(), block.getBodyEn(), block.getBodyZht(), block.getBodyPt());
        return StoryContentBlockResponse.builder()
                .id(block.getId())
                .code(block.getCode())
                .blockType(block.getBlockType())
                .title(resolveLocalizedOverride(link.getOverrideTitleJson(), localeHint, defaultTitle))
                .summary(resolveLocalizedOverride(link.getOverrideSummaryJson(), localeHint, defaultSummary))
                .body(resolveLocalizedOverride(link.getOverrideBodyJson(), localeHint, defaultBody))
                .stylePreset(block.getStylePreset())
                .displayMode(block.getDisplayMode())
                .visibilityJson(block.getVisibilityJson())
                .displayConditionJson(link.getDisplayConditionJson())
                .configJson(localizedContentSupport.firstNonBlank(link.getOverrideConfigJson(), block.getConfigJson()))
                .sortOrder(link.getSortOrder() == null ? block.getSortOrder() : link.getSortOrder())
                .primaryAsset(toStoryMediaAssetResponse(block.getPrimaryAssetId(), assets))
                .attachmentAssets(toStoryMediaAssetsFromAssetLinks(blockAssetLinksByBlock.get(block.getId()), assets))
                .build();
    }

    private StoryChapterUnlockResponse buildUnlockResponse(StoryChapter chapter) {
        Map<String, Object> config = readObjectJson(chapter.getUnlockParamJson());
        return StoryChapterUnlockResponse.builder()
                .type(localizedContentSupport.firstNonBlank(chapter.getUnlockType(), resolveStructuredType(config, null), "sequence"))
                .config(config)
                .rawJson(chapter.getUnlockParamJson())
                .build();
    }

    private StoryChapterConditionResponse buildConditionResponse(String rawJson, String fallbackType) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        Map<String, Object> config = readObjectJson(rawJson);
        return StoryChapterConditionResponse.builder()
                .type(resolveStructuredType(config, fallbackType))
                .config(config)
                .rawJson(rawJson)
                .build();
    }

    private StoryChapterEffectResponse buildEffectResponse(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        Map<String, Object> config = readObjectJson(rawJson);
        return StoryChapterEffectResponse.builder()
                .type(resolveStructuredType(config, "custom"))
                .config(config)
                .rawJson(rawJson)
                .build();
    }

    private Map<Long, ContentAsset> getPublishedAssetsWithFallbacks(Collection<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashSet<Long> directIds = assetIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (directIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, ContentAsset> assets = new LinkedHashMap<>(catalogFoundationService.getPublishedAssetsByIds(directIds));
        LinkedHashSet<Long> fallbackIds = assets.values().stream()
                .flatMap(asset -> Stream.of(asset.getPosterAssetId(), asset.getFallbackAssetId()))
                .filter(Objects::nonNull)
                .filter(id -> !assets.containsKey(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!fallbackIds.isEmpty()) {
            assets.putAll(catalogFoundationService.getPublishedAssetsByIds(fallbackIds));
        }
        return assets;
    }

    private StoryMediaAssetResponse toStoryMediaAssetResponse(Long assetId, Map<Long, ContentAsset> assets) {
        return assetId == null ? null : toStoryMediaAssetResponse(assets.get(assetId), assets);
    }

    private StoryMediaAssetResponse toStoryMediaAssetResponse(ContentAsset asset, Map<Long, ContentAsset> assets) {
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
                .posterUrl(localizedContentSupport.resolveAssetUrl(assets, asset.getPosterAssetId()))
                .fallbackAssetId(asset.getFallbackAssetId())
                .fallbackUrl(localizedContentSupport.resolveAssetUrl(assets, asset.getFallbackAssetId()))
                .build();
    }

    private List<StoryMediaAssetResponse> toStoryMediaAssetsFromRelationLinks(
            List<ContentRelationLink> links,
            Map<Long, ContentAsset> assets
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(ContentRelationLink::getTargetId)
                .filter(Objects::nonNull)
                .map(assetId -> toStoryMediaAssetResponse(assetId, assets))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<StoryMediaAssetResponse> toStoryMediaAssetsFromAssetLinks(
            List<ContentAssetLink> links,
            Map<Long, ContentAsset> assets
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(ContentAssetLink::getAssetId)
                .filter(Objects::nonNull)
                .map(assetId -> toStoryMediaAssetResponse(assetId, assets))
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Object> readObjectJson(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return Collections.emptyMap();
        }
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            if (node == null || node.isNull()) {
                return Collections.emptyMap();
            }
            if (node.isObject()) {
                return objectMapper.convertValue(node, new TypeReference<LinkedHashMap<String, Object>>() {});
            }
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("value", objectMapper.convertValue(node, Object.class));
            return wrapper;
        } catch (Exception exception) {
            return Collections.emptyMap();
        }
    }

    private String resolveStructuredType(Map<String, Object> config, String fallbackType) {
        if (config != null) {
            Object rawType = config.get("type");
            if (rawType instanceof String type && StringUtils.hasText(type)) {
                return type;
            }
        }
        return StringUtils.hasText(fallbackType) ? fallbackType : "custom";
    }

    private String resolveLocalizedOverride(String rawJson, String localeHint, String fallbackValue) {
        Map<String, Object> config = readObjectJson(rawJson);
        if (config.isEmpty()) {
            return fallbackValue;
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        config.forEach((key, value) -> normalized.put(normalizeLocaleKey(key), value));
        for (String candidate : localeCandidates(localeHint)) {
            Object value = normalized.get(normalizeLocaleKey(candidate));
            if (value instanceof String text && StringUtils.hasText(text)) {
                return text;
            }
        }
        return config.values().stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(fallbackValue);
    }

    private List<String> localeCandidates(String localeHint) {
        if ("zh-Hans".equalsIgnoreCase(localeHint)) {
            return List.of("zh-Hans", "zhHans", "zh", "zh-CN", "zh_cn", "zhs", "zh-Hant", "zht", "en", "pt");
        }
        if ("en".equalsIgnoreCase(localeHint)) {
            return List.of("en", "en-US", "en_us", "zh-Hant", "zht", "zh", "pt");
        }
        if ("pt".equalsIgnoreCase(localeHint)) {
            return List.of("pt", "pt-PT", "pt_pt", "zh-Hant", "zht", "zh", "en");
        }
        return List.of("zh-Hant", "zhHant", "zht", "zh-TW", "zh_tw", "zh", "en", "pt");
    }

    private String normalizeLocaleKey(String value) {
        return value == null ? "" : value.replace("_", "").replace("-", "").toLowerCase();
    }

    private String inferLegacyBlockType(StoryMediaAssetResponse primaryAsset) {
        if (primaryAsset == null || !StringUtils.hasText(primaryAsset.getAssetKind())) {
            return "attachment_list";
        }
        return switch (primaryAsset.getAssetKind()) {
            case "image", "icon" -> "image";
            case "video" -> "video";
            case "audio" -> "audio";
            case "lottie" -> "lottie";
            default -> "attachment_list";
        };
    }

    private List<CatalogRelationBindingResponse> toCityBindings(
            List<ContentRelationLink> links,
            Map<Long, City> citiesById,
            Long legacyCityId,
            String localeHint
    ) {
        List<CatalogRelationBindingResponse> bindings = (links == null ? Collections.<ContentRelationLink>emptyList() : links).stream()
                .map(link -> {
                    City city = citiesById.get(link.getTargetId());
                    if (city == null) {
                        return null;
                    }
                      return CatalogRelationBindingResponse.builder()
                              .id(city.getId())
                              .code(city.getCode())
                              .name(localizedContentSupport.resolveText(localeHint, city.getNameZh(), city.getNameEn(), city.getNameZht(), city.getNamePt()))
                              .build();
                  })
                .filter(Objects::nonNull)
                .toList();
        if (!bindings.isEmpty()) {
            return bindings;
        }
        City legacyCity = legacyCityId == null ? null : citiesById.get(legacyCityId);
        if (legacyCity == null) {
            return Collections.emptyList();
        }
        return List.of(CatalogRelationBindingResponse.builder()
                .id(legacyCity.getId())
                .code(legacyCity.getCode())
                .name(localizedContentSupport.resolveText(localeHint, legacyCity.getNameZh(), legacyCity.getNameEn(), legacyCity.getNameZht(), legacyCity.getNamePt()))
                .build());
    }

    private List<CatalogRelationBindingResponse> toSubMapBindings(
            List<ContentRelationLink> links,
            Map<Long, SubMap> subMapsById,
            String localeHint
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(link -> {
                    SubMap subMap = subMapsById.get(link.getTargetId());
                    if (subMap == null) {
                        return null;
                    }
                      return CatalogRelationBindingResponse.builder()
                              .id(subMap.getId())
                              .code(subMap.getCode())
                              .name(localizedContentSupport.resolveText(localeHint, subMap.getNameZh(), subMap.getNameEn(), subMap.getNameZht(), subMap.getNamePt()))
                              .build();
                  })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CatalogRelationBindingResponse> toStorylineBindings(
            List<ContentRelationLink> links,
            Map<Long, StoryLine> storyLinesById,
            String localeHint
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(link -> {
                    StoryLine storyLine = storyLinesById.get(link.getTargetId());
                    if (storyLine == null) {
                        return null;
                    }
                      return CatalogRelationBindingResponse.builder()
                              .id(storyLine.getId())
                              .code(storyLine.getCode())
                              .name(localizedContentSupport.resolveText(localeHint, storyLine.getNameZh(), storyLine.getNameEn(), storyLine.getNameZht(), storyLine.getNamePt()))
                              .build();
                  })
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, List<ContentRelationLink>> groupRelationLinks(
            String ownerType,
            Collection<Long> ownerIds,
            String relationType,
            String targetType
    ) {
        if (!StringUtils.hasText(ownerType) || ownerIds == null || ownerIds.isEmpty() || !StringUtils.hasText(relationType)) {
            return Collections.emptyMap();
        }
        return catalogFoundationService.listRelationLinks(ownerType, ownerIds, relationType).stream()
                .filter(link -> !StringUtils.hasText(targetType) || targetType.equals(link.getTargetType()))
                .collect(Collectors.groupingBy(ContentRelationLink::getOwnerId, LinkedHashMap::new, Collectors.toList()));
    }

    private List<Long> flattenTargetIds(Map<Long, List<ContentRelationLink>> relationMap) {
        if (relationMap == null || relationMap.isEmpty()) {
            return Collections.emptyList();
        }
        return relationMap.values().stream()
                .flatMap(List::stream)
                .map(ContentRelationLink::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<CatalogRelationBindingResponse> toIndoorBuildingBindings(
            List<ContentRelationLink> links,
            Map<Long, IndoorBuilding> indoorBuildingsById,
            String localeHint
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(link -> {
                    IndoorBuilding building = indoorBuildingsById.get(link.getTargetId());
                    if (building == null) {
                        return null;
                    }
                    return CatalogRelationBindingResponse.builder()
                            .id(building.getId())
                            .code(building.getBuildingCode())
                            .name(localizedContentSupport.resolveText(
                                    localeHint,
                                    building.getNameZh(),
                                    building.getNameEn(),
                                    building.getNameZht(),
                                    building.getNamePt()))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<CatalogRelationBindingResponse> toIndoorFloorBindings(
            List<ContentRelationLink> links,
            Map<Long, IndoorFloor> indoorFloorsById,
            String localeHint
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(link -> {
                    IndoorFloor floor = indoorFloorsById.get(link.getTargetId());
                    if (floor == null) {
                        return null;
                    }
                    return CatalogRelationBindingResponse.builder()
                            .id(floor.getId())
                            .code(floor.getFloorCode())
                            .name(localizedContentSupport.resolveText(
                                    localeHint,
                                    floor.getFloorNameZh(),
                                    floor.getFloorNameEn(),
                                    floor.getFloorNameZht(),
                                    floor.getFloorNamePt()))
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> toAssetUrls(
            List<ContentRelationLink> links,
            Map<Long, ContentAsset> assets
    ) {
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }
        return links.stream()
                .map(ContentRelationLink::getTargetId)
                .filter(Objects::nonNull)
                .map(assetId -> localizedContentSupport.resolveAssetUrl(assets, assetId))
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<RewardRuleSummaryResponse> toRewardRuleSummaries(
            List<RewardRuleBinding> bindings,
            Map<Long, RewardRule> rewardRulesById,
            String localeHint
    ) {
        if (bindings == null || bindings.isEmpty()) {
            return Collections.emptyList();
        }
        return bindings.stream()
                .map(binding -> rewardRulesById.get(binding.getRuleId()))
                .filter(Objects::nonNull)
                .map(rule -> RewardRuleSummaryResponse.builder()
                        .id(rule.getId())
                        .code(rule.getCode())
                        .name(localizedContentSupport.resolveText(localeHint, rule.getNameZh(), null, rule.getNameZht(), null))
                        .ruleType(rule.getRuleType())
                        .summaryText(rule.getSummaryText())
                        .build())
                .toList();
    }

    private RewardPresentationResponse toRewardPresentationResponse(
            RewardPresentation presentation,
            Map<Long, List<RewardPresentationStep>> stepsByPresentationId,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        if (presentation == null) {
            return null;
        }
        List<RewardPresentationStep> steps = stepsByPresentationId.getOrDefault(presentation.getId(), Collections.emptyList());
        return RewardPresentationResponse.builder()
                .id(presentation.getId())
                .code(presentation.getCode())
                .name(localizedContentSupport.resolveText(localeHint, presentation.getNameZh(), null, presentation.getNameZht(), null))
                .presentationType(presentation.getPresentationType())
                .firstTimeOnly(presentation.getFirstTimeOnly())
                .skippable(presentation.getSkippable())
                .minimumDisplayMs(presentation.getMinimumDisplayMs())
                .interruptPolicy(presentation.getInterruptPolicy())
                .queuePolicy(presentation.getQueuePolicy())
                .priorityWeight(presentation.getPriorityWeight())
                .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, presentation.getCoverAssetId()))
                .voiceOverUrl(localizedContentSupport.resolveAssetUrl(assets, presentation.getVoiceOverAssetId()))
                .sfxUrl(localizedContentSupport.resolveAssetUrl(assets, presentation.getSfxAssetId()))
                .summaryText(presentation.getSummaryText())
                .configJson(presentation.getConfigJson())
                .steps(steps.stream()
                        .map(step -> RewardPresentationStepResponse.builder()
                                .stepType(step.getStepType())
                                .stepCode(step.getStepCode())
                                .titleText(step.getTitleText())
                                .assetUrl(localizedContentSupport.resolveAssetUrl(assets, step.getAssetId()))
                                .durationMs(step.getDurationMs())
                                .skippableOverride(step.getSkippableOverride())
                                .triggerSfxUrl(localizedContentSupport.resolveAssetUrl(assets, step.getTriggerSfxAssetId()))
                                .voiceOverUrl(localizedContentSupport.resolveAssetUrl(assets, step.getVoiceOverAssetId()))
                                .overlayConfigJson(step.getOverlayConfigJson())
                                .sortOrder(step.getSortOrder())
                                .build())
                        .toList())
                .build();
    }

    private List<Long> presentationAssetIds(
            Collection<RewardPresentation> presentations,
            Map<Long, List<RewardPresentationStep>> stepsByPresentationId
    ) {
        if ((presentations == null || presentations.isEmpty())
                && (stepsByPresentationId == null || stepsByPresentationId.isEmpty())) {
            return Collections.emptyList();
        }
        return Stream.concat(
                        presentations == null ? Stream.empty() : presentations.stream()
                                .flatMap(item -> Stream.of(item.getCoverAssetId(), item.getVoiceOverAssetId(), item.getSfxAssetId())),
                        stepsByPresentationId == null ? Stream.empty() : stepsByPresentationId.values().stream()
                                .flatMap(List::stream)
                                .flatMap(step -> Stream.of(step.getAssetId(), step.getTriggerSfxAssetId(), step.getVoiceOverAssetId())))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String firstBindingName(List<CatalogRelationBindingResponse> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return "";
        }
        return localizedContentSupport.firstNonBlank(bindings.get(0).getName(), bindings.get(0).getCode());
    }

    private CityResponse toCityResponse(City city, List<SubMap> subMaps, Map<Long, ContentAsset> assets, String localeHint) {
        return CityResponse.builder()
                .id(city.getId())
                .code(city.getCode())
                .name(localizedContentSupport.resolveText(localeHint, city.getNameZh(), city.getNameEn(), city.getNameZht(), city.getNamePt()))
                .subtitle(localizedContentSupport.resolveText(localeHint, city.getSubtitleZh(), city.getSubtitleEn(), city.getSubtitleZht(), city.getSubtitlePt()))
                .description(localizedContentSupport.resolveText(localeHint, city.getDescriptionZh(), city.getDescriptionEn(), city.getDescriptionZht(), city.getDescriptionPt()))
                .countryCode(city.getCountryCode())
                .sourceCoordinateSystem(city.getSourceCoordinateSystem())
                .sourceCenterLat(city.getSourceCenterLat())
                .sourceCenterLng(city.getSourceCenterLng())
                .centerLat(city.getCenterLat())
                .centerLng(city.getCenterLng())
                .defaultZoom(city.getDefaultZoom())
                .unlockType(city.getUnlockType())
                .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, city.getCoverAssetId()))
                .bannerImageUrl(localizedContentSupport.resolveAssetUrl(assets, city.getBannerAssetId()))
                .popupConfigJson(city.getPopupConfigJson())
                .displayConfigJson(city.getDisplayConfigJson())
                .subMaps(subMaps.stream()
                        .map(subMap -> toSubMapResponse(subMap, Map.of(city.getId(), city), assets, localeHint))
                        .toList())
                .sortOrder(city.getSortOrder())
                .build();
    }

    private SubMapResponse toSubMapResponse(
            SubMap subMap,
            Map<Long, City> citiesById,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        City city = citiesById.get(subMap.getCityId());
        return SubMapResponse.builder()
                .id(subMap.getId())
                .cityId(subMap.getCityId())
                .cityCode(city == null ? "" : city.getCode())
                .code(subMap.getCode())
                .name(localizedContentSupport.resolveText(localeHint, subMap.getNameZh(), subMap.getNameEn(), subMap.getNameZht(), subMap.getNamePt()))
                .subtitle(localizedContentSupport.resolveText(localeHint, subMap.getSubtitleZh(), subMap.getSubtitleEn(), subMap.getSubtitleZht(), subMap.getSubtitlePt()))
                .description(localizedContentSupport.resolveText(localeHint, subMap.getDescriptionZh(), subMap.getDescriptionEn(), subMap.getDescriptionZht(), subMap.getDescriptionPt()))
                .sourceCoordinateSystem(subMap.getSourceCoordinateSystem())
                .sourceCenterLat(subMap.getSourceCenterLat())
                .sourceCenterLng(subMap.getSourceCenterLng())
                .centerLat(subMap.getCenterLat())
                .centerLng(subMap.getCenterLng())
                .boundsJson(subMap.getBoundsJson())
                .popupConfigJson(subMap.getPopupConfigJson())
                .displayConfigJson(subMap.getDisplayConfigJson())
                .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, subMap.getCoverAssetId()))
                .sortOrder(subMap.getSortOrder())
                .publishedAt(subMap.getPublishedAt())
                .build();
    }

    private PoiResponse toPoiResponse(
            Poi poi,
            Map<Long, City> citiesById,
            Map<Long, SubMap> subMapsById,
            Map<Long, StoryLine> storyLinesById,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        City city = citiesById.get(poi.getCityId());
        SubMap subMap = subMapsById.get(poi.getSubMapId());
        StoryLine storyLine = storyLinesById.get(poi.getStorylineId());
        return PoiResponse.builder()
                .id(poi.getId())
                .cityId(poi.getCityId())
                .cityCode(city == null ? "" : city.getCode())
                .subMapId(poi.getSubMapId())
                .subMapCode(subMap == null ? "" : subMap.getCode())
                .subMapName(subMap == null ? "" : localizedContentSupport.resolveText(localeHint, subMap.getNameZh(), subMap.getNameEn(), subMap.getNameZht(), subMap.getNamePt()))
                .storylineId(poi.getStorylineId())
                .storylineCode(storyLine == null ? "" : storyLine.getCode())
                .storylineName(storyLine == null ? "" : localizedContentSupport.resolveText(localeHint, storyLine.getNameZh(), storyLine.getNameEn(), storyLine.getNameZht(), storyLine.getNamePt()))
                .code(poi.getCode())
                .name(localizedContentSupport.resolveText(localeHint, poi.getNameZh(), poi.getNameEn(), poi.getNameZht(), poi.getNamePt()))
                .subtitle(localizedContentSupport.resolveText(localeHint, poi.getSubtitleZh(), poi.getSubtitleEn(), poi.getSubtitleZht(), poi.getSubtitlePt()))
                .address(localizedContentSupport.resolveText(localeHint, poi.getAddressZh(), poi.getAddressEn(), poi.getAddressZht(), poi.getAddressPt()))
                .sourceCoordinateSystem(poi.getSourceCoordinateSystem())
                .sourceLatitude(poi.getSourceLatitude())
                .sourceLongitude(poi.getSourceLongitude())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .triggerRadius(poi.getTriggerRadius())
                .manualCheckinRadius(poi.getManualCheckinRadius())
                .staySeconds(poi.getStaySeconds())
                .categoryCode(poi.getCategoryCode())
                .difficulty(poi.getDifficulty())
                .district(localizedContentSupport.resolveText(localeHint, poi.getDistrictZh(), poi.getDistrictEn(), poi.getDistrictZht(), poi.getDistrictPt()))
                .description(localizedContentSupport.resolveText(localeHint, poi.getDescriptionZh(), poi.getDescriptionEn(), poi.getDescriptionZht(), poi.getDescriptionPt()))
                .introTitle(localizedContentSupport.resolveText(localeHint, poi.getIntroTitleZh(), poi.getIntroTitleEn(), poi.getIntroTitleZht(), poi.getIntroTitlePt()))
                .introSummary(localizedContentSupport.resolveText(localeHint, poi.getIntroSummaryZh(), poi.getIntroSummaryEn(), poi.getIntroSummaryZht(), poi.getIntroSummaryPt()))
                .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, poi.getCoverAssetId()))
                .mapIconUrl(localizedContentSupport.resolveAssetUrl(assets, poi.getMapIconAssetId()))
                .audioUrl(localizedContentSupport.resolveAssetUrl(assets, poi.getAudioAssetId()))
                .popupConfigJson(poi.getPopupConfigJson())
                .displayConfigJson(poi.getDisplayConfigJson())
                .sortOrder(poi.getSortOrder())
                .publishedAt(poi.getPublishedAt())
                .build();
    }

    private TipArticleResponse toTipArticleResponse(
            TipArticle article,
            Map<Long, City> citiesById,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        City city = citiesById.get(article.getCityId());
        String content = localizedContentSupport.resolveText(localeHint, article.getContentZh(), article.getContentEn(), article.getContentZht(), article.getContentPt());
        return TipArticleResponse.builder()
                .id(article.getId())
                .cityId(article.getCityId())
                .cityCode(city == null ? "" : city.getCode())
                .code(article.getCode())
                .categoryCode(article.getCategoryCode())
                .title(localizedContentSupport.resolveText(localeHint, article.getTitleZh(), article.getTitleEn(), article.getTitleZht(), article.getTitlePt()))
                .summary(localizedContentSupport.resolveText(localeHint, article.getSummaryZh(), article.getSummaryEn(), article.getSummaryZht(), article.getSummaryPt()))
                .contentParagraphs(localizedContentSupport.splitParagraphs(content))
                .authorDisplayName(article.getAuthorDisplayName())
                .locationName(localizedContentSupport.resolveText(localeHint, article.getLocationNameZh(), article.getLocationNameEn(), article.getLocationNameZht(), article.getLocationNamePt()))
                .tags(localizedContentSupport.parseStringList(article.getTagsJson()))
                .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, article.getCoverAssetId()))
                .sourceType(article.getSourceType())
                .sortOrder(article.getSortOrder())
                .publishedAt(article.getPublishedAt())
                .build();
    }

    private DiscoverCardResponse buildActivityCard(String localeHint) {
        List<ActivityResponse> activities = listActivities(localeHint);
        if (!activities.isEmpty()) {
            ActivityResponse activity = activities.get(0);
            String district = localizedContentSupport.firstNonBlank(
                    firstBindingName(activity.getSubMapBindings()),
                    firstBindingName(activity.getCityBindings()),
                    activity.getVenueName(),
                    activity.getAddress(),
                    "Macau");
            String subtitle = localizedContentSupport.firstNonBlank(
                    activity.getVenueName(),
                    activity.getActivityType(),
                    "Featured activity");
            String description = localizedContentSupport.firstNonBlank(
                    activity.getSummary(),
                    activity.getDescription());
            String tag = safeInt(activity.getIsPinned()) > 0
                    ? "Featured"
                    : localizedContentSupport.firstNonBlank(activity.getActivityType(), "Activity");
            return DiscoverCardResponse.builder()
                    .id("discover-activity-" + activity.getId())
                    .title(activity.getTitle())
                    .subtitle(subtitle)
                    .description(description)
                    .tag(tag)
                    .icon("event")
                    .type("activity")
                    .district(district)
                    .actionText("View activity")
                    .coverColor("#ffe2ef")
                    .sourceType("activity")
                    .sourceId(activity.getId())
                    .build();
        }

        List<TipArticleResponse> tips = listTipArticles(localeHint, null, null);
        if (!tips.isEmpty()) {
            TipArticleResponse tip = tips.get(0);
            return DiscoverCardResponse.builder()
                    .id("discover-activity-" + tip.getId())
                    .title(tip.getTitle())
                    .subtitle(localizedContentSupport.firstNonBlank(tip.getCategoryCode(), "活动推荐"))
                    .description(tip.getSummary())
                    .tag("编辑精选")
                    .icon("🌃")
                    .type("activity")
                    .district(localizedContentSupport.firstNonBlank(tip.getLocationName(), "澳门"))
                    .actionText("查看活动")
                    .coverColor("#ffe2ef")
                    .sourceType("tip")
                    .sourceId(tip.getId())
                    .build();
        }

        List<StoryLineResponse> storyLines = listStoryLines(localeHint);
        if (!storyLines.isEmpty()) {
            StoryLineResponse storyLine = storyLines.get(0);
            return DiscoverCardResponse.builder()
                    .id("discover-activity-story-" + storyLine.getId())
                    .title(storyLine.getName())
                    .subtitle("故事推荐")
                    .description(storyLine.getDescription())
                    .tag("今日推荐")
                    .icon("🌃")
                    .type("activity")
                    .district("澳门")
                    .actionText("查看活动")
                    .coverColor("#ffe2ef")
                    .sourceType("storyline")
                    .sourceId(storyLine.getId())
                    .build();
        }

        return null;
    }

    private DiscoverCardResponse buildMerchantCard(String localeHint) {
        List<RewardResponse> rewards = listRewards(localeHint);
        if (rewards.isEmpty()) {
            return null;
        }
        RewardResponse reward = rewards.get(0);
        return DiscoverCardResponse.builder()
                .id("discover-merchant-" + reward.getId())
                .title(reward.getName())
                .subtitle(localizedContentSupport.firstNonBlank(reward.getSubtitle(), "奖励兑换"))
                .description(localizedContentSupport.firstNonBlank(reward.getHighlight(), reward.getDescription()))
                .tag(reward.getAvailableInventory() > 0 ? "可兑换" : "敬请期待")
                .icon("🧁")
                .type("merchant")
                .district("澳门")
                .actionText("去兑换")
                .coverColor("#fff0c8")
                .sourceType("reward")
                .sourceId(reward.getId())
                .build();
    }

    private DiscoverCardResponse buildCheckinCard(String localeHint) {
        List<PoiResponse> pois = listPois(localeHint, null, null, null, null);
        if (pois.isEmpty()) {
            return null;
        }
        PoiResponse poi = pois.get(0);
        return DiscoverCardResponse.builder()
                .id("discover-checkin-" + poi.getId())
                .title(poi.getName())
                .subtitle(localizedContentSupport.firstNonBlank(poi.getSubtitle(), "热门足迹"))
                .description(poi.getDescription())
                .tag("热门打卡")
                .icon("🔥")
                .type("checkin")
                .district(localizedContentSupport.firstNonBlank(poi.getDistrict(), "澳门"))
                .actionText("跟着打卡")
                .coverColor("#dff3ff")
                .sourceType("poi")
                .sourceId(poi.getId())
                .build();
    }

    private DiscoverCardResponse toConfiguredDiscoverCard(Map<String, Object> item) {
        String type = Objects.toString(item.get("type"), "");
        if (!List.of("activity", "merchant", "checkin").contains(type)) {
            return null;
        }

        String id = localizedContentSupport.firstNonBlank(
                Objects.toString(item.get("id"), ""),
                "discover-" + type + "-" + Objects.toString(item.get("sourceId"), "runtime"));

        return DiscoverCardResponse.builder()
                .id(id)
                .title(Objects.toString(item.get("title"), ""))
                .subtitle(Objects.toString(item.get("subtitle"), ""))
                .description(Objects.toString(item.get("description"), ""))
                .tag(Objects.toString(item.get("tag"), ""))
                .icon(Objects.toString(item.get("icon"), ""))
                .type(type)
                .district(Objects.toString(item.get("district"), ""))
                .actionText(Objects.toString(item.get("actionText"), ""))
                .coverColor(Objects.toString(item.get("coverColor"), ""))
                .actionUrl(Objects.toString(item.get("actionUrl"), ""))
                .sourceType(Objects.toString(item.get("sourceType"), "runtime"))
                .sourceId(toLong(item.get("sourceId")))
                .build();
    }

    private List<Long> nonNullIds(Long... ids) {
        return Arrays.stream(ids).filter(Objects::nonNull).toList();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(Objects.toString(value, ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
