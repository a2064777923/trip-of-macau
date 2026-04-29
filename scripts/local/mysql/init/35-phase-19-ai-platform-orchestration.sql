SET NAMES utf8mb4;
USE `aoxiaoyou`;

DROP PROCEDURE IF EXISTS `ensure_column`;
DELIMITER $$
CREATE PROCEDURE `ensure_column`(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_alter_sql LONGTEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = p_alter_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL ensure_column('ai_provider_configs', 'platform_code', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `platform_code` VARCHAR(64) NOT NULL DEFAULT ''custom'' AFTER `provider_name`');
CALL ensure_column('ai_provider_configs', 'platform_label', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `platform_label` VARCHAR(128) NULL AFTER `display_name`');
CALL ensure_column('ai_provider_configs', 'sync_strategy', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `sync_strategy` VARCHAR(64) NOT NULL DEFAULT ''manual'' AFTER `endpoint_style`');
CALL ensure_column('ai_provider_configs', 'auth_scheme', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `auth_scheme` VARCHAR(32) NOT NULL DEFAULT ''bearer_key'' AFTER `sync_strategy`');
CALL ensure_column('ai_provider_configs', 'docs_url', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `docs_url` VARCHAR(512) NULL AFTER `api_base_url`');
CALL ensure_column('ai_provider_configs', 'credential_schema_json', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `credential_schema_json` LONGTEXT NULL AFTER `api_secret_masked`');
CALL ensure_column('ai_provider_configs', 'provider_settings_json', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `provider_settings_json` LONGTEXT NULL AFTER `feature_flags_json`');
CALL ensure_column('ai_provider_configs', 'last_inventory_sync_status', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `last_inventory_sync_status` VARCHAR(32) NOT NULL DEFAULT ''idle'' AFTER `health_message`');
CALL ensure_column('ai_provider_configs', 'last_inventory_sync_message', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `last_inventory_sync_message` VARCHAR(255) NULL AFTER `last_inventory_sync_status`');
CALL ensure_column('ai_provider_configs', 'last_inventory_synced_at', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `last_inventory_synced_at` DATETIME NULL AFTER `last_inventory_sync_message`');
CALL ensure_column('ai_provider_configs', 'inventory_record_count', 'ALTER TABLE `ai_provider_configs` ADD COLUMN `inventory_record_count` INT NOT NULL DEFAULT 0 AFTER `last_inventory_synced_at`');

CALL ensure_column('ai_capability_policies', 'scene_preset_code', 'ALTER TABLE `ai_capability_policies` ADD COLUMN `scene_preset_code` VARCHAR(64) NULL AFTER `response_mode`');
CALL ensure_column('ai_capability_policies', 'parameter_config_json', 'ALTER TABLE `ai_capability_policies` ADD COLUMN `parameter_config_json` LONGTEXT NULL AFTER `post_process_rules_json`');
CALL ensure_column('ai_capability_policies', 'expert_override_json', 'ALTER TABLE `ai_capability_policies` ADD COLUMN `expert_override_json` LONGTEXT NULL AFTER `parameter_config_json`');

CALL ensure_column('ai_policy_provider_bindings', 'inventory_id', 'ALTER TABLE `ai_policy_provider_bindings` ADD COLUMN `inventory_id` BIGINT NULL AFTER `provider_id`');
CALL ensure_column('ai_policy_provider_bindings', 'route_mode', 'ALTER TABLE `ai_policy_provider_bindings` ADD COLUMN `route_mode` VARCHAR(32) NOT NULL DEFAULT ''primary'' AFTER `binding_role`');
CALL ensure_column('ai_policy_provider_bindings', 'timeout_ms_override', 'ALTER TABLE `ai_policy_provider_bindings` ADD COLUMN `timeout_ms_override` INT NULL AFTER `weight_percent`');
CALL ensure_column('ai_policy_provider_bindings', 'retry_count_override', 'ALTER TABLE `ai_policy_provider_bindings` ADD COLUMN `retry_count_override` INT NULL AFTER `timeout_ms_override`');
CALL ensure_column('ai_policy_provider_bindings', 'parameter_override_json', 'ALTER TABLE `ai_policy_provider_bindings` ADD COLUMN `parameter_override_json` LONGTEXT NULL AFTER `retry_count_override`');

CALL ensure_column('ai_generation_jobs', 'inventory_id', 'ALTER TABLE `ai_generation_jobs` ADD COLUMN `inventory_id` BIGINT NULL AFTER `provider_id`');
CALL ensure_column('ai_request_logs', 'inventory_id', 'ALTER TABLE `ai_request_logs` ADD COLUMN `inventory_id` BIGINT NULL AFTER `provider_id`');
CALL ensure_column('ai_request_logs', 'inventory_code', 'ALTER TABLE `ai_request_logs` ADD COLUMN `inventory_code` VARCHAR(128) NULL AFTER `capability_code`');

DROP PROCEDURE IF EXISTS `ensure_column`;

CREATE TABLE IF NOT EXISTS `ai_provider_inventory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `provider_id` BIGINT NOT NULL,
  `inventory_code` VARCHAR(128) NOT NULL,
  `external_id` VARCHAR(128) NOT NULL,
  `display_name` VARCHAR(255) NOT NULL,
  `inventory_type` VARCHAR(32) NOT NULL DEFAULT 'model',
  `modality_codes_json` LONGTEXT NULL,
  `capability_codes_json` LONGTEXT NULL,
  `sync_strategy` VARCHAR(64) NOT NULL DEFAULT 'manual',
  `source_type` VARCHAR(32) NOT NULL DEFAULT 'manual',
  `availability_status` VARCHAR(32) NOT NULL DEFAULT 'available',
  `endpoint_path` VARCHAR(255) NULL,
  `context_window_tokens` INT NULL,
  `input_price_per_1k` DECIMAL(12,6) NULL,
  `output_price_per_1k` DECIMAL(12,6) NULL,
  `image_price_per_call` DECIMAL(12,6) NULL,
  `audio_price_per_minute` DECIMAL(12,6) NULL,
  `feature_flags_json` LONGTEXT NULL,
  `raw_payload_json` LONGTEXT NULL,
  `is_default` TINYINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `last_seen_at` DATETIME NULL,
  `synced_at` DATETIME NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_ai_provider_inventory_code` (`provider_id`, `inventory_code`),
  UNIQUE KEY `uk_ai_provider_inventory_external` (`provider_id`, `external_id`),
  KEY `idx_ai_provider_inventory_provider` (`provider_id`),
  KEY `idx_ai_provider_inventory_source` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_provider_sync_jobs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `provider_id` BIGINT NOT NULL,
  `platform_code` VARCHAR(64) NOT NULL,
  `sync_strategy` VARCHAR(64) NOT NULL,
  `job_status` VARCHAR(32) NOT NULL DEFAULT 'pending',
  `message` VARCHAR(255) NULL,
  `error_detail` LONGTEXT NULL,
  `discovered_count` INT NOT NULL DEFAULT 0,
  `created_count` INT NOT NULL DEFAULT 0,
  `updated_count` INT NOT NULL DEFAULT 0,
  `stale_count` INT NOT NULL DEFAULT 0,
  `raw_payload_json` LONGTEXT NULL,
  `started_at` DATETIME NULL,
  `finished_at` DATETIME NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_ai_provider_sync_jobs_provider` (`provider_id`),
  KEY `idx_ai_provider_sync_jobs_status` (`job_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE `ai_provider_configs`
SET
  `platform_code` = CASE
    WHEN `provider_name` LIKE 'dashscope%' THEN 'bailian'
    WHEN `provider_name` = 'hunyuan' THEN 'hunyuan'
    ELSE COALESCE(NULLIF(`platform_code`, ''), 'custom')
  END,
  `platform_label` = CASE
    WHEN `provider_name` LIKE 'dashscope%' THEN CONVERT(0xE998BFE9878CE799BEE98D8A USING utf8mb4)
    WHEN `provider_name` = 'hunyuan' THEN CONVERT(0xE9A8B0E8A88AE6B7B7E58583 USING utf8mb4)
    ELSE COALESCE(NULLIF(`platform_label`, ''), `display_name`)
  END,
  `sync_strategy` = CASE
    WHEN `endpoint_style` = 'openai_compatible' AND `provider_name` LIKE 'dashscope%' THEN 'hybrid_list_or_catalog'
    WHEN `endpoint_style` = 'openai_compatible' AND `provider_name` = 'hunyuan' THEN 'documented_catalog'
    WHEN `endpoint_style` IN ('dashscope_image', 'dashscope_tts') THEN 'documented_catalog'
    ELSE COALESCE(NULLIF(`sync_strategy`, ''), 'manual')
  END,
  `auth_scheme` = COALESCE(NULLIF(`auth_scheme`, ''), 'bearer_key'),
  `last_inventory_sync_status` = COALESCE(NULLIF(`last_inventory_sync_status`, ''), 'idle');

UPDATE `ai_provider_configs`
SET
  `display_name` = CASE `provider_name`
    WHEN 'dashscope-chat' THEN CONVERT(0xE998BFE9878CE799BEE98D8AE5B08DE8A9B1 USING utf8mb4)
    WHEN 'dashscope-image' THEN CONVERT(0xE998BFE9878CE799BEE98D8AE69687E7949FE59C96 USING utf8mb4)
    WHEN 'dashscope-tts' THEN CONVERT(0xE998BFE9878CE799BEE98D8AE8AA9EE99FB3E59088E68890 USING utf8mb4)
    WHEN 'hunyuan' THEN CONVERT(0xE9A8B0E8A88AE6B7B7E58583 USING utf8mb4)
    ELSE `display_name`
  END,
  `provider_type` = CASE
    WHEN `provider_name` LIKE 'dashscope%' THEN 'bailian'
    WHEN `provider_name` = 'hunyuan' THEN 'hunyuan'
    ELSE `provider_type`
  END,
  `docs_url` = CASE
    WHEN `provider_name` LIKE 'dashscope%' THEN 'https://bailian.console.aliyun.com'
    WHEN `provider_name` = 'hunyuan' THEN 'https://cloud.tencent.com/document/product/1729/111007'
    ELSE `docs_url`
  END,
  `api_base_url` = CASE
    WHEN `provider_name` = 'dashscope-image' THEN 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image-generation/generation'
    ELSE `api_base_url`
  END,
  `model_name` = CASE
    WHEN `provider_name` = 'dashscope-chat' THEN 'qwen3.5-flash'
    WHEN `provider_name` = 'dashscope-image' THEN 'wan2.6-image'
    WHEN `provider_name` = 'dashscope-tts' THEN 'cosyvoice-v3-flash'
    ELSE `model_name`
  END,
  `health_message` = CASE
    WHEN `health_status` = 'healthy' THEN CONVERT(0xE69C80E8BF91E4B880E6ACA1E980A3E9809AE6B8ACE8A9A6E68890E58A9F USING utf8mb4)
    ELSE CONVERT(0xE5B09AE69CAAE5AE8CE68890E980A3E9809AE6B8ACE8A9A6 USING utf8mb4)
  END
WHERE `provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts', 'hunyuan');

UPDATE `ai_capability_policies`
SET
  `policy_name` = CASE `policy_code`
    WHEN 'admin-image-default' THEN CONVERT(0x414920E59C96E5838FE7949FE68890E59FBAE7A48EE7AD96E795A5 USING utf8mb4)
    WHEN 'admin-tts-default' THEN CONVERT(0x414920E8AA9EE99FB3E59088E68890E59FBAE7A48EE7AD96E795A5 USING utf8mb4)
    WHEN 'itinerary-default' THEN CONVERT(0xE8A18CE7A88BE8A68FE58A83E9A090E8A8ADE7AD96E795A5 USING utf8mb4)
    WHEN 'travel-qa-default' THEN CONVERT(0xE69785E8A18CE5958FE7AD94E9A090E8A8ADE7AD96E795A5 USING utf8mb4)
    WHEN 'npc-voice-default' THEN CONVERT(0x4E504320E8AA9EE99FB3E5B08DE8A9B1E9A090E8A8ADE7AD96E795A5 USING utf8mb4)
    WHEN 'navigation-default' THEN CONVERT(0xE5B08EE888AAE8BC94E58AA9E9A090E8A8ADE7AD96E795A5 USING utf8mb4)
    ELSE `policy_name`
  END,
  `default_model` = CASE
    WHEN `policy_code` IN ('itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default') THEN 'qwen3.5-flash'
    WHEN `policy_code` = 'admin-image-default' THEN 'wan2.6-image'
    WHEN `policy_code` = 'admin-tts-default' THEN 'cosyvoice-v3-flash'
    ELSE `default_model`
  END
WHERE `policy_code` IN (
  'admin-image-default',
  'admin-tts-default',
  'itinerary-default',
  'travel-qa-default',
  'npc-voice-default',
  'navigation-default'
);

UPDATE `ai_policy_provider_bindings` b
JOIN `ai_capability_policies` p ON p.id = b.policy_id
LEFT JOIN `ai_provider_inventory` inv
  ON inv.provider_id = b.provider_id
 AND inv.availability_status = 'available'
 AND inv.inventory_code = CASE
   WHEN p.policy_code IN ('itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default') THEN 'qwen3.5-flash'
   WHEN p.policy_code = 'admin-image-default' THEN 'wan2.6-image'
   WHEN p.policy_code = 'admin-tts-default' THEN 'cosyvoice-v3-flash'
   ELSE inv.inventory_code
 END
SET
  b.inventory_id = inv.id,
  b.model_override = CASE
    WHEN p.policy_code IN ('itinerary-default', 'travel-qa-default', 'npc-voice-default', 'navigation-default') THEN 'qwen3.5-flash'
    WHEN p.policy_code = 'admin-image-default' THEN 'wan2.6-image'
    WHEN p.policy_code = 'admin-tts-default' THEN 'cosyvoice-v3-flash'
    ELSE b.model_override
  END,
  b.notes = 'Phase 19 default primary provider'
WHERE p.policy_code IN (
  'admin-image-default',
  'admin-tts-default',
  'itinerary-default',
  'travel-qa-default',
  'npc-voice-default',
  'navigation-default'
);

UPDATE `ai_provider_inventory` inv
JOIN `ai_provider_configs` pr ON pr.id = inv.provider_id
SET inv.`inventory_code` = 'wan2.6-image',
    inv.`external_id` = 'wan2.6-image',
    inv.`display_name` = 'Wan 2.6 Image',
    inv.`last_seen_at` = NOW(),
    inv.`synced_at` = NOW()
WHERE pr.`provider_name` = 'dashscope-image'
  AND inv.`inventory_code` = 'wan2.6-t2i-turbo';

UPDATE `ai_provider_inventory` inv
JOIN `ai_provider_configs` pr ON pr.id = inv.provider_id
SET inv.`is_default` = CASE
  WHEN pr.`provider_name` = 'dashscope-chat'
       AND inv.`inventory_code` = 'qwen3.5-flash'
       AND inv.`availability_status` = 'available' THEN 1
  WHEN pr.`provider_name` = 'dashscope-image'
       AND inv.`inventory_code` = 'wan2.6-image'
       AND inv.`availability_status` = 'available' THEN 1
  WHEN pr.`provider_name` = 'dashscope-tts'
       AND inv.`inventory_code` = 'cosyvoice-v3-flash'
       AND inv.`availability_status` = 'available' THEN 1
  ELSE 0
END
WHERE pr.`provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts');

UPDATE `ai_provider_configs` pr
LEFT JOIN (
  SELECT `provider_id`, COUNT(*) AS `available_count`
  FROM `ai_provider_inventory`
  WHERE `availability_status` = 'available'
  GROUP BY `provider_id`
) stats ON stats.provider_id = pr.id
SET pr.`inventory_record_count` = COALESCE(stats.available_count, 0)
WHERE pr.`provider_name` IN ('dashscope-chat', 'dashscope-image', 'dashscope-tts', 'hunyuan');
