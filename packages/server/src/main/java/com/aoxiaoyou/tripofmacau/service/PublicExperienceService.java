package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.request.ExperienceEventRequest;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceEventResponse;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceRuntimeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StorylineSessionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserExplorationResponse;

public interface PublicExperienceService {

    ExperienceRuntimeResponse.Flow getPoiExperience(Long poiId, String localeHint);

    ExperienceRuntimeResponse.StorylineRuntime getStorylineRuntime(Long storylineId, String localeHint);

    ExperienceEventResponse recordEvent(Long userId, ExperienceEventRequest request);

    StorylineSessionResponse startStorylineSession(Long userId, Long storylineId);

    StorylineSessionResponse exitStorylineSession(Long userId, Long storylineId, String sessionId);

    UserExplorationResponse getUserExploration(Long userId, String localeHint, String scopeType, Long scopeId);
}
