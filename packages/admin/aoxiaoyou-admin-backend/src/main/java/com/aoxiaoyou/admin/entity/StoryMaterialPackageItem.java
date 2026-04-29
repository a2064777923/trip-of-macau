package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("story_material_package_items")
public class StoryMaterialPackageItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("item_key")
    private String itemKey;

    @TableField("item_type")
    private String itemType;

    @TableField("asset_kind")
    private String assetKind;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_code")
    private String targetCode;

    @TableField("asset_id")
    private Long assetId;

    @TableField("local_path")
    private String localPath;

    @TableField("cos_object_key")
    private String cosObjectKey;

    @TableField("canonical_url")
    private String canonicalUrl;

    @TableField("usage_target")
    private String usageTarget;

    @TableField("chapter_code")
    private String chapterCode;

    @TableField("provenance_type")
    private String provenanceType;

    @TableField("prompt_text")
    private String promptText;

    @TableField("script_text")
    private String scriptText;

    @TableField("historical_basis_zh")
    private String historicalBasisZh;

    @TableField("historical_basis_zht")
    private String historicalBasisZht;

    @TableField("literary_dramatization_zh")
    private String literaryDramatizationZh;

    @TableField("literary_dramatization_zht")
    private String literaryDramatizationZht;

    @TableField("fallback_item_key")
    private String fallbackItemKey;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;
}
