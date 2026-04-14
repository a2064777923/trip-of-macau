USE `aoxiaoyou`;

SET NAMES utf8mb4;

UPDATE `cities`
SET
  `name_zh` = CONVERT(0xe58d8ee4b89ce5b888e88c83e5a4a7e5ada6 USING utf8mb4),
  `name_en` = 'East China Normal University',
  `name_zht` = CONVERT(0xe88fafe69db1e5b8abe7af84e5a4a7e5adb8 USING utf8mb4),
  `subtitle_zh` = CONVERT(0xe9ab98e6a0a1e6a0a1e59bade68ea2e7b4a2e5ae9ee9aa8ce58cba USING utf8mb4),
  `subtitle_en` = 'University campus exploration sandbox',
  `subtitle_zht` = CONVERT(0xe9ab98e6a0a1e6a0a1e59c92e68ea2e7b4a2e5afa6e9a997e58d80 USING utf8mb4),
  `description_zh` = CONVERT(0xe4bd9ce4b8bae58d8ee4b89ce5b888e88c83e5a4a7e5ada6e6a0a1e59bade59cb0e59bbee4b88ee5908ee7bbade5aea4e58685e883bde58a9be8a784e58892e79a84e9a284e79599e59f8ee5b882e585a5e58fa3efbc8ce5bd93e5898de4bf9de79599e4b8bae88d89e7a8bfe38082 USING utf8mb4),
  `description_en` = 'Reserved as the ECNU campus map entry for future campus and indoor authoring work, currently kept in draft.',
  `description_zht` = CONVERT(0xe4bd9ce782bae88fafe69db1e5b8abe7af84e5a4a7e5adb8e6a0a1e59c92e59cb0e59c96e88887e5be8ce7ba8ce5aea4e585a7e883bde58a9be8a68fe58a83e79a84e9a090e79599e59f8ee5b882e585a5e58fa3efbc8ce795b6e5898de4bf9de79599e782bae88d89e7a8bfe38082 USING utf8mb4)
WHERE `code` = 'ecnu';
