package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.response.IndoorBuildingResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorMarkerResponse;

import java.util.List;

public interface PublicIndoorService {

    IndoorBuildingResponse getBuilding(Long buildingId, String localeHint);

    IndoorBuildingResponse getBuildingByPoi(Long poiId, String localeHint);

    IndoorFloorResponse getFloor(Long floorId, String localeHint);

    List<IndoorMarkerResponse> getFloorMarkers(Long floorId, String localeHint);
}
