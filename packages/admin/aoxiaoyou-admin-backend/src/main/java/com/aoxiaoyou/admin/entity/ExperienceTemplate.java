package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experience_templates")
public class ExperienceTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("template_type")
    private String templateType;

    private String category;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("summary_zh")
    private String summaryZh;

    @TableField("summary_en")
    private String summaryEn;

    @TableField("summary_zht")
    private String summaryZht;

    @TableField("summary_pt")
    private String summaryPt;

    @TableField("config_json")
    private String configJson;

    @TableField("schema_json")
    private String schemaJson;

    @TableField("risk_level")
    private String riskLevel;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;
}
