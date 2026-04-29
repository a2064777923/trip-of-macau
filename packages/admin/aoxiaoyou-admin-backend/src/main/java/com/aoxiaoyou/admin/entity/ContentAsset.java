package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_assets")
public class ContentAsset extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("asset_kind")
    private String assetKind;

    @TableField("bucket_name")
    private String bucketName;

    private String region;

    @TableField("object_key")
    private String objectKey;

    @TableField("canonical_url")
    private String canonicalUrl;

    @TableField("mime_type")
    private String mimeType;

    @TableField("animation_subtype")
    private String animationSubtype;

    @TableField("poster_asset_id")
    private Long posterAssetId;

    @TableField("fallback_asset_id")
    private Long fallbackAssetId;

    @TableField("default_loop")
    private Boolean defaultLoop;

    @TableField("default_autoplay")
    private Boolean defaultAutoplay;

    @TableField("locale_code")
    private String localeCode;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("file_extension")
    private String fileExtension;

    @TableField("upload_source")
    private String uploadSource;

    @TableField("client_relative_path")
    private String clientRelativePath;

    @TableField("uploaded_by_admin_id")
    private Long uploadedByAdminId;

    @TableField("uploaded_by_admin_name")
    private String uploadedByAdminName;

    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    @TableField("width_px")
    private Integer widthPx;

    @TableField("height_px")
    private Integer heightPx;

    private String checksum;

    private String etag;

    @TableField("processing_policy_code")
    private String processingPolicyCode;

    @TableField("processing_profile_json")
    private String processingProfileJson;

    @TableField("processing_status")
    private String processingStatus;

    @TableField("processing_note")
    private String processingNote;

    private String status;

    @TableField("published_at")
    private java.time.LocalDateTime publishedAt;
}
