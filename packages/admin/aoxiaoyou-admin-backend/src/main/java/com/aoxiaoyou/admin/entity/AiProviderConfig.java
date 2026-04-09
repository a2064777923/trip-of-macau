package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider_configs")
public class AiProviderConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_name")
    private String providerName;

    @TableField("display_name")
    private String displayName;

    @TableField("api_base_url")
    private String apiBaseUrl;

    @TableField("api_key_encrypted")
    private String apiKeyEncrypted;

    @TableField("api_secret_encrypted")
    private String apiSecretEncrypted;

    @TableField("model_name")
    private String modelName;

    private String capabilities;

    @TableField("request_timeout_ms")
    private Integer requestTimeoutMs;

    @TableField("max_retries")
    private Integer maxRetries;

    @TableField("quota_daily")
    private Integer quotaDaily;

    @TableField("cost_per_1k_tokens")
    private BigDecimal costPer1kTokens;

    private Integer status;
}
