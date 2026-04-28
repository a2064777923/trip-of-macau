# Phase 28: Story and Content Control-Plane Completion - Research

**Researched:** 2026-04-28
**Status:** Ready for planning

## Research Summary

Phase 28 should be planned as the foundation slice for the user's v3.0 experience-orchestration redesign. The codebase already contains a first vertical slice for experience templates, flows, bindings, overrides, exploration elements, public runtime responses, and an admin workbench. The plan should therefore avoid rebuilding those pieces blindly and focus on hardening, filling missing contract gaps, validating schema/runtime behavior, and making the foundation reliable enough for later Phase 29-34 work.

## Current Implementation Surface

### Admin Backend

Existing entrypoint:

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java`

Current admin API group:

- `GET/POST/PUT/DELETE /api/admin/v1/experience/templates`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/flows`
- `POST/PUT/DELETE /api/admin/v1/experience/flows/{flowId}/steps`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/bindings`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/overrides`
- `GET/POST/PUT/DELETE /api/admin/v1/experience/exploration-elements`
- governance overview endpoint in service/API layer

Key support files:

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminExperienceOrchestrationService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminExperienceRequest.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminExperienceResponse.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExperienceTemplate.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExperienceFlow.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExperienceFlowStep.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExperienceBinding.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExperienceOverride.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExplorationElement.java`

### Public Backend

Existing entrypoint:

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java`

Current public runtime API group:

- `GET /api/v1/experience/poi/{poiId}`
- `POST /api/v1/experience/events`
- `GET /api/v1/storylines/{storylineId}/runtime`
- `POST /api/v1/storylines/{storylineId}/sessions/start`
- `POST /api/v1/storylines/{storylineId}/sessions/{sessionId}/events`
- `POST /api/v1/storylines/{storylineId}/sessions/{sessionId}/exit`
- `GET /api/v1/users/me/exploration`

Key support files:

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicExperienceService.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java`

### Admin UI

Existing workbench:

- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx`

The workbench already imports admin API helpers and exposes tabs for:

- templates
- flows
- bindings
- overrides
- exploration
- governance

Current UX direction is acceptable as a first foundation slice, but the plan should require:

- no raw JSON as the only path for basic setup
- better validation panels and schema-version hints
- meaningful cards/timeline for flow steps
- explicit Traditional Chinese labels
- route/sidebar placement under story/content management

### Database

Primary foundation script:

- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql`

Tables introduced:

- `experience_templates`
- `experience_flows`
- `experience_flow_steps`
- `experience_bindings`
- `experience_overrides`
- `exploration_elements`
- `user_exploration_events`
- `user_exploration_state`

Story chapter columns added:

- `experience_flow_id`
- `override_policy_json`
- `story_mode_config_json`

Related media/story script:

- `scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql`

That script should remain part of Phase 28 verification because Lottie/content blocks are part of the foundation, but full material production belongs to later Phase 33.

## What the Plan Should Build

### 1. Foundation Hardening, Not Rebuild

The executor should first audit the existing Phase 28 slice, then patch gaps. The plan should not instruct a blind new implementation of tables/controllers/components that already exist. The implementation goal is to make this slice reliable, documented, smoke-testable, and aligned with the user's replacement model.

Concrete areas to verify/harden:

- All new JSON fields save only object payloads with `schemaVersion: 1`.
- Template `template_type`, flow `flow_type`, owner types, override modes, trigger types, and weight levels use consistent enums/options across SQL, Java DTOs, admin UI, and public DTOs.
- Soft delete and status filtering are consistent.
- Deleting flows does not leave broken bindings/steps.
- Public runtime only exposes published or otherwise runtime-eligible rows.
- Public event recording is idempotent by `clientEventId`.
- Dynamic exploration calculation uses `exploration_elements` denominator and completed `user_exploration_events`, not fixed progress increments.

### 2. Roadmap/Requirement Reconciliation

The v3.0 roadmap still says Phase 28 is story/chapter/content-block completion, while the context says the user replaced it with the experience-orchestration foundation. The plan should include a docs/planning task to reconcile roadmap/requirements language if allowed by the workflow, without hiding the old STORY-01 to STORY-04 coverage.

The Phase 28 plan should still cover the spirit of STORY-01 to STORY-04:

- STORY-01 through canonical bindings across maps/sub-maps/POIs/indoor/rewards/rules.
- STORY-02 through chapter anchors, inherited flows, overrides, prerequisites/completion/effects as versioned runtime payloads.
- STORY-03 through content block/media/Lottie foundations and reusable asset references.
- STORY-04 through dedicated admin pages/routes instead of placeholders.

### 3. Admin Workbench Usability

The first workbench should be treated as the foundation editor. It does not need the full Phase 29 POI-specific editor or Phase 30 story override editor, but it must support basic creation and inspection without requiring the operator to hand-write every JSON field.

Minimum plan-level improvements:

- Preset buttons/templates for common step payloads:
  - intro modal
  - route guidance
  - proximity full-screen media
  - check-in task
  - pickup
  - hidden challenge
  - reward grant
- Visible validation result panel for schemaVersion and required fields.
- Governance overview that shows counts and high-risk conflicts.
- Exploration element editor with semantic weights and published denominator explanation.
- Clear separation of foundation workbench tabs versus future POI/story-specific workbenches.

### 4. Public Runtime Contract

The public backend should compile runtime flows into DTOs. The mini-program must not need to understand raw admin forms.

Important contract properties:

- Flow response includes steps sorted by `sort_order`.
- Step response includes stable `stepCode`, `stepType`, trigger/effect/condition objects, media asset summary, reward rule ids, required-for-completion flag, and exploration weight level.
- Storyline runtime response includes story mode config, chapters, anchor info, inherited flow, override-applied flow where available, and content block/media summaries.
- Event recording accepts click/proximity/dwell/content-complete/task-complete/pickup style event types.
- Exploration response includes available weight, completed weight, progress percent, and item summaries by scope.

### 5. SQL and Seed Safety

The project has repeated Chinese mojibake history. Phase 28 seed and repair work must keep SQL as UTF-8/utf8mb4 files and avoid inline PowerShell Chinese literals.

Plan should require:

- `SET NAMES utf8mb4` or equivalent connection settings in smoke scripts where applicable.
- No API keys or provider secrets in SQL/docs.
- MySQL-compatible guarded ALTER statements rather than unsupported `ADD COLUMN IF NOT EXISTS`.
- Seed data should be useful but bounded: enough to show the 媽閣廟 POI default flow and the first story override slice, not the full five-chapter asset package.

### 6. Security Threat Model

Security enforcement is enabled at ASVS L1 and each plan should include a `<threat_model>` block.

Threats to address:

- Broken access control on admin `/api/admin/v1/experience/**` endpoints.
- Public runtime leaking draft/unpublished flow data.
- Public event ingestion allowing spoofed users or duplicate event inflation.
- Raw JSON fields storing malformed, unexpectedly large, or non-object payloads.
- Future SSRF/file risks from media URLs should stay in media subsystem, not direct arbitrary fetches in experience APIs.
- Secrets must not be hardcoded in seed manifests or docs.

## Recommended Plan Breakdown

Phase 28 should be split into 4 executable plans:

1. **Schema and backend contract hardening**
   - SQL/idempotency/enums/schemaVersion validators.
   - Admin backend consistency.
   - Public runtime contract filtering and dynamic exploration calculation.

2. **Admin workbench foundation UX**
   - Improve existing `ExperienceOrchestrationWorkbench.tsx`.
   - Add presets, validation, governance, and Traditional Chinese operator copy.
   - Ensure routes/sidebar land correctly.

3. **Runtime smoke and seed alignment**
   - Ensure seed data for 媽閣廟 default flow and first story override is present and UTF-8-safe.
   - Add smoke script(s) for admin/public APIs and SQL verification.
   - Verify admin backend, public backend, admin UI build.

4. **Planning artifact reconciliation and future phase handoff**
   - Update roadmap/requirements or add explicit handoff notes so Phase 29-34 are not lost.
   - Document what remains for POI workbench, story override workbench, template governance, dynamic progress UI, content package, and mini-program runtime.

## Verification Commands

Use these commands from `D:/Archive/trip-of-macau`:

```powershell
mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml
mvn -q -DskipTests compile -f packages/server/pom.xml
npm run build --prefix packages/admin/aoxiaoyou-admin-ui
```

If local services are running, smoke-test:

```powershell
Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:8080/api/v1/experience/poi/9?locale=zh-Hant"
Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:8080/api/v1/storylines/8/runtime?locale=zh-Hant"
```

Admin API checks require login token and should verify `/api/admin/v1/experience/templates`, `/flows`, `/bindings`, `/overrides`, `/exploration-elements`, and governance overview.

## Validation Architecture

Plan verification should check:

- Schema files contain all foundation tables and guarded story chapter ALTERs.
- Java compile passes for both admin and public backends.
- Admin UI build passes.
- Public runtime DTOs return compiled flows with ordered steps.
- Admin workbench exposes tabs for templates, flows, bindings, overrides, exploration, and governance.
- Phase 28 plan files mention STORY-01, STORY-02, STORY-03, and STORY-04 or explicitly explain how the replacement context covers them.
- No Chinese seed content is introduced through inline PowerShell literals.

## RESEARCH COMPLETE
