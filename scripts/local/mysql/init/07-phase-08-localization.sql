USE `aoxiaoyou`;

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS `ensure_column`;

DELIMITER $$

CREATE PROCEDURE `ensure_column`(
  IN p_table_name VARCHAR(128),
  IN p_column_name VARCHAR(128),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `',
      p_table_name,
      '` ADD COLUMN `',
      p_column_name,
      '` ',
      p_definition
    );
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

DELIMITER ;

CALL ensure_column('app_runtime_settings', 'title_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `title_zht`');
CALL ensure_column('app_runtime_settings', 'description_pt', 'TEXT NULL AFTER `description_zht`');

CALL ensure_column('cities', 'name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `name_zht`');
CALL ensure_column('cities', 'subtitle_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `subtitle_zht`');
CALL ensure_column('cities', 'description_pt', 'TEXT NULL AFTER `description_zht`');

CALL ensure_column('storylines', 'name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `name_zht`');
CALL ensure_column('storylines', 'description_pt', 'TEXT NULL AFTER `description_zht`');
CALL ensure_column('storylines', 'reward_badge_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `reward_badge_zht`');

CALL ensure_column('pois', 'name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `name_zht`');
CALL ensure_column('pois', 'subtitle_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `subtitle_zht`');
CALL ensure_column('pois', 'address_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `address_zht`');
CALL ensure_column('pois', 'district_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `district_zht`');
CALL ensure_column('pois', 'description_pt', 'TEXT NULL AFTER `description_zht`');
CALL ensure_column('pois', 'intro_title_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `intro_title_zht`');
CALL ensure_column('pois', 'intro_summary_pt', 'TEXT NULL AFTER `intro_summary_zht`');

CALL ensure_column('story_chapters', 'title_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `title_zht`');
CALL ensure_column('story_chapters', 'summary_pt', 'TEXT NULL AFTER `summary_zht`');
CALL ensure_column('story_chapters', 'detail_pt', 'LONGTEXT NULL AFTER `detail_zht`');
CALL ensure_column('story_chapters', 'achievement_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `achievement_zht`');
CALL ensure_column('story_chapters', 'collectible_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `collectible_zht`');
CALL ensure_column('story_chapters', 'location_name_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `location_name_zht`');

CALL ensure_column('tip_articles', 'title_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `title_zht`');
CALL ensure_column('tip_articles', 'summary_pt', 'TEXT NULL AFTER `summary_zht`');
CALL ensure_column('tip_articles', 'content_pt', 'LONGTEXT NULL AFTER `content_zht`');
CALL ensure_column('tip_articles', 'location_name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `location_name_zht`');

CALL ensure_column('rewards', 'name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `name_zht`');
CALL ensure_column('rewards', 'subtitle_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `subtitle_zht`');
CALL ensure_column('rewards', 'description_pt', 'TEXT NULL AFTER `description_zht`');
CALL ensure_column('rewards', 'highlight_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `highlight_zht`');

CALL ensure_column('stamps', 'name_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `name_zht`');
CALL ensure_column('stamps', 'description_pt', 'TEXT NULL AFTER `description_zht`');

CALL ensure_column('notifications', 'title_pt', 'VARCHAR(255) NOT NULL DEFAULT '''' AFTER `title_zht`');
CALL ensure_column('notifications', 'content_pt', 'TEXT NULL AFTER `content_zht`');

CALL ensure_column('user_profiles', 'title_pt', 'VARCHAR(128) NOT NULL DEFAULT '''' AFTER `title_zht`');

UPDATE `app_runtime_settings`
SET
  `title_pt` = COALESCE(NULLIF(`title_pt`, ''), NULLIF(`title_en`, ''), NULLIF(`title_zht`, ''), NULLIF(`title_zh`, ''), ''),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`);

UPDATE `cities`
SET
  `name_pt` = COALESCE(NULLIF(`name_pt`, ''), NULLIF(`name_en`, ''), NULLIF(`name_zht`, ''), `name_zh`),
  `subtitle_pt` = COALESCE(NULLIF(`subtitle_pt`, ''), NULLIF(`subtitle_en`, ''), NULLIF(`subtitle_zht`, ''), `subtitle_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`);

UPDATE `storylines`
SET
  `name_pt` = COALESCE(NULLIF(`name_pt`, ''), NULLIF(`name_en`, ''), NULLIF(`name_zht`, ''), `name_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`),
  `reward_badge_pt` = COALESCE(NULLIF(`reward_badge_pt`, ''), NULLIF(`reward_badge_en`, ''), NULLIF(`reward_badge_zht`, ''), `reward_badge_zh`);

UPDATE `pois`
SET
  `name_pt` = COALESCE(NULLIF(`name_pt`, ''), NULLIF(`name_en`, ''), NULLIF(`name_zht`, ''), `name_zh`),
  `subtitle_pt` = COALESCE(NULLIF(`subtitle_pt`, ''), NULLIF(`subtitle_en`, ''), NULLIF(`subtitle_zht`, ''), `subtitle_zh`),
  `address_pt` = COALESCE(NULLIF(`address_pt`, ''), NULLIF(`address_en`, ''), NULLIF(`address_zht`, ''), `address_zh`),
  `district_pt` = COALESCE(NULLIF(`district_pt`, ''), NULLIF(`district_en`, ''), NULLIF(`district_zht`, ''), `district_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`),
  `intro_title_pt` = COALESCE(NULLIF(`intro_title_pt`, ''), NULLIF(`intro_title_en`, ''), NULLIF(`intro_title_zht`, ''), `intro_title_zh`),
  `intro_summary_pt` = COALESCE(NULLIF(`intro_summary_pt`, ''), NULLIF(`intro_summary_en`, ''), NULLIF(`intro_summary_zht`, ''), `intro_summary_zh`);

UPDATE `story_chapters`
SET
  `title_pt` = COALESCE(NULLIF(`title_pt`, ''), NULLIF(`title_en`, ''), NULLIF(`title_zht`, ''), `title_zh`),
  `summary_pt` = COALESCE(NULLIF(`summary_pt`, ''), NULLIF(`summary_en`, ''), NULLIF(`summary_zht`, ''), `summary_zh`),
  `detail_pt` = COALESCE(NULLIF(`detail_pt`, ''), NULLIF(`detail_en`, ''), NULLIF(`detail_zht`, ''), `detail_zh`),
  `achievement_pt` = COALESCE(NULLIF(`achievement_pt`, ''), NULLIF(`achievement_en`, ''), NULLIF(`achievement_zht`, ''), `achievement_zh`),
  `collectible_pt` = COALESCE(NULLIF(`collectible_pt`, ''), NULLIF(`collectible_en`, ''), NULLIF(`collectible_zht`, ''), `collectible_zh`),
  `location_name_pt` = COALESCE(NULLIF(`location_name_pt`, ''), NULLIF(`location_name_en`, ''), NULLIF(`location_name_zht`, ''), `location_name_zh`);

UPDATE `tip_articles`
SET
  `title_pt` = COALESCE(NULLIF(`title_pt`, ''), NULLIF(`title_en`, ''), NULLIF(`title_zht`, ''), `title_zh`),
  `summary_pt` = COALESCE(NULLIF(`summary_pt`, ''), NULLIF(`summary_en`, ''), NULLIF(`summary_zht`, ''), `summary_zh`),
  `content_pt` = COALESCE(NULLIF(`content_pt`, ''), NULLIF(`content_en`, ''), NULLIF(`content_zht`, ''), `content_zh`),
  `location_name_pt` = COALESCE(NULLIF(`location_name_pt`, ''), NULLIF(`location_name_en`, ''), NULLIF(`location_name_zht`, ''), `location_name_zh`);

UPDATE `rewards`
SET
  `name_pt` = COALESCE(NULLIF(`name_pt`, ''), NULLIF(`name_en`, ''), NULLIF(`name_zht`, ''), `name_zh`),
  `subtitle_pt` = COALESCE(NULLIF(`subtitle_pt`, ''), NULLIF(`subtitle_en`, ''), NULLIF(`subtitle_zht`, ''), `subtitle_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`),
  `highlight_pt` = COALESCE(NULLIF(`highlight_pt`, ''), NULLIF(`highlight_en`, ''), NULLIF(`highlight_zht`, ''), `highlight_zh`);

UPDATE `stamps`
SET
  `name_pt` = COALESCE(NULLIF(`name_pt`, ''), NULLIF(`name_en`, ''), NULLIF(`name_zht`, ''), `name_zh`),
  `description_pt` = COALESCE(NULLIF(`description_pt`, ''), NULLIF(`description_en`, ''), NULLIF(`description_zht`, ''), `description_zh`);

UPDATE `notifications`
SET
  `title_pt` = COALESCE(NULLIF(`title_pt`, ''), NULLIF(`title_en`, ''), NULLIF(`title_zht`, ''), `title_zh`),
  `content_pt` = COALESCE(NULLIF(`content_pt`, ''), NULLIF(`content_en`, ''), NULLIF(`content_zht`, ''), `content_zh`);

UPDATE `user_profiles`
SET
  `title_pt` = COALESCE(NULLIF(`title_pt`, ''), NULLIF(`title_en`, ''), NULLIF(`title_zht`, ''), `title_zh`);

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `deleted`, `_openid`)
VALUES
  ('translation.primary_authoring_locale', 'zh-Hant', 'string', 'Primary authoring locale for multilingual content', 0, ''),
  ('translation.engine_priority', '["bing","google","tencent"]', 'json', 'Ordered translation engine priority for operator-triggered machine translation', 0, ''),
  ('translation.overwrite_filled_locales', 'false', 'boolean', 'Whether one-click translation overwrites already filled locale fields by default', 0, '')
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `config_type` = VALUES(`config_type`),
  `description` = VALUES(`description`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

DROP PROCEDURE IF EXISTS `ensure_column`;
