USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @seed_openid = 'phase11-seed';
SET @storyline_macau_fire_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_macau_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @cover_story = 'seed://asset/300001';
SET @cover_archive = 'seed://asset/300009';
SET @cover_activity = 'seed://asset/300010';

SET @old_activity_night_walk_id = (SELECT `id` FROM `activities` WHERE `code` = 'macau_fortress_night_walk' LIMIT 1);
SET @old_activity_archive_task_id = (SELECT `id` FROM `activities` WHERE `code` = 'harbour_boundary_archive_task' LIMIT 1);
SET @old_activity_photo_challenge_id = (SELECT `id` FROM `activities` WHERE `code` = 'monte_fort_photo_challenge' LIMIT 1);
SET @old_activity_poster_workshop_id = (SELECT `id` FROM `activities` WHERE `code` = 'senado_history_poster_workshop' LIMIT 1);

DELETE FROM `content_relation_links`
WHERE `owner_type` = 'activity'
  AND `owner_id` IN (@old_activity_night_walk_id, @old_activity_archive_task_id, @old_activity_photo_challenge_id, @old_activity_poster_workshop_id);

DELETE FROM `activities`
WHERE `code` IN ('macau_fortress_night_walk', 'harbour_boundary_archive_task', 'monte_fort_photo_challenge', 'senado_history_poster_workshop');

INSERT INTO `activities` (
  `code`, `activity_type`, `title`, `description`, `cover_url`,
  `start_time`, `end_time`, `status`, `participation_count`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `html_zh`, `html_en`, `html_zht`, `html_pt`,
  `venue_name_zh`, `venue_name_en`, `venue_name_zht`, `venue_name_pt`,
  `address_zh`, `address_en`, `address_zht`, `address_pt`,
  `organizer_name`, `organizer_contact`, `organizer_website`,
  `signup_capacity`, `signup_fee_amount`, `signup_start_at`, `signup_end_at`,
  `publish_start_at`, `publish_end_at`, `is_pinned`, `cover_asset_id`, `hero_asset_id`, `sort_order`, `_openid`
)
VALUES
  ('macau_fortress_night_walk', 'official_event', '炮台夜行导览', '沿主线夜游妈阁庙至大炮台的官方沉浸式步行活动。', @cover_activity, '2026-05-01 19:00:00', '2027-12-31 22:30:00', 'published', 186, '炮台夜行导览', 'Fortress Night Walk', '炮台夜行導覽', 'Caminhada noturna da fortaleza', '黄昏集合、沿线打卡、夜间音景与炮台灯光同场展开。', 'A dusk event with check-ins, sound design, and fortress lighting.', '黃昏集合、沿線打卡、夜間音景與炮台燈光同場展開。', 'Evento ao entardecer com carimbos, audio e luz na fortaleza.', '从妈阁庙出发，经亚婆井、岗顶剧院抵达大炮台，让玩家在夜色里重走战火与边界。', 'Starting at A-Ma Temple, the guided route moves through Lilau and the hill district before reaching Monte Fort.', '從媽閣廟出發，經亞婆井、崗頂劇院抵達大炮台，讓玩家在夜色裡重走戰火與邊界。', 'Parte de A-Ma, passa por Lilau e pela colina e termina na fortaleza.', '<section><h2>夜行节奏</h2><p>沿线打卡、剧情播报与夜景回顾同场展开。</p></section>', '<section><h2>Night Flow</h2><p>Check-ins, scene prompts, and a night recap unfold along the walk.</p></section>', '<section><h2>夜行節奏</h2><p>沿線打卡、劇情播報與夜景回顧同場展開。</p></section>', '<section><h2>Ritmo noturno</h2><p>Carimbos, narrativa e recapitulacao acompanham a caminhada.</p></section>', '妈阁庙至大炮台', 'A-Ma Temple to Monte Fort', '媽閣廟至大炮台', 'De A-Ma a Fortaleza do Monte', '妈阁庙前地集合', 'Meet at Largo do Pagode da Barra', '媽閣廟前地集合', 'Encontro no Largo do Pagode da Barra', 'Trip of Macau 策展组', 'ops@tripofmacau.com', 'https://tripofmacau.com/events/night-walk', 40, 88.00, '2026-04-01 10:00:00', '2027-12-31 18:00:00', '2026-04-01 00:00:00', '2027-12-31 23:59:59', 1, 300010, 300001, 0, @seed_openid),
  ('harbour_boundary_archive_task', 'global_task', '界线史料收集任务', '围绕路线关键站点的常驻任务。', @cover_archive, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 'published', 412, '界线史料收集任务', 'Boundary Archive Mission', '界線史料收集任務', 'Missao do arquivo de fronteira', '要求玩家在妈阁庙、亚婆井与岗顶完成阅读、停留与互动。', 'Players read, stay, and interact at the key route stops.', '要求玩家在媽閣廟、亞婆井與崗頂完成閱讀、停留與互動。', 'Pede leitura, permanencia e interacao nos pontos-chave da rota.', '完成后可同步解锁界线守护者的徽章路径与路线史料卡。', 'Completion unlocks the boundary badge path and the route archive card.', '完成後可同步解鎖界線守護者的徽章路徑與路線史料卡。', 'Ao concluir, desbloqueia a via da insignia e o cartao de arquivo da rota.', '<section><h2>任务条件</h2><ul><li>阅读妈阁开场</li><li>亚婆井停留十秒</li><li>查看岗顶瞭望对照</li></ul></section>', '<section><h2>Mission Conditions</h2><ul><li>Read the opening at A-Ma</li><li>Stay in Lilau for ten seconds</li><li>Open the hill lookout comparison</li></ul></section>', '<section><h2>任務條件</h2><ul><li>閱讀媽閣開場</li><li>亞婆井停留十秒</li><li>查看崗頂瞭望對照</li></ul></section>', '<section><h2>Condicoes</h2><ul><li>Ler a abertura em A-Ma</li><li>Ficar dez segundos em Lilau</li><li>Abrir a comparacao da vigia</li></ul></section>', '澳门半岛历史步行核心', 'Historic walking core of Macau', '澳門半島歷史步行核心', 'Nucleo historico pedonal de Macau', '妈阁庙、亚婆井、岗顶剧院', 'A-Ma Temple, Lilau Square, Dom Pedro V Theatre', '媽閣廟、亞婆井、崗頂劇院', 'A-Ma, Lilau e Teatro Dom Pedro V', 'Trip of Macau Runtime Mission', 'mission@tripofmacau.com', 'https://tripofmacau.com/missions/boundary-archive', NULL, 0.00, NULL, NULL, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 1, 300009, 300010, 1, @seed_openid),
  ('monte_fort_photo_challenge', 'discovery_campaign', '炮台光影摄影挑战', '面向发现页的路线摄影活动。', @cover_activity, '2026-05-01 00:00:00', '2027-12-31 23:59:59', 'published', 263, '炮台光影摄影挑战', 'Monte Fort Photo Challenge', '炮台光影攝影挑戰', 'Desafio fotografico da fortaleza', '鼓励玩家在大炮台与议事亭前地拍摄两张指定构图。', 'Players capture two guided compositions across Monte Fort and Senado Square.', '鼓勵玩家在大炮台與議事亭前地拍攝兩張指定構圖。', 'Convida o utilizador a captar duas composicoes guiadas entre a fortaleza e o Senado.', '上传后会生成一张路线主题海报，可回流到故事终章。', 'Uploads generate a themed poster that can flow back into the story finale.', '上傳後會生成一張路線主題海報，可回流到故事終章。', 'Depois do envio, gera-se um poster tematico que regressa ao final da historia.', '<section><h2>拍摄要求</h2><p>先拍炮位与海面，再拍议事亭前地的波浪铺地与立面。</p></section>', '<section><h2>Shot Guide</h2><p>Capture the battery and harbor first, then the wave pavement and facade at Senado Square.</p></section>', '<section><h2>拍攝要求</h2><p>先拍炮位與海面，再拍議事亭前地的波浪鋪地與立面。</p></section>', '<section><h2>Guia</h2><p>Primeiro a bateria com o mar; depois o pavimento ondulado e a fachada do Senado.</p></section>', '大炮台与议事亭前地', 'Monte Fort and Senado Square', '大炮台與議事亭前地', 'Fortaleza do Monte e Largo do Senado', '大炮台街与议事亭前地', 'Calcada das Verdades and Senado Square', '大炮台街與議事亭前地', 'Calcada das Verdades e Largo do Senado', 'Trip of Macau Discover Lab', 'discover@tripofmacau.com', 'https://tripofmacau.com/campaigns/fort-photo', 120, 18.00, '2026-04-15 10:00:00', '2027-12-31 20:00:00', '2026-04-15 00:00:00', '2027-12-31 23:59:59', 0, 300010, 300009, 2, @seed_openid),
  ('senado_history_poster_workshop', 'private_event', '议事亭史料海报工坊', '小规模报名制的路线创作活动。', @cover_archive, '2026-06-01 14:00:00', '2027-12-31 17:00:00', 'published', 78, '议事亭史料海报工坊', 'Senado Archive Poster Workshop', '議事亭史料海報工坊', 'Oficina de cartazes historicos do Senado', '把路线收集物、地图碎片与照片成果整理成个人探索海报。', 'Turn route collectibles, map fragments, and photo results into a personal poster.', '把路線收集物、地圖碎片與照片成果整理成個人探索海報。', 'Transforma colecionaveis, fragmentos e fotos da rota em poster pessoal.', '活动提供已授权史料卡与模板，让玩家做出可分享版本。', 'The workshop provides licensed archive cards and layouts for a shareable result.', '活動提供已授權史料卡與模板，讓玩家做出可分享版本。', 'A oficina oferece cartoes de arquivo e modelos para um resultado partilhavel.', '<section><h2>工坊内容</h2><p>素材挑选、标题编排、色彩方向与海报导出。</p></section>', '<section><h2>Workshop Flow</h2><p>Asset selection, title composition, color direction, and poster export.</p></section>', '<section><h2>工坊內容</h2><p>素材挑選、標題編排、色彩方向與海報導出。</p></section>', '<section><h2>Fluxo</h2><p>Selecao de materiais, titulo, cor e exportacao do poster.</p></section>', '议事亭前地周边', 'Around Senado Square', '議事亭前地周邊', 'Zona do Largo do Senado', '议事亭前地附近工作坊空间', 'Workshop studio near Senado Square', '議事亭前地附近工作坊空間', 'Estudio perto do Largo do Senado', 'Trip of Macau Community', 'community@tripofmacau.com', 'https://tripofmacau.com/private/senado-poster', 24, 128.00, '2026-05-01 10:00:00', '2027-12-31 12:00:00', '2026-05-01 00:00:00', '2027-12-31 23:59:59', 0, 300009, 300010, 3, @seed_openid);

SET @activity_night_walk_id = (SELECT `id` FROM `activities` WHERE `code` = 'macau_fortress_night_walk' LIMIT 1);
SET @activity_archive_task_id = (SELECT `id` FROM `activities` WHERE `code` = 'harbour_boundary_archive_task' LIMIT 1);
SET @activity_photo_challenge_id = (SELECT `id` FROM `activities` WHERE `code` = 'monte_fort_photo_challenge' LIMIT 1);
SET @activity_poster_workshop_id = (SELECT `id` FROM `activities` WHERE `code` = 'senado_history_poster_workshop' LIMIT 1);

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
VALUES
  ('activity', @activity_night_walk_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('entry', 'official'), 0),
  ('activity', @activity_night_walk_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'historic-core'), 0),
  ('activity', @activity_night_walk_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('mode', 'guided'), 0),
  ('activity', @activity_night_walk_id, 'attachment_asset', 'asset', 300009, 'phase11/macau-fire/archive-card.svg', JSON_OBJECT('label', '夜行任务卡'), 0),
  ('activity', @activity_archive_task_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('entry', 'task'), 0),
  ('activity', @activity_archive_task_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'historic-core'), 0),
  ('activity', @activity_archive_task_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('mode', 'mission'), 0),
  ('activity', @activity_archive_task_id, 'attachment_asset', 'asset', 300010, 'phase11/macau-fire/activity-card.svg', JSON_OBJECT('label', '任务流程图'), 0),
  ('activity', @activity_photo_challenge_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('entry', 'discover'), 0),
  ('activity', @activity_photo_challenge_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'fortress-senado'), 0),
  ('activity', @activity_photo_challenge_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('mode', 'challenge'), 0),
  ('activity', @activity_photo_challenge_id, 'attachment_asset', 'asset', 300010, 'phase11/macau-fire/activity-card.svg', JSON_OBJECT('label', '摄影示意图'), 0),
  ('activity', @activity_poster_workshop_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('entry', 'community'), 0),
  ('activity', @activity_poster_workshop_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('scope', 'senado'), 0),
  ('activity', @activity_poster_workshop_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('mode', 'community'), 0),
  ('activity', @activity_poster_workshop_id, 'attachment_asset', 'asset', 300009, 'phase11/macau-fire/archive-card.svg', JSON_OBJECT('label', '工坊示例图'), 0);
