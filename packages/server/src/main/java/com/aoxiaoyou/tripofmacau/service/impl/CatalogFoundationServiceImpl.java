package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
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
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.CityMapper;
import com.aoxiaoyou.tripofmacau.mapper.NotificationMapper;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardMapper;
import com.aoxiaoyou.tripofmacau.mapper.StampMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryChapterMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryLineMapper;
import com.aoxiaoyou.tripofmacau.mapper.SubMapMapper;
import com.aoxiaoyou.tripofmacau.mapper.TipArticleMapper;
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
    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final TipArticleMapper tipArticleMapper;
    private final RewardMapper rewardMapper;
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
}
