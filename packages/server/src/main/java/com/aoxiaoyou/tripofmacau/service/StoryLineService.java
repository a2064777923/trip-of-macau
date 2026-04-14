package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;

import java.util.List;

public interface StoryLineService {

    List<StoryLineResponse> listPublished(String localeHint);

    StoryLineResponse getDetail(Long storyLineId, String localeHint);
}
