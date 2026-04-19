# Phase 19: AI Capability Platform Redesign and Provider Model Orchestration - Research

**Researched:** 2026-04-17
**Confidence:** High for platform shape, medium for vendor-specific sync edge cases that still need live verification

## Current-State Gap

The current AI center implementation is a monolithic page with generic provider, policy, quota, template, and log CRUD. It does not satisfy the operator goals for:

- workspace-level information architecture
- provider-first onboarding
- capability-specific model routing
- form-first structured configuration
- cost and sync visibility
- creative-workbench integration

The code review starting point is:

- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`

The current page already proves the backend can support real data, but it is not the final product shape.

## Official Provider Research

### OpenAI

Confirmed from official docs:

- OpenAI documents `GET /v1/models` for listing currently available models.
- Model IDs can be retrieved and then used for downstream capability binding.
- This makes OpenAI the cleanest case for true runtime model discovery.

Design implication:

- Support a direct `LIST_API` sync strategy for OpenAI-compatible providers that actually expose `/v1/models`.

Source:

- https://platform.openai.com/docs/api-reference/models/list

### Bailian / DashScope

Confirmed from official docs:

- DashScope provides OpenAI-compatible base URLs under `.../compatible-mode/v1`.
- It separately documents native APIs for image generation and speech synthesis.
- The docs emphasize base URL, API key, and model name compatibility rather than a canonical generic list-model endpoint.

Design implication:

- Bailian must not be modeled as text-chat only.
- The provider template should support both:
  - OpenAI-compatible text or structured response calls
  - native modality adapters for image and speech
- Because the docs surfaced here do not establish one uniform list-model endpoint, the platform should support `DOC_CATALOG` or `MANUAL_ENABLEMENT` sync for Bailian in addition to any later live probe.
- This is an inference from the official documentation structure, not a confirmed absence of all discovery APIs.

Sources:

- https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope
- https://help.aliyun.com/zh/model-studio/compatibility-with-openai-responses-api
- https://help.aliyun.com/zh/model-studio/install-sdk/

### MiniMax

Confirmed from official docs:

- MiniMax publishes an API overview that spans text, speech, image, video, music, and files.
- The text overview explicitly lists supported model names.
- The docs also explain API key management and rate-limit escalation through official channels.

Design implication:

- MiniMax inventory is at least partly documentation-driven by modality and model family.
- The platform should support a `DOC_CATALOG` sync strategy that can hydrate supported models from provider metadata or curated catalogs.
- The platform should not assume a universal `/v1/models` listing endpoint just because OpenAI compatibility exists for some flows.
- Pricing and entitlement may also vary by plan, so operator-visible enablement should remain explicit.

Sources:

- https://platform.minimaxi.com/docs/api-reference/api-overview
- https://platform.minimaxi.com/docs/faq/about-apis
- https://platform.minimaxi.com/docs/api-reference/text-intro

### Tencent Hunyuan

Confirmed from official docs:

- Tencent Hunyuan provides an OpenAI-compatible base URL at `https://api.hunyuan.cloud.tencent.com/v1`.
- Tencent also documents its own native API and a product overview with model naming.
- Pricing is documented separately, including token-based billing examples.

Design implication:

- Hunyuan provider onboarding should support at least the OpenAI-compatible path, but the platform must keep room for native Tencent-specific credentials and features.
- The official docs surfaced here do not show a generic `/v1/models` listing contract comparable to OpenAI.
- The model inventory design should therefore allow `DOC_CATALOG` or provider-maintained catalog sync for Hunyuan.
- Cost visibility can be bootstrapped from documented pricing plus local request logs even before any provider-side usage sync exists.

Sources:

- https://cloud.tencent.com/document/product/1729/111007
- https://cloud.tencent.com/document/product/1729/97730
- https://cloud.tencent.com/document/product/1729/104753
- https://cloud.tencent.com/document/product/1729/97731

### Volcengine Ark

Confirmed from official docs:

- Volcengine Ark is organized around model selection plus inference endpoints.
- The docs expose endpoint lifecycle management such as `StartEndpoint`.
- The API surface also includes an `api/v3` style compatible endpoint in some docs and workflow examples.

Design implication:

- Volcengine cannot be flattened into `provider + modelName` only.
- The normalized inventory needs endpoint-aware records with fields such as endpoint ID, endpoint status, model family, region, and deployment mode.
- The sync strategy should include an `ENDPOINT_DISCOVERY` or `DEPLOYMENT_DISCOVERY` mode for providers that inventory deployable endpoints rather than just raw model IDs.

Sources:

- https://www.volcengine.com/docs/82379
- https://www.volcengine.com/docs/82379/1261492
- https://www.volcengine.com/docs/82379/1511946
- https://www.volcengine.com/docs/82379/1541594

## Cross-Provider Conclusions

### 1. One provider abstraction is not enough

The current Phase 18 abstraction is still too close to `provider + modelName + policy`.

Phase 19 needs at least:

- provider platform profile
- credential schema
- sync strategy
- inventory record
- capability binding
- routing chain
- advanced parameter profile
- price rule
- health snapshot
- usage rollup

### 2. Inventory sync must support multiple strategies

Recommended normalized sync strategies:

- `LIST_API`: provider exposes a real runtime list-model endpoint
- `DOC_CATALOG`: provider models are synced from curated platform metadata or official catalog snapshots
- `ENDPOINT_DISCOVERY`: provider inventory depends on deployed endpoints or model units
- `MANUAL`: operator enters or imports inventory manually

This keeps the backend honest across OpenAI, Bailian, Hunyuan, MiniMax, Volcengine, and Custom.

### 3. Cost visibility should not wait for provider billing APIs

Provider-side usage and billing APIs are inconsistent across the documented platforms.

Recommended normalized cost model:

- use local request logs as the canonical invocation record
- store normalized usage units per request:
  - input tokens
  - output tokens
  - image count or image resolution tier
  - audio seconds
  - video seconds
- maintain price tables by provider plus model plus unit type plus effective date
- compute estimated cost locally
- optionally attach provider-side usage imports later

### 4. Structured forms should be the default

The user specifically rejected JSON-first configuration. Phase 19 should therefore expose:

- form-driven provider templates
- form-driven capability routing
- preset parameter panels
- fallback chain builders
- advanced JSON only for power users

Fields that should become structured first:

- response mode
- temperature
- top_p
- max tokens
- tool use toggle
- structured output toggle
- voice style selection
- image ratio and transparency requirements
- timeout and retry policy
- fallback ordering
- rate limit windows

### 5. The AI center needs route-level decomposition

Recommended AI center pages:

- Overview
- Provider Registry
- Model Inventory
- Capability Matrix
- Traveler Services
- Creative Studio
- Usage and Cost
- Governance and Alerts
- Settings

Recommended capability detail pages or sub-routes:

- Itinerary Planning
- Travel Q and A
- Photo Recognition Positioning
- NPC Voice Dialogue
- Navigation Assistance
- Creative Copy
- Creative Image
- Speech Synthesis

### 6. Creative workbench belongs inside content authoring

The workbench should not live only as a central AI page. It should also be callable from content forms with:

- prompt assembly from form data and asset slot
- provider or model preselection from the capability policy
- candidate preview
- history and rollback
- COS-backed finalization
- form field backfill

## Recommended Phase Shape

Phase 19 should be planned as four execution waves:

1. AI center shell and information architecture redesign
2. Provider onboarding and normalized inventory sync
3. Capability routing plus structured configuration and observability
4. Creative workbench integration and asset-history finalization

## What Still Needs Live Verification During Execution

- Which provider credentials are needed in practice for Hunyuan native APIs versus OpenAI-compatible APIs
- Whether Bailian exposes a production-safe model discovery endpoint worth automating directly
- The exact endpoint or catalog sync path for Volcengine accounts in this workstation setup
- Which provider-side usage metadata is returned on successful requests and can be normalized without additional dashboards
