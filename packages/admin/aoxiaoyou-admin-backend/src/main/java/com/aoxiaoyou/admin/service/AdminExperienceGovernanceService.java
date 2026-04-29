package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;

import java.util.List;

public interface AdminExperienceGovernanceService {

    PageResponse<AdminExperienceResponse.GovernanceItem> pageGovernanceItems(AdminExperienceRequest.GovernanceQuery query);

    AdminExperienceResponse.GovernanceDetail getGovernanceDetail(String itemKey);

    List<AdminExperienceResponse.GovernanceFinding> checkGovernanceConflicts(AdminExperienceRequest.GovernanceQuery query);
}
