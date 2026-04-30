package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @TableField("ai_job_id")
    private Long aiJobId;

    @TableField("ai_candidate_id")
    private Long aiCandidateId;

    @TableField("source_type")
    private String sourceType;

    @TableField("provider_name")
    private String providerName;

    @TableField("model_code")
    private String modelCode;

    @TableField("parent_version_id")
    private Long parentVersionId;

    @TableField("parent_item_key")
    private String parentItemKey;

    @TableField("local_path")
    private String localPath;

    @TableField("cos_object_key")
    private String cosObjectKey;

    @TableField("canonical_url")
    private String canonicalUrl;

    @TableField("asset_kind")
    private String assetKind;

    @TableField("poster_fallback_item_key")
    private String posterFallbackItemKey;

    @TableField("prompt_text")
    private String promptText;

    @TableField("script_text")
    private String scriptText;

    @TableField("provenance_json")
    private String provenanceJson;

    @TableField("crop_metadata_json")
    private String cropMetadataJson;

    @TableField("subtitle_metadata_json")
    private String subtitleMetadataJson;

    @TableField("estimated_cost")
    private BigDecimal estimatedCost;

    @TableField("actual_cost")
    private BigDecimal actualCost;

    @TableField("currency_code")
    private String currencyCode;

    @TableField("verified_by_admin_id")
    private Long verifiedByAdminId;

    @TableField("verified_by_admin_name")
    private String verifiedByAdminName;

    @TableField("verified_at")
    private LocalDateTime verifiedAt;

    @TableField("rollback_of_version_id")
    private Long rollbackOfVersionId;

    @TableField("created_by_admin_id")
    private Long createdByAdminId;

    @TableField("created_by_admin_name")
    private String createdByAdminName;

    private Integer deleted;
}
