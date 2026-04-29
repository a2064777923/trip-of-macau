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
@TableName("story_material_packages")
public class StoryMaterialPackage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("storyline_id")
    private Long storylineId;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_zht")
    private String titleZht;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_pt")
    private String titlePt;

    @TableField("summary_zh")
    private String summaryZh;

    @TableField("summary_zht")
    private String summaryZht;

    @TableField("historical_basis_zh")
    private String historicalBasisZh;

    @TableField("historical_basis_zht")
    private String historicalBasisZht;

    @TableField("literary_dramatization_zh")
    private String literaryDramatizationZh;

    @TableField("literary_dramatization_zht")
    private String literaryDramatizationZht;

    @TableField("local_root")
    private String localRoot;

    @TableField("cos_prefix")
    private String cosPrefix;

    @TableField("manifest_path")
    private String manifestPath;

    @TableField("manifest_json")
    private String manifestJson;

    @TableField("package_status")
    private String packageStatus;

    @TableField("material_count")
    private Integer materialCount;

    @TableField("asset_count")
    private Integer assetCount;

    @TableField("story_object_count")
    private Integer storyObjectCount;

    @TableField("created_by_admin_id")
    private Long createdByAdminId;

    @TableField("created_by_admin_name")
    private String createdByAdminName;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    private Integer deleted;
}
