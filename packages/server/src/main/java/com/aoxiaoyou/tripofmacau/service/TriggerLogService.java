package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.request.TriggerLogCreateRequest;
import com.aoxiaoyou.tripofmacau.dto.response.TriggerLogResponse;

public interface TriggerLogService {

    TriggerLogResponse create(TriggerLogCreateRequest request);
}
