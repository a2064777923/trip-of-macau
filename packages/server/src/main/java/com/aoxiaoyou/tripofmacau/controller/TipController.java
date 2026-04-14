package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.TipArticleResponse;
import com.aoxiaoyou.tripofmacau.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Tips")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tips")
public class TipController {

    private final PublicCatalogService publicCatalogService;

    @Operation(summary = "List published tip articles")
    @GetMapping
    public ApiResponse<List<TipArticleResponse>> list(
            @RequestParam(required = false) String locale,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(publicCatalogService.listTipArticles(locale, categoryCode, keyword));
    }

    @Operation(summary = "Get tip article detail")
    @GetMapping("/{articleId}")
    public ApiResponse<TipArticleResponse> detail(
            @PathVariable Long articleId,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(publicCatalogService.getTipArticle(articleId, locale));
    }
}
