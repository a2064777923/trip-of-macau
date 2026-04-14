-- Phase 2 admin domain alignment for legacy city / storyline / POI tables.
-- This upgrades the local brownfield schema to the canonical column contract
-- expected by the real admin APIs while preserving legacy columns for readers
-- that have not been migrated yet.

ALTER TABLE cities
  ADD COLUMN `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `name_zht`,
  ADD COLUMN `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_zh`,
  ADD COLUMN `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_en`,
  ADD COLUMN `unlock_condition_json` JSON NULL AFTER `unlock_type`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `unlock_condition_json`,
  ADD COLUMN `banner_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 AFTER `updated_at`;

ALTER TABLE cities
  MODIFY COLUMN `code` VARCHAR(64) NOT NULL,
  MODIFY COLUMN `name_zh` VARCHAR(128) NOT NULL,
  MODIFY COLUMN `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `country_code` VARCHAR(16) NOT NULL DEFAULT '',
  MODIFY COLUMN `unlock_type` VARCHAR(32) NOT NULL DEFAULT 'default',
  MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  MODIFY COLUMN `sort_order` INT NOT NULL DEFAULT 0;

UPDATE cities
SET
  `name_en` = COALESCE(NULLIF(`name_en`, ''), `name_zh`),
  `name_zht` = COALESCE(NULLIF(`name_zht`, ''), `name_zh`),
  `country_code` = COALESCE(NULLIF(`country_code`, ''), 'MO'),
  `unlock_type` = CASE
    WHEN `unlock_type` IS NULL OR `unlock_type` = '' THEN 'default'
    ELSE `unlock_type`
  END,
  `unlock_condition_json` = COALESCE(`unlock_condition_json`, `unlock_condition`),
  `description_en` = COALESCE(`description_en`, `description_zh`),
  `description_zht` = COALESCE(`description_zht`, `description_zh`),
  `status` = CASE
    WHEN `status` IN ('1', 'published') THEN 'published'
    WHEN `status` IN ('2', 'archived') THEN 'archived'
    ELSE 'draft'
  END;

SET @default_city_id = (
  SELECT id
  FROM cities
  WHERE code = 'macau'
  ORDER BY id
  LIMIT 1
);

INSERT INTO storylines (
  `id`,
  `city_id`,
  `code`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `estimated_minutes`,
  `difficulty`,
  `cover_asset_id`,
  `banner_asset_id`,
  `reward_badge_zh`,
  `reward_badge_en`,
  `reward_badge_zht`,
  `status`,
  `sort_order`,
  `published_at`,
  `created_at`,
  `updated_at`,
  `deleted`
)
SELECT
  sl.`id`,
  @default_city_id,
  sl.`code`,
  sl.`name_zh`,
  COALESCE(NULLIF(sl.`name_en`, ''), sl.`name_zh`),
  sl.`name_zh`,
  sl.`description`,
  sl.`description`,
  sl.`description`,
  COALESCE(sl.`estimated_duration_minutes`, 0),
  COALESCE(NULLIF(sl.`difficulty`, ''), 'easy'),
  NULL,
  NULL,
  '',
  '',
  '',
  CASE
    WHEN sl.`status` IN ('published', 'active') THEN 'published'
    WHEN sl.`status` = 'archived' THEN 'archived'
    ELSE 'draft'
  END,
  sl.`id`,
  sl.`publish_at`,
  sl.`created_at`,
  sl.`updated_at`,
  COALESCE(sl.`deleted`, 0)
FROM story_lines sl
WHERE NOT EXISTS (
  SELECT 1
  FROM storylines s
  WHERE s.`id` = sl.`id`
     OR s.`code` = sl.`code`
);

ALTER TABLE pois
  ADD COLUMN `city_id` BIGINT NULL AFTER `id`,
  ADD COLUMN `storyline_id` BIGINT NULL AFTER `city_id`,
  ADD COLUMN `code` VARCHAR(64) NULL AFTER `storyline_id`,
  ADD COLUMN `subtitle_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `name_zht`,
  ADD COLUMN `subtitle_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_zh`,
  ADD COLUMN `subtitle_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_en`,
  ADD COLUMN `address_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `subtitle_zht`,
  ADD COLUMN `address_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `address_zh`,
  ADD COLUMN `address_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `address_en`,
  ADD COLUMN `manual_checkin_radius` INT NOT NULL DEFAULT 200 AFTER `trigger_radius`,
  ADD COLUMN `stay_seconds` INT NOT NULL DEFAULT 30 AFTER `manual_checkin_radius`,
  ADD COLUMN `category_code` VARCHAR(64) NOT NULL DEFAULT '' AFTER `stay_seconds`,
  ADD COLUMN `district_zh` VARCHAR(128) NOT NULL DEFAULT '' AFTER `difficulty`,
  ADD COLUMN `district_en` VARCHAR(128) NOT NULL DEFAULT '' AFTER `district_zh`,
  ADD COLUMN `district_zht` VARCHAR(128) NOT NULL DEFAULT '' AFTER `district_en`,
  ADD COLUMN `cover_asset_id` BIGINT NULL AFTER `district_zht`,
  ADD COLUMN `audio_asset_id` BIGINT NULL AFTER `cover_asset_id`,
  ADD COLUMN `description_zh` TEXT NULL AFTER `audio_asset_id`,
  ADD COLUMN `description_en` TEXT NULL AFTER `description_zh`,
  ADD COLUMN `description_zht` TEXT NULL AFTER `description_en`,
  ADD COLUMN `intro_title_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `description_zht`,
  ADD COLUMN `intro_title_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `intro_title_zh`,
  ADD COLUMN `intro_title_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `intro_title_en`,
  ADD COLUMN `intro_summary_zh` TEXT NULL AFTER `intro_title_zht`,
  ADD COLUMN `intro_summary_en` TEXT NULL AFTER `intro_summary_zh`,
  ADD COLUMN `intro_summary_zht` TEXT NULL AFTER `intro_summary_en`,
  ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `status`,
  ADD COLUMN `published_at` DATETIME NULL AFTER `sort_order`;

ALTER TABLE pois
  MODIFY COLUMN `name_en` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `name_zht` VARCHAR(128) NOT NULL DEFAULT '',
  MODIFY COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  MODIFY COLUMN `_openid` VARCHAR(256) NOT NULL DEFAULT '';

UPDATE pois p
LEFT JOIN cities c
  ON c.`code` = p.`city_code`
SET
  p.`city_id` = COALESCE(p.`city_id`, c.`id`, @default_city_id),
  p.`storyline_id` = COALESCE(p.`storyline_id`, p.`story_line_id`),
  p.`code` = COALESCE(NULLIF(p.`code`, ''), CONCAT('legacy_poi_', p.`id`)),
  p.`name_en` = COALESCE(NULLIF(p.`name_en`, ''), p.`name_zh`),
  p.`name_zht` = COALESCE(NULLIF(p.`name_zht`, ''), p.`name_zh`),
  p.`subtitle_zh` = COALESCE(NULLIF(p.`subtitle_zh`, ''), p.`subtitle`, ''),
  p.`subtitle_en` = COALESCE(NULLIF(p.`subtitle_en`, ''), p.`subtitle_zh`, p.`subtitle`, ''),
  p.`subtitle_zht` = COALESCE(NULLIF(p.`subtitle_zht`, ''), p.`subtitle_zh`, p.`subtitle`, ''),
  p.`address_zh` = COALESCE(NULLIF(p.`address_zh`, ''), p.`address`, ''),
  p.`address_en` = COALESCE(NULLIF(p.`address_en`, ''), p.`address_zh`, p.`address`, ''),
  p.`address_zht` = COALESCE(NULLIF(p.`address_zht`, ''), p.`address_zh`, p.`address`, ''),
  p.`manual_checkin_radius` = CASE
    WHEN p.`manual_checkin_radius` = 200 THEN GREATEST(COALESCE(p.`trigger_radius`, 50) * 4, 200)
    ELSE p.`manual_checkin_radius`
  END,
  p.`stay_seconds` = CASE
    WHEN p.`stay_seconds` = 30 THEN 30
    ELSE p.`stay_seconds`
  END,
  p.`category_code` = COALESCE(NULLIF(p.`category_code`, ''), p.`poi_type`, ''),
  p.`district_zh` = COALESCE(NULLIF(p.`district_zh`, ''), p.`region_code`, ''),
  p.`district_en` = COALESCE(NULLIF(p.`district_en`, ''), p.`district_zh`, p.`region_code`, ''),
  p.`district_zht` = COALESCE(NULLIF(p.`district_zht`, ''), p.`district_zh`, p.`region_code`, ''),
  p.`description_zh` = COALESCE(NULLIF(p.`description_zh`, ''), p.`description`),
  p.`description_en` = COALESCE(NULLIF(p.`description_en`, ''), p.`description_zh`, p.`description`),
  p.`description_zht` = COALESCE(NULLIF(p.`description_zht`, ''), p.`description_zh`, p.`description`),
  p.`sort_order` = CASE
    WHEN p.`sort_order` = 0 THEN p.`id`
    ELSE p.`sort_order`
  END,
  p.`published_at` = COALESCE(p.`published_at`, p.`created_at`),
  p.`status` = CASE
    WHEN p.`status` IN ('published', 'active') THEN 'published'
    WHEN p.`status` = 'archived' THEN 'archived'
    ELSE 'draft'
  END;

ALTER TABLE pois
  MODIFY COLUMN `city_id` BIGINT NOT NULL,
  MODIFY COLUMN `code` VARCHAR(64) NOT NULL;

ALTER TABLE pois
  ADD UNIQUE KEY `uk_pois_city_code` (`city_id`, `code`),
  ADD KEY `idx_pois_status_sort` (`status`, `sort_order`),
  ADD KEY `idx_pois_city_id` (`city_id`),
  ADD KEY `idx_pois_storyline_id` (`storyline_id`);

ALTER TABLE story_chapters
  DROP FOREIGN KEY `story_chapters_ibfk_1`;

ALTER TABLE story_chapters
  DROP INDEX `idx_story_line`;

ALTER TABLE story_chapters
  CHANGE COLUMN `story_line_id` `storyline_id` BIGINT NOT NULL,
  MODIFY COLUMN `media_type` VARCHAR(32) NULL,
  MODIFY COLUMN `media_url` VARCHAR(256) NULL,
  MODIFY COLUMN `_openid` VARCHAR(256) NOT NULL DEFAULT '',
  ADD COLUMN `title_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `title_zh`,
  ADD COLUMN `title_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `title_en`,
  ADD COLUMN `summary_zh` TEXT NULL AFTER `title_zht`,
  ADD COLUMN `summary_en` TEXT NULL AFTER `summary_zh`,
  ADD COLUMN `summary_zht` TEXT NULL AFTER `summary_en`,
  ADD COLUMN `detail_zh` LONGTEXT NULL AFTER `summary_zht`,
  ADD COLUMN `detail_en` LONGTEXT NULL AFTER `detail_zh`,
  ADD COLUMN `detail_zht` LONGTEXT NULL AFTER `detail_en`,
  ADD COLUMN `achievement_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `detail_zht`,
  ADD COLUMN `achievement_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `achievement_zh`,
  ADD COLUMN `achievement_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `achievement_en`,
  ADD COLUMN `collectible_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `achievement_zht`,
  ADD COLUMN `collectible_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `collectible_zh`,
  ADD COLUMN `collectible_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `collectible_en`,
  ADD COLUMN `location_name_zh` VARCHAR(255) NOT NULL DEFAULT '' AFTER `collectible_zht`,
  ADD COLUMN `location_name_en` VARCHAR(255) NOT NULL DEFAULT '' AFTER `location_name_zh`,
  ADD COLUMN `location_name_zht` VARCHAR(255) NOT NULL DEFAULT '' AFTER `location_name_en`,
  ADD COLUMN `media_asset_id` BIGINT NULL AFTER `location_name_zht`,
  ADD COLUMN `unlock_param_json` JSON NULL AFTER `unlock_type`,
  ADD COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'draft' AFTER `unlock_param_json`,
  ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `status`,
  ADD COLUMN `published_at` DATETIME NULL AFTER `sort_order`,
  ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`,
  ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 AFTER `updated_at`;

UPDATE story_chapters
SET
  `title_en` = COALESCE(NULLIF(`title_en`, ''), `title_zh`),
  `title_zht` = COALESCE(NULLIF(`title_zht`, ''), `title_zh`),
  `summary_zh` = COALESCE(`summary_zh`, `script_zh`),
  `summary_en` = COALESCE(`summary_en`, `script_en`),
  `summary_zht` = COALESCE(`summary_zht`, `script_zht`),
  `detail_zh` = COALESCE(`detail_zh`, `script_zh`),
  `detail_en` = COALESCE(`detail_en`, `script_en`),
  `detail_zht` = COALESCE(`detail_zht`, `script_zht`),
  `sort_order` = CASE
    WHEN `sort_order` = 0 THEN `chapter_order`
    ELSE `sort_order`
  END,
  `published_at` = COALESCE(`published_at`, `created_at`),
  `status` = CASE
    WHEN `status` IN ('published', 'active') THEN 'published'
    WHEN `status` = 'archived' THEN 'archived'
    ELSE 'draft'
  END;

ALTER TABLE story_chapters
  ADD UNIQUE KEY `uk_story_chapters_storyline_order` (`storyline_id`, `chapter_order`),
  ADD KEY `idx_story_chapters_status_sort` (`status`, `sort_order`),
  ADD KEY `idx_story_chapters_storyline_id` (`storyline_id`),
  ADD CONSTRAINT `fk_story_chapters_storyline`
    FOREIGN KEY (`storyline_id`) REFERENCES `storylines` (`id`);
