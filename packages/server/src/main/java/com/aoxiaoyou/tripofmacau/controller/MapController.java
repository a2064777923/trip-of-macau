package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.CityResponse;
import com.aoxiaoyou.tripofmacau.dto.response.SubMapResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Maps")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maps")
public class MapController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "List published large maps and sub maps")
    @GetMapping
    public ApiResponse<MapCatalogResponse> list(@RequestParam(required = false) String locale) {
        List<CityResponse> cities = publicCatalogService.listCities(locale);
        List<SubMapResponse> subMaps = publicCatalogService.listSubMaps(locale, null);
        return ApiResponse.success(MapCatalogResponse.builder()
                .cities(cities)
                .subMaps(subMaps)
                .defaultCityCode(cities.isEmpty() ? "" : cities.get(0).getCode())
                .build());
    }

    @Data
    @Builder
    public static class MapCatalogResponse {
        private List<CityResponse> cities;
        private List<SubMapResponse> subMaps;
        private String defaultCityCode;
    }
}
