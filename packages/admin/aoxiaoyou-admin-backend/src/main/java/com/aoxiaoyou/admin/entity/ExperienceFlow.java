package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experience_flows")
public class ExperienceFlow extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("flow_type")
    private String flowType;

    private String mode;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("map_policy_json")
    private String mapPolicyJson;

    @TableField("advanced_config_json")
    private String advancedConfigJson;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    private Integer deleted;
}
