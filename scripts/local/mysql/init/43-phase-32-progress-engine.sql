-- Phase 32 Plan 32-01: canonical dynamic progress engine foundation.
-- Implements D32-01, D32-03, D32-04, D32-07, D32-09, and D32-34.
-- All text is UTF-8 / utf8mb4. Do not rewrite this file through non-UTF-8 shell literals.
-- Canonical owner-backed scope tokens:
--   task -> experience_flow_step
--   collectible -> collectible
--   reward -> reward
--   media -> content_asset
-- No new traveler snapshot table is introduced here. user_exploration_state remains the only rebuildable cache table.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `column_name` = 'poi_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `exploration_elements` ADD COLUMN `poi_id` BIGINT NULL AFTER `sub_map_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `column_name` = 'indoor_building_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `exploration_elements` ADD COLUMN `indoor_building_id` BIGINT NULL AFTER `poi_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `column_name` = 'indoor_floor_id'
);
SET @ddl = IF(
  @column_exists = 0,
  'ALTER TABLE `exploration_elements` ADD COLUMN `indoor_floor_id` BIGINT NULL AFTER `indoor_building_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `index_name` = 'idx_exploration_elements_scope_poi'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `exploration_elements` ADD KEY `idx_exploration_elements_scope_poi` (`poi_id`, `status`, `include_in_exploration`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `index_name` = 'idx_exploration_elements_scope_indoor_building'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `exploration_elements` ADD KEY `idx_exploration_elements_scope_indoor_building` (`indoor_building_id`, `status`, `include_in_exploration`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`statistics`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'exploration_elements'
    AND `index_name` = 'idx_exploration_elements_scope_indoor_floor'
);
SET @ddl = IF(
  @index_exists = 0,
  'ALTER TABLE `exploration_elements` ADD KEY `idx_exploration_elements_scope_indoor_floor` (`indoor_floor_id`, `status`, `include_in_exploration`)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Scope resolution contract for D32-07 / D32-09:
--   global -> all published rows where include_in_exploration = 1
--   city -> city_id
--   sub_map -> sub_map_id
--   poi -> poi_id
--   indoor_building -> indoor_building_id
--   indoor_floor -> indoor_floor_id
--   storyline -> storyline_id
--   story_chapter -> story_chapter_id
--   task -> owner_type = 'experience_flow_step' and owner_id = {scopeId}
--   collectible -> owner_type = 'collectible' and owner_id = {scopeId}
--   reward -> owner_type = 'reward' and owner_id = {scopeId}
--   media -> owner_type = 'content_asset' and owner_id = {scopeId}
--
-- The owner_type / owner_id token contract above is canonical for owner-backed scopes.
-- Do not introduce parallel per-domain progress tables for task, collectible, reward, or media scopes.
