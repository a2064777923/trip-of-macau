USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @seed_openid = 'phase14-seed';
SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_macau_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @storyline_macau_fire_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @indoor_building_id = COALESCE(
  (SELECT `id` FROM `buildings` WHERE `building_code` = 'lisboeta_macau' LIMIT 1),
  (SELECT `id` FROM `buildings` WHERE `building_code` = 'lisboeta_demo' LIMIT 1),
  (SELECT `id` FROM `buildings` WHERE `building_code` = 'bldg_sjm_museum' LIMIT 1),
  (
    SELECT `b`.`id`
    FROM `buildings` `b`
    WHERE EXISTS (
      SELECT 1
      FROM `indoor_floors` `f`
      WHERE `f`.`building_id` = `b`.`id`
    )
    ORDER BY `b`.`id`
    LIMIT 1
  ),
  (SELECT `id` FROM `buildings` ORDER BY `id` LIMIT 1)
);
SET @indoor_building_code = COALESCE(
  (SELECT `building_code` FROM `buildings` WHERE `id` = @indoor_building_id LIMIT 1),
  'indoor-building'
);
SET @indoor_floor_id = COALESCE(
  (SELECT `id` FROM `indoor_floors` WHERE `building_id` = @indoor_building_id AND `floor_code` IN ('G', '1F', 'L1') ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `indoor_floors` WHERE `building_id` = @indoor_building_id ORDER BY `floor_number`, `id` LIMIT 1),
  (SELECT `id` FROM `indoor_floors` ORDER BY `id` LIMIT 1)
);
SET @indoor_floor_code = COALESCE(
  (SELECT `floor_code` FROM `indoor_floors` WHERE `id` = @indoor_floor_id LIMIT 1),
  'floor-1'
);

SET @old_collectible_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_lisboeta_night_pass' LIMIT 1);
SET @old_badge_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_lisboeta_pathfinder' LIMIT 1);
SET @old_reward_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_lisboeta_secret_cut' LIMIT 1);

DELETE FROM `content_relation_links`
WHERE (`owner_type` = 'collectible' AND `owner_id` = @old_collectible_id)
   OR (`owner_type` = 'badge' AND `owner_id` = @old_badge_id)
   OR (`owner_type` = 'reward' AND `owner_id` = @old_reward_id);

DELETE FROM `collectibles` WHERE `collectible_code` = 'collectible_lisboeta_night_pass';
DELETE FROM `badges` WHERE `badge_code` = 'badge_lisboeta_pathfinder';
DELETE FROM `rewards` WHERE `code` = 'reward_lisboeta_secret_cut';

INSERT INTO `collectibles` (
  `collectible_code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `collectible_type`, `rarity`, `cover_asset_id`, `icon_asset_id`, `animation_asset_id`,
  `series_id`, `acquisition_source`,
  `popup_preset_code`, `popup_config_json`,
  `display_preset_code`, `display_config_json`,
  `trigger_preset_code`, `trigger_config_json`,
  `example_content_zh`, `example_content_en`, `example_content_zht`, `example_content_pt`,
  `is_repeatable`, `is_limited`, `max_ownership`, `status`, `sort_order`, `_openid`
)
VALUES (
  'collectible_lisboeta_night_pass',
  '葡京人夜巡通行證',
  'Lisboeta Night Patrol Pass',
  '葡京人夜巡通行證',
  'Passe da patrulha noturna do Lisboeta',
  '完成室內夜巡支線後取得的限定通行證，會在室內地圖上高亮下一個互動點。',
  'A limited pass earned from the indoor night-patrol branch, highlighting the next indoor interaction.',
  '完成室內夜巡支線後取得的限定通行證，會在室內地圖上高亮下一個互動點。',
  'Passe limitado obtido na rota interna noturna, destacando o próximo ponto interativo.',
  'document',
  'epic',
  300010,
  300008,
  NULL,
  14,
  'indoor_storyline',
  'story-modal',
  JSON_OBJECT('title', '夜巡通行證已解鎖', 'body', '把這張通行證帶進室內地圖，可直接打開下一個支線互動。'),
  'map-keepsake',
  JSON_OBJECT('theme', 'neon-route', 'accent', 'amber', 'showLocationBinding', true),
  'poi-arrival',
  JSON_OBJECT('radiusMeters', 40, 'dwellSeconds', 12, 'requiresIndoorFloor', true),
  '作為「夜巡」支線的樣板收集物，可示範故事彈窗、室內綁定與地圖高亮一起運作。',
  'Showcase collectible for the night-patrol branch, combining modal storytelling, indoor binding, and map highlighting.',
  '作為「夜巡」支線的樣板收集物，可示範故事彈窗、室內綁定與地圖高亮一起運作。',
  'Exemplo de colecionável para a rota noturna, combinando modal narrativo, vínculo interno e destaque no mapa.',
  0,
  1,
  1,
  'published',
  30,
  @seed_openid
);

INSERT INTO `badges` (
  `badge_code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `cover_asset_id`, `icon_asset_id`, `animation_asset_id`,
  `badge_type`, `rarity`, `is_hidden`,
  `popup_preset_code`, `popup_config_json`,
  `display_preset_code`, `display_config_json`,
  `trigger_preset_code`, `trigger_config_json`,
  `example_content_zh`, `example_content_en`, `example_content_zht`, `example_content_pt`,
  `status`, `_openid`
)
VALUES (
  'badge_lisboeta_pathfinder',
  '夜巡引路人',
  'Night Route Pathfinder',
  '夜巡引路人',
  'Guia da rota noturna',
  '完成室內夜巡節點串聯後取得，代表玩家已掌握跨樓層路線。',
  'Unlocked after chaining the indoor night-patrol nodes, representing mastery of a cross-floor route.',
  '完成室內夜巡節點串聯後取得，代表玩家已掌握跨樓層路線。',
  'Desbloqueada após ligar os nós internos da rota noturna entre andares.',
  300009,
  300008,
  NULL,
  'collection',
  'legendary',
  0,
  'achievement-toast',
  JSON_OBJECT('title', '夜巡引路人', 'subtitle', '已完成跨樓層路線'),
  'badge-ribbon',
  JSON_OBJECT('theme', 'night-patrol', 'showUnlockTime', true),
  'chapter-completion',
  JSON_OBJECT('requiredChapters', 2, 'requiresIndoorBuilding', true),
  '這枚徽章示範室內建築 + 樓層綁定如何回顯到後台與前台獎勵系統。',
  'This badge demonstrates how indoor-building and floor bindings flow through admin and public rewards.',
  '這枚徽章示範室內建築 + 樓層綁定如何回顯到後台與前台獎勵系統。',
  'Esta insígnia demonstra como os vínculos de edifício e piso interno fluem pelo painel e pela app.',
  'published',
  @seed_openid
);

INSERT INTO `rewards` (
  `code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`, `subtitle_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `highlight_zh`, `highlight_en`, `highlight_zht`, `highlight_pt`,
  `stamp_cost`, `inventory_total`, `inventory_redeemed`, `cover_asset_id`,
  `popup_preset_code`, `popup_config_json`,
  `display_preset_code`, `display_config_json`,
  `trigger_preset_code`, `trigger_config_json`,
  `example_content_zh`, `example_content_en`, `example_content_zht`, `example_content_pt`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`, `_openid`
)
VALUES (
  'reward_lisboeta_secret_cut',
  '夜巡隱藏片段解鎖卡',
  'Night Patrol Hidden Cut Unlock',
  '夜巡隱藏片段解鎖卡',
  'Cartão de desbloqueio do corte oculto',
  '完成室內夜巡後開放的隱藏片段',
  'Hidden cut unlocked after the indoor night patrol',
  '完成室內夜巡後開放的隱藏片段',
  'Trecho oculto aberto após a rota interna noturna',
  '兌換後可在對應室內地圖樓層播放一段限定旁白與隱藏畫面，作為室內支線的獎勵樣板。',
  'Redeeming it unlocks an exclusive narration and visual cut on the linked indoor floor.',
  '兌換後可在對應室內地圖樓層播放一段限定旁白與隱藏畫面，作為室內支線的獎勵樣板。',
  'Ao trocar, libera uma narração exclusiva e uma cena oculta no piso interno vinculado.',
  '用 36 枚印章解鎖室內支線的限定收尾。',
  'Spend 36 stamps to unlock the indoor branch finale.',
  '用 36 枚印章解鎖室內支線的限定收尾。',
  'Troque 36 selos pelo final exclusivo da rota interna.',
  36,
  120,
  4,
  300009,
  'reward-modal',
  JSON_OBJECT('title', '隱藏片段已解鎖', 'ctaLabel', '立即前往對應樓層'),
  'inventory-card',
  JSON_OBJECT('accent', 'ruby', 'showInventory', true, 'showIndoorBinding', true),
  'reward-redemption',
  JSON_OBJECT('requiresBadgeCode', 'badge_lisboeta_pathfinder', 'consumeStamps', true),
  '這筆獎勵用來示範 reward 也能綁定到大地圖、子地圖與室內樓層，並帶著預設化的兌換彈窗。',
  'This reward showcases city, sub-map, and indoor-floor bindings together with preset redemption UX.',
  '這筆獎勵用來示範 reward 也能綁定到大地圖、子地圖與室內樓層，並帶著預設化的兌換彈窗。',
  'Este prêmio demonstra vínculos com cidade, submapa e piso interno junto com uma experiência de troca predefinida.',
  'published',
  31,
  '2026-01-01 00:00:00',
  '2027-12-31 23:59:59',
  @seed_openid
);

SET @collectible_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_lisboeta_night_pass' LIMIT 1);
SET @badge_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_lisboeta_pathfinder' LIMIT 1);
SET @reward_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_lisboeta_secret_cut' LIMIT 1);

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
VALUES
  ('collectible', @collectible_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('collectible', @collectible_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('collectible', @collectible_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('collectible', @collectible_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('collectible', @collectible_id, 'indoor_floor_binding', 'indoor_floor', @indoor_floor_id, @indoor_floor_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('collectible', @collectible_id, 'attachment_asset', 'asset', 300009, 'asset-300009', JSON_OBJECT('usage', 'showcase-archive'), 0),
  ('badge', @badge_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('badge', @badge_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('badge', @badge_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('badge', @badge_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('badge', @badge_id, 'indoor_floor_binding', 'indoor_floor', @indoor_floor_id, @indoor_floor_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('reward', @reward_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('reward', @reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('reward', @reward_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'night-patrol'), 0),
  ('reward', @reward_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('reward', @reward_id, 'indoor_floor_binding', 'indoor_floor', @indoor_floor_id, @indoor_floor_code, JSON_OBJECT('source', 'phase14-showcase'), 0),
  ('reward', @reward_id, 'attachment_asset', 'asset', 300010, 'asset-300010', JSON_OBJECT('usage', 'showcase-preview'), 0)
ON DUPLICATE KEY UPDATE
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase14-collection-showcase',
  'Phase 14 showcase seed for collection/reward carryover with indoor building and floor bindings',
  'completed',
  NOW(),
  'Adds one collectible, one badge, and one reward that bind to city, sub-map, storyline, indoor building, indoor floor, and media attachments.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
