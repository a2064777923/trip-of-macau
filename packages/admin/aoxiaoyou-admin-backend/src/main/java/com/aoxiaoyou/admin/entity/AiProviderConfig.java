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
@TableName("ai_provider_configs")
public class AiProviderConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_name")
    private String providerName;

    @TableField("platform_code")
    private String platformCode;

    @TableField("display_name")
    private String displayName;

    @TableField("platform_label")
    private String platformLabel;

    @TableField("provider_type")
    private String providerType;

    @TableField("endpoint_style")
    private String endpointStyle;

    @TableField("sync_strategy")
    private String syncStrategy;

    @TableField("auth_scheme")
    private String authScheme;

    @TableField("api_base_url")
    private String apiBaseUrl;

    @TableField("docs_url")
    private String docsUrl;

    @TableField("api_key_encrypted")
    private String apiKeyEncrypted;

    @TableField("api_key_masked")
    private String apiKeyMasked;

    @TableField("api_secret_encrypted")
    private String apiSecretEncrypted;

    @TableField("api_secret_masked")
    private String apiSecretMasked;

    @TableField("credential_schema_json")
    private String credentialSchemaJson;

    @TableField("model_name")
    private String modelName;

    private String capabilities;

    @TableField("feature_flags_json")
    private String featureFlagsJson;

    @TableField("provider_settings_json")
    private String providerSettingsJson;

    @TableField("request_timeout_ms")
    private Integer requestTimeoutMs;

    @TableField("max_retries")
    private Integer maxRetries;

    @TableField("quota_daily")
    private Integer quotaDaily;

    @TableField("cost_per_1k_tokens")
    private BigDecimal costPer1kTokens;

    private Integer status;

    @TableField("health_status")
    private String healthStatus;

    @TableField("health_message")
    private String healthMessage;

    @TableField("last_inventory_sync_status")
    private String lastInventorySyncStatus;

    @TableField("last_inventory_sync_message")
    private String lastInventorySyncMessage;

    @TableField("last_inventory_synced_at")
    private LocalDateTime lastInventorySyncedAt;

    @TableField("inventory_record_count")
    private Integer inventoryRecordCount;

    @TableField("last_health_checked_at")
    private LocalDateTime lastHealthCheckedAt;

    @TableField("last_success_at")
    private LocalDateTime lastSuccessAt;

    @TableField("last_failure_at")
    private LocalDateTime lastFailureAt;

    @TableField("secret_updated_at")
    private LocalDateTime secretUpdatedAt;
}
