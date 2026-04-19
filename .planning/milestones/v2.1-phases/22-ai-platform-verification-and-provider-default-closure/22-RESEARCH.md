# Phase 22: AI Platform Verification and Provider Default Closure - Research

**Researched:** 2026-04-19  
**Confidence:** High for current code-paths and closure shape, medium for non-Bailian live-provider proof because it depends on real credentials

## Current-State Gap

Phase 19 built most of the AI workspace structure, but the closure bar for `AI-04` through `AI-08` is still not met honestly.

What exists today:

- The admin shell already exposes `/ai` with overview, providers, models, capabilities, creative studio, observability, and settings routes.
- Backend AI APIs already cover provider templates, provider CRUD, inventory, policies, quota rules, generation jobs, candidate finalize and restore, and platform settings.
- `MediaAssetPickerField.tsx` already launches `AiCreativeWorkbenchModal.tsx` for `image`, `icon`, and `audio` asset slots.
- `AdminAiServiceImpl.java` already finalizes selected candidates into canonical `content_assets` rows and records `finalizedAssetId`.

What is still missing for milestone-grade closure:

- verification evidence is still centered on the standalone AI workspace and a Phase 19 smoke that only proves text generation
- the workspace still contains route-truth mismatches, most visibly the ghost `traveler-services` item in `catalog.tsx` and the redirect route in `App.tsx`
- live default routing for text, image, and TTS still depends on seeded provider and inventory assumptions instead of current proof against the running stack
- provider onboarding truth is not explicit enough about what is truly live-verified versus only template-ready or documentation-seeded
- observability closure still needs explicit proof that provider health, sync status, usage, latency, fallback, and estimated cost are understandable and not overstated

## Existing Implementation Evidence

### Workspace routing and shell

The current route map already exists in:

- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/AiWorkspaceLayout.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/catalog.tsx`

Important current mismatch:

- `catalog.tsx` still defines `traveler-services`
- `AiWorkspaceLayout.tsx` filters that item out at render time
- `App.tsx` keeps `/ai/traveler-services` as a redirect back to `/ai/capabilities`

This is not an honest final state for `AI-04`. Phase 22 should either remove that ghost route or give it a truthful dedicated page.

### Provider platform and defaults

The provider template registry already models:

- `openai`
- `bailian`
- `hunyuan`
- `minimax`
- `volcengine`
- `custom`

The template registry already records:

- `syncStrategy`
- `inventorySemantics`
- `defaultBaseUrl`
- `defaultModelName`
- `supportedModalities`
- documentation-backed inventory seeds

The local SQL baseline also already seeds the actual Phase 19 witness providers:

- `dashscope-chat`
- `dashscope-image`
- `dashscope-tts`

and aligns them to witness defaults:

- `qwen3.5-flash`
- `wan2.6-t2i-turbo`
- `cosyvoice-v3-flash`

This evidence is present in:

- `scripts/local/mysql/init/34-phase-18-ai-baseline-seed.sql`
- `scripts/local/mysql/init/35-phase-19-ai-platform-orchestration.sql`

### Live gateway truth

The backend currently routes live outbound execution through:

- `DashScopeProviderGateway.java`
- `AiOutboundUrlGuard.java`
- `AiSecretCryptoService.java`

Key observation:

- text chat and model listing already use a generic OpenAI-compatible request shape
- image and TTS are still implemented with DashScope-specific endpoints
- therefore Bailian is the most realistic live-provider proof target for this workstation
- non-Bailian templates can still be verified for onboarding truthfulness, but not claimed as live-passed without credentials

### Creative finalize path

The current finalize path is already real:

- `AdminAiServiceImpl.finalizeCandidate(...)` inserts a `ContentAsset`
- the selected candidate gets `finalizedAssetId`
- `restoreCandidate(...)` switches the selected candidate without destroying history

That means Phase 22 does not need a new asset pipeline. It needs proof from real authoring surfaces that this shared path works end to end.

## Verification Hooks Already Present

### Existing smoke

The current smoke baseline is:

- `scripts/local/smoke-phase-19-ai-platform.ps1`

What it proves now:

- admin login
- provider list presence
- one provider connectivity test
- one inventory sync
- one text generation job

What it does **not** prove:

- image and TTS witness defaults on the current stack
- candidate finalize into `content_assets`
- history restore behavior
- authoring-surface launch from real forms
- honest handling of credential-missing providers
- route-by-route workspace verification

### Existing tests

The current backend tests already protect key security and governance primitives:

- `AiGovernanceServiceTest.java`
- `AiOutboundUrlGuardTest.java`
- `AiSecretCryptoServiceTest.java`

Phase 22 should build on these instead of replacing them.

## Closure Risks That Must Be Planned Explicitly

### 1. Route and navigation truthfulness

The AI workspace must not have hidden or duplicate route ownership.

Required closure behavior:

- every visible AI entry lands on a truthful dedicated page
- no ghost navigation item exists only in config while being filtered in layout
- no route silently redirects to another page unless that redirect is an intentional IA decision and the source route is removed from operator-facing navigation

### 2. Provider proof honesty

Phase 22 must separate:

- live-verified provider and modality paths
- credential-missing provider templates
- documentation-seeded catalog inventory
- manual custom onboarding

The UI and verification artifacts should use explicit language such as:

- live verified
- template ready
- credential missing
- documentation-seeded
- manual inventory

### 3. Multimodal default normalization

The minimum live-proof witness set should stay fixed:

- text: `travel_qa` through `dashscope-chat` and `qwen3.5-flash`
- image: `admin_image_generation` through `dashscope-image` and `wan2.6-t2i-turbo`
- TTS: `admin_tts_generation` through `dashscope-tts` and `cosyvoice-v3-flash`

If any of these defaults are stale or unreachable, the system should fail honestly and the seed or binding should be corrected in Phase 22.

### 4. Creative workbench must be proven from real forms

The best witness form already exists in `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx`.

It contains real AI-launchable asset slots:

- `coverAssetId`
- `mapIconAssetId`
- `audioAssetId`

This one form is sufficient to prove:

- visual asset launch and finalize
- icon-style visual launch and finalize
- narration or guide-audio launch and finalize
- field backfill into canonical authoring state

Phase 22 can optionally add a second visual witness form, but `POIManagement` is already the strongest mandatory proof surface.

### 5. Observability must stay honest

The current overview and observability pages already surface:

- provider health
- sync status
- request count
- failure count
- fallback count
- estimated cost

Phase 22 should verify and, if needed, tighten:

- provider-level metrics
- model or inventory-level filtering
- capability-level filtering
- wording that cost is locally estimated when it comes from `AiRequestLog.costUsd` or configured price rules rather than vendor billing APIs

## Recommended Witness Set

### Providers

- `dashscope-chat`
- `dashscope-image`
- `dashscope-tts`

### Capabilities

- `travel_qa`
- `admin_image_generation`
- `admin_tts_generation`

### Real authoring-surface witnesses

- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` `coverAssetId`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` `mapIconAssetId`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` `audioAssetId`

### Core backend methods to protect

- `AdminAiServiceImpl.testProvider(...)`
- `AdminAiServiceImpl.syncProviderInventory(...)`
- `AdminAiServiceImpl.createGenerationJob(...)`
- `AdminAiServiceImpl.finalizeCandidate(...)`
- `AdminAiServiceImpl.restoreCandidate(...)`

## Validation Architecture

Phase 22 should use a three-layer validation stack.

### Layer 1: targeted backend tests

Purpose:

- keep security and governance guarantees green while the phase adjusts provider defaults and finalize behavior
- add precise regression tests for default route resolution and finalize/history behavior

Expected command shape:

- `mvn -q -Dtest=AiGovernanceServiceTest,AiOutboundUrlGuardTest,AiSecretCryptoServiceTest,<Phase22Tests> test`

### Layer 2: deterministic live smoke

Purpose:

- prove the running admin stack on `8081` works with the real local database and current AI seed state
- login, list templates and providers, verify witness providers, run connectivity tests, sync inventory, create generation jobs, finalize a candidate, and confirm resulting asset linkage

Expected artifact:

- `scripts/local/smoke-phase-22-ai-platform-verification.ps1`

Critical design rule:

- missing credentials for optional providers should be reported as honest non-pass states, not treated as full smoke failure
- broken witness providers or broken witness defaults **must** fail the smoke

### Layer 3: operator UAT and verification artifact

Purpose:

- prove route truthfulness, page-level AI workspace behavior, and creative launch from real authoring forms

Expected artifacts:

- `22-UAT.md`
- `22-VERIFICATION.md`

Manual checkpoints must cover:

- `/ai`
- `/ai/providers`
- `/ai/models`
- `/ai/capabilities`
- one capability detail page
- `/ai/creative-studio`
- `/ai/observability`
- `/ai/settings`
- POI visual finalize
- POI audio finalize

## Recommended Phase Shape

Phase 22 should be split into three execute plans:

1. provider truthfulness, route cleanup, and default multimodal closure
2. authoring-surface creative finalize proof and history ownership closure
3. smoke, observability, UAT, and milestone verification artifacts

This keeps code changes in Wave 1 and verification closure in Wave 2.

## What Should Not Be Reopened

- no new traveler-facing AI product rollout
- no fresh AI feature invention beyond what is needed to make the existing workspace truthful
- no fake verification for providers that do not have live credentials in this workstation
- no second, parallel asset pipeline outside `content_assets`

---

*Phase: 22-ai-platform-verification-and-provider-default-closure*  
*Research completed: 2026-04-19*
