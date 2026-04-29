package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardPresentationResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Reward Presentations")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reward-presentations")
public class RewardPresentationController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "Get published reward presentation")
    @GetMapping("/{presentationId}")
    public ApiResponse<RewardPresentationResponse> get(
            @PathVariable Long presentationId,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(publicCatalogService.getRewardPresentation(presentationId, locale));
    }
}
