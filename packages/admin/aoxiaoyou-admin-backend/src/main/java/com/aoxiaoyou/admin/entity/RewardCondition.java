package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_conditions")
public class RewardCondition extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("condition_type")
    private String conditionType;

    @TableField("metric_type")
    private String metricType;

    @TableField("operator_type")
    private String operatorType;

    @TableField("comparator_value")
    private String comparatorValue;

    @TableField("comparator_unit")
    private String comparatorUnit;

    @TableField("summary_text")
    private String summaryText;

    @TableField("config_json")
    private String configJson;

    @TableField("sort_order")
    private Integer sortOrder;
}
