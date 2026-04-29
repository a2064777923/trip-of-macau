package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_rules")
public class RewardRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("rule_type")
    private String ruleType;

    private String status;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_zht")
    private String nameZht;

    @TableField("summary_text")
    private String summaryText;

    @TableField("root_condition_group_id")
    private Long rootConditionGroupId;

    @TableField("advanced_config_json")
    private String advancedConfigJson;
}
