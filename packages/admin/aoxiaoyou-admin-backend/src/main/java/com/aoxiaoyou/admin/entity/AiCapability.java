package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_capabilities")
public class AiCapability extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("domain_code")
    private String domainCode;

    @TableField("capability_code")
    private String capabilityCode;

    @TableField("display_name_zht")
    private String displayNameZht;

    @TableField("display_name_zh")
    private String displayNameZh;

    @TableField("display_name_en")
    private String displayNameEn;

    @TableField("display_name_pt")
    private String displayNamePt;

    @TableField("summary_zht")
    private String summaryZht;

    @TableField("summary_zh")
    private String summaryZh;

    @TableField("summary_en")
    private String summaryEn;

    @TableField("summary_pt")
    private String summaryPt;

    @TableField("supports_public_runtime")
    private Integer supportsPublicRuntime;

    @TableField("supports_admin_creative")
    private Integer supportsAdminCreative;

    @TableField("supports_text")
    private Integer supportsText;

    @TableField("supports_image")
    private Integer supportsImage;

    @TableField("supports_audio")
    private Integer supportsAudio;

    @TableField("supports_vision")
    private Integer supportsVision;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;
}
