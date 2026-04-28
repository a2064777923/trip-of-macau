package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experience_flow_steps")
public class ExperienceFlowStep extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("flow_id")
    private Long flowId;

    @TableField("step_code")
    private String stepCode;

    @TableField("step_type")
    private String stepType;

    @TableField("template_id")
    private Long templateId;

    @TableField("step_name_zh")
    private String stepNameZh;

    @TableField("step_name_en")
    private String stepNameEn;

    @TableField("step_name_zht")
    private String stepNameZht;

    @TableField("step_name_pt")
    private String stepNamePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_config_json")
    private String triggerConfigJson;

    @TableField("condition_config_json")
    private String conditionConfigJson;

    @TableField("effect_config_json")
    private String effectConfigJson;

    @TableField("media_asset_id")
    private Long mediaAssetId;

    @TableField("reward_rule_ids_json")
    private String rewardRuleIdsJson;

    @TableField("exploration_weight_level")
    private String explorationWeightLevel;

    @TableField("required_for_completion")
    private Boolean requiredForCompletion;

    @TableField("inherit_key")
    private String inheritKey;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;
}
