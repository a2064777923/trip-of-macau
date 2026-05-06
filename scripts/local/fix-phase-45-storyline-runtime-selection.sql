-- Phase 45: safe duplicate-storyline runtime selection correction.
-- All text is UTF-8 / utf8mb4. This script is idempotent and never deletes rows.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

UPDATE `storylines`
SET
  `status` = 'archived',
  `published_at` = NULL,
  `sort_order` = GREATEST(COALESCE(`sort_order`, 0), 9000),
  `deleted` = 0,
  `updated_at` = NOW()
WHERE `code` = 'macau_fire_route';

UPDATE `storylines`
SET
  `status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW()),
  `sort_order` = 0,
  `deleted` = 0,
  `updated_at` = NOW()
WHERE `code` = 'east_west_war_and_coexistence';

SELECT
  `id`,
  `code`,
  `name_zht`,
  `status`,
  `sort_order`,
  `published_at`,
  `deleted`
FROM `storylines`
WHERE `code` IN ('macau_fire_route', 'east_west_war_and_coexistence')
ORDER BY `code`;
