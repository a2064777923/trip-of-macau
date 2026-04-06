# 澳小遊 項目協作指南

## 📋 項目概況

- **項目名稱**: 澳小遊 (Trip of Macau)
- **倉庫**: https://github.com/a2064777923/trip-of-macau
- **狀態**: 項目初始化中
- **目標**: Q2 2026 MVP 發布

---

## 👥 團隊分工

| 成員 | 角色 | 當前任務 |
|------|------|---------|
| **PonyMa** (你) | 項目負責人 | 倉庫初始化、CDN對接、騰訊雲部署 |
| **前端工程師墨涅塔** | 前端 | Canvas 渲染引擎、瓦片加載、觸發防抖 |
| **後端工程師瑟希斯** | 後端 | Spring Boot 服務、後台管理系統 |
| **UI設計師玖妖丸** | 設計 | 卡通地圖風格、長者模式 UI |

---

## 🗂️ 項目結構

```
trip-of-macau/
├── packages/
│   ├── client/           ← 小程序前端 (Taro 3.x)
│   │   ├── config/       # Taro 配置
│   │   ├── src/
│   │   │   ├── components/   # 通用組件
│   │   │   ├── pages/        # 頁面
│   │   │   ├── store/        # Zustand 狀態
│   │   │   ├── services/     # API 調用
│   │   │   └── utils/        # 工具函數
│   │   └── package.json
│   │
│   ├── server/           ← 後端服務 (Spring Boot)
│   │   ├── src/main/java/com/aoxiaoyou/
│   │   │   ├── controller/   # REST API
│   │   │   ├── service/      # 業務邏輯
│   │   │   ├── mapper/       # MyBatis-Plus
│   │   │   ├── entity/        # 實體類
│   │   │   └── config/        # 配置類
│   │   └── src/main/resources/
│   │       └── application.yml
│   │
│   └── admin/            ← 後台管理系統
│       ├── aoxiaoyou-admin-ui/      # React 前端
│       └── aoxiaoyou-admin-backend/  # Spring Boot 後端
│
└── docs/
    ├── 數據庫設計.md
    ├── 技術方案 V5.2.md
    └── 後台管理系統技術方案 V1.0.md
```

---

## 🚀 本地開發環境

### 前置要求

| 軟件 | 版本 | 用途 |
|------|------|------|
| Node.js | >= 18 | 前端開發 |
| JDK | 17+ | Java 後端 |
| MySQL | 8.0+ | 數據庫 |
| Redis | 7.x | 緩存 |
| Git | 最新 | 版本控制 |

### 數據庫初始化

```bash
# 登錄 MySQL
mysql -u root -p

# 執行初始化腳本
source docs/數據庫設計.md
```

### 啟動各模塊

```bash
# 克隆倉庫
git clone https://github.com/a2064777923/trip-of-macau.git
cd trip-of-macau

# 小程序前端
cd packages/client
npm install
npm run dev:weapp

# 後端服務 (另一終端)
cd packages/server
# 修改 application.yml 中的數據源配置
mvn spring-boot:run

# 後台前端 (另一終端)
cd packages/admin/aoxiaoyou-admin-ui
npm install
npm run dev

# 後台後端 (另一終端)
cd packages/admin/aoxiaoyou-admin-backend
mvn spring-boot:run
```

---

## 📐 開發規範

### Git Flow

1. **從 `main` 拉取新分支**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/xxx  # 或 fix/xxx
   ```

2. **提交代碼**
   ```bash
   git add .
   git commit -m "feat: add xxx feature"
   ```

3. **推送並創建 PR**
   ```bash
   git push -u origin feature/xxx
   # 在 GitHub 上創建 Pull Request
   ```

### 分支命名

| 前綴 | 用途 | 示例 |
|------|------|------|
| `feature/` | 新功能 | `feature/map-renderer` |
| `fix/` | Bug 修復 | `fix/trigger-logic` |
| `refactor/` | 重構 | `refactor/state-management` |
| `docs/` | 文檔 | `docs/api-spec` |

### Commit 規範

```
feat: 新功能
fix: 修復問題
docs: 文檔變更
style: 代碼格式（不影響功能）
refactor: 重構
test: 測試
chore: 構建/工具
```

---

## 🔑 關鍵技術點

### 小程序地圖系統 (墨涅塔負責)
- Canvas 2D 離屏渲染
- CDN 瓦片按需加載
- 地理坐標 → 屏幕坐標轉換
- 頁面可見性變化時資源釋放

### 觸發引擎 (墨涅塔 + 瑟希斯)
- GPS + Wi-Fi 指紋多源定位
- 動態觸發半徑（30/50/80米）
- 2秒防抖 + 30分鐘冷卻
- 手動補簽兜底（200米）

### 後台管理系統 (瑟希斯負責)
- RBAC 權限管理
- JWT 雙 Token 認證
- 測試工具：模擬定位、印章操作、進度重置

---

## 📊 環境信息

| 環境 | 域名 | 用途 |
|------|------|------|
| 開發 | localhost | 本地開發 |
| 測試 | dev-api.tripofmacau.com | 測試環境 |
| 生產 | api.tripofmacau.com | 正式環境 |

### 後台管理
| 環境 | 域名 |
|------|------|
| 測試 | dev-admin.tripofmacau.com |
| 生產 | admin.tripofmacau.com |

---

## ❓ 獲取幫助

- **技術問題**: 在群里 @對應負責人
- **流程問題**: 找 PonyMa
- **緊急問題**: 直接打電話

---

_文檔版本: 1.0 | 更新時間: 2026-04-06_
