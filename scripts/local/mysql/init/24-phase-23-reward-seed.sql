USE `aoxiaoyou`;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @city_macau_id = (SELECT `id` FROM `cities` WHERE `code` = 'macau' LIMIT 1);
SET @sub_map_macau_peninsula_id = (SELECT `id` FROM `sub_maps` WHERE `code` = 'macau-peninsula' LIMIT 1);
SET @sub_map_taipa_id = COALESCE(
  (SELECT `id` FROM `sub_maps` WHERE `code` = 'taipa' LIMIT 1),
  @sub_map_macau_peninsula_id
);
SET @sub_map_coloane_id = COALESCE(
  (SELECT `id` FROM `sub_maps` WHERE `code` = 'coloane' LIMIT 1),
  @sub_map_macau_peninsula_id
);
SET @storyline_macau_fire_id = (SELECT `id` FROM `storylines` WHERE `code` = 'macau_fire_route' LIMIT 1);
SET @indoor_building_id = COALESCE(
  (SELECT `id` FROM `buildings` WHERE `building_code` = 'lisboeta_macau' LIMIT 1),
  (SELECT `id` FROM `buildings` WHERE `building_code` = 'lisboeta_demo' LIMIT 1),
  (SELECT `id` FROM `buildings` ORDER BY `id` LIMIT 1)
);
SET @indoor_building_code = COALESCE(
  (SELECT `building_code` FROM `buildings` WHERE `id` = @indoor_building_id LIMIT 1),
  'indoor-building'
);
SET @indoor_floor_id = COALESCE(
  (SELECT `id` FROM `indoor_floors` WHERE `building_id` = @indoor_building_id AND `floor_code` IN ('G', '1F', 'B1') ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `indoor_floors` WHERE `building_id` = @indoor_building_id ORDER BY `floor_number`, `id` LIMIT 1),
  (SELECT `id` FROM `indoor_floors` ORDER BY `id` LIMIT 1)
);
SET @indoor_floor_code = COALESCE(
  (SELECT `floor_code` FROM `indoor_floors` WHERE `id` = @indoor_floor_id LIMIT 1),
  'floor-1'
);
SET @indoor_behavior_id = (SELECT `id` FROM `indoor_node_behaviors` ORDER BY `id` LIMIT 1);
SET @indoor_behavior_code = COALESCE(
  (SELECT `behavior_code` FROM `indoor_node_behaviors` WHERE `id` = @indoor_behavior_id LIMIT 1),
  'indoor-behavior'
);

SET @cover_asset_id = 300009;
SET @icon_asset_id = 300008;
SET @audio_asset_id = 300010;
SET @video_asset_id = 300009;

SET @presentation_code = 'presentation_lisboeta_fullscreen_finale';
SET @offline_prize_code = 'prize_lisboeta_offline_postcard';
SET @voucher_prize_code = 'prize_macau_harbor_voucher';
SET @badge_reward_code = 'reward_badge_lisboeta_night_patrol';
SET @title_reward_code = 'reward_title_mirror_harbor_chronicler';
SET @title_peninsula_reward_code = 'reward_title_peninsula_memory_keeper';
SET @title_taipa_reward_code = 'reward_title_taipa_twilight_balladeer';
SET @title_coloane_reward_code = 'reward_title_coloane_tide_listener';
SET @fragment_reward_code = 'reward_fragment_macau_fire_archive';
SET @rule_redemption_code = 'rule_lisboeta_redeem_showcase';
SET @rule_fragment_code = 'rule_fire_archive_fragment_grant';
SET @rule_title_code = 'rule_harbor_title_showcase';
SET @rule_peninsula_title_code = 'rule_peninsula_title_showcase';
SET @rule_taipa_title_code = 'rule_taipa_title_showcase';
SET @rule_coloane_title_code = 'rule_coloane_title_showcase';

DELETE FROM `reward_rule_bindings`
WHERE (`owner_domain` = 'redeemable_prize' AND `owner_id` IN (
        SELECT `id` FROM `redeemable_prizes` WHERE `code` IN (@offline_prize_code, @voucher_prize_code)
      ))
   OR (`owner_domain` = 'game_reward' AND `owner_id` IN (
        SELECT `id` FROM `game_rewards`
        WHERE `code` IN (
          @badge_reward_code,
          @title_reward_code,
          @title_peninsula_reward_code,
          @title_taipa_reward_code,
          @title_coloane_reward_code,
          @fragment_reward_code
        )
      ))
   OR (`owner_domain` = 'indoor_behavior' AND `owner_id` = @indoor_behavior_id AND `rule_id` IN (
        SELECT `id`
        FROM `reward_rules`
        WHERE `code` IN (
          @rule_redemption_code,
          @rule_fragment_code,
          @rule_title_code,
          @rule_peninsula_title_code,
          @rule_taipa_title_code,
          @rule_coloane_title_code
        )
      ));

DELETE FROM `content_relation_links`
WHERE (`owner_type` = 'redeemable_prize' AND `owner_id` IN (
        SELECT `id` FROM `redeemable_prizes` WHERE `code` IN (@offline_prize_code, @voucher_prize_code)
      ))
   OR (`owner_type` = 'game_reward' AND `owner_id` IN (
        SELECT `id` FROM `game_rewards`
        WHERE `code` IN (
          @badge_reward_code,
          @title_reward_code,
          @title_peninsula_reward_code,
          @title_taipa_reward_code,
          @title_coloane_reward_code,
          @fragment_reward_code
        )
      ));

DELETE FROM `reward_presentation_steps`
WHERE `presentation_id` IN (
  SELECT `id` FROM `reward_presentations` WHERE `code` = @presentation_code
);

DELETE FROM `reward_conditions`
WHERE `group_id` IN (
  SELECT `id`
  FROM `reward_condition_groups`
  WHERE `rule_id` IN (
    SELECT `id`
    FROM `reward_rules`
    WHERE `code` IN (
      @rule_redemption_code,
      @rule_fragment_code,
      @rule_title_code,
      @rule_peninsula_title_code,
      @rule_taipa_title_code,
      @rule_coloane_title_code
    )
  )
);

DELETE FROM `reward_condition_groups`
WHERE `rule_id` IN (
  SELECT `id`
  FROM `reward_rules`
  WHERE `code` IN (
    @rule_redemption_code,
    @rule_fragment_code,
    @rule_title_code,
    @rule_peninsula_title_code,
    @rule_taipa_title_code,
    @rule_coloane_title_code
  )
);

DELETE FROM `reward_presentations` WHERE `code` = @presentation_code;
DELETE FROM `redeemable_prizes` WHERE `code` IN (@offline_prize_code, @voucher_prize_code);
DELETE FROM `game_rewards`
WHERE `code` IN (
  @badge_reward_code,
  @title_reward_code,
  @title_peninsula_reward_code,
  @title_taipa_reward_code,
  @title_coloane_reward_code,
  @fragment_reward_code
);
DELETE FROM `reward_rules`
WHERE `code` IN (
  @rule_redemption_code,
  @rule_fragment_code,
  @rule_title_code,
  @rule_peninsula_title_code,
  @rule_taipa_title_code,
  @rule_coloane_title_code
);

INSERT INTO `reward_presentations` (
  `code`,
  `name_zh`,
  `name_zht`,
  `presentation_type`,
  `first_time_only`,
  `skippable`,
  `minimum_display_ms`,
  `interrupt_policy`,
  `queue_policy`,
  `priority_weight`,
  `cover_asset_id`,
  `voice_over_asset_id`,
  `sfx_asset_id`,
  `summary_text`,
  `config_json`,
  `status`
)
VALUES (
  @presentation_code,
  '葡京人夜巡終章演出',
  '葡京人夜巡終章演出',
  'fullscreen_video',
  1,
  1,
  4200,
  'queue_after_current',
  'enqueue',
  95,
  @cover_asset_id,
  @audio_asset_id,
  @audio_asset_id,
  '以全屏影片、旁白與音效呈現高價值獎勵與稱號的獲得瞬間。',
  JSON_OBJECT(
    'fallbackMode', 'popup_card',
    'blockMapInput', TRUE,
    'voiceOverPolicy', 'play_once',
    'downgradeRepeatAcquisitionToToast', TRUE,
    'theme', 'lisboeta-night-patrol'
  ),
  'published'
);

SET @presentation_id = (SELECT `id` FROM `reward_presentations` WHERE `code` = @presentation_code LIMIT 1);

INSERT INTO `reward_presentation_steps` (
  `presentation_id`,
  `step_type`,
  `step_code`,
  `title_text`,
  `asset_id`,
  `duration_ms`,
  `skippable_override`,
  `trigger_sfx_asset_id`,
  `voice_over_asset_id`,
  `overlay_config_json`,
  `sort_order`
)
VALUES
  (
    @presentation_id,
    'fullscreen_video',
    'intro_video',
    '夜巡終章啟幕',
    @video_asset_id,
    4200,
    0,
    @audio_asset_id,
    @audio_asset_id,
    JSON_OBJECT('overlayTitle', '濠江夜巡', 'overlaySubtitle', '終章演出'),
    0
  ),
  (
    @presentation_id,
    'popup_card',
    'reward_recap',
    '獲得獎勵總結',
    @cover_asset_id,
    2200,
    1,
    @audio_asset_id,
    NULL,
    JSON_OBJECT('ctaLabel', '收下獎勵', 'showEquipButton', TRUE),
    1
  );

INSERT INTO `reward_rules` (
  `code`,
  `rule_type`,
  `status`,
  `name_zh`,
  `name_zht`,
  `summary_text`,
  `advanced_config_json`
)
VALUES
  (
    @rule_redemption_code,
    'redemption_rule',
    'published',
    '夜巡線下明信片兌換條件',
    '夜巡線下明信片兌換條件',
    '澳門探索進度達 70%，且已完成夜巡互動鏈後，可兌換線下明信片。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'city')
  ),
  (
    @rule_fragment_code,
    'grant_rule',
    'published',
    '濠江烽煙碎片發放條件',
    '濠江烽煙碎片發放條件',
    '首次完成葡京人夜巡室內互動後，可獲得 1 枚「濠江烽煙碎片」。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'indoor_behavior')
  ),
  (
    @rule_title_code,
    'grant_rule',
    'published',
    '鏡海記述者稱號條件',
    '鏡海記述者稱號條件',
    '澳門探索進度達 85%，並曾觸發夜巡互動內容，即可獲得「鏡海記述者」稱號。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'city')
  ),
  (
    @rule_peninsula_title_code,
    'grant_rule',
    'published',
    '半島拾史人稱號條件',
    '半島拾史人稱號條件',
    '澳門半島探索進度達 72%，即可獲得「半島拾史人」稱號。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'sub_map')
  ),
  (
    @rule_taipa_title_code,
    'grant_rule',
    'published',
    '氹仔暮色行吟者稱號條件',
    '氹仔暮色行吟者稱號條件',
    '氹仔島探索進度達 68%，並累積 6 枚濠江烽煙碎片後，即可獲得「氹仔暮色行吟者」稱號。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'sub_map')
  ),
  (
    @rule_coloane_title_code,
    'grant_rule',
    'published',
    '路環潮聲守望者稱號條件',
    '路環潮聲守望者稱號條件',
    '路環島探索進度達 66%，即可獲得「路環潮聲守望者」稱號。',
    JSON_OBJECT('source', 'phase23-seed', 'scopeType', 'sub_map')
  );

SET @rule_redemption_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_redemption_code LIMIT 1);
SET @rule_fragment_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_fragment_code LIMIT 1);
SET @rule_title_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_title_code LIMIT 1);
SET @rule_peninsula_title_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_peninsula_title_code LIMIT 1);
SET @rule_taipa_title_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_taipa_title_code LIMIT 1);
SET @rule_coloane_title_id = (SELECT `id` FROM `reward_rules` WHERE `code` = @rule_coloane_title_code LIMIT 1);

INSERT INTO `reward_condition_groups` (
  `rule_id`,
  `parent_group_id`,
  `group_code`,
  `operator_type`,
  `minimum_match_count`,
  `summary_text`,
  `advanced_config_json`,
  `sort_order`
)
VALUES
  (
    @rule_redemption_id,
    NULL,
    'root_redemption',
    'all',
    NULL,
    '需同時滿足探索進度與互動完成條件。',
    JSON_OBJECT('mode', 'all'),
    0
  ),
  (
    @rule_fragment_id,
    NULL,
    'root_fragment',
    'all',
    NULL,
    '需先完成指定室內互動後才會發放碎片。',
    JSON_OBJECT('mode', 'all'),
    0
  ),
  (
    @rule_title_id,
    NULL,
    'root_title',
    'all',
    NULL,
    '需同時滿足探索進度與歷史互動條件。',
    JSON_OBJECT('mode', 'all'),
    0
  ),
  (
    @rule_peninsula_title_id,
    NULL,
    'root_peninsula_title',
    'all',
    NULL,
    '需完成澳門半島探索進度門檻。',
    JSON_OBJECT('mode', 'all'),
    0
  ),
  (
    @rule_taipa_title_id,
    NULL,
    'root_taipa_title',
    'all',
    NULL,
    '需同時滿足氹仔探索與碎片累積條件。',
    JSON_OBJECT('mode', 'all'),
    0
  ),
  (
    @rule_coloane_title_id,
    NULL,
    'root_coloane_title',
    'all',
    NULL,
    '需完成路環探索進度門檻。',
    JSON_OBJECT('mode', 'all'),
    0
  );

SET @redemption_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_redemption_id AND `group_code` = 'root_redemption'
  LIMIT 1
);
SET @fragment_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_fragment_id AND `group_code` = 'root_fragment'
  LIMIT 1
);
SET @title_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_title_id AND `group_code` = 'root_title'
  LIMIT 1
);
SET @peninsula_title_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_peninsula_title_id AND `group_code` = 'root_peninsula_title'
  LIMIT 1
);
SET @taipa_title_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_taipa_title_id AND `group_code` = 'root_taipa_title'
  LIMIT 1
);
SET @coloane_title_group_id = (
  SELECT `id` FROM `reward_condition_groups`
  WHERE `rule_id` = @rule_coloane_title_id AND `group_code` = 'root_coloane_title'
  LIMIT 1
);

UPDATE `reward_rules` SET `root_condition_group_id` = @redemption_group_id WHERE `id` = @rule_redemption_id;
UPDATE `reward_rules` SET `root_condition_group_id` = @fragment_group_id WHERE `id` = @rule_fragment_id;
UPDATE `reward_rules` SET `root_condition_group_id` = @title_group_id WHERE `id` = @rule_title_id;
UPDATE `reward_rules` SET `root_condition_group_id` = @peninsula_title_group_id WHERE `id` = @rule_peninsula_title_id;
UPDATE `reward_rules` SET `root_condition_group_id` = @taipa_title_group_id WHERE `id` = @rule_taipa_title_id;
UPDATE `reward_rules` SET `root_condition_group_id` = @coloane_title_group_id WHERE `id` = @rule_coloane_title_id;

INSERT INTO `reward_conditions` (
  `group_id`,
  `condition_type`,
  `metric_type`,
  `operator_type`,
  `comparator_value`,
  `comparator_unit`,
  `summary_text`,
  `config_json`,
  `sort_order`
)
VALUES
  (
    @redemption_group_id,
    'numeric_progress',
    'city_exploration_percent',
    'gte',
    '70',
    'percent',
    '澳門探索進度需達 70%。',
    JSON_OBJECT('cityId', @city_macau_id, 'cityCode', 'macau'),
    0
  ),
  (
    @redemption_group_id,
    'interaction_history',
    'triggered_behavior',
    'contains',
    @indoor_behavior_code,
    'behavior_code',
    '需曾觸發室內夜巡互動行為。',
    JSON_OBJECT('behaviorId', @indoor_behavior_id, 'behaviorCode', @indoor_behavior_code),
    1
  ),
  (
    @fragment_group_id,
    'interaction_history',
    'triggered_behavior',
    'contains',
    @indoor_behavior_code,
    'behavior_code',
    '完成葡京人夜巡互動後發放碎片。',
    JSON_OBJECT('behaviorId', @indoor_behavior_id, 'behaviorCode', @indoor_behavior_code, 'grantCount', 1),
    0
  ),
  (
    @title_group_id,
    'numeric_progress',
    'city_exploration_percent',
    'gte',
    '85',
    'percent',
    '澳門探索進度需達 85%。',
    JSON_OBJECT('cityId', @city_macau_id, 'cityCode', 'macau'),
    0
  ),
  (
    @title_group_id,
    'interaction_history',
    'triggered_behavior',
    'contains',
    @indoor_behavior_code,
    'behavior_code',
    '需曾觸發夜巡導覽互動。',
    JSON_OBJECT('behaviorId', @indoor_behavior_id, 'behaviorCode', @indoor_behavior_code),
    1
  ),
  (
    @peninsula_title_group_id,
    'numeric_progress',
    'sub_map_exploration_percent',
    'gte',
    '72',
    'percent',
    '澳門半島探索進度需達 72%。',
    JSON_OBJECT('subMapId', @sub_map_macau_peninsula_id, 'subMapCode', 'macau-peninsula'),
    0
  ),
  (
    @taipa_title_group_id,
    'numeric_progress',
    'sub_map_exploration_percent',
    'gte',
    '68',
    'percent',
    '氹仔島探索進度需達 68%。',
    JSON_OBJECT('subMapId', @sub_map_taipa_id, 'subMapCode', 'taipa'),
    0
  ),
  (
    @taipa_title_group_id,
    'inventory_progress',
    'reward_fragment_count',
    'gte',
    '6',
    'pieces',
    '需累積 6 枚濠江烽煙碎片。',
    JSON_OBJECT('rewardCode', @fragment_reward_code, 'requiredCount', 6),
    1
  ),
  (
    @coloane_title_group_id,
    'numeric_progress',
    'sub_map_exploration_percent',
    'gte',
    '66',
    'percent',
    '路環島探索進度需達 66%。',
    JSON_OBJECT('subMapId', @sub_map_coloane_id, 'subMapCode', 'coloane'),
    0
  );

INSERT INTO `redeemable_prizes` (
  `code`,
  `legacy_source_type`,
  `legacy_source_id`,
  `prize_type`,
  `fulfillment_mode`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `name_pt`,
  `subtitle_zh`,
  `subtitle_en`,
  `subtitle_zht`,
  `subtitle_pt`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `description_pt`,
  `highlight_zh`,
  `highlight_en`,
  `highlight_zht`,
  `highlight_pt`,
  `cover_asset_id`,
  `stamp_cost`,
  `inventory_total`,
  `inventory_redeemed`,
  `stock_policy_json`,
  `fulfillment_config_json`,
  `operator_notes`,
  `presentation_id`,
  `status`,
  `sort_order`,
  `publish_start_at`,
  `publish_end_at`
)
VALUES
  (
    @offline_prize_code,
    '',
    NULL,
    'postcard',
    'offline_pickup',
    '葡京人夜巡實體明信片',
    'Lisboeta Night Patrol Postcard',
    '葡京人夜巡實體明信片',
    'Postal da patrulha noturna do Lisboeta',
    '線下領取限定版',
    'Offline pickup limited edition',
    '線下領取限定版',
    'Edição limitada para retirada offline',
    '完成夜巡故事與室內互動後，可到指定櫃檯兌換一套實體明信片。',
    'Complete the night patrol story and indoor interactions to redeem a physical postcard set at the counter.',
    '完成夜巡故事與室內互動後，可到指定櫃檯兌換一套實體明信片。',
    'Após concluir a rota noturna e as interações internas, pode trocar um conjunto de postais no balcão.',
    '限量 50 份，需到場核銷。',
    'Limited to 50 sets with on-site verification.',
    '限量 50 份，需到場核銷。',
    'Limitado a 50 unidades, com verificação presencial.',
    @cover_asset_id,
    42,
    50,
    3,
    JSON_OBJECT('stockMode', 'limited', 'remainingHintThreshold', 10),
    JSON_OBJECT('pickupCityId', @city_macau_id, 'pickupVenue', '澳門葡京人資訊櫃檯', 'verificationMethod', 'qr_code'),
    'Phase 23 線下兌換樣板。',
    @presentation_id,
    'published',
    10,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @voucher_prize_code,
    '',
    NULL,
    'code',
    'voucher_code',
    '鏡海夜色數碼兌換券',
    'Mirror Harbor Night Voucher',
    '鏡海夜色數碼兌換券',
    'Cupão digital da noite do Porto Interior',
    '自動發放優惠碼',
    'Voucher code issued automatically',
    '自動發放優惠碼',
    'Código emitido automaticamente',
    '用於兌換夜間路線限定優惠或延伸內容的數碼券樣板。',
    'A voucher sample for night-route benefits or unlockable bonus content.',
    '用於兌換夜間路線限定優惠或延伸內容的數碼券樣板。',
    'Modelo de cupão digital para benefícios da rota noturna ou conteúdo extra.',
    '可重試生成備用碼。',
    'Supports backup code generation.',
    '可重試生成備用碼。',
    'Suporta geração de código suplente.',
    @cover_asset_id,
    18,
    200,
    12,
    JSON_OBJECT('stockMode', 'pool', 'poolSize', 200),
    JSON_OBJECT('codePool', 'lisboeta-night-voucher', 'singleUse', TRUE, 'claimTimeoutHours', 24),
    'Phase 23 券碼型獎勵樣板。',
    @presentation_id,
    'published',
    20,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  );

INSERT INTO `game_rewards` (
  `code`,
  `legacy_source_type`,
  `legacy_source_id`,
  `reward_type`,
  `rarity`,
  `stackable`,
  `max_owned`,
  `can_equip`,
  `can_consume`,
  `name_zh`,
  `name_en`,
  `name_zht`,
  `name_pt`,
  `subtitle_zh`,
  `subtitle_en`,
  `subtitle_zht`,
  `subtitle_pt`,
  `description_zh`,
  `description_en`,
  `description_zht`,
  `description_pt`,
  `highlight_zh`,
  `highlight_en`,
  `highlight_zht`,
  `highlight_pt`,
  `cover_asset_id`,
  `icon_asset_id`,
  `animation_asset_id`,
  `reward_config_json`,
  `presentation_id`,
  `status`,
  `sort_order`,
  `publish_start_at`,
  `publish_end_at`
)
VALUES
  (
    @badge_reward_code,
    '',
    NULL,
    'badge',
    'legendary',
    0,
    1,
    0,
    0,
    '夜巡引路人',
    'Night Patrol Pathfinder',
    '夜巡引路人',
    'Guia da patrulha noturna',
    '完成夜巡後的榮譽徽章',
    'Honor badge after finishing the night patrol',
    '完成夜巡後的榮譽徽章',
    'Insígnia honorífica após concluir a patrulha noturna',
    '代表玩家已掌握葡京人夜巡與室內支線節奏。',
    'Marks mastery over the Lisboeta night patrol and indoor branch pacing.',
    '代表玩家已掌握葡京人夜巡與室內支線節奏。',
    'Representa domínio da rota noturna do Lisboeta e do ramo interno.',
    '徽章獲得時會播放完整演出。',
    'Triggers the full acquisition ceremony.',
    '徽章獲得時會播放完整演出。',
    'Aciona a cerimónia completa ao obter.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('badgeType', 'collection', 'showInProfile', TRUE),
    @presentation_id,
    'published',
    10,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @title_reward_code,
    '',
    NULL,
    'title',
    'legendary',
    0,
    1,
    1,
    0,
    '鏡海記述者',
    'Mirror Harbor Chronicler',
    '鏡海記述者',
    'Cronista do Porto Interior',
    '可裝備稱號',
    'Equippable title',
    '可裝備稱號',
    'Título equipável',
    '授予完成高階探索與互動鏈的玩家，作為長線城市探索身份標記。',
    'Granted to players who finish high-tier exploration and interaction chains.',
    '授予完成高階探索與互動鏈的玩家，作為長線城市探索身份標記。',
    'Concedido a quem conclui exploração avançada e cadeias de interação.',
    '稱號獲得後可在個人頁裝備展示。',
    'Can be equipped in the profile page.',
    '稱號獲得後可在個人頁裝備展示。',
    'Pode ser equipado na página pessoal.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('titleStyle', 'epic', 'profileAccent', 'ruby', 'scopeCode', 'macau'),
    @presentation_id,
    'published',
    20,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @title_peninsula_reward_code,
    '',
    NULL,
    'title',
    'epic',
    0,
    1,
    1,
    0,
    '半島拾史人',
    'Peninsula Memory Keeper',
    '半島拾史人',
    'Guardião das Memórias da Península',
    '澳門半島探索稱號',
    'Macau Peninsula exploration title',
    '澳門半島探索稱號',
    'Título de exploração da Península de Macau',
    '獻給在石板街巷與古老立面之間耐心尋史的旅人，當你把澳門半島的故事拼回輪廓，這個稱號便會亮起。',
    'Awarded to travelers who patiently gather the stories hidden in Macau Peninsula streets and facades.',
    '獻給在石板街巷與古老立面之間耐心尋史的旅人，當你把澳門半島的故事拼回輪廓，這個稱號便會亮起。',
    'Concedido a viajantes que reconstroem, com paciência, as histórias escondidas nas ruas e fachadas da Península de Macau.',
    '澳門半島探索度達 72% 即可獲得。',
    'Unlocks at 72% Macau Peninsula exploration.',
    '澳門半島探索度達 72% 即可獲得。',
    'Desbloqueia ao atingir 72% de exploração da Península de Macau.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('titleStyle', 'historic', 'profileAccent', 'amber', 'scopeCode', 'macau-peninsula'),
    @presentation_id,
    'published',
    21,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @title_taipa_reward_code,
    '',
    NULL,
    'title',
    'legendary',
    0,
    1,
    1,
    0,
    '氹仔暮色行吟者',
    'Taipa Twilight Balladeer',
    '氹仔暮色行吟者',
    'Trovador do Crepúsculo da Taipa',
    '氹仔夜色特別稱號',
    'Taipa dusk special title',
    '氹仔夜色特別稱號',
    'Título especial do entardecer da Taipa',
    '當你在氹仔島的巷陌與光影之間走得足夠久，並把戰火碎片拼成歌，這個稱號會替夜色為你署名。',
    'Granted to explorers who walk deep enough into Taipa twilight and gather the fragments of its stories.',
    '當你在氹仔島的巷陌與光影之間走得足夠久，並把戰火碎片拼成歌，這個稱號會替夜色為你署名。',
    'Concedido a quem percorre a Taipa ao cair da noite e reúne fragmentos suficientes para lhes dar voz.',
    '氹仔島探索度達 68%，並累積 6 枚濠江烽煙碎片。',
    'Requires 68% Taipa exploration and 6 Harbor Fire fragments.',
    '氹仔島探索度達 68%，並累積 6 枚濠江烽煙碎片。',
    'Requer 68% de exploração da Taipa e 6 fragmentos das chamas do Porto.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('titleStyle', 'night', 'profileAccent', 'violet', 'scopeCode', 'taipa'),
    @presentation_id,
    'published',
    22,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @title_coloane_reward_code,
    '',
    NULL,
    'title',
    'epic',
    0,
    1,
    1,
    0,
    '路環潮聲守望者',
    'Coloane Tide Listener',
    '路環潮聲守望者',
    'Guardião das Marés de Coloane',
    '路環海岸探索稱號',
    'Coloane coast exploration title',
    '路環海岸探索稱號',
    'Título de exploração costeira de Coloane',
    '授予願意在路環風聲與潮聲之間停下腳步的旅人，只有真正聽見海岸節奏的人，才能佩戴這個稱號。',
    'Awarded to travelers who slow down enough to hear the rhythm of Coloane shorelines.',
    '授予願意在路環風聲與潮聲之間停下腳步的旅人，只有真正聽見海岸節奏的人，才能佩戴這個稱號。',
    'Concedido a quem abranda o passo para ouvir, de verdade, o ritmo das marés de Coloane.',
    '路環島探索度達 66% 即可獲得。',
    'Unlocks at 66% Coloane exploration.',
    '路環島探索度達 66% 即可獲得。',
    'Desbloqueia ao atingir 66% de exploração de Coloane.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('titleStyle', 'coastal', 'profileAccent', 'teal', 'scopeCode', 'coloane'),
    @presentation_id,
    'published',
    23,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  ),
  (
    @fragment_reward_code,
    '',
    NULL,
    'city_fragment',
    'epic',
    1,
    99,
    0,
    0,
    '濠江烽煙碎片',
    'Harbor Fire Fragment',
    '濠江烽煙碎片',
    'Fragmento das chamas do Porto',
    '城市限定碎片',
    'City-exclusive fragment',
    '城市限定碎片',
    'Fragmento exclusivo da cidade',
    '可用於後續兌換戰火主題的完整典藏或限定內容。',
    'Used later for war-theme archive exchanges and bonus content.',
    '可用於後續兌換戰火主題的完整典藏或限定內容。',
    'Usado depois para trocar por coleções completas ou conteúdo bónus.',
    '可疊加收集並由互動規則自動發放。',
    'Stackable and auto-granted by interaction rules.',
    '可疊加收集並由互動規則自動發放。',
    'Acumulável e concedido automaticamente por regras de interação.',
    @cover_asset_id,
    @icon_asset_id,
    NULL,
    JSON_OBJECT('cityCode', 'macau', 'fragmentSet', 'harbor-fire-archive'),
    NULL,
    'published',
    30,
    '2026-01-01 00:00:00',
    '2027-12-31 23:59:59'
  );

SET @offline_prize_id = (SELECT `id` FROM `redeemable_prizes` WHERE `code` = @offline_prize_code LIMIT 1);
SET @voucher_prize_id = (SELECT `id` FROM `redeemable_prizes` WHERE `code` = @voucher_prize_code LIMIT 1);
SET @badge_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @badge_reward_code LIMIT 1);
SET @title_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @title_reward_code LIMIT 1);
SET @title_peninsula_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @title_peninsula_reward_code LIMIT 1);
SET @title_taipa_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @title_taipa_reward_code LIMIT 1);
SET @title_coloane_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @title_coloane_reward_code LIMIT 1);
SET @fragment_reward_id = (SELECT `id` FROM `game_rewards` WHERE `code` = @fragment_reward_code LIMIT 1);

INSERT INTO `reward_rule_bindings` (
  `rule_id`,
  `owner_domain`,
  `owner_id`,
  `owner_code`,
  `binding_role`,
  `sort_order`
)
VALUES
  (@rule_redemption_id, 'redeemable_prize', @offline_prize_id, @offline_prize_code, 'eligibility', 0),
  (@rule_redemption_id, 'redeemable_prize', @voucher_prize_id, @voucher_prize_code, 'eligibility', 0),
  (@rule_title_id, 'game_reward', @badge_reward_id, @badge_reward_code, 'grant_rule', 0),
  (@rule_title_id, 'game_reward', @title_reward_id, @title_reward_code, 'grant_rule', 0),
  (@rule_peninsula_title_id, 'game_reward', @title_peninsula_reward_id, @title_peninsula_reward_code, 'grant_rule', 0),
  (@rule_taipa_title_id, 'game_reward', @title_taipa_reward_id, @title_taipa_reward_code, 'grant_rule', 0),
  (@rule_coloane_title_id, 'game_reward', @title_coloane_reward_id, @title_coloane_reward_code, 'grant_rule', 0),
  (@rule_fragment_id, 'game_reward', @fragment_reward_id, @fragment_reward_code, 'grant_rule', 0)
ON DUPLICATE KEY UPDATE
  `owner_code` = VALUES(`owner_code`),
  `sort_order` = VALUES(`sort_order`);

INSERT INTO `reward_rule_bindings` (
  `rule_id`,
  `owner_domain`,
  `owner_id`,
  `owner_code`,
  `binding_role`,
  `sort_order`
)
SELECT
  @rule_fragment_id,
  'indoor_behavior',
  @indoor_behavior_id,
  @indoor_behavior_code,
  'grant_rule',
  0
FROM `indoor_node_behaviors`
WHERE `id` = @indoor_behavior_id
ON DUPLICATE KEY UPDATE
  `owner_code` = VALUES(`owner_code`),
  `sort_order` = VALUES(`sort_order`);

INSERT INTO `content_relation_links` (
  `owner_type`,
  `owner_id`,
  `relation_type`,
  `target_type`,
  `target_id`,
  `target_code`,
  `metadata_json`,
  `sort_order`
)
VALUES
  ('redeemable_prize', @offline_prize_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @offline_prize_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @offline_prize_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @offline_prize_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @offline_prize_id, 'indoor_floor_binding', 'indoor_floor', @indoor_floor_id, @indoor_floor_code, JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @offline_prize_id, 'attachment_asset', 'asset', @cover_asset_id, CONCAT('asset-', @cover_asset_id), JSON_OBJECT('usage', 'cover-gallery'), 0),
  ('redeemable_prize', @voucher_prize_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @voucher_prize_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('redeemable_prize', @voucher_prize_id, 'attachment_asset', 'asset', @audio_asset_id, CONCAT('asset-', @audio_asset_id), JSON_OBJECT('usage', 'voucher-voice-over'), 0),
  ('game_reward', @badge_reward_id, 'storyline_binding', 'storyline', @storyline_macau_fire_id, 'macau_fire_route', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @badge_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @badge_reward_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @badge_reward_id, 'indoor_floor_binding', 'indoor_floor', @indoor_floor_id, @indoor_floor_code, JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_reward_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_peninsula_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_peninsula_reward_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_taipa_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_taipa_reward_id, 'sub_map_binding', 'sub_map', @sub_map_taipa_id, 'taipa', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_coloane_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @title_coloane_reward_id, 'sub_map_binding', 'sub_map', @sub_map_coloane_id, 'coloane', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @fragment_reward_id, 'city_binding', 'city', @city_macau_id, 'macau', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @fragment_reward_id, 'sub_map_binding', 'sub_map', @sub_map_macau_peninsula_id, 'macau-peninsula', JSON_OBJECT('source', 'phase23-seed'), 0),
  ('game_reward', @fragment_reward_id, 'indoor_building_binding', 'indoor_building', @indoor_building_id, @indoor_building_code, JSON_OBJECT('source', 'phase23-seed'), 0)
ON DUPLICATE KEY UPDATE
  `metadata_json` = VALUES(`metadata_json`),
  `sort_order` = VALUES(`sort_order`),
  `deleted` = 0;

INSERT INTO `seed_runs` (`seed_key`, `description`, `status`, `executed_at`, `notes`)
VALUES (
  'phase23-reward-domain-seed',
  'Phase 23 split reward showcase seed',
  'completed',
  NOW(),
  'Seeds offline_pickup and voucher_code prizes, badge and four title rewards, one city fragment reward, fullscreen_video acquisition presentation, and exploration-threshold honor rules.'
)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `executed_at` = VALUES(`executed_at`),
  `notes` = VALUES(`notes`);
