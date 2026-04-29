SET NAMES utf8mb4;

UPDATE `buildings`
SET
  `name_zh` = '澳门威尼斯人',
  `name_en` = 'The Venetian Macao',
  `name_zht` = '澳門威尼斯人',
  `name_pt` = 'The Venetian Macao',
  `address_zh` = '澳门氹仔望德圣母湾大马路，路氹城',
  `address_en` = 'Estrada da Baia de N. Senhora da Esperanca, s/n, Taipa, Macau',
  `address_zht` = '澳門氹仔望德聖母灣大馬路，路氹城',
  `address_pt` = 'Estrada da Baia de N. Senhora da Esperanca, s/n, Taipa, Macau',
  `description_zh` = '澳门威尼斯人室内建筑主档，供大型综合度假区的室内地图、楼层与后续互动内容配置使用。',
  `description_en` = 'Indoor building master record for The Venetian Macao, used for indoor map, floor and future interactive content authoring.',
  `description_zht` = '澳門威尼斯人室內建築主檔，供大型綜合度假區的室內地圖、樓層與後續互動內容配置使用。',
  `description_pt` = 'Registo mestre do edificio indoor do The Venetian Macao, usado para configuracao de mapas interiores, pisos e conteudos interativos futuros.'
WHERE `building_code` = 'venetian_macau';

UPDATE `buildings`
SET
  `name_zh` = '澳门巴黎人',
  `name_en` = 'The Parisian Macao',
  `name_zht` = '澳門巴黎人',
  `name_pt` = 'The Parisian Macao',
  `address_zh` = '澳门路氹金光大道连贯公路',
  `address_en` = 'Estrada do Istmo, Lote 3, Cotai Strip, Cotai, Macau, China',
  `address_zht` = '澳門路氹金光大道連貫公路',
  `address_pt` = 'Estrada do Istmo, Lote 3, Cotai Strip, Cotai, Macau, China',
  `description_zh` = '澳门巴黎人室内建筑主档，可用于酒店综合体的室内楼层组织、导览点位与故事内容编排。',
  `description_en` = 'Indoor building master record for The Parisian Macao, ready for hotel-complex floor organization, navigation points and story content authoring.',
  `description_zht` = '澳門巴黎人室內建築主檔，可用於酒店綜合體的室內樓層組織、導覽點位與故事內容編排。',
  `description_pt` = 'Registo mestre do edificio indoor do The Parisian Macao, pronto para organizar pisos, pontos de navegacao e conteudos narrativos do complexo.'
WHERE `building_code` = 'parisian_macau';

UPDATE `buildings`
SET
  `name_zh` = '澳门新濠影汇',
  `name_en` = 'Studio City Macau',
  `name_zht` = '澳門新濠影滙',
  `name_pt` = 'Studio City Macau',
  `address_zh` = '澳门路氹连贯公路',
  `address_en` = 'Estrada do Istmo, Cotai, Macau',
  `address_zht` = '澳門路氹連貫公路',
  `address_pt` = 'Estrada do Istmo, Cotai, Macau',
  `description_zh` = '澳门新濠影汇室内建筑主档，可用于娱乐综合体的室内导览、楼层编排与互动内容配置。',
  `description_en` = 'Indoor building master record for Studio City Macau, suitable for entertainment-resort indoor navigation, floor authoring and interactive content setup.',
  `description_zht` = '澳門新濠影滙室內建築主檔，可用於娛樂綜合體的室內導覽、樓層編排與互動內容配置。',
  `description_pt` = 'Registo mestre do edificio indoor do Studio City Macau, adequado para navegacao interior, organizacao de pisos e configuracao de conteudos interativos.'
WHERE `building_code` = 'studio_city_macau';

UPDATE `buildings`
SET
  `name_zh` = '澳门葡京人',
  `name_en` = 'Lisboeta Macau',
  `name_zht` = '澳門葡京人',
  `name_pt` = 'Lisboeta Macau',
  `address_zh` = '澳门路氹溜冰路，位于路氹城葡京人综合体',
  `address_en` = 'Rua da Patinagem, Cotai, Macau',
  `address_zht` = '澳門路氹溜冰路，位於路氹城葡京人綜合體',
  `address_pt` = 'Rua da Patinagem, Cotai, Macau',
  `description_zh` = '澳门葡京人室内示范地图，收录地下层、一楼与二楼的瓦片、附件与标记点数据，可用于小程序室内导览联调。',
  `description_en` = 'Indoor demo dataset for Lisboeta Macau, including basement, first-floor and second-floor tiles, attachments and markers for mini-program indoor integration.',
  `description_zht` = '澳門葡京人室內示範地圖，收錄地下層、一樓與二樓的瓦片、附件與標記點資料，可用於小程序室內導覽聯調。',
  `description_pt` = 'Conjunto de demonstracao indoor do Lisboeta Macau com piso -1, primeiro piso e segundo piso, incluindo azulejos, anexos e marcadores para integracao do mini-programa.'
WHERE `building_code` = 'lisboeta_macau';

UPDATE `indoor_floors`
SET
  `floor_name_zh` = '地下层',
  `floor_name_en` = 'Basement Level',
  `floor_name_zht` = '地下層',
  `floor_name_pt` = 'Piso -1',
  `description_zh` = '澳门葡京人地下层室内导览示范楼层，包含瓦片地图、附件资源与示例标记点。',
  `description_en` = 'Lisboeta Macau indoor showcase layer for Basement Level, including tile map assets, attachments and sample markers.',
  `description_zht` = '澳門葡京人地下層室內導覽示範樓層，包含瓦片地圖、附件資源與示例標記點。',
  `description_pt` = 'Camada de demonstracao indoor do Lisboeta Macau para o Piso -1, com mapa em azulejos, anexos e marcadores de exemplo.'
WHERE `building_id` = 5 AND `floor_code` = 'G';

UPDATE `indoor_floors`
SET
  `floor_name_zh` = '一楼',
  `floor_name_en` = '1st Floor',
  `floor_name_zht` = '一樓',
  `floor_name_pt` = 'Primeiro Piso',
  `description_zh` = '澳门葡京人一楼室内导览示范楼层，包含瓦片地图、附件资源与示例标记点。',
  `description_en` = 'Lisboeta Macau indoor showcase layer for 1st Floor, including tile map assets, attachments and sample markers.',
  `description_zht` = '澳門葡京人一樓室內導覽示範樓層，包含瓦片地圖、附件資源與示例標記點。',
  `description_pt` = 'Camada de demonstracao indoor do Lisboeta Macau para o Primeiro Piso, com mapa em azulejos, anexos e marcadores de exemplo.'
WHERE `building_id` = 5 AND `floor_code` = '1F';

UPDATE `indoor_floors`
SET
  `floor_name_zh` = '二楼',
  `floor_name_en` = '2nd Floor',
  `floor_name_zht` = '二樓',
  `floor_name_pt` = 'Segundo Piso',
  `description_zh` = '澳门葡京人二楼室内导览示范楼层，包含瓦片地图、附件资源与示例标记点。',
  `description_en` = 'Lisboeta Macau indoor showcase layer for 2nd Floor, including tile map assets, attachments and sample markers.',
  `description_zht` = '澳門葡京人二樓室內導覽示範樓層，包含瓦片地圖、附件資源與示例標記點。',
  `description_pt` = 'Camada de demonstracao indoor do Lisboeta Macau para o Segundo Piso, com mapa em azulejos, anexos e marcadores de exemplo.'
WHERE `building_id` = 5 AND `floor_code` = '2F';
