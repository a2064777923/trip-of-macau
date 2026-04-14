package com.aoxiaoyou.tripofmacau.service;

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

    List<TipArticle> listPublishedTipArticles(String categoryCode, String keyword);

    Optional<TipArticle> getPublishedTipArticle(Long articleId);

    List<Reward> listPublishedRewards();

    List<Stamp> listPublishedStamps();

    List<Notification> listPublishedNotifications();

    Map<Long, ContentAsset> getPublishedAssetsByIds(Collection<Long> assetIds);
}
