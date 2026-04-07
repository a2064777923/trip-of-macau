package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.request.TriggerLogCreateRequest;
import com.aoxiaoyou.tripofmacau.dto.response.TriggerLogResponse;
import com.aoxiaoyou.tripofmacau.service.TriggerLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "签到与触发")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trigger-logs")
public class TriggerLogController {

    private final TriggerLogService triggerLogService;

    @Operation(summary = "写入签到/触发日志")
    @PostMapping
    public ApiResponse<TriggerLogResponse> create(@Valid @RequestBody TriggerLogCreateRequest request) {
        return ApiResponse.success("触发日志写入成功", triggerLogService.create(request));
    }
}
