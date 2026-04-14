package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;

import java.util.List;

public interface PoiService {

    List<PoiResponse> listPublished(String localeHint, String cityCode, String subMapCode, Long storylineId, String keyword);

    PoiResponse getDetail(Long poiId, String localeHint);
}
