package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.service.PoiService;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoiServiceImpl implements PoiService {

    private final PublicCatalogService publicCatalogService;

    @Override
    public List<PoiResponse> listPublished(String localeHint, String cityCode, String subMapCode, Long storylineId, String keyword) {
        return publicCatalogService.listPois(localeHint, cityCode, subMapCode, storylineId, keyword);
    }

    @Override
    public PoiResponse getDetail(Long poiId, String localeHint) {
        return publicCatalogService.getPoi(poiId, localeHint);
    }
}
