package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_navigation_policies")
public class AiPolicy extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("policy_name")
    private String policyName;

    @TableField("scenario_code")
    private String scenarioCode;

    @TableField("policy_type")
    private String policyType;

    @TableField("scenario_group")
    private String scenarioGroup;

    @TableField("provider_id")
    private Long providerId;

    @TableField("prompt_template")
    private String promptTemplate;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("model_override")
    private String modelOverride;

    @TableField("multimodal_enabled")
    private Integer multimodalEnabled;

    @TableField("voice_enabled")
    private Integer voiceEnabled;

    private BigDecimal temperature;

    @TableField("max_tokens")
    private Integer maxTokens;

    @TableField("response_schema")
    private String responseSchema;

    @TableField("post_process_rules")
    private String postProcessRules;

    @TableField("fallback_policy_id")
    private Long fallbackPolicyId;

    private Integer status;
}
