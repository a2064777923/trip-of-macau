package com.aoxiaoyou.tripofmacau.entity;

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

    @TableField("usage_target")
    private String usageTarget;

    @TableField("chapter_code")
    private String chapterCode;

    @TableField("fallback_item_key")
    private String fallbackItemKey;

    @TableField("status")
    private String status;

    @TableField("current_version_id")
    private Long currentVersionId;

    @TableField("current_version_no")
    private Integer currentVersionNo;

    @TableField("sort_order")
    private Integer sortOrder;
}
