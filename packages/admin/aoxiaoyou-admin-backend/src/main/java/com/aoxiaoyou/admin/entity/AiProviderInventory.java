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
@TableName("ai_provider_inventory")
public class AiProviderInventory extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_id")
    private Long providerId;

    @TableField("inventory_code")
    private String inventoryCode;

    @TableField("external_id")
    private String externalId;

    @TableField("provider_voice_code")
    private String providerVoiceCode;

    @TableField("parent_inventory_code")
    private String parentInventoryCode;

    @TableField("display_name")
    private String displayName;

    @TableField("inventory_type")
    private String inventoryType;

    @TableField("modality_codes_json")
    private String modalityCodesJson;

    @TableField("capability_codes_json")
    private String capabilityCodesJson;

    @TableField("language_codes_json")
    private String languageCodesJson;

    @TableField("sync_strategy")
    private String syncStrategy;

    @TableField("source_type")
    private String sourceType;

    @TableField("availability_status")
    private String availabilityStatus;

    @TableField("endpoint_path")
    private String endpointPath;

    @TableField("context_window_tokens")
    private Integer contextWindowTokens;

    @TableField("input_price_per_1k")
    private BigDecimal inputPricePer1k;

    @TableField("output_price_per_1k")
    private BigDecimal outputPricePer1k;

    @TableField("image_price_per_call")
    private BigDecimal imagePricePerCall;

    @TableField("audio_price_per_minute")
    private BigDecimal audioPricePerMinute;

    @TableField("preview_url")
    private String previewUrl;

    @TableField("preview_text")
    private String previewText;

    @TableField("owner_admin_id")
    private Long ownerAdminId;

    @TableField("owner_admin_name")
    private String ownerAdminName;

    @TableField("source_asset_id")
    private Long sourceAssetId;

    @TableField("clone_status")
    private String cloneStatus;

    @TableField("last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @TableField("feature_flags_json")
    private String featureFlagsJson;

    @TableField("raw_payload_json")
    private String rawPayloadJson;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("last_seen_at")
    private LocalDateTime lastSeenAt;

    @TableField("synced_at")
    private LocalDateTime syncedAt;
}
