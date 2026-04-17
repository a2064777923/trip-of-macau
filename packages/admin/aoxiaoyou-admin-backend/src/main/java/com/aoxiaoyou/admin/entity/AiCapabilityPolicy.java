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
@TableName("ai_capability_policies")
public class AiCapabilityPolicy extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("policy_code")
    private String policyCode;

    @TableField("policy_name")
    private String policyName;

    @TableField("policy_type")
    private String policyType;

    @TableField("execution_mode")
    private String executionMode;

    @TableField("response_mode")
    private String responseMode;

    @TableField("scene_preset_code")
    private String scenePresetCode;

    @TableField("default_model")
    private String defaultModel;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("prompt_template")
    private String promptTemplate;

    @TableField("response_schema_json")
    private String responseSchemaJson;

    @TableField("post_process_rules_json")
    private String postProcessRulesJson;

    @TableField("parameter_config_json")
    private String parameterConfigJson;

    @TableField("expert_override_json")
    private String expertOverrideJson;

    @TableField("manual_switch_provider_id")
    private Long manualSwitchProviderId;

    @TableField("multimodal_enabled")
    private Integer multimodalEnabled;

    @TableField("voice_enabled")
    private Integer voiceEnabled;

    @TableField("structured_output_enabled")
    private Integer structuredOutputEnabled;

    private BigDecimal temperature;

    @TableField("max_tokens")
    private Integer maxTokens;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    private String notes;
}
