package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminPoiExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiExperienceResponse;

public interface AdminPoiExperienceService {

    AdminPoiExperienceResponse.Snapshot getDefaultExperience(Long poiId);

    AdminPoiExperienceResponse.Snapshot upsertDefaultFlow(Long poiId, AdminPoiExperienceRequest.FlowUpsert request);

    AdminPoiExperienceResponse.Step createStep(Long poiId, AdminPoiExperienceRequest.StepStructuredUpsert request);

    AdminPoiExperienceResponse.Step updateStep(Long poiId, Long stepId, AdminPoiExperienceRequest.StepStructuredUpsert request);

    void deleteStep(Long poiId, Long stepId);

    AdminExperienceResponse.Template saveStepAsTemplate(Long poiId, Long stepId, AdminPoiExperienceRequest.SaveTemplateRequest request);
}
