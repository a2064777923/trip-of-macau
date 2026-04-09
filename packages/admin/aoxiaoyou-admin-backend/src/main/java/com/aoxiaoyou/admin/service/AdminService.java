package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.LoginRequest;
import com.aoxiaoyou.admin.dto.response.AdminAuthResponse;
import com.aoxiaoyou.admin.entity.SysAdmin;

public interface AdminService {

    AdminAuthResponse login(LoginRequest request);

    AdminAuthResponse refresh(String refreshToken);

    AdminAuthResponse me(Long adminId);

    SysAdmin getProfile(Long adminId);
}
