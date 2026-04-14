package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.CityResponse;
import com.aoxiaoyou.tripofmacau.dto.response.DiscoverCardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.NotificationResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StampResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.dto.response.SubMapResponse;
import com.aoxiaoyou.tripofmacau.dto.response.TipArticleResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.City;
import com.aoxiaoyou.tripofmacau.entity.Notification;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.entity.Reward;
import com.aoxiaoyou.tripofmacau.entity.Stamp;
import com.aoxiaoyou.tripofmacau.entity.StoryChapter;
import com.aoxiaoyou.tripofmacau.entity.StoryLine;
import com.aoxiaoyou.tripofmacau.entity.SubMap;
import com.aoxiaoyou.tripofmacau.entity.TipArticle;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(rewards.stream()
                .map(Reward::getCoverAssetId)
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
                        .sortOrder(reward.getSortOrder())
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
        Map<Long, City> citiesById = catalogFoundationService.listPublishedCities().stream()
                .collect(Collectors.toMap(City::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        List<StoryChapter> chapters = catalogFoundationService.listPublishedStoryChapters(storyLines.stream()
                .map(StoryLine::getId)
                .toList());
        Map<Long, List<StoryChapter>> chaptersByStoryline = chapters.stream()
                .collect(Collectors.groupingBy(StoryChapter::getStorylineId, LinkedHashMap::new, Collectors.toList()));

        List<Long> assetIds = new ArrayList<>();
        storyLines.forEach(storyLine -> assetIds.addAll(nonNullIds(storyLine.getCoverAssetId(), storyLine.getBannerAssetId())));
        chapters.forEach(chapter -> assetIds.addAll(nonNullIds(chapter.getMediaAssetId())));
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(assetIds);

        return storyLines.stream()
                .sorted(Comparator.comparing(StoryLine::getSortOrder, Comparator.nullsLast(Integer::compareTo)).thenComparing(StoryLine::getId))
                .map(storyLine -> {
                    City city = citiesById.get(storyLine.getCityId());
                    List<StoryChapterResponse> chapterResponses = chaptersByStoryline.getOrDefault(storyLine.getId(), Collections.emptyList()).stream()
                            .map(chapter -> StoryChapterResponse.builder()
                                    .id(chapter.getId())
                                    .chapterOrder(chapter.getChapterOrder())
                                    .title(localizedContentSupport.resolveText(localeHint, chapter.getTitleZh(), chapter.getTitleEn(), chapter.getTitleZht(), chapter.getTitlePt()))
                                    .summary(localizedContentSupport.resolveText(localeHint, chapter.getSummaryZh(), chapter.getSummaryEn(), chapter.getSummaryZht(), chapter.getSummaryPt()))
                                    .detail(localizedContentSupport.resolveText(localeHint, chapter.getDetailZh(), chapter.getDetailEn(), chapter.getDetailZht(), chapter.getDetailPt()))
                                    .achievement(localizedContentSupport.resolveText(localeHint, chapter.getAchievementZh(), chapter.getAchievementEn(), chapter.getAchievementZht(), chapter.getAchievementPt()))
                                    .collectible(localizedContentSupport.resolveText(localeHint, chapter.getCollectibleZh(), chapter.getCollectibleEn(), chapter.getCollectibleZht(), chapter.getCollectiblePt()))
                                    .locationName(localizedContentSupport.resolveText(localeHint, chapter.getLocationNameZh(), chapter.getLocationNameEn(), chapter.getLocationNameZht(), chapter.getLocationNamePt()))
                                    .unlockType(chapter.getUnlockType())
                                    .mediaUrl(localizedContentSupport.resolveAssetUrl(assets, chapter.getMediaAssetId()))
                                    .sortOrder(chapter.getSortOrder())
                                    .build())
                            .toList();
                    return StoryLineResponse.builder()
                            .id(storyLine.getId())
                            .cityId(storyLine.getCityId())
                            .cityCode(city == null ? "" : city.getCode())
                            .code(storyLine.getCode())
                            .name(localizedContentSupport.resolveText(localeHint, storyLine.getNameZh(), storyLine.getNameEn(), storyLine.getNameZht(), storyLine.getNamePt()))
                            .nameEn(localizedContentSupport.firstNonBlank(storyLine.getNameEn(), storyLine.getNamePt(), storyLine.getNameZh(), storyLine.getNameZht()))
                            .description(localizedContentSupport.resolveText(localeHint, storyLine.getDescriptionZh(), storyLine.getDescriptionEn(), storyLine.getDescriptionZht(), storyLine.getDescriptionPt()))
                            .estimatedMinutes(storyLine.getEstimatedMinutes())
                            .difficulty(storyLine.getDifficulty())
                            .rewardBadge(localizedContentSupport.resolveText(localeHint, storyLine.getRewardBadgeZh(), storyLine.getRewardBadgeEn(), storyLine.getRewardBadgeZht(), storyLine.getRewardBadgePt()))
                            .coverImageUrl(localizedContentSupport.resolveAssetUrl(assets, storyLine.getCoverAssetId()))
                            .bannerImageUrl(localizedContentSupport.resolveAssetUrl(assets, storyLine.getBannerAssetId()))
                            .totalChapters(chapterResponses.size())
                            .sortOrder(storyLine.getSortOrder())
                            .chapters(chapterResponses)
                            .build();
                })
                .toList();
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
