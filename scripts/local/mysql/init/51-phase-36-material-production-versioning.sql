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

INSERT INTO `story_material_package_item_versions` (
  `package_item_id`,
  `version_no`,
  `version_status`,
  `promotion_status`,
  `content_asset_id`,
  `source_type`,
  `provider_name`,
  `model_code`,
  `local_path`,
  `cos_object_key`,
  `canonical_url`,
  `asset_kind`,
  `poster_fallback_item_key`,
  `prompt_text`,
  `script_text`,
  `provenance_json`,
  `currency_code`,
  `created_by_admin_id`,
  `created_by_admin_name`,
  `created_at`,
  `updated_at`,
  `deleted`
)
SELECT
  item.`id`,
  1,
  'uploaded',
  CASE
    WHEN item.`status` = 'published' THEN 'published'
    WHEN item.`status` = 'approved' THEN 'approved'
    ELSE 'uploaded'
  END,
  item.`asset_id`,
  'seed_baseline',
  COALESCE(NULLIF(item.`provenance_type`, ''), 'seed'),
  'phase33-material-package',
  COALESCE(item.`local_path`, ''),
  COALESCE(item.`cos_object_key`, ''),
  COALESCE(item.`canonical_url`, ''),
  COALESCE(item.`asset_kind`, ''),
  COALESCE(item.`fallback_item_key`, ''),
  item.`prompt_text`,
  item.`script_text`,
  JSON_OBJECT(
    'schemaVersion', 1,
    'sourceType', 'seed_baseline',
    'note', 'Backfilled baseline version from existing story_material_package_items after Phase 36 versioning migration.',
    'itemStatus', COALESCE(item.`status`, ''),
    'provenanceType', COALESCE(item.`provenance_type`, '')
  ),
  'CNY',
  NULL,
  'phase36-baseline-backfill',
  COALESCE(item.`updated_at`, NOW()),
  COALESCE(item.`updated_at`, NOW()),
  0
FROM `story_material_package_items` item
JOIN `content_assets` asset ON asset.`id` = item.`asset_id`
WHERE item.`deleted` = 0
  AND item.`asset_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `story_material_package_item_versions` existing
    WHERE existing.`package_item_id` = item.`id`
      AND existing.`deleted` = 0
  );

UPDATE `story_material_package_items` item
JOIN (
  SELECT latest.`package_item_id`, version.`id`, version.`version_no`, version.`created_at`
  FROM (
    SELECT `package_item_id`, MAX(`version_no`) AS `latest_version_no`
    FROM `story_material_package_item_versions`
    WHERE `deleted` = 0
    GROUP BY `package_item_id`
  ) latest
  JOIN `story_material_package_item_versions` version
    ON version.`package_item_id` = latest.`package_item_id`
   AND version.`version_no` = latest.`latest_version_no`
   AND version.`deleted` = 0
) current_version ON current_version.`package_item_id` = item.`id`
SET
  item.`current_version_id` = IF(item.`current_version_id` IS NULL, current_version.`id`, item.`current_version_id`),
  item.`current_version_no` = IF(item.`current_version_no` IS NULL OR item.`current_version_no` = 0, current_version.`version_no`, item.`current_version_no`),
  item.`last_produced_at` = IF(item.`last_produced_at` IS NULL, current_version.`created_at`, item.`last_produced_at`)
WHERE item.`deleted` = 0
  AND (
    item.`current_version_id` IS NULL
    OR item.`current_version_no` IS NULL
    OR item.`current_version_no` = 0
    OR item.`last_produced_at` IS NULL
  );

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase36-material-production-versioning',
  'Create material production version journal and package item current-version pointers',
  'completed',
  NOW(),
  'Adds immutable story_material_package_item_versions, additive pointer columns, and idempotent baseline version backfill for existing material package items.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
