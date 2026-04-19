# Phase 18: AI Capability Center - Context

**Gathered:** 2026-04-17
**Status:** Ready for planning

<domain>
## Phase Boundary

本 phase 先把 AI 能力中心做成可真正落地的控制平面基礎：提供商配置、加密密鑰管理、能力分欄、策略/回退/配額/限流治理、用量與健康總覽，以及後續「管理後台 AI 創作工作台」與「小程序用戶 AI 能力」能共用的底層契約。

本 phase 不要求一次把所有 AI 最終用戶功能完整交付到小程序前台，但資料模型、策略模型、資源流轉、權限與審計設計必須為後續能力直接鋪路，不能做成只能展示的假中台。

</domain>

<decisions>
## Implementation Decisions

### 能力分域
- **D-01:** AI 能力中心分為兩個一級能力域：
  - `內部創作輔助域`
  - `小程序用戶服務域`
- **D-02:** 翻譯不屬於 AI 能力中心範圍，沿用既有翻譯接口與翻譯設置，不與本 phase 的 AI 策略/配額/供應商治理混用。
- **D-03:** 本 phase 的配置、策略、審計與概覽設計必須同時服務兩個能力域，不能只偏向單一「AI 導航」舊模型。

### 供應商與密鑰策略
- **D-04:** 第一個真實接入的供應商使用阿里雲百煉 / DashScope。
- **D-05:** 圖像生成與語音合成優先走該平台官方原生能力接口，不強行全部套進 OpenAI 相容模式。
- **D-06:** 後台需要支持後續接入多供應商與自定義端點，至少在資料模型與配置界面上預留：
  - OpenAI
  - Anthropic
  - DeepSeek
  - OpenRouter
  - Minimax
  - 火山引擎
  - 自定義接入
- **D-07:** API Key / Secret 必須由後台配置並以服務端加密方式保存；管理頁只允許看遮罩值與最後更新資訊，任何管理 API 響應都不可回傳明文密鑰。
- **D-08:** 使用者在對話中提供的測試 Key 只可作本地驗證用途，不得寫入 repo、seed、planning 文檔或前端常量。

### 小程序用戶服務能力排序
- **D-09:** 小程序原定能力分類以以下五類為準：
  - `行程推薦規劃`
  - `旅行問答`
  - `拍照識別定位`
  - `NPC 語音對話`
  - `導航輔助`
- **D-10:** `拍照識別定位` 因涉及室內視覺錨點、樓層圖、參考物與定位驗證，作為較重能力後置為後續專門 phase，不在本 phase 內做完整落地。
- **D-11:** 首批真正要可交付的用戶服務能力，按上一輪推薦方案鎖定為：
  - `行程推薦規劃`
  - `旅行問答`
- **D-12:** `行程推薦規劃` 的首版輸出格式選擇 `B`：輸出結構化行程結果，但暫不直接落存成 app 內正式路線實體。
- **D-13:** `NPC 語音對話` 與 `導航輔助` 的供應商/策略/配額配置需要在本 phase 的能力中心內可配置，但其完整終端用戶交付可後置。

### 後台 AI 創作工作台
- **D-14:** 在地圖、子地圖、POI、故事等編輔表單中，需要有 `AI 創作工作台` 入口，以彈窗 / 工作台形式承載，而不是跳去獨立陌生頁。
- **D-15:** AI 創作工作台會根據當前表單已填資料與目標資源位自動組裝預設提示詞；編輯者可直接生成，也可修改提示詞後再生成。
- **D-16:** 生成後的資源流必須包含：
  1. 先產生候選結果
  2. 候選結果保存一份歷史版本
  3. 歷史版本上傳 COS 並保留生成記錄
  4. `admin` 可看全部生成記錄
  5. 普通後台帳號只能看自己生成的記錄
  6. 可重新生成
  7. 可回退 / 恢復到舊版本
  8. 圖像支持框選保留區域
  9. 音頻支持按秒裁剪保留區間
  10. 編輯者確認後，再按該帳號壓縮規則輸出正式資源並回填表單字段
- **D-17:** AI 生成資源的最終回填不能繞過既有媒體資源體系，仍需與 COS、`content_assets`、管理員上傳權限與壓縮規則對齊。
- **D-18:** 語音合成除輸入文案與選音色外，需為後續聲音克隆與可保存聲音配置預留模型與資源位。

### 治理、限流與可觀測性
- **D-19:** 每個 AI 能力都要支持：
  - 手動切換供應商
  - 自動 fallback
  - 模型覆蓋
  - 時段限額
  - 指定用戶群限額
  - 可疑高併發限流 / 截流
- **D-20:** AI 概覽頁需要能集中看到能力狀態、供應商健康、近期用量、錯誤率、限流狀態與成本摘要。
- **D-21:** 審計模型至少要能區分：
  - 內部創作請求
  - 小程序用戶服務請求
  - 生成候選版本
  - 正式採納版本
- **D-22:** 目前所有 AI 產出先按「可直接保存/上線」處理；完整審核流程後續再單獨規劃，不塞進本 phase。

### the agent's Discretion
- AI 概覽儀表盤的具體視覺呈現方式。
- 不同供應商配置頁的具體表單布局與欄位分組。
- 加密實現細節，可用現有後台安全配置方式延伸，但必須達成「服務端加密存儲 + 前端不可見明文」。
- 配額/限流規則的具體資料表拆分方式，只要能支撐後續擴展到更多能力與供應商。

</decisions>

<specifics>
## Specific Ideas

- 地圖編輯場景的圖片生成預設提示詞應能根據城市名、城市介紹、資源位類型、自動拼接出類似：
  - `生成 [城市] 的寫實風遊戲 CG 宣傳封面，要美觀大氣引人入勝，該城市是 [城市介紹]，圖片比例為 [比例]。`
- POI 地圖疊加物的生圖提示詞應能根據景點資料拼出偏文旅遊戲化風格的專用素材，例如：
  - `文旅 Q 版手绘 2.5D 立体模型，亚婆井前地，古井 + 复古石墙 + 南洋葡式矮屋，极简造型，历史复古色调，干净线条，透明背景无杂物，45 度俯视，8K 高清，无文字无水印，手机地图 POI 叠加专用。`
- 語音合成工作台要允許編輯者輸入文案、選音色、生成結果、裁剪秒數區間、回存為正式資源。
- 提示詞模板、供應商綁定、模型選擇、默認主語氣與輸出格式都要在 AI 能力中心的子頁內可調。

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and phase contract
- `.planning/PROJECT.md` - 當前 milestone 目標、核心約束、關鍵決策。
- `.planning/ROADMAP.md` - Phase 18 現有官方邊界與成功標準。
- `.planning/REQUIREMENTS.md` - `AI-01` / `AI-02` / `AI-03` 的需求映射。

### Existing admin AI surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx` - 現有 AI 能力中心總覽頁原型與場景分組入口。
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` - 現有 AI provider / policy / logs 前端接口定義。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java` - 現有 AI 管理接口入口。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java` - 目前 provider / policy / logs 的後端聚合邏輯。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderConfig.java` - 供應商配置實體與加密字段基礎。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiPolicy.java` - AI 策略實體。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiRequestLog.java` - AI 請求日誌實體。
- `scripts/local/mysql/init/01-init.sql` - 既有 AI 配置、策略、日誌表與 seed。

### Existing media and asset pipeline
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java` - COS 上傳與資源落庫通道。
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAssetServiceImpl.java` - 媒體資源管理既有模式。

### Public/backend integration base
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/WechatAuthService.java` - 已有真實微信登錄與公域身份模式。
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java` - 公域內容裝配現有模式。

### Official provider references
- `https://help.aliyun.com/zh/model-studio/what-is-model-studio` - 百煉 / Model Studio 產品與能力總覽。
- `https://help.aliyun.com/zh/model-studio/wam-image-generation-api-reference` - 官方圖像生成接口參考。
- `https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api` - 官方非實時 CosyVoice 語音合成接口參考。
- `https://help.aliyun.com/zh/model-studio/model-telemetry/` - 官方模型可觀測性 / 監控能力參考。

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AiCapabilityCenter.tsx`: 已有總覽頁雛形、場景卡片、provider/policy/log 三塊數據讀取，可在此基礎上重構成真正分域導航與治理總覽。
- `AiProviderConfig` / `AiPolicy` / `AiRequestLog`: 已有最初版資料表與 ORM 映射，不需要從零起模型，但需要擴成多能力域、多供應商、多用途治理模型。
- `CosAssetStorageService` + 既有 asset pipeline: 可直接承接 AI 生成圖片/音頻的候選版本與正式版本保存。
- 現有 admin 權限、角色與用戶體系: 可直接掛接「誰可看全部生成歷史」「誰只能看自己」等行為規則。

### Established Patterns
- Admin 後端沿用 Spring Boot + MyBatis-Plus + `ApiResponse<T>` / `PageResponse<T>` 風格；Phase 18 新接口應保持這一模式。
- Admin 前端以 React + Ant Design + `request.ts` 中央請求封裝為主；AI 能力中心新頁和工作台要延續此模式，不另起一套前端框架。
- 媒體資源最終都應走 COS + 資源表，不應額外生成一套脫離現有媒體中心的「AI 專用孤島資源庫」。

### Integration Points
- 管理後台表單頁：地圖、子地圖、POI、故事/章節等編輔表單需要掛入 AI 創作工作台入口。
- Admin backend：需要新增供應商配置、安全存儲、生成請求代理、候選版本歷史、採納回填與治理統計接口。
- Public backend：後續小程序真實 AI 功能會從這裡落地，所以 Phase 18 的策略與配額模型不能只綁 admin 自測。

</code_context>

<deferred>
## Deferred Ideas

- `拍照識別定位` 的完整終端能力交付後置到專門 phase；本 phase 只需保證供應商能力、策略模型與場景代碼可承載它。
- `行程推薦規劃` 直接保存為 app 內正式路線 / 行程實體後置；首版先交付結構化輸出。
- AI 產出內容的完整審核鏈路、審批狀態與上線前審核流程後置。
- 若要把 AI 能力完整拆成多個 phase，推薦後續至少拆為：
  - AI 中台基礎與治理
  - 後台 AI 創作工作台與資源歷史
  - 小程序首批 AI 用戶服務
  - 視覺定位與高階導航

</deferred>

---

*Phase: 18-ai-capability-center*
*Context gathered: 2026-04-17*
