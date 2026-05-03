package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("story_material_package_item_versions")
public class StoryMaterialPackageItemVersion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("package_item_id")
    private Long packageItemId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("version_status")
    private String versionStatus;

    @TableField("promotion_status")
    private String promotionStatus;

    @TableField("content_asset_id")
    private Long contentAssetId;

    @TableField("source_type")
    private String sourceType;

    @TableField("parent_version_id")
    private Long parentVersionId;

    @TableField("asset_kind")
    private String assetKind;

    @TableField("poster_fallback_item_key")
    private String posterFallbackItemKey;
}
