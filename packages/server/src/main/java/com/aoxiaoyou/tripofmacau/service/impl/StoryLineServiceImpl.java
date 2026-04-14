package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryLineServiceImpl implements StoryLineService {

    private final PublicCatalogService publicCatalogService;

    @Override
    public List<StoryLineResponse> listPublished(String localeHint) {
        return publicCatalogService.listStoryLines(localeHint);
    }

    @Override
    public StoryLineResponse getDetail(Long storyLineId, String localeHint) {
        return publicCatalogService.getStoryLine(storyLineId, localeHint);
    }
}
