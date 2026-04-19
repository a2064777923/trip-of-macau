# Phase 22: AI Platform Verification and Provider Default Closure - Context

**Gathered:** 2026-04-19
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 22 is a verification-and-closure phase for the Phase 19 AI platform redesign. It must prove that the current admin AI workspace works on the live local stack with honest evidence around provider onboarding, inventory truthfulness, capability routing defaults, observability, and creative-workbench finalization back into canonical authoring fields.

This phase does not reopen the Phase 19 information architecture redesign, does not expand into net-new traveler-facing AI product delivery, and does not absorb unrelated mini-program frontend acceptance work that has already been accepted as later-milestone carryover.

</domain>

<decisions>
## Implementation Decisions

### Verification scope and exit criteria
- **D-01:** Phase 22 is a verification/default-closure phase, not a new feature-expansion phase.
- **D-02:** Verification must cover the current AI workspace end-to-end: overview, providers, models, capabilities, capability detail, observability, creative studio, and settings.
- **D-03:** Success requires live evidence from the running local admin stack plus backend tests and smoke artifacts. Route rendering alone is not enough.
- **D-04:** Unsupported or unverified provider paths must be labeled honestly as unverified, template-only, or credential-missing rather than reported as passed.

### Provider proof strategy
- **D-05:** Bailian/DashScope is the primary real-provider proof target because it is the provider family the user explicitly expects to be testable in the current project context.
- **D-06:** OpenAI, Hunyuan, MiniMax, Volcengine, and Custom remain in scope for onboarding-template correctness and sync semantics, but a green live-verification result requires real credentials and must not be faked.
- **D-07:** Inventory handling must preserve provider-specific truth. The system should distinguish synced model lists, endpoint-based inventory, manual registry, and unsupported discovery modes instead of forcing every provider into one fake discovery contract.
- **D-08:** Phase 22 must normalize defaults so the local stack has at least one real text route, one real image-generation route, and one real TTS route that can be exercised end-to-end.

### Creative workbench verification
- **D-09:** Creative-workbench verification must originate from real authoring surfaces, not only the standalone AI workspace.
- **D-10:** Representative finalize proof should include at least one visual asset slot and one narration/copy-oriented slot so the finalize path is proven across more than one content type.
- **D-11:** Candidate history, restore/reselect behavior, COS persistence, and canonical `content_assets` backfill are part of verification, not optional polish.
- **D-12:** Ownership isolation remains locked: admins can inspect all generation history, while ordinary operators should see only self-owned generation history unless elevated.

### Governance and observability
- **D-13:** Secret masking and server-side encryption are hard gates. No raw API key, secret, or unmasked credential preview may appear in browser payloads, read APIs, logs, or admin tables.
- **D-14:** Observability proof must inspect provider health, sync status, usage, latency, and estimated cost at provider/model/capability levels. If cost is derived from local pricing rules rather than provider billing APIs, the UI must say so clearly.
- **D-15:** Quota, suspicious-concurrency throttling, fallback activation, and provider test flows should be verified through deterministic smoke and targeted tests rather than accepted from static UI copy.

### Out-of-scope handling
- **D-16:** Full traveler-facing AI runtime delivery remains out of scope for this phase.
- **D-17:** Photo-recognition positioning, broad mini-program AI feature rollout, and AI approval workflows stay deferred unless a direct Phase 22 verification dependency forces minimal enabling work.
- **D-18:** The already accepted Phase 21 mini-program indoor DevTools carryover is not reopened inside Phase 22.

### the agent's Discretion
- Exact test-matrix shape and evidence naming.
- Which two real authoring surfaces best represent visual finalize plus narration/copy finalize, as long as they use the real reusable workbench/finalize path.
- Whether missing live credentials for non-Bailian providers should be closed as honest carryover notes or template-only PASSes, as long as the distinction is explicit.

</decisions>

<specifics>
## Specific Ideas

- The user wants the AI Capability Center to behave like a real platform workspace, not a generic AI CRUD page.
- Provider onboarding should feel operator-friendly: choose a provider template, set base URL and secret material, test connectivity, and sync usable models or endpoints when the provider supports it.
- Structured forms remain the default operator path for capability routing and advanced parameters; JSON stays expert-only.
- The creative workbench should prefill prompts from authoring forms. Representative examples already discussed by the user include:
  - city cover or banner image generation
  - POI overlay icon or illustration generation
  - narration or guide voice synthesis
- The user already split long-term AI work into two directions:
  - admin-side creative assistance for editors
  - traveler-facing service capabilities
  Phase 22 closes the platform-verification side only; it does not promise the full traveler runtime rollout yet.
- Auto-selected discuss choice for this routed `/gsd-next`: focus on honest verification closure and default normalization, not scope expansion.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone and phase contract
- `.planning/PROJECT.md` - current milestone goals, constraints, and prior AI decisions
- `.planning/REQUIREMENTS.md` - `AI-04` through `AI-08` plus current traceability state
- `.planning/ROADMAP.md` - official Phase 22 goal, success criteria, and dependency position
- `.planning/STATE.md` - current active phase and accepted carryover decisions

### Prior AI decisions
- `.planning/phases/18-ai-capability-center/18-CONTEXT.md` - initial domain split, security posture, and creative-workbench intent
- `.planning/phases/18-ai-capability-center/18-AI-SPEC.md` - original framework, governance, and evaluation contract
- `.planning/phases/19-ai-capability-platform-redesign-and-provider-model-orchestra/19-CONTEXT.md` - locked product decisions for the platform redesign
- `.planning/phases/19-ai-capability-platform-redesign-and-provider-model-orchestra/19-AI-SPEC.md` - provider normalization, observability, and creative-workbench design contract

### Current admin AI workspace
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - route ownership and top-level AI workspace integration
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/AiWorkspaceLayout.tsx` - workspace shell and summary surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/ProvidersPage.tsx` - provider onboarding, connectivity, sync, and inventory history UX
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/ModelsPage.tsx` - model or endpoint inventory surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/CapabilitiesPage.tsx` - capability registry and routing matrix surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/CapabilityDetailPage.tsx` - per-capability structured configuration surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/CreativeStudioPage.tsx` - standalone creative workbench and generation history surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/ObservabilityPage.tsx` - health, usage, latency, and cost surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/SettingsPage.tsx` - platform-level settings and defaults
- `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx` - reusable workbench modal used by creative-generation flows
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - real authoring-surface integration point that can launch the workbench and finalize selected assets back into form fields

### Backend AI orchestration
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java` - admin AI API surface
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java` - orchestration logic for providers, inventory, capabilities, logs, and creative jobs
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiCapabilityProperties.java` - platform defaults and runtime properties
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java` - secret encryption and masking contract
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiProviderTemplateRegistry.java` - provider template registry and provider metadata
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/AiOutboundUrlGuard.java` - outbound allowlist and SSRF protection boundary
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/routing/AiGovernanceEvaluator.java` - quota, fallback, and governance evaluation
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderConfig.java` - provider config persistence
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderInventory.java` - normalized inventory persistence
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiCapabilityPolicy.java` - per-capability structured routing persistence
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiGenerationJob.java` - generation job lifecycle
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiGenerationCandidate.java` - candidate history and finalize source
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiRequestLog.java` - usage, latency, cost, and audit evidence

### Existing verification hooks
- `scripts/local/smoke-phase-18-ai-capability-center.ps1` - prior AI center smoke baseline
- `scripts/local/smoke-phase-19-ai-platform.ps1` - current platform smoke baseline to evolve for Phase 22 closure
- `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AiGovernanceServiceTest.java` - governance verification
- `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AiOutboundUrlGuardTest.java` - outbound URL guard and SSRF protection verification
- `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AiSecretCryptoServiceTest.java` - secret-safety verification

### Provider references already chosen in prior phases
- `https://platform.openai.com/docs/api-reference/models/list` - OpenAI model inventory reference
- `https://help.aliyun.com/zh/model-studio/compatibility-of-openai-with-dashscope` - DashScope compatibility semantics
- `https://help.aliyun.com/zh/model-studio/compatibility-with-openai-responses-api` - DashScope responses compatibility reference
- `https://platform.minimaxi.com/docs/api-reference/api-overview` - MiniMax API overview
- `https://cloud.tencent.com/document/product/1729/111007` - Hunyuan OpenAI-compatible access reference
- `https://www.volcengine.com/docs/82379/1261492` - Volcengine endpoint-style reference

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AiWorkspaceLayout.tsx` already provides the top-level AI workspace shell, summary band, and subpage navigation.
- `ProvidersPage.tsx` already contains the core provider onboarding UX: template selection, masked credential replacement toggles, capability binding, connectivity testing, sync, and sync-history inspection.
- `CreativeStudioPage.tsx` already provides a standalone workbench entry plus generation-history table and candidate inspection.
- `AiCreativeWorkbenchModal.tsx` is the core reusable component for generation and finalize flows.
- `MediaAssetPickerField.tsx` already integrates the workbench into real authoring forms and can auto-backfill finalized asset IDs into form fields.

### Established Patterns
- Admin UI routing is explicit in `App.tsx`; Phase 22 should verify the existing route decomposition rather than inventing a second navigation model.
- Backend AI orchestration stays in the Spring Boot admin backend with typed DTOs and MyBatis-backed persistence. Verification should preserve that ownership model.
- Secret handling, outbound URL restrictions, and governance logic already have dedicated backend classes and tests. Phase 22 should build on these rather than replacing them.
- COS and canonical media persistence are already shared platform primitives. Creative finalize proof should route through the same asset pipeline instead of inventing an AI-only storage side path.

### Integration Points
- Real authoring-surface finalize proof can be anchored through components that already embed `MediaAssetPickerField`.
- Provider defaults, capability routes, and observability summaries all converge in the admin AI API surface. Phase 22 planning should keep verification concentrated on those canonical endpoints.
- Existing smoke scripts and backend tests already cover part of the stack; the plan should extend them instead of creating an entirely separate verification harness.

</code_context>

<deferred>
## Deferred Ideas

- Full traveler-facing AI runtime rollout for itinerary planning, travel Q and A, NPC dialogue, navigation assistance, and photo-positioning UX remains outside this phase unless a direct verification dependency appears.
- AI-generated asset approval workflow, moderation chain, and publish-review gating remain deferred.
- Any revisit of Phase 21 mini-program indoor DevTools acceptance remains in a later milestone, not inside Phase 22.
- If non-Bailian providers still lack live credentials during execution, their full live-proof closure can be documented as honest carryover rather than forcing fake verification.

</deferred>

---

*Phase: 22-ai-platform-verification-and-provider-default-closure*
*Context gathered: 2026-04-19*
