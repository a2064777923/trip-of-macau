package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminExperienceRequest;
import com.aoxiaoyou.admin.dto.response.AdminExperienceResponse;

public interface AdminExperienceOrchestrationService {

    PageResponse<AdminExperienceResponse.Template> pageTemplates(long pageNum, long pageSize, String keyword, String templateType, String status);

    AdminExperienceResponse.Template createTemplate(AdminExperienceRequest.TemplateUpsert request);

    AdminExperienceResponse.Template updateTemplate(Long templateId, AdminExperienceRequest.TemplateUpsert request);

    void deleteTemplate(Long templateId);

    PageResponse<AdminExperienceResponse.Flow> pageFlows(long pageNum, long pageSize, String keyword, String flowType, String status);

    AdminExperienceResponse.Flow getFlow(Long flowId);

    AdminExperienceResponse.Flow createFlow(AdminExperienceRequest.FlowUpsert request);

    AdminExperienceResponse.Flow updateFlow(Long flowId, AdminExperienceRequest.FlowUpsert request);

    void deleteFlow(Long flowId);

    AdminExperienceResponse.Step createStep(Long flowId, AdminExperienceRequest.StepUpsert request);

    AdminExperienceResponse.Step updateStep(Long flowId, Long stepId, AdminExperienceRequest.StepUpsert request);

    void deleteStep(Long flowId, Long stepId);

    PageResponse<AdminExperienceResponse.Binding> pageBindings(long pageNum, long pageSize, String ownerType, Long ownerId, String ownerCode);

    AdminExperienceResponse.Binding createBinding(AdminExperienceRequest.BindingUpsert request);

    AdminExperienceResponse.Binding updateBinding(Long bindingId, AdminExperienceRequest.BindingUpsert request);

    void deleteBinding(Long bindingId);

    PageResponse<AdminExperienceResponse.OverrideRule> pageOverrides(long pageNum, long pageSize, String ownerType, Long ownerId);

    AdminExperienceResponse.OverrideRule createOverride(AdminExperienceRequest.OverrideUpsert request);

    AdminExperienceResponse.OverrideRule updateOverride(Long overrideId, AdminExperienceRequest.OverrideUpsert request);

    void deleteOverride(Long overrideId);

    PageResponse<AdminExperienceResponse.ExplorationElement> pageExplorationElements(
            long pageNum,
            long pageSize,
            String keyword,
            String ownerType,
            Long ownerId,
            Long cityId,
            Long subMapId,
            Long storylineId,
            String status);

    AdminExperienceResponse.ExplorationElement createExplorationElement(AdminExperienceRequest.ExplorationElementUpsert request);

    AdminExperienceResponse.ExplorationElement updateExplorationElement(Long elementId, AdminExperienceRequest.ExplorationElementUpsert request);

    void deleteExplorationElement(Long elementId);

    AdminExperienceResponse.GovernanceOverview getGovernanceOverview();
}
