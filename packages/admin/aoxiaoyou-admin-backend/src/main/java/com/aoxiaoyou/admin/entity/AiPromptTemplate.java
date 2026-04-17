package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_prompt_templates")
public class AiPromptTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("template_code")
    private String templateCode;

    @TableField("template_name")
    private String templateName;

    @TableField("template_type")
    private String templateType;

    @TableField("asset_slot_code")
    private String assetSlotCode;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("prompt_template")
    private String promptTemplate;

    @TableField("variable_schema_json")
    private String variableSchemaJson;

    @TableField("output_constraints_json")
    private String outputConstraintsJson;

    @TableField("default_provider_id")
    private Long defaultProviderId;

    @TableField("default_policy_id")
    private Long defaultPolicyId;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;
}
