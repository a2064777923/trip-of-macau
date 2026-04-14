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
- Docker Desktop（推薦，用於本地 MySQL / MongoDB）
- 微信開發者工具

### 推薦的本地聯調方式

```bash
# 1) 在倉庫根目錄啟動本地資料庫
cd d:/Archive/trip-of-macau
docker compose -f docker-compose.local.yml up -d

# 2) 啟動後台後端（默認連接本地 MySQL：127.0.0.1:3306/aoxiaoyou）
cd packages/admin/aoxiaoyou-admin-backend
mvn spring-boot:run

# 3) 啟動後台前端（.env.local 已默認指向 http://127.0.0.1:8081）
cd packages/admin/aoxiaoyou-admin-ui
npm install
npm run dev
```

### 本地資料庫說明

- `docker-compose.local.yml` 會啟動：
  - `MySQL 8.0`：`127.0.0.1:3306`
  - `MongoDB 7.0`：`127.0.0.1:27017`
- 默認賬號密碼：
  - MySQL：`root / root`
  - MongoDB：`root / root`
- MySQL 初始化腳本：`scripts/local/mysql/init/01-init.sql`
- Mongo 初始化腳本：`scripts/local/mongo/init/01-init.js`

> 当前 `packages/admin/aoxiaoyou-admin-backend` 已接入 **MongoDB 基础连接与集合初始化**，但业务接口当前仍以 **MySQL** 为主；MongoDB 现阶段主要承载文档型配置、故事编排、AI 策略与事件日志的预留集合。

### 後台本地默認登錄

- 地址：`http://127.0.0.1:5173`
- 用戶名：`admin`
- 密碼：`admin123`

### 一鍵啟動腳本

- 後台後端：`scripts/local/start-admin-backend.cmd`
- 後台前端：`scripts/local/start-admin-ui.cmd`
- 如果你的終端環境還殘留舊版 Java，優先使用上面兩個腳本，它們會直接按本地聯調配置啟動。

### 舊的本地開發方式

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
- [後台管理系統技術方案 V1.0](./docs/後台管理系統技術方案%20V1.0.md)
- **[後台管理系統技術設計 V2.0（最新）](./docs/aoxiaoyou-admin-technical-design-v2.0.md)** ← 六大域重構版
- **[數據庫設計 V2.0（最新）](./docs/數據庫設計-v2.0.md)** ← 40+ 表完整模型
- [現狀與缺口清單 2026-04-08](./docs/后台现状与缺口清单_2026-04-08.md)
- [API 接口設計 V1.1](./docs/澳小遊后台管理系统_API设计_V1.1.md)

## CloudBase 部署信息

- 環境：`macau-trip-2gn2zm5jefa4a987`（別名：`macau-trip`）
- 地域：`ap-shanghai`
- 後台後端（Cloud Run）：`aoxiaoyou-admin-api`
  - 公網地址：`https://aoxiaoyou-admin-api-243434-4-1301163924.sh.run.tcloudbase.com`
  - 類型：容器型服務
- 後台前端（靜態托管）：`/admin/`
  - 訪問地址：`https://macau-trip-2gn2zm5jefa4a987-1301163924.tcloudbaseapp.com/admin/`
- 使用資源：Cloud Run、靜態網站托管、CloudBase MySQL、雲存儲

### 後台 V2 當前已落地能力

- 六大域分組式導航與 `HashRouter`（已修復 SPA 直連 404）
- 多城市 `城市管理` 頁面與城市發布接口
- `城市瓦片地圖` 真實列表頁（已修復 `map-tiles` 500）
- `室內建築與樓層` 基礎管理頁
- `AI 能力中心` 真實頁面：覆蓋行程規劃、旅行問答、拍照識別室內定位、NPC 語音對話與導航策略
- `收集物`、`徽章` 真實管理頁
- `管理員賬號` 真實列表頁（接現有 `sys_admin`）
- `角色與權限` 真實權限矩陣頁（角色創建 + 權限保存）
- 40+ 張後台核心表與種子數據



### 管理入口

- 環境概覽：`https://tcb.cloud.tencent.com/dev?envId=macau-trip-2gn2zm5jefa4a987#/overview`
- Cloud Run：`https://tcb.cloud.tencent.com/dev?envId=macau-trip-2gn2zm5jefa4a987#/platform-run`
- 靜態托管：`https://tcb.cloud.tencent.com/dev?envId=macau-trip-2gn2zm5jefa4a987#/static-hosting`
- MySQL：`https://tcb.cloud.tencent.com/dev?envId=macau-trip-2gn2zm5jefa4a987#/db/mysql`

## 許可證

Private Project - All Rights Reserved

## Local Backend Foundation (Phase 1)

Use this flow when working on the live mini-program backend foundation.

1. Start local datastores:

```bash
docker compose -f docker-compose.local.yml up -d mysql mongodb
```

2. Start the public backend in a separate terminal:

```bash
cmd /c scripts\local\start-public-backend.cmd
```

3. Start the admin backend in a separate terminal:

```bash
cmd /c scripts\local\start-admin-backend.cmd
```

4. Run the Phase 1 smoke harness:

```bash
powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-01-foundation.ps1
```

Notes:

- Phase 1 does not require Redis for the local smoke path. The public backend local profile disables Redis health as a startup blocker.
- Local Mongo authentication uses `root:root` with `authSource=admin`.
- If port `3306` is already occupied by a local MySQL instance, the smoke script reuses that server as long as `root / Abc123456` can reach database `aoxiaoyou`.
- The smoke script verifies these exact endpoints:
  - `http://127.0.0.1:8080/api/v1/health`
  - `http://127.0.0.1:8080/actuator/health`
  - `http://127.0.0.1:8081/api/v1/health`
