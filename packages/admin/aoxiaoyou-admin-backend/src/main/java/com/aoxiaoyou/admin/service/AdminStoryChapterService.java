package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryChapterUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterResponse;

import java.util.List;

public interface AdminStoryChapterService {

    PageResponse<AdminStoryChapterResponse> page(Long storylineId, long pageNum, long pageSize);

    List<AdminStoryChapterResponse> listByStoryline(Long storylineId);

    AdminStoryChapterResponse detail(Long storylineId, Long chapterId);

    AdminStoryChapterResponse create(AdminStoryChapterUpsertRequest.Upsert request);

    AdminStoryChapterResponse update(Long chapterId, AdminStoryChapterUpsertRequest.Upsert request);

    void delete(Long chapterId);
}
