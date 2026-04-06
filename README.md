# 澳小遊 (Trip of Macau)

澳門旅行打卡小程序 — 一款以手繪地圖為核心的旅遊探索遊戲。

## 項目架構

```
trip-of-macau/
├── packages/
│   ├── client/          # 微信小程序前端 (Taro 3.x + React 18)
│   ├── server/          # 後端服務 (Spring Boot 單體)
│   └── admin/           # 後台管理系統
│       ├── aoxiaoyou-admin-ui/     # React 18 + Ant Design Pro
│       └── aoxiaoyou-admin-backend/ # Spring Boot 3.x
├── docs/                # 技術文檔
└── README.md
```

## 技術棧

| 模塊 | 技術 |
|------|------|
| 小程序框架 | Taro 3.x + React 18 |
| 地圖渲染 | Canvas 2D + 瓦片加載 |
| 狀態管理 | Zustand / Jotai |
| 後端 | Spring Boot 3.x (Java 17) |
| 數據庫 | MySQL 8.0 + Redis 7.x |
| 後台前端 | React 18 + Ant Design Pro 5.x |
| 後台後端 | Spring Boot 3.x + MyBatis-Plus |
| 對象存儲 | 阿里雲 OSS / 騰訊雲 COS |

## 快速開始

### 前置環境

- Node.js >= 18
- JDK 17+
- MySQL 8.0
- Redis 7.x
- 微信開發者工具

### 本地開發

```bash
# 克隆倉庫
git clone https://github.com/a2064777923/trip-of-macau.git
cd trip-of-macau

# 前端小程序
cd packages/client
npm install
npm run dev:weapp

# 後端服務
cd packages/server
# 導入數據庫腳本
# 修改 application.yml 中的數據源配置
mvn spring-boot:run

# 後台管理前端
cd packages/admin/aoxiaoyou-admin-ui
npm install
npm run dev

# 後台管理後端
cd packages/admin/aoxiaoyou-admin-backend
mvn spring-boot:run
```

## 團隊成員

| 成員 | 職責 |
|------|------|
| PonyMa | 小程序架構、CDN對接、審核合規 |
| 前端工程師墨涅塔 | Canvas渲染、瓦片加載、觸發防抖 |
| 後端工程師瑟希斯 | Spring Boot、定位服務、數據庫、後台管理 |
| UI設計師玖妖丸 | 卡通地圖風格、長者模式UI |

## 文檔

- [小程序技術方案](./docs/技術方案%20V5.2.md)
- [後台管理系統技術方案](./docs/後台管理系統技術方案%20V1.0.md)

## 許可證

Private Project - All Rights Reserved
