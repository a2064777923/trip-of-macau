package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.GameRewardResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Game Rewards")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-rewards")
public class GameRewardController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "List published game rewards")
    @GetMapping
    public ApiResponse<List<GameRewardResponse>> list(
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) Boolean honorsOnly
    ) {
        return ApiResponse.success(publicCatalogService.listGameRewards(locale, honorsOnly));
    }
}
