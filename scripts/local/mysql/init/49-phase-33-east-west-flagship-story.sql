-- Phase 33: complete East-West flagship story seed.
-- All multilingual text is UTF-8 / utf8mb4. Do not rewrite through non-UTF-8 inline shell literals.
-- Exploration progress is calculated from exploration_elements semantic weights, never fixed percent grants.

USE `aoxiaoyou`;

SET NAMES utf8mb4;

SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @package_id = (SELECT `id` FROM `story_material_packages` WHERE `code` = 'east_west_war_and_coexistence_package' AND `deleted` = 0 LIMIT 1);

INSERT INTO `storylines` (
  `city_id`, `code`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `estimated_minutes`, `difficulty`, `cover_asset_id`, `banner_asset_id`,
  `reward_badge_zh`, `reward_badge_en`, `reward_badge_zht`, `reward_badge_pt`,
  `status`, `sort_order`, `published_at`, `deleted`
) VALUES (
  @city_macau_id,
  'east_west_war_and_coexistence',
  '東西方文明的戰火與共生',
  'War and Coexistence of Eastern and Western Civilizations',
  '東西方文明的戰火與共生',
  'Guerra e coexistencia das civilizacoes oriental e ocidental',
  '你是一名穿越時空的濠江歷史見證者，追隨一枚殘缺的海防銅鏡，踏遍澳門軍事要塞與城市交界，見證戰火從對峙走向文明共生。',
  'A time-travel witness follows a broken coastal-defense bronze mirror across Macau and sees conflict move toward coexistence.',
  '你是一名穿越時空的濠江歷史見證者，追隨一枚殘缺的海防銅鏡，踏遍澳門軍事要塞與城市交界，見證戰火從對峙走向文明共生。',
  'Uma testemunha historica segue um espelho de defesa costeira por Macau, do conflito a convivencia.',
  150,
  'medium',
  333001,
  333002,
  '濠江見證者',
  'Witness of the Inner Harbour',
  '濠江見證者',
  'Testemunha do Porto Interior',
  'published',
  3300,
  NOW(),
  0
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

SET @storyline_east_west_id = (SELECT `id` FROM `storylines` WHERE `code` = 'east_west_war_and_coexistence' AND `deleted` = 0 LIMIT 1);

INSERT INTO `story_material_packages` (
  `code`, `storyline_id`, `title_zh`, `title_zht`, `title_en`, `title_pt`,
  `summary_zh`, `summary_zht`, `historical_basis_zh`, `historical_basis_zht`,
  `literary_dramatization_zh`, `literary_dramatization_zht`,
  `local_root`, `cos_prefix`, `manifest_path`, `manifest_json`, `package_status`,
  `material_count`, `asset_count`, `story_object_count`, `created_by_admin_name`, `published_at`, `deleted`
) SELECT
  'east_west_war_and_coexistence_package',
  @storyline_east_west_id,
  '東西方文明的戰火與共生',
  '東西方文明的戰火與共生',
  'War and Coexistence of Eastern and Western Civilizations',
  'Guerra e coexistencia das civilizacoes oriental e ocidental',
  '五章旗艦故事素材包，供 v3.0 故事體驗編排系統追蹤與復用。',
  '五章旗艦故事素材包，供 v3.0 故事體驗編排系統追蹤與復用。',
  '見 historical-checklist.md：史實依據章節。',
  '見 historical-checklist.md：史實依據章節。',
  '見 historical-checklist.md：文學演繹章節。',
  '見 historical-checklist.md：文學演繹章節。',
  'local-content/phase33/east-west-war-and-coexistence',
  'miniapp/assets/phase33/east-west-war-and-coexistence',
  'docs/content-packages/east-west-war-and-coexistence/content-manifest.json',
  JSON_OBJECT('schemaVersion', 1, 'manifestPath', 'docs/content-packages/east-west-war-and-coexistence/content-manifest.json', 'materialCount', 54),
  'published',
  54,
  54,
  0,
  'phase33-seed',
  NOW(),
  0
WHERE @package_id IS NULL
ON DUPLICATE KEY UPDATE
  `storyline_id` = VALUES(`storyline_id`),
  `package_status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW()),
  `deleted` = 0;

SET @package_id = (SELECT `id` FROM `story_material_packages` WHERE `code` = 'east_west_war_and_coexistence_package' AND `deleted` = 0 LIMIT 1);

SET @poi_ama_id = (SELECT `id` FROM `pois` WHERE `city_id` = @city_macau_id AND `code` = 'ama_temple' AND COALESCE(`deleted`, 0) = 0 LIMIT 1);
SET @poi_lilau_id = (SELECT `id` FROM `pois` WHERE `city_id` = @city_macau_id AND `code` = 'lilau_square' AND COALESCE(`deleted`, 0) = 0 LIMIT 1);
SET @poi_monte_id = (SELECT `id` FROM `pois` WHERE `city_id` = @city_macau_id AND `code` = 'monte_fort' AND COALESCE(`deleted`, 0) = 0 LIMIT 1);
SET @poi_senado_id = (SELECT `id` FROM `pois` WHERE `city_id` = @city_macau_id AND `code` = 'senado_square' AND COALESCE(`deleted`, 0) = 0 LIMIT 1);
SET @poi_hill_id = (
  SELECT `id`
  FROM `pois`
  WHERE `city_id` = @city_macau_id
    AND `code` IN ('st_augustine_square', 'dom_pedro_v_theatre', 'st_lawrence_church', 'st_augustine_church')
    AND COALESCE(`deleted`, 0) = 0
  ORDER BY FIELD(`code`, 'st_augustine_square', 'dom_pedro_v_theatre', 'st_lawrence_church', 'st_augustine_church')
  LIMIT 1
);

INSERT INTO `pois` (
  `city_id`, `sub_map_id`, `storyline_id`, `code`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`, `subtitle_pt`,
  `address_zh`, `address_en`, `address_zht`, `address_pt`,
  `source_coordinate_system`, `source_latitude`, `source_longitude`,
  `city_code`, `latitude`, `longitude`, `category_code`, `district_zh`, `district_en`, `district_zht`, `district_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `intro_title_zh`, `intro_title_en`, `intro_title_zht`, `intro_title_pt`,
  `intro_summary_zh`, `intro_summary_en`, `intro_summary_zht`, `intro_summary_pt`,
  `trigger_radius`, `manual_checkin_radius`, `stay_seconds`, `status`, `sort_order`, `published_at`, `_openid`, `deleted`
) SELECT
  @city_macau_id, @sub_map_peninsula_id, @storyline_east_west_id, 'st_augustine_square',
  '崗頂前地', 'St. Augustine Square', '崗頂前地', 'Largo de Santo Agostinho',
  '教會、劇院與高地視野交會的章節錨點', 'Hilltop cultural anchor', '教會、劇院與高地視野交會的章節錨點', 'Ancora cultural na colina',
  '澳門半島崗頂前地一帶', 'St. Augustine Square, Macau Peninsula', '澳門半島崗頂前地一帶', 'Largo de Santo Agostinho, Macau',
  'GCJ02', 22.1935000, 113.5390000,
  'macau', 22.19350000, 113.53900000, 'historic_square', '澳門半島', 'Macau Peninsula', '澳門半島', 'Península de Macau',
  '崗頂一帶具教堂、劇院、修院與高地視野，適合承載宗教建築、城市教育與海面觀察的歷史空間閱讀。',
  'The hill area connects churches, theatre, seminary space and views over the city.',
  '崗頂一帶具教堂、劇院、修院與高地視野，適合承載宗教建築、城市教育與海面觀察的歷史空間閱讀。',
  'A zona da colina liga igrejas, teatro, seminario e vistas urbanas.',
  '山城戒備', 'Hill Watch', '山城戒備', 'Vigia da colina',
  '此 POI 為 Phase 33 旗艦故事線的安全錨點；若正式 POI 已存在，seed 會優先使用正式資料。',
  'Fixture anchor for Phase 33 if no official hill POI exists.',
  '此 POI 為 Phase 33 旗艦故事線的安全錨點；若正式 POI 已存在，seed 會優先使用正式資料。',
  'Ancora de fixture para Phase 33.',
  50, 200, 30, 'published', 33330, NOW(), 'phase33-seed', 0
WHERE @poi_hill_id IS NULL;

SET @poi_hill_id = COALESCE(
  @poi_hill_id,
  (SELECT `id` FROM `pois` WHERE `city_id` = @city_macau_id AND `code` = 'st_augustine_square' AND COALESCE(`deleted`, 0) = 0 LIMIT 1)
);

DROP TEMPORARY TABLE IF EXISTS `phase33_chapters`;
CREATE TEMPORARY TABLE `phase33_chapters` (
  `chapter_order` INT PRIMARY KEY,
  `chapter_code` VARCHAR(96) NOT NULL,
  `flow_code` VARCHAR(96) NOT NULL,
  `anchor_code` VARCHAR(64) NOT NULL,
  `anchor_id` BIGINT NULL,
  `title_zht` VARCHAR(255) NOT NULL,
  `title_en` VARCHAR(255) NOT NULL,
  `location_zht` VARCHAR(255) NOT NULL,
  `summary_zht` TEXT NOT NULL,
  `detail_zht` LONGTEXT NOT NULL,
  `achievement_zht` VARCHAR(255) NOT NULL,
  `collectible_zht` VARCHAR(255) NOT NULL,
  `media_asset_id` BIGINT NOT NULL,
  `hero_asset_id` BIGINT NOT NULL,
  `audio_asset_id` BIGINT NOT NULL,
  `mainline_names_json` JSON NOT NULL,
  `pickup_codes_json` JSON NOT NULL,
  `hidden_challenge_zht` VARCHAR(128) NOT NULL,
  `hidden_summary_zht` TEXT NOT NULL,
  `base_title_zht` VARCHAR(128) NOT NULL,
  `full_title_zht` VARCHAR(128) NOT NULL,
  `hidden_title_zht` VARCHAR(128) NOT NULL,
  `fragment_zht` VARCHAR(128) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `phase33_chapters` VALUES
  (1, 'ch01_mirror_sea_clash', 'story_east_west_ch01_flow', 'ama_temple', @poi_ama_id,
   '鏡海初戰：中葡首次海防對峙', 'Mirror Sea Clash: First Coastal Defense Confrontation', '媽閣廟',
   '抵達媽閣廟後，核心劇情短片、古戰場聲景與三個主線疊加物會接管 POI 預設體驗。',
   '1553 年前後，葡萄牙武裝商船以借地曬貨為名進入濠江。明朝水師與媽閣漁民在海岸防線上共同抵禦。這一章以史實背景作骨架，以銅鏡碎片、戰船、防線與商船疊加物作為文學化互動，帶玩家撿起戰火的第一塊碎片。',
   '初見濠江', '媽閣戰火銅鏡碎片', 333015, 333015, 333010,
   JSON_ARRAY('明朝水師戰船', '媽閣漁民防線', '葡國武裝商船'),
   JSON_ARRAY('ming_coastal_token', 'fisher_net_fragment', 'tax_contract_page'),
   '鏡海守護者', '三個拾取物全部收集且在媽閣廟範圍內停留五分鐘後，開啟三題中葡首次衝突歷史問答。',
   '濠江初見者', '鏡海探索者', '海岸守護人', '媽閣戰火銅鏡碎片'),
  (2, 'ch02_south_bay_boundary', 'story_east_west_ch02_flow', 'lilau_square', @poi_lilau_id,
   '南灣防線：葡人築城的邊界博弈', 'South Bay Line: Boundary Negotiation', '亞婆井前地',
   '亞婆井前地承載葡人早期聚居、水源記憶與中葡社群生活，故事模式將其轉成邊界路徑與雙點 AR 打卡。',
   '16 世紀中葉，葡人在南灣一帶形成居留與防禦空間，與明朝官府長期協商邊界。這一章以古井、界碑與老榕樹作為舞台，讓玩家用路徑動畫看見無硝煙的邊界博弈。',
   '界線守護者', '南灣界線銅鏡碎片', 333016, 333016, 333011,
   JSON_ARRAY('明葡邊界界碑', '亞婆井古井', '前地老榕樹'),
   JSON_ARRAY('boundary_rubbing', 'fort_design_page', 'garrison_flask'),
   '邊界見證者', '本章三個拾取物全收集且連續點擊古井疊加物三次後，開啟明葡邊界走向拼圖。',
   '南灣行路人', '界線記錄者', '無硝煙戰士', '南灣界線銅鏡碎片'),
  (3, 'ch03_hill_watch', 'story_east_west_ch03_flow', 'st_augustine_square', @poi_hill_id,
   '山城戒備：教會與軍防的雙重佈局', 'Hill Watch: Church and Defense', '崗頂前地',
   '崗頂一帶的教堂、劇院、修院與高地視野被編排為視角對比、多點打卡與隱藏瞭望挑戰。',
   '明清兩代，崗頂一帶既是宗教、教育與城市文化的重要場域，也具備俯瞰海面與城市的視野。這一章把鐘聲、修院牆面與山城視線編成一場安靜戰場的探索。',
   '山城瞭望者', '山城瞭望銅鏡碎片', 333017, 333017, 333012,
   JSON_ARRAY('軍事瞭望點', '崗頂劇院標記', '聖若瑟修院標記'),
   JSON_ARRAY('patrol_record', 'mission_military_letter', 'hill_defense_sketch'),
   '山城暗哨', '本章三個拾取物全收集且完成 360 度全景打卡後，要求標記三個核心海防觀測點。',
   '山城登頂者', '暗哨發現者', '全景見證人', '山城瞭望銅鏡碎片'),
  (4, 'ch04_fortress_fire', 'story_east_west_ch04_flow', 'monte_fort', @poi_monte_id,
   '炮台硝煙：荷澳戰役的生死反擊', 'Fortress Fire: Dutch-Macau Battle', '大炮台',
   '1622 年荷蘭艦隊攻澳與大炮台防守被編排成古炮點擊、城牆環繞與炮台佈防遊戲。',
   '1622 年荷蘭東印度公司艦隊攻澳，大炮台成為防守核心。這一章以城牆、古炮、炮彈與作戰圖重構中西軍民並肩防守的高張力戰役。',
   '要塞守護者', '炮台硝煙銅鏡碎片', 333018, 333018, 333013,
   JSON_ARRAY('東牆古炮', '南牆古炮', '西牆古炮', '北牆古炮'),
   JSON_ARRAY('cannonball_fragment', 'defender_diary', 'dutch_fleet_map', 'guardian_oath'),
   '要塞保衛戰', '本章四個拾取物全收集且澳門半島探索度達門檻後，開啟炮台防守佈局遊戲。',
   '炮台登臨者', '戰役還原者', '濠江守護神', '炮台硝煙銅鏡碎片'),
  (5, 'ch05_coexistence_finale', 'story_east_west_ch05_flow', 'senado_square', @poi_senado_id,
   '烽煙落幕：從對峙到文明共生', 'Finale: From Conflict to Coexistence', '議事亭前地',
   '終章在議事亭前地播放路線回顧、展示個人探索報告，並以完整銅鏡合成閉合全線故事。',
   '數百年戰火硝煙散去，澳門形成中西建築、宗教、商貿與城市治理交疊的文化景觀。玩家作為濠江歷史見證者，在議事亭前地完成文明共生紀念碑互動，讓殘缺銅鏡重新完整。',
   '濠江見證者', '完整濠江戰火銅鏡', 333019, 333019, 333014,
   JSON_ARRAY('全線路線回顧', '個人探索數據報告', '文明共生紀念碑'),
   JSON_ARRAY('coexistence_declaration', 'time_witness_pass', 'complete_copper_mirror'),
   '濠江通史大師', '全線所有拾取物收集且故事線探索元素完成後，開啟十題終極歷史問答。',
   '濠江見證者', '歷史還原大師', '濠江通史掌門人', '完整濠江戰火銅鏡');

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
)
SELECT
  @storyline_east_west_id,
  `chapter_order`,
  `title_zht`,
  `title_en`,
  `title_zht`,
  '',
  `summary_zht`,
  `summary_zht`,
  `summary_zht`,
  '',
  `detail_zht`,
  `detail_zht`,
  `detail_zht`,
  '',
  `achievement_zht`,
  `achievement_zht`,
  `achievement_zht`,
  '',
  `collectible_zht`,
  `collectible_zht`,
  `collectible_zht`,
  '',
  `location_zht`,
  `location_zht`,
  `location_zht`,
  '',
  `media_asset_id`,
  'poi',
  `anchor_id`,
  `anchor_code`,
  'sequence',
  JSON_OBJECT('schemaVersion', 1, 'preset', 'sequence_unlock', 'chapterOrder', `chapter_order`),
  JSON_OBJECT('schemaVersion', 1, 'summary', IF(`chapter_order` = 1, '開始故事線後解鎖', CONCAT('完成第 ', `chapter_order` - 1, ' 章後解鎖'))),
  JSON_OBJECT('schemaVersion', 1, 'summary', '完成主線互動、必要疊加物與章節結算'),
  JSON_OBJECT('schemaVersion', 1, 'baseTitle', `base_title_zht`, 'fullCollectionTitle', `full_title_zht`, 'hiddenTitle', `hidden_title_zht`, 'fragmentReward', `fragment_zht`),
  'published',
  `chapter_order` * 10,
  NOW()
FROM `phase33_chapters`
ON DUPLICATE KEY UPDATE
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
  `achievement_zht` = VALUES(`achievement_zht`),
  `collectible_zh` = VALUES(`collectible_zh`),
  `collectible_zht` = VALUES(`collectible_zht`),
  `location_name_zh` = VALUES(`location_name_zh`),
  `location_name_zht` = VALUES(`location_name_zht`),
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

INSERT INTO `experience_flows` (
  `code`, `flow_type`, `mode`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `map_policy_json`, `advanced_config_json`, `status`, `sort_order`, `published_at`
)
SELECT
  `flow_code`,
  'story_chapter',
  'storyline',
  CONCAT(`title_zht`, '故事專屬流程'),
  CONCAT(`title_en`, ' flow'),
  CONCAT(`title_zht`, '故事專屬流程'),
  '',
  CONCAT('繼承錨點 ', `location_zht`, ' 的預設體驗，並追加故事線主線、支線拾取、隱藏挑戰與獎勵稱號。'),
  CONCAT('Storyline chapter flow for ', `title_en`, '.'),
  CONCAT('繼承錨點 ', `location_zht`, ' 的預設體驗，並追加故事線主線、支線拾取、隱藏挑戰與獎勵稱號。'),
  '',
  JSON_OBJECT('schemaVersion', 1, 'storylineMode', TRUE, 'routeStyle', 'copper_flame', 'inactiveRouteStyle', 'muted_ink'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-east-west-flagship-story', 'chapterCode', `chapter_code`),
  'published',
  33000 + `chapter_order` * 10,
  NOW()
FROM `phase33_chapters`
ON DUPLICATE KEY UPDATE
  `flow_type` = VALUES(`flow_type`),
  `mode` = VALUES(`mode`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `map_policy_json` = VALUES(`map_policy_json`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

UPDATE `story_chapters` `sc`
JOIN `phase33_chapters` `c`
  ON `c`.`chapter_order` = `sc`.`chapter_order`
JOIN `experience_flows` `f`
  ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0
SET
  `sc`.`experience_flow_id` = `f`.`id`,
  `sc`.`override_policy_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'inheritDefaultFlow', TRUE,
    'appendStorySpecificRewards', TRUE,
    'storyOverrideFlowCode', `c`.`flow_code`,
    'anchorCode', `c`.`anchor_code`,
    'overrideModes', JSON_ARRAY('inherit', 'disable', 'replace', 'append')
  ),
  `sc`.`story_mode_config_json` = JSON_OBJECT(
    'schemaVersion', 1,
    'hideUnrelatedContent', TRUE,
    'currentRouteStyle', 'copper_flame',
    'inactiveRouteStyle', 'muted_ink',
    'exitResetsSessionProgress', TRUE,
    'preservePermanentEvents', TRUE,
    'branchRecommendationStrategy', JSON_OBJECT('source', 'nearby_or_manual', 'insertPosition', 'between_chapters', 'skippable', TRUE)
  )
WHERE `sc`.`storyline_id` = @storyline_east_west_id
  AND `sc`.`deleted` = 0;

SET @tpl_fullscreen_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'presentation.fullscreen_media' AND `deleted` = 0 LIMIT 1);
SET @tpl_overlay_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'presentation.map_overlay' AND `deleted` = 0 LIMIT 1);
SET @tpl_pickup_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'trigger_effect.grant_collectible' AND `deleted` = 0 LIMIT 1);
SET @tpl_quiz_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'task_gameplay.quiz' AND `deleted` = 0 LIMIT 1);
SET @tpl_reward_id = (SELECT `id` FROM `experience_templates` WHERE `code` = 'reward_presentation.fullscreen_unlock' AND `deleted` = 0 LIMIT 1);

DELETE FROM `experience_flow_steps`
WHERE `flow_id` IN (
  SELECT `id` FROM `experience_flows`
  WHERE `code` IN (
    'story_east_west_ch01_flow',
    'story_east_west_ch02_flow',
    'story_east_west_ch03_flow',
    'story_east_west_ch04_flow',
    'story_east_west_ch05_flow'
  )
);

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
)
SELECT `f`.`id`, CONCAT(`c`.`chapter_code`, '_arrival_media'), 'fullscreen_media', @tpl_fullscreen_id,
  '抵達播放主線劇情', 'Arrival story media', '抵達播放主線劇情', '',
  CONCAT('到達 ', `c`.`location_zht`, ' 後播放章節核心劇情、旁白與沉浸式音效。'), CONCAT('Arrival media for ', `c`.`title_en`, '.'),
  CONCAT('到達 ', `c`.`location_zht`, ' 後播放章節核心劇情、旁白與沉浸式音效。'), '',
  'proximity', JSON_OBJECT('schemaVersion', 1, 'radiusMeters', IF(`c`.`chapter_order` = 1, 50, 30), 'oncePerSession', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'requiresStoryMode', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'fullscreen_media', 'audioAssetId', `c`.`audio_asset_id`, 'fallbackText', `c`.`summary_zht`),
  `c`.`hero_asset_id`, NULL, 'core', 1, 'arrival_intro_media', 'published', 10
FROM `phase33_chapters` `c` JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
)
SELECT `f`.`id`, CONCAT(`c`.`chapter_code`, '_mainline_interaction'), 'mainline_interaction', @tpl_overlay_id,
  '主線必做互動', 'Mainline interaction', '主線必做互動', '',
  CONCAT('完成本章主線互動：', JSON_UNQUOTE(JSON_EXTRACT(`c`.`mainline_names_json`, '$[0]')), ' 等章節目標。'), CONCAT('Mainline interaction for ', `c`.`title_en`, '.'),
  CONCAT('完成本章主線互動：', JSON_UNQUOTE(JSON_EXTRACT(`c`.`mainline_names_json`, '$[0]')), ' 等章節目標。'), '',
  'after_step', JSON_OBJECT('schemaVersion', 1, 'afterStepCode', CONCAT(`c`.`chapter_code`, '_arrival_media')),
  JSON_OBJECT('schemaVersion', 1, 'afterStepCode', CONCAT(`c`.`chapter_code`, '_arrival_media')),
  JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_mainline_overlays', 'requiredNames', `c`.`mainline_names_json`, 'completionMode', 'all_required_targets'),
  333003, NULL, 'core', 1, 'story_mainline', 'published', 20
FROM `phase33_chapters` `c` JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
)
SELECT `f`.`id`, CONCAT(`c`.`chapter_code`, '_side_pickups'), 'side_pickup_bundle', @tpl_pickup_id,
  '支線探索拾取', 'Side pickups', '支線探索拾取', '',
  CONCAT('生成本章拾取物與線索，拾取後寫入背包與探索事件。'), CONCAT('Side pickups for ', `c`.`title_en`, '.'),
  CONCAT('生成本章拾取物與線索，拾取後寫入背包與探索事件。'), '',
  'after_step', JSON_OBJECT('schemaVersion', 1, 'afterStepCode', CONCAT(`c`.`chapter_code`, '_mainline_interaction')),
  JSON_OBJECT('schemaVersion', 1, 'afterStepCode', CONCAT(`c`.`chapter_code`, '_arrival_media')),
  JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'spawn_side_pickups', 'pickupCodes', `c`.`pickup_codes_json`, 'defaultVisual', 'red_dot_or_lottie_shimmer'),
  333004, NULL, 'large', 0, 'story_side_pickups', 'published', 30
FROM `phase33_chapters` `c` JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
)
SELECT `f`.`id`, CONCAT(`c`.`chapter_code`, '_hidden_challenge'), 'hidden_challenge', @tpl_quiz_id,
  `c`.`hidden_challenge_zht`, 'Hidden challenge', `c`.`hidden_challenge_zht`, '',
  `c`.`hidden_summary_zht`, `c`.`hidden_summary_zht`, `c`.`hidden_summary_zht`, '',
  'compound', JSON_OBJECT('schemaVersion', 1, 'requiresAllPickups', TRUE, 'requiresDwellOrSpecialAction', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'pickupCodes', `c`.`pickup_codes_json`, 'challengeName', `c`.`hidden_challenge_zht`),
  JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'start_hidden_challenge', 'challengeName', `c`.`hidden_challenge_zht`, 'gameplay', IF(`c`.`chapter_order` IN (1, 5), 'quiz', IF(`c`.`chapter_order` = 2, 'map_puzzle', IF(`c`.`chapter_order` = 4, 'fortress_defense', 'panorama_marking')))),
  NULL, NULL, 'core', 0, 'story_hidden_challenge', 'published', 40
FROM `phase33_chapters` `c` JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0;

INSERT INTO `experience_flow_steps` (
  `flow_id`, `step_code`, `step_type`, `template_id`,
  `step_name_zh`, `step_name_en`, `step_name_zht`, `step_name_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `trigger_type`, `trigger_config_json`, `condition_config_json`, `effect_config_json`,
  `media_asset_id`, `reward_rule_ids_json`, `exploration_weight_level`, `required_for_completion`, `inherit_key`, `status`, `sort_order`
)
SELECT `f`.`id`, CONCAT(`c`.`chapter_code`, '_reward_titles'), 'reward_title_presentation', @tpl_reward_id,
  '章節獎勵與稱號演出', 'Chapter reward presentation', '章節獎勵與稱號演出', '',
  CONCAT('發放 ', `c`.`base_title_zht`, '、', `c`.`full_title_zht`, '、', `c`.`hidden_title_zht`, ' 與 ', `c`.`fragment_zht`, '。'), CONCAT('Reward presentation for ', `c`.`title_en`, '.'),
  CONCAT('發放 ', `c`.`base_title_zht`, '、', `c`.`full_title_zht`, '、', `c`.`hidden_title_zht`, ' 與 ', `c`.`fragment_zht`, '。'), '',
  'chapter_complete', JSON_OBJECT('schemaVersion', 1, 'afterStepCode', CONCAT(`c`.`chapter_code`, '_hidden_challenge')),
  JSON_OBJECT('schemaVersion', 1, 'chapterOrder', `c`.`chapter_order`),
  JSON_OBJECT('schemaVersion', 1, 'effectPreset', 'grant_rewards_titles', 'baseTitle', `c`.`base_title_zht`, 'fullCollectionTitle', `c`.`full_title_zht`, 'hiddenTitle', `c`.`hidden_title_zht`, 'fragmentReward', `c`.`fragment_zht`),
  333005, NULL, 'core', 1, 'story_reward_titles', 'published', 50
FROM `phase33_chapters` `c` JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0;

INSERT INTO `experience_bindings` (
  `owner_type`, `owner_id`, `owner_code`, `binding_role`, `flow_id`, `priority`, `inherit_policy`, `status`, `sort_order`
)
SELECT
  'story_chapter',
  `sc`.`id`,
  `c`.`chapter_code`,
  'story_override_flow',
  `f`.`id`,
  30,
  'override',
  'published',
  `c`.`chapter_order` * 10
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc`
  ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
JOIN `experience_flows` `f`
  ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `priority` = VALUES(`priority`),
  `inherit_policy` = VALUES(`inherit_policy`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `story_content_blocks` (
  `code`, `block_type`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `body_zh`, `body_en`, `body_zht`, `body_pt`,
  `primary_asset_id`, `style_preset`, `display_mode`, `visibility_json`, `config_json`,
  `status`, `sort_order`, `published_at`, `deleted`
)
SELECT CONCAT(`chapter_code`, '_script'), 'rich_text',
  `title_zht`, `title_en`, `title_zht`, '',
  `summary_zht`, `summary_zht`, `summary_zht`, '',
  `detail_zht`, `detail_zht`, `detail_zht`, '',
  NULL, 'historical-scroll', 'article', JSON_OBJECT('schemaVersion', 1, 'visibleInStoryMode', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'historicalBasis', '史實依據已記錄於內容包 historical-checklist.md', 'literaryDramatization', '文學演繹已記錄於 story-script.md'),
  'published', 33000 + `chapter_order` * 10 + 1, NOW(), 0
FROM `phase33_chapters`
ON DUPLICATE KEY UPDATE
  `block_type` = VALUES(`block_type`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `body_zh` = VALUES(`body_zh`),
  `body_en` = VALUES(`body_en`),
  `body_zht` = VALUES(`body_zht`),
  `primary_asset_id` = VALUES(`primary_asset_id`),
  `style_preset` = VALUES(`style_preset`),
  `display_mode` = VALUES(`display_mode`),
  `visibility_json` = VALUES(`visibility_json`),
  `config_json` = VALUES(`config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

INSERT INTO `story_content_blocks` (
  `code`, `block_type`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `body_zh`, `body_en`, `body_zht`, `body_pt`,
  `primary_asset_id`, `style_preset`, `display_mode`, `visibility_json`, `config_json`,
  `status`, `sort_order`, `published_at`, `deleted`
)
SELECT CONCAT(`chapter_code`, '_narration_audio'), 'audio',
  CONCAT(`title_zht`, '旁白'), CONCAT(`title_en`, ' narration'), CONCAT(`title_zht`, '旁白'), '',
  '播放本章粵語繁體旁白與環境音，作為故事內容消費端的音頻積木。', 'Chapter narration audio.', '播放本章粵語繁體旁白與環境音，作為故事內容消費端的音頻積木。', '',
  `summary_zht`, `summary_zht`, `summary_zht`, '',
  `audio_asset_id`, 'ambient-audio', 'inline_audio', JSON_OBJECT('schemaVersion', 1, 'visibleInStoryMode', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'assetRole', 'chapter.narration', 'language', 'zh-Hant', 'scriptSource', 'phase33-story-script', 'autoplay', FALSE),
  'published', 33000 + `chapter_order` * 10 + 3, NOW(), 0
FROM `phase33_chapters`
ON DUPLICATE KEY UPDATE
  `block_type` = VALUES(`block_type`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `body_zh` = VALUES(`body_zh`),
  `body_en` = VALUES(`body_en`),
  `body_zht` = VALUES(`body_zht`),
  `primary_asset_id` = VALUES(`primary_asset_id`),
  `style_preset` = VALUES(`style_preset`),
  `display_mode` = VALUES(`display_mode`),
  `visibility_json` = VALUES(`visibility_json`),
  `config_json` = VALUES(`config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

INSERT INTO `story_content_blocks` (
  `code`, `block_type`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `body_zh`, `body_en`, `body_zht`, `body_pt`,
  `primary_asset_id`, `style_preset`, `display_mode`, `visibility_json`, `config_json`,
  `status`, `sort_order`, `published_at`, `deleted`
)
SELECT CONCAT(`chapter_code`, '_hero_media'), 'image',
  CONCAT(`location_zht`, '章節主視覺'), CONCAT(`title_en`, ' hero'), CONCAT(`location_zht`, '章節主視覺'), '',
  `summary_zht`, `summary_zht`, `summary_zht`, '',
  `summary_zht`, `summary_zht`, `summary_zht`, '',
  `hero_asset_id`, 'cinematic-card', 'media_card', JSON_OBJECT('schemaVersion', 1, 'visibleInStoryMode', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'assetRole', 'chapter.hero', 'fallbackAssetId', 333001),
  'published', 33000 + `chapter_order` * 10 + 2, NOW(), 0
FROM `phase33_chapters`
ON DUPLICATE KEY UPDATE
  `block_type` = VALUES(`block_type`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `body_zh` = VALUES(`body_zh`),
  `body_en` = VALUES(`body_en`),
  `body_zht` = VALUES(`body_zht`),
  `primary_asset_id` = VALUES(`primary_asset_id`),
  `style_preset` = VALUES(`style_preset`),
  `display_mode` = VALUES(`display_mode`),
  `visibility_json` = VALUES(`visibility_json`),
  `config_json` = VALUES(`config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

INSERT INTO `story_content_blocks` (
  `code`, `block_type`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `summary_zh`, `summary_en`, `summary_zht`, `summary_pt`,
  `body_zh`, `body_en`, `body_zht`, `body_pt`,
  `primary_asset_id`, `style_preset`, `display_mode`, `visibility_json`, `config_json`,
  `status`, `sort_order`, `published_at`, `deleted`
) VALUES (
  'ch05_final_mirror_lottie', 'lottie',
  '完整銅鏡合成動畫', 'Final mirror synthesis', '完整銅鏡合成動畫', '',
  '終章通關時播放 Lottie 動畫，失效時回退到終章海報與混剪影片。', 'Final Lottie with fallback.', '終章通關時播放 Lottie 動畫，失效時回退到終章海報與混剪影片。', '',
  '完整濠江戰火銅鏡合成，象徵戰火記憶閉合與文明共生。', 'Final mirror synthesis.', '完整濠江戰火銅鏡合成，象徵戰火記憶閉合與文明共生。', '',
  333006, 'fullscreen-finale', 'lottie_player', JSON_OBJECT('schemaVersion', 1, 'visibleInStoryMode', TRUE),
  JSON_OBJECT('schemaVersion', 1, 'assetKind', 'lottie', 'defaultLoop', TRUE, 'fallbackAssetId', 333007, 'videoFallbackAssetId', 333008),
  'published', 33099, NOW(), 0
) ON DUPLICATE KEY UPDATE
  `block_type` = VALUES(`block_type`),
  `title_zh` = VALUES(`title_zh`),
  `title_en` = VALUES(`title_en`),
  `title_zht` = VALUES(`title_zht`),
  `summary_zh` = VALUES(`summary_zh`),
  `summary_en` = VALUES(`summary_en`),
  `summary_zht` = VALUES(`summary_zht`),
  `body_zh` = VALUES(`body_zh`),
  `body_en` = VALUES(`body_en`),
  `body_zht` = VALUES(`body_zht`),
  `primary_asset_id` = VALUES(`primary_asset_id`),
  `style_preset` = VALUES(`style_preset`),
  `display_mode` = VALUES(`display_mode`),
  `visibility_json` = VALUES(`visibility_json`),
  `config_json` = VALUES(`config_json`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `published_at` = VALUES(`published_at`),
  `deleted` = 0;

DELETE `l`
FROM `story_chapter_block_links` `l`
JOIN `story_content_blocks` `b`
  ON `b`.`id` = `l`.`block_id`
WHERE `b`.`code` IN (
  'ch01_mirror_sea_clash_script',
  'ch01_mirror_sea_clash_hero_media',
  'ch01_mirror_sea_clash_narration_audio',
  'ch02_south_bay_boundary_script',
  'ch02_south_bay_boundary_hero_media',
  'ch02_south_bay_boundary_narration_audio',
  'ch03_hill_watch_script',
  'ch03_hill_watch_hero_media',
  'ch03_hill_watch_narration_audio',
  'ch04_fortress_fire_script',
  'ch04_fortress_fire_hero_media',
  'ch04_fortress_fire_narration_audio',
  'ch05_coexistence_finale_script',
  'ch05_coexistence_finale_hero_media',
  'ch05_coexistence_finale_narration_audio',
  'ch05_final_mirror_lottie'
);

INSERT INTO `story_chapter_block_links` (
  `chapter_id`, `block_id`, `override_title_json`, `override_summary_json`, `override_body_json`,
  `display_condition_json`, `override_config_json`, `status`, `sort_order`, `deleted`
)
SELECT `sc`.`id`, `b`.`id`, NULL, NULL, NULL,
  JSON_OBJECT('schemaVersion', 1, 'condition', 'always'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'),
  'published',
  10,
  0
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
JOIN `story_content_blocks` `b` ON `b`.`code` = CONCAT(`c`.`chapter_code`, '_script') AND `b`.`deleted` = 0;

INSERT INTO `story_chapter_block_links` (
  `chapter_id`, `block_id`, `override_title_json`, `override_summary_json`, `override_body_json`,
  `display_condition_json`, `override_config_json`, `status`, `sort_order`, `deleted`
)
SELECT `sc`.`id`, `b`.`id`, NULL, NULL, NULL,
  JSON_OBJECT('schemaVersion', 1, 'condition', 'after_intro'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'),
  'published',
  20,
  0
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
JOIN `story_content_blocks` `b` ON `b`.`code` = CONCAT(`c`.`chapter_code`, '_hero_media') AND `b`.`deleted` = 0;

INSERT INTO `story_chapter_block_links` (
  `chapter_id`, `block_id`, `override_title_json`, `override_summary_json`, `override_body_json`,
  `display_condition_json`, `override_config_json`, `status`, `sort_order`, `deleted`
)
SELECT `sc`.`id`, `b`.`id`, NULL, NULL, NULL,
  JSON_OBJECT('schemaVersion', 1, 'condition', 'after_intro'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed', 'assetRole', 'chapter.narration'),
  'published',
  30,
  0
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
JOIN `story_content_blocks` `b` ON `b`.`code` = CONCAT(`c`.`chapter_code`, '_narration_audio') AND `b`.`deleted` = 0;

INSERT INTO `story_chapter_block_links` (
  `chapter_id`, `block_id`, `override_title_json`, `override_summary_json`, `override_body_json`,
  `display_condition_json`, `override_config_json`, `status`, `sort_order`, `deleted`
)
SELECT `sc`.`id`, `b`.`id`, NULL, NULL, NULL,
  JSON_OBJECT('schemaVersion', 1, 'condition', 'chapter_finale'),
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'),
  'published',
  40,
  0
FROM `story_chapters` `sc`
JOIN `story_content_blocks` `b` ON `b`.`code` = 'ch05_final_mirror_lottie' AND `b`.`deleted` = 0
WHERE `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = 5 AND `sc`.`deleted` = 0;

DROP TEMPORARY TABLE IF EXISTS `phase33_pickups`;
CREATE TEMPORARY TABLE `phase33_pickups` (
  `chapter_order` INT NOT NULL,
  `pickup_code` VARCHAR(96) NOT NULL,
  `pickup_name` VARCHAR(128) NOT NULL,
  `weight_level` VARCHAR(32) NOT NULL,
  `weight_value` INT NOT NULL,
  `sort_order` INT NOT NULL,
  PRIMARY KEY (`chapter_order`, `pickup_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `phase33_pickups` VALUES
  (1, 'ming_coastal_token', '明朝海防銅令牌', 'medium', 3, 1),
  (1, 'fisher_net_fragment', '濠江漁民禦敵漁網殘片', 'large', 5, 2),
  (1, 'tax_contract_page', '葡人通商納稅契約殘頁', 'large', 5, 3),
  (2, 'boundary_rubbing', '明葡邊界界碑拓片', 'medium', 3, 1),
  (2, 'fort_design_page', '南灣防線堡壘設計圖殘頁', 'large', 5, 2),
  (2, 'garrison_flask', '古井駐軍水壺殘件', 'large', 5, 3),
  (3, 'patrol_record', '瞭望哨巡邏記錄殘頁', 'medium', 3, 1),
  (3, 'mission_military_letter', '遠東傳教與軍防密信', 'large', 5, 2),
  (3, 'hill_defense_sketch', '崗頂山城防線手繪圖', 'large', 5, 3),
  (4, 'cannonball_fragment', '荷澳戰役炮彈殘片', 'medium', 3, 1),
  (4, 'defender_diary', '守軍作戰日記殘頁', 'large', 5, 2),
  (4, 'dutch_fleet_map', '荷蘭艦隊作戰地圖', 'large', 5, 3),
  (4, 'guardian_oath', '中西守軍聯名誓詞', 'large', 5, 4),
  (5, 'coexistence_declaration', '濠江文明共生宣言', 'medium', 3, 1),
  (5, 'time_witness_pass', '時空見證者通行證', 'large', 5, 2),
  (5, 'complete_copper_mirror', '完整濠江戰火銅鏡', 'core', 8, 3);

INSERT INTO `reward_presentations` (
  `code`, `name_zh`, `name_zht`, `presentation_type`, `first_time_only`, `skippable`,
  `minimum_display_ms`, `interrupt_policy`, `queue_policy`, `priority_weight`,
  `cover_asset_id`, `voice_over_asset_id`, `sfx_asset_id`, `summary_text`, `config_json`, `status`
) VALUES (
  'presentation_east_west_story_reward_unlock',
  '東西方文明故事獎勵演出',
  '東西方文明故事獎勵演出',
  'fullscreen_video',
  1,
  1,
  3600,
  'queue_after_current',
  'enqueue',
  95,
  333005,
  333014,
  333009,
  '章節通關、全收集、隱藏挑戰與終章合成時使用的全屏獎勵演出。',
  JSON_OBJECT('schemaVersion', 1, 'fallbackMode', 'popup_card', 'lottieAssetId', 333005, 'sfxAssetId', 333009),
  'published'
) ON DUPLICATE KEY UPDATE
  `name_zh` = VALUES(`name_zh`),
  `name_zht` = VALUES(`name_zht`),
  `presentation_type` = VALUES(`presentation_type`),
  `minimum_display_ms` = VALUES(`minimum_display_ms`),
  `cover_asset_id` = VALUES(`cover_asset_id`),
  `voice_over_asset_id` = VALUES(`voice_over_asset_id`),
  `sfx_asset_id` = VALUES(`sfx_asset_id`),
  `summary_text` = VALUES(`summary_text`),
  `config_json` = VALUES(`config_json`),
  `status` = VALUES(`status`),
  `deleted` = 0;

SET @reward_presentation_id = (SELECT `id` FROM `reward_presentations` WHERE `code` = 'presentation_east_west_story_reward_unlock' LIMIT 1);

DROP TEMPORARY TABLE IF EXISTS `phase33_rewards`;
CREATE TEMPORARY TABLE `phase33_rewards` (
  `chapter_order` INT NOT NULL,
  `reward_code` VARCHAR(96) NOT NULL,
  `reward_type` VARCHAR(32) NOT NULL,
  `rarity` VARCHAR(32) NOT NULL,
  `reward_name` VARCHAR(128) NOT NULL,
  `role_code` VARCHAR(48) NOT NULL,
  `asset_id` BIGINT NULL,
  `sort_order` INT NOT NULL,
  PRIMARY KEY (`reward_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `phase33_rewards` VALUES
  (1, 'title_east_west_first_harbour_witness', 'title', 'rare', '濠江初見者', 'chapter_completion', 333040, 33101),
  (1, 'title_east_west_mirror_sea_explorer', 'title', 'epic', '鏡海探索者', 'full_collection', 333041, 33102),
  (1, 'title_east_west_coast_guardian', 'title', 'legendary', '海岸守護人', 'hidden_challenge', 333042, 33103),
  (1, 'fragment_east_west_ama_battle_mirror', 'city_fragment', 'epic', '媽閣戰火銅鏡碎片', 'mirror_fragment', 333023, 33104),
  (2, 'title_east_west_south_bay_wayfarer', 'title', 'rare', '南灣行路人', 'chapter_completion', 333043, 33201),
  (2, 'title_east_west_boundary_recorder', 'title', 'epic', '界線記錄者', 'full_collection', 333044, 33202),
  (2, 'title_east_west_silent_frontier_soldier', 'title', 'legendary', '無硝煙戰士', 'hidden_challenge', 333045, 33203),
  (2, 'fragment_east_west_south_bay_mirror', 'city_fragment', 'epic', '南灣界線銅鏡碎片', 'mirror_fragment', 333027, 33204),
  (3, 'title_east_west_hill_climber', 'title', 'rare', '山城登頂者', 'chapter_completion', 333046, 33301),
  (3, 'title_east_west_hidden_sentry_finder', 'title', 'epic', '暗哨發現者', 'full_collection', 333047, 33302),
  (3, 'title_east_west_panorama_witness', 'title', 'legendary', '全景見證人', 'hidden_challenge', 333048, 33303),
  (3, 'fragment_east_west_hill_watch_mirror', 'city_fragment', 'epic', '山城瞭望銅鏡碎片', 'mirror_fragment', 333031, 33304),
  (4, 'title_east_west_fortress_visitor', 'title', 'rare', '炮台登臨者', 'chapter_completion', 333049, 33401),
  (4, 'title_east_west_battle_restorer', 'title', 'epic', '戰役還原者', 'full_collection', 333050, 33402),
  (4, 'title_east_west_harbour_guardian_deity', 'title', 'legendary', '濠江守護神', 'hidden_challenge', 333051, 33403),
  (4, 'fragment_east_west_fortress_fire_mirror', 'city_fragment', 'epic', '炮台硝煙銅鏡碎片', 'mirror_fragment', 333036, 33404),
  (5, 'title_east_west_harbour_witness_final', 'title', 'legendary', '濠江見證者', 'chapter_completion', 333052, 33501),
  (5, 'title_east_west_history_restoration_master', 'title', 'legendary', '歷史還原大師', 'full_collection', 333053, 33502),
  (5, 'title_east_west_harbour_history_grandmaster', 'title', 'mythic', '濠江通史掌門人', 'hidden_challenge', 333054, 33503),
  (5, 'fragment_east_west_complete_copper_mirror', 'city_fragment', 'mythic', '完整濠江戰火銅鏡', 'mirror_fragment', 333039, 33504);

INSERT INTO `game_rewards` (
  `code`, `legacy_source_type`, `legacy_source_id`, `reward_type`, `rarity`, `stackable`, `max_owned`, `can_equip`, `can_consume`,
  `name_zh`, `name_en`, `name_zht`, `name_pt`,
  `subtitle_zh`, `subtitle_en`, `subtitle_zht`, `subtitle_pt`,
  `description_zh`, `description_en`, `description_zht`, `description_pt`,
  `highlight_zh`, `highlight_en`, `highlight_zht`, `highlight_pt`,
  `cover_asset_id`, `icon_asset_id`, `animation_asset_id`, `reward_config_json`, `presentation_id`,
  `status`, `sort_order`, `publish_start_at`, `publish_end_at`
)
SELECT
  `r`.`reward_code`,
  '',
  NULL,
  `r`.`reward_type`,
  `r`.`rarity`,
  IF(`r`.`reward_type` = 'city_fragment', 1, 0),
  IF(`r`.`reward_type` = 'city_fragment', 99, 1),
  IF(`r`.`reward_type` = 'title', 1, 0),
  0,
  `r`.`reward_name`,
  `r`.`reward_name`,
  `r`.`reward_name`,
  '',
  '東西方文明的戰火與共生故事線獎勵',
  'East-West flagship storyline reward',
  '東西方文明的戰火與共生故事線獎勵',
  '',
  CONCAT('由故事線第 ', `r`.`chapter_order`, ' 章的 ', `r`.`role_code`, ' 條件發放。'),
  CONCAT('Granted by chapter ', `r`.`chapter_order`, ' ', `r`.`role_code`, ' condition.'),
  CONCAT('由故事線第 ', `r`.`chapter_order`, ' 章的 ', `r`.`role_code`, ' 條件發放。'),
  '',
  '可在個人頁與故事結算中展示。',
  'Visible in profile and story recap.',
  '可在個人頁與故事結算中展示。',
  '',
  `r`.`asset_id`,
  `r`.`asset_id`,
  IF(`r`.`chapter_order` = 5 AND `r`.`role_code` IN ('hidden_challenge', 'mirror_fragment'), 333006, 333005),
  JSON_OBJECT('schemaVersion', 1, 'storylineCode', 'east_west_war_and_coexistence', 'chapterOrder', `r`.`chapter_order`, 'roleCode', `r`.`role_code`),
  @reward_presentation_id,
  'published',
  `r`.`sort_order`,
  '2026-01-01 00:00:00',
  '2028-12-31 23:59:59'
FROM `phase33_rewards` `r`
ON DUPLICATE KEY UPDATE
  `reward_type` = VALUES(`reward_type`),
  `rarity` = VALUES(`rarity`),
  `stackable` = VALUES(`stackable`),
  `max_owned` = VALUES(`max_owned`),
  `can_equip` = VALUES(`can_equip`),
  `name_zh` = VALUES(`name_zh`),
  `name_en` = VALUES(`name_en`),
  `name_zht` = VALUES(`name_zht`),
  `subtitle_zh` = VALUES(`subtitle_zh`),
  `subtitle_zht` = VALUES(`subtitle_zht`),
  `description_zh` = VALUES(`description_zh`),
  `description_en` = VALUES(`description_en`),
  `description_zht` = VALUES(`description_zht`),
  `highlight_zh` = VALUES(`highlight_zh`),
  `highlight_zht` = VALUES(`highlight_zht`),
  `cover_asset_id` = VALUES(`cover_asset_id`),
  `icon_asset_id` = VALUES(`icon_asset_id`),
  `animation_asset_id` = VALUES(`animation_asset_id`),
  `reward_config_json` = VALUES(`reward_config_json`),
  `presentation_id` = VALUES(`presentation_id`),
  `status` = VALUES(`status`),
  `sort_order` = VALUES(`sort_order`),
  `publish_start_at` = VALUES(`publish_start_at`),
  `publish_end_at` = VALUES(`publish_end_at`),
  `deleted` = 0;

INSERT INTO `reward_rules` (
  `code`, `rule_type`, `status`, `name_zh`, `name_zht`, `summary_text`, `advanced_config_json`
)
SELECT
  CONCAT('rule_east_west_ch', LPAD(`chapter_order`, 2, '0'), '_rewards'),
  'grant_rule',
  'published',
  CONCAT(`title_zht`, '獎勵發放規則'),
  CONCAT(`title_zht`, '獎勵發放規則'),
  CONCAT('第 ', `chapter_order`, ' 章主線、全收集、隱藏挑戰與銅鏡碎片發放規則。'),
  JSON_OBJECT('schemaVersion', 1, 'storylineCode', 'east_west_war_and_coexistence', 'chapterCode', `chapter_code`, 'source', 'phase33-seed')
FROM `phase33_chapters`
UNION ALL
SELECT
  'rule_east_west_final_route_master',
  'grant_rule',
  'published',
  '濠江通史大師終極挑戰發放規則',
  '濠江通史大師終極挑戰發放規則',
  '全線所有拾取物與章節探索元素完成後，解鎖終極稱號、完整銅鏡與終極內容。',
  JSON_OBJECT('schemaVersion', 1, 'storylineCode', 'east_west_war_and_coexistence', 'challengeName', '濠江通史大師', 'source', 'phase33-seed')
ON DUPLICATE KEY UPDATE
  `rule_type` = VALUES(`rule_type`),
  `status` = VALUES(`status`),
  `name_zh` = VALUES(`name_zh`),
  `name_zht` = VALUES(`name_zht`),
  `summary_text` = VALUES(`summary_text`),
  `advanced_config_json` = VALUES(`advanced_config_json`),
  `deleted` = 0;

INSERT INTO `reward_rule_bindings` (
  `rule_id`, `owner_domain`, `owner_id`, `owner_code`, `binding_role`, `sort_order`
)
SELECT
  `rr`.`id`,
  'game_reward',
  `gr`.`id`,
  `gr`.`code`,
  `r`.`role_code`,
  `r`.`sort_order`
FROM `phase33_rewards` `r`
JOIN `game_rewards` `gr` ON `gr`.`code` = `r`.`reward_code` AND `gr`.`deleted` = 0
JOIN `reward_rules` `rr` ON `rr`.`code` = CONCAT('rule_east_west_ch', LPAD(`r`.`chapter_order`, 2, '0'), '_rewards') AND `rr`.`deleted` = 0
UNION ALL
SELECT
  `rr`.`id`,
  'storyline',
  @storyline_east_west_id,
  'east_west_war_and_coexistence',
  'final_route_challenge',
  33999
FROM `reward_rules` `rr`
WHERE `rr`.`code` = 'rule_east_west_final_route_master' AND `rr`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `owner_code` = VALUES(`owner_code`),
  `binding_role` = VALUES(`binding_role`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

DELETE FROM `exploration_elements`
WHERE `storyline_id` = @storyline_east_west_id
  AND (
    `element_code` LIKE 'ch01_mirror_sea_clash_%'
    OR `element_code` LIKE 'ch02_south_bay_boundary_%'
    OR `element_code` LIKE 'ch03_hill_watch_%'
    OR `element_code` LIKE 'ch04_fortress_fire_%'
    OR `element_code` LIKE 'ch05_coexistence_finale_%'
    OR `element_code` = 'east_west_route_grandmaster_final'
  );

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `poi_id`, `indoor_building_id`, `indoor_floor_id`,
  `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
)
SELECT CONCAT(`c`.`chapter_code`, '_chapter_complete'), 'story_chapter_complete', 'story_chapter', `sc`.`id`, `c`.`chapter_code`,
  @city_macau_id, @sub_map_peninsula_id, `c`.`anchor_id`, NULL, NULL, @storyline_east_west_id, `sc`.`id`,
  CONCAT('完成', `c`.`title_zht`), CONCAT('Complete ', `c`.`title_en`), CONCAT('完成', `c`.`title_zht`), '',
  'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed', 'elementRole', 'chapter_complete'), 'published', `c`.`chapter_order` * 100 + 10
FROM `phase33_chapters` `c` JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `poi_id`, `indoor_building_id`, `indoor_floor_id`,
  `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
)
SELECT CONCAT(`c`.`chapter_code`, '_mainline_complete'), 'story_mainline_interaction_complete', 'story_chapter', `sc`.`id`, CONCAT(`c`.`chapter_code`, '_mainline'),
  @city_macau_id, @sub_map_peninsula_id, `c`.`anchor_id`, NULL, NULL, @storyline_east_west_id, `sc`.`id`,
  CONCAT('完成', `c`.`location_zht`, '主線互動'), CONCAT('Complete ', `c`.`title_en`, ' mainline'), CONCAT('完成', `c`.`location_zht`, '主線互動'), '',
  'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'mainlineNames', `c`.`mainline_names_json`), 'published', `c`.`chapter_order` * 100 + 20
FROM `phase33_chapters` `c` JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `poi_id`, `indoor_building_id`, `indoor_floor_id`,
  `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
)
SELECT CONCAT(`c`.`chapter_code`, '_hidden_challenge_complete'), 'story_hidden_challenge_complete', 'story_chapter', `sc`.`id`, `c`.`hidden_challenge_zht`,
  @city_macau_id, @sub_map_peninsula_id, `c`.`anchor_id`, NULL, NULL, @storyline_east_west_id, `sc`.`id`,
  CONCAT('完成', `c`.`hidden_challenge_zht`, '隱藏挑戰'), CONCAT('Complete ', `c`.`hidden_challenge_zht`), CONCAT('完成', `c`.`hidden_challenge_zht`, '隱藏挑戰'), '',
  'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'challengeName', `c`.`hidden_challenge_zht`), 'published', `c`.`chapter_order` * 100 + 30
FROM `phase33_chapters` `c` JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `poi_id`, `indoor_building_id`, `indoor_floor_id`,
  `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
)
SELECT CONCAT(`c`.`chapter_code`, '_pickup_', `p`.`pickup_code`), 'story_side_pickup', 'story_chapter', `sc`.`id`, `p`.`pickup_code`,
  @city_macau_id, @sub_map_peninsula_id, `c`.`anchor_id`, NULL, NULL, @storyline_east_west_id, `sc`.`id`,
  CONCAT('拾取', `p`.`pickup_name`), CONCAT('Pick up ', `p`.`pickup_code`), CONCAT('拾取', `p`.`pickup_name`), '',
  `p`.`weight_level`, `p`.`weight_value`, 1, JSON_OBJECT('schemaVersion', 1, 'pickupCode', `p`.`pickup_code`, 'pickupName', `p`.`pickup_name`), 'published', `c`.`chapter_order` * 100 + 40 + `p`.`sort_order`
FROM `phase33_pickups` `p`
JOIN `phase33_chapters` `c` ON `c`.`chapter_order` = `p`.`chapter_order`
JOIN `story_chapters` `sc` ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
;

INSERT INTO `exploration_elements` (
  `element_code`, `element_type`, `owner_type`, `owner_id`, `owner_code`,
  `city_id`, `sub_map_id`, `poi_id`, `indoor_building_id`, `indoor_floor_id`,
  `storyline_id`, `story_chapter_id`,
  `title_zh`, `title_en`, `title_zht`, `title_pt`,
  `weight_level`, `weight_value`, `include_in_exploration`, `metadata_json`, `status`, `sort_order`
)
SELECT 'east_west_route_grandmaster_final', 'storyline_final_challenge_complete', 'storyline', @storyline_east_west_id, '濠江通史大師',
  @city_macau_id, @sub_map_peninsula_id, NULL, NULL, NULL, @storyline_east_west_id, NULL,
  '完成濠江通史大師終極挑戰', 'Complete Grandmaster of Macau History', '完成濠江通史大師終極挑戰', '',
  'core', 8, 1, JSON_OBJECT('schemaVersion', 1, 'challengeName', '濠江通史大師', 'completionMode', 'all_storyline_elements'), 'published', 33999;

UPDATE `story_material_package_items` `i`
JOIN `phase33_chapters` `c`
  ON `i`.`chapter_code` = `c`.`chapter_code`
JOIN `story_chapters` `sc`
  ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
SET
  `i`.`target_id` = `sc`.`id`,
  `i`.`target_code` = `c`.`chapter_code`,
  `i`.`status` = 'planned'
WHERE `i`.`package_id` = @package_id
  AND `i`.`deleted` = 0
  AND `i`.`target_type` = 'story_chapter';

UPDATE `story_material_package_items`
SET
  `target_id` = @storyline_east_west_id,
  `target_code` = 'east_west_war_and_coexistence',
  `status` = 'planned'
WHERE `package_id` = @package_id
  AND `deleted` = 0
  AND (`chapter_code` IN ('storyline', 'global') OR `target_type` = 'storyline');

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
SELECT 'storyline', @storyline_east_west_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'), 10
UNION ALL
SELECT 'storyline', @storyline_east_west_id, 'sub_map_binding', 'sub_map', @sub_map_peninsula_id, 'macau-peninsula', JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'), 20
UNION ALL
SELECT 'story_material_package', @package_id, 'package_storyline', 'storyline', @storyline_east_west_id, 'east_west_war_and_coexistence', JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'), 30
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
SELECT
  'story_chapter',
  `sc`.`id`,
  'story_chapter_anchor',
  'poi',
  `c`.`anchor_id`,
  `c`.`anchor_code`,
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'),
  `c`.`chapter_order` * 10
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc`
  ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
SELECT
  'story_chapter',
  `sc`.`id`,
  'story_override_target',
  'experience_flow',
  `f`.`id`,
  `c`.`flow_code`,
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed', 'anchorCode', `c`.`anchor_code`),
  `c`.`chapter_order` * 10 + 1
FROM `phase33_chapters` `c`
JOIN `story_chapters` `sc`
  ON `sc`.`storyline_id` = @storyline_east_west_id AND `sc`.`chapter_order` = `c`.`chapter_order` AND `sc`.`deleted` = 0
JOIN `experience_flows` `f`
  ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `content_relation_links` (
  `owner_type`, `owner_id`, `relation_type`, `target_type`, `target_id`, `target_code`, `metadata_json`, `sort_order`
)
SELECT
  'story_material_package',
  @package_id,
  'package_content_asset',
  'content_asset',
  `i`.`asset_id`,
  `i`.`item_key`,
  JSON_OBJECT('schemaVersion', 1, 'usageTarget', `i`.`usage_target`, 'assetKind', `i`.`asset_kind`),
  `i`.`sort_order`
FROM `story_material_package_items` `i`
WHERE `i`.`package_id` = @package_id
  AND `i`.`deleted` = 0
  AND `i`.`asset_id` IS NOT NULL
UNION ALL
SELECT
  'story_material_package',
  @package_id,
  'package_experience_flow',
  'experience_flow',
  `f`.`id`,
  `f`.`code`,
  JSON_OBJECT('schemaVersion', 1, 'source', 'phase33-seed'),
  34000 + `c`.`chapter_order`
FROM `phase33_chapters` `c`
JOIN `experience_flows` `f` ON `f`.`code` = `c`.`flow_code` AND `f`.`deleted` = 0
UNION ALL
SELECT
  'story_material_package',
  @package_id,
  'package_game_reward',
  'game_reward',
  `gr`.`id`,
  `gr`.`code`,
  JSON_OBJECT('schemaVersion', 1, 'rewardName', `r`.`reward_name`, 'roleCode', `r`.`role_code`),
  35000 + `r`.`sort_order`
FROM `phase33_rewards` `r`
JOIN `game_rewards` `gr` ON `gr`.`code` = `r`.`reward_code` AND `gr`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `target_id` = VALUES(`target_id`),
  `target_code` = VALUES(`target_code`),
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

UPDATE `story_material_packages` `p`
SET
  `storyline_id` = @storyline_east_west_id,
  `package_status` = 'published',
  `published_at` = COALESCE(`published_at`, NOW()),
  `material_count` = (SELECT COUNT(*) FROM `story_material_package_items` `i` WHERE `i`.`package_id` = `p`.`id` AND `i`.`deleted` = 0),
  `asset_count` = (SELECT COUNT(*) FROM `story_material_package_items` `i` WHERE `i`.`package_id` = `p`.`id` AND `i`.`deleted` = 0 AND `i`.`asset_id` IS NOT NULL),
  `story_object_count` = (
    SELECT COUNT(*)
    FROM `story_material_package_items` `i`
    WHERE `i`.`package_id` = `p`.`id`
      AND `i`.`deleted` = 0
      AND `i`.`target_type` <> ''
      AND `i`.`target_id` IS NOT NULL
  )
WHERE `p`.`id` = @package_id;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase33-east-west-flagship-story',
  'Phase 33 complete five-chapter East-West flagship story seed',
  'completed',
  NOW(),
  'Seeds five published chapters, story content blocks, structured experience flows, reward/title rows, semantic exploration elements, and package relations. Exploration progress uses weights, not fixed percentages.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
