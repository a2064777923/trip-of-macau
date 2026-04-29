USE `aoxiaoyou`;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `story_material_packages` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(96) NOT NULL,
  `storyline_id` BIGINT NULL,
  `title_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `title_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `title_en` VARCHAR(255) NOT NULL DEFAULT '',
  `title_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `summary_zh` TEXT NULL,
  `summary_zht` TEXT NULL,
  `historical_basis_zh` LONGTEXT NULL,
  `historical_basis_zht` LONGTEXT NULL,
  `literary_dramatization_zh` LONGTEXT NULL,
  `literary_dramatization_zht` LONGTEXT NULL,
  `local_root` VARCHAR(1024) NOT NULL DEFAULT '',
  `cos_prefix` VARCHAR(512) NOT NULL DEFAULT '',
  `manifest_path` VARCHAR(1024) NOT NULL DEFAULT '',
  `manifest_json` JSON NULL,
  `package_status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `material_count` INT NOT NULL DEFAULT 0,
  `asset_count` INT NOT NULL DEFAULT 0,
  `story_object_count` INT NOT NULL DEFAULT 0,
  `created_by_admin_id` BIGINT NULL,
  `created_by_admin_name` VARCHAR(128) NOT NULL DEFAULT '',
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_story_material_packages_code` (`code`, `deleted`),
  KEY `idx_story_material_packages_storyline` (`storyline_id`),
  KEY `idx_story_material_packages_status` (`package_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Traceable story material package registry for generated assets, scripts and provenance';

CREATE TABLE IF NOT EXISTS `story_material_package_items` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL,
  `item_key` VARCHAR(128) NOT NULL,
  `item_type` VARCHAR(48) NOT NULL,
  `asset_kind` VARCHAR(32) NOT NULL DEFAULT '',
  `target_type` VARCHAR(64) NOT NULL DEFAULT '',
  `target_id` BIGINT NULL,
  `target_code` VARCHAR(128) NOT NULL DEFAULT '',
  `asset_id` BIGINT NULL,
  `local_path` VARCHAR(1024) NOT NULL DEFAULT '',
  `cos_object_key` VARCHAR(512) NOT NULL DEFAULT '',
  `canonical_url` VARCHAR(1024) NOT NULL DEFAULT '',
  `usage_target` VARCHAR(255) NOT NULL DEFAULT '',
  `chapter_code` VARCHAR(96) NOT NULL DEFAULT '',
  `provenance_type` VARCHAR(32) NOT NULL DEFAULT 'manual',
  `prompt_text` LONGTEXT NULL,
  `script_text` LONGTEXT NULL,
  `historical_basis_zh` LONGTEXT NULL,
  `historical_basis_zht` LONGTEXT NULL,
  `literary_dramatization_zh` LONGTEXT NULL,
  `literary_dramatization_zht` LONGTEXT NULL,
  `fallback_item_key` VARCHAR(128) NOT NULL DEFAULT '',
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_story_material_package_items_key` (`package_id`, `item_key`, `deleted`),
  KEY `idx_story_material_package_items_package` (`package_id`, `status`, `sort_order`),
  KEY `idx_story_material_package_items_asset` (`asset_id`),
  KEY `idx_story_material_package_items_target` (`target_type`, `target_id`, `target_code`),
  CONSTRAINT `fk_story_material_package_items_package` FOREIGN KEY (`package_id`) REFERENCES `story_material_packages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_story_material_package_items_asset` FOREIGN KEY (`asset_id`) REFERENCES `content_assets` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Manifest-level item provenance for story packages and generated materials';

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase33-story-material-package-model',
  'Create story material package registry and item provenance schema',
  'completed',
  NOW(),
  'Adds package-level traceability without replacing content_assets, story, experience, reward, or exploration tables.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
