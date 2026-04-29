package com.aoxiaoyou.tripofmacau.entity;

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

    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    @TableField("width_px")
    private Integer widthPx;

    @TableField("height_px")
    private Integer heightPx;

    private String checksum;

    private String etag;

    private String status;
}
