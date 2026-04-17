package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_generation_candidates")
public class AiGenerationCandidate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("job_id")
    private Long jobId;

    @TableField("candidate_index")
    private Integer candidateIndex;

    @TableField("candidate_type")
    private String candidateType;

    @TableField("storage_bucket_name")
    private String storageBucketName;

    @TableField("storage_region")
    private String storageRegion;

    @TableField("storage_object_key")
    private String storageObjectKey;

    @TableField("storage_url")
    private String storageUrl;

    @TableField("mime_type")
    private String mimeType;

    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    @TableField("width_px")
    private Integer widthPx;

    @TableField("height_px")
    private Integer heightPx;

    @TableField("duration_ms")
    private Integer durationMs;

    @TableField("transcript_text")
    private String transcriptText;

    @TableField("preview_text")
    private String previewText;

    @TableField("provider_asset_url")
    private String providerAssetUrl;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("is_selected")
    private Integer isSelected;

    @TableField("is_finalized")
    private Integer isFinalized;

    @TableField("finalized_asset_id")
    private Long finalizedAssetId;
}
