-- Phase 29: POI default experience workbench acceptance seed.
-- All multilingual text in this file is UTF-8 / utf8mb4. Do not rewrite through non-UTF-8 shell literals.
-- This seed reuses Phase 28 experience_* tables; it does not create a POI-only schema.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @poi_ama_id = (SELECT `id` FROM `pois` WHERE `code` = 'ama_temple' LIMIT 1);
SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);

INSERT INTO `experience_templates` (
  `code`, `template_type`, `category`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `config_json`, `schema_json`, `risk_level`, `status`, `sort_order`
) VALUES
  ('tpl_poi_intro_modal', 'presentation', 'poi_intro',
   '地點圖文介紹彈窗', 'POI intro modal', '地點圖文介紹彈窗', 'Janela de introducao do local',
   '點擊地點後顯示圖文介紹與「前往探索該地」按鈕。', 'Shows intro content and a start exploration button after tapping a POI.', '點擊地點後顯示圖文介紹與「前往探索該地」按鈕。', 'Mostra introducao e botao para explorar.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'intro_modal', 'effectPreset', 'show_modal', 'primaryActionLabel', '前往探索該地'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('modalTitle', 'modalBody', 'primaryActionLabel')), 'normal', 'published', 10),
  ('tpl_poi_route_guidance', 'effect', 'poi_route',
   '目的地導航與推薦卡', 'Destination route guidance cards', '目的地導航與推薦卡', 'Cartoes de navegacao',
   '點擊前往後規劃路線，並展示交通、故事線、附近與途經地點推薦。', 'After choosing to explore, renders route and recommendation cards.', '點擊前往後規劃路線，並展示交通、故事線、附近與途經地點推薦。', 'Mostra rota e recomendacoes.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'route_guidance', 'cards', JSON_ARRAY('transport', 'recommended_storyline', 'nearby_poi', 'waypoint_poi')),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('routeCardTypes')), 'normal', 'published', 20),
  ('tpl_proximity_fullscreen_media', 'trigger_effect', 'poi_proximity_media',
   '靠近範圍播放全屏媒體', 'Proximity fullscreen media', '靠近範圍播放全屏媒體', 'Media em tela cheia por proximidade',
   '進入指定半徑後播放全屏動畫、影片、Lottie 與背景音。', 'Plays fullscreen animation, video, Lottie, and audio when entering a radius.', '進入指定半徑後播放全屏動畫、影片、Lottie 與背景音。', 'Reproduz media em tela cheia ao entrar no raio.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'proximity_media', 'triggerType', 'proximity', 'effectPreset', 'fullscreen_media'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('triggerRadiusMeters', 'fullScreenMediaAssetId')), 'high', 'published', 30),
  ('tpl_poi_checkin_task_release', 'task_gameplay', 'poi_checkin',
   '打卡後派發任務', 'Check-in task release', '打卡後派發任務', 'Lancamento de tarefas',
   '介紹動畫或抵達事件完成後，派發拍照、互動遊戲或打卡任務。', 'Releases photo, mini-game, or check-in tasks after arrival media.', '介紹動畫或抵達事件完成後，派發拍照、互動遊戲或打卡任務。', 'Liberta tarefas apos a chegada.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'checkin_task', 'effectPreset', 'release_tasks', 'taskKinds', JSON_ARRAY('photo', 'mini_game', 'checkin')),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('taskCodes')), 'normal', 'published', 40),
  ('tpl_poi_pickup_side_clues', 'trigger_effect', 'poi_pickup',
   '支線拾取物出現與收集', 'Side clue pickups', '支線拾取物出現與收集', 'Pistas secundarias',
   '按前置條件、停留或點擊事件，在地點周邊顯示可拾取疊加物。', 'Shows collectible overlays around a POI according to prerequisite and dwell rules.', '按前置條件、停留或點擊事件，在地點周邊顯示可拾取疊加物。', 'Mostra objetos recolhiveis no local.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'pickup', 'effectPreset', 'grant_pickups', 'defaultVisual', 'red_dot_overlay'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('pickupCodes')), 'normal', 'published', 50),
  ('tpl_poi_hidden_dwell_achievement', 'gameplay', 'poi_hidden_dwell',
   '停留隱藏成就', 'Hidden dwell achievement', '停留隱藏成就', 'Conquista oculta por permanencia',
   '在指定範圍內累計停留達標後，觸發隱藏成就、稱號或挑戰。', 'Unlocks hidden achievement, title, or challenge after dwell conditions are met.', '在指定範圍內累計停留達標後，觸發隱藏成就、稱號或挑戰。', 'Desbloqueia conquista oculta apos permanencia.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'hidden_challenge', 'triggerType', 'dwell', 'effectPreset', 'grant_hidden_achievement'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('triggerRadiusMeters', 'dwellSeconds', 'rewardSummary')), 'high', 'published', 60),
  ('tpl_poi_completion_reward_title', 'reward_presentation', 'poi_completion_reward',
   '完成獎勵與稱號演出', 'Completion reward and title grant', '完成獎勵與稱號演出', 'Recompensa e titulo final',
   '完成地點流程後發放金币、徽章、稱號、物品或全屏獎勵演出。', 'Grants coins, badges, titles, items, or reward presentation after completing the POI flow.', '完成地點流程後發放金币、徽章、稱號、物品或全屏獎勵演出。', 'Concede moedas, titulos e recompensas.',
   JSON_OBJECT('schemaVersion', 1, 'stepType', 'reward_grant', 'effectPreset', 'grant_reward_title', 'presentation', 'fullscreen_or_toast'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('rewardSummary')), 'high', 'published', 70)
ON DUPLICATE KEY UPDATE
  `template_type` = VALUES(`template_type`),
  `category` = VALUES(`category`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `name_pt` = VALUES(`name_pt`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `summary_pt` = VALUES(`summary_pt`),
  `config_json` = VALUES(`config_json`),
  `schema_json` = VALUES(`schema_json`),
  `risk_level` = VALUES(`risk_level`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `experience_flows` (
  `code`, `flow_type`, `mode`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `map_policy_json`, `advanced_config_json`, `status`, `sort_order`, `published_at`
) VALUES
  ('poi_ama_default_walk_in', 'default_poi', 'walk_in',
   '媽閣廟預設地點體驗', 'A-Ma Temple default POI experience', '媽閣廟預設地點體驗', 'Experiencia padrao do Templo A-Ma',
   '自然走近或點擊媽閣廟時使用的 POI 預設流程：介紹、前往探索、到達媒體、打卡任務、支線拾取、隱藏停留成就與完成獎勵。', 'Default walk-in and tap-to-explore flow for A-Ma Temple.', '自然走近或點擊媽閣廟時使用的 POI 預設流程：介紹、前往探索、到達媒體、打卡任務、支線拾取、隱藏停留成就與完成獎勵。', 'Fluxo padrao do Templo A-Ma.',
   JSON_OBJECT('schemaVersion', 1, 'mapPolicy', 'poi_default', 'supportsStoryOverride', TRUE, 'hideUnrelatedContent', FALSE),
   JSON_OBJECT('schemaVersion', 1, 'source', 'phase29-poi-default-experience'), 'published', 10, NOW())
ON DUPLICATE KEY UPDATE
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

SET @flow_ama_default_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'poi_ama_default_walk_in' AND `deleted` = 0 LIMIT 1);
SET @tpl_intro_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_intro_modal' AND `deleted` = 0 LIMIT 1);
SET @tpl_route_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_route_guidance' AND `deleted` = 0 LIMIT 1);
SET @tpl_media_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_proximity_fullscreen_media' AND `deleted` = 0 LIMIT 1);
SET @tpl_task_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_checkin_task_release' AND `deleted` = 0 LIMIT 1);
SET @tpl_pickup_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_pickup_side_clues' AND `deleted` = 0 LIMIT 1);
SET @tpl_hidden_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_hidden_dwell_achievement' AND `deleted` = 0 LIMIT 1);
SET @tpl_reward_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'tpl_poi_completion_reward_title' AND `deleted` = 0 LIMIT 1);

UPDATE `experience_flow_steps`
SET `deleted` = 1
WHERE `flow_id` = @flow_ama_default_id
  AND `step_code` IN ('route_guidance', 'checkin_task_release', 'hidden_dwell_title')
  AND `deleted` = 0;

UPDATE `experience_bindings`
SET `deleted` = 1
WHERE `owner_type` = 'poi'
  AND `owner_id` = @poi_ama_id
  AND `binding_role` = 'default_experience_flow'
  AND `flow_id` <> @flow_ama_default_id
  AND `deleted` = 0;

INSERT INTO `experience_bindings` (
  `owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `priority`, `inherit_policy`, `status`, `sort_order`
) VALUES
  ('poi', @poi_ama_id, 'ama_temple', 'default_experience_flow', @flow_ama_default_id, 0, 'inherit', 'published', 0)
ON DUPLICATE KEY UPDATE
  `owner_code` = VALUES(`owner_code`),
  `priority` = VALUES(`priority`),
  `inherit_policy` = VALUES(`inherit_policy`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
) VALUES
  (@flow_ama_default_id, 'tap_intro', 'intro_modal', @tpl_intro_id,
   '點擊地點介紹', 'Tap POI intro', '點擊地點介紹', 'Introducao por toque',
   '用圖文彈窗展示媽閣廟的海神信仰、海上交通記憶與澳門開埠前後的歷史語境，並提供「前往探索該地」按鈕。', 'Shows A-Ma Temple intro and a start exploration button.', '用圖文彈窗展示媽閣廟的海神信仰、海上交通記憶與澳門開埠前後的歷史語境，並提供「前往探索該地」按鈕。', 'Mostra introducao do Templo A-Ma.',
   'tap',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'tap_poi_intro', 'triggerType', 'tap', 'tapActionCode', 'open_poi_intro'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'always', 'oncePerUser', FALSE),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'show_modal', 'modalTitle', '媽閣廟：濠江海路的開端', 'modalBody', '這裏是澳門海上記憶與民間信仰交會的入口。點擊前往後，系統會把媽閣廟設定為探索目的地。', 'primaryActionLabel', '前往探索該地'),
   NULL, NULL, 'tiny', 0, '', 'published', 10),
  (@flow_ama_default_id, 'start_route_guidance', 'route_guidance', @tpl_route_id,
   '前往探索與路線建議', 'Start route guidance', '前往探索與路線建議', 'Navegacao para explorar',
   '點擊前往後，把媽閣廟設為目的地，展示交通方式、故事線、附近與途經地點推薦。', 'Sets A-Ma Temple as destination and shows route recommendations.', '點擊前往後，把媽閣廟設為目的地，展示交通方式、故事線、附近與途經地點推薦。', 'Mostra rota e recomendacoes.',
   'tap_action',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'start_route_guidance', 'triggerType', 'tap_action', 'tapActionCode', 'start_explore', 'afterStepCode', 'tap_intro'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'after_step', 'afterStepCode', 'tap_intro', 'oncePerUser', FALSE),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'route_guidance', 'routeCardTypes', JSON_ARRAY('transport', 'recommended_storyline', 'nearby_poi', 'waypoint_poi'), 'modalTitle', '已設定目的地：媽閣廟'),
   NULL, NULL, 'tiny', 0, 'tap_intro', 'published', 20),
  (@flow_ama_default_id, 'arrival_intro_media', 'proximity_media', @tpl_media_id,
   '抵達播放全屏介紹', 'Arrival fullscreen intro', '抵達播放全屏介紹', 'Media ao chegar',
   '進入媽閣廟 50 米範圍後播放全屏介紹動畫與背景音，介紹媽閣廟在海防、信仰與城市起源敘事中的位置。', 'Plays fullscreen intro media within 50 meters.', '進入媽閣廟 50 米範圍後播放全屏介紹動畫與背景音，介紹媽閣廟在海防、信仰與城市起源敘事中的位置。', 'Reproduz media ao chegar.',
   'proximity',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'proximity_fullscreen_media', 'triggerType', 'proximity', 'radiusMeters', 50),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'once_per_user', 'oncePerUser', TRUE),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'fullscreen_media', 'effectPreset', 'fullscreen_media', 'fullScreenMediaAssetId', 328001, 'audioAssetId', 328002, 'fallbackText', '媽閣廟全屏介紹動畫'),
   328001, NULL, 'small', 0, 'start_route_guidance', 'published', 30),
  (@flow_ama_default_id, 'release_checkin_tasks', 'checkin_task', @tpl_task_id,
   '抵達後派發打卡任務', 'Release check-in tasks', '抵達後派發打卡任務', 'Lancamento de tarefas',
   '全屏介紹播放後開放打卡，並派發「大門照片」與「賽博點香」兩個任務。', 'Releases gate photo and cyber incense tasks after arrival media.', '全屏介紹播放後開放打卡，並派發「大門照片」與「賽博點香」兩個任務。', 'Liberta tarefas apos a introducao.',
   'media_finished',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'after_media_finished', 'triggerType', 'media_finished', 'afterStepCode', 'arrival_intro_media'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'after_step_once', 'afterStepCode', 'arrival_intro_media', 'oncePerUser', TRUE),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'release_tasks', 'taskCodes', JSON_ARRAY('ama_gate_photo', 'ama_cyber_incense'), 'modalTitle', '媽閣廟打卡任務已開放', 'modalBody', '請拍攝山門照片，並完成賽博點香互動。'),
   NULL, NULL, 'medium', 1, 'arrival_intro_media', 'published', 40),
  (@flow_ama_default_id, 'pickup_side_clues', 'pickup', @tpl_pickup_id,
   '支線拾取物線索', 'Side clue pickups', '支線拾取物線索', 'Pistas secundarias',
   '主線打卡任務開放後，在媽閣廟周邊出現明朝海防銅令牌、濠江漁民禦敵漁網殘片與葡人通商納稅契約殘頁。', 'Shows three side clue pickups around A-Ma Temple.', '主線打卡任務開放後，在媽閣廟周邊出現明朝海防銅令牌、濠江漁民禦敵漁網殘片與葡人通商納稅契約殘頁。', 'Mostra tres pistas secundarias.',
   'tap',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'tap_pickup_bundle', 'triggerType', 'tap', 'afterStepCode', 'release_checkin_tasks'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'after_step', 'afterStepCode', 'release_checkin_tasks', 'requiredItemCodes', JSON_ARRAY()),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'grant_pickups', 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page'), 'rewardSummary', '拾取後存入背包，按拾取物等級計入區域探索度。'),
   NULL, NULL, 'large', 0, 'release_checkin_tasks', 'published', 50),
  (@flow_ama_default_id, 'hidden_dwell_achievement', 'hidden_challenge', @tpl_hidden_id,
   '停留隱藏成就', 'Hidden dwell achievement', '停留隱藏成就', 'Conquista oculta',
   '在媽閣廟 30 米範圍內累計停留 30 分鐘後，觸發隱藏成就「媽祖最愛心誠的孩子」。', 'Unlocks a hidden achievement after 30 minutes within 30 meters.', '在媽閣廟 30 米範圍內累計停留 30 分鐘後，觸發隱藏成就「媽祖最愛心誠的孩子」。', 'Desbloqueia conquista oculta apos permanencia.',
   'dwell',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'hidden_dwell_achievement', 'triggerType', 'dwell', 'radiusMeters', 30, 'dwellSeconds', 1800),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'dwell_in_radius', 'oncePerUser', TRUE),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'grant_hidden_achievement', 'rewardSummary', '隱藏稱號：媽祖最愛心誠的孩子', 'modalTitle', '隱藏成就解鎖'),
   NULL, JSON_ARRAY('ama_hidden_dwell_achievement'), 'core', 0, 'arrival_intro_media', 'published', 60),
  (@flow_ama_default_id, 'completion_reward_title', 'reward_grant', @tpl_reward_id,
   '完成獎勵與稱號', 'Completion reward and title', '完成獎勵與稱號', 'Recompensa final',
   '完成媽閣廟預設流程後發放金币、徽章與地點稱號，並向後續故事線或自由探索推薦做銜接。', 'Grants coins, badge, and location title after completing the default POI flow.', '完成媽閣廟預設流程後發放金币、徽章與地點稱號，並向後續故事線或自由探索推薦做銜接。', 'Concede recompensas finais.',
   'task_complete',
   JSON_OBJECT('schemaVersion', 1, 'preset', 'completion_reward', 'triggerType', 'task_complete', 'afterStepCode', 'release_checkin_tasks'),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'required_tasks_done', 'requiredItemCodes', JSON_ARRAY('ama_gate_photo', 'ama_cyber_incense')),
   JSON_OBJECT('schemaVersion', 1, 'preset', 'grant_reward_title', 'rewardSummary', '金币、媽閣廟打卡徽章、地點稱號「濠江初見者」', 'primaryActionLabel', '查看我的獎勵'),
   NULL, JSON_ARRAY('ama_completion_reward_title'), 'core', 1, 'release_checkin_tasks', 'published', 70)
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

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
) VALUES
  ('ama_poi_arrival_intro', 'poi_proximity_media_complete', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '觀看媽閣廟抵達介紹', 'Watch A-Ma arrival intro', '觀看媽閣廟抵達介紹', 'Ver introducao do Templo A-Ma',
   'small', 2, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'arrival_intro_media', 'source', 'poi_default'), 'published', 10),
  ('ama_checkin_tasks_released', 'poi_checkin_tasks_released', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '開放媽閣廟打卡任務', 'Release A-Ma check-in tasks', '開放媽閣廟打卡任務', 'Libertar tarefas do Templo A-Ma',
   'medium', 3, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'release_checkin_tasks', 'taskCodes', JSON_ARRAY('ama_gate_photo', 'ama_cyber_incense')), 'published', 20),
  ('ama_side_clues_collected', 'poi_side_pickup_bundle', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '收集媽閣廟支線線索', 'Collect A-Ma side clues', '收集媽閣廟支線線索', 'Recolher pistas secundarias',
   'large', 5, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'pickup_side_clues', 'pickupCodes', JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page')), 'published', 30),
  ('ama_hidden_dwell_achievement', 'poi_hidden_achievement', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '解鎖媽閣廟停留隱藏成就', 'Unlock A-Ma hidden dwell achievement', '解鎖媽閣廟停留隱藏成就', 'Desbloquear conquista oculta',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'hidden_dwell_achievement', 'radiusMeters', 30, 'dwellSeconds', 1800), 'published', 40),
  ('ama_completion_reward_title', 'poi_completion_reward', 'poi', @poi_ama_id, 'ama_temple', @city_macau_id, @sub_map_peninsula_id, NULL, NULL,
   '完成媽閣廟地點體驗', 'Complete A-Ma POI experience', '完成媽閣廟地點體驗', 'Completar experiencia do Templo A-Ma',
   'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'sourceStepCode', 'completion_reward_title', 'rewardSummary', '金币、徽章與濠江初見者稱號'), 'published', 50)
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
