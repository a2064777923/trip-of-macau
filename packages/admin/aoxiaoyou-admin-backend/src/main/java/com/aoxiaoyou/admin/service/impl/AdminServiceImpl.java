package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.LoginRequest;
import com.aoxiaoyou.admin.dto.response.AdminAuthResponse;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.entity.SysLoginLog;
import com.aoxiaoyou.admin.mapper.SysAdminMapper;
import com.aoxiaoyou.admin.mapper.SysLoginLogMapper;
import com.aoxiaoyou.admin.service.AdminService;
import com.aoxiaoyou.admin.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysAdminMapper adminMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public AdminAuthResponse login(LoginRequest request) {
        try {
            SysAdmin admin = adminMapper.selectOne(
                    new LambdaQueryWrapper<SysAdmin>()
                            .eq(SysAdmin::getUsername, request.getUsername())
                            .eq(SysAdmin::getStatus, "active")
                            .last("limit 1")
            );

            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setOpenid("");
            loginLog.setUsername(request.getUsername());
            loginLog.setIp(request.getIp());
            loginLog.setUserAgent(request.getUserAgent());

            if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                loginLog.setLoginStatus("failed");
                loginLog.setFailReason("用户名或密码错误");
                if (admin != null) {
                    loginLog.setAdminId(admin.getId());
                }
                loginLogMapper.insert(loginLog);
                throw new BusinessException(4010, "用户名或密码错误");
            }

            adminMapper.update(null,
                    new LambdaUpdateWrapper<SysAdmin>()
                            .eq(SysAdmin::getId, admin.getId())
                            .set(SysAdmin::getLastLoginAt, LocalDateTime.now())
                            .set(SysAdmin::getLastLoginIp, request.getIp())
            );

            loginLog.setAdminId(admin.getId());
            loginLog.setLoginStatus("success");
            loginLogMapper.insert(loginLog);

            return buildAuthResponse(adminMapper.selectById(admin.getId()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Admin login failed. username={}, ip={}, userAgent={}", request.getUsername(), request.getIp(), request.getUserAgent(), ex);
            throw ex;
        }
    }

    @Override
    public AdminAuthResponse refresh(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(4011, "refreshToken 无效");
        }
        Long adminId = jwtUtil.getUserId(refreshToken);
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(4041, "管理员不存在");
        }
        return buildAuthResponse(admin);
    }

    @Override
    public AdminAuthResponse me(Long adminId) {
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(4041, "管理员不存在");
        }
        return buildAuthResponse(admin);
    }

    @Override
    public SysAdmin getProfile(Long adminId) {
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        admin.setPassword(null);
        return admin;
    }

    private AdminAuthResponse buildAuthResponse(SysAdmin admin) {
        List<String> roles = List.of("SUPER_ADMIN");
        List<String> permissions = List.of(
                "dashboard:view",
                "poi:view", "poi:create", "poi:update", "poi:delete",
                "storyline:view", "storyline:create", "storyline:update",
                "user:view", "user:test-flag",
                "test-console:view", "test-console:operate"
        );

        String accessToken = jwtUtil.generateAccessToken(admin.getId(), admin.getUsername(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), admin.getUsername());
        return AdminAuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200L)
                .user(AdminAuthResponse.AdminCurrentUser.builder()
                        .userId(admin.getId())
                        .username(admin.getUsername())
                        .realName(admin.getNickname())
                        .email(admin.getEmail())
                        .roles(roles)
                        .permissions(permissions)
                        .lastLoginAt(admin.getLastLoginAt())
                        .build())
                .build();
    }
}

