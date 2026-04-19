# Phase 18: AI Capability Center - Research

**Researched:** 2026-04-17
**Domain:** AI control plane, provider gateway, secret-safe governance, and admin observability
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

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

### Claude's Discretion
- AI 概覽儀表盤的具體視覺呈現方式。
- 不同供應商配置頁的具體表單布局與欄位分組。
- 加密實現細節，可用現有後台安全配置方式延伸，但必須達成「服務端加密存儲 + 前端不可見明文」。
- 配額/限流規則的具體資料表拆分方式，只要能支撐後續擴展到更多能力與供應商。

### Deferred Ideas (OUT OF SCOPE)
- `拍照識別定位` 的完整終端能力交付後置到專門 phase；本 phase 只需保證供應商能力、策略模型與場景代碼可承載它。
- `行程推薦規劃` 直接保存為 app 內正式路線 / 行程實體後置；首版先交付結構化輸出。
- AI 產出內容的完整審核鏈路、審批狀態與上線前審核流程後置。
- 若要把 AI 能力完整拆成多個 phase，推薦後續至少拆為：
  - AI 中台基礎與治理
  - 後台 AI 創作工作台與資源歷史
  - 小程序首批 AI 用戶服務
  - 視覺定位與高階導航
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| AI-01 | Operator can manage an AI capability center with per-capability provider configuration across mainstream providers and custom endpoints. | Use a capability catalog plus provider profiles, provider-type adapters, masked secret metadata, and per-capability policy routing instead of the current read-only `providers/policies/logs` slice. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
| AI-02 | Operator can configure provider fallback, manual switching, quotas, suspicious-concurrency throttling, and usage governance without exposing API secrets. | Use server-side encryption, write-only secret updates, policy routes, quota-rule tables, and Resilience4j guards; keep distributed throttling as an explicit extension path because Redis is not available locally today. [VERIFIED: local planning docs] [VERIFIED: codebase grep] [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc] [VERIFIED: local environment probe] |
| AI-03 | Operator can inspect cross-capability AI usage, health, and status from a central overview. | Use provider health snapshots plus usage rollups and audit records; reuse existing admin React/Ant Design charting and table stack for the overview instead of raw log-only pages. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
</phase_requirements>

## Summary

Phase 18 starts from a narrow brownfield AI slice: the admin backend currently exposes only `GET /api/admin/v1/ai/providers`, `GET /api/admin/v1/ai/policies`, and paged `GET /api/admin/v1/ai/logs`; the admin UI route and menu still use `/space/ai-navigation`; and the public backend contains no AI endpoints or provider adapters today. [VERIFIED: codebase grep]

The current schema is not sufficient for the phase contract. `ai_provider_configs` stores one provider profile with encrypted-field placeholders but no encryption implementation; `ai_navigation_policies` is still navigation-shaped and only supports one provider plus one fallback pointer; `ai_request_logs` stores only basic request metadata; and there are no canonical tables for capability cataloging, ordered fallback routes, quota rules, provider health snapshots, usage rollups, or candidate/adoption history for future AI-generated assets. [VERIFIED: codebase grep]

DashScope is the correct first real provider for this phase because official docs expose OpenAI-compatible chat endpoints plus separate native APIs for image generation and CosyVoice speech synthesis, which matches the locked phase decision to avoid forcing image/TTS through OpenAI compatibility. Both Wan image outputs and CosyVoice audio outputs are returned as time-limited URLs, so the phase should persist accepted artifacts into COS and `content_assets` immediately instead of storing provider URLs as durable assets. [VERIFIED: local planning docs] [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] [VERIFIED: codebase grep]

**Primary recommendation:** Keep `/admin` as the write/control plane, implement a provider-neutral AI gateway in the admin backend with `RestClient`, Spring Security crypto, and Resilience4j, normalize the schema around capabilities/policies/routes/governance, and stage any content-form creative-workbench integration after the control-plane foundation inside the same phase so the planner does not front-load UI work against a schema that will still change. [VERIFIED: local planning docs] [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc]

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Provider profile CRUD, masked secret preview, key rotation metadata | API / Backend | Database / Storage | Secret material must never be visible to the browser; admin UI should only edit and display masked metadata while the backend encrypts and persists ciphertext. [VERIFIED: local planning docs] [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] |
| Capability catalog, policy binding, fallback order, quota rule authoring | API / Backend | Browser / Client | The browser renders forms, but the canonical rule validation and persistence belong in the admin backend so the same contract can later be consumed by `packages/server`. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
| Provider invocation, health checks, retry/circuit-breaker behavior, suspicious-concurrency guard | API / Backend | External provider boundary | Runtime protection and health normalization belong next to the outbound HTTP calls, not in the UI or database. [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html] [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc] |
| Usage overview, cost rollups, health summary | API / Backend | Browser / Client | The backend must aggregate raw audits into stable overview metrics; the UI should not derive cross-capability health from paged logs alone. [VERIFIED: codebase grep] [CITED: https://help.aliyun.com/zh/model-studio/model-telemetry/] |
| Candidate/final AI media persistence | Database / Storage | API / Backend | Durable media ownership belongs in COS plus `content_assets`; the backend orchestrates upload and metadata writes, and the browser only previews. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
| Future traveler-facing AI runtime | API / Backend | Database / Storage | The public mini-program backend (`packages/server`) owns end-user AI execution later, but Phase 18 should define persistence and policy contracts now rather than shipping public endpoints. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |

## Standard Stack

### Core

| Library / Platform | Version | Purpose | Why Standard |
|--------------------|---------|---------|--------------|
| Spring Boot admin backend | `3.2.4` [VERIFIED: codebase grep] | Control-plane APIs, provider gateway, policy governance | Already pinned in the repo and aligned with existing admin backend conventions; avoids a repo-wide backend upgrade during this phase. [VERIFIED: codebase grep] |
| Spring Framework `RestClient` | `6.1.x via Spring Boot 3.2.4` [VERIFIED: codebase grep] | Provider HTTP client for DashScope and future custom endpoints | Official synchronous fluent client with request-factory, default header, and status-handler support; matches the existing `WechatAuthService` pattern already in the repo. [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html] |
| MyBatis-Plus + MySQL | `3.5.6` and local MySQL `8.0` [VERIFIED: codebase grep] | Canonical persistence for providers, capabilities, policies, governance, logs, and rollups | The brownfield stack already uses MyBatis-Plus on MySQL, and local Docker wiring plus live ports are already present. [VERIFIED: codebase grep] [VERIFIED: local environment probe] |
| React admin UI + Ant Design | `React 18.3.1`, `antd 5.24.6` [VERIFIED: codebase grep] | Capability center UI, forms, overview, tables | Existing admin UI stack already includes route/layout patterns plus `@ant-design/charts`; no new frontend framework is needed. [VERIFIED: codebase grep] |
| DashScope Model Studio | `current official API docs` [CITED: https://help.aliyun.com/zh/model-studio/what-is-model-studio] | First real AI provider | Official docs confirm both OpenAI-compatible and native multimodal APIs, matching the locked first-provider decision and the image/TTS split. [VERIFIED: local planning docs] [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] |

### Supporting

| Library / Platform | Version | Purpose | When to Use |
|--------------------|---------|---------|-------------|
| Spring Security Crypto | `via existing spring-boot-starter-security` [VERIFIED: codebase grep] | Server-side secret encryption and write-only secret updates | Use for encrypting provider credentials before persistence and for masked-preview-only admin responses; do not hand-roll AES helpers. [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] |
| Resilience4j Spring Boot 3 | `2.3.0` [VERIFIED: Maven Central] | Circuit breaker, retry, time limiter, rate limiter, bulkhead | Use around outbound provider calls and suspicious-concurrency protection; configure aspect order explicitly if combining retry and circuit breaker. [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc] |
| Tencent COS + existing media pipeline | `cos_api 5.6.246` [VERIFIED: codebase grep] | Durable storage for generated images/audio and adoption flow | Reuse the existing `CosAssetStorageService`, `MediaIntakeService`, and `content_assets` flow for accepted AI assets. [VERIFIED: codebase grep] |
| `@ant-design/charts` | `2.2.7` [VERIFIED: codebase grep] | Overview charts for health, usage, and cost | Already installed in the admin UI; use it instead of adding a second charting library. [VERIFIED: codebase grep] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Spring-native gateway on Boot `3.2.4` [VERIFIED: codebase grep] | Spring AI `1.x` | Spring AI upgrade notes state the framework moved to Spring Boot `3.4.2` for dependency management, so adopting it now would force a backend-platform upgrade outside this phase. [CITED: https://docs.spring.io/spring-ai/reference/upgrade-notes.html] |
| DashScope chat via compatibility + native Wan/CosyVoice APIs [VERIFIED: local planning docs] | Force everything through OpenAI-compatible mode | The native image and speech APIs expose modality-specific contracts and expiring output URLs; forcing those through chat-shaped compatibility would lose capability-specific control. [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] |
| MySQL-persisted quota rules plus in-process Resilience4j enforcement [VERIFIED: codebase grep] | Distributed Redis-backed throttling in Phase 18 | The current local stack has MySQL/Mongo running and Docker installed, but no Redis service or CLI is present; distributed throttling should be an explicit infra decision, not an accidental hidden dependency. [VERIFIED: local environment probe] [VERIFIED: codebase grep] [ASSUMED] |

**Installation:**

```xml
<!-- New backend dependency recommended for Phase 18 -->
<dependency>
  <groupId>io.github.resilience4j</groupId>
  <artifactId>resilience4j-spring-boot3</artifactId>
  <version>2.3.0</version>
</dependency>
```

**Version verification:** Existing runtime versions were verified from `pom.xml` and `package.json`, while `resilience4j-spring-boot3` `2.3.0` and optional `dashscope-sdk-java` `2.20.6` were verified via Maven Central queries. [VERIFIED: codebase grep] [VERIFIED: Maven Central]

## Architecture Patterns

### System Architecture Diagram

```text
Admin UI capability center / future form-level AI workbench entry
        |
        v
Admin backend AI control plane
  - provider profile API
  - capability/policy/route API
  - quota/governance API
  - overview/test/health API
        |
        +--> Secret crypto service --> MySQL provider ciphertext + masked metadata
        |
        +--> Policy resolver --> quota guard --> Resilience4j wrapper --> provider adapter
        |                                                           |
        |                                                           v
        |                                                    DashScope / future providers
        |
        +--> Audit logger + usage rollup --> ai_request_logs / ai_usage_rollups / health snapshots
        |
        +--> Candidate/final media capture --> COS + content_assets
        |
        +--> Later shared contract for packages/server public AI endpoints
```

The planner should keep provider invocation and governance inside a backend service boundary, with the browser limited to authoring and visualization. [VERIFIED: local planning docs] [VERIFIED: codebase grep]

### Recommended Project Structure

```text
packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/
├── ai/config/           # provider properties, secret crypto, masking, health settings
├── ai/controller/       # provider/policy/governance/overview/test endpoints
├── ai/dto/              # request/response contracts and policy snapshots
├── ai/entity/           # capability, route, quota, health, usage, candidate tables
├── ai/mapper/           # MyBatis-Plus mappers
├── ai/provider/         # DashScope and future provider adapters
├── ai/routing/          # policy resolution, manual switch, fallback, quota evaluation
├── ai/audit/            # logs, usage rollups, health summaries
└── ai/service/          # orchestration facade

packages/admin/aoxiaoyou-admin-ui/src/
├── pages/AiCapabilityCenter/   # overview + tabs/subroutes
├── components/ai/              # provider forms, policy editors, governance cards
└── services/api.ts             # central request wiring
```

Use a dedicated `ai/` subtree rather than further expanding the legacy `AdminAiServiceImpl` flat slice. [VERIFIED: codebase grep]

### Pattern 1: Provider Adapter + Normalized Result
**What:** Each provider implementation should map its native request/response contract into one internal invocation/result shape. [VERIFIED: local planning docs] [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html]
**When to use:** Every outbound AI call, including admin test/health requests and future public invocations. [VERIFIED: local planning docs]
**Example:**
```java
// Source: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
RestClient customClient = RestClient.builder()
    .baseUrl("https://example.com")
    .defaultHeader("Authorization", "Bearer ...")
    .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { /* map provider error */ })
    .build();
```

### Pattern 2: Write-Only Secret Updates + Masked Preview
**What:** Accept new secret material only on create/update, encrypt it server-side, and expose only masked preview plus rotation metadata on read APIs. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
**When to use:** Provider profile CRUD, custom endpoint credentials, and future voice-clone credentials. [VERIFIED: local planning docs]
**Example:**
```java
// Source: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html
BytesEncryptor encryptor = Encryptors.stronger(password, salt);
byte[] ciphertext = encryptor.encrypt(secretBytes);
```

### Pattern 3: Provider Guardrail Wrapper
**What:** Wrap provider calls with timeout, retry, circuit breaker, and bulkhead behavior instead of baking resilience into controller code. [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc]
**When to use:** Any remote model call, especially health checks, admin test runs, and later traveler-facing execution. [VERIFIED: local planning docs]
**Example:**
```yaml
# Source: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc
resilience4j:
  circuitbreaker:
    instances:
      aiProvider:
        slidingWindowSize: 100
        failureRateThreshold: 50
  retry:
    instances:
      aiProvider:
        maxAttempts: 3
        waitDuration: 500ms
  timelimiter:
    instances:
      aiProvider:
        timeoutDuration: 3s
```

### Anti-Patterns to Avoid

- **Keep coding against `ai_navigation_policies`:** The phase context explicitly says the design cannot remain biased to the old navigation model, and the current table name will keep bleeding that bias into code if it stays canonical. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
- **UI-only secret masking:** The current provider response DTO omits secrets, but there is no encryption or masked-preview service behind it yet; masking must be enforced in the backend response contract, not only in the form component. [VERIFIED: codebase grep]
- **Treat provider URLs as durable media:** Official Wan and CosyVoice docs say returned resource URLs are time-limited, so saving those URLs directly into business fields will create broken assets later. [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api]
- **Ship overview from raw paged logs only:** The current page derives totals and averages from the current page of results, which is not a reliable cross-capability overview. [VERIFIED: codebase grep]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Secret encryption and decryption | Custom `Cipher` helper with homegrown key derivation | Spring Security `Encryptors.stronger` / `BytesEncryptor` | The existing security stack already includes crypto utilities, and the official API documents AES-GCM support. [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] |
| Circuit breaker, timeout, retry, bulkhead logic | Nested `try/catch` + `Thread.sleep` retry loops in services | Resilience4j | Resilience4j already provides Spring Boot 3 support and ordered resilience layers for remote calls. [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc] [VERIFIED: Maven Central] |
| AI-generated media durability | Temporary files or provider URL persistence | Existing COS + `content_assets` + `MediaIntakeService` pipeline | The repo already persists asset metadata, upload ownership, and processing profiles through a tested media pipeline. [VERIFIED: codebase grep] |
| Cross-node distributed rate limiting | Ad hoc database counters embedded in provider services | Bucket4j with Redis or JDBC once distributed throttling is explicitly needed | Bucket4j exists for clustered rate limiting, but Phase 18 should not silently depend on absent Redis infrastructure. [CITED: https://github.com/bucket4j/bucket4j/blob/master/README.md] [VERIFIED: local environment probe] [ASSUMED] |

**Key insight:** The risky part of this phase is not the HTTP call; it is the surrounding contract for secrets, policy routing, quota enforcement, audit ownership, and media persistence. Those concerns should be standardized early so later AI features reuse them instead of bypassing them. [VERIFIED: local planning docs] [VERIFIED: codebase grep]

## Common Pitfalls

### Pitfall 1: Navigation-Shaped Schema Lock-In
**What goes wrong:** New code keeps stretching `ai_navigation_policies` until every capability, route, and fallback rule becomes an opaque JSON blob or overloaded enum. [VERIFIED: codebase grep]
**Why it happens:** The current seed and service layer were built for one old “AI navigation” overview, not for a two-domain capability center. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
**How to avoid:** Introduce canonical `ai_capabilities`, `ai_capability_policies`, and `ai_policy_routes` tables early, and treat the legacy navigation table as migration input rather than the future domain model. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
**Warning signs:** New fields start being named `scenarioGroup` everywhere, or provider/fallback logic depends on hard-coded `planning|qa|vision|dialogue|navigation` values in UI and backend. [VERIFIED: codebase grep]

### Pitfall 2: Secret-Safe UI with Unsafe Backend
**What goes wrong:** The UI hides secrets, but service logs, DB rows, or error payloads still leak plaintext credentials or prompt content. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
**Why it happens:** The current code has encrypted-looking columns but no encryption implementation yet, and `AdminAiLogResponse` still exposes fields like `userOpenid`, `outputSummary`, and `errorMessage` directly. [VERIFIED: codebase grep]
**How to avoid:** Make secret storage write-only, encrypt before persistence, return masked metadata only, and redact/normalize audit payloads before API response. [VERIFIED: local planning docs] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html]
**Warning signs:** Any admin GET response includes raw secret fields, raw provider payloads, or direct end-user identifiers. [VERIFIED: codebase grep]

### Pitfall 3: Treating Multimodal Jobs as Simple Synchronous CRUD
**What goes wrong:** Image and audio generation are wired like short chat requests, causing long blocking requests, missing retries, and broken artifact retention. [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api]
**Why it happens:** Chat-style request thinking leaks into image/audio flows even though returned artifacts are time-limited URLs and often need post-processing before becoming durable assets. [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api]
**How to avoid:** Model image/audio generation as candidate jobs that immediately persist accepted outputs into COS and `content_assets`, with explicit adoption/backfill flow. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
**Warning signs:** Controllers attempt to return provider asset URLs directly to long-lived CMS fields, or there is no place to record candidate/adoption history. [VERIFIED: local planning docs] [VERIFIED: codebase grep]

### Pitfall 4: Promise Distributed Throttling Without Infra
**What goes wrong:** The planner assumes cluster-wide suspicious-concurrency protection, but the local stack has no Redis and the admin backend excludes Redis dependencies today. [VERIFIED: codebase grep] [VERIFIED: local environment probe]
**Why it happens:** The public backend already references Redis, so it is easy to assume the admin AI phase can lean on it immediately. [VERIFIED: codebase grep]
**How to avoid:** Make the phase decision explicit: either add Redis as a prerequisite plan item, or scope Phase 18 to MySQL-backed quota policy plus single-node runtime enforcement. [VERIFIED: codebase grep] [ASSUMED]
**Warning signs:** AI-02 plans mention distributed counters or shared throttling state without any infra task, dependency, or fallback. [VERIFIED: local environment probe] [ASSUMED]

## Code Examples

Verified patterns from official sources:

### RestClient Builder for Provider Adapters
```java
// Source: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
RestClient customClient = RestClient.builder()
    .baseUrl("https://example.com")
    .defaultHeader("My-Header", "Foo")
    .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { /* custom error handling */ })
    .build();
```

### Spring Security Encryptor for Provider Secrets
```java
// Source: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html
BytesEncryptor encryptor = Encryptors.stronger(password, salt);
byte[] ciphertext = encryptor.encrypt(secretBytes);
```

### Resilience4j Provider Guard Configuration
```yaml
# Source: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc
resilience4j:
  circuitbreaker:
    instances:
      aiProvider:
        failureRateThreshold: 50
  retry:
    instances:
      aiProvider:
        maxAttempts: 3
  timelimiter:
    instances:
      aiProvider:
        timeoutDuration: 3s
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Treat Spring AI as the default Java abstraction for provider integrations | Stay on Spring Boot `3.2.4` and use Spring-native `RestClient` plus provider adapters in this phase | Spring AI upgrade notes for `1.0.0-M6` state dependency management moved to Spring Boot `3.4.2`. [CITED: https://docs.spring.io/spring-ai/reference/upgrade-notes.html] | Avoids a repo-wide backend-platform upgrade just to build the control plane. [VERIFIED: codebase grep] |
| Force DashScope traffic through one OpenAI-compatible contract | Use OpenAI-compatible mode for chat-like text flows and native Wan/CosyVoice APIs for image and speech | Current official DashScope docs as of `2026-04-17`. [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] | Preserves modality-specific features and makes asset URL expiry an explicit persistence concern. [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] |
| Derive overview health from raw local log pages | Store provider health snapshots plus usage rollups and render overview from those aggregates | Current phase contract and existing UI gap verified on `2026-04-17`. [VERIFIED: local planning docs] [VERIFIED: codebase grep] | Produces a real AI capability center instead of a paged debug console. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |

**Deprecated/outdated:**

- `ai_navigation_policies` as the canonical future domain table: it is still acceptable as migration input, but it is outdated as the long-term model for a two-domain capability center. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
- The `/space/ai-navigation` route name: the page label already says “AI 能力中心”, so the route and menu naming should be normalized early to avoid further navigation-era coupling. [VERIFIED: codebase grep]

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Single-node runtime enforcement plus persisted quota rules is acceptable for Phase 18 unless the planner explicitly adds Redis-backed distributed throttling. [ASSUMED] | Standard Stack, Common Pitfalls, Environment Availability | AI-02 could require an extra infra plan and a different limiter implementation. |
| A2 | The full creative-workbench UX can be staged after the control-plane schema/API foundation inside the same phase rather than front-loaded into plan 1. [ASSUMED] | Summary, Open Questions | If the user expects immediate form-level integration across all content surfaces, the plan count and wave structure will grow substantially. |

## Open Questions

1. **How much of the AI creative workbench must Phase 18 ship versus merely scaffold?**
   What we know: D-14 through D-18 lock in workbench entry, candidate/adoption history, and asset-flow expectations. [VERIFIED: local planning docs]
   What's unclear: The phase success criteria name the capability center, governance, and overview, but do not explicitly require full form-by-form workbench rollout. [VERIFIED: local planning docs]
   Recommendation: Put schema/API foundations in an early plan, and isolate form-level workbench integration into a later plan with a narrow first surface such as image/audio asset-producing forms. [ASSUMED]

2. **Should Redis become a Phase 18 prerequisite?**
   What we know: Java, Maven, Node, npm, Python, Docker, MySQL, Mongo, admin backend, and public backend are available locally, but Redis is neither in `docker-compose.local.yml` nor reachable on port `6379`. [VERIFIED: local environment probe] [VERIFIED: codebase grep]
   What's unclear: Whether the user wants AI-02 suspicious-concurrency rules to be cluster-ready now or single-node/local-ready now. [ASSUMED]
   Recommendation: Decide this before plan finalization so the planner either adds an infra plan or keeps throttling local-node scoped. [ASSUMED]

3. **Migrate in place or cut over to canonical v2 tables?**
   What we know: Existing admin code and SQL already read `ai_provider_configs`, `ai_navigation_policies`, and `ai_request_logs`, but only for read-only overview flows. [VERIFIED: codebase grep]
   What's unclear: Whether the user prefers minimum migration churn or cleaner canonical names now. [ASSUMED]
   Recommendation: Keep `ai_provider_configs` and `ai_request_logs` if practical, but introduce new canonical policy/capability tables rather than extending `ai_navigation_policies` further. [ASSUMED]

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java | Admin/backend implementation | ✓ [VERIFIED: local environment probe] | `17.0.12` [VERIFIED: local environment probe] | — |
| Maven | Admin/backend tests and packaging | ✓ [VERIFIED: local environment probe] | `3.8.8` [VERIFIED: local environment probe] | — |
| Node.js | Admin UI build and type-check | ✓ [VERIFIED: local environment probe] | `v25.9.0` [VERIFIED: local environment probe] | — |
| npm | Admin UI install/build | ✓ [VERIFIED: local environment probe] | `11.6.0` [VERIFIED: local environment probe] | — |
| Python | AI eval harness from AI-SPEC | ✓ [VERIFIED: local environment probe] | `3.12.4` [VERIFIED: local environment probe] | Skip eval harness temporarily if only backend/UI plans are being executed first. [ASSUMED] |
| Docker | Local infra bootstrapping | ✓ [VERIFIED: local environment probe] | `27.1.1` [VERIFIED: local environment probe] | — |
| MySQL local stack | Canonical persistence | ✓ [VERIFIED: local environment probe] | `mysql:8.0` in compose [VERIFIED: codebase grep] | — |
| Mongo local stack | Existing admin backend runtime | ✓ [VERIFIED: local environment probe] | `mongo:7.0` in compose [VERIFIED: codebase grep] | — |
| Admin backend port | Live admin verification | ✓ [VERIFIED: local environment probe] | `8081 reachable` [VERIFIED: local environment probe] | — |
| Public backend port | Future contract verification and shared stack checks | ✓ [VERIFIED: local environment probe] | `8080 reachable` [VERIFIED: local environment probe] | — |
| Redis | Distributed throttling / shared counters | ✗ [VERIFIED: local environment probe] | — | Use MySQL-persisted rules plus single-node Resilience4j enforcement in Phase 18, or add Redis explicitly as infra scope. [ASSUMED] |

**Missing dependencies with no fallback:**

- None for the core admin capability-center phase if the planner accepts single-node runtime enforcement first. [ASSUMED]

**Missing dependencies with fallback:**

- Redis for distributed throttling and cross-node suspicious-concurrency detection. [VERIFIED: local environment probe] [ASSUMED]

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Mockito via `spring-boot-starter-test` in both Java services. [VERIFIED: codebase grep] |
| Config file | none dedicated; Spring Boot defaults plus test classes under `src/test/java`. [VERIFIED: codebase grep] |
| Quick run command | `mvn -q -Dtest=AdminAi*Test test` in `packages/admin/aoxiaoyou-admin-backend`, plus `npm run type-check` in `packages/admin/aoxiaoyou-admin-ui`. [VERIFIED: codebase grep] [ASSUMED] |
| Full suite command | `mvn -q test` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui`. [VERIFIED: codebase grep] [ASSUMED] |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| AI-01 | Provider profile CRUD, secret masking, custom-endpoint validation, per-capability policy binding | service + controller | `mvn -q -Dtest=AdminAiProviderConfigServiceTest,AdminAiProviderControllerTest test` | ❌ Wave 0 [VERIFIED: codebase grep] |
| AI-02 | Manual switch, ordered fallback, quota evaluation, suspicious-concurrency guard, no secret leaks | service | `mvn -q -Dtest=AdminAiRoutingServiceTest,AdminAiQuotaGuardTest,AdminAiExecutionServiceTest test` | ❌ Wave 0 [VERIFIED: codebase grep] |
| AI-03 | Overview rollups, provider health summary, ownership-safe log views, admin UI rendering | service + smoke | `mvn -q -Dtest=AdminAiOverviewServiceTest,AdminAiUsageRollupServiceTest test` and `npm run build` | ❌ Wave 0 [VERIFIED: codebase grep] |

### Sampling Rate

- **Per task commit:** `mvn -q -Dtest=AdminAi*Test test` in the admin backend, plus `npm run type-check` in the admin UI. [ASSUMED]
- **Per wave merge:** `mvn -q test` in the admin backend and `npm run build` in the admin UI. [ASSUMED]
- **Phase gate:** Backend AI tests green, admin UI build green, and a new `scripts/local/smoke-phase-18-ai-capability-center.ps1` smoke run against live `8081`/`8080` stack. [VERIFIED: local planning docs] [VERIFIED: codebase grep] [ASSUMED]

### Wave 0 Gaps

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiProviderConfigServiceTest.java` — covers AI-01. [VERIFIED: codebase grep]
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiRoutingServiceTest.java` — covers AI-02. [VERIFIED: codebase grep]
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminAiOverviewServiceTest.java` — covers AI-03. [VERIFIED: codebase grep]
- [ ] `scripts/local/smoke-phase-18-ai-capability-center.ps1` — live verification for provider config, masked secrets, health, fallback, and overview. [VERIFIED: codebase grep]
- [ ] Frontend automation beyond build/type-check if the planner wants UI regression coverage; none is configured today. [VERIFIED: codebase grep]

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes [VERIFIED: codebase grep] | Existing JWT-based admin auth via `AdminAuthInterceptor`; AI APIs should remain under `/api/admin/v1/*`. [VERIFIED: codebase grep] |
| V3 Session Management | yes [VERIFIED: codebase grep] | Existing bearer-token flow in admin UI and backend; do not add secret-bearing session state to the browser. [VERIFIED: codebase grep] |
| V4 Access Control | yes [VERIFIED: codebase grep] | Use `adminUserId` and `adminRoles` for ownership filtering on generation history and sensitive logs; current AI log endpoints do not do this yet. [VERIFIED: codebase grep] |
| V5 Input Validation | yes [VERIFIED: codebase grep] | Continue Spring Validation and typed DTOs for provider/policy requests, plus allowlists for provider type, auth type, capability code, and model override. [VERIFIED: codebase grep] |
| V6 Cryptography | yes [VERIFIED: local planning docs] | Use Spring Security crypto for server-side encryption of secrets; never hand-roll application crypto. [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Secret leakage through API responses, logs, or error payloads | Information Disclosure | Write-only secret updates, encrypted persistence, masked previews, and log redaction. [VERIFIED: local planning docs] [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html] |
| Wrong provider/model selected for a capability | Tampering | Canonical policy resolver, ordered fallback routes, and audited resolved-policy snapshots per invocation. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
| Abuse bursts or suspicious concurrency | Denial of Service | Quota-rule tables plus Resilience4j bulkhead/rate-limiter/time-limiter around provider calls; add distributed store only if explicitly planned. [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc] [ASSUMED] |
| Prompt/data over-collection in audit tables | Information Disclosure | Store hashes, summaries, and minimal actor identifiers instead of full raw input/output payloads by default. [VERIFIED: local planning docs] [VERIFIED: codebase grep] |
| Candidate/final asset ownership leak | Elevation of Privilege | Use `adminUserId` ownership and role-aware reads; current media pipeline already records uploader identity and can be mirrored for AI candidate history. [VERIFIED: codebase grep] |
| Expired provider asset URL referenced as final content | Integrity / Availability | Copy accepted provider outputs into COS and `content_assets` immediately, and store provider URLs only as transient job metadata. [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] [VERIFIED: codebase grep] |

## Sources

### Primary (HIGH confidence)

- Local planning docs (`.planning/PROJECT.md`, `.planning/ROADMAP.md`, `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, `18-CONTEXT.md`, `18-AI-SPEC.md`) - phase scope, locked decisions, milestone boundaries, and requirement mapping. [VERIFIED: local planning docs]
- Admin AI code and SQL (`AiCapabilityCenter.tsx`, `AdminAiController.java`, `AdminAiServiceImpl.java`, `AiProviderConfig.java`, `AiPolicy.java`, `AiRequestLog.java`, `01-init.sql`) - current schema/API/UI behavior and gaps. [VERIFIED: codebase grep]
- Existing admin media/auth patterns (`AdminAuthInterceptor.java`, `MediaIntakeService.java`, `MediaUploadPolicyService.java`, `CosAssetStorageService.java`, `AdminContentManagementServiceImpl.java`, `WechatAuthService.java`) - reuse points for ownership, crypto boundary, media persistence, and provider HTTP clients. [VERIFIED: codebase grep]
- Spring Framework RestClient reference - provider adapter HTTP pattern. [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html]
- Spring Security Encryptors API - server-side encryption strategy. [CITED: https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/crypto/encrypt/Encryptors.html]
- Spring AI upgrade notes - compatibility constraint against Boot `3.2.4`. [CITED: https://docs.spring.io/spring-ai/reference/upgrade-notes.html]
- DashScope docs: Model Studio overview, OpenAI compatibility, install SDK, Wan image API, CosyVoice API, model telemetry. [CITED: https://help.aliyun.com/zh/model-studio/what-is-model-studio] [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope] [CITED: https://help.aliyun.com/zh/model-studio/install-sdk] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api] [CITED: https://help.aliyun.com/zh/model-studio/model-telemetry/]
- Local environment probes for Java/Maven/Node/npm/Python/Docker, reachable ports, and missing Redis. [VERIFIED: local environment probe]

### Secondary (MEDIUM confidence)

- Maven Central query for `resilience4j-spring-boot3` latest version. [VERIFIED: Maven Central]
- Maven Central query for `dashscope-sdk-java` latest version. [VERIFIED: Maven Central]
- Official Resilience4j README and Spring Boot 3 README via Context7 source references. [CITED: https://github.com/resilience4j/resilience4j/blob/master/README.adoc] [CITED: https://github.com/resilience4j/resilience4j/blob/master/resilience4j-spring-boot3/README.adoc]
- Official Bucket4j README and distributed docs via Context7 source references. [CITED: https://github.com/bucket4j/bucket4j/blob/master/README.md] [CITED: https://github.com/bucket4j/bucket4j/blob/master/asciidoc/src/main/docs/asciidoc/distributed/redis/redisson.adoc]

### Tertiary (LOW confidence)

- None.

## Metadata

**Confidence breakdown:**

- Standard stack: HIGH - the recommendation aligns with the repo-pinned stack and official Spring/Aliyun documentation. [VERIFIED: codebase grep] [CITED: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html] [CITED: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope]
- Architecture: HIGH - current codebase gaps are concrete, and the proposed boundaries match locked phase decisions plus existing admin/public ownership boundaries. [VERIFIED: local planning docs] [VERIFIED: codebase grep]
- Pitfalls: HIGH - they are directly evidenced by current code/schema gaps and official provider behavior around expiring assets and telemetry. [VERIFIED: codebase grep] [CITED: https://help.aliyun.com/zh/model-studio/wan-image-generation-api-reference] [CITED: https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api]

**Research date:** 2026-04-17
**Valid until:** 2026-05-17 for repo/codebase facts; 2026-04-24 for provider doc/version assumptions in this fast-moving AI area.
