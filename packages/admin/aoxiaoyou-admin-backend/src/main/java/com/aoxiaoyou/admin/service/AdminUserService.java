package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminTestFlagRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserListItemResponse;

public interface AdminUserService {

    PageResponse<AdminUserListItemResponse> pageUsers(long pageNum, long pageSize, String keyword, Boolean isTestAccount);

    AdminUserDetailResponse getUserDetail(Long userId);

    AdminUserListItemResponse updateTestFlag(Long userId, AdminTestFlagRequest request, Long operatorId, String operatorName, String ip);
}
