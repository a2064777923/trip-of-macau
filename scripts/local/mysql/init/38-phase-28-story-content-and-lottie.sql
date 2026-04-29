USE `aoxiaoyou`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'content_assets'
    AND `column_name` = 'animation_subtype'
);
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE `content_assets` ADD COLUMN `animation_subtype` VARCHAR(64) NULL AFTER `mime_type`', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'content_assets'
    AND `column_name` = 'poster_asset_id'
);
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE `content_assets` ADD COLUMN `poster_asset_id` BIGINT NULL AFTER `animation_subtype`', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'content_assets'
    AND `column_name` = 'fallback_asset_id'
);
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE `content_assets` ADD COLUMN `fallback_asset_id` BIGINT NULL AFTER `poster_asset_id`', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'content_assets'
    AND `column_name` = 'default_loop'
);
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE `content_assets` ADD COLUMN `default_loop` TINYINT NOT NULL DEFAULT 1 AFTER `fallback_asset_id`', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM `information_schema`.`columns`
  WHERE `table_schema` = DATABASE()
    AND `table_name` = 'content_assets'
    AND `column_name` = 'default_autoplay'
);
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE `content_assets` ADD COLUMN `default_autoplay` TINYINT NOT NULL DEFAULT 1 AFTER `default_loop`', 'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `story_content_blocks` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL,
  `block_type` VARCHAR(32) NOT NULL,
  `title_zh` VARCHAR(255) NOT NULL DEFAULT '',
  `title_en` VARCHAR(255) NOT NULL DEFAULT '',
  `title_zht` VARCHAR(255) NOT NULL DEFAULT '',
  `title_pt` VARCHAR(255) NOT NULL DEFAULT '',
  `summary_zh` TEXT NULL,
  `summary_en` TEXT NULL,
  `summary_zht` TEXT NULL,
  `summary_pt` TEXT NULL,
  `body_zh` LONGTEXT NULL,
  `body_en` LONGTEXT NULL,
  `body_zht` LONGTEXT NULL,
  `body_pt` LONGTEXT NULL,
  `primary_asset_id` BIGINT NULL,
  `style_preset` VARCHAR(64) NOT NULL DEFAULT '',
  `display_mode` VARCHAR(64) NOT NULL DEFAULT '',
  `visibility_json` JSON NULL,
  `config_json` JSON NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_story_content_blocks_code` (`code`, `deleted`),
  KEY `idx_story_content_blocks_status_sort` (`status`, `sort_order`),
  KEY `idx_story_content_blocks_block_type` (`block_type`),
  KEY `idx_story_content_blocks_primary_asset_id` (`primary_asset_id`),
  CONSTRAINT `fk_story_content_blocks_primary_asset` FOREIGN KEY (`primary_asset_id`) REFERENCES `content_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `story_chapter_block_links` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `chapter_id` BIGINT NOT NULL,
  `block_id` BIGINT NOT NULL,
  `override_title_json` JSON NULL,
  `override_summary_json` JSON NULL,
  `override_body_json` JSON NULL,
  `display_condition_json` JSON NULL,
  `override_config_json` JSON NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'draft',
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_story_chapter_block_links_unique` (`chapter_id`, `block_id`, `deleted`),
  KEY `idx_story_chapter_block_links_chapter` (`chapter_id`, `sort_order`),
  KEY `idx_story_chapter_block_links_block` (`block_id`),
  CONSTRAINT `fk_story_chapter_block_links_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `story_chapters` (`id`),
  CONSTRAINT `fk_story_chapter_block_links_block` FOREIGN KEY (`block_id`) REFERENCES `story_content_blocks` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @phase28_seed_openid = 'phase28-seed';
SET @phase28_lottie_json = '{
  "v":"5.7.4",
  "fr":30,
  "ip":0,
  "op":60,
  "w":256,
  "h":256,
  "nm":"macau-pulse",
  "ddd":0,
  "assets":[],
  "layers":[
    {
      "ddd":0,
      "ind":1,
      "ty":4,
      "nm":"pulse",
      "sr":1,
      "ks":{
        "o":{"a":0,"k":100},
        "r":{"a":0,"k":0},
        "p":{"a":0,"k":[128,128,0]},
        "a":{"a":0,"k":[0,0,0]},
        "s":{"a":1,"k":[
          {"t":0,"s":[45,45,100]},
          {"t":20,"s":[92,92,100]},
          {"t":40,"s":[45,45,100]},
          {"t":60,"s":[45,45,100]}
        ]}
      },
      "shapes":[
        {"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[108,108]},"nm":"Ellipse Path 1","mn":"ADBE Vector Shape - Ellipse","hd":false},
        {"ty":"fl","c":{"a":0,"k":[0.231,0.529,0.925,1]},"o":{"a":0,"k":100},"r":1,"bm":0,"nm":"Fill 1","mn":"ADBE Vector Graphic - Fill","hd":false},
        {"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100},"sk":{"a":0,"k":0},"sa":{"a":0,"k":0},"nm":"Transform"}
      ],
      "ao":0,
      "ip":0,
      "op":60,
      "st":0,
      "bm":0
    }
  ]
}';
SET @phase28_lottie_url = 'https://tripofmacau-1301163924.cos.ap-hongkong.myqcloud.com/miniapp/assets/phase28/story/macau-pulse.json';
SET @phase28_audio_url = 'https://interactive-examples.mdn.mozilla.net/media/cc0-audio/t-rex-roar.mp3';
SET @phase28_video_url = 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4';

DELETE FROM `content_assets` WHERE `id` IN (328001, 328002, 328003);
INSERT INTO `content_assets` (
  `id`, `asset_kind`, `bucket_name`, `region`, `object_key`, `canonical_url`, `mime_type`, `animation_subtype`,
  `locale_code`, `original_filename`, `file_extension`, `upload_source`, `uploaded_by_admin_name`,
  `file_size_bytes`, `width_px`, `height_px`, `checksum`, `etag`, `processing_policy_code`, `processing_status`,
  `default_loop`, `default_autoplay`, `status`, `published_at`
)
VALUES
  (328001, 'lottie', 'seed', 'local', 'phase28/story/macau-pulse.json', @phase28_lottie_url, 'application/json', 'lottie-json',
   'zh-Hant', 'macau-pulse.json', 'json', 'seed', 'phase28-seed',
   CHAR_LENGTH(@phase28_lottie_json), 256, 256, SHA2(@phase28_lottie_json, 256), 'seed-328001', 'seeded', 'stored',
   1, 1, 'published', NOW()),
  (328002, 'audio', 'seed', 'remote', 'phase28/story/sample-audio.mp3', @phase28_audio_url, 'audio/mpeg', NULL,
   'zh-Hant', 'sample-audio.mp3', 'mp3', 'seed', 'phase28-seed',
   CHAR_LENGTH(@phase28_audio_url), NULL, NULL, SHA2(@phase28_audio_url, 256), 'seed-328002', 'seeded', 'stored',
   1, 0, 'published', NOW()),
  (328003, 'video', 'seed', 'remote', 'phase28/story/sample-video.mp4', @phase28_video_url, 'video/mp4', NULL,
   'zh-Hant', 'sample-video.mp4', 'mp4', 'seed', 'phase28-seed',
   CHAR_LENGTH(@phase28_video_url), NULL, NULL, SHA2(@phase28_video_url, 256), 'seed-328003', 'seeded', 'stored',
   1, 0, 'published', NOW());

SET @phase28_storyline_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @phase28_chapter_1_id = (SELECT `id` FROM `story_chapters` WHERE `storyline_id` = @phase28_storyline_id AND `chapter_order` = 1 LIMIT 1);
SET @phase28_chapter_4_id = (SELECT `id` FROM `story_chapters` WHERE `storyline_id` = @phase28_storyline_id AND `chapter_order` = 4 LIMIT 1);
SET @phase28_chapter_5_id = (SELECT `id` FROM `story_chapters` WHERE `storyline_id` = @phase28_storyline_id AND `chapter_order` = 5 LIMIT 1);

DELETE FROM `content_asset_links` WHERE `entity_type` = 'story_content_block' AND `entity_id` BETWEEN 328101 AND 328108;
DELETE FROM `story_chapter_block_links` WHERE `block_id` BETWEEN 328101 AND 328108;
DELETE FROM `story_content_blocks` WHERE `id` BETWEEN 328101 AND 328108;

INSERT INTO `story_content_blocks` (
  `id`, `code`, `block_type`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `body_zh`, `body_en`, `body_zht`, `body_pt`,
  `primary_asset_id`, `style_preset`, `display_mode`, `visibility_json`, `config_json`,
  `status`, `sort_order`, `published_at`
)
VALUES
  (328101, 'macau-fire-opening-rich-text', 'rich_text',
   '海風初起', 'When the Sea Wind Rises', '海風初起', 'Quando sopra o vento do mar',
   '在媽閣廟前，故事從海風、香火與第一聲炮響開始。', 'The route opens with incense, sea wind, and the first echo of artillery.', '在媽閣廟前，故事從海風、香火與第一聲炮響開始。', 'A rota abre com incenso, vento do mar e o primeiro eco de artilharia.',
   '明朝水師、漁民與初來乍到的武裝勢力，在海岸邊界上短兵相接。這一章不是觀光導覽，而是把你帶回濠江最早的防線，聽見一座城市如何在風浪之間定義自己的邊界。',
   'Ming coastal forces, fishers, and armed arrivals meet at the first shoreline boundary. This chapter frames the city through defense and negotiation rather than sightseeing.',
   '明朝水師、漁民與初來乍到的武裝勢力，在海岸邊界上短兵相接。這一章不是觀光導覽，而是把你帶回濠江最早的防線，聽見一座城市如何在風浪之間定義自己的邊界。',
   'Forcas costeiras ming, pescadores e grupos armados encontram-se na primeira fronteira litoral. O capitulo devolve-te a defesa inicial da cidade.',
   NULL, 'narrative-opening', 'full-width', NULL, JSON_OBJECT('lead', TRUE),
   'published', 1, NOW()),
  (328102, 'macau-fire-opening-image', 'image',
   '媽閣海岸場景', 'A-Ma Coastline Scene', '媽閣海岸場景', 'Cena costeira de A-Ma',
   '用一張主視覺開場，建立故事氣氛。', 'A hero still image that sets the tone.', '用一張主視覺開場，建立故事氣氛。', 'Uma imagem principal que define a atmosfera.',
   NULL, NULL, NULL, NULL,
   300003, 'hero-card', 'media-card', NULL, JSON_OBJECT('rounded', TRUE),
   'published', 2, NOW()),
  (328103, 'macau-fire-opening-audio', 'audio',
   '古戰場環境音', 'Ancient Battlefield Soundscape', '古戰場環境音', 'Paisagem sonora do campo antigo',
   '播放海風、鼓聲與炮響，讓第一章更沉浸。', 'Plays sea wind, drums, and cannon echoes.', '播放海風、鼓聲與炮響，讓第一章更沉浸。', 'Toca vento, tambores e canhoes para aumentar a imersao.',
   NULL, NULL, NULL, NULL,
   328002, 'ambient-audio', 'inline-audio', NULL, JSON_OBJECT('caption', '靠近媽閣後自動播放'),
   'published', 3, NOW()),
  (328104, 'macau-fire-opening-lottie', 'lottie',
   '炮火脈衝動畫', 'Cannon Pulse Animation', '炮火脈衝動畫', 'Animacao de pulso de canhao',
   '以 Lottie 顯示故事節奏，作為互動動畫層的第一版示例。', 'A Lottie pulse that signals the story rhythm.', '以 Lottie 顯示故事節奏，作為互動動畫層的第一版示例。', 'Uma animacao Lottie que marca o ritmo da historia.',
   NULL, NULL, NULL, NULL,
   328001, 'ambient-lottie', 'inline-lottie', NULL, JSON_OBJECT('heightPx', 220, 'loop', TRUE, 'autoplay', TRUE),
   'published', 4, NOW()),
  (328105, 'macau-fire-boundary-quote', 'quote',
   '邊界從來不只是地理線', 'A boundary is never only a line on a map', '邊界從來不只是地理線', 'A fronteira nunca e apenas uma linha',
   '第二章引用型積木。', 'Quote block for chapter two.', '第二章引用型積木。', 'Bloco de citacao para o segundo capitulo.',
   '一堵牆、一口井、一份舊地圖，都可能比一場正面衝突更長久地改變城市的走向。',
   'A wall, a well, and an old map may shape a city longer than open conflict does.',
   '一堵牆、一口井、一份舊地圖，都可能比一場正面衝突更長久地改變城市的走向。',
   'Um muro, um poço e um mapa antigo podem moldar a cidade por mais tempo do que uma batalha aberta.',
   NULL, 'pull-quote', 'callout', NULL, JSON_OBJECT('tone', 'historic'),
   'published', 5, NOW()),
  (328106, 'macau-fire-battle-video', 'video',
   '戰役回顧短片', 'Battle Recap Clip', '戰役回顧短片', 'Video resumo da batalha',
   '第四章高潮段落的短片。', 'A short recap clip for the climax chapter.', '第四章高潮段落的短片。', 'Um pequeno video para o capitulo de auge.',
   NULL, NULL, NULL, NULL,
   328003, 'battle-video', 'video-card', NULL, JSON_OBJECT('posterLabel', '大炮台戰役回顧'),
   'published', 6, NOW()),
  (328107, 'macau-fire-route-gallery', 'gallery',
   '路線回顧圖集', 'Route Recap Gallery', '路線回顧圖集', 'Galeria da rota',
   '第五章結尾以圖集回顧整條路線。', 'A closing gallery that recaps the route.', '第五章結尾以圖集回顧整條路線。', 'Uma galeria final que recapitula o percurso.',
   NULL, NULL, NULL, NULL,
   300007, 'recap-gallery', 'gallery-grid', NULL, JSON_OBJECT('columns', 2),
   'published', 7, NOW()),
  (328108, 'macau-fire-hidden-archive', 'attachment_list',
   '隱藏史料庫', 'Hidden Archive', '隱藏史料庫', 'Arquivo oculto',
   '第五章完成後解鎖的附件型內容。', 'Attachment-list content unlocked at the finale.', '第五章完成後解鎖的附件型內容。', 'Conteudo em anexos desbloqueado no final.',
   NULL, NULL, NULL, NULL,
   NULL, 'archive-list', 'attachment-list', NULL, JSON_OBJECT('showIcons', TRUE),
   'published', 8, NOW());

INSERT INTO `content_asset_links` (
  `entity_type`, `entity_id`, `usage_type`, `asset_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `status`, `sort_order`
)
VALUES
  ('story_content_block', 328107, 'gallery', 300005, '山城戒備', 'Hill Watch', '山城戒備', 'Vigia da colina', 'published', 0),
  ('story_content_block', 328107, 'gallery', 300006, '炮台硝煙', 'Fortress Fire', '炮台硝煙', 'Fumo da fortaleza', 'published', 1),
  ('story_content_block', 328108, 'attachment', 300009, '戰事檔案卡', 'Archive Card', '戰事檔案卡', 'Cartao de arquivo', 'published', 0),
  ('story_content_block', 328108, 'attachment', 300010, '活動延伸卡', 'Activity Card', '活動延伸卡', 'Cartao de atividade', 'published', 1);

INSERT INTO `story_chapter_block_links` (
  `chapter_id`, `block_id`, `override_title_json`, `override_summary_json`, `override_body_json`,
  `display_condition_json`, `override_config_json`, `status`, `sort_order`
)
VALUES
  (@phase28_chapter_1_id, 328101, NULL, NULL, NULL, NULL, NULL, 'published', 0),
  (@phase28_chapter_1_id, 328102, NULL, NULL, NULL, NULL, NULL, 'published', 1),
  (@phase28_chapter_1_id, 328103, NULL, NULL, NULL, JSON_OBJECT('trigger', 'proximity', 'distanceMeters', 50), NULL, 'published', 2),
  (@phase28_chapter_1_id, 328104, JSON_OBJECT('zh-Hant', '炮火初起', 'en', 'First Pulse', 'zh-Hans', '炮火初起', 'pt', 'Primeiro pulso'), NULL, NULL, NULL, JSON_OBJECT('heightPx', 220), 'published', 3),
  (@phase28_chapter_4_id, 328106, NULL, NULL, NULL, NULL, NULL, 'published', 0),
  (@phase28_chapter_5_id, 328107, NULL, NULL, NULL, NULL, NULL, 'published', 0),
  (@phase28_chapter_5_id, 328108, NULL, NULL, NULL, JSON_OBJECT('requiresRewardCode', 'reward_historic_archive'), NULL, 'published', 1);

UPDATE `storylines`
SET
  `name_zh` = '濠江烽煙：東西方文明的戰火與共生',
  `name_zht` = '濠江烽煙：東西方文明的戰火與共生',
  `name_en` = 'Macau Under Fire: Conflict and Coexistence',
  `name_pt` = 'Macau em Chamas: guerra e convivencia',
  `description_zh` = '沿媽閣廟、亞婆井前地、崗頂劇院、大炮台到議事亭前地，重走澳門從海防對峙走向文明共生的故事線。',
  `description_zht` = '沿媽閣廟、亞婆井前地、崗頂劇院、大炮台到議事亭前地，重走澳門從海防對峙走向文明共生的故事線。',
  `reward_badge_zh` = '要塞英雄',
  `reward_badge_zht` = '要塞英雄'
WHERE `id` = @phase28_storyline_id;

UPDATE `story_chapters`
SET
  `title_zh` = CASE `chapter_order`
    WHEN 1 THEN '第一章 鏡海初戰'
    WHEN 2 THEN '第二章 南灣防線'
    WHEN 3 THEN '第三章 山城戒備'
    WHEN 4 THEN '第四章 炮台硝煙'
    WHEN 5 THEN '第五章 烽煙落幕'
    ELSE `title_zh`
  END,
  `title_zht` = CASE `chapter_order`
    WHEN 1 THEN '第一章 鏡海初戰'
    WHEN 2 THEN '第二章 南灣防線'
    WHEN 3 THEN '第三章 山城戒備'
    WHEN 4 THEN '第四章 炮台硝煙'
    WHEN 5 THEN '第五章 烽煙落幕'
    ELSE `title_zht`
  END
WHERE `storyline_id` = @phase28_storyline_id;

SET FOREIGN_KEY_CHECKS = 1;
