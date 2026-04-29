USE `aoxiaoyou`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `collectibles`
  MODIFY COLUMN `collectible_type` VARCHAR(32) NOT NULL DEFAULT 'item',
  MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  MODIFY COLUMN `sort_order` INT NOT NULL DEFAULT 0,
  ADD COLUMN `name_zht` VARCHAR(128) NULL AFTER `name_en`,
  ADD COLUMN `name_pt` VARCHAR(128) NULL AFTER `name_zht`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `description_pt` TEXT NULL AFTER `description_zht`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `rarity`,
  ADD COLUMN `icon_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `animation_asset_id` BIGINT NULL AFTER `icon_asset_id`,
  ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 AFTER `updated_at`;

ALTER TABLE `badges`
  MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  ADD COLUMN `name_en` VARCHAR(128) NULL AFTER `name_zh`,
  ADD COLUMN `name_zht` VARCHAR(128) NULL AFTER `name_en`,
  ADD COLUMN `name_pt` VARCHAR(128) NULL AFTER `name_zht`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `description_pt` TEXT NULL AFTER `description_zht`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `rarity`,
  ADD COLUMN `icon_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `animation_asset_id` BIGINT NULL AFTER `icon_asset_id`,
  ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 AFTER `updated_at`;

UPDATE `collectibles`
SET `name_zht` = COALESCE(NULLIF(`name_zht`, ''), `name_zh`),
    `name_pt` = COALESCE(NULLIF(`name_pt`, ''), `name_en`),
    `description_zht` = COALESCE(NULLIF(`description_zht`, ''), `description_zh`),
    `description_pt` = COALESCE(NULLIF(`description_pt`, ''), `description_en`),
    `status` = CASE
      WHEN `status` IN ('1', 'published', 'active') THEN 'published'
      WHEN `status` IN ('0', 'archived', 'inactive') THEN 'archived'
      ELSE 'draft'
    END,
    `sort_order` = COALESCE(`sort_order`, 0);

UPDATE `badges`
SET `name_zht` = COALESCE(NULLIF(`name_zht`, ''), `name_zh`),
    `name_pt` = COALESCE(NULLIF(`name_pt`, ''), `name_en`),
    `description_zht` = COALESCE(NULLIF(`description_zht`, ''), `description_zh`),
    `description_pt` = COALESCE(NULLIF(`description_pt`, ''), `description_en`),
    `status` = CASE
      WHEN `status` IN ('1', 'published', 'active') THEN 'published'
      WHEN `status` IN ('0', 'archived', 'inactive') THEN 'archived'
      ELSE 'draft'
    END;

SET FOREIGN_KEY_CHECKS = 1;
