package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_condition_groups")
public class RewardConditionGroup extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("parent_group_id")
    private Long parentGroupId;

    @TableField("group_code")
    private String groupCode;

    @TableField("operator_type")
    private String operatorType;

    @TableField("minimum_match_count")
    private Integer minimumMatchCount;

    @TableField("summary_text")
    private String summaryText;

    @TableField("advanced_config_json")
    private String advancedConfigJson;

    @TableField("sort_order")
    private Integer sortOrder;
}
