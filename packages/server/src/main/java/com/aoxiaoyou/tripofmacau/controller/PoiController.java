package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "Public POIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pois")
public class PoiController {

    private final PoiService poiService;

    @Operation(summary = "List published POIs")
    @GetMapping
    public ApiResponse<List<PoiResponse>> list(
            @Parameter(description = "Locale hint such as zh-Hans / zh-Hant / en")
            @RequestParam(required = false) String locale,
            @Parameter(description = "Filter by city code")
            @RequestParam(required = false) String cityCode,
            @Parameter(description = "Filter by sub-map code")
            @RequestParam(required = false) String subMapCode,
            @Parameter(description = "Filter by storyline id")
            @RequestParam(required = false) Long storylineId,
            @Parameter(description = "Keyword search")
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(poiService.listPublished(locale, cityCode, subMapCode, storylineId, keyword));
    }

    @Operation(summary = "Get POI detail")
    @GetMapping("/{poiId}")
    public ApiResponse<PoiResponse> detail(
            @PathVariable Long poiId,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(poiService.getDetail(poiId, locale));
    }
}
