package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Public Health")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/health")
public class HealthController {

    private final CatalogFoundationService catalogFoundationService;
    private final RuntimeSettingsService runtimeSettingsService;
    private final LocalizedContentSupport localizedContentSupport;

    @Operation(summary = "Service health with live catalog/runtime details")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        RuntimeGroupResponse discoverRuntime = runtimeSettingsService.getRuntimeSettingsByGroup("discover", "en");
        RuntimeGroupResponse travelRuntime = runtimeSettingsService.getRuntimeSettingsByGroup("travel", "en");

        Map<String, Object> publishedCatalog = new LinkedHashMap<>();
        publishedCatalog.put("cities", catalogFoundationService.listPublishedCities().size());
        publishedCatalog.put("storylines", catalogFoundationService.listPublishedStoryLines().size());
        publishedCatalog.put("storyChapters", catalogFoundationService.listPublishedStoryChapters(
                catalogFoundationService.listPublishedStoryLines().stream().map(item -> item.getId()).toList()).size());
        publishedCatalog.put("subMaps", catalogFoundationService.listPublishedSubMaps(null).size());
        publishedCatalog.put("pois", catalogFoundationService.listPublishedPois(null, null, null, null).size());
        publishedCatalog.put("tips", catalogFoundationService.listPublishedTipArticles(null, null).size());
        publishedCatalog.put("rewards", catalogFoundationService.listPublishedRewards().size());
        publishedCatalog.put("stamps", catalogFoundationService.listPublishedStamps().size());
        publishedCatalog.put("notifications", catalogFoundationService.listPublishedNotifications().size());

        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "trip-of-macau-server",
                "publishedCatalog", publishedCatalog,
                "discoverCuratedCardsConfigured", !localizedContentSupport.parseListOfMaps(discoverRuntime.getSettings().get("curated_cards")).isEmpty(),
                "travelRecommendationProfilesConfigured", !localizedContentSupport.parseListOfMaps(travelRuntime.getSettings().get("recommendation_profiles")).isEmpty()
        ));
    }
}
