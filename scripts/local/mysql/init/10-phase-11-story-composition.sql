USE `aoxiaoyou`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `content_relation_links` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `owner_type` VARCHAR(32) NOT NULL,
  `owner_id` BIGINT NOT NULL,
  `relation_type` VARCHAR(64) NOT NULL,
  `target_type` VARCHAR(32) NOT NULL,
  `target_id` BIGINT NULL,
  `target_code` VARCHAR(128) NOT NULL DEFAULT '',
  `metadata_json` JSON NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_content_relation_links_owner_target` (`owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `deleted`),
  KEY `idx_content_relation_links_owner` (`owner_type`, `owner_id`, `relation_type`, `sort_order`),
  KEY `idx_content_relation_links_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `story_chapters`
  ADD COLUMN `anchor_type` VARCHAR(32) NOT NULL DEFAULT 'manual' AFTER `media_asset_id`,
  ADD COLUMN `anchor_target_id` BIGINT NULL AFTER `anchor_type`,
  ADD COLUMN `anchor_target_code` VARCHAR(64) NOT NULL DEFAULT '' AFTER `anchor_target_id`,
  ADD COLUMN `prerequisite_json` JSON NULL AFTER `unlock_param_json`,
  ADD COLUMN `completion_json` JSON NULL AFTER `prerequisite_json`,
  ADD COLUMN `reward_json` JSON NULL AFTER `completion_json`;

INSERT INTO `content_relation_links` (
  `owner_type`,
  `owner_id`,
  `relation_type`,
  `target_type`,
  `target_id`,
  `target_code`,
  `metadata_json`,
  `sort_order`
)
SELECT
  'storyline',
  s.`id`,
  'city_binding',
  'city',
  s.`city_id`,
  c.`code`,
  NULL,
  0
FROM `storylines` s
LEFT JOIN `cities` c ON c.`id` = s.`city_id`
WHERE s.`city_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `content_relation_links` links
    WHERE links.`owner_type` = 'storyline'
      AND links.`owner_id` = s.`id`
      AND links.`relation_type` = 'city_binding'
      AND links.`target_type` = 'city'
      AND links.`target_id` = s.`city_id`
      AND links.`deleted` = 0
  );

SET FOREIGN_KEY_CHECKS = 1;
