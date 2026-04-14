package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminCoordinatePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminSpatialMetadataSuggestionRequest;
import com.aoxiaoyou.admin.dto.response.AdminCoordinatePreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminSpatialMetadataSuggestionResponse;

public interface AdminSpatialMetadataService {
    AdminCoordinatePreviewResponse previewCoordinate(AdminCoordinatePreviewRequest request);
    AdminSpatialMetadataSuggestionResponse suggestMetadata(AdminSpatialMetadataSuggestionRequest request);
}
