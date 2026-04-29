USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @seed_openid = 'phase11-seed';
SET @storyline_macau_fire_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_macau_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @pin_url = 'seed://asset/300008';
SET @archive_url = 'seed://asset/300009';
SET @activity_url = 'seed://asset/300010';
SET @fortress_url = 'seed://asset/300006';

SET @old_collectible_first_sight_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_first_sight_macau' LIMIT 1);
SET @old_collectible_boundary_fragment_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_boundary_map_fragment' LIMIT 1);
SET @old_collectible_hill_log_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_hill_watch_log' LIMIT 1);
SET @old_badge_boundary_keeper_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_boundary_keeper' LIMIT 1);
SET @old_badge_fortress_hero_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_fortress_hero' LIMIT 1);
SET @old_reward_archive_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_historic_archive' LIMIT 1);
SET @old_reward_voice_pack_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_war_voice_pack' LIMIT 1);

DELETE FROM `content_relation_links`
WHERE (`owner_type` = 'collectible' AND `owner_id` IN (@old_collectible_first_sight_id, @old_collectible_boundary_fragment_id, @old_collectible_hill_log_id))
   OR (`owner_type` = 'badge' AND `owner_id` IN (@old_badge_boundary_keeper_id, @old_badge_fortress_hero_id))
   OR (`owner_type` = 'reward' AND `owner_id` IN (@old_reward_archive_id, @old_reward_voice_pack_id));

DELETE FROM `collectibles`
WHERE `collectible_code` IN ('collectible_first_sight_macau', 'collectible_boundary_map_fragment', 'collectible_hill_watch_log');
DELETE FROM `badges`
WHERE `badge_code` IN ('badge_boundary_keeper', 'badge_fortress_hero');
DELETE FROM `rewards`
WHERE `code` IN ('reward_historic_archive', 'reward_war_voice_pack');

INSERT INTO `collectibles` (
  `collectible_code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `collectible_type`, `rarity`, `cover_asset_id`, `icon_asset_id`, `animation_asset_id`,
  `image_url`, `animation_url`, `series_id`, `acquisition_source`, `bind_condition`, `display_rule`,
  `is_repeatable`, `is_limited`, `cross_city`, `max_ownership`, `status`, `sort_order`, `_openid`
)
VALUES
  ('collectible_first_sight_macau', '初见濠江', 'First Sight of Macau', '初見濠江', 'Primeira visao de Macau', '在妈阁庙完成开场章节后获得，象征第一次看见澳门的海防边界。', 'Earned after the opening chapter at A-Ma Temple, symbolizing the first sight of Macau frontier.', '在媽閣廟完成開場章節後獲得，象徵第一次看見澳門的海防邊界。', 'Obtido em A-Ma, simboliza a primeira visao da fronteira costeira de Macau.', 'document', 'rare', 300009, 300008, NULL, @archive_url, NULL, 11, 'storyline', JSON_OBJECT('chapterId', 311001, 'trigger', 'story_complete'), JSON_OBJECT('displayMode', 'story-route-highlight'), 0, 0, 0, 1, 'published', 0, @seed_openid),
  ('collectible_boundary_map_fragment', '南湾界线地图碎片', 'Southern Boundary Map Fragment', '南灣界線地圖碎片', 'Fragmento do mapa da fronteira sul', '在亚婆井完成停留与互动后获得，用来拼出南湾防线。', 'Earned after Lilau interactions and used to reconstruct the southern line.', '在亞婆井完成停留與互動後獲得，用來拼出南灣防線。', 'Obtido em Lilau para reconstruir a linha defensiva do sul.', 'fragment', 'epic', 300010, 300008, NULL, @activity_url, NULL, 11, 'storyline', JSON_OBJECT('chapterId', 311002, 'trigger', 'chapter_complete'), JSON_OBJECT('displayMode', 'map-fragment'), 0, 0, 0, 1, 'published', 1, @seed_openid),
  ('collectible_hill_watch_log', '山城瞭望札记', 'Hill Watch Log', '山城瞭望札記', 'Caderno da vigia da colina', '在岗顶完成瞭望点对照后获得，记录山城如何兼具文化与防守。', 'Unlocked after the hill lookout comparison and records how the district balanced culture and defense.', '在崗頂完成瞭望點對照後獲得，記錄山城如何兼具文化與防守。', 'Obtido apos a comparacao da vigia, regista a colina entre cultura e defesa.', 'document', 'rare', 300009, 300008, NULL, @archive_url, NULL, 11, 'storyline', JSON_OBJECT('chapterId', 311003, 'trigger', 'overlay_viewed'), JSON_OBJECT('displayMode', 'story-route-highlight'), 0, 0, 0, 1, 'published', 2, @seed_openid);

INSERT INTO `badges` (
  `badge_code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `cover_asset_id`, `icon_asset_id`, `animation_asset_id`,
  `icon_url`, `badge_type`, `rarity`, `is_hidden`, `is_limited_time`,
  `limited_start`, `limited_end`, `image_url`, `animation_unlock`, `status`, `_openid`
)
VALUES
  ('badge_boundary_keeper', '界线守护者', 'Keeper of the Boundary', '界線守護者', 'Guardiao da Fronteira', '完成第二章的停留与界碑互动后获得，象征守住边界的耐性。', 'Unlocked after the second chapter, honoring the patience of holding the line.', '完成第二章的停留與界碑互動後獲得，象徵守住邊界的耐性。', 'Obtida apos o segundo capitulo, simboliza a paciencia de sustentar a fronteira.', 300009, 300008, NULL, @pin_url, 'storyline', 'epic', 0, 0, NULL, NULL, @archive_url, NULL, 'published', @seed_openid),
  ('badge_fortress_hero', '要塞英雄', 'Fortress Hero', '要塞英雄', 'Heroi da Fortaleza', '完成大炮台双炮位互动后获得，是整条路线的高潮徽章。', 'Earned after the dual battery interaction at Monte Fort, this is the climax badge of the route.', '完成大炮台雙炮位互動後獲得，是整條路線的高潮徽章。', 'Obtida apos a interacao dupla na fortaleza, e a insignia do auge da rota.', 300006, 300008, NULL, @pin_url, 'storyline', 'legendary', 0, 0, NULL, NULL, @fortress_url, NULL, 'published', @seed_openid);

INSERT INTO `rewards` (
  `code`, `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`, `subtitle_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `highlight_zh`, `highlight_en`, `highlight_zht`, `highlight_pt`,
  `stamp_cost`, `inventory_total`, `inventory_redeemed`, `cover_asset_id`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`, `_openid`
)
VALUES
  ('reward_historic_archive', '濠江通史档案', 'Macau Historical Archive', '濠江通史檔案', 'Arquivo Historico de Macau', '解锁全线隐藏史料库', 'Unlock the full hidden route archive', '解鎖全線隱藏史料庫', 'Desbloqueia o arquivo oculto da rota', '整合路线中的界线资料、观察札记与终章回顾，作为完成后的深度奖励。', 'Bundles the boundary material, observation log, and finale recap into one deeper reward.', '整合路線中的界線資料、觀察札記與終章回顧，作為完成後的深度獎勵。', 'Reune materiais da fronteira, caderno da vigia e recapitulacao final num premio unico.', '用章节成果兑换整条路线的深度史料。', 'Trade chapter progress for the deep route archive.', '用章節成果兌換整條路線的深度史料。', 'Troca o progresso pelos materiais profundos da rota.', 88, 9999, 124, 300009, 'published', 0, '2026-01-01 00:00:00', '2027-12-31 23:59:59', @seed_openid),
  ('reward_war_voice_pack', '战争主题 AI 语音包', 'War Theme AI Voice Pack', '戰爭主題 AI 語音包', 'Pacote de voz tematico de guerra', '让 AI 导览切换到路线专属叙事音色', 'Switch AI guidance to the route thematic narration', '讓 AI 導覽切換到路線專屬敘事音色', 'Muda a guia AI para a narracao tematica da rota', '完成全线后可解锁的语音奖励，让 AI 导览在关键节点使用更沉浸的战火口吻。', 'Unlocked after the full route, this reward gives AI guidance a more dramatic wartime tone.', '完成全線後可解鎖的語音獎勵，讓 AI 導覽在關鍵節點使用更沉浸的戰火口吻。', 'Desbloqueado apos a rota completa, altera a voz AI para um tom mais imersivo.', '让导览声音也进入这条故事线的氛围。', 'Bring the guide voice into the mood of this storyline.', '讓導覽聲音也進入這條故事線的氛圍。', 'Leva a voz-guia para o mesmo ambiente desta historia.', 56, 9999, 39, 300010, 'published', 1, '2026-01-01 00:00:00', '2027-12-31 23:59:59', @seed_openid);

SET @collectible_first_sight_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_first_sight_macau' LIMIT 1);
SET @collectible_boundary_fragment_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_boundary_map_fragment' LIMIT 1);
SET @collectible_hill_log_id = (SELECT `id` FROM `collectibles` WHERE `collectible_code` = 'collectible_hill_watch_log' LIMIT 1);
SET @badge_boundary_keeper_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_boundary_keeper' LIMIT 1);
SET @badge_fortress_hero_id = (SELECT `id` FROM `badges` WHERE `badge_code` = 'badge_fortress_hero' LIMIT 1);
SET @reward_archive_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_historic_archive' LIMIT 1);
SET @reward_voice_pack_id = (SELECT `id` FROM `rewards` WHERE `code` = 'reward_war_voice_pack' LIMIT 1);

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
VALUES
  ('collectible', @collectible_first_sight_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 1), 0),
  ('collectible', @collectible_first_sight_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('collectible', @collectible_first_sight_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('collectible', @collectible_boundary_fragment_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 2), 0),
  ('collectible', @collectible_boundary_fragment_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('collectible', @collectible_boundary_fragment_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('collectible', @collectible_hill_log_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 3), 0),
  ('collectible', @collectible_hill_log_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('collectible', @collectible_hill_log_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('badge', @badge_boundary_keeper_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 2), 0),
  ('badge', @badge_boundary_keeper_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('badge', @badge_boundary_keeper_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('badge', @badge_fortress_hero_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 4), 0),
  ('badge', @badge_fortress_hero_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('badge', @badge_fortress_hero_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('reward', @reward_archive_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 5), 0),
  ('reward', @reward_archive_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('reward', @reward_archive_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0),
  ('reward', @reward_voice_pack_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('chapterOrder', 5), 0),
  ('reward', @reward_voice_pack_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('scope', 'route'), 0),
  ('reward', @reward_voice_pack_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'route'), 0);

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase11-showcase-story-activity-collection',
  'Phase 11 showcase seed for the Macau fire-route storyline, authored activities, and collection graph',
  'completed',
  NOW(),
  'Adds four-language story, activity, collectible, badge, reward, POI, and media showcase content for Phase 11.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);

DELETE `links`
FROM `content_relation_links` `links`
LEFT JOIN `storylines` `story` ON `links`.`owner_type` = 'storyline' AND `links`.`owner_id` = `story`.`id`
LEFT JOIN `activities` `activity` ON `links`.`owner_type` = 'activity' AND `links`.`owner_id` = `activity`.`id`
LEFT JOIN `collectibles` `collectible` ON `links`.`owner_type` = 'collectible' AND `links`.`owner_id` = `collectible`.`id`
LEFT JOIN `badges` `badge` ON `links`.`owner_type` = 'badge' AND `links`.`owner_id` = `badge`.`id`
LEFT JOIN `rewards` `reward` ON `links`.`owner_type` = 'reward' AND `links`.`owner_id` = `reward`.`id`
WHERE (`links`.`owner_type` = 'storyline' AND `story`.`id` IS NULL)
   OR (`links`.`owner_type` = 'activity' AND `activity`.`id` IS NULL)
   OR (`links`.`owner_type` = 'collectible' AND `collectible`.`id` IS NULL)
   OR (`links`.`owner_type` = 'badge' AND `badge`.`id` IS NULL)
   OR (`links`.`owner_type` = 'reward' AND `reward`.`id` IS NULL);
