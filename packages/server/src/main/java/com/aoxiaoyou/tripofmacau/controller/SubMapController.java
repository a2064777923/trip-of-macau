package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.SubMapResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Sub Maps")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sub-maps")
public class SubMapController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "List published sub-maps")
    @GetMapping
    public ApiResponse<List<SubMapResponse>> list(
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String cityCode
    ) {
        return ApiResponse.success(publicCatalogService.listSubMaps(locale, cityCode));
    }
}
