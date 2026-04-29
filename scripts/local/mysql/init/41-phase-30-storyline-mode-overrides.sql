-- Phase 30: storyline mode and chapter override acceptance seed.
-- All multilingual text in this file must remain UTF-8 / utf8mb4.
-- Do not rewrite Chinese, SQL, JSON, CSV, or scripted payloads through non-UTF-8 inline shell literals.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @poi_ama_id = (SELECT `id` FROM `pois` WHERE `code` = 'ama_temple' LIMIT 1);
SET @poi_ama_flow_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'poi_ama_default_walk_in' AND `deleted` = 0 LIMIT 1);

INSERT INTO `storylines` (
  `city_id`, `code`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `estimated_minutes`, `difficulty`, `cover_asset_id`, `banner_asset_id`,
  `reward_badge_zh`, `reward_badge_en`, `reward_badge_zht`, `reward_badge_pt`,
  `status`, `sort_order`, `published_at`
) VALUES (
  @city_macau_id,
  'east_west_war_and_coexistence',
  '東西方文明的戰火與共生',
  'War and Coexistence of Eastern and Western Civilizations',
  '東西方文明的戰火與共生',
  'Guerra e coexistencia das civilizacoes oriental e ocidental',
  '你是一名穿越時空的濠江歷史見證者，追隨一枚殘缺的海防銅鏡，踏遍媽閣、南灣、崗頂、大炮台與議事亭前地，見證戰火如何走向文明共生。',
  'A time-travel witness follows a broken coastal-defense bronze mirror across Macau and sees conflict move toward coexistence.',
  '你是一名穿越時空的濠江歷史見證者，追隨一枚殘缺的海防銅鏡，踏遍媽閣、南灣、崗頂、大炮台與議事亭前地，見證戰火如何走向文明共生。',
  'Uma testemunha historica segue um espelho de defesa costeira por Macau, do conflito a convivencia.',
  150,
  'medium',
  328001,
  328003,
  '濠江見證者',
  'Witness of the Inner Harbour',
  '濠江見證者',
  'Testemunha do Porto Interior',
  'published',
  3010,
  NOW()
) ON DUPLICATE KEY UPDATE
  `city_id` = VALUES(`city_id`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `name_pt` = VALUES(`name_pt`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `description_pt` = VALUES(`description_pt`),
  `estimated_minutes` = VALUES(`estimated_minutes`),
  `difficulty` = VALUES(`difficulty`),
  `cover_asset_id` = VALUES(`cover_asset_id`),
  `banner_asset_id` = VALUES(`banner_asset_id`),
  `reward_badge_zh` = VALUES(`reward_badge_zh`),
  `reward_badge_en` = VALUES(`reward_badge_en`),
  `reward_badge_zht` = VALUES(`reward_badge_zht`),
  `reward_badge_pt` = VALUES(`reward_badge_pt`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

SET @storyline_east_west_id = (SELECT `id` FROM `storylines` WHERE `code` = 'east_west_war_and_coexistence' LIMIT 1);

INSERT INTO `story_chapters` (
  `storyline_id`, `chapter_order`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `detail_zh`, `detail_en`, `detail_zht`, `detail_pt`,
  `achievement_zh`, `achievement_en`, `achievement_zht`, `achievement_pt`,
  `collectible_zh`, `collectible_en`, `collectible_zht`, `collectible_pt`,
  `location_name_zh`, `location_name_en`, `location_name_zht`, `location_name_pt`,
  `media_asset_id`, `anchor_type`, `anchor_target_id`, `anchor_target_code`,
  `unlock_type`, `unlock_param_json`, `prerequisite_json`, `completion_json`, `reward_json`,
  `status`, `sort_order`, `published_at`
) VALUES (
  @storyline_east_west_id,
  1,
  '鏡海初戰：中葡首次海防對峙',
  'Mirror Sea Clash: First Coastal Defense Confrontation',
  '鏡海初戰：中葡首次海防對峙',
  'Primeiro confronto costeiro sino-portugues',
  '抵達媽閣廟後，核心劇情短片、古戰場聲景與三個主線疊加物會接管 POI 預設體驗。',
  'At A-Ma Temple, story media and three main overlays take over the default POI experience.',
  '抵達媽閣廟後，核心劇情短片、古戰場聲景與三個主線疊加物會接管 POI 預設體驗。',
  'No Templo A-Ma, media narrativa e tres sobreposicoes substituem a experiencia padrao.',
  '1553 年前後，葡萄牙武裝商船以借地曬貨為名闖入濠江，明朝水師與媽閣漁民在海岸防線上共同抵禦。你將在這裏撿起戰火的第一塊碎片，還原這場海岸保衛戰的多重視角。',
  'Around 1553, armed Portuguese traders entered the Inner Harbour under a trading pretext. This chapter presents the encounter as a historically grounded, literary reconstruction.',
  '1553 年前後，葡萄牙武裝商船以借地曬貨為名闖入濠江，明朝水師與媽閣漁民在海岸防線上共同抵禦。你將在這裏撿起戰火的第一塊碎片，還原這場海岸保衛戰的多重視角。',
  'Por volta de 1553, comerciantes armados chegaram ao porto interior; o capitulo reconstroi literariamente o primeiro confronto costeiro.',
  '初見濠江',
  'First Glimpse of the Inner Harbour',
  '初見濠江',
  'Primeiro olhar sobre o porto',
  '媽閣戰火銅鏡碎片',
  'A-Ma Battle Bronze Mirror Fragment',
  '媽閣戰火銅鏡碎片',
  'Fragmento do espelho de batalha A-Ma',
  '媽閣廟',
  'A-Ma Temple',
  '媽閣廟',
  'Templo A-Ma',
  328003,
  'poi',
  @poi_ama_id,
  'ama_temple',
  'sequence',
  JSON_OBJECT('schemaVersion', 1, 'preset', 'sequence', 'chapterOrder', 1),
  JSON_OBJECT('schemaVersion', 1, 'preset', 'storyline_started'),
  JSON_OBJECT('schemaVersion', 1, 'preset', 'mainline_overlays_all_clicked', 'requiredStepCode', 'story_ch01_mainline_overlays'),
  JSON_OBJECT('schemaVersion', 1, 'baseReward', '初見濠江勳章', 'hiddenReward', '媽閣戰火銅鏡碎片'),
  'published',
  10,
  NOW()
) ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `title_pt` = VALUES(`title_pt`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `summary_pt` = VALUES(`summary_pt`),
  `detail_zh` = VALUES(`detail_zh`),
  `detail_en` = VALUES(`detail_en`),
  `detail_zht` = VALUES(`detail_zht`),
  `detail_pt` = VALUES(`detail_pt`),
  `achievement_zh` = VALUES(`achievement_zh`),
  `achievement_en` = VALUES(`achievement_en`),
  `achievement_zht` = VALUES(`achievement_zht`),
  `achievement_pt` = VALUES(`achievement_pt`),
  `collectible_zh` = VALUES(`collectible_zh`),
  `collectible_en` = VALUES(`collectible_en`),
  `collectible_zht` = VALUES(`collectible_zht`),
  `collectible_pt` = VALUES(`collectible_pt`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_en` = VALUES(`location_name_en`),
  `location_name_zht` = VALUES(`location_name_zht`),
  `location_name_pt` = VALUES(`location_name_pt`),
  `media_asset_id` = VALUES(`media_asset_id`),
  `anchor_type` = VALUES(`anchor_type`),
  `anchor_target_id` = VALUES(`anchor_target_id`),
  `anchor_target_code` = VALUES(`anchor_target_code`),
  `unlock_type` = VALUES(`unlock_type`),
  `unlock_param_json` = VALUES(`unlock_param_json`),
  `prerequisite_json` = VALUES(`prerequisite_json`),
  `completion_json` = VALUES(`completion_json`),
  `reward_json` = VALUES(`reward_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

SET @chapter_east_west_ch01_id = (
  SELECT `id`
  FROM `story_chapters`
  WHERE `storyline_id` = @storyline_east_west_id
    AND `chapter_order` = 1
  LIMIT 1
);

UPDATE `story_chapters`
SET
  `story_mode_config_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'hideUnrelatedContent', TRUE,
    'nearbyRevealEnabled', TRUE,
    'nearbyRevealRadiusMeters', 120,
    'nearbyRevealMeters', 120,
    'currentRouteHighlight', 'copper_flame',
    'currentRouteStyle', 'copper_flame',
    'inactiveRouteStyle', 'muted_ink',
    'clearTemporaryProgressOnExit', TRUE,
    'exitResetsSessionProgress', TRUE,
    'preservePermanentEvents', TRUE,
    'branchSourceType', 'nearby_poi',
    'branchInsertPosition', 'between_chapters',
    'branchSkippable', TRUE,
    'branchAffectsStoryProgress', FALSE,
    'manualBranchPoiIds', JSON_ARRAY()
  ),
  `override_policy_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'inheritDefaultFlow', TRUE,
    'disableDefaultArrivalMedia', FALSE,
    'appendStorySpecificRewards', TRUE,
    'inheritedFlowCode', 'poi_ama_default_walk_in',
    'storyOverrideFlowCode', 'story_east_west_ch01_flow'
  )
WHERE `id` = @chapter_east_west_ch01_id;

INSERT INTO `experience_flows` (
  `code`, `flow_type`, `mode`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `map_policy_json`, `advanced_config_json`, `status`, `sort_order`, `published_at`
) VALUES (
  'story_east_west_ch01_flow',
  'story_chapter',
  'storyline',
  '鏡海初戰故事專屬流程',
  'Mirror Sea Clash storyline chapter flow',
  '鏡海初戰故事專屬流程',
  'Fluxo narrativo do primeiro capitulo',
  '繼承媽閣廟預設地點體驗，替換抵達媒體，追加主線疊加物、支線拾取物、隱藏挑戰、獎勵與稱號。',
  'Inherits the A-Ma POI default flow, replaces arrival media, and appends story overlays, pickups, hidden challenge, and rewards.',
  '繼承媽閣廟預設地點體驗，替換抵達媒體，追加主線疊加物、支線拾取物、隱藏挑戰、獎勵與稱號。',
  'Herda o fluxo A-Ma e adiciona media, sobreposicoes, coletas, desafio e recompensas.',
  JSON_OBJECT('schemaVersion', 1, 'storylineMode', TRUE, 'routeStyle', 'copper_flame'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase30-storyline-mode-overrides'),
  'published',
  10,
  NOW()
) ON DUPLICATE KEY UPDATE
  `flow_type` = VALUES(`flow_type`),
  `mode` = VALUES(`mode`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `name_pt` = VALUES(`name_pt`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `description_pt` = VALUES(`description_pt`),
  `map_policy_json` = VALUES(`map_policy_json`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

SET @flow_story_ch01_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'story_east_west_ch01_flow' AND `deleted` = 0 LIMIT 1);

UPDATE `story_chapters`
SET `experience_flow_id` = @flow_story_ch01_id
WHERE `id` = @chapter_east_west_ch01_id;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
) VALUES
  (@flow_story_ch01_id, 'story_ch01_arrival_immersive_media', 'fullscreen_media', NULL,
   '故事專屬抵達沉浸短片', 'Storyline arrival immersive media', '故事專屬抵達沉浸短片', 'Media imersiva de chegada',
   '抵達媽閣廟 50 米範圍後，播放鏡海初戰核心歷史劇情短片與古戰場背景音。',
   'When the player reaches A-Ma Temple, play the Mirror Sea Clash story clip and battlefield soundscape.',
   '抵達媽閣廟 50 米範圍後，播放鏡海初戰核心歷史劇情短片與古戰場背景音。',
   'Ao chegar ao Templo A-Ma, reproduz a narrativa principal e a paisagem sonora.',
   'proximity',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'nearby_radius', 'radiusMeters', 50),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'storyline_mode_active', 'oncePerSession', TRUE),
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'fullscreen_media', 'fullScreenMediaAssetId', 328003, 'audioAssetId', 328002, 'fallbackText', '1553 年海防對峙主線短片'),
   328003, NULL, 'core', 1, 'arrival_intro_media', 'published', 10),
  (@flow_story_ch01_id, 'story_ch01_mainline_overlays', 'mainline_overlay', NULL,
   '明葡初遇三個主線疊加物', 'Three mainline overlays', '明葡初遇三個主線疊加物', 'Tres sobreposicoes principais',
   '在地圖上依次生成明朝水師戰船、媽閣漁民防線與葡國武裝商船，完成後章節主線通關。',
   'Spawns Ming warship, fisher defense line, and Portuguese armed ship overlays; tapping all completes the main objective.',
   '在地圖上依次生成明朝水師戰船、媽閣漁民防線與葡國武裝商船，完成後章節主線通關。',
   'Gera tres sobreposicoes principais no mapa.',
   'after_step',
   JSON_OBJECT('schemaVersion', 1, 'afterStepCode', 'story_ch01_arrival_immersive_media'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'after_step', 'afterStepCode', 'story_ch01_arrival_immersive_media'),
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_mainline_overlays', 'overlayCodes', JSON_ARRAY('ming_warship', 'fisher_defense', 'portuguese_armed_ship'), 'completionMode', 'all_clicked'),
   NULL, NULL, 'core', 1, 'story_ch01_arrival_immersive_media', 'published', 20),
  (@flow_story_ch01_id, 'story_ch01_side_pickups', 'side_pickup_bundle', NULL,
   '支線探索拾取物', 'Side exploration pickups', '支線探索拾取物', 'Coletas secundarias',
   '生成明朝海防銅令牌、濠江漁民禦敵漁網殘片與葡人通商納稅契約殘頁三個拾取物。',
   'Spawns Ming coastal token, fisher net fragment, and Portuguese trade tax contract page pickups.',
   '生成明朝海防銅令牌、濠江漁民禦敵漁網殘片與葡人通商納稅契約殘頁三個拾取物。',
   'Gera tres objetos colecionaveis secundarios.',
   'after_step',
   JSON_OBJECT('schemaVersion', 1, 'afterStepCode', 'story_ch01_mainline_overlays'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'story_main_read', 'afterStepCode', 'story_ch01_arrival_immersive_media'),
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_side_pickups', 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page'), 'defaultVisual', 'red_dot_overlay'),
   NULL, NULL, 'large', 0, 'story_ch01_mainline_overlays', 'published', 30),
  (@flow_story_ch01_id, 'story_ch01_hidden_challenge', 'hidden_challenge', NULL,
   '鏡海守護者隱藏挑戰', 'Mirror Sea Guardian hidden challenge', '鏡海守護者隱藏挑戰', 'Desafio Guardiao do Mar',
   '本章三個拾取物全部收集，且在媽閣廟範圍內停留超過五分鐘後，開啟三題歷史問答挑戰。',
   'After collecting all three pickups and staying near A-Ma Temple for five minutes, unlock a three-question history quiz.',
   '本章三個拾取物全部收集，且在媽閣廟範圍內停留超過五分鐘後，開啟三題歷史問答挑戰。',
   'Desbloqueia um quiz historico apos coleta total e permanencia.',
   'compound',
   JSON_OBJECT('schemaVersion', 1, 'requiresAllPickups', TRUE, 'dwellSeconds', 300),
   JSON_OBJECT('schemaVersion', 1, 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page'), 'dwellSeconds', 300),
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'start_hidden_challenge', 'challengeCode', 'mirror_sea_guardian_quiz', 'questionCount', 3, 'passMode', 'all_correct'),
   NULL, NULL, 'core', 0, 'story_ch01_side_pickups', 'published', 40),
  (@flow_story_ch01_id, 'story_ch01_reward_titles', 'reward_title', NULL,
   '通關獎勵與稱號演出', 'Chapter rewards and titles', '通關獎勵與稱號演出', 'Recompensas e titulos',
   '主線通關、全收集與隱藏挑戰分別發放初見濠江、鏡海探索者與海岸守護人稱號。',
   'Grants titles and rewards for main completion, full collection, and hidden challenge completion.',
   '主線通關、全收集與隱藏挑戰分別發放初見濠江、鏡海探索者與海岸守護人稱號。',
   'Concede titulos e recompensas do capitulo.',
   'challenge_complete',
   JSON_OBJECT('schemaVersion', 1, 'afterStepCode', 'story_ch01_hidden_challenge'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'chapter_completion_or_hidden'),
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'grant_rewards_titles', 'baseTitle', '濠江初見者', 'fullCollectionTitle', '鏡海探索者', 'hiddenTitle', '海岸守護人', 'rewardCodes', JSON_ARRAY('first_harbour_badge', 'ama_battle_mirror_fragment')),
   NULL, NULL, 'core', 1, 'story_ch01_hidden_challenge', 'published', 50)
ON DUPLICATE KEY UPDATE
  `step_type` = VALUES(`step_type`),
  `template_id` = VALUES(`template_id`),
  `step_name_zh` = VALUES(`step_name_zh`),
  `step_name_en` = VALUES(`step_name_en`),
  `step_name_zht` = VALUES(`step_name_zht`),
  `step_name_pt` = VALUES(`step_name_pt`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `description_pt` = VALUES(`description_pt`),
  `trigger_type` = VALUES(`trigger_type`),
  `trigger_config_json` = VALUES(`trigger_config_json`),
  `condition_config_json` = VALUES(`condition_config_json`),
  `effect_config_json` = VALUES(`effect_config_json`),
  `media_asset_id` = VALUES(`media_asset_id`),
  `reward_rule_ids_json` = VALUES(`reward_rule_ids_json`),
  `exploration_weight_level` = VALUES(`exploration_weight_level`),
  `required_for_completion` = VALUES(`required_for_completion`),
  `inherit_key` = VALUES(`inherit_key`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

SET @step_arrival_media_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_story_ch01_id AND `step_code` = 'story_ch01_arrival_immersive_media' AND `deleted` = 0 LIMIT 1);
SET @step_mainline_overlays_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_story_ch01_id AND `step_code` = 'story_ch01_mainline_overlays' AND `deleted` = 0 LIMIT 1);
SET @step_side_pickups_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_story_ch01_id AND `step_code` = 'story_ch01_side_pickups' AND `deleted` = 0 LIMIT 1);
SET @step_hidden_challenge_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_story_ch01_id AND `step_code` = 'story_ch01_hidden_challenge' AND `deleted` = 0 LIMIT 1);
SET @step_reward_titles_id = (SELECT `id` FROM `experience_flow_steps` WHERE `flow_id` = @flow_story_ch01_id AND `step_code` = 'story_ch01_reward_titles' AND `deleted` = 0 LIMIT 1);

DELETE FROM `experience_overrides`
WHERE `owner_type` = 'story_chapter'
  AND `owner_id` = @chapter_east_west_ch01_id
  AND `target_owner_type` = 'poi'
  AND `target_owner_id` = @poi_ama_id
  AND `target_step_code` IN (
    'arrival_intro_media',
    'story_ch01_mainline_overlays',
    'story_ch01_side_pickups',
    'story_ch01_hidden_challenge'
  );

INSERT INTO `experience_overrides` (
  `owner_type`, `owner_id`, `target_owner_type`, `target_owner_id`, `target_step_code`,
  `override_mode`, `replacement_step_id`, `override_config_json`, `status`, `sort_order`
) VALUES
  ('story_chapter', @chapter_east_west_ch01_id, 'poi', @poi_ama_id, 'arrival_intro_media',
   'replace', @step_arrival_media_id,
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'fullscreen_media', 'reason', '故事線專屬沉浸短片替換 POI 一般抵達動畫'),
   'published', 10),
  ('story_chapter', @chapter_east_west_ch01_id, 'poi', @poi_ama_id, 'arrival_intro_media',
   'append', @step_mainline_overlays_id,
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_mainline_overlays', 'pickupCodes', JSON_ARRAY('ming_warship', 'fisher_defense', 'portuguese_armed_ship')),
   'published', 20),
  ('story_chapter', @chapter_east_west_ch01_id, 'poi', @poi_ama_id, 'story_ch01_mainline_overlays',
   'append', @step_side_pickups_id,
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_side_pickups', 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page')),
   'published', 30),
  ('story_chapter', @chapter_east_west_ch01_id, 'poi', @poi_ama_id, 'story_ch01_side_pickups',
   'append', @step_hidden_challenge_id,
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'start_hidden_challenge', 'challengeCode', 'mirror_sea_guardian_quiz'),
   'published', 40),
  ('story_chapter', @chapter_east_west_ch01_id, 'poi', @poi_ama_id, 'story_ch01_hidden_challenge',
   'append', @step_reward_titles_id,
   JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'grant_rewards_titles', 'rewardSummary', '初見濠江、鏡海探索者、海岸守護人'),
   'published', 50);

INSERT INTO `experience_bindings` (
  `owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `priority`, `inherit_policy`, `status`, `sort_order`
) VALUES
  ('story_chapter', @chapter_east_west_ch01_id, 'east_west_war_ch01', 'story_override_flow', @flow_story_ch01_id, 30, 'override', 'published', 10)
ON DUPLICATE KEY UPDATE
  `priority` = VALUES(`priority`),
  `inherit_policy` = VALUES(`inherit_policy`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
) VALUES
  ('storyline', @storyline_east_west_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('schemaVersion', 1), 10),
  ('storyline', @storyline_east_west_id, 'sub_map_binding', 'sub_map', @sub_map_peninsula_id, 'macau-peninsula', JSON_OBJECT('schemaVersion', 1), 20),
  ('story_chapter', @chapter_east_west_ch01_id, 'story_chapter_anchor', 'poi', @poi_ama_id, 'ama_temple', JSON_OBJECT('schemaVersion', 1), 10),
  ('story_chapter', @chapter_east_west_ch01_id, 'story_override_target', 'experience_flow', @poi_ama_flow_id, 'poi_ama_default_walk_in', JSON_OBJECT('schemaVersion', 1, 'overrideFlowId', @flow_story_ch01_id), 20)
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
) VALUES
  ('story_ch01_arrival_immersive_media', 'story_media_complete', 'story_chapter', @chapter_east_west_ch01_id, 'story_ch01_arrival_immersive_media', @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, @chapter_east_west_ch01_id,
   '觀看鏡海初戰核心劇情短片', 'Watch Mirror Sea Clash core media', '觀看鏡海初戰核心劇情短片', 'Ver media principal do primeiro capitulo',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'story_ch01_arrival_immersive_media'), 'published', 10),
  ('story_ch01_mainline_overlays', 'story_mainline_overlay_complete', 'story_chapter', @chapter_east_west_ch01_id, 'story_ch01_mainline_overlays', @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, @chapter_east_west_ch01_id,
   '完成三個主線疊加物互動', 'Complete three mainline overlays', '完成三個主線疊加物互動', 'Completar tres sobreposicoes principais',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'overlayCodes', JSON_ARRAY('ming_warship', 'fisher_defense', 'portuguese_armed_ship')), 'published', 20),
  ('story_ch01_side_pickups', 'story_side_pickup_bundle', 'story_chapter', @chapter_east_west_ch01_id, 'story_ch01_side_pickups', @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, @chapter_east_west_ch01_id,
   '收集三個支線拾取物', 'Collect three side pickups', '收集三個支線拾取物', 'Recolher tres objetos secundarios',
   'large', 5, 1, JSON_OBJECT('schemaVersion', 1, 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page')), 'published', 30),
  ('story_ch01_hidden_challenge', 'story_hidden_challenge_complete', 'story_chapter', @chapter_east_west_ch01_id, 'story_ch01_hidden_challenge', @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, @chapter_east_west_ch01_id,
   '完成鏡海守護者隱藏挑戰', 'Complete Mirror Sea Guardian hidden challenge', '完成鏡海守護者隱藏挑戰', 'Completar desafio oculto',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'challengeCode', 'mirror_sea_guardian_quiz'), 'published', 40),
  ('story_ch01_reward_titles', 'story_reward_titles_granted', 'story_chapter', @chapter_east_west_ch01_id, 'story_ch01_reward_titles', @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, @chapter_east_west_ch01_id,
   '獲得鏡海初戰章節獎勵與稱號', 'Receive chapter rewards and titles', '獲得鏡海初戰章節獎勵與稱號', 'Receber recompensas e titulos',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'titles', JSON_ARRAY('濠江初見者', '鏡海探索者', '海岸守護人')), 'published', 50)
ON DUPLICATE KEY UPDATE
  `element_type` = VALUES(`element_type`),
  `owner_type` = VALUES(`owner_type`),
  `owner_id` = VALUES(`owner_id`),
  `owner_code` = VALUES(`owner_code`),
  `city_id` = VALUES(`city_id`),
  `sub_map_id` = VALUES(`sub_map_id`),
  `storyline_id` = VALUES(`storyline_id`),
  `story_chapter_id` = VALUES(`story_chapter_id`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `title_pt` = VALUES(`title_pt`),
  `weight_level` = VALUES(`weight_level`),
  `weight_value` = VALUES(`weight_value`),
  `include_in_exploration` = VALUES(`include_in_exploration`),
  `metadata_json` = VALUES(`metadata_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase30-storyline-mode-overrides',
  'Phase 30 storyline mode and chapter override acceptance slice',
  'completed',
  NOW(),
  'Seeds east_west_war_and_coexistence chapter 1 with A-Ma POI inheritance, replacement arrival media, append story steps, and exploration elements.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
