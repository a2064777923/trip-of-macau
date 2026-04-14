-- Phase 2 admin control-plane alignment
-- Keep legacy reward columns intact while adding the canonical fields used by
-- the real admin APIs. This lets local environments migrate forward without
-- breaking any still-legacy readers.

ALTER TABLE rewards
  ADD COLUMN `code` VARCHAR(64) NULL AFTER `id`,
  ADD COLUMN `name_en` VARCHAR(128) NOT NULL DEFAULT '' AFTER `name_zh`,
  ADD COLUMN `name_zht` VARCHAR(128) NOT NULL DEFAULT '' AFTER `name_en`,
  ADD COLUMN `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `name_zht`,
  ADD COLUMN `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_zh`,
  ADD COLUMN `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_en`,
  ADD COLUMN `description_zh` TEXT NULL AFTER `subtitle_zht`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `highlight_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `description_zht`,
  ADD COLUMN `highlight_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `highlight_zh`,
  ADD COLUMN `highlight_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `highlight_en`,
  ADD COLUMN `stamp_cost` INT NOT NULL DEFAULT 0 AFTER `highlight_zht`,
  ADD COLUMN `inventory_total` INT NOT NULL DEFAULT 0 AFTER `stamp_cost`,
  ADD COLUMN `inventory_redeemed` INT NOT NULL DEFAULT 0 AFTER `inventory_total`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `inventory_redeemed`,
  ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `status`,
  ADD COLUMN `publish_start_at` DATETIME NULL AFTER `sort_order`,
  ADD COLUMN `publish_end_at` DATETIME NULL AFTER `publish_start_at`;

ALTER TABLE rewards
  MODIFY COLUMN `code` VARCHAR(64) NULL,
  MODIFY COLUMN `name_zh` VARCHAR(128) NOT NULL,
  MODIFY COLUMN `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `highlight_zh` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `highlight_en` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `highlight_zht` VARCHAR(255) NOT NULL DEFAULT '',
  MODIFY COLUMN `stamp_cost` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `inventory_total` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `inventory_redeemed` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  MODIFY COLUMN `sort_order` INT NOT NULL DEFAULT 0;

UPDATE rewards
SET
  `code` = COALESCE(NULLIF(`code`, ''), CONCAT('legacy_reward_', `id`)),
  `name_en` = COALESCE(NULLIF(`name_en`, ''), `name_zh`),
  `name_zht` = COALESCE(NULLIF(`name_zht`, ''), `name_zh`),
  `description_zh` = COALESCE(NULLIF(`description_zh`, ''), `description`),
  `stamp_cost` = CASE
    WHEN `stamp_cost` = 0 AND `stamps_required` IS NOT NULL THEN `stamps_required`
    ELSE `stamp_cost`
  END,
  `inventory_total` = CASE
    WHEN `inventory_total` = 0 AND `total_quantity` IS NOT NULL THEN `total_quantity`
    ELSE `inventory_total`
  END,
  `inventory_redeemed` = CASE
    WHEN `inventory_redeemed` = 0 AND `redeemed_count` IS NOT NULL THEN `redeemed_count`
    ELSE `inventory_redeemed`
  END,
  `publish_start_at` = COALESCE(`publish_start_at`, `start_time`),
  `publish_end_at` = COALESCE(`publish_end_at`, `end_time`),
  `status` = CASE
    WHEN `status` IN ('active', 'published') THEN 'published'
    WHEN `status` IN ('inactive', 'draft') THEN 'draft'
    WHEN `status` = 'archived' THEN 'archived'
    ELSE 'draft'
  END,
  `sort_order` = CASE
    WHEN `sort_order` = 0 THEN `id`
    ELSE `sort_order`
  END;

ALTER TABLE rewards
  MODIFY COLUMN `code` VARCHAR(64) NOT NULL;

ALTER TABLE rewards
  ADD UNIQUE KEY `uk_rewards_code` (`code`),
  ADD KEY `idx_rewards_status_sort` (`status`, `sort_order`),
  ADD KEY `idx_rewards_publish_window` (`publish_start_at`, `publish_end_at`);

-- Phase 9 spatial hierarchy rebuild
ALTER TABLE cities
  ADD COLUMN `source_coordinate_system` VARCHAR(32) NOT NULL DEFAULT 'GCJ02' AFTER `country_code`,
  ADD COLUMN `source_center_lat` DECIMAL(10,7) NULL AFTER `source_coordinate_system`,
  ADD COLUMN `source_center_lng` DECIMAL(10,7) NULL AFTER `source_center_lat`,
  ADD COLUMN `popup_config_json` JSON NULL AFTER `description_pt`,
  ADD COLUMN `display_config_json` JSON NULL AFTER `popup_config_json`;

UPDATE cities
SET
  `source_coordinate_system` = COALESCE(NULLIF(`source_coordinate_system`, ''), 'GCJ02'),
  `source_center_lat` = COALESCE(`source_center_lat`, `center_lat`),
  `source_center_lng` = COALESCE(`source_center_lng`, `center_lng`);

CREATE TABLE IF NOT EXISTS sub_maps (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `city_id` BIGINT NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `name_zh` VARCHAR(128) NOT NULL,
  `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  `name_pt` VARCHAR(128) NOT NULL DEFAULT '',
  `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `cover_asset_id` BIGINT NULL,
  `source_coordinate_system` VARCHAR(32) NOT NULL DEFAULT 'GCJ02',
  `source_center_lat` DECIMAL(10,7) NULL,
  `source_center_lng` DECIMAL(10,7) NULL,
  `center_lat` DECIMAL(10,7) NULL,
  `center_lng` DECIMAL(10,7) NULL,
  `bounds_json` JSON NULL,
  `popup_config_json` JSON NULL,
  `display_config_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_sub_maps_code` (`code`),
  KEY `idx_sub_maps_city_status_sort` (`city_id`, `status`, `sort_order`),
  KEY `idx_sub_maps_cover_asset_id` (`cover_asset_id`),
  CONSTRAINT `fk_sub_maps_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE pois
  ADD COLUMN `sub_map_id` BIGINT NULL AFTER `city_id`,
  ADD COLUMN `source_coordinate_system` VARCHAR(32) NOT NULL DEFAULT 'GCJ02' AFTER `address_pt`,
  ADD COLUMN `source_latitude` DECIMAL(10,7) NULL AFTER `source_coordinate_system`,
  ADD COLUMN `source_longitude` DECIMAL(10,7) NULL AFTER `source_latitude`,
  ADD COLUMN `map_icon_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `popup_config_json` JSON NULL AFTER `intro_summary_pt`,
  ADD COLUMN `display_config_json` JSON NULL AFTER `popup_config_json`;

UPDATE pois
SET
  `source_coordinate_system` = COALESCE(NULLIF(`source_coordinate_system`, ''), 'GCJ02'),
  `source_latitude` = COALESCE(`source_latitude`, `latitude`),
  `source_longitude` = COALESCE(`source_longitude`, `longitude`);

ALTER TABLE pois
  ADD KEY `idx_pois_sub_map_id` (`sub_map_id`),
  ADD KEY `idx_pois_map_icon_asset_id` (`map_icon_asset_id`),
  ADD CONSTRAINT `fk_pois_sub_map` FOREIGN KEY (`sub_map_id`) REFERENCES `sub_maps` (`id`);

CREATE TABLE IF NOT EXISTS content_asset_links (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `entity_type` VARCHAR(32) NOT NULL,
  `entity_id` BIGINT NOT NULL,
  `usage_type` VARCHAR(32) NOT NULL,
  `asset_id` BIGINT NOT NULL,
  `title_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `title_en` VARCHAR(255) NOT NULL DEFAULT '',
  `title_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `title_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `description_zh` TEXT NULL,
  `description_en` TEXT NULL,
  `description_zht` TEXT NULL,
  `description_pt` TEXT NULL,
  `display_config_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  KEY `idx_content_asset_links_entity_status_sort` (`entity_type`, `entity_id`, `status`, `sort_order`),
  KEY `idx_content_asset_links_usage_type` (`usage_type`),
  KEY `idx_content_asset_links_asset_id` (`asset_id`),
  CONSTRAINT `fk_content_asset_links_asset` FOREIGN KEY (`asset_id`) REFERENCES `content_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
