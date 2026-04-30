-- Phase 35: lifecycle operation scheduling and dependency impact audit.
-- All multilingual text is UTF-8 / utf8mb4. Do not rewrite Chinese through inline PowerShell literals.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `content_lifecycle_operations` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `operation_code` VARCHAR(96) NOT NULL,
  `target_type` VARCHAR(64) NOT NULL,
  `target_id` BIGINT NULL,
  `target_code` VARCHAR(128) NULL,
  `target_name` VARCHAR(255) NULL,
  `action` VARCHAR(32) NOT NULL,
  `from_status` VARCHAR(32) NULL,
  `to_status` VARCHAR(32) NOT NULL,
  `operation_status` VARCHAR(32) NOT NULL DEFAULT 'scheduled',
  `scheduled_at` DATETIME NULL,
  `applied_at` DATETIME NULL,
  `cancelled_at` DATETIME NULL,
  `failed_at` DATETIME NULL,
  `requested_by` BIGINT NULL,
  `requested_by_name` VARCHAR(128) NULL,
  `reason` VARCHAR(1024) NULL,
  `preview_hash` VARCHAR(128) NULL,
  `preview_json` JSON NULL,
  `request_json` JSON NULL,
  `result_json` JSON NULL,
  `error_message` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_content_lifecycle_operations_code` (`operation_code`),
  KEY `idx_lifecycle_operations_target` (`target_type`, `target_id`),
  KEY `idx_lifecycle_operations_status_due` (`operation_status`, `scheduled_at`),
  KEY `idx_lifecycle_operations_action` (`action`),
  KEY `idx_lifecycle_operations_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `content_lifecycle_operation_impacts` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `operation_id` BIGINT NOT NULL,
  `impact_type` VARCHAR(64) NOT NULL,
  `severity` VARCHAR(32) NOT NULL,
  `source_type` VARCHAR(64) NULL,
  `source_id` BIGINT NULL,
  `source_code` VARCHAR(128) NULL,
  `source_name` VARCHAR(255) NULL,
  `relation_type` VARCHAR(96) NULL,
  `target_type` VARCHAR(64) NULL,
  `target_id` BIGINT NULL,
  `target_code` VARCHAR(128) NULL,
  `target_name` VARCHAR(255) NULL,
  `impact_summary` VARCHAR(1024) NULL,
  `metadata_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  KEY `idx_lifecycle_impacts_operation` (`operation_id`, `deleted`),
  KEY `idx_lifecycle_impacts_target` (`target_type`, `target_id`),
  KEY `idx_lifecycle_impacts_type_severity` (`impact_type`, `severity`),
  CONSTRAINT `fk_lifecycle_impacts_operation`
    FOREIGN KEY (`operation_id`) REFERENCES `content_lifecycle_operations` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase35-lifecycle-operations',
  'Phase 35 lifecycle operation scheduling and dependency impact audit tables',
  'completed',
  NOW(),
  'Creates content_lifecycle_operations and content_lifecycle_operation_impacts for preview-first publish/unpublish/remove operations.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
