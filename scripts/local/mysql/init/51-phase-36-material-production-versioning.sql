USE `aoxiaoyou`;

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS `ensure_phase36_column`;
DELIMITER //
CREATE PROCEDURE `ensure_phase36_column`(
  IN in_table_name VARCHAR(128),
  IN in_column_name VARCHAR(128),
  IN in_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = in_table_name
      AND column_name = in_column_name
  ) THEN
    SET @ddl = in_definition;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

CALL `ensure_phase36_column`(
  'story_material_package_items',
  'current_version_id',
  'ALTER TABLE `story_material_package_items` ADD COLUMN `current_version_id` BIGINT NULL AFTER `asset_id`'
);

CALL `ensure_phase36_column`(
  'story_material_package_items',
  'current_version_no',
  'ALTER TABLE `story_material_package_items` ADD COLUMN `current_version_no` INT NOT NULL DEFAULT 0 AFTER `current_version_id`'
);

CALL `ensure_phase36_column`(
  'story_material_package_items',
  'last_produced_at',
  'ALTER TABLE `story_material_package_items` ADD COLUMN `last_produced_at` DATETIME NULL AFTER `current_version_no`'
);

DROP PROCEDURE IF EXISTS `ensure_phase36_column`;

CREATE TABLE IF NOT EXISTS `story_material_package_item_versions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `package_item_id` BIGINT NOT NULL,
  `version_no` INT NOT NULL,
  `version_status` VARCHAR(32) NOT NULL DEFAULT 'uploaded',
  `promotion_status` VARCHAR(32) NOT NULL DEFAULT 'uploaded',
  `content_asset_id` BIGINT NULL,
  `ai_job_id` BIGINT NULL,
  `ai_candidate_id` BIGINT NULL,
  `source_type` VARCHAR(32) NOT NULL DEFAULT 'local_import',
  `provider_name` VARCHAR(128) NOT NULL DEFAULT '',
  `model_code` VARCHAR(128) NOT NULL DEFAULT '',
  `parent_version_id` BIGINT NULL,
  `parent_item_key` VARCHAR(128) NOT NULL DEFAULT '',
  `local_path` VARCHAR(1024) NOT NULL DEFAULT '',
  `cos_object_key` VARCHAR(512) NOT NULL DEFAULT '',
  `canonical_url` VARCHAR(1024) NOT NULL DEFAULT '',
  `asset_kind` VARCHAR(32) NOT NULL DEFAULT '',
  `poster_fallback_item_key` VARCHAR(128) NOT NULL DEFAULT '',
  `prompt_text` LONGTEXT NULL,
  `script_text` LONGTEXT NULL,
  `provenance_json` JSON NULL,
  `crop_metadata_json` JSON NULL,
  `subtitle_metadata_json` JSON NULL,
  `estimated_cost` DECIMAL(18,6) NULL,
  `actual_cost` DECIMAL(18,6) NULL,
  `currency_code` VARCHAR(16) NOT NULL DEFAULT 'CNY',
  `verified_by_admin_id` BIGINT NULL,
  `verified_by_admin_name` VARCHAR(128) NOT NULL DEFAULT '',
  `verified_at` DATETIME NULL,
  `rollback_of_version_id` BIGINT NULL,
  `created_by_admin_id` BIGINT NULL,
  `created_by_admin_name` VARCHAR(128) NOT NULL DEFAULT '',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_story_material_package_item_versions_no` (`package_item_id`, `version_no`, `deleted`),
  KEY `idx_story_material_package_item_versions_asset` (`content_asset_id`),
  KEY `idx_story_material_package_item_versions_candidate` (`ai_candidate_id`),
  KEY `idx_story_material_package_item_versions_parent` (`parent_version_id`),
  KEY `idx_story_material_package_item_versions_promotion` (`promotion_status`),
  KEY `idx_story_material_package_item_versions_item_status` (`package_item_id`, `promotion_status`),
  CONSTRAINT `fk_story_material_package_item_versions_item` FOREIGN KEY (`package_item_id`) REFERENCES `story_material_package_items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_story_material_package_item_versions_asset` FOREIGN KEY (`content_asset_id`) REFERENCES `content_assets` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Immutable package item material production versions and rollback targets';

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase36-material-production-versioning',
  'Create material production version journal and package item current-version pointers',
  'completed',
  NOW(),
  'Adds immutable story_material_package_item_versions and additive pointer columns for Phase 36 production promotion and rollback.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
