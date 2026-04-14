USE `aoxiaoyou`;

SET NAMES utf8mb4;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase6-mock-dataset-migration',
  'Phase 6 canonical migration of the mini-program mock dataset into live MySQL tables',
  'completed',
  NOW(),
  'Upserts cities, storylines, chapters, POIs, stamps, rewards, tips, notifications, and runtime settings for the final live cutover.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);

INSERT INTO `cities` (
  `id`, `code`,
  `name_zh`, `name_en`, `name_zht`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`,
  `country_code`, `center_lat`, `center_lng`, `default_zoom`,
  `unlock_type`, `unlock_condition_json`,
  `description_zh`, `description_en`, `description_zht`,
  `sort_order`, `status`, `published_at`, `deleted`, `_openid`
)
VALUES
  (
    1, 'macau',
    '澳门半岛', 'Macau Peninsula', '澳門半島',
    '世界遗产与老城故事线', 'World heritage and old-town story routes', '世界遺產與老城故事線',
    'MO', 22.198700, 113.543900, 14,
    'auto', JSON_OBJECT('mode', 'default'),
    '从妈阁到大三巴，串联澳门最核心的城市叙事与地标体验。',
    'From A-Ma Temple to the Ruins, this is the main narrative spine of Macau.',
    '從媽閣到大三巴，串聯澳門最核心的城市敘事與地標體驗。',
    1, 'published', NOW(), 0, ''
  ),
  (
    2, 'taipa',
    '氹仔旧城区', 'Taipa Old Town', '氹仔舊城區',
    '甜点、色彩与慢行街区', 'Pastel streets, desserts, and a slower rhythm', '甜點、色彩與慢行街區',
    'MO', 22.156300, 113.560600, 14,
    'location', JSON_OBJECT('mode', 'proximity', 'radiusMeters', 1200),
    '适合轻松拍照、慢游与收集城市细节的氹仔路线入口。',
    'A softer side of the city built for photo walks, desserts, and slow exploration.',
    '適合輕鬆拍照、慢遊與收集城市細節的氹仔路線入口。',
    2, 'published', NOW(), 0, ''
  ),
  (
    3, 'coloane',
    '路环海岸', 'Coloane Coast', '路環海岸',
    '海风、步道与放慢节奏的终章', 'Sea breeze, coastal walks, and slower story beats', '海風、步道與放慢節奏的終章',
    'MO', 22.119700, 113.569500, 13,
    'location', JSON_OBJECT('mode', 'proximity', 'radiusMeters', 1500),
    '以海岸步行和自然留白为主的路线区域，适合收束整段旅程。',
    'A coastal district for slower pacing, fresh air, and reflective story endings.',
    '以海岸步行和自然留白為主的路線區域，適合收束整段旅程。',
    3, 'published', NOW(), 0, ''
  ),
  (
    4, 'ecnu',
    CONVERT(0xe58d8ee4b89ce5b888e88c83e5a4a7e5ada6 USING utf8mb4), 'East China Normal University', CONVERT(0xe88fafe69db1e5b8abe7af84e5a4a7e5adb8 USING utf8mb4),
    CONVERT(0xe9ab98e6a0a1e6a0a1e59bade68ea2e7b4a2e5ae9ee9aa8ce58cba USING utf8mb4), 'University campus exploration sandbox', CONVERT(0xe9ab98e6a0a1e6a0a1e59c92e68ea2e7b4a2e5afa6e9a997e58d80 USING utf8mb4),
    'CN', 31.228120, 121.406270, 15,
    'manual', JSON_OBJECT('mode', 'archived'),
    CONVERT(0xe4bd9ce4b8bae58d8ee4b89ce5b888e88c83e5a4a7e5ada6e6a0a1e59bade59cb0e59bbee4b88ee5908ee7bbade5aea4e58685e883bde58a9be8a784e58892e79a84e9a284e79599e59f8ee5b882e585a5e58fa3efbc8ce5bd93e5898de4bf9de79599e4b8bae88d89e7a8bfe38082 USING utf8mb4),
    'Reserved as the ECNU campus map entry for future campus and indoor authoring work, currently kept in draft.',
    CONVERT(0xe4bd9ce782bae88fafe69db1e5b8abe7af84e5a4a7e5adb8e6a0a1e59c92e59cb0e59c96e88887e5be8ce7ba8ce5aea4e585a7e883bde58a9be8a68fe58a83e79a84e9a090e79599e59f8ee5b882e585a5e58fa3efbc8ce795b6e5898de4bf9de79599e782bae88d89e7a8bfe38082 USING utf8mb4),
    99, 'draft', NULL, 0, ''
  )
ON DUPLICATE KEY UPDATE
  `code` = VALUES(`code`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_en` = VALUES(`subtitle_en`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `country_code` = VALUES(`country_code`),
  `center_lat` = VALUES(`center_lat`),
  `center_lng` = VALUES(`center_lng`),
  `default_zoom` = VALUES(`default_zoom`),
  `unlock_type` = VALUES(`unlock_type`),
  `unlock_condition_json` = VALUES(`unlock_condition_json`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `storylines` (
  `id`, `city_id`, `code`,
  `name_zh`, `name_en`, `name_zht`,
  `description_zh`, `description_en`, `description_zht`,
  `estimated_minutes`, `difficulty`,
  `reward_badge_zh`, `reward_badge_en`, `reward_badge_zht`,
  `status`, `sort_order`, `published_at`, `deleted`
)
VALUES
  (
    1, 1, 'maritime_silk_road',
    '海上丝路', 'Maritime Silk Road', '海上絲路',
    '从妈阁庙出发，沿着海风和旧港记忆理解澳门最早的城市叙事。',
    'Start from A-Ma Temple and follow the waterfront memory of early Macau.',
    '從媽閣廟出發，沿著海風和舊港記憶理解澳門最早的城市敘事。',
    150, 'easy',
    '航海学徒', 'Voyage Apprentice', '航海學徒',
    'published', 1, NOW(), 0
  ),
  (
    2, 1, 'east_meets_west',
    '东西相遇', 'East Meets West', '東西相遇',
    '从大三巴到玫瑰堂，感受宗教、战事与共处如何塑造澳门老城。',
    'From the Ruins to St Dominic Church, trace how conflict and coexistence shaped old Macau.',
    '從大三巴到玫瑰堂，感受宗教、戰事與共處如何塑造澳門老城。',
    210, 'medium',
    '旧城见证者', 'Witness of the Old City', '舊城見證者',
    'published', 2, NOW(), 0
  ),
  (
    3, 2, 'taipa_leisure_walk',
    '氹仔慢行', 'Taipa Leisure Walk', '氹仔慢行',
    '以色彩街区、甜点和街角细节为主的轻松路线，适合慢慢收藏城市气味。',
    'A gentle Taipa route built around pastel facades, dessert stops, and quiet corners.',
    '以色彩街區、甜點和街角細節為主的輕鬆路線，適合慢慢收藏城市氣味。',
    105, 'easy',
    '慢游收藏家', 'Slow Walk Collector', '慢遊收藏家',
    'published', 3, NOW(), 0
  ),
  (
    4, 3, 'coastal_echoes',
    '海岸回响', 'Coastal Echoes', '海岸回響',
    '沿着路环海岸散步，把整趟旅程的节奏慢下来，留给风和海浪一点位置。',
    'Slow the journey down along the Coloane coast and let the sea carry the last chapter.',
    '沿著路環海岸散步，把整趟旅程的節奏慢下來，留給風和海浪一點位置。',
    120, 'medium',
    '海风观察员', 'Coastal Observer', '海風觀察員',
    'published', 4, NOW(), 0
  ),
  (
    5, 4, 'campus_ghost_stories',
    '校园怪谈', 'Campus Ghost Stories', '校園怪談',
    '历史 mock 数据中的校园悬疑支线，保留为后台归档内容。',
    'A legacy campus mystery branch from the old mock dataset, kept as archived content.',
    '歷史 mock 數據中的校園懸疑支線，保留為後台歸檔內容。',
    60, 'hard',
    '怪谈探索者', 'Ghost Story Explorer', '怪談探索者',
    'draft', 99, NULL, 0
  )
ON DUPLICATE KEY UPDATE
  `city_id` = VALUES(`city_id`),
  `code` = VALUES(`code`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `estimated_minutes` = VALUES(`estimated_minutes`),
  `difficulty` = VALUES(`difficulty`),
  `reward_badge_zh` = VALUES(`reward_badge_zh`),
  `reward_badge_en` = VALUES(`reward_badge_en`),
  `reward_badge_zht` = VALUES(`reward_badge_zht`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `story_chapters` (
  `id`, `storyline_id`, `chapter_order`,
  `title_zh`, `title_en`, `title_zht`,
  `summary_zh`, `summary_en`, `summary_zht`,
  `detail_zh`, `detail_en`, `detail_zht`,
  `achievement_zh`, `achievement_en`, `achievement_zht`,
  `collectible_zh`, `collectible_en`, `collectible_zht`,
  `location_name_zh`, `location_name_en`, `location_name_zht`,
  `unlock_type`, `unlock_param_json`,
  `status`, `sort_order`, `published_at`, `deleted`, `_openid`
)
VALUES
  (1011, 1, 1, '序章：潮汐与信仰', 'Prologue: Tides and Faith', '序章：潮汐與信仰', '从妈阁的香火与海潮开始理解澳门。', 'Begin at A-Ma Temple with tide, incense, and early harbor memory.', '從媽閣的香火與海潮開始理解澳門。', '这一章让旅程回到澳门最早的海上入口，用最温和的方式展开整段故事线。', 'The route opens with the oldest harbor-side memory of the city and sets the tone for the journey.', '這一章讓旅程回到澳門最早的海上入口，用最溫和的方式展開整段故事線。', '解锁丝路序章印记', 'Unlock the Silk Road prologue mark', '解鎖絲路序章印記', '潮汐明信片', 'Tide postcard', '潮汐明信片', '妈阁庙', 'A-Ma Temple', '媽閣廟', 'sequence', NULL, 'published', 1, NOW(), 0, ''),
  (1012, 1, 2, '第一章：海风来信', 'Chapter 1: Letters from the Sea Breeze', '第一章：海風來信', '顺着海边步道，把港口记忆慢慢拼起来。', 'Follow the waterfront and piece together the harbor memory.', '順著海邊步道，把港口記憶慢慢拼起來。', '海风和旧码头的想象会把这条故事线从宗教叙事带向更宽的海上视角。', 'Sea breeze and harbor traces widen the story from faith into maritime exchange.', '海風和舊碼頭的想像會把這條故事線從宗教敘事帶向更寬的海上視角。', '解锁港口对话', 'Unlock harbor dialogue', '解鎖港口對話', '航海日记残页', 'Sailing journal fragment', '航海日記殘頁', '妈阁海边', 'A-Ma waterfront', '媽閣海邊', 'sequence', NULL, 'published', 2, NOW(), 0, ''),
  (1013, 1, 3, '第二章：港湾回声', 'Chapter 2: Harbor Echoes', '第二章：港灣回聲', '更多船影与港湾线索在前方等你靠近。', 'More ship silhouettes and harbor clues wait ahead.', '更多船影與港灣線索在前方等你靠近。', '当你继续向外走，故事不再只是一个地点，而是一整段航线与城市相遇的过程。', 'The story expands from a single stop into the wider route between ships and the city.', '當你繼續向外走，故事不再只是一個地點，而是一整段航線與城市相遇的過程。', '解锁海风徽记', 'Unlock the sea breeze emblem', '解鎖海風徽記', '港口徽章', 'Harbor badge', '港口徽章', '港湾步道', 'Harbor promenade', '港灣步道', 'sequence', NULL, 'published', 3, NOW(), 0, ''),
  (1014, 1, 4, '终章：夜航', 'Finale: Night Voyage', '終章：夜航', '把白天收集到的海上记忆带进夜色。', 'Carry the gathered maritime memory into the evening.', '把白天收集到的海上記憶帶進夜色。', '这一章让海上丝路从历史说明转成旅人自己的夜行体验。', 'The route closes by turning maritime history into a personal night walk.', '這一章讓海上絲路從歷史說明轉成旅人自己的夜行體驗。', '称号：夜航记录者', 'Title: Night Voyage Recorder', '稱號：夜航記錄者', '夜航星图', 'Night voyage star chart', '夜航星圖', '夜色海港', 'Night harbor', '夜色海港', 'sequence', NULL, 'published', 4, NOW(), 0, ''),
  (1021, 2, 1, '序章：城墙之外', 'Prologue: Beyond the City Wall', '序章：城牆之外', '站在大三巴前，读懂城市留下的痕迹。', 'Stand before the Ruins and read the city scars.', '站在大三巴前，讀懂城市留下的痕跡。', '这一章把老城最强烈的视觉记忆变成进入故事的入口。', 'The Ruins becomes the strongest visual gateway into the old city story.', '這一章把老城最強烈的視覺記憶變成進入故事的入口。', '解锁大三巴足迹', 'Unlock the Ruins footprint', '解鎖大三巴足跡', '城墙拓片', 'Wall rubbing', '城牆拓片', '大三巴牌坊', 'Ruins of St Paul', '大三巴牌坊', 'sequence', NULL, 'published', 1, NOW(), 0, ''),
  (1022, 2, 2, '第一章：炮台回声', 'Chapter 1: Cannon Echoes', '第一章：炮台回聲', '顺着高低起伏的坡道，把战事记忆重新拼起来。', 'Follow the slopes and rebuild the memory of past conflict.', '順著高低起伏的坡道，把戰事記憶重新拼起來。', '从石阶到高处视角，故事会把遗迹与城市防御联系起来。', 'The climb links the ruins to defense, height, and the city viewpoint.', '從石階到高處視角，故事會把遺跡與城市防禦聯繫起來。', '解锁冲突叙事', 'Unlock the conflict narration', '解鎖衝突敘事', '炮台徽印', 'Fortress seal', '炮台徽印', '炮台山', 'Fortress hill', '炮台山', 'sequence', NULL, 'published', 2, NOW(), 0, ''),
  (1023, 2, 3, '第二章：广场与教堂', 'Chapter 2: Square and Church', '第二章：廣場與教堂', '广场、人群与钟声，让老城节奏变得更柔和。', 'Square, crowds, and bells soften the rhythm of the old city.', '廣場、人群與鐘聲，讓老城節奏變得更柔和。', '当你从遗迹走向教堂，澳门的共处叙事会变得更完整。', 'The route shifts from ruins into coexistence as you move toward the church district.', '當你從遺跡走向教堂，澳門的共處敘事會變得更完整。', '解锁和平线索', 'Unlock the peace clue', '解鎖和平線索', '玫瑰窗碎片', 'Rose window fragment', '玫瑰窗碎片', '议事亭前地', 'Senado Square', '議事亭前地', 'sequence', NULL, 'published', 3, NOW(), 0, ''),
  (1024, 2, 4, '终章：和平之门', 'Finale: Gate of Peace', '終章：和平之門', '把所有线索收束成对澳门老城的新理解。', 'Gather every clue into a new understanding of old Macau.', '把所有線索收束成對澳門老城的新理解。', '这条故事线最终会把冲突、宗教与日常街区放到同一条叙事线上。', 'The finale joins conflict, faith, and everyday streets into one story arc.', '這條故事線最終會把衝突、宗教與日常街區放到同一條敘事線上。', '称号：旧城见证者', 'Title: Witness of the Old City', '稱號：舊城見證者', '和平纪念封', 'Peace commemorative cover', '和平紀念封', '玫瑰堂', 'St Dominic Church', '玫瑰堂', 'sequence', NULL, 'published', 4, NOW(), 0, ''),
  (1031, 3, 1, '序章：色彩外墙', 'Prologue: Painted Facades', '序章：色彩外牆', '从氹仔色彩鲜明的街屋开始，让节奏慢下来。', 'Start with the painted facades of Taipa and slow the route down.', '從氹仔色彩鮮明的街屋開始，讓節奏慢下來。', '这一章把氹仔最适合慢游与拍照的气氛先交给旅人。', 'The route opens with Taipa at its softest and most photogenic.', '這一章把氹仔最適合慢遊與拍照的氣氛先交給旅人。', '解锁慢行线索', 'Unlock the slow-walk clue', '解鎖慢行線索', '彩墙贴纸', 'Pastel facade sticker', '彩牆貼紙', '龙环葡韵', 'Taipa Houses', '龍環葡韻', 'sequence', NULL, 'published', 1, NOW(), 0, ''),
  (1032, 3, 2, '第一章：甜点小径', 'Chapter 1: Dessert Lane', '第一章：甜點小徑', '把甜点、街角与路人节奏一起收进旅程。', 'Collect dessert stops, corners, and the softer cadence of Taipa.', '把甜點、街角與路人節奏一起收進旅程。', '故事不追求快，而是让城市气味和街区肌理慢慢浮出来。', 'The route favors texture and atmosphere over speed.', '故事不追求快，而是讓城市氣味和街區肌理慢慢浮出來。', '解锁甜点标记', 'Unlock the dessert marker', '解鎖甜點標記', '蛋挞兑换券', 'Egg tart voucher', '蛋撻兌換券', '氹仔旧城', 'Taipa Old Town', '氹仔舊城', 'sequence', NULL, 'published', 2, NOW(), 0, ''),
  (1033, 3, 3, '终章：薄暮散步', 'Finale: Dusk Stroll', '終章：薄暮散步', '把白天的彩色街景换成适合傍晚散步的温柔节奏。', 'Turn the daytime color into a gentler dusk walk.', '把白天的彩色街景換成適合傍晚散步的溫柔節奏。', '收束这一条故事线时，氹仔更像被收藏下来的城市片段。', 'Taipa closes like a collected city fragment rather than a completed task.', '收束這一條故事線時，氹仔更像被收藏下來的城市片段。', '称号：慢游收藏家', 'Title: Slow Walk Collector', '稱號：慢遊收藏家', '晚光明信片', 'Dusk postcard', '晚光明信片', '海边小路', 'Coastal lane', '海邊小路', 'sequence', NULL, 'published', 3, NOW(), 0, ''),
  (1041, 4, 1, '序章：黑沙海岸', 'Prologue: Hac Sa Coast', '序章：黑沙海岸', '让风、海浪与黑沙先把脚步放慢。', 'Let the wind, waves, and black sand slow the walk down first.', '讓風、海浪與黑沙先把腳步放慢。', '故事从最直接的自然感受开始，为最后一段旅程留白。', 'The route starts with pure landscape so the final act can breathe.', '故事從最直接的自然感受開始，為最後一段旅程留白。', '解锁海岸旅程', 'Unlock the coastal route', '解鎖海岸旅程', '黑沙玻璃瓶', 'Black sand bottle', '黑沙玻璃瓶', '黑沙海滩', 'Hac Sa Beach', '黑沙海灘', 'sequence', NULL, 'published', 1, NOW(), 0, ''),
  (1042, 4, 2, '第一章：灯塔守望', 'Chapter 1: Watching the Beacon', '第一章：燈塔守望', '朝更远一点的海边继续走，让海岸视角更完整。', 'Walk farther toward the coast and widen the sea-facing view.', '朝更遠一點的海邊繼續走，讓海岸視角更完整。', '这段路线把城市边缘的安静感放到最前面。', 'This chapter foregrounds the quiet edge of the city.', '這段路線把城市邊緣的安靜感放到最前面。', '解锁海岸观察', 'Unlock the coastal lookout', '解鎖海岸觀察', '灯塔徽章', 'Beacon badge', '燈塔徽章', '路环海边', 'Coloane coast', '路環海邊', 'sequence', NULL, 'published', 2, NOW(), 0, ''),
  (1043, 4, 3, '终章：海雾日记', 'Finale: Sea Mist Notes', '終章：海霧日記', '把海风收进旅程结尾，让整段路线平静落下。', 'End the route by packing the sea breeze into your travel notes.', '把海風收進旅程結尾，讓整段路線平靜落下。', '路环的终章不是高潮，而是让人愿意停下来呼吸。', 'Coloane closes the trip through calm rather than spectacle.', '路環的終章不是高潮，而是讓人願意停下來呼吸。', '称号：海风观察员', 'Title: Coastal Observer', '稱號：海風觀察員', '海雾签章', 'Sea mist seal', '海霧簽章', '海岸步道', 'Seaside path', '海岸步道', 'sequence', NULL, 'published', 3, NOW(), 0, ''),
  (1051, 5, 1, '序章：夜色倒影', 'Prologue: Night Reflection', '序章：夜色倒影', '历史 mock 数据中的校园悬疑支线起点。', 'An archived starting point from the legacy campus mock route.', '歷史 mock 數據中的校園懸疑支線起點。', '这组章节只保留在后台，帮助后续内容迁移时核对旧数据。', 'These archived chapters remain for admin-side legacy data inspection only.', '這組章節只保留在後台，幫助後續內容遷移時核對舊數據。', '解锁旧档案', 'Unlock the archived file', '解鎖舊檔案', '旧校徽', 'Old campus crest', '舊校徽', '校园河道', 'Campus canal', '校園河道', 'sequence', NULL, 'draft', 1, NULL, 0, ''),
  (1052, 5, 2, '第一章：走廊脚步', 'Chapter 1: Corridor Footsteps', '第一章：走廊腳步', '保留在数据库中的历史 mock 章节。', 'A preserved chapter from the legacy mock dataset.', '保留在資料庫中的歷史 mock 章節。', '不面向正式发布，仅用于后台归档和核对。', 'Not part of the live Macau release; kept for archival review only.', '不面向正式發布，僅用於後台歸檔和核對。', '查看归档章节', 'Inspect the archived chapter', '查看歸檔章節', '旧借书证', 'Archived library card', '舊借書證', '文史楼', 'History Building', '文史樓', 'sequence', NULL, 'draft', 2, NULL, 0, ''),
  (1053, 5, 3, '终章：雨夜迷踪', 'Finale: Rainy Trace', '終章：雨夜迷蹤', '历史校园支线的结尾，保留为后台档案。', 'The archived ending of the legacy campus mystery branch.', '歷史校園支線的結尾，保留為後台檔案。', '这部分内容不会出现在正式游客端，但会保留在管理后台中。', 'This content stays out of the live traveler app but remains visible in admin tools.', '這部分內容不會出現在正式遊客端，但會保留在管理後台中。', '归档完成', 'Archive complete', '歸檔完成', '守夜徽记', 'Night watch emblem', '守夜徽記', '旧校舍', 'Old campus block', '舊校舍', 'sequence', NULL, 'draft', 3, NULL, 0, '')
ON DUPLICATE KEY UPDATE
  `storyline_id` = VALUES(`storyline_id`),
  `chapter_order` = VALUES(`chapter_order`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `detail_zh` = VALUES(`detail_zh`),
  `detail_en` = VALUES(`detail_en`),
  `detail_zht` = VALUES(`detail_zht`),
  `achievement_zh` = VALUES(`achievement_zh`),
  `achievement_en` = VALUES(`achievement_en`),
  `achievement_zht` = VALUES(`achievement_zht`),
  `collectible_zh` = VALUES(`collectible_zh`),
  `collectible_en` = VALUES(`collectible_en`),
  `collectible_zht` = VALUES(`collectible_zht`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_en` = VALUES(`location_name_en`),
  `location_name_zht` = VALUES(`location_name_zht`),
  `unlock_type` = VALUES(`unlock_type`),
  `unlock_param_json` = VALUES(`unlock_param_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `pois` (
  `id`, `city_id`, `storyline_id`, `code`,
  `name_zh`, `name_en`, `name_zht`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`,
  `address_zh`, `address_en`, `address_zht`,
  `city_code`,
  `latitude`, `longitude`,
  `trigger_radius`, `manual_checkin_radius`, `stay_seconds`,
  `category_code`, `check_in_method`,
  `district_zh`, `district_en`, `district_zht`,
  `description_zh`, `description_en`, `description_zht`,
  `intro_title_zh`, `intro_title_en`, `intro_title_zht`,
  `intro_summary_zh`, `intro_summary_en`, `intro_summary_zht`,
  `difficulty`, `tags`,
  `status`, `sort_order`, `published_at`, `deleted`, `_openid`
)
VALUES
  (1, 1, 2, 'ruins_st_paul', '大三巴牌坊', 'Ruins of St Paul', '大三巴牌坊', '东西相遇 · 序章入口', 'East Meets West · Opening stop', '東西相遇 · 序章入口', '花王堂下街', 'Rua de Sao Paulo', '花王堂下街', 'macau', 22.19754600, 113.54085400, 50, 50, 30, 'landmark', 'gps', '澳门半岛', 'Macau Peninsula', '澳門半島', '澳门最具代表性的世界遗产之一，也是许多旅人进入老城故事的第一站。', 'One of the most iconic heritage landmarks in Macau and a natural opening for the old city route.', '澳門最具代表性的世界遺產之一，也是許多旅人進入老城故事的第一站。', '世界遗产的入口', 'Gateway to the heritage city', '世界遺產的入口', '站在大三巴前，城市的宗教、战事与游客节奏会同时涌到眼前。', 'At the Ruins, faith, conflict, and present-day traveler rhythm all meet in one frame.', '站在大三巴前，城市的宗教、戰事與遊客節奏會同時湧到眼前。', 'easy', JSON_ARRAY('heritage', 'landmark', 'story-route'), 'published', 1, NOW(), 0, ''),
  (2, 1, 2, 'st_dominic_church', '玫瑰堂', 'St Dominic Church', '玫瑰堂', '东西相遇 · 教堂节点', 'East Meets West · Church stop', '東西相遇 · 教堂節點', '板樟堂前地 2 号', 'Largo de Sao Domingos 2', '板樟堂前地 2 號', 'macau', 22.19280100, 113.54093600, 50, 55, 35, 'story_point', 'gps', '澳门半岛', 'Macau Peninsula', '澳門半島', '明快的立面、钟声与广场动线，让老城从遗迹叙事转入更柔和的日常节奏。', 'A bright church facade and bell-driven square rhythm soften the old city route.', '明快的立面、鐘聲與廣場動線，讓老城從遺跡敘事轉入更柔和的日常節奏。', '钟声与街区节奏', 'Bells and city rhythm', '鐘聲與街區節奏', '从大三巴继续向前，玫瑰堂会把这条故事线从冲突转向共处。', 'Moving forward from the Ruins, the church shifts the route from conflict toward coexistence.', '從大三巴繼續向前，玫瑰堂會把這條故事線從衝突轉向共處。', 'easy', JSON_ARRAY('church', 'square', 'story-route'), 'published', 2, NOW(), 0, ''),
  (3, 1, 1, 'ama_temple', '妈阁庙', 'A-Ma Temple', '媽閣廟', '海上丝路 · 起点', 'Maritime Silk Road · Opening stop', '海上絲路 · 起點', '妈阁庙前地', 'Largo do Pagode da Barra', '媽閣廟前地', 'macau', 22.18684000, 113.53159000, 80, 80, 60, 'story_point', 'gps', '澳门半岛', 'Macau Peninsula', '澳門半島', '澳门地名起源的重要地点，也是理解海上丝路故事的自然入口。', 'A-Ma Temple anchors the name and maritime origin story of Macau.', '澳門地名起源的重要地點，也是理解海上絲路故事的自然入口。', '澳门名字的起点', 'Where the name of Macau begins', '澳門名字的起點', '如果想理解澳门最早的海上叙事，从妈阁庙出发是最自然的方式。', 'If you want the earliest maritime story of Macau, A-Ma Temple is the natural place to begin.', '如果想理解澳門最早的海上敘事，從媽閣廟出發是最自然的方式。', 'medium', JSON_ARRAY('faith', 'maritime', 'elder-friendly'), 'published', 3, NOW(), 0, ''),
  (4, 1, 2, 'dom_pedro_v_theatre', '岗顶剧院', 'Dom Pedro V Theatre', '崗頂劇院', '东西相遇 · 静谧舞台', 'East Meets West · Theatre stop', '東西相遇 · 靜謐舞台', '岗顶前地', 'Largo de Santo Agostinho', '崗頂前地', 'macau', 22.18854200, 113.54162800, 50, 55, 40, 'theater', 'gps', '澳门半岛', 'Macau Peninsula', '澳門半島', '岗顶剧院把老城中的安静、演出与文化空间连接起来。', 'Dom Pedro V Theatre offers a quieter cultural stop inside the old city route.', '崗頂劇院把老城中的安靜、演出與文化空間連接起來。', '一座适合停下来听故事的老剧院', 'A theatre made for quieter story beats', '一座適合停下來聽故事的老劇院', '它不像遗迹那样强烈，却很适合承接城市文化层面的叙事。', 'Less dramatic than the ruins, the theatre is ideal for slower cultural storytelling.', '它不像遺跡那樣強烈，卻很適合承接城市文化層面的敘事。', 'easy', JSON_ARRAY('theatre', 'culture', 'quiet-stop'), 'published', 4, NOW(), 0, ''),
  (5, 1, 2, 'hotel_lisboa', '葡京酒店', 'Hotel Lisboa', '葡京酒店', '东西相遇 · 城市霓虹', 'East Meets West · Neon city stop', '東西相遇 · 城市霓虹', '友谊大马路 2-4 号', 'Avenida da Amizade 2-4', '友誼大馬路 2-4 號', 'macau', 22.19055000, 113.54407300, 50, 60, 45, 'museum', 'gps', '澳门半岛', 'Macau Peninsula', '澳門半島', '从历史遗迹转向现代城市表情，葡京像是另一种澳门记忆的切片。', 'Lisboa represents the neon-era urban layer of Macau beyond the heritage core.', '從歷史遺跡轉向現代城市表情，葡京像是另一種澳門記憶的切片。', '霓虹时代的城市切面', 'A slice of the neon-era city', '霓虹時代的城市切面', '它把老城外的另一种澳门想象也纳入旅程里。', 'It folds the neon-era imagination of Macau into the route.', '它把老城外的另一種澳門想像也納入旅程裡。', 'medium', JSON_ARRAY('neon', 'cityscape', 'modern-layer'), 'published', 5, NOW(), 0, ''),
  (6, 2, 3, 'taipa_houses', '龙环葡韵', 'Taipa Houses', '龍環葡韻', '氹仔慢行 · 色彩街屋', 'Taipa Leisure Walk · Pastel houses', '氹仔慢行 · 色彩街屋', '氹仔海边马路', 'Avenida da Praia, Taipa', '氹仔海邊馬路', 'taipa', 22.15630000, 113.56060000, 60, 60, 45, 'story_point', 'gps', '氹仔', 'Taipa', '氹仔', '彩色街屋与海边步道很适合把旅程切换到更轻松的慢游节奏。', 'Pastel facades and the nearby waterfront make this an ideal slow-walk stop.', '彩色街屋與海邊步道很適合把旅程切換到更輕鬆的慢遊節奏。', '最适合慢下来的颜色', 'The color palette that slows the route down', '最適合慢下來的顏色', '在这里，旅程从任务感转成更松弛的城市观察。', 'Here the route shifts from task-driven movement into relaxed observation.', '在這裡，旅程從任務感轉成更鬆弛的城市觀察。', 'easy', JSON_ARRAY('taipa', 'photo-walk', 'slow-travel'), 'published', 6, NOW(), 0, ''),
  (7, 3, 4, 'hac_sa_beach', '黑沙海滩', 'Hac Sa Beach', '黑沙海灘', '海岸回响 · 黑沙序章', 'Coastal Echoes · Black sand opening', '海岸回響 · 黑沙序章', '路环黑沙海滩', 'Hac Sa Beach, Coloane', '路環黑沙海灘', 'coloane', 22.11970000, 113.56950000, 90, 90, 60, 'landmark', 'gps', '路环', 'Coloane', '路環', '海风、黑沙与开阔的视线，会把整趟旅程自然地放慢下来。', 'Sea breeze, black sand, and open sightlines slow the entire journey down.', '海風、黑沙與開闊的視線，會把整趟旅程自然地放慢下來。', '海边会重新定义步速', 'The coast resets your walking pace', '海邊會重新定義步速', '黑沙海滩更像是一口深呼吸，是路线最后阶段最重要的节奏转换点。', 'Hac Sa works like a deep breath and resets the rhythm of the final route act.', '黑沙海灘更像是一口深呼吸，是路線最後階段最重要的節奏轉換點。', 'medium', JSON_ARRAY('coast', 'slow-walk', 'nature'), 'published', 7, NOW(), 0, ''),
  (8, 4, 5, 'history_building', '文史楼', 'History Building', '文史樓', '校园怪谈 · 归档节点', 'Campus Ghost Stories · Archived stop', '校園怪談 · 歸檔節點', '校园文史楼', 'Campus History Building', '校園文史樓', 'ecnu', 31.22812000, 121.40627000, 50, 55, 45, 'story_point', 'gps', '校园区', 'Campus district', '校園區', '保留在数据库中的历史 mock POI，用于后台归档核对。', 'Archived legacy mock POI kept for admin-side inspection only.', '保留在資料庫中的歷史 mock POI，用於後台歸檔核對。', '历史 mock 节点', 'Archived legacy stop', '歷史 mock 節點', '这不是正式澳门发布内容，但会继续保存在后台中。', 'This is not part of the live Macau release, but it remains in the admin archive.', '這不是正式澳門發布內容，但會繼續保存在後台中。', 'hard', JSON_ARRAY('legacy', 'archived', 'mock'), 'draft', 99, NULL, 0, '')
ON DUPLICATE KEY UPDATE
  `city_id` = VALUES(`city_id`),
  `storyline_id` = VALUES(`storyline_id`),
  `code` = VALUES(`code`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_en` = VALUES(`subtitle_en`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `address_zh` = VALUES(`address_zh`),
  `address_en` = VALUES(`address_en`),
  `address_zht` = VALUES(`address_zht`),
  `city_code` = VALUES(`city_code`),
  `latitude` = VALUES(`latitude`),
  `longitude` = VALUES(`longitude`),
  `trigger_radius` = VALUES(`trigger_radius`),
  `manual_checkin_radius` = VALUES(`manual_checkin_radius`),
  `stay_seconds` = VALUES(`stay_seconds`),
  `category_code` = VALUES(`category_code`),
  `check_in_method` = VALUES(`check_in_method`),
  `district_zh` = VALUES(`district_zh`),
  `district_en` = VALUES(`district_en`),
  `district_zht` = VALUES(`district_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `intro_title_zh` = VALUES(`intro_title_zh`),
  `intro_title_en` = VALUES(`intro_title_en`),
  `intro_title_zht` = VALUES(`intro_title_zht`),
  `intro_summary_zh` = VALUES(`intro_summary_zh`),
  `intro_summary_en` = VALUES(`intro_summary_en`),
  `intro_summary_zht` = VALUES(`intro_summary_zht`),
  `difficulty` = VALUES(`difficulty`),
  `tags` = VALUES(`tags`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `stamps` (
  `id`, `code`,
  `name_zh`, `name_en`, `name_zht`,
  `description_zh`, `description_en`, `description_zht`,
  `stamp_type`, `rarity`,
  `related_poi_id`, `related_storyline_id`,
  `status`, `sort_order`, `published_at`, `deleted`
)
VALUES
  (101, 'stamp_ruins_footprint', '大三巴足迹章', 'Ruins Footprint Stamp', '大三巴足跡章', '到达大三巴牌坊即可获得。', 'Granted when you arrive at the Ruins of St Paul.', '到達大三巴牌坊即可獲得。', 'location', 'common', 1, NULL, 'published', 1, NOW(), 0),
  (102, 'stamp_dominic_echo', '玫瑰堂回响章', 'St Dominic Echo Stamp', '玫瑰堂回響章', '完成玫瑰堂节点后获得。', 'Granted after completing the St Dominic Church stop.', '完成玫瑰堂節點後獲得。', 'location', 'common', 2, NULL, 'published', 2, NOW(), 0),
  (103, 'stamp_ama_origin', '妈阁起源章', 'A-Ma Origin Stamp', '媽閣起源章', '完成海上丝路起点后获得。', 'Granted after completing the maritime origin stop at A-Ma Temple.', '完成海上絲路起點後獲得。', 'story', 'rare', NULL, 1, 'published', 3, NOW(), 0),
  (104, 'stamp_first_explorer', '初探澳门', 'First Macau Explorer', '初探澳門', '收集前三枚旅程印记后获得。', 'Granted after collecting the first set of route marks.', '收集前三枚旅程印記後獲得。', 'mission', 'common', NULL, NULL, 'published', 4, NOW(), 0),
  (105, 'stamp_night_church_echo', '教堂夜响', 'Night Church Echo', '教堂夜響', '在夜间触发教堂故事节点后解锁。', 'Unlocked after triggering the church storyline in night mode.', '在夜間觸發教堂故事節點後解鎖。', 'secret', 'epic', NULL, 2, 'published', 5, NOW(), 0),
  (106, 'stamp_theatre_pass', '岗顶剧院通行章', 'Theatre Passage Stamp', '崗頂劇院通行章', '完成岗顶剧院节点后获得。', 'Granted after completing the Dom Pedro V Theatre stop.', '完成崗頂劇院節點後獲得。', 'location', 'rare', 4, NULL, 'published', 6, NOW(), 0),
  (107, 'stamp_lisboa_neon', '葡京霓虹章', 'Lisboa Neon Stamp', '葡京霓虹章', '完成葡京城市切面节点后获得。', 'Granted after the Hotel Lisboa cityscape stop.', '完成葡京城市切面節點後獲得。', 'location', 'rare', 5, NULL, 'published', 7, NOW(), 0),
  (108, 'stamp_taipa_leisure', '氹仔慢行章', 'Taipa Leisure Stamp', '氹仔慢行章', '完成龙环葡韵慢游节点后获得。', 'Granted after completing the Taipa Houses slow-walk stop.', '完成龍環葡韻慢遊節點後獲得。', 'location', 'rare', 6, NULL, 'published', 8, NOW(), 0),
  (109, 'stamp_coastal_echo', '海岸回响章', 'Coastal Echo Stamp', '海岸回響章', '完成黑沙海滩海岸节点后获得。', 'Granted after completing the Hac Sa coastal stop.', '完成黑沙海灘海岸節點後獲得。', 'location', 'rare', 7, NULL, 'published', 9, NOW(), 0),
  (202, 'stamp_campus_ghost', '校园怪谈章', 'Campus Ghost Stamp', '校園怪談章', '历史 mock 数据中的归档印记。', 'Archived stamp from the legacy campus mock dataset.', '歷史 mock 數據中的歸檔印記。', 'secret', 'epic', 8, 5, 'draft', 99, NULL, 0)
ON DUPLICATE KEY UPDATE
  `code` = VALUES(`code`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `stamp_type` = VALUES(`stamp_type`),
  `rarity` = VALUES(`rarity`),
  `related_poi_id` = VALUES(`related_poi_id`),
  `related_storyline_id` = VALUES(`related_storyline_id`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `rewards` (
  `id`, `code`,
  `name_zh`, `name_en`, `name_zht`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`,
  `description_zh`, `description_en`, `description_zht`,
  `highlight_zh`, `highlight_en`, `highlight_zht`,
  `stamp_cost`, `inventory_total`, `inventory_redeemed`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`, `deleted`, `_openid`
)
VALUES
  (1, 'old_city_postcards', '澳门老城明信片', 'Macau Old Town Postcards', '澳門老城明信片', '旅途周边', 'Trip souvenir', '旅途周邊', '收集足够印章后即可兑换一套澳门老城主题明信片。', 'Redeem a postcard set themed around the old city after collecting enough stamps.', '收集足夠印章後即可兌換一套澳門老城主題明信片。', '适合把这段旅程带回家', 'A keepsake that lets the trip travel home with you', '適合把這段旅程帶回家', 4, 128, 0, 'published', 1, NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 0, ''),
  (2, 'silk_road_badge', '海上丝路限定徽章', 'Maritime Silk Road Badge', '海上絲路限定徽章', '收藏徽章', 'Collectible badge', '收藏徽章', '完成故事线与足迹收集后可兑换限定海上丝路徽章。', 'Redeem a limited Maritime Silk Road badge after completing the route collection.', '完成故事線與足跡收集後可兌換限定海上絲路徽章。', '适合收藏与路线纪念', 'Ideal for collectors and route completion memories', '適合收藏與路線紀念', 6, 999, 0, 'published', 2, NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 0, ''),
  (3, 'egg_tart_partner_offer', '葡式蛋挞合作礼遇', 'Portuguese Egg Tart Partner Offer', '葡式蛋撻合作禮遇', '合作商户', 'Partner reward', '合作商戶', '完成更多城市足迹后可解锁的合作门店礼遇，当前保留展示位。', 'A partner-store offer reserved for future unlock after more route progress.', '完成更多城市足跡後可解鎖的合作門店禮遇，當前保留展示位。', '下一站很适合配一枚蛋挞', 'Best paired with the next slow walk and an egg tart', '下一站很適合配一枚蛋撻', 8, 26, 26, 'published', 3, NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 0, '')
ON DUPLICATE KEY UPDATE
  `code` = VALUES(`code`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_en` = VALUES(`subtitle_en`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `highlight_zh` = VALUES(`highlight_zh`),
  `highlight_en` = VALUES(`highlight_en`),
  `highlight_zht` = VALUES(`highlight_zht`),
  `stamp_cost` = VALUES(`stamp_cost`),
  `inventory_total` = VALUES(`inventory_total`),
  `inventory_redeemed` = VALUES(`inventory_redeemed`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `publish_start_at` = VALUES(`publish_start_at`),
  `publish_end_at` = VALUES(`publish_end_at`),
  `deleted` = VALUES(`deleted`),
  `_openid` = VALUES(`_openid`);

INSERT INTO `tip_articles` (
  `id`, `city_id`, `code`, `category_code`,
  `title_zh`, `title_en`, `title_zht`,
  `summary_zh`, `summary_en`, `summary_zht`,
  `content_zh`, `content_en`, `content_zht`,
  `author_display_name`,
  `location_name_zh`, `location_name_en`, `location_name_zht`,
  `tags_json`, `source_type`,
  `status`, `sort_order`, `published_at`, `deleted`
)
VALUES
  (301, 1, 'tip_first_trip', 'newbie', '第一次来澳门怎么走最顺？', 'How to enjoy Macau on your first trip?', '第一次來澳門怎麼走最順？', '用两条故事线串起老城核心区域，不必反复折返。', 'Use two storylines to connect the historic core without doubling back.', '用兩條故事線串起老城核心區域，不必反覆折返。', '先从大三巴一带进入老城，借由遗迹与广场迅速建立城市节奏。\n之后再往玫瑰堂和岗顶方向延伸，路线会更完整也更顺。', 'Start near the Ruins to establish the old city rhythm quickly.\nThen continue toward St Dominic Church and the theatre district for a fuller route.', '先從大三巴一帶進入老城，藉由遺跡與廣場迅速建立城市節奏。\n之後再往玫瑰堂和崗頂方向延伸，路線會更完整也更順。', '澳门小游编辑部', '澳门半岛', 'Macau Peninsula', '澳門半島', JSON_ARRAY('route', 'newbie', 'checkin'), 'editorial', 'published', 1, NOW(), 0),
  (302, 1, 'tip_senior_walk', 'slow-travel', '适合长者的半日慢游路线', 'A senior friendly half day route', '適合長者的半日慢遊路線', '把步行距离控制在舒服范围内，依旧能感受到澳门的核心故事。', 'Keep the walk comfortable while still touching the core Macau story beats.', '把步行距離控制在舒服範圍內，依舊能感受到澳門的核心故事。', '建议把妈阁、议事亭与可以坐下休息的点位串联起来。\n打开语音导览与大字显示，整体体验会更从容。', 'Connect A-Ma, Senado Square, and stops with seating to keep the route gentle.\nVoice guidance and larger text make the experience much easier to follow.', '建議把媽閣、議事亭與可以坐下休息的點位串聯起來。\n打開語音導覽與大字顯示，整體體驗會更從容。', '城市导览员', '妈阁庙', 'A-Ma Temple', '媽閣廟', JSON_ARRAY('senior-mode', 'slow-travel', 'voice'), 'editorial', 'published', 2, NOW(), 0),
  (303, 1, 'tip_photo_spots', 'photo', '澳门最出片的 5 个角度', 'Five photo spots that shine in Macau', '澳門最出片的 5 個角度', '从大三巴正面到教堂侧街，这篇攻略更关心光线与构图。', 'From the Ruins frontage to church side streets, this guide focuses on light and framing.', '從大三巴正面到教堂側街，這篇攻略更關心光線與構圖。', '清晨适合拍立面，傍晚更适合层次丰富的街景。\n多往侧街走几步，往往更容易找到安静的取景角度。', 'Morning works best for facades, while dusk is better for layered street scenes.\nStep into side streets to find calmer and more original compositions.', '清晨適合拍立面，傍晚更適合層次豐富的街景。\n多往側街走幾步，往往更容易找到安靜的取景角度。', '旅拍观察员', '大三巴牌坊', 'Ruins of St Paul', '大三巴牌坊', JSON_ARRAY('photo', 'guide', 'popular'), 'editorial', 'published', 3, NOW(), 0)
ON DUPLICATE KEY UPDATE
  `city_id` = VALUES(`city_id`),
  `code` = VALUES(`code`),
  `category_code` = VALUES(`category_code`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `content_zh` = VALUES(`content_zh`),
  `content_en` = VALUES(`content_en`),
  `content_zht` = VALUES(`content_zht`),
  `author_display_name` = VALUES(`author_display_name`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_en` = VALUES(`location_name_en`),
  `location_name_zht` = VALUES(`location_name_zht`),
  `tags_json` = VALUES(`tags_json`),
  `source_type` = VALUES(`source_type`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `notifications` (
  `id`, `code`,
  `title_zh`, `title_en`, `title_zht`,
  `content_zh`, `content_en`, `content_zht`,
  `notification_type`, `target_scope`, `action_url`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`, `deleted`
)
VALUES
  (1, 'notif_night_walk', '今晚的夜游提醒已送达', 'Your night walk reminder is here', '今晚的夜遊提醒已送達', '今晚的福隆新街夜游仍有名额，记得提早一点出发。', 'A Fuk Long night walk slot is still open this evening. Leave a little early to stay on rhythm.', '今晚的福隆新街夜遊仍有名額，記得提早一點出發。', 'activity', 'all', '/pages/discover/index', 'published', 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0),
  (2, 'notif_tip_saved', '有人收藏了你的路线灵感', 'Someone saved your route notes', '有人收藏了你的路線靈感', '你整理的旅拍灵感被更多旅人看到了。', 'More travelers found and saved the route notes you organized.', '你整理的旅拍靈感被更多旅人看到了。', 'ugc', 'all', '/pages/tips/index', 'published', 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0),
  (3, 'notif_city_unlock', '新的城市探索现已开放', 'New city exploration is now available', '新的城市探索現已開放', '澳門、橫琴、香港與華東師範大學已整理為可切換的大地圖，進入地圖頁即可切換瀏覽。', 'Macau, Hengqin, Hong Kong, and ECNU are now available as top-level map regions in the explorer.', '澳門、橫琴、香港與華東師範大學已整理為可切換的大地圖，進入地圖頁即可切換瀏覽。', 'system', 'all', '/pages/map/index', 'published', 3, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 0)
ON DUPLICATE KEY UPDATE
  `code` = VALUES(`code`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `content_zh` = VALUES(`content_zh`),
  `content_en` = VALUES(`content_en`),
  `content_zht` = VALUES(`content_zht`),
  `notification_type` = VALUES(`notification_type`),
  `target_scope` = VALUES(`target_scope`),
  `action_url` = VALUES(`action_url`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `publish_start_at` = VALUES(`publish_start_at`),
  `publish_end_at` = VALUES(`publish_end_at`),
  `deleted` = VALUES(`deleted`);

INSERT INTO `app_runtime_settings` (
  `setting_group`, `setting_key`, `locale_code`,
  `title_zh`, `title_en`, `title_zht`,
  `value_json`, `value_text`,
  `description_zh`, `description_en`, `description_zht`,
  `status`, `sort_order`, `published_at`
)
VALUES
  (
    'home', 'hero_cards', '',
    '首页城市卡片', 'Home hero cards', '首頁城市卡片',
    JSON_ARRAY(
      JSON_OBJECT('cityCode', 'macau', 'priority', 1, 'accentColor', '#ffd9e5'),
      JSON_OBJECT('cityCode', 'hengqin', 'priority', 2, 'accentColor', '#dff7ef'),
      JSON_OBJECT('cityCode', 'hong-kong', 'priority', 3, 'accentColor', '#dfeaff'),
      JSON_OBJECT('cityCode', 'ecnu', 'priority', 4, 'accentColor', '#fff0c8')
    ),
    NULL,
    '控制首页城市主卡片排序与视觉强调。',
    'Controls the order and visual emphasis of the home city hero cards.',
    '控制首頁城市主卡片排序與視覺強調。',
    'published', 10, NOW()
  ),
  (
    'discover', 'featured_cards', '',
    '发现页卡片排序', 'Discover featured card order', '發現頁卡片排序',
    JSON_ARRAY(
      JSON_OBJECT('cardType', 'activity', 'priority', 1),
      JSON_OBJECT('cardType', 'merchant', 'priority', 2),
      JSON_OBJECT('cardType', 'checkin', 'priority', 3)
    ),
    NULL,
    '控制发现页卡片类型顺序。',
    'Controls discover card type ordering.',
    '控制發現頁卡片類型順序。',
    'published', 10, NOW()
  ),
  (
    'discover', 'curated_cards', 'en',
    '发现页运营卡片', 'Discover curated cards', '發現頁運營卡片',
    JSON_ARRAY(
      JSON_OBJECT('id', 'discover-activity-night-walk', 'title', 'Fuk Long Street Night Walk', 'subtitle', 'Activity spotlight', 'description', 'An evening route that links old-town lights, stories, and a slower city rhythm.', 'tag', 'Tonight', 'icon', '🌖', 'type', 'activity', 'district', 'Macau Peninsula', 'actionText', 'View activity', 'coverColor', '#ffe2ef', 'actionUrl', '/pages/discover/index', 'sourceType', 'runtime'),
      JSON_OBJECT('id', 'discover-merchant-egg-tart', 'title', 'Portuguese Egg Tart Partner Offer', 'subtitle', 'Merchant reward', 'description', 'Finish a few key route stops and redeem a small treat from a partner store.', 'tag', 'Badge reward', 'icon', '🧁', 'type', 'merchant', 'district', 'Senado area', 'actionText', 'Go redeem', 'coverColor', '#fff0c8', 'actionUrl', '/pages/rewards/index', 'sourceType', 'runtime'),
      JSON_OBJECT('id', 'discover-checkin-hot-route', 'title', 'This Week Top Check-in Route', 'subtitle', 'Popular route', 'description', 'Ruins, A-Ma Temple, and the old square remain the most popular route anchors this week.', 'tag', 'Trending', 'icon', '📍', 'type', 'checkin', 'district', 'Historic Centre', 'actionText', 'Follow route', 'coverColor', '#dff3ff', 'actionUrl', '/pages/map/index', 'sourceType', 'runtime')
    ),
    NULL,
    '运营可直接配置发现页卡片文案与跳转，不依赖代码拼装。',
    'Allows operations to fully configure discover cards without code-side synthesis.',
    '運營可直接配置發現頁卡片文案與跳轉，不依賴代碼拼裝。',
    'published', 20, NOW()
  ),
  (
    'map', 'checkin_rules', '',
    '地图打卡规则', 'Map check-in rules', '地圖打卡規則',
    JSON_OBJECT('cooldownSeconds', 1800, 'debounceMillis', 2000, 'manualRadius', 200, 'gpsIntervalSeconds', 2, 'radiusPolicy', 'dynamic'),
    NULL,
    '控制地图端打卡冷却、手动半径与定位节奏。',
    'Controls cooldown, manual radius, and location cadence for map check-ins.',
    '控制地圖端打卡冷卻、手動半徑與定位節奏。',
    'published', 10, NOW()
  ),
  (
    'profile', 'badge_panel', '',
    '个人页徽章面板', 'Profile badge panel', '個人頁徽章面板',
    JSON_OBJECT('showRecentBadges', true, 'maxItems', 6, 'showCityRewards', true),
    NULL,
    '控制个人页最近徽章与城市奖励展示。',
    'Controls recent badge and city-reward presentation in the profile view.',
    '控制個人頁最近徽章與城市獎勵展示。',
    'published', 10, NOW()
  ),
  (
    'settings', 'accessibility_defaults', '',
    '无障碍默认设置', 'Accessibility defaults', '無障礙默認設置',
    JSON_OBJECT('interfaceMode', 'standard', 'fontScale', 1.0, 'voiceGuideEnabled', true, 'highContrast', false),
    NULL,
    '控制首次进入时的无障碍默认选项。',
    'Controls default accessibility preferences for first-time travelers.',
    '控制首次進入時的無障礙默認選項。',
    'published', 10, NOW()
  ),
  (
    'tips', 'feed_defaults', '',
    '攻略列表默认配置', 'Tips feed defaults', '攻略列表默認配置',
    JSON_OBJECT('pageSize', 10, 'allowUserPublish', true),
    NULL,
    '控制攻略流默认分页与旅人投稿开关。',
    'Controls feed defaults and whether traveler posts are allowed.',
    '控制攻略流默認分頁與旅人投稿開關。',
    'published', 10, NOW()
  ),
  (
    'travel', 'recommendation_profiles', 'en',
    '行程推荐画像', 'Travel recommendation profiles', '行程推薦畫像',
    JSON_ARRAY(
      JSON_OBJECT('profileId', 'history_first_trip', 'playDuration', 'Half day', 'interests', JSON_ARRAY('History', 'Culture'), 'storyId', 2, 'storyCode', 'east_meets_west', 'activityTitle', 'Old Town Story Walk', 'poiId', 1, 'poiCode', 'ruins_st_paul', 'tipId', 301, 'tipCode', 'tip_first_trip', 'reason', 'A compact first-trip route that connects the strongest old-town landmarks without backtracking.'),
      JSON_OBJECT('profileId', 'senior_slow_walk', 'playDuration', 'Half day', 'interests', JSON_ARRAY('Slow Travel', 'Voice Guide'), 'storyId', 1, 'storyCode', 'maritime_silk_road', 'activityTitle', 'A-Ma Slow Walk', 'poiId', 3, 'poiCode', 'ama_temple', 'tipId', 302, 'tipCode', 'tip_senior_walk', 'reason', 'Best for a gentle pace, shorter walking spans, and voice-guided exploration.'),
      JSON_OBJECT('profileId', 'photo_taipa', 'playDuration', 'Half day', 'interests', JSON_ARRAY('Photo', 'Dessert'), 'storyId', 3, 'storyCode', 'taipa_leisure_walk', 'activityTitle', 'Taipa Photo and Dessert Walk', 'poiId', 6, 'poiCode', 'taipa_houses', 'tipId', 303, 'tipCode', 'tip_photo_spots', 'reason', 'Pastel facades, slower lanes, and dessert stops make Taipa the best fit for photo-minded travelers.')
    ),
    NULL,
    '用于旅人测评后的路线推荐，避免推荐逻辑只存在于客户端 mock 中。',
    'Provides admin-managed recommendation profiles so travel suggestions do not live only in client mock code.',
    '用於旅人測評後的路線推薦，避免推薦邏輯只存在於客戶端 mock 中。',
    'published', 10, NOW()
  )
ON DUPLICATE KEY UPDATE
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `value_json` = VALUES(`value_json`),
  `value_text` = VALUES(`value_text`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`);

-- Phase 9 canonical spatial hierarchy correction
UPDATE `cities`
SET
  `name_zh` = '澳门',
  `name_en` = 'Macau',
  `name_zht` = '澳門',
  `country_code` = 'MO',
  `source_coordinate_system` = 'GCJ02',
  `source_center_lat` = COALESCE(`source_center_lat`, `center_lat`, 22.1987000),
  `source_center_lng` = COALESCE(`source_center_lng`, `center_lng`, 113.5439000),
  `center_lat` = COALESCE(`center_lat`, 22.1987000),
  `center_lng` = COALESCE(`center_lng`, 113.5439000),
  `status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW())
WHERE `code` = 'macau';

INSERT INTO `cities` (
  `code`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `subtitle_zh`,
  `subtitle_en`,
  `subtitle_zht`,
  `country_code`,
  `source_coordinate_system`,
  `source_center_lat`,
  `source_center_lng`,
  `center_lat`,
  `center_lng`,
  `default_zoom`,
  `unlock_type`,
  `unlock_condition_json`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `sort_order`,
  `status`,
  `published_at`,
  `deleted`
)
VALUES
  (
    'hengqin',
    '横琴',
    'Hengqin',
    '橫琴',
    '澳门周边扩展探索区域',
    'Cross-border expansion area beside Macau',
    '澳門周邊延伸探索區域',
    'CN',
    'GCJ02',
    22.1150000,
    113.5500000,
    22.1150000,
    113.5500000,
    13,
    'auto',
    JSON_OBJECT('mode', 'default'),
    '用于承接横琴片区的后续地图、POI 与故事线配置。',
    'Reserved for Hengqin map, POI, and story authoring.',
    '用於承接橫琴片區的後續地圖、POI 與故事線配置。',
    2,
    'published',
    NOW(),
    0
  ),
  (
    'hong-kong',
    '香港',
    'Hong Kong',
    '香港',
    '可切换的大区域地图入口',
    'Top-level regional map entry',
    '可切換的大區域地圖入口',
    'HK',
    'GCJ02',
    22.3193000,
    114.1694000,
    22.3193000,
    114.1694000,
    12,
    'auto',
    JSON_OBJECT('mode', 'default'),
    '用于承接香港主地图与后续空间内容配置。',
    'Reserved for Hong Kong spatial authoring.',
    '用於承接香港主地圖與後續空間內容配置。',
    3,
    'published',
    NOW(),
    0
  )
ON DUPLICATE KEY UPDATE
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_en` = VALUES(`subtitle_en`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `country_code` = VALUES(`country_code`),
  `source_coordinate_system` = VALUES(`source_coordinate_system`),
  `source_center_lat` = VALUES(`source_center_lat`),
  `source_center_lng` = VALUES(`source_center_lng`),
  `center_lat` = VALUES(`center_lat`),
  `center_lng` = VALUES(`center_lng`),
  `default_zoom` = VALUES(`default_zoom`),
  `unlock_type` = VALUES(`unlock_type`),
  `unlock_condition_json` = VALUES(`unlock_condition_json`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`);

UPDATE `cities`
SET
  `name_zh` = '华东师范大学',
  `name_en` = 'East China Normal University',
  `name_zht` = '華東師範大學',
  `country_code` = 'CN',
  `source_coordinate_system` = 'GCJ02',
  `source_center_lat` = COALESCE(`source_center_lat`, `center_lat`, 31.2281200),
  `source_center_lng` = COALESCE(`source_center_lng`, `center_lng`, 121.4062700),
  `center_lat` = COALESCE(`center_lat`, 31.2281200),
  `center_lng` = COALESCE(`center_lng`, 121.4062700),
  `unlock_type` = 'auto',
  `status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW())
WHERE `code` = 'ecnu';

UPDATE `cities`
SET
  `status` = 'archived',
  `published_at` = NULL
WHERE `code` IN ('taipa', 'coloane');

INSERT INTO `sub_maps` (
  `id`,
  `city_id`,
  `code`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `subtitle_zh`,
  `subtitle_en`,
  `subtitle_zht`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `cover_asset_id`,
  `source_coordinate_system`,
  `source_center_lat`,
  `source_center_lng`,
  `center_lat`,
  `center_lng`,
  `bounds_json`,
  `popup_config_json`,
  `display_config_json`,
  `sort_order`,
  `status`,
  `published_at`,
  `deleted`
)
SELECT
  seeded.`id`,
  macau.`id`,
  seeded.`code`,
  seeded.`name_zh`,
  seeded.`name_en`,
  seeded.`name_zht`,
  seeded.`subtitle_zh`,
  seeded.`subtitle_en`,
  seeded.`subtitle_zht`,
  seeded.`description_zh`,
  seeded.`description_en`,
  seeded.`description_zht`,
  NULL,
  'GCJ02',
  seeded.`center_lat`,
  seeded.`center_lng`,
  seeded.`center_lat`,
  seeded.`center_lng`,
  NULL,
  JSON_OBJECT('enabled', TRUE, 'mode', 'sheet'),
  JSON_OBJECT('showIntroPopup', TRUE, 'displayMode', 'card'),
  seeded.`sort_order`,
  'published',
  NOW(),
  0
FROM (
  SELECT
    1001 AS `id`,
    'macau-peninsula' AS `code`,
    '澳门半岛' AS `name_zh`,
    'Macau Peninsula' AS `name_en`,
    '澳門半島' AS `name_zht`,
    '澳门主城区历史步行圈' AS `subtitle_zh`,
    'Historic core walking district' AS `subtitle_en`,
    '澳門主城區歷史步行圈' AS `subtitle_zht`,
    '澳门主城区与历史城区的核心浏览区域。' AS `description_zh`,
    'Historic core district of Macau.' AS `description_en`,
    '澳門主城區與歷史城區的核心瀏覽區域。' AS `description_zht`,
    22.1987000 AS `center_lat`,
    113.5439000 AS `center_lng`,
    1 AS `sort_order`
  UNION ALL
  SELECT
    1002,
    'taipa',
    '氹仔岛',
    'Taipa',
    '氹仔島',
    '氹仔旧城区与休闲探索区域',
    'Taipa leisure exploration district',
    '氹仔舊城區與休閒探索區域',
    '用于承接氹仔相关地图、POI 与故事线。' ,
    'Sub-map for Taipa routes and POIs.',
    '用於承接氹仔相關地圖、POI 與故事線。',
    22.1563000,
    113.5606000,
    2
  UNION ALL
  SELECT
    1003,
    'coloane',
    '路环岛',
    'Coloane',
    '路環島',
    '路环海岸与慢节奏探索区域',
    'Coloane coastal exploration district',
    '路環海岸與慢節奏探索區域',
    '用于承接路环相关地图、POI 与故事线。',
    'Sub-map for Coloane routes and POIs.',
    '用於承接路環相關地圖、POI 與故事線。',
    22.1197000,
    113.5695000,
    3
) seeded
JOIN `cities` macau ON macau.`code` = 'macau'
ON DUPLICATE KEY UPDATE
  `city_id` = VALUES(`city_id`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_en` = VALUES(`subtitle_en`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `source_coordinate_system` = VALUES(`source_coordinate_system`),
  `source_center_lat` = VALUES(`source_center_lat`),
  `source_center_lng` = VALUES(`source_center_lng`),
  `center_lat` = VALUES(`center_lat`),
  `center_lng` = VALUES(`center_lng`),
  `bounds_json` = VALUES(`bounds_json`),
  `popup_config_json` = VALUES(`popup_config_json`),
  `display_config_json` = VALUES(`display_config_json`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `published_at` = VALUES(`published_at`),
  `deleted` = VALUES(`deleted`);

UPDATE `storylines`
SET `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1)
WHERE `code` IN ('taipa_leisure_walk', 'coastal_echoes');

UPDATE `pois`
SET
  `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1),
  `sub_map_id` = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1),
  `source_coordinate_system` = COALESCE(NULLIF(`source_coordinate_system`, ''), 'GCJ02'),
  `source_latitude` = COALESCE(`source_latitude`, `latitude`),
  `source_longitude` = COALESCE(`source_longitude`, `longitude`)
WHERE `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);

UPDATE `pois`
SET
  `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1),
  `sub_map_id` = (SELECT `id` FROM `sub_maps` WHERE `code` = 'taipa' LIMIT 1),
  `source_coordinate_system` = COALESCE(NULLIF(`source_coordinate_system`, ''), 'GCJ02'),
  `source_latitude` = COALESCE(`source_latitude`, `latitude`),
  `source_longitude` = COALESCE(`source_longitude`, `longitude`)
WHERE `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'taipa' LIMIT 1);

UPDATE `pois`
SET
  `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1),
  `sub_map_id` = (SELECT `id` FROM `sub_maps` WHERE `code` = 'coloane' LIMIT 1),
  `source_coordinate_system` = COALESCE(NULLIF(`source_coordinate_system`, ''), 'GCJ02'),
  `source_latitude` = COALESCE(`source_latitude`, `latitude`),
  `source_longitude` = COALESCE(`source_longitude`, `longitude`)
WHERE `city_id` = (SELECT `id` FROM `cities` WHERE `code` = 'coloane' LIMIT 1);

UPDATE `cities`
SET
  `popup_config_json` = COALESCE(`popup_config_json`, JSON_OBJECT('enabled', TRUE, 'mode', 'sheet')),
  `display_config_json` = COALESCE(`display_config_json`, JSON_OBJECT('showCover', TRUE, 'showDescription', TRUE))
WHERE `code` IN ('macau', 'hengqin', 'hong-kong', 'ecnu');
