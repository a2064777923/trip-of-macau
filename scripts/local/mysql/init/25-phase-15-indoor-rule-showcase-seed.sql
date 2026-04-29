USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @lisboeta_building_id := (
  SELECT `id`
  FROM `buildings`
  WHERE `building_code` = 'lisboeta_macau'
  ORDER BY `id` DESC
  LIMIT 1
);

SET @lisboeta_floor_1f_id := (
  SELECT `id`
  FROM `indoor_floors`
  WHERE `building_id` = @lisboeta_building_id
    AND `floor_code` = '1F'
  ORDER BY `id` DESC
  LIMIT 1
);

DELETE b
FROM `indoor_node_behaviors` b
JOIN `indoor_nodes` n ON n.`id` = b.`node_id`
WHERE n.`floor_id` = @lisboeta_floor_1f_id
  AND n.`marker_code` IN (
    '1f-phase15-night-market-overlay',
    '1f-phase15-royal-palace-dwell',
    '1f-phase15-zipcity-path'
  );

DELETE FROM `indoor_nodes`
WHERE `floor_id` = @lisboeta_floor_1f_id
  AND `marker_code` IN (
    '1f-phase15-night-market-overlay',
    '1f-phase15-royal-palace-dwell',
    '1f-phase15-zipcity-path'
  );

INSERT INTO `indoor_nodes` (
  `building_id`,
  `floor_id`,
  `marker_code`,
  `node_type`,
  `presentation_mode`,
  `overlay_type`,
  `node_name_zh`,
  `node_name_en`,
  `node_name_zht`,
  `node_name_pt`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `description_pt`,
  `relative_x`,
  `relative_y`,
  `tags`,
  `popup_config_json`,
  `display_config_json`,
  `overlay_geometry_json`,
  `inherit_linked_entity_rules`,
  `runtime_support_level`,
  `metadata_json`,
  `sort_order`,
  `status`
)
SELECT
  @lisboeta_building_id,
  @lisboeta_floor_1f_id,
  seed.`marker_code`,
  seed.`node_type`,
  seed.`presentation_mode`,
  seed.`overlay_type`,
  seed.`node_name_zh`,
  seed.`node_name_en`,
  seed.`node_name_zht`,
  seed.`node_name_pt`,
  seed.`description_zh`,
  seed.`description_en`,
  seed.`description_zht`,
  seed.`description_pt`,
  seed.`relative_x`,
  seed.`relative_y`,
  seed.`tags`,
  seed.`popup_config_json`,
  seed.`display_config_json`,
  seed.`overlay_geometry_json`,
  seed.`inherit_linked_entity_rules`,
  seed.`runtime_support_level`,
  seed.`metadata_json`,
  seed.`sort_order`,
  seed.`status`
FROM (
  SELECT
    '1f-phase15-night-market-overlay' AS `marker_code`,
    'landmark' AS `node_type`,
    'overlay' AS `presentation_mode`,
    'polygon' AS `overlay_type`,
    '夜市流光浮幕' AS `node_name_zh`,
    'Night Market Luminous Veil' AS `node_name_en`,
    '夜市流光浮幕' AS `node_name_zht`,
    'Veu Luminoso do Mercado Noturno' AS `node_name_pt`,
    '当暮色落在葡京人夜市，光幕会沿着摊档边界亮起，提醒旅人这一段故事只在夜里苏醒。' AS `description_zh`,
    'A luminous overlay wakes along the night market boundary after dusk, inviting travellers into the evening story lane.' AS `description_en`,
    '當暮色落在葡京人夜市，光幕會沿著攤檔邊界亮起，提醒旅人這一段故事只在夜裡甦醒。' AS `description_zht`,
    'Ao cair da noite, um veu luminoso desperta ao longo do mercado e conduz o visitante para a narrativa noturna.' AS `description_pt`,
    0.249000 AS `relative_x`,
    0.634000 AS `relative_y`,
    '["phase15","showcase","night-market","overlay"]' AS `tags`,
    '{"enabled":true,"mode":"sheet","title":"夜市光幕"}' AS `popup_config_json`,
    '{"labelMode":"hover","showPulse":false,"theme":"night-market"}' AS `display_config_json`,
    '{"geometryType":"polygon","points":[{"x":0.175000,"y":0.585000,"order":0},{"x":0.316000,"y":0.585000,"order":1},{"x":0.332000,"y":0.680000,"order":2},{"x":0.196000,"y":0.704000,"order":3}],"properties":{"label":"夜市流光區","theme":"golden-night"}}' AS `overlay_geometry_json`,
    0 AS `inherit_linked_entity_rules`,
    'phase16_supported' AS `runtime_support_level`,
    '{"showcase":"phase15","scene":"night_market_overlay"}' AS `metadata_json`,
    9101 AS `sort_order`,
    'published' AS `status`
  UNION ALL
  SELECT
    '1f-phase15-royal-palace-dwell',
    'landmark',
    'marker',
    NULL,
    '海鲜舫回声藏章',
    'Royal Palace Echo Token',
    '海鮮舫回聲藏章',
    'Eco do Royal Palace',
    '旅人若在皇家海鲜舫前静立片刻，旧日宴席的回声便会换来一枚故事藏章。',
    'Pause by Royal Palace and the banquet hall echo answers with a collectible story token.',
    '旅人若在皇家海鮮舫前靜立片刻，舊日宴席的回聲便會換來一枚故事藏章。',
    'Se o visitante permanecer por alguns instantes diante do Royal Palace, um eco narrativo concede um selo colecionavel.',
    0.272000,
    0.796000,
    '["phase15","showcase","dwell","collectible"]',
    '{"enabled":true,"mode":"popup","title":"海鮮舫回聲"}',
    '{"labelMode":"always","showPulse":true,"theme":"royal-palace"}',
    NULL,
    0,
    'phase16_supported',
    '{"showcase":"phase15","scene":"royal_palace_dwell"}',
    9102,
    'published'
  UNION ALL
  SELECT
    '1f-phase15-zipcity-path',
    'landmark',
    'hybrid',
    'polyline',
    '飞索引路光迹',
    'Zipcity Guiding Trail',
    '飛索引路光跡',
    'Rasto Guia da Zipcity',
    '在飞索入口触发第一道光点后，光迹会沿着中庭边线缓缓游走，带玩家走向下一段互动。',
    'Once the first point is tapped at the Zipcity entrance, a guiding trail glides along the atrium edge toward the next interaction.',
    '在飛索入口觸發第一道光點後，光跡會沿著中庭邊線緩緩游走，帶玩家走向下一段互動。',
    'Depois do primeiro toque na entrada da Zipcity, um rasto luminoso percorre lentamente o atrio e conduz ao proximo passo.',
    0.104000,
    0.633000,
    '["phase15","showcase","path-motion","chain"]',
    '{"enabled":true,"mode":"bubble","title":"飛索引路"}',
    '{"labelMode":"always","showPulse":true,"theme":"zipcity"}',
    '{"geometryType":"polyline","points":[{"x":0.104000,"y":0.633000,"order":0},{"x":0.173000,"y":0.604000,"order":1},{"x":0.238000,"y":0.595000,"order":2},{"x":0.306000,"y":0.586000,"order":3}],"properties":{"label":"光跡投影","style":"guiding-trail"}}',
    1,
    'phase16_supported',
    '{"showcase":"phase15","scene":"zipcity_path_chain"}',
    9103,
    'published'
) seed
WHERE @lisboeta_floor_1f_id IS NOT NULL;

INSERT INTO `indoor_node_behaviors` (
  `node_id`,
  `behavior_code`,
  `behavior_name_zh`,
  `behavior_name_en`,
  `behavior_name_zht`,
  `behavior_name_pt`,
  `appearance_preset_code`,
  `trigger_template_code`,
  `effect_template_code`,
  `appearance_rules_json`,
  `trigger_rules_json`,
  `effect_rules_json`,
  `path_graph_json`,
  `inherit_mode`,
  `runtime_support_level`,
  `sort_order`,
  `status`
)
SELECT
  n.`id`,
  seed.`behavior_code`,
  seed.`behavior_name_zh`,
  seed.`behavior_name_en`,
  seed.`behavior_name_zht`,
  seed.`behavior_name_pt`,
  seed.`appearance_preset_code`,
  seed.`trigger_template_code`,
  seed.`effect_template_code`,
  seed.`appearance_rules_json`,
  seed.`trigger_rules_json`,
  seed.`effect_rules_json`,
  seed.`path_graph_json`,
  seed.`inherit_mode`,
  seed.`runtime_support_level`,
  seed.`sort_order`,
  seed.`status`
FROM `indoor_nodes` n
JOIN (
  SELECT
    '1f-phase15-night-market-overlay' AS `marker_code`,
    'night-market-schedule-overlay' AS `behavior_code`,
    '夜市开场光幕' AS `behavior_name_zh`,
    'Night Market Opening Veil' AS `behavior_name_en`,
    '夜市開場光幕' AS `behavior_name_zht`,
    'Abertura Luminosa do Mercado' AS `behavior_name_pt`,
    'night-market-schedule' AS `appearance_preset_code`,
    'proximity' AS `trigger_template_code`,
    'popup' AS `effect_template_code`,
    '[{"id":"appearance-night","category":"schedule_window","label":"夜間時段","config":{"startAt":"19:00","endAt":"23:30"}}]' AS `appearance_rules_json`,
    '[{"id":"trigger-night-near","category":"proximity","label":"靠近夜市入口","config":{"radiusMeters":35,"targetCode":"night-market-gate"}}]' AS `trigger_rules_json`,
    '[{"id":"effect-night-popup","category":"popup","label":"顯示夜市浮幕介紹","config":{"title":"夜市流光浮幕","body":"暮色亮起後，攤檔邊界會成為故事舞台。"}}]' AS `effect_rules_json`,
    '{"points":[{"x":0.175000,"y":0.585000,"order":0},{"x":0.316000,"y":0.585000,"order":1},{"x":0.332000,"y":0.680000,"order":2},{"x":0.196000,"y":0.704000,"order":3}],"durationMs":2400,"holdMs":0,"loop":false,"easing":"linear"}' AS `path_graph_json`,
    'override' AS `inherit_mode`,
    'phase16_supported' AS `runtime_support_level`,
    0 AS `sort_order`,
    'published' AS `status`
  UNION ALL
  SELECT
    '1f-phase15-royal-palace-dwell',
    'royal-palace-dwell-reveal',
    '宴席回声藏章',
    'Banquet Echo Token',
    '宴席回聲藏章',
    'Selo do Eco do Banquete',
    'royal-palace-dwell',
    'dwell',
    'collectible_grant',
    '[{"id":"appearance-always","category":"always_on","label":"常駐顯示","config":{"note":"等待旅人駐足"}}]',
    '[{"id":"trigger-dwell-echo","category":"dwell","label":"停留聆聽回聲","config":{"seconds":10}}]',
    '[{"id":"effect-echo-popup","category":"popup","label":"顯示宴席回聲","config":{"title":"海鮮舫回聲","body":"你在此停留得足夠久，便聽見往日宴席的低語。"}},{"id":"effect-echo-collectible","category":"collectible_grant","label":"發放故事藏章","config":{"entityId":1,"quantity":1}}]',
    '{"points":[],"durationMs":1200,"holdMs":400,"loop":false,"easing":"ease-in-out"}',
    'override',
    'phase16_supported',
    0,
    'published'
  UNION ALL
  SELECT
    '1f-phase15-zipcity-path',
    'zipcity-guiding-path',
    '飞索引路光迹',
    'Zipcity Guiding Trail',
    '飛索引路光跡',
    'Rasto Guia da Zipcity',
    'zipcity-guiding-path',
    'tap-chain',
    'path-motion',
    '[{"id":"appearance-manual","category":"manual","label":"點亮後啟動","config":{"note":"等待第一道觸發"}}]',
    '[{"id":"trigger-zipcity-tap","category":"tap","label":"點擊飛索入口","config":{"targetHint":"入口光點"}},{"id":"trigger-zipcity-follow","category":"proximity","label":"跟隨光跡前行","dependsOnTriggerId":"trigger-zipcity-tap","config":{"radiusMeters":18,"targetCode":"zipcity-trail-end"}}]',
    '[{"id":"effect-zipcity-path","category":"path_motion","label":"播放引路光跡","config":{"trail":"zipcity-guiding"}},{"id":"effect-zipcity-popup","category":"popup","label":"提示下一站","config":{"title":"跟著光跡前行","body":"沿著光路走，下一個互動會在中庭邊線醒來。"}}]',
    '{"points":[{"x":0.104000,"y":0.633000,"order":0},{"x":0.158000,"y":0.612000,"order":1},{"x":0.226000,"y":0.602000,"order":2},{"x":0.306000,"y":0.586000,"order":3}],"durationMs":3600,"holdMs":600,"loop":false,"easing":"ease-in-out"}',
    'append',
    'phase16_supported',
    0,
    'published'
) seed ON seed.`marker_code` = n.`marker_code`
WHERE n.`floor_id` = @lisboeta_floor_1f_id;
