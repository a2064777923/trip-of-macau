package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;

public interface PoiService {

    PageResponse<PoiResponse> pagePois(long pageNum, long pageSize, Long storyLineId, String keyword);

    PoiResponse getDetail(Long poiId);
}
