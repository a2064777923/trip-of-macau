# AI-SPEC - Phase 19: AI Capability Platform Redesign and Provider Model Orchestration

> AI design contract for the Phase 19 redesign of the admin AI platform workspace.
> This phase builds an operator-grade control plane, not the final full traveler-facing runtime rollout.

---

## 1. System Classification

**System Type:** Hybrid

**Description:**
Phase 19 is an AI platform control plane inside the existing admin system. It must let operators onboard providers, manage provider-specific model or endpoint inventory, bind capabilities to primary and fallback models, observe health and cost, and launch creative-generation workflows from authoring forms. Good output is not just a successful model response. Good output means the platform can safely configure, test, govern, observe, and operationalize AI capabilities without leaking secrets or forcing operators into raw JSON.

**Critical Failure Modes:**
1. API keys, secret material, or unsafe provider responses leak to the browser or normal logs.
2. The platform assumes every provider supports the same discovery or billing contract and therefore syncs the wrong inventory or computes misleading costs.
3. Capability routing or fallback binds the wrong model to the wrong feature and silently degrades content quality or runtime stability.
4. Creative workbench outputs bypass canonical media and audit flows, creating orphaned or untraceable assets.
5. Operators cannot understand provider health, sync status, and cost well enough to safely run the platform.

---

## 1b. Domain Context

**Industry Vertical:** Smart tourism operations, story-driven content authoring, and multi-provider AI platform governance

**User Population:**
- admin operators configuring providers, models, routing, and quotas
- content editors generating covers, overlays, copy, and narration assets
- future traveler-facing services consuming the governed capability stack

**Stakes Level:** Medium to High

**Output Consequence:**
Wrong output can waste money, route user traffic through the wrong models, create inconsistent content, or expose secrets. In creative flows it can also produce unusable assets that pollute the media library. In later traveler-facing flows it can mislead itinerary or guidance behavior.

### What Domain Experts Evaluate Against

| Dimension | Good (expert accepts) | Bad (expert flags) | Stakes | Source |
|-----------|-----------------------|--------------------|--------|--------|
| Provider truthfulness | Inventory matches what the account can actually use, with clear sync status and provider caveats | UI pretends models are available when the provider or endpoint is not actually usable | High | Official provider docs listed in Section 3 |
| Capability routing clarity | Operators can see which capability uses which model, fallback chain, and parameter preset | Policy is hidden in JSON or scattered across unrelated pages | High | User requirement set |
| Secret safety | Secrets are encrypted, masked, and never echoed back to the browser | API keys or raw credentials are displayed, logged, or exported casually | Critical | Existing repo constraints and provider docs |
| Cost governance | Usage and estimated cost are visible per provider, model, and capability | Operators only see raw logs and have no practical cost visibility | High | User requirement set and provider billing docs |
| Creative workflow fit | Generated assets can be reviewed, restored, and finalized into canonical asset fields | AI output lives outside COS or media records and cannot be safely reused | High | User requirement set |

### Known Failure Modes in This Domain

- OpenAI-style UX is copied blindly onto providers that are endpoint-driven rather than model-list driven.
- Text, image, and TTS providers are treated as one flat modality and lose capability-specific configuration.
- Cost reporting depends on provider billing APIs that do not exist or are not exposed uniformly.
- Admin UI exposes too many freeform JSON fields and becomes unmaintainable for operators.
- Creative generation history becomes disconnected from the canonical asset library.

### Regulatory / Compliance Context

No specialized medical, legal, or financial regulatory regime defines the system, but the phase still has strict operational compliance boundaries:

- secrets must remain server-side and encrypted
- operator audit trails must be preserved
- provider-specific usage must remain attributable
- generated media must keep provenance and version history

### Domain Expert Roles for Evaluation

| Role | Responsibility |
|------|---------------|
| Platform operator | Validates onboarding, routing clarity, and governance usability |
| Content editor | Validates creative-workbench fit, prompt usefulness, and asset finalization |
| Backend engineer | Validates provider normalization, health metrics, and secret safety |
| Product owner | Validates that capability pages map to real business functions rather than generic AI CRUD |

---

## 2. Framework Decision

**Selected Framework:** Spring Boot native provider gateway plus React route-based admin workspace

**Version:** Spring Boot 3.2.4, Spring Framework 6.1, React 18, Ant Design 5

**Rationale:**
The repo already runs on Spring Boot and a React plus Ant Design admin shell. Phase 19 is primarily a platform-control-plane redesign with provider orchestration, not a standalone agent runtime. The most appropriate path is therefore a provider-adapter architecture in the existing admin backend plus a route-decomposed workspace in the existing admin UI. This keeps the redesign aligned with the current stack while allowing provider-specific sync strategies and rich admin forms.

**Alternatives Considered:**

| Framework | Ruled Out Because |
|-----------|------------------|
| Spring AI-first redesign | The phase is governance and provider-orchestration heavy, and the repo is not being upgraded around a framework migration here |
| Separate Node or Python AI gateway | Would fragment ownership away from the existing admin backend and duplicate security, auth, and asset-pipeline concerns |
| Keep the current monolithic React page | Cannot express the information architecture, sync semantics, or operator experience the user asked for |

**Vendor Lock-In Accepted:** Partial

The platform must support provider-specific adapters, but no single provider contract should become the canonical internal model.

---

## 3. Framework Quick Reference

### Installation

No new top-level frontend or backend framework is required. Phase 19 should extend the current admin UI and admin backend packages.

Potential supporting dependency additions during execution may include resilience and chart helpers already consistent with the stack.

### Core Imports

```java
import org.springframework.web.client.RestClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
```

```tsx
import { PageContainer } from '@ant-design/pro-layout';
import { Card, Form, Select, Table, Statistic } from 'antd';
```

### Entry Point Pattern

```java
public interface AiInventorySyncStrategy {
    String strategyCode();
    InventorySyncResult sync(ProviderProfile profile);
}

public interface AiProviderAdapter {
    String providerCode();
    ProviderTestResult testConnection(ProviderProfile profile, ProviderTestRequest request);
}
```

```tsx
<Route path="/space/ai" element={<AiCapabilityLayout />}>
  <Route index element={<AiOverviewPage />} />
  <Route path="providers" element={<AiProviderRegistryPage />} />
  <Route path="models" element={<AiModelInventoryPage />} />
  <Route path="capabilities" element={<AiCapabilityMatrixPage />} />
  <Route path="creative" element={<AiCreativeStudioPage />} />
  <Route path="observability" element={<AiObservabilityPage />} />
  <Route path="settings" element={<AiSettingsPage />} />
</Route>
```

### Key Abstractions

| Concept | What It Is | When You Use It |
|---------|-----------|-----------------|
| Provider platform | The vendor template and credential schema | When onboarding OpenAI, Bailian, Hunyuan, MiniMax, Volcengine, or Custom |
| Inventory sync strategy | The way models or endpoints are discovered | When fetching or refreshing available inventory |
| Inventory record | Normalized model or endpoint item | When binding capabilities to usable provider assets |
| Capability policy | The structured routing and parameter profile for one feature | When configuring itinerary, QA, NPC voice, creative image, and so on |
| Price rule | Local normalized pricing metadata | When estimating cost from logs |
| Creative job | Candidate generation request and history | When editors create copy or assets from forms |

### Common Pitfalls

1. Treating every provider as OpenAI plus `baseUrl` when the platform is actually endpoint-driven.
2. Making JSON the primary operator path for routing and advanced parameters.
3. Mixing provider credentials and capability policy fields into one flat form.
4. Displaying raw request logs as if they were the final observability experience.
5. Letting generated assets bypass COS and canonical asset metadata.

### Recommended Project Structure

```text
packages/admin/aoxiaoyou-admin-ui/src/
  pages/AiCapabilityCenter/
    layout/
    overview/
    providers/
    models/
    capabilities/
    observability/
    creative/
    settings/
  components/ai/

packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/
  adapter/
  inventory/
  policy/
  pricing/
  creative/
  dto/
  entity/
  mapper/
  service/
```

### Primary Sources

- OpenAI model list: https://platform.openai.com/docs/api-reference/models/list
- DashScope compatibility: https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope
- DashScope responses compatibility: https://help.aliyun.com/zh/model-studio/compatibility-with-openai-responses-api
- MiniMax API overview: https://platform.minimaxi.com/docs/api-reference/api-overview
- MiniMax API FAQ: https://platform.minimaxi.com/docs/faq/about-apis
- Hunyuan OpenAI compatibility: https://cloud.tencent.com/document/product/1729/111007
- Hunyuan quick start: https://cloud.tencent.com/document/product/1729/97730
- Hunyuan pricing: https://cloud.tencent.com/document/product/1729/97731
- Volcengine Ark overview: https://www.volcengine.com/docs/82379
- Volcengine endpoint API: https://www.volcengine.com/docs/82379/1261492
- Volcengine API list: https://www.volcengine.com/docs/82379/1511946

---

## 4. Implementation Guidance

**Model Configuration:**

- Separate text, image, speech, and multimodal configuration paths.
- Capability binding should resolve to:
  - primary inventory record
  - ordered fallback inventory records
  - parameter preset
  - optional advanced override
- Structured fields should cover:
  - temperature
  - max tokens
  - reasoning mode where applicable
  - response format
  - timeout
  - retry policy
  - image ratio or transparency flags
  - speech voice preset and output format

**Core Pattern:**

1. Onboard provider platform and credentials.
2. Sync or register usable inventory with a provider-specific strategy.
3. Bind capabilities to inventory records through structured policies.
4. Aggregate health, usage, and cost from normalized request logs.
5. Launch creative-generation jobs through reusable workbench entry points.
6. Finalize accepted outputs into COS and canonical asset metadata.

**Tool Use:**

- Use live connectivity tests per provider during onboarding.
- Keep runtime model discovery optional and provider-specific.
- Use local normalized price rules for cost estimation when billing APIs are inconsistent.

**State Management:**

- MySQL holds providers, inventory, policies, sync status, price rules, usage rollups, and creative job history.
- COS remains the durable store for generated media that is kept.
- The UI should persist only non-sensitive workbench draft state locally.

**Context Window Strategy:**

- Creative workbench prompts should be composed from a bounded set of form fields and slot requirements rather than full-record dumps.
- Capability presets should define what structured data is sent to each provider.

---

## 4b. AI Systems Best Practices

### Structured Outputs with Pydantic

Structured output should be the default for itinerary and capability-policy previews in eval and regression harnesses, even if the production runtime uses Java DTOs.

```python
from pydantic import BaseModel, Field
from typing import List, Optional


class CapabilityRoute(BaseModel):
    provider_code: str
    inventory_code: str
    role: str = Field(description="primary or fallback")


class CapabilityPolicyPreview(BaseModel):
    capability_code: str
    response_mode: str
    routes: List[CapabilityRoute]
    timeout_ms: int
    max_tokens: Optional[int] = None
```

### Async-First Design

- Connectivity tests can be synchronous.
- Inventory syncs, cost rollups, and creative-generation jobs should be job-friendly and refreshable.
- Generated media should never block a large authoring workflow on one long request.

### Prompt Engineering Discipline

- System prompt, slot template, and editor-supplied prompt additions must stay separate.
- Creative workbench should prefill prompts, not hide them completely from the editor.
- Capability presets should define stable structure so every provider binding does not invent its own ad hoc prompt contract.

### Context Window Management

- Capability routing pages should not expose huge freeform prompt blobs by default.
- Creative generation should assemble prompts from only the fields relevant to the target asset slot and content type.

### Cost and Latency Budget

- Provider tests should stay fast and isolated.
- Inventory sync should be safe to retry.
- Cost estimation should be derived from normalized usage units even when providers expose different billing surfaces.

---

## 5. Evaluation Strategy

### Dimensions

| Dimension | Rubric (Pass/Fail or 1-5) | Measurement Approach | Priority |
|-----------|---------------------------|----------------------|----------|
| Provider onboarding correctness | Pass if each supported provider template can save encrypted credentials, test connectivity, and expose valid inventory semantics | Code + Manual | Critical |
| Inventory sync truthfulness | Pass if the UI clearly distinguishes synced, manual, endpoint-based, and unsupported discovery modes | Code + Human | Critical |
| Capability routing usability | 1-5 based on whether operators can configure primary and fallback routes without resorting to JSON | Human | High |
| Secret safety | Pass if no secret leaks to read APIs, browser payloads, or logs | Code | Critical |
| Cost and health visibility | 1-5 based on whether operators can understand usage, sync state, health, and estimated cost at a glance | Human + Code | High |
| Creative workbench fit | 1-5 based on whether generated outputs can be reviewed, restored, finalized, and backfilled cleanly | Human | High |

### Eval Tooling

**Primary Tool:** Manual operator UAT plus backend integration tests; Arize Phoenix remains optional for downstream runtime evals but is not the core need for this admin-control-plane redesign.

**Setup:**

```bash
mvn -q -DskipTests compile
```

**CI/CD Integration:**

```bash
mvn -q -Dtest=AdminAi*Test test
```

### Reference Dataset

**Size:** 20 operator scenarios to start

**Composition:**

- 5 provider onboarding scenarios
- 4 inventory sync scenarios
- 4 capability routing scenarios
- 3 cost and observability scenarios
- 4 creative workbench scenarios

**Labeling:**

- platform operator for onboarding and observability
- content editor for creative workbench
- backend engineer for secret-safety and inventory truthfulness

---

## 6. Guardrails

### Online (Real-Time)

| Guardrail | Trigger | Intervention |
|-----------|---------|--------------|
| Secret redaction | Secret field appears in read response or UI payload | Block |
| Invalid routing | Capability policy references missing or disabled inventory | Block |
| Stale inventory warning | Sync strategy requires refresh and inventory is stale | Flag |
| Unsupported sync assumption | Provider template does not support runtime listing | Flag |
| Candidate-only media | Creative output is not finalized into canonical asset storage | Flag |

### Offline (Flywheel)

| Metric | Sampling Strategy | Action on Degradation |
|--------|-------------------|----------------------|
| Provider test failure rate | Track all provider tests | Inspect credentials, base URL, or sync strategy |
| Sync drift | Daily sample of synced versus expected inventory | Refresh catalog or update provider adapter |
| Policy JSON escape rate | Track how often users must open expert JSON mode | Improve structured forms |
| Creative candidate rejection rate | Sample every finalized or discarded generation set | Improve prompt templates and asset-slot presets |

---

## 7. Production Monitoring

**Tracing Tool:** Existing admin audit logs plus normalized AI usage and sync-status records

**Key Metrics to Track:**

- provider connectivity pass rate
- inventory sync success and staleness
- fallback activation rate
- estimated cost by provider, model, and capability
- creative candidate acceptance ratio

**Alert Thresholds:**

- any secret-safety violation is Sev1
- provider connectivity failures above 20 percent in a short window trigger warning
- cost spikes above configured daily thresholds trigger operator alert
- stale inventory beyond configured freshness windows triggers warning

**Smart Sampling Strategy:**

- 100 percent of failed tests, failed syncs, and failed creative jobs
- 100 percent of fallback activations
- rolling sample of successful creative finalizations

---

## Checklist

- [x] System type classified
- [x] Critical failure modes identified
- [x] Domain context researched
- [x] Regulatory or operational constraints stated
- [x] Domain expert roles identified
- [x] Framework decision documented
- [x] Alternatives considered
- [x] Framework quick reference provided
- [x] AI systems best practices provided
- [x] Evaluation dimensions defined
- [x] Reference dataset outlined
- [x] Online guardrails defined
- [x] Production monitoring outlined
