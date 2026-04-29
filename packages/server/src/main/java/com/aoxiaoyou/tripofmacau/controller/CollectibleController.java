package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.CollectibleResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Collectibles")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collectibles")
public class CollectibleController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "List published collectibles")
    @GetMapping
    public ApiResponse<List<CollectibleResponse>> list(@RequestParam(required = false) String locale) {
        return ApiResponse.success(publicCatalogService.listCollectibles(locale));
    }
}
