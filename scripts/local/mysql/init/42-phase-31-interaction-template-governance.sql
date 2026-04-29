-- Phase 31: reusable interaction/task templates and governance conflict fixtures.
-- All multilingual text is UTF-8 / utf8mb4. Do not rewrite Chinese through inline PowerShell literals.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

INSERT INTO `experience_templates` (
  `code`, `template_type`, `category`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `config_json`, `schema_json`, `risk_level`, `status`, `sort_order`
) VALUES
  ('presentation.fullscreen_media', 'presentation', 'fullscreen_media',
   '全屏媒體演出', 'Fullscreen media presentation', '全屏媒體演出', 'Media em tela cheia',
   '播放全屏影片、Lottie、音效或劇情圖，用於到達、通關與獎勵時刻。', 'Plays fullscreen video, Lottie, audio or story art.', '播放全屏影片、Lottie、音效或劇情圖，用於到達、通關與獎勵時刻。', 'Reproduz media em tela cheia.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'presentation.fullscreen_media', 'effectFamily', 'fullscreen_media'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('mediaAssetId', 'fallbackText')), 'high', 'published', 31010),
  ('presentation.rich_popup', 'presentation', 'rich_popup',
   '圖文彈窗展示', 'Rich popup presentation', '圖文彈窗展示', 'Janela rica',
   '顯示景點簡介、史實依據、行動按鈕與補充內容。', 'Shows intro, evidence and actions.', '顯示景點簡介、史實依據、行動按鈕與補充內容。', 'Mostra introducao e acoes.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'presentation.rich_popup', 'effectFamily', 'rich_popup'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('title', 'body')), 'normal', 'published', 31020),
  ('presentation.lottie_overlay', 'presentation', 'lottie_overlay',
   'Lottie 地圖疊加動畫', 'Lottie map overlay', 'Lottie 地圖疊加動畫', 'Sobreposicao Lottie',
   '在地圖或樓層圖上顯示可動態出現的 Lottie 疊加物。', 'Shows animated Lottie overlays on maps.', '在地圖或樓層圖上顯示可動態出現的 Lottie 疊加物。', 'Mostra Lottie no mapa.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'presentation.lottie_overlay', 'effectFamily', 'lottie_overlay'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('lottieAssetId')), 'normal', 'published', 31030),
  ('presentation.map_overlay', 'presentation', 'map_overlay',
   '地圖疊加物標記', 'Map overlay marker', '地圖疊加物標記', 'Marcador de mapa',
   '用於紅點、拾取物、任務點、路徑提示與 POI 視覺標記。', 'Used for pickup, task and route overlays.', '用於紅點、拾取物、任務點、路徑提示與 POI 視覺標記。', 'Usado para marcadores.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'presentation.map_overlay', 'effectFamily', 'map_overlay'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('position')), 'normal', 'published', 31040),
  ('display_condition.always', 'display_condition', 'always',
   '恆常顯示條件', 'Always visible condition', '恆常顯示條件', 'Sempre visivel',
   '無需特殊條件即可顯示，適合普通 POI 入口與基礎提示。', 'Always visible baseline condition.', '無需特殊條件即可顯示，適合普通 POI 入口與基礎提示。', 'Condicao sempre visivel.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'display_condition.always', 'condition', 'always'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('condition')), 'low', 'published', 31050),
  ('display_condition.proximity_radius', 'display_condition', 'proximity',
   '靠近範圍顯示條件', 'Proximity display condition', '靠近範圍顯示條件', 'Condicao de proximidade',
   '進入指定半徑後顯示內容或解鎖互動。', 'Shows content after entering a radius.', '進入指定半徑後顯示內容或解鎖互動。', 'Mostra conteudo por raio.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'display_condition.proximity_radius', 'condition', 'proximity'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('radiusMeters')), 'normal', 'published', 31060),
  ('display_condition.dwell_duration', 'display_condition', 'dwell',
   '停留時長顯示條件', 'Dwell duration condition', '停留時長顯示條件', 'Condicao de permanencia',
   '在場景內停留一定秒數後顯示隱藏內容。', 'Shows hidden content after dwell time.', '在場景內停留一定秒數後顯示隱藏內容。', 'Mostra conteudo por permanencia.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'display_condition.dwell_duration', 'condition', 'dwell'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('dwellSeconds')), 'normal', 'published', 31070),
  ('trigger_condition.tap', 'trigger_condition', 'tap',
   '點擊觸發條件', 'Tap trigger condition', '點擊觸發條件', 'Gatilho por toque',
   '點擊 POI、標記、疊加物或按鈕後觸發效果。', 'Triggers after tapping a target.', '點擊 POI、標記、疊加物或按鈕後觸發效果。', 'Aciona por toque.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'trigger_condition.tap', 'triggerType', 'tap'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('target')), 'low', 'published', 31080),
  ('trigger_condition.tap_sequence', 'trigger_condition', 'tap_sequence',
   '依序點擊觸發條件', 'Tap sequence condition', '依序點擊觸發條件', 'Sequencia de toques',
   '需要按指定順序點擊多個疊加物或標記。', 'Requires tapping targets in order.', '需要按指定順序點擊多個疊加物或標記。', 'Exige sequencia de toques.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'trigger_condition.tap_sequence', 'triggerType', 'tap_sequence'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('sequence')), 'normal', 'published', 31090),
  ('trigger_condition.photo_checkin', 'trigger_condition', 'photo_checkin',
   '拍照打卡觸發條件', 'Photo check-in trigger', '拍照打卡觸發條件', 'Check-in por foto',
   '拍攝指定對象或完成照片上報後觸發任務進度。', 'Triggers progress after photo submission.', '拍攝指定對象或完成照片上報後觸發任務進度。', 'Aciona por foto.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'trigger_condition.photo_checkin', 'triggerType', 'photo_checkin'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('photoTarget')), 'high', 'published', 31100),
  ('task_gameplay.quiz', 'task_gameplay', 'quiz',
   '問答挑戰玩法', 'Quiz gameplay', '問答挑戰玩法', 'Jogo de perguntas',
   '完成歷史、地點或故事知識問答，支持全答對通關。', 'Runs a quiz challenge.', '完成歷史、地點或故事知識問答，支持全答對通關。', 'Executa perguntas.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'task_gameplay.quiz', 'gameplay', 'quiz'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('questionCount', 'passMode')), 'normal', 'published', 31110),
  ('task_gameplay.cyber_incense', 'task_gameplay', 'cyber_incense',
   '賽博點香互動', 'Cyber incense gameplay', '賽博點香互動', 'Incenso digital',
   '以小遊戲形式完成點香祈願，可接獎勵、稱號與動畫演出。', 'Runs a cyber incense mini-game.', '以小遊戲形式完成點香祈願，可接獎勵、稱號與動畫演出。', 'Mini-jogo de incenso.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'task_gameplay.cyber_incense', 'gameplay', 'cyber_incense'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('gesture', 'successEffect')), 'normal', 'published', 31120),
  ('trigger_effect.grant_collectible', 'trigger_effect', 'grant_collectible',
   '發放收集物效果', 'Grant collectible effect', '發放收集物效果', 'Conceder colecionavel',
   '互動成功後把收集物寫入背包並產生探索事件。', 'Grants collectibles after interaction.', '互動成功後把收集物寫入背包並產生探索事件。', 'Concede colecionaveis.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'trigger_effect.grant_collectible', 'effectFamily', 'grant_collectible'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('collectibleCode')), 'normal', 'published', 31130),
  ('trigger_effect.grant_badge_title', 'trigger_effect', 'grant_badge_title',
   '發放徽章與稱號效果', 'Grant badge and title effect', '發放徽章與稱號效果', 'Conceder titulo',
   '通關、全收集或隱藏挑戰成功後發放徽章與榮譽稱號。', 'Grants badges and honor titles.', '通關、全收集或隱藏挑戰成功後發放徽章與榮譽稱號。', 'Concede titulos.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'trigger_effect.grant_badge_title', 'effectFamily', 'grant_badge_title'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('badgeCode', 'titleCode')), 'high', 'published', 31140),
  ('reward_presentation.fullscreen_unlock', 'reward_presentation', 'fullscreen_unlock',
   '全屏解鎖獎勵演出', 'Fullscreen reward unlock', '全屏解鎖獎勵演出', 'Desbloqueio em tela cheia',
   '獲得稀有稱號、秘寶或章節通關時播放全屏動畫和音效。', 'Shows fullscreen reward animation.', '獲得稀有稱號、秘寶或章節通關時播放全屏動畫和音效。', 'Mostra recompensa em tela cheia.',
   JSON_OBJECT('schemaVersion', 1, 'presetCode', 'reward_presentation.fullscreen_unlock', 'effectFamily', 'fullscreen_reward'),
   JSON_OBJECT('schemaVersion', 1, 'required', JSON_ARRAY('rewardCode', 'presentationAssetId')), 'high', 'published', 31150)
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
) VALUES (
  'phase31_governance_conflict_flow',
  'default_overlay',
  'manual',
  'Phase 31 治理衝突示例流程',
  'Phase 31 governance conflict fixture flow',
  'Phase 31 治理衝突示例流程',
  'Fluxo de conflitos Phase 31',
  '用於驗證全屏效果重疊、重複獎勵與治理中心詳情展示。',
  'Fixture for fullscreen overlap and duplicate reward governance.',
  '用於驗證全屏效果重疊、重複獎勵與治理中心詳情展示。',
  'Fixture de governanca.',
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase31'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase31'),
  'published',
  31910,
  NOW()
) ON DUPLICATE KEY UPDATE
  `flow_type` = VALUES(`flow_type`),
  `mode` = VALUES(`mode`),
  `name_zh` = VALUES(`name_zh`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_zht` = VALUES(`description_zht`),
  `map_policy_json` = VALUES(`map_policy_json`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

SET @phase31_flow_id = (SELECT `id` FROM `experience_flows` WHERE `code` = 'phase31_governance_conflict_flow' AND `deleted` = 0 LIMIT 1);
SET @tpl_fullscreen_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'presentation.fullscreen_media' AND `deleted` = 0 LIMIT 1);
SET @tpl_quiz_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'task_gameplay.quiz' AND `deleted` = 0 LIMIT 1);
SET @tpl_badge_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'trigger_effect.grant_badge_title' AND `deleted` = 0 LIMIT 1);

INSERT INTO `experience_bindings` (
  `owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `priority`, `inherit_policy`, `status`, `sort_order`
) VALUES (
  'manual_target', NULL, 'phase31_conflict_scope', 'default_experience_flow', @phase31_flow_id, 0, 'inherit', 'published', 31910
) ON DUPLICATE KEY UPDATE
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
  (@phase31_flow_id, 'phase31_fullscreen_a', 'fullscreen_media', @tpl_fullscreen_id,
   '治理示例全屏演出 A', 'Governance fullscreen A', '治理示例全屏演出 A', 'Media A',
   '同一主體同一觸發條件下的第一個全屏效果。', 'First fullscreen fixture.', '同一主體同一觸發條件下的第一個全屏效果。', 'Fixture A.',
   'tap', JSON_OBJECT('schemaVersion', 1, 'triggerType', 'tap'), JSON_OBJECT('schemaVersion', 1, 'condition', 'always'), JSON_OBJECT('schemaVersion', 1, 'effectFamily', 'fullscreen_media', 'reward', 'badge_title'),
   NULL, JSON_ARRAY('phase31_badge_title'), 'medium', 1, 'phase31_conflict', 'published', 10),
  (@phase31_flow_id, 'phase31_fullscreen_b', 'fullscreen_media', @tpl_fullscreen_id,
   '治理示例全屏演出 B', 'Governance fullscreen B', '治理示例全屏演出 B', 'Media B',
   '同一主體同一觸發條件下的第二個全屏效果。', 'Second fullscreen fixture.', '同一主體同一觸發條件下的第二個全屏效果。', 'Fixture B.',
   'tap', JSON_OBJECT('schemaVersion', 1, 'triggerType', 'tap'), JSON_OBJECT('schemaVersion', 1, 'condition', 'always'), JSON_OBJECT('schemaVersion', 1, 'effectFamily', 'fullscreen_media', 'reward', 'badge_title'),
   NULL, JSON_ARRAY('phase31_badge_title'), 'medium', 1, 'phase31_conflict', 'published', 20),
  (@phase31_flow_id, 'phase31_quiz_reward', 'task_gameplay', @tpl_quiz_id,
   '治理示例問答任務', 'Governance quiz task', '治理示例問答任務', 'Quiz',
   '使用問答模板並在完成後發放相同稱號獎勵。', 'Quiz fixture that grants the same reward.', '使用問答模板並在完成後發放相同稱號獎勵。', 'Fixture quiz.',
   'task_complete', JSON_OBJECT('schemaVersion', 1, 'triggerType', 'task_complete'), JSON_OBJECT('schemaVersion', 1, 'condition', 'quiz_passed'), JSON_OBJECT('schemaVersion', 1, 'effectFamily', 'grant_badge_title'),
   NULL, JSON_ARRAY('phase31_badge_title'), 'core', 1, 'phase31_conflict', 'published', 30)
ON DUPLICATE KEY UPDATE
  `step_type` = VALUES(`step_type`),
  `template_id` = VALUES(`template_id`),
  `step_name_zh` = VALUES(`step_name_zh`),
  `step_name_zht` = VALUES(`step_name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_zht` = VALUES(`description_zht`),
  `trigger_type` = VALUES(`trigger_type`),
  `trigger_config_json` = VALUES(`trigger_config_json`),
  `condition_config_json` = VALUES(`condition_config_json`),
  `effect_config_json` = VALUES(`effect_config_json`),
  `reward_rule_ids_json` = VALUES(`reward_rule_ids_json`),
  `exploration_weight_level` = VALUES(`exploration_weight_level`),
  `required_for_completion` = VALUES(`required_for_completion`),
  `inherit_key` = VALUES(`inherit_key`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

SET @phase31_chapter_id = COALESCE((SELECT `id` FROM `story_chapters` WHERE `anchor_target_code` = 'ama_temple' LIMIT 1), 310001);
DELETE FROM `experience_overrides`
WHERE `owner_type` = 'story_chapter'
  AND `owner_id` = @phase31_chapter_id
  AND `target_owner_type` = 'poi'
  AND `target_owner_id` = 0
  AND `target_step_code` = 'phase31_required_arrival_media'
  AND `override_mode` = 'disable';
INSERT INTO `experience_overrides` (
  `owner_type`, `owner_id`, `target_owner_type`, `target_owner_id`, `target_step_code`,
  `override_mode`, `replacement_step_id`, `override_config_json`, `status`, `sort_order`
) VALUES (
  'story_chapter', @phase31_chapter_id, 'poi', 0, 'phase31_required_arrival_media',
  'disable', NULL, JSON_OBJECT('schemaVersion', 1, 'reason', 'Phase 31 governance fixture: required step disabled without replacement'), 'published', 31920
);

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase31-interaction-template-governance',
  'Phase 31 reusable template library and governance conflict fixtures',
  'completed',
  NOW(),
  'Seeds canonical interaction/task templates, usage refs and deterministic governance conflicts.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
