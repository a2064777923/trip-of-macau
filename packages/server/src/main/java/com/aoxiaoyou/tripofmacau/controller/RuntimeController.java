package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RuntimeGroupResponse;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Runtime")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/runtime")
public class RuntimeController {

    private final RuntimeSettingsService runtimeSettingsService;

    @Operation(summary = "Get published runtime settings by group")
    @GetMapping("/{group}")
    public ApiResponse<RuntimeGroupResponse> getGroup(
            @PathVariable String group,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(runtimeSettingsService.getRuntimeSettingsByGroup(group, locale));
    }
}
