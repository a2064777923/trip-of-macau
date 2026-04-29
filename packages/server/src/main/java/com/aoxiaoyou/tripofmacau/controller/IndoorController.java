package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.auth.JwtUtil;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.dto.request.IndoorRuntimeInteractionRequest;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorBuildingResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorMarkerResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeInteractionResponse;
import com.aoxiaoyou.tripofmacau.service.PublicIndoorRuntimeService;
import com.aoxiaoyou.tripofmacau.service.PublicIndoorService;
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

import java.util.List;

@Tag(name = "Public Indoor")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/indoor")
public class IndoorController {

    private final PublicIndoorService publicIndoorService;
    private final PublicIndoorRuntimeService publicIndoorRuntimeService;
    private final JwtUtil jwtUtil;

    @GetMapping("/buildings/{buildingId}")
    public ApiResponse<IndoorBuildingResponse> getBuilding(
            @PathVariable Long buildingId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicIndoorService.getBuilding(buildingId, locale));
    }

    @GetMapping("/buildings/by-poi/{poiId}")
    public ApiResponse<IndoorBuildingResponse> getBuildingByPoi(
            @PathVariable Long poiId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicIndoorService.getBuildingByPoi(poiId, locale));
    }

    @GetMapping("/floors/{floorId}")
    public ApiResponse<IndoorFloorResponse> getFloor(
            @PathVariable Long floorId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicIndoorService.getFloor(floorId, locale));
    }

    @GetMapping("/floors/{floorId}/markers")
    public ApiResponse<List<IndoorMarkerResponse>> getFloorMarkers(
            @PathVariable Long floorId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicIndoorService.getFloorMarkers(floorId, locale));
    }

    @GetMapping("/floors/{floorId}/runtime")
    public ApiResponse<IndoorRuntimeFloorResponse> getFloorRuntime(
            @PathVariable Long floorId,
            @RequestParam(required = false) String locale) {
        return ApiResponse.success(publicIndoorRuntimeService.getFloorRuntime(floorId, locale));
    }

    @PostMapping("/runtime/interactions")
    public ApiResponse<IndoorRuntimeInteractionResponse> evaluateInteraction(
            @Valid @RequestBody IndoorRuntimeInteractionRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(publicIndoorRuntimeService.evaluateInteraction(
                request,
                request.getLocale(),
                extractOptionalUserId(httpServletRequest)));
    }

    private Long extractOptionalUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtUtil.getUserId(authHeader.substring(7));
        } catch (Exception ex) {
            throw new BusinessException(4010, "Unauthorized");
        }
    }
}
