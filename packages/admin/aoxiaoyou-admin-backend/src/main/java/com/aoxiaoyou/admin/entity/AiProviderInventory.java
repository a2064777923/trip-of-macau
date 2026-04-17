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

    @TableField("display_name")
    private String displayName;

    @TableField("inventory_type")
    private String inventoryType;

    @TableField("modality_codes_json")
    private String modalityCodesJson;

    @TableField("capability_codes_json")
    private String capabilityCodesJson;

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
