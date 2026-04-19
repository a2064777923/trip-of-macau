# Phase 19: AI Capability Platform Redesign and Provider Model Orchestration - Context

**Gathered:** 2026-04-17
**Status:** Ready for AI-spec and planning

## Why Phase 19 Exists

Phase 18 delivered a real AI control-plane foundation, but it still presents operators with a single overloaded page, shallow provider forms, and too much JSON-first configuration. The target operator experience is materially larger:

- AI Capability Center must become a major top-level admin workspace.
- Each AI capability family needs its own dedicated sub-surface and explanation.
- Provider setup must feel like onboarding a real platform, not editing a flat row.
- Model selection, fallback, testing, sync, cost, and health must be visible together.
- Internal creative-assistance flows must be integrated into authoring forms and media history.

This phase is therefore an additive redesign on top of Phase 18, not a rollback.

## Locked Product Decisions

### Information architecture

- AI Capability Center remains a top-level admin section.
- The section must be split into dedicated sub-pages rather than one tab soup.
- The workspace should be information-dense, visually stronger, and more operator-oriented than the current Phase 18 page.
- Future AI modules may remain visible before full implementation, but each one must land on a dedicated page or dedicated placeholder.

### Capability domains

- The platform serves two major domains:
  - operator creative assistance inside admin authoring flows
  - traveler-facing service capabilities for the mini-program
- Translation is explicitly out of scope for the AI center because translation already belongs to separate translation tooling.
- The user-facing service domain currently includes:
  - itinerary recommendation planning
  - travel Q and A
  - photo recognition positioning
  - NPC voice dialogue
  - navigation assistance
- The first admin-facing creative flows to plan around are:
  - city cover and banner generation
  - POI overlay icon or illustration generation
  - story or content introduction copy assistance
  - speech synthesis for narration or guide audio

### Provider expectations

- The operator must be able to choose a provider platform template such as OpenAI, Bailian, Hunyuan, MiniMax, Volcengine, or Custom.
- Provider setup must support encrypted API credentials and base URL overrides.
- The platform should test connectivity and surface usable models or endpoints as automatically as the provider allows.
- The platform must not assume every provider supports the same runtime discovery endpoint.
- Model selection must support primary and fallback assignments per capability.
- Model configuration should be form-driven first, with JSON available only as expert mode.

### Observability and governance

- Operators need provider health, model sync status, usage, latency, and cost visibility.
- Cost visibility must be usable even where the provider does not expose a uniform billing API.
- Suspicious concurrency, fallback activation, and limit enforcement remain part of the platform.
- API keys and secrets must stay encrypted server-side and never be echoed back to the browser.

### Creative workbench

- Editing screens such as city, map, sub-map, POI, and story forms should expose an AI creation workbench entry.
- The workbench should preassemble prompt drafts from form data and target asset slots.
- Editors must be able to adjust prompts before generation.
- Generated candidates should be stored to COS with history.
- Admin can inspect all generation history; ordinary operators should see their own history only unless elevated.
- Editors need version restore and finalization back into the target form field.
- Image outputs may later support crop or keep-region tools; audio outputs may later support trim-range tools.

## Explicit Scope Boundary

Phase 19 focuses on the admin AI platform experience and its backend orchestration contracts.

In scope:

- admin information architecture redesign
- provider onboarding redesign
- normalized model inventory and sync strategy design
- capability to model routing and structured configuration
- observability, health, usage, and cost visibility
- creative workbench integration contracts and first admin workbench flows

Out of scope for this phase unless discovered as a direct dependency:

- full mini-program traveler-facing AI runtime rollout
- full photo positioning product delivery
- full approval workflow for AI-generated assets
- non-AI translation tooling

## Codebase Starting Point

- Current AI UI: `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`
- Current admin AI API surface centers on provider, policy, quota, prompt template, job, and log CRUD.
- The backend data model is still too flat for provider-aware model orchestration and capability-specific operator UX.
- The current page does not explain the capability roles clearly and does not reflect the future page hierarchy the user wants.

## Research Questions This Phase Must Answer

1. Which providers expose true model discovery APIs, and which require catalog or endpoint-style sync?
2. How should provider inventory normalization differ for raw model providers versus endpoint-based platforms?
3. How should cost visibility work when providers do not expose one consistent usage API?
4. What is the cleanest route and page decomposition for the AI center in the existing admin shell?
5. Which configuration fields should be structured by default, and which should remain advanced-mode JSON only?
