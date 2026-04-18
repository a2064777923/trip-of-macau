# Roadmap: Trip of Macau

## Milestones

- [x] **v1.0 Live Backend Cutover** - Phases 1-6 shipped on 2026-04-13. Archive: `.planning/milestones/v1.0-ROADMAP.md`
- [x] **v2.0 Admin Control-Plane Reconstruction** - Phases 7-12 executed and archived on 2026-04-15 with accepted gaps. Archive: `.planning/milestones/v2.0-ROADMAP.md`
- [ ] **v2.1 Interactive Rules Platform and AI Capability Center** - Phases 14-19 executed by 2026-04-17; milestone audit on 2026-04-18 opened Phases 20-22 for gap closure

## Active Milestone

**Milestone v2.1: Interactive Rules Platform and AI Capability Center**

**Goal:** Close the accepted `v2.0` control-plane gaps while delivering the indoor interaction-rules platform and the AI capability center on top of the existing live admin/public/runtime stack.

**Coverage:** 9 phases, 16 requirements, 16 mapped, 0 unmapped

| # | Phase | Goal | Requirements |
|---|-------|------|--------------|
| 14 | Carryover Control Plane Closure | Finish the accepted `v2.0` collection/reward, user-progress, operations, and system-control-plane gaps and close them with explicit verification | `CARRY-01`, `CARRY-02`, `CARRY-03` |
| 15 | Indoor Interaction Rule Authoring | Add operator-facing appearance / trigger / effect rule authoring and path-based indoor behavior composition | `RULE-01`, `RULE-02` |
| 16 | Indoor Rule Workbench and Governance Center | Rebuild complex indoor rule editing into a dedicated validated workbench and add cross-entity rule governance / conflict visibility | `RULE-04`, `RULE-05` |
| 17 | Indoor Runtime Evaluation and Mini-Program Alignment | Evaluate authored indoor rules safely through public APIs and the mini-program runtime | `RULE-03` |
| 18 | AI Capability Center | Build the AI capability center for provider configuration, fallback, quotas/governance, and overview visibility | `AI-01`, `AI-02`, `AI-03` |
| 19 | AI Capability Platform Redesign and Provider Model Orchestration | Redesign the AI capability center into a real platform workspace with provider onboarding, model inventory, structured routing, cost observability, and creative-workbench flows | `AI-04`, `AI-05`, `AI-06`, `AI-07`, `AI-08` |
| 20 | Carryover Verification and Stability Closure | Close the remaining carryover verification gaps, stabilize collection/reward authoring under live admin usage, and finish milestone-grade verification for the carryover control plane | `CARRY-01`, `CARRY-02`, `CARRY-03` |
| 21 | Indoor Rule Acceptance and Verification Closure | Finish milestone-grade verification for the indoor authoring, governance, and runtime chain, including WeChat DevTools acceptance and verification artifacts | `RULE-01`, `RULE-02`, `RULE-03`, `RULE-04`, `RULE-05` |
| 22 | AI Platform Verification and Provider Default Closure | Close the Phase 19 verification gap, normalize live multimodal provider defaults, and verify creative-workbench finalize flows back into authoring surfaces | `AI-04`, `AI-05`, `AI-06`, `AI-07`, `AI-08` |

## Phase Details

### Phase 14: Carryover Control Plane Closure

**Goal:** Finish the accepted `v2.0` gaps around collections/rewards, user progress, operations/testing, system settings, and milestone-grade verification.

**Requirements:** `CARRY-01`, `CARRY-02`, `CARRY-03`

**Success criteria:**
1. Operators can author collectibles, badges, and rewards with richer bindings to maps, sub-maps, indoor maps, example content, and easier preset-driven configuration.
2. Operators can inspect traveler progress, exploration recomputation, and live operations/testing surfaces without relying on legacy placeholder tooling.
3. System settings clearly own translation defaults, upload policies, map/runtime defaults, and other control-plane settings left ambiguous in `v2.0`.
4. Repeatable smoke/UAT coverage closes the archived `v2.0` carryover items explicitly instead of relying on archive-time assumptions.

**Depends on:** Archived milestone `v2.0`

### Phase 15: Indoor Interaction Rule Authoring

**Goal:** Add a real authoring model for indoor appearance conditions, trigger chains, effects, and animated path-based behaviors.

**Requirements:** `RULE-01`, `RULE-02`

**Success criteria:**
1. Operators can define appearance and trigger conditions for indoor markers/overlays through structured admin forms instead of raw JSON only.
2. Operators can compose multiple trigger/effect steps, including prerequisite chains and reusable presets where appropriate.
3. Operators can author animated path-based movement/effect behaviors with persisted geometry and runtime-safe metadata.

**Depends on:** Phase 14

### Phase 16: Indoor Rule Workbench and Governance Center

**Goal:** Move complex indoor rule editing into a dedicated validated workbench and provide a global governance center for rule-bearing entities, conflicts, and enable/disable control.

**Requirements:** `RULE-04`, `RULE-05`

**Success criteria:**
1. Operators edit interaction behaviors in a dedicated full-screen modal or drawer workbench with isolated validation and explicit apply/save semantics instead of a crowded inline section.
2. Each behavior supports operator-defined naming, ordering, status control, and in-workbench thumbnail-based point/path authoring for indoor maps.
3. A dedicated rule governance page lets operators filter rule-bearing entities, inspect trigger/appearance/effect overlaps, follow trigger chains, and enable or disable rules centrally.
4. The authored workbench contract remains aligned with the indoor marker/building forms instead of duplicating or drifting from the canonical persistence model.

**Depends on:** Phase 15

### Phase 17: Indoor Runtime Evaluation and Mini-Program Alignment

**Goal:** Execute authored indoor interaction rules safely through the public backend and mini-program runtime.

**Requirements:** `RULE-03`

**Success criteria:**
1. Public indoor APIs return the authored rule payloads needed by the mini-program without exposing unsafe internal state.
2. The mini-program indoor runtime evaluates supported appearance / trigger / effect rules predictably against real authored data.
3. Verification covers rule evaluation, trigger ordering, and failure-safe behavior on the live local stack.

**Depends on:** Phase 16

### Phase 18: AI Capability Center

**Goal:** Build the AI capability center for provider configuration, secret-safe governance, quotas, fallback, and operator overview.

**Requirements:** `AI-01`, `AI-02`, `AI-03`

**Success criteria:**
1. Operators can configure multiple providers or custom endpoints per AI capability without leaking secrets in admin responses or stored config previews.
2. Operators can define fallback/manual-switch rules, quotas, and suspicious-concurrency controls for the supported AI capabilities.
3. The admin overview shows provider health, usage, and capability status in one coherent AI capability center.

**Depends on:** Phase 14

### Phase 19: AI Capability Platform Redesign and Provider Model Orchestration

**Goal:** Turn the Phase 18 foundation into an operator-grade AI platform workspace with dedicated sub-sections, provider-aware model orchestration, structured capability routing, observability, and creative authoring integration.

**Requirements:** `AI-04`, `AI-05`, `AI-06`, `AI-07`, `AI-08`

**Success criteria:**
1. The admin shell exposes AI Capability Center as a major control-plane section with dedicated overview, provider, model, capability, observability, and settings surfaces instead of one overloaded CRUD page.
2. Operators can onboard OpenAI, Bailian, Hunyuan, MiniMax, Volcengine, and custom providers through provider templates, encrypted credentials, connectivity tests, and provider-specific model or endpoint inventory sync flows.
3. Operators can bind primary and fallback models to each AI capability through structured forms first, with expert JSON reserved for advanced overrides instead of being the default path.
4. The platform shows provider health, sync status, usage, latency, and estimated cost at provider, model, and capability levels using normalized backend metrics.
5. Content editors can open a reusable AI creative workbench from authoring forms, generate candidate assets or copy, keep COS-backed version history, and finalize selected outputs back into the target form field.

**Depends on:** Phase 18

### Phase 20: Carryover Verification and Stability Closure

**Goal:** Close the remaining carryover regression risk and finish milestone-grade verification for collection/reward authoring, traveler progress, and carryover system settings.

**Requirements:** `CARRY-01`, `CARRY-02`, `CARRY-03`

**Gap Closure:** Opens from `v2.1` milestone audit findings around missing verification and the unresolved Phase 14 carryover authoring regression trail.

**Success criteria:**
1. Current collectible, badge, and reward authoring stays responsive under live admin navigation and save flows, with the earlier freeze regression either reproduced and fixed or explicitly retired through fresh evidence.
2. Traveler progress, operations/testing, and carryover system settings receive a fresh verification pass against the current live stack rather than relying on stale UAT state.
3. Phase 14 receives a formal verification artifact that supersedes the earlier incomplete UAT.

**Depends on:** Phase 14

### Phase 21: Indoor Rule Acceptance and Verification Closure

**Goal:** Close the indoor milestone chain by finishing operator/runtime acceptance across authoring, governance, public runtime, and mini-program execution.

**Requirements:** `RULE-01`, `RULE-02`, `RULE-03`, `RULE-04`, `RULE-05`

**Gap Closure:** Opens from `v2.1` milestone audit findings around missing verification artifacts and pending WeChat DevTools acceptance for the indoor runtime.

**Success criteria:**
1. The indoor rule authoring surfaces from Phases 15 and 16 are re-verified on the current admin UI with explicit pass/fail evidence.
2. The mini-program indoor runtime is exercised in WeChat DevTools against live authored data, including auth-gated and supported-behavior flows.
3. The indoor chain receives a formal verification artifact that closes the remaining milestone audit gap for `RULE-01` through `RULE-05`.

**Depends on:** Phase 17

### Phase 22: AI Platform Verification and Provider Default Closure

**Goal:** Finish milestone-grade closure for the redesigned AI platform by proving live provider onboarding, multimodal defaults, observability, and creative-workbench finalization on the current stack.

**Requirements:** `AI-04`, `AI-05`, `AI-06`, `AI-07`, `AI-08`

**Gap Closure:** Opens from `v2.1` milestone audit findings around the missing Phase 19 verification artifact and incomplete live validation for multimodal provider defaults.

**Success criteria:**
1. The AI workspace has a dedicated verification artifact that covers overview, provider registry, model inventory, routing, observability, settings, and creative workbench.
2. Live provider onboarding evidence covers the currently supported provider templates with deterministic outcomes, including cleanup of seeded image/TTS defaults that currently fail real tests.
3. Creative-workbench launch and finalize flows are verified from real authoring surfaces instead of the standalone workspace only.

**Depends on:** Phase 19

## Archived Milestones

<details>
<summary>[x] v2.0 Admin Control-Plane Reconstruction (Phases 7-12 executed, archived 2026-04-15 with accepted gaps)</summary>

- [x] Phase 7: Admin Shell and Real Auth Alignment - Traditional Chinese shell cleanup and real-auth baseline alignment landed.
- [x] Phase 8: Multilingual Authoring Foundation - Four-language field patterns and translation-settings groundwork landed.
- [x] Phase 9: Spatial Model Rebuild - Cities, sub-maps, POIs, and coordinate normalization were rebuilt.
- [x] Phase 10: Media Asset Pipeline and Library - COS-backed upload handling and the central media library landed.
- [x] Phase 11: Story, Activity, and Collection Composition - Story, chapter, activity, and collection/reward authoring foundations expanded.
- [x] Phase 12: Indoor Map Authoring Basics - Indoor building/floor/tile/marker authoring basics and public runtime alignment landed.
- [ ] Planned Phase 13: User Progress, Operations, and System Control Plane - not executed as its own phase; absorbed into `v2.1` Phase 14.

</details>

<details>
<summary>[x] v1.0 Live Backend Cutover (Phases 1-6) - shipped 2026-04-13</summary>

- [x] Phase 1: Canonical Backend Foundation - Canonical contract, shared enums, schema foundation, and local smoke baseline completed.
- [x] Phase 2: Admin Control Plane Completion - Admin CRUD and UI surfaces aligned to canonical MySQL-backed mini-program domains.
- [x] Phase 3: Public Read APIs Cutover - Public catalog/runtime reads replaced mock-backed mini-program content flows.
- [x] Phase 4: Public Progress and Gameplay Writes - Traveler login, preferences, check-ins, progress, and rewards moved to live public APIs.
- [x] Phase 5: COS Media Pipeline - Authenticated admin uploads, canonical asset metadata, and Tencent COS-backed media delivery went live.
- [x] Phase 6: Migration, Cutover, and Hardening - Mock data migrated into MySQL, health visibility added, and end-to-end live smoke verification completed.

</details>

## Progress

| Milestone | Phase Range | Plans Complete | Status | Completed |
|-----------|-------------|----------------|--------|-----------|
| v1.0 Live Backend Cutover | 1-6 | 19/19 | Complete | 2026-04-13 |
| v2.0 Admin Control-Plane Reconstruction | 7-12 executed, 13 skipped | 15/15 executed plans | Archived with gaps | 2026-04-15 |
| v2.1 Interactive Rules Platform and AI Capability Center | 14-22 | 17/17 executed before audit; gap-closure phases 20-22 pending | Open - audit gaps routed to Phase 20 | - |
