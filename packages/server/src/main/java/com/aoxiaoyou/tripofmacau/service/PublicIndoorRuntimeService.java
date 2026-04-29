package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.request.IndoorRuntimeInteractionRequest;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeInteractionResponse;

public interface PublicIndoorRuntimeService {

    IndoorRuntimeFloorResponse getFloorRuntime(Long floorId, String localeHint);

    IndoorRuntimeInteractionResponse evaluateInteraction(IndoorRuntimeInteractionRequest request, String localeHint, Long userId);
}
