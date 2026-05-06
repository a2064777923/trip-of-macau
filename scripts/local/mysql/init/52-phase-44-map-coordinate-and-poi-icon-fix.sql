USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @phase44_icon_base_url = 'https://tripofmacau-1301163924.cos.ap-hongkong.myqcloud.com/miniapp/assets/icon/2026/05/06/zh-Hant';

-- Phase 44 runtime map correction.
-- WeChat mini-program native map consumes GCJ-02 coordinates. These early seed
-- Macau points were stored as GCJ-02 but the numbers were WGS84-like source
-- coordinates, causing an obvious offset on the mini-program map.

UPDATE `cities`
SET
  `source_coordinate_system` = 'WGS84',
  `source_center_lat` = 22.1987000,
  `source_center_lng` = 113.5439000,
  `center_lat` = 22.195761,
  `center_lng` = 113.549003,
  `updated_at` = NOW()
WHERE `code` = 'macau'
  AND COALESCE(`deleted`, 0) = 0;

UPDATE `sub_maps`
SET
  `source_coordinate_system` = 'WGS84',
  `source_center_lat` = 22.1987000,
  `source_center_lng` = 113.5439000,
  `center_lat` = 22.1957608,
  `center_lng` = 113.5490033,
  `updated_at` = NOW()
WHERE `code` = 'macau-peninsula'
  AND COALESCE(`deleted`, 0) = 0;

UPDATE `sub_maps`
SET
  `source_coordinate_system` = 'WGS84',
  `source_center_lat` = 22.1563000,
  `source_center_lng` = 113.5606000,
  `center_lat` = 22.1533437,
  `center_lng` = 113.5656599,
  `updated_at` = NOW()
WHERE `code` = 'taipa'
  AND COALESCE(`deleted`, 0) = 0;

UPDATE `sub_maps`
SET
  `source_coordinate_system` = 'WGS84',
  `source_center_lat` = 22.1197000,
  `source_center_lng` = 113.5695000,
  `center_lat` = 22.1167454,
  `center_lng` = 113.5745406,
  `updated_at` = NOW()
WHERE `code` = 'coloane'
  AND COALESCE(`deleted`, 0) = 0;

INSERT INTO `content_assets` (
  `id`, `asset_kind`, `bucket_name`, `region`, `object_key`, `canonical_url`, `mime_type`,
  `animation_subtype`, `poster_asset_id`, `fallback_asset_id`, `default_loop`, `default_autoplay`,
  `locale_code`, `original_filename`, `file_extension`, `upload_source`, `client_relative_path`, `uploaded_by_admin_name`,
  `file_size_bytes`, `width_px`, `height_px`, `checksum`, `etag`, `processing_policy_code`, `processing_status`, `processing_note`, `status`, `published_at`
) VALUES
  (333199, 'icon', 'tripofmacau-1301163924', 'ap-hongkong', 'miniapp/assets/icon/2026/05/06/zh-Hant/ama-temple-2-5d-image1-59618d2bd3ab.png', CONCAT(@phase44_icon_base_url, '/ama-temple-2-5d-image1-59618d2bd3ab.png'), 'image/png', NULL, NULL, NULL, 0, 0, 'zh-Hant', 'ama-temple-2_5d-image1.png', 'png', 'seed', 'local-content/phase44/poi-icons-image1/ama-temple-2_5d-image1.png', 'phase44-image1-icon-board', 232559, 512, 512, '0df543e0dc056e41109f71777affc124b0be286e98219f48f872ac312de08140', '46efc2b54918007019b665a234040210', 'image-compressed', 'processed', 'Image-1 sprite board POI marker sliced into transparent 2.5D icon for WeChat native map rendering.', 'published', NOW()),
  (333200, 'icon', 'tripofmacau-1301163924', 'ap-hongkong', 'miniapp/assets/icon/2026/05/06/zh-Hant/lilau-square-2-5d-image1-82a7f415203c.png', CONCAT(@phase44_icon_base_url, '/lilau-square-2-5d-image1-82a7f415203c.png'), 'image/png', NULL, NULL, NULL, 0, 0, 'zh-Hant', 'lilau-square-2_5d-image1.png', 'png', 'seed', 'local-content/phase44/poi-icons-image1/lilau-square-2_5d-image1.png', 'phase44-image1-icon-board', 225238, 512, 512, '83f1a717aa2e1befff8f80ac5b5d16060e153e8f3c2d16c804c91f5ece23a872', '50eac472767f4d40e0fca2776948d05b', 'image-compressed', 'processed', 'Image-1 sprite board POI marker sliced into transparent 2.5D icon for WeChat native map rendering.', 'published', NOW()),
  (333201, 'icon', 'tripofmacau-1301163924', 'ap-hongkong', 'miniapp/assets/icon/2026/05/06/zh-Hant/hill-watch-2-5d-image1-d409e5db367e.png', CONCAT(@phase44_icon_base_url, '/hill-watch-2-5d-image1-d409e5db367e.png'), 'image/png', NULL, NULL, NULL, 0, 0, 'zh-Hant', 'hill-watch-2_5d-image1.png', 'png', 'seed', 'local-content/phase44/poi-icons-image1/hill-watch-2_5d-image1.png', 'phase44-image1-icon-board', 244474, 512, 512, 'a0cb78ed90e9a36c844f4befa7573d4955ea35e2922021a465ae5b48c26d25fa', 'e3a79b72870a571bde0c44592cd8ec2a', 'image-compressed', 'processed', 'Image-1 sprite board POI marker sliced into transparent 2.5D icon for WeChat native map rendering.', 'published', NOW()),
  (333202, 'icon', 'tripofmacau-1301163924', 'ap-hongkong', 'miniapp/assets/icon/2026/05/06/zh-Hant/monte-fort-2-5d-image1-5fc5d85766b5.png', CONCAT(@phase44_icon_base_url, '/monte-fort-2-5d-image1-5fc5d85766b5.png'), 'image/png', NULL, NULL, NULL, 0, 0, 'zh-Hant', 'monte-fort-2_5d-image1.png', 'png', 'seed', 'local-content/phase44/poi-icons-image1/monte-fort-2_5d-image1.png', 'phase44-image1-icon-board', 201312, 512, 512, 'c6128d7440623135e7a6c3d1152792d467825a088863e53cc6d618ee5bf5a4b5', '144dc3787f3043c2d90b27d01b488526', 'image-compressed', 'processed', 'Image-1 sprite board POI marker sliced into transparent 2.5D icon for WeChat native map rendering.', 'published', NOW()),
  (333203, 'icon', 'tripofmacau-1301163924', 'ap-hongkong', 'miniapp/assets/icon/2026/05/06/zh-Hant/senado-square-2-5d-image1-6184936da5e5.png', CONCAT(@phase44_icon_base_url, '/senado-square-2-5d-image1-6184936da5e5.png'), 'image/png', NULL, NULL, NULL, 0, 0, 'zh-Hant', 'senado-square-2_5d-image1.png', 'png', 'seed', 'local-content/phase44/poi-icons-image1/senado-square-2_5d-image1.png', 'phase44-image1-icon-board', 252944, 512, 512, '463a9feec6ef701168997040a71afe1419b6a96749e18729c59b6dcd3589e1d8', '6c5ec9a5431ffbcce3609dbc01a91e6c', 'image-compressed', 'processed', 'Image-1 sprite board POI marker sliced into transparent 2.5D icon for WeChat native map rendering.', 'published', NOW())
ON DUPLICATE KEY UPDATE
  `asset_kind` = VALUES(`asset_kind`),
  `bucket_name` = VALUES(`bucket_name`),
  `region` = VALUES(`region`),
  `object_key` = VALUES(`object_key`),
  `canonical_url` = VALUES(`canonical_url`),
  `mime_type` = VALUES(`mime_type`),
  `locale_code` = VALUES(`locale_code`),
  `original_filename` = VALUES(`original_filename`),
  `file_extension` = VALUES(`file_extension`),
  `upload_source` = VALUES(`upload_source`),
  `uploaded_by_admin_name` = VALUES(`uploaded_by_admin_name`),
  `file_size_bytes` = VALUES(`file_size_bytes`),
  `width_px` = VALUES(`width_px`),
  `height_px` = VALUES(`height_px`),
  `checksum` = VALUES(`checksum`),
  `etag` = VALUES(`etag`),
  `processing_policy_code` = VALUES(`processing_policy_code`),
  `processing_status` = VALUES(`processing_status`),
  `processing_note` = VALUES(`processing_note`),
  `status` = VALUES(`status`),
  `published_at` = COALESCE(`published_at`, VALUES(`published_at`)),
  `deleted` = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_phase44_poi_coordinate_fix`;
CREATE TEMPORARY TABLE `tmp_phase44_poi_coordinate_fix` (
  `code` VARCHAR(64) PRIMARY KEY,
  `source_latitude` DECIMAL(10,7) NOT NULL,
  `source_longitude` DECIMAL(10,7) NOT NULL,
  `latitude` DECIMAL(10,7) NOT NULL,
  `longitude` DECIMAL(10,7) NOT NULL,
  `map_icon_asset_id` BIGINT NULL
) ENGINE=Memory;

INSERT INTO `tmp_phase44_poi_coordinate_fix`
  (`code`, `source_latitude`, `source_longitude`, `latitude`, `longitude`, `map_icon_asset_id`)
VALUES
  ('ruins_st_paul', 22.1975460, 113.5408540, 22.1946143, 113.5459657, NULL),
  ('st_dominic_church', 22.1928010, 113.5409360, 22.1898708, 113.5460472, NULL),
  ('hotel_lisboa', 22.1905500, 113.5440730, 22.1876133, 113.5491753, NULL),
  ('taipa_houses', 22.1563000, 113.5606000, 22.1533437, 113.5656599, NULL),
  ('hac_sa_beach', 22.1197000, 113.5695000, 22.1167454, 113.5745406, NULL),
  ('ama_temple', 22.1868400, 113.5315900, 22.1839351, 113.5367287, 333199),
  ('lilau_square', 22.1852450, 113.5373910, 22.1823260, 113.5425120, 333200),
  ('dom_pedro_v_theatre', 22.1913260, 113.5404180, 22.1883975, 113.5455306, 333201),
  ('monte_fort', 22.1974760, 113.5407570, 22.1945445, 113.5458690, 333202),
  ('senado_square', 22.1934650, 113.5392280, 22.1905386, 113.5443441, 333203);

UPDATE `pois` `p`
JOIN `tmp_phase44_poi_coordinate_fix` `f` ON `f`.`code` = `p`.`code`
SET
  `p`.`source_coordinate_system` = 'WGS84',
  `p`.`source_latitude` = `f`.`source_latitude`,
  `p`.`source_longitude` = `f`.`source_longitude`,
  `p`.`latitude` = `f`.`latitude`,
  `p`.`longitude` = `f`.`longitude`,
  `p`.`map_icon_asset_id` = COALESCE(`f`.`map_icon_asset_id`, `p`.`map_icon_asset_id`),
  `p`.`updated_at` = NOW()
WHERE `p`.`city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1)
  AND COALESCE(`p`.`deleted`, 0) = 0;

UPDATE `content_assets`
SET
  `status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW()),
  `updated_at` = NOW()
WHERE `id` IN (333199, 333200, 333201, 333202, 333203)
  AND COALESCE(`deleted`, 0) = 0;

UPDATE `content_assets`
SET
  `status` = 'archived',
  `deleted` = 1,
  `updated_at` = NOW()
WHERE `id` IN (333193, 333194, 333195, 333196, 333197, 333198)
  AND `id` NOT IN (
    SELECT DISTINCT `map_icon_asset_id`
    FROM `pois`
    WHERE `map_icon_asset_id` IS NOT NULL
      AND COALESCE(`deleted`, 0) = 0
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_phase44_poi_coordinate_fix`;
