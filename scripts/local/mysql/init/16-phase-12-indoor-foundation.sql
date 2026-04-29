USE `aoxiaoyou`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `buildings`
  ADD COLUMN `city_id` BIGINT NULL AFTER `city_code`,
  ADD COLUMN `sub_map_id` BIGINT NULL AFTER `city_id`,
  ADD COLUMN `binding_mode` VARCHAR(16) NOT NULL DEFAULT 'map' AFTER `sub_map_id`,
  ADD COLUMN `name_en` VARCHAR(256) NULL AFTER `name_zh`,
  ADD COLUMN `name_zht` VARCHAR(256) NULL AFTER `name_en`,
  ADD COLUMN `name_pt` VARCHAR(256) NULL AFTER `name_zht`,
  ADD COLUMN `address_en` VARCHAR(512) NULL AFTER `address_zh`,
  ADD COLUMN `address_zht` VARCHAR(512) NULL AFTER `address_en`,
  ADD COLUMN `address_pt` VARCHAR(512) NULL AFTER `address_zht`,
  ADD COLUMN `source_coordinate_system` VARCHAR(16) NOT NULL DEFAULT 'GCJ02' AFTER `address_pt`,
  ADD COLUMN `source_latitude` DECIMAL(10,6) NULL AFTER `source_coordinate_system`,
  ADD COLUMN `source_longitude` DECIMAL(11,6) NULL AFTER `source_latitude`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `cover_image_url`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `description_pt` TEXT NULL AFTER `description_zht`,
  ADD COLUMN `popup_config_json` JSON NULL AFTER `description_pt`,
  ADD COLUMN `display_config_json` JSON NULL AFTER `popup_config_json`,
  ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `display_config_json`,
  ADD COLUMN `published_at` DATETIME NULL AFTER `sort_order`,
  MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '狀態';

UPDATE `buildings` b
LEFT JOIN `cities` c ON c.`code` = b.`city_code`
SET
  b.`city_id` = COALESCE(b.`city_id`, c.`id`),
  b.`binding_mode` = CASE WHEN b.`poi_id` IS NULL THEN 'map' ELSE 'poi' END,
  b.`name_en` = COALESCE(NULLIF(b.`name_en`, ''), b.`name_zh`),
  b.`name_zht` = COALESCE(NULLIF(b.`name_zht`, ''), b.`name_zh`),
  b.`name_pt` = COALESCE(NULLIF(b.`name_pt`, ''), b.`name_zh`),
  b.`address_en` = COALESCE(NULLIF(b.`address_en`, ''), b.`address_zh`),
  b.`address_zht` = COALESCE(NULLIF(b.`address_zht`, ''), b.`address_zh`),
  b.`address_pt` = COALESCE(NULLIF(b.`address_pt`, ''), b.`address_zh`),
  b.`source_latitude` = COALESCE(b.`source_latitude`, b.`lat`),
  b.`source_longitude` = COALESCE(b.`source_longitude`, b.`lng`),
  b.`status` = CASE
    WHEN b.`status` IN ('1', 'published') THEN 'published'
    WHEN b.`status` IN ('0', 'archived') THEN 'archived'
    ELSE 'draft'
  END;

ALTER TABLE `buildings`
  ADD KEY `idx_buildings_city_id` (`city_id`),
  ADD KEY `idx_buildings_sub_map_id` (`sub_map_id`),
  ADD KEY `idx_buildings_poi_id` (`poi_id`),
  ADD KEY `idx_buildings_binding_mode` (`binding_mode`);

ALTER TABLE `indoor_floors`
  MODIFY COLUMN `indoor_map_id` BIGINT NULL COMMENT '關聯室內地圖',
  ADD COLUMN `floor_code` VARCHAR(64) NULL AFTER `indoor_map_id`,
  ADD COLUMN `floor_name_en` VARCHAR(128) NULL AFTER `floor_name_zh`,
  ADD COLUMN `floor_name_zht` VARCHAR(128) NULL AFTER `floor_name_en`,
  ADD COLUMN `floor_name_pt` VARCHAR(128) NULL AFTER `floor_name_zht`,
  ADD COLUMN `description_zh` TEXT NULL AFTER `floor_name_pt`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `description_pt` TEXT NULL AFTER `description_zht`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `floor_plan_url`,
  ADD COLUMN `floor_plan_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `area_sqm` DECIMAL(12,2) NULL AFTER `altitude_meters`,
  ADD COLUMN `zoom_min` DECIMAL(6,2) NULL AFTER `area_sqm`,
  ADD COLUMN `zoom_max` DECIMAL(6,2) NULL AFTER `zoom_min`,
  ADD COLUMN `default_zoom` DECIMAL(6,2) NULL AFTER `zoom_max`,
  ADD COLUMN `popup_config_json` JSON NULL AFTER `default_zoom`,
  ADD COLUMN `display_config_json` JSON NULL AFTER `popup_config_json`,
  ADD COLUMN `published_at` DATETIME NULL AFTER `sort_order`,
  MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '狀態';

UPDATE `indoor_floors`
SET
  `floor_code` = COALESCE(
    NULLIF(`floor_code`, ''),
    CASE
      WHEN `floor_number` < 0 THEN CONCAT('B', ABS(`floor_number`))
      WHEN `floor_number` = 0 THEN 'G'
      ELSE CONCAT('F', `floor_number`)
    END
  ),
  `floor_name_en` = COALESCE(NULLIF(`floor_name_en`, ''), `floor_name_zh`),
  `floor_name_zht` = COALESCE(NULLIF(`floor_name_zht`, ''), `floor_name_zh`),
  `floor_name_pt` = COALESCE(NULLIF(`floor_name_pt`, ''), `floor_name_zh`),
  `zoom_min` = COALESCE(`zoom_min`, 0.50),
  `zoom_max` = COALESCE(`zoom_max`, 2.50),
  `default_zoom` = COALESCE(`default_zoom`, 1.00),
  `sort_order` = COALESCE(`sort_order`, `floor_number`),
  `status` = CASE
    WHEN `status` IN ('1', 'published') THEN 'published'
    WHEN `status` IN ('0', 'archived') THEN 'archived'
    ELSE 'draft'
  END;

ALTER TABLE `indoor_floors`
  ADD UNIQUE KEY `uk_indoor_floors_building_floor_number` (`building_id`, `floor_number`),
  ADD KEY `idx_indoor_floors_status` (`status`);

SET FOREIGN_KEY_CHECKS = 1;
