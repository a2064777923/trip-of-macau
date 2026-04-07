package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.dto.request.UserLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.response.UserProfileResponse;
import com.aoxiaoyou.tripofmacau.entity.User;
import com.aoxiaoyou.tripofmacau.mapper.UserMapper;
import com.aoxiaoyou.tripofmacau.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserProfileResponse login(UserLoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, request.getOpenId())
                .last("limit 1"));

        if (user == null) {
            user = buildNewUser(request);
            userMapper.insert(user);
        } else {
            boolean changed = false;
            if (StringUtils.hasText(request.getNickname())) {
                user.setNickname(request.getNickname());
                changed = true;
            }
            if (StringUtils.hasText(request.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl());
                changed = true;
            }
            if (StringUtils.hasText(request.getLanguagePreference())) {
                user.setLanguagePreference(request.getLanguagePreference());
                changed = true;
            }
            if (StringUtils.hasText(request.getInterfaceMode())) {
                user.setInterfaceMode(request.getInterfaceMode());
                changed = true;
            }
            if (changed) {
                userMapper.updateById(user);
            }
        }
        return toResponse(userMapper.selectById(user.getId()));
    }

    @Override
    public UserProfileResponse getByOpenId(String openId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenId, openId)
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException(4040, "用户不存在");
        }
        return toResponse(user);
    }

    @Override
    public UserProfileResponse getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(4040, "用户不存在");
        }
        return toResponse(user);
    }

    private User buildNewUser(UserLoginRequest request) {
        User user = new User();
        user.setOpenId(request.getOpenId());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "微信用户");
        user.setAvatarUrl(request.getAvatarUrl());
        user.setLanguagePreference(StringUtils.hasText(request.getLanguagePreference()) ? request.getLanguagePreference() : "zh_CN");
        user.setLevel(1);
        user.setTitle("探索新手");
        user.setTotalStamps(0);
        user.setInterfaceMode(StringUtils.hasText(request.getInterfaceMode()) ? request.getInterfaceMode() : "standard");
        user.setFontScale(BigDecimal.ONE);
        user.setHighContrast(Boolean.FALSE);
        user.setVoiceGuideEnabled(Boolean.FALSE);
        user.setSimplifiedMode(Boolean.FALSE);
        user.setDeleted(0);
        return user;
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .openId(user.getOpenId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .languagePreference(user.getLanguagePreference())
                .level(user.getLevel())
                .title(user.getTitle())
                .totalStamps(user.getTotalStamps())
                .interfaceMode(user.getInterfaceMode())
                .fontScale(user.getFontScale())
                .highContrast(user.getHighContrast())
                .voiceGuideEnabled(user.getVoiceGuideEnabled())
                .simplifiedMode(user.getSimplifiedMode())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
