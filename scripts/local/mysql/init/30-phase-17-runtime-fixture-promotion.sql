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

UPDATE `indoor_nodes`
SET
  `runtime_support_level` = 'phase17_supported',
  `status` = 'published',
  `node_name_zh` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '夜市流光幕'
    WHEN '1f-phase15-royal-palace-dwell' THEN '皇家海鮮舫回響'
    WHEN '1f-phase15-zipcity-path' THEN '飛索引路光軌'
    ELSE `node_name_zh`
  END,
  `node_name_en` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN 'Night Market Light Veil'
    WHEN '1f-phase15-royal-palace-dwell' THEN 'Royal Palace Echo'
    WHEN '1f-phase15-zipcity-path' THEN 'Zipcity Guiding Trail'
    ELSE `node_name_en`
  END,
  `node_name_zht` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '夜市流光幕'
    WHEN '1f-phase15-royal-palace-dwell' THEN '皇家海鮮舫回響'
    WHEN '1f-phase15-zipcity-path' THEN '飛索引路光軌'
    ELSE `node_name_zht`
  END,
  `node_name_pt` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN 'Véu Luminoso do Mercado Noturno'
    WHEN '1f-phase15-royal-palace-dwell' THEN 'Eco do Royal Palace'
    WHEN '1f-phase15-zipcity-path' THEN 'Trilho Guia da Zipcity'
    ELSE `node_name_pt`
  END,
  `description_zh` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '夜色降臨後，葡京人夜市邊界會泛起一層流光，提醒旅人這段只在晚間甦醒的故事即將展開。'
    WHEN '1f-phase15-royal-palace-dwell' THEN '在皇家海鮮舫前停留片刻，舊日宴會的回響會被喚醒，先送上一段故事，再提示需要登入後才能領取的收藏物。'
    WHEN '1f-phase15-zipcity-path' THEN '點亮飛索入口後，一道引路光軌會沿著中庭邊界滑行，帶你前往下一個互動節點。'
    ELSE `description_zh`
  END,
  `description_en` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN 'After dusk, a luminous veil rises along the Lisboeta night market boundary and opens the evening story lane.'
    WHEN '1f-phase15-royal-palace-dwell' THEN 'Pause beside the Royal Palace long enough and the banquet echo answers with a story beat and a login-gated collectible hint.'
    WHEN '1f-phase15-zipcity-path' THEN 'Once the Zipcity entrance is activated, a guiding light trail sweeps along the atrium edge toward the next interactive stop.'
    ELSE `description_en`
  END,
  `description_zht` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '夜色降臨後，葡京人夜市邊界會泛起一層流光，提醒旅人這段只在晚間甦醒的故事即將展開。'
    WHEN '1f-phase15-royal-palace-dwell' THEN '在皇家海鮮舫前停留片刻，舊日宴會的回響會被喚醒，先送上一段故事，再提示需要登入後才能領取的收藏物。'
    WHEN '1f-phase15-zipcity-path' THEN '點亮飛索入口後，一道引路光軌會沿著中庭邊界滑行，帶你前往下一個互動節點。'
    ELSE `description_zht`
  END,
  `description_pt` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN 'Depois do anoitecer, um véu luminoso desperta ao longo do mercado noturno e abre a narrativa da noite.'
    WHEN '1f-phase15-royal-palace-dwell' THEN 'Se o visitante permanecer junto ao Royal Palace, o eco do antigo salão devolve uma narrativa e indica um colecionável que exige login.'
    WHEN '1f-phase15-zipcity-path' THEN 'Depois de ativar a entrada da Zipcity, um trilho luminoso conduz o visitante até ao próximo ponto interativo.'
    ELSE `description_pt`
  END,
  `popup_config_json` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '{"enabled":true,"mode":"sheet","title":"夜市流光幕"}'
    WHEN '1f-phase15-royal-palace-dwell' THEN '{"enabled":true,"mode":"popup","title":"皇家海鮮舫回響"}'
    WHEN '1f-phase15-zipcity-path' THEN '{"enabled":true,"mode":"bubble","title":"飛索引路光軌"}'
    ELSE `popup_config_json`
  END,
  `display_config_json` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '{"labelMode":"hover","showPulse":false,"theme":"night-market"}'
    WHEN '1f-phase15-royal-palace-dwell' THEN '{"labelMode":"always","showPulse":true,"theme":"royal-palace"}'
    WHEN '1f-phase15-zipcity-path' THEN '{"labelMode":"always","showPulse":true,"theme":"zipcity"}'
    ELSE `display_config_json`
  END,
  `overlay_geometry_json` = CASE `marker_code`
    WHEN '1f-phase15-night-market-overlay' THEN '{"geometryType":"polygon","points":[{"x":0.175000,"y":0.585000,"order":0},{"x":0.316000,"y":0.585000,"order":1},{"x":0.332000,"y":0.680000,"order":2},{"x":0.196000,"y":0.704000,"order":3}],"properties":{"label":"夜市流光區","theme":"golden-night"}}'
    WHEN '1f-phase15-zipcity-path' THEN '{"geometryType":"polyline","points":[{"x":0.104000,"y":0.633000,"order":0},{"x":0.173000,"y":0.604000,"order":1},{"x":0.238000,"y":0.595000,"order":2},{"x":0.306000,"y":0.586000,"order":3}],"properties":{"label":"飛索引路光軌","style":"guiding-trail"}}'
    ELSE `overlay_geometry_json`
  END
WHERE `floor_id` = @lisboeta_floor_1f_id
  AND `marker_code` IN (
    '1f-phase15-night-market-overlay',
    '1f-phase15-royal-palace-dwell',
    '1f-phase15-zipcity-path'
  );

UPDATE `indoor_node_behaviors` b
JOIN `indoor_nodes` n ON n.`id` = b.`node_id`
SET
  b.`runtime_support_level` = 'phase17_supported',
  b.`status` = 'published',
  b.`behavior_name_zh` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '夜市揭幕光幕'
    WHEN 'royal-palace-dwell-reveal' THEN '海鮮舫回響'
    WHEN 'zipcity-guiding-path' THEN '飛索引路光軌'
    ELSE b.`behavior_name_zh`
  END,
  b.`behavior_name_en` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN 'Night Market Opening Veil'
    WHEN 'royal-palace-dwell-reveal' THEN 'Royal Palace Echo'
    WHEN 'zipcity-guiding-path' THEN 'Zipcity Guiding Trail'
    ELSE b.`behavior_name_en`
  END,
  b.`behavior_name_zht` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '夜市揭幕光幕'
    WHEN 'royal-palace-dwell-reveal' THEN '海鮮舫回響'
    WHEN 'zipcity-guiding-path' THEN '飛索引路光軌'
    ELSE b.`behavior_name_zht`
  END,
  b.`behavior_name_pt` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN 'Abertura Luminosa do Mercado'
    WHEN 'royal-palace-dwell-reveal' THEN 'Eco do Royal Palace'
    WHEN 'zipcity-guiding-path' THEN 'Trilho Guia da Zipcity'
    ELSE b.`behavior_name_pt`
  END,
  b.`appearance_rules_json` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '[{"id":"appearance-night","category":"schedule_window","label":"夜間時段","config":{"startAt":"19:00","endAt":"23:30"}}]'
    WHEN 'royal-palace-dwell-reveal' THEN '[{"id":"appearance-always","category":"always_on","label":"常駐顯示","config":{"note":"等待旅人停留"}}]'
    WHEN 'zipcity-guiding-path' THEN '[{"id":"appearance-manual","category":"manual","label":"手動喚醒","config":{"note":"等待第一下點擊"}}]'
    ELSE b.`appearance_rules_json`
  END,
  b.`trigger_rules_json` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '[{"id":"trigger-night-near","category":"proximity","label":"靠近夜市入口","config":{"radiusMeters":35,"targetCode":"night-market-gate"}}]'
    WHEN 'royal-palace-dwell-reveal' THEN '[{"id":"trigger-dwell-echo","category":"dwell","label":"停留喚醒回響","config":{"seconds":10}}]'
    WHEN 'zipcity-guiding-path' THEN '[{"id":"trigger-zipcity-tap","category":"tap","label":"點擊飛索入口","config":{"targetHint":"入口光點"}},{"id":"trigger-zipcity-follow","category":"proximity","label":"沿光軌前行","dependsOnTriggerId":"trigger-zipcity-tap","config":{"radiusMeters":18,"targetCode":"zipcity-trail-end"}}]'
    ELSE b.`trigger_rules_json`
  END,
  b.`effect_rules_json` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '[{"id":"effect-night-popup","category":"popup","label":"顯示夜市揭幕故事","config":{"title":"夜市流光幕","body":"夜市邊界在入夜後化作故事舞台，提醒旅人晚間篇章已經開場。"}}]'
    WHEN 'royal-palace-dwell-reveal' THEN '[{"id":"effect-echo-popup","category":"popup","label":"顯示海鮮舫回響","config":{"title":"皇家海鮮舫回響","body":"你已停留足夠久，舊日宴席的回響開始向你低聲講述。"}},{"id":"effect-echo-collectible","category":"collectible_grant","label":"登入後解鎖收藏物","config":{"entityId":1,"quantity":1}}]'
    WHEN 'zipcity-guiding-path' THEN '[{"id":"effect-zipcity-path","category":"path_motion","label":"播放引路光軌","config":{"trail":"zipcity-guiding"}},{"id":"effect-zipcity-bubble","category":"bubble","label":"提示下一站","config":{"title":"沿著光軌前行","body":"光軌會帶你走向中庭的下一個互動節點。"}}]'
    ELSE b.`effect_rules_json`
  END,
  b.`path_graph_json` = CASE b.`behavior_code`
    WHEN 'night-market-schedule-overlay' THEN '{"points":[{"x":0.175000,"y":0.585000,"order":0},{"x":0.316000,"y":0.585000,"order":1},{"x":0.332000,"y":0.680000,"order":2},{"x":0.196000,"y":0.704000,"order":3}],"durationMs":2400,"holdMs":0,"loop":false,"easing":"linear"}'
    WHEN 'royal-palace-dwell-reveal' THEN '{"points":[],"durationMs":1200,"holdMs":400,"loop":false,"easing":"ease-in-out"}'
    WHEN 'zipcity-guiding-path' THEN '{"points":[{"x":0.104000,"y":0.633000,"order":0},{"x":0.158000,"y":0.612000,"order":1},{"x":0.226000,"y":0.602000,"order":2},{"x":0.306000,"y":0.586000,"order":3}],"durationMs":3600,"holdMs":600,"loop":false,"easing":"ease-in-out"}'
    ELSE b.`path_graph_json`
  END
WHERE n.`floor_id` = @lisboeta_floor_1f_id
  AND b.`behavior_code` IN (
    'night-market-schedule-overlay',
    'royal-palace-dwell-reveal',
    'zipcity-guiding-path'
  );
