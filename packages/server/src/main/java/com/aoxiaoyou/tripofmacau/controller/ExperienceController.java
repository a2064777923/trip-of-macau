package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.auth.JwtUtil;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.dto.request.ExperienceEventRequest;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceEventResponse;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceRuntimeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StorylineSessionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserExplorationResponse;
import com.aoxiaoyou.tripofmacau.service.PublicExperienceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Experience Runtime")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ExperienceController {

    private final PublicExperienceService publicExperienceService;
    private final JwtUtil jwtUtil;

    @GetMapping("/experience/poi/{poiId}")
    public ApiResponse<ExperienceRuntimeResponse.Flow> getPoiExperience(
            @PathVariable Long poiId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicExperienceService.getPoiExperience(poiId, locale));
    }

    @PostMapping("/experience/events")
    public ApiResponse<ExperienceEventResponse> recordEvent(
            @Valid @RequestBody ExperienceEventRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(publicExperienceService.recordEvent(requireUserId(httpServletRequest), request));
    }

    @GetMapping("/storylines/{storylineId}/runtime")
    public ApiResponse<ExperienceRuntimeResponse.StorylineRuntime> getStorylineRuntime(
            @PathVariable Long storylineId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicExperienceService.getStorylineRuntime(storylineId, locale));
    }

    @PostMapping("/storylines/{storylineId}/sessions/start")
    public ApiResponse<StorylineSessionResponse> startStorylineSession(
            @PathVariable Long storylineId,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(publicExperienceService.startStorylineSession(requireUserId(httpServletRequest), storylineId));
    }

    @PostMapping("/storylines/{storylineId}/sessions/{sessionId}/events")
    public ApiResponse<ExperienceEventResponse> recordStorylineSessionEvent(
            @PathVariable Long storylineId,
            @PathVariable String sessionId,
            @Valid @RequestBody ExperienceEventRequest request,
            HttpServletRequest httpServletRequest) {
        request.setStorylineSessionId(sessionId);
        return ApiResponse.success(publicExperienceService.recordEvent(requireUserId(httpServletRequest), request));
    }

    @PostMapping("/storylines/{storylineId}/sessions/{sessionId}/exit")
    public ApiResponse<StorylineSessionResponse> exitStorylineSession(
            @PathVariable Long storylineId,
            @PathVariable String sessionId,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(publicExperienceService.exitStorylineSession(requireUserId(httpServletRequest), storylineId, sessionId));
    }

    @GetMapping("/users/me/exploration")
    public ApiResponse<UserExplorationResponse> getUserExploration(
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) Long scopeId,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(publicExperienceService.getUserExploration(requireUserId(httpServletRequest), locale, scopeType, scopeId));
    }

    private Long requireUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(4010, "Unauthorized");
        }
        try {
            return jwtUtil.getUserId(authHeader.substring(7));
        } catch (Exception ex) {
            throw new BusinessException(4010, "Unauthorized");
        }
    }
}
