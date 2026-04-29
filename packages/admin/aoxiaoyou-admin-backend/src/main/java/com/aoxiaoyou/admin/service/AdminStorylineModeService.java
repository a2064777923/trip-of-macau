package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminStorylineModeRequest;
import com.aoxiaoyou.admin.dto.response.AdminStorylineModeResponse;

public interface AdminStorylineModeService {

    AdminStorylineModeResponse.Snapshot getSnapshot(Long storylineId);

    AdminStorylineModeResponse.Snapshot updateModeConfig(Long storylineId, AdminStorylineModeRequest.StoryModeConfigUpsert request);

    AdminStorylineModeResponse.Snapshot updateChapterAnchor(Long storylineId, Long chapterId, AdminStorylineModeRequest.ChapterAnchorUpsert request);

    AdminStorylineModeResponse.Snapshot updateChapterOverridePolicy(Long storylineId, Long chapterId, AdminStorylineModeRequest.ChapterOverridePolicyUpsert request);

    AdminStorylineModeResponse.OverrideRule createOverrideStep(Long storylineId, Long chapterId, AdminStorylineModeRequest.OverrideStepUpsert request);

    AdminStorylineModeResponse.OverrideRule updateOverrideStep(Long storylineId, Long chapterId, Long overrideId, AdminStorylineModeRequest.OverrideStepUpsert request);

    void deleteOverrideStep(Long storylineId, Long chapterId, Long overrideId);

    AdminStorylineModeResponse.RuntimePreview runtimePreview(Long storylineId);
}
