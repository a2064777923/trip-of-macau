SET NAMES utf8mb4;
USE `aoxiaoyou`;

ALTER TABLE `ai_provider_configs` ADD COLUMN `provider_type` VARCHAR(64) DEFAULT 'dashscope' AFTER `display_name`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `endpoint_style` VARCHAR(64) DEFAULT 'openai_compatible' AFTER `provider_type`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `api_key_masked` VARCHAR(128) NULL AFTER `api_key_encrypted`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `api_secret_masked` VARCHAR(128) NULL AFTER `api_secret_encrypted`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `feature_flags_json` LONGTEXT NULL AFTER `capabilities`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `health_status` VARCHAR(32) DEFAULT 'unknown' AFTER `status`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `health_message` VARCHAR(255) NULL AFTER `health_status`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `last_health_checked_at` DATETIME NULL AFTER `health_message`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `last_success_at` DATETIME NULL AFTER `last_health_checked_at`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `last_failure_at` DATETIME NULL AFTER `last_success_at`;
ALTER TABLE `ai_provider_configs` ADD COLUMN `secret_updated_at` DATETIME NULL AFTER `last_failure_at`;

CREATE TABLE IF NOT EXISTS `ai_capabilities` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `domain_code` VARCHAR(32) NOT NULL,
  `capability_code` VARCHAR(64) NOT NULL,
  `display_name_zht` VARCHAR(128) NOT NULL,
  `display_name_zh` VARCHAR(128) NULL,
  `display_name_en` VARCHAR(128) NULL,
  `display_name_pt` VARCHAR(128) NULL,
  `summary_zht` VARCHAR(255) NULL,
  `summary_zh` VARCHAR(255) NULL,
  `summary_en` VARCHAR(255) NULL,
  `summary_pt` VARCHAR(255) NULL,
  `supports_public_runtime` TINYINT DEFAULT 0,
  `supports_admin_creative` TINYINT DEFAULT 0,
  `supports_text` TINYINT DEFAULT 0,
  `supports_image` TINYINT DEFAULT 0,
  `supports_audio` TINYINT DEFAULT 0,
  `supports_vision` TINYINT DEFAULT 0,
  `status` VARCHAR(32) DEFAULT 'enabled',
  `sort_order` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_ai_capability_code` (`capability_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_capability_policies` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `capability_id` BIGINT NOT NULL,
  `policy_code` VARCHAR(64) NOT NULL,
  `policy_name` VARCHAR(128) NOT NULL,
  `policy_type` VARCHAR(32) DEFAULT 'default',
  `execution_mode` VARCHAR(32) DEFAULT 'auto',
  `response_mode` VARCHAR(32) DEFAULT 'structured',
  `default_model` VARCHAR(128) NULL,
  `system_prompt` LONGTEXT NULL,
  `prompt_template` LONGTEXT NULL,
  `response_schema_json` LONGTEXT NULL,
  `post_process_rules_json` LONGTEXT NULL,
  `manual_switch_provider_id` BIGINT NULL,
  `multimodal_enabled` TINYINT DEFAULT 0,
  `voice_enabled` TINYINT DEFAULT 0,
  `structured_output_enabled` TINYINT DEFAULT 0,
  `temperature` DECIMAL(6,3) NULL,
  `max_tokens` INT NULL,
  `status` VARCHAR(32) DEFAULT 'enabled',
  `sort_order` INT DEFAULT 0,
  `notes` VARCHAR(255) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_ai_policy_code` (`policy_code`),
  KEY `idx_ai_policy_capability` (`capability_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_policy_provider_bindings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `policy_id` BIGINT NOT NULL,
  `provider_id` BIGINT NOT NULL,
  `binding_role` VARCHAR(32) DEFAULT 'primary',
  `sort_order` INT DEFAULT 0,
  `enabled` TINYINT DEFAULT 1,
  `model_override` VARCHAR(128) NULL,
  `weight_percent` INT NULL,
  `notes` VARCHAR(255) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_ai_policy_provider_binding_policy` (`policy_id`),
  KEY `idx_ai_policy_provider_binding_provider` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_quota_rules` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `capability_id` BIGINT NOT NULL,
  `policy_id` BIGINT NULL,
  `scope_type` VARCHAR(32) DEFAULT 'global',
  `scope_value` VARCHAR(128) NULL,
  `window_type` VARCHAR(32) DEFAULT 'minute',
  `window_size` INT DEFAULT 1,
  `request_limit` INT NULL,
  `token_limit` INT NULL,
  `suspicious_concurrency_threshold` INT NULL,
  `action_mode` VARCHAR(32) DEFAULT 'throttle',
  `status` VARCHAR(32) DEFAULT 'enabled',
  `notes` VARCHAR(255) NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_ai_quota_capability` (`capability_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_prompt_templates` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `capability_id` BIGINT NOT NULL,
  `template_code` VARCHAR(64) NOT NULL,
  `template_name` VARCHAR(128) NOT NULL,
  `template_type` VARCHAR(32) DEFAULT 'text',
  `asset_slot_code` VARCHAR(64) NULL,
  `system_prompt` LONGTEXT NULL,
  `prompt_template` LONGTEXT NOT NULL,
  `variable_schema_json` LONGTEXT NULL,
  `output_constraints_json` LONGTEXT NULL,
  `default_provider_id` BIGINT NULL,
  `default_policy_id` BIGINT NULL,
  `status` VARCHAR(32) DEFAULT 'enabled',
  `sort_order` INT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_ai_prompt_template_code` (`template_code`),
  KEY `idx_ai_prompt_template_capability` (`capability_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_generation_jobs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `capability_id` BIGINT NOT NULL,
  `policy_id` BIGINT NULL,
  `prompt_template_id` BIGINT NULL,
  `provider_id` BIGINT NULL,
  `provider_binding_id` BIGINT NULL,
  `owner_admin_id` BIGINT NOT NULL,
  `owner_admin_name` VARCHAR(128) NOT NULL,
  `generation_type` VARCHAR(32) NOT NULL,
  `source_scope` VARCHAR(64) NULL,
  `source_scope_id` BIGINT NULL,
  `job_status` VARCHAR(32) DEFAULT 'pending',
  `prompt_title` VARCHAR(128) NULL,
  `prompt_text` LONGTEXT NULL,
  `prompt_variables_json` LONGTEXT NULL,
  `request_payload_json` LONGTEXT NULL,
  `provider_request_id` VARCHAR(128) NULL,
  `result_summary` LONGTEXT NULL,
  `error_message` LONGTEXT NULL,
  `latest_candidate_id` BIGINT NULL,
  `finalized_candidate_id` BIGINT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_ai_generation_job_owner` (`owner_admin_id`),
  KEY `idx_ai_generation_job_capability` (`capability_id`),
  KEY `idx_ai_generation_job_status` (`job_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_generation_candidates` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `candidate_index` INT DEFAULT 1,
  `candidate_type` VARCHAR(32) NOT NULL,
  `storage_bucket_name` VARCHAR(128) NULL,
  `storage_region` VARCHAR(64) NULL,
  `storage_object_key` VARCHAR(512) NULL,
  `storage_url` VARCHAR(1024) NULL,
  `mime_type` VARCHAR(128) NULL,
  `file_size_bytes` BIGINT NULL,
  `width_px` INT NULL,
  `height_px` INT NULL,
  `duration_ms` INT NULL,
  `transcript_text` LONGTEXT NULL,
  `preview_text` LONGTEXT NULL,
  `provider_asset_url` VARCHAR(1024) NULL,
  `metadata_json` LONGTEXT NULL,
  `is_selected` TINYINT DEFAULT 0,
  `is_finalized` TINYINT DEFAULT 0,
  `finalized_asset_id` BIGINT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_ai_generation_candidate_job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `ai_request_logs` ADD COLUMN `capability_code` VARCHAR(64) NULL AFTER `policy_id`;
ALTER TABLE `ai_request_logs` ADD COLUMN `admin_owner_id` BIGINT NULL AFTER `user_openid`;
ALTER TABLE `ai_request_logs` ADD COLUMN `admin_owner_name` VARCHAR(128) NULL AFTER `admin_owner_id`;
ALTER TABLE `ai_request_logs` ADD COLUMN `fallback_triggered` TINYINT DEFAULT 0 AFTER `success`;
ALTER TABLE `ai_request_logs` ADD COLUMN `blocked_reason` VARCHAR(255) NULL AFTER `fallback_triggered`;
ALTER TABLE `ai_request_logs` ADD COLUMN `trace_id` VARCHAR(64) NULL AFTER `blocked_reason`;
