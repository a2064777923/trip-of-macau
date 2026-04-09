package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminCityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCityResponse;

public interface AdminCityService {
    PageResponse<AdminCityResponse> pageCities(long pageNum, long pageSize, String keyword, String status);
    AdminCityResponse getCityDetail(Long id);
    AdminCityResponse createCity(AdminCityUpsertRequest request);
    AdminCityResponse updateCity(Long id, AdminCityUpsertRequest request);
    AdminCityResponse publishCity(Long id);
}
