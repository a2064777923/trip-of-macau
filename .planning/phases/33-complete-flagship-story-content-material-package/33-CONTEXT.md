# Phase 33: Complete Flagship Story Content/Material Package - Context

**Gathered:** 2026-04-29
**Status:** Ready for planning
**Source:** Roadmap + prior Phase 28-32 handoffs + user-provided flagship story specification

<domain>
## Phase Boundary

Phase 33 completes the seeded flagship content/material package for `東西方文明的戰火與共生`.

This phase is not a new runtime architecture phase. It must reuse the existing v3.0 control-plane foundation:
- storylines and chapters from Phase 30
- reusable content blocks and Lottie-aware media assets from Phase 28
- experience flows, flow steps, bindings, and overrides from Phase 28-31
- game rewards, honor titles, reward rules, and reward presentations from Phase 23
- dynamic exploration elements from Phase 32
- admin media/content/experience workbenches as the authoring surfaces

The deliverable is a complete, inspectable, reusable content package, not a mini-program gameplay implementation. Phase 34 remains the public runtime and mini-program consumption baseline.
</domain>

<decisions>
## Implementation Decisions

### Flagship Story Scope

- The canonical storyline code is `east_west_war_and_coexistence`.
- The storyline title is `東西方文明的戰火與共生`.
- The storyline contains five main chapters:
  - Chapter 1: `鏡海初戰：中葡首次海防對峙`, anchor `poi:ama_temple`.
  - Chapter 2: `南灣防線：葡人築城的邊界博弈`, anchor `poi:lilau_square`.
  - Chapter 3: `山城戒備：教會與軍防的雙重佈局`, anchor `poi:st_lawrence_or_st_augustine_hill_context`; executor must resolve to the existing 崗頂前地/崗頂劇院 POI code in local data before seeding.
  - Chapter 4: `炮台硝煙：荷澳戰役的生死反擊`, anchor `poi:monte_fort`.
  - Chapter 5: `烽煙落幕：從對峙到文明共生`, anchor `poi:senado_square`.
- Each chapter must include mainline interactions, side pickups, hidden challenge, reward rules, honor titles, exploration elements, and material links.
- The final chapter must include the full-route finale challenge `濠江通史大師`.

### Content Model

- Historical basis and literary dramatization must remain separate fields in the material package and seed payloads.
- Any strong historical claim must be written as either:
  - a checked historical basis item, or
  - a clearly labeled literary reconstruction.
- Story content should be authored as reusable content blocks where possible, not only as `story_chapters.detail_*` fields.
- `prerequisite_json`, `completion_json`, and `reward_json` may stay populated for compatibility, but the main authoring source must be structured experience flows, steps, overrides, reward rules, and exploration elements.

### Asset and Manifest Model

- Every material generated or prepared for the story must appear in a manifest with:
  - stable manifest key
  - asset kind
  - local path
  - COS object key
  - `content_assets.id`
  - usage target
  - prompt or script provenance
  - historical basis / literary dramatization note when relevant
  - fallback asset reference for Lottie/audio/video
- Lottie remains JSON-only for this milestone.
- GIF remains an image asset. Lottie is `asset_kind='lottie'`.
- Large binary assets should not be casually committed unless they are intentionally lightweight fixtures. The manifest and SQL records are the source of traceability.

### Admin Visibility

- Operators must be able to find the package from the admin content area without inspecting raw SQL.
- A dedicated material package API and admin page are acceptable because Phase 33 introduces package-level provenance and manifest traceability not currently represented by `content_assets` alone.
- Existing media/content/experience pages must be reused for deep editing; the package page should aggregate, inspect, and navigate, not duplicate every editor.

### Exploration and Rewards

- Exploration progress must use `exploration_elements.weight_level` and `weight_value`; no fixed `+10%` style progress grants should be saved in flow effects.
- Reward effects should reference game rewards, honor titles, fragments, badges, and presentations through the existing reward domain where possible.
- Chapter completion, full collection, hidden challenge, side pickup, major media completion, and finale completion should each register exploration elements.

### UTF-8 Safety

- SQL seed files must start with `SET NAMES utf8mb4;`.
- Chinese story text, SQL, JSON, CSV, and scripted HTTP payloads must be written through UTF-8 files, not inline PowerShell literals.
- Smoke scripts should avoid embedding Chinese assertion strings directly when Windows encoding may corrupt them; use UTF-8 file reads or codepoint-safe assertions.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project and Phase Scope
- `.planning/ROADMAP.md` - Phase 33 goal, success criteria, and dependencies.
- `.planning/REQUIREMENTS.md` - `STORY-03` and `VER-02` requirement definitions.
- `.planning/STATE.md` - current milestone state and deferred mini-program boundary.
- `AGENTS.md` - project stack, UTF-8, security, and GSD workflow constraints.

### Prior v3.0 Foundation
- `.planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` - Phase 28 foundation boundaries and relation-link vocabulary.
- `.planning/phases/29-poi-default-experience-workbench/29-HANDOFF.md` - POI default experience pattern and A-Ma fixture.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-HANDOFF.md` - storyline inheritance/override handoff.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-HANDOFF.md` - reusable template and governance expectations.
- `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md` - dynamic exploration decisions and progress scope semantics.

### Current Schema and Services
- `scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql` - content block and Lottie-aware asset schema.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - experience flow, step, binding, override, and exploration foundation.
- `scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql` - current one-chapter flagship seed.
- `scripts/local/mysql/init/42-phase-31-interaction-template-governance.sql` - reusable interaction/task template seed.
- `scripts/local/mysql/init/43-phase-32-progress-engine.sql` - dynamic exploration denominator contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java` - content asset admin API pattern.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStorylineModeController.java` - story mode admin API pattern.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - experience/template/governance admin API pattern.
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` - admin route ownership.
- `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx` - admin sidebar IA.
</canonical_refs>

<specifics>
## Specific Ideas

### Required Package Contents

- Storyline introduction:
  - user role: `濠江歷史見證者`
  - core motif: `殘缺的海防銅鏡`
  - route: 媽閣廟 -> 亞婆井前地 -> 崗頂前地/崗頂劇院 -> 大炮台 -> 議事亭前地
- Per chapter:
  - mainline arrival/intro media
  - mainline overlay or interaction sequence
  - at least three side pickups or interaction items, except chapter 5 may include finale-only synthesis pickups
  - one hidden challenge
  - base reward, full-collection reward, hidden reward
  - honor titles for chapter completion, full collection, and hidden challenge
  - exploration elements with semantic weights
- Required named challenges:
  - `鏡海守護者`
  - `邊界見證者`
  - `山城暗哨`
  - `要塞保衛戰`
  - `濠江通史大師`
- Required finale rewards:
  - `濠江見證者`
  - `歷史還原大師`
  - `濠江通史掌門人`
  - `完整濠江戰火銅鏡`

### Material Types

- Images: storyline cover/banner, chapter hero images, pickup icon sheets, badge/title visuals, fallback poster images.
- Lottie JSON: route pulse, pickup shimmer, chapter completion, hidden challenge unlock, finale mirror synthesis.
- Audio: chapter narration scripts, ambient background/sfx entries, reward unlock sfx entries.
- Video: video entries may be represented by seeded metadata and fallback assets if real video production is not available in this phase.
- Text: script, historical basis, literary dramatization, prompts, AI generation notes.
</specifics>

<deferred>
## Deferred Ideas

- Full mini-program story runtime gameplay acceptance remains Phase 34 or later.
- Complex AR recognition, real puzzle gameplay, voice input gameplay, and route-recommendation algorithm implementation remain future scope.
- Full approval workflow remains future scope.
- `.lottie` packages, sprite sheets, sequence-frame animation systems, and advanced animation pipelines remain future scope.
</deferred>

---

*Phase: 33-complete-flagship-story-content-material-package*
*Context gathered: 2026-04-29*
