package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.CityResponse;
import com.aoxiaoyou.tripofmacau.dto.response.DiscoverCardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.NotificationResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StampResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.dto.response.SubMapResponse;
import com.aoxiaoyou.tripofmacau.dto.response.TipArticleResponse;

import java.util.List;

public interface PublicCatalogService {

    List<CityResponse> listCities(String localeHint);

    List<SubMapResponse> listSubMaps(String localeHint, String cityCode);

    List<PoiResponse> listPois(String localeHint, String cityCode, String subMapCode, Long storylineId, String keyword);

    PoiResponse getPoi(Long poiId, String localeHint);

    List<StoryLineResponse> listStoryLines(String localeHint);

    StoryLineResponse getStoryLine(Long storyLineId, String localeHint);

    List<TipArticleResponse> listTipArticles(String localeHint, String categoryCode, String keyword);

    TipArticleResponse getTipArticle(Long articleId, String localeHint);

    List<RewardResponse> listRewards(String localeHint);

    List<StampResponse> listStamps(String localeHint);

    List<NotificationResponse> listNotifications(String localeHint);

    List<DiscoverCardResponse> listDiscoverCards(String localeHint);
}
