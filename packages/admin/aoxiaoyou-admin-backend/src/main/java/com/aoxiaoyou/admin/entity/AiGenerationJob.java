package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_generation_jobs")
public class AiGenerationJob extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("policy_id")
    private Long policyId;

    @TableField("prompt_template_id")
    private Long promptTemplateId;

    @TableField("provider_id")
    private Long providerId;

    @TableField("inventory_id")
    private Long inventoryId;

    @TableField("provider_binding_id")
    private Long providerBindingId;

    @TableField("owner_admin_id")
    private Long ownerAdminId;

    @TableField("owner_admin_name")
    private String ownerAdminName;

    @TableField("generation_type")
    private String generationType;

    @TableField("source_scope")
    private String sourceScope;

    @TableField("source_scope_id")
    private Long sourceScopeId;

    @TableField("job_status")
    private String jobStatus;

    @TableField("prompt_title")
    private String promptTitle;

    @TableField("prompt_text")
    private String promptText;

    @TableField("prompt_variables_json")
    private String promptVariablesJson;

    @TableField("request_payload_json")
    private String requestPayloadJson;

    @TableField("provider_request_id")
    private String providerRequestId;

    @TableField("result_summary")
    private String resultSummary;

    @TableField("error_message")
    private String errorMessage;

    @TableField("latest_candidate_id")
    private Long latestCandidateId;

    @TableField("finalized_candidate_id")
    private Long finalizedCandidateId;
}
