# Phase 28: Story and Content Control-Plane Completion - Context

**Gathered:** 2026-04-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 28 is superseded by the user's v3.0 story-experience redesign. It is no longer a narrow "storyline + chapter + content block" CRUD phase. The phase now delivers the shared foundation for a reusable experience orchestration system:

- POI, indoor building, indoor floor, indoor node, story chapter, activity, and manual targets can bind to default experience flows.
- Story chapters inherit a target's default experience and can disable, replace, or append specific steps through structured overrides.
- Interaction/display/task/game/reward behavior is expressed through reusable templates and versioned JSON payloads, not raw JSON-first forms.
- Exploration progress uses registered exploration elements and configurable weights instead of hardcoded percentage increases.
- Admin and public backend contracts must be aligned enough for later story runtime and mini-program consumption.

The full end-user mini-program story runtime remains future milestone depth, but Phase 28 must establish the data model, backend contracts, and first admin workbench slice that later phases can extend without rebuilding the foundation.

</domain>

<decisions>
## Implementation Decisions

### Story Experience Model
- **D-01:** A POI or spatial target can define its own default experience flow for natural walk-in and "go explore this place" behavior.
- **D-02:** A story chapter binds to a POI, indoor building, indoor floor, indoor node, task, marker, overlay, or manual anchor, then inherits that anchor's default experience unless overridden.
- **D-03:** Storyline mode is an independent orchestrated mode, not just a sequence of content pages. It can hide unrelated map content, highlight current chapter routes, show greyed future routes, and clear temporary session progress on exit while preserving permanent exploration events and rewards.
- **D-04:** Chapter-level overrides must support `inherit`, `disable`, `replace`, and `append` semantics per flow step.

### Reusable Templates and Rules
- **D-05:** Interaction behavior must be reusable across POI, indoor, story, rewards, and future activities. The foundation should support template categories for display, appearance conditions, trigger conditions, trigger effects, task gameplay, and reward presentation.
- **D-06:** Existing indoor node behaviors are not discarded. They should be treated as a specialized implementation that can be surfaced or bridged into the generalized experience-template model.
- **D-07:** Advanced JSON may exist for expert fallback, but operator-facing admin workbenches should default to cards, timelines, selectors, and validation panels.
- **D-08:** JSON persisted for conditions, effects, overrides, and compiled runtime data must be schema-versioned and validated before saving.

### Dynamic Exploration Progress
- **D-09:** Exploration progress must not be stored as fixed additions such as `+10%`. Admins configure semantic weights such as `tiny`, `small`, `medium`, `large`, and `core`.
- **D-10:** Region/story/user progress is calculated dynamically from published exploration elements: completed weighted elements divided by currently published weighted elements.
- **D-11:** Content additions, removals, and lifecycle changes may change displayed percentages without deleting already completed user events.
- **D-12:** Storyline session progress is separate from permanent exploration state. Exiting story mode may clear temporary story session progress but must not remove completed check-ins, pickups, rewards, or exploration events.

### Admin UX Direction
- **D-13:** Workbenches should use a three-panel orchestration layout: left tree or step list, middle visual flow/composition area, right property panel, with bottom validation and conflict hints.
- **D-14:** All admin UI copy must remain Traditional Chinese.
- **D-15:** Form validation should keep the existing project expectation: scroll to invalid fields, focus them, and apply a brief shake/error affordance.
- **D-16:** The admin experience should let operators configure the user's "媽閣廟" example without hand-writing JSON: intro popup, route planning, proximity full-screen media, check-in tasks, pickup items, hidden achievement, and reward/title grants.

### Assets and Media
- **D-17:** Lottie is the preferred animation asset type, with GIF/video fallback compatibility.
- **D-18:** Lottie first pass supports JSON assets only. `.lottie` packages, sprite sheets, and sequence-frame systems are out of scope for this foundation.
- **D-19:** Lottie assets must be treated as globally reusable media assets so story, POI, indoor, reward, badge, title, and interaction modules can all select and preview them later.
- **D-20:** AI-generated and manually prepared story assets must be tracked through a manifest that records local file, COS object key, content asset id, prompt/script, and usage target.

### Seeded Narrative Direction
- **D-21:** The flagship seeded story package is "東西方文明的戰火與共生", built around five Macau chapters: 媽閣廟, 亞婆井前地, 崗頂前地, 大炮台, and 議事亭前地.
- **D-22:** Story text may be literary, but the admin model must preserve a distinction between historical basis and literary dramatization.
- **D-23:** Each chapter should be able to express mainline media, required interactions, side pickups, hidden challenges, reward grants, honor titles, and route/anchor bindings.
- **D-24:** The complete asset generation package belongs to a later content-production phase, but Phase 28 must not block that package from being represented in the data model.

### Roadmap Realignment
- **D-25:** The user's replacement plan redefines the next story-experience phases as:
  - Phase 28: experience orchestration foundation and data model.
  - Phase 29: POI default experience workbench.
  - Phase 30: storyline mode and chapter override workbench.
  - Phase 31: interaction/task template library and governance center.
  - Phase 32: dynamic exploration and user progress model.
  - Phase 33: complete "東西方文明的戰火與共生" content/material package.
  - Phase 34: public runtime and mini-program consumption baseline.
- **D-26:** The existing v3.0 roadmap file still lists the older Phase 28-32 shape; downstream planning should treat this context as the user-approved replacement direction and update roadmap artifacts when the GSD workflow allows it.

### the agent's Discretion
- The exact Java DTO class boundaries, mapper organization, and service decomposition can follow existing Spring Boot/MyBatis-Plus project patterns.
- The exact Ant Design component choices and visual styling details can be decided by the implementing agent as long as the UI remains a real workbench, not a table-only CRUD page.
- Conflict detection can start with high-value rule checks and expand in later phases, provided the schema leaves room for richer checks.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Scope and Constraints
- `.planning/PROJECT.md` - v3.0 milestone boundary, stack constraints, admin/public ownership, UTF-8 and secret-handling requirements.
- `.planning/REQUIREMENTS.md` - v3.0 requirement map and current STORY/USER/OPS/LINK/VER requirement definitions.
- `.planning/ROADMAP.md` - current roadmap baseline that needs to be reconciled with the user's replacement Phase 28-34 story-experience roadmap.
- `AGENTS.md` - project instructions, UTF-8 rule, MySQL/COS constraints, GSD workflow requirement, and verification expectations.

### Existing Experience, Story, Media, Reward, and Indoor Code
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Experience/ExperienceOrchestrationWorkbench.tsx` - current Phase 28 foundation workbench slice.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - admin experience orchestration API entrypoint.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - admin orchestration persistence and DTO assembly.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public experience runtime API entrypoint.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - public compiled runtime flow behavior.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/StoryContentBlockManagement.tsx` - reusable story content-block admin surface.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Content/MediaLibraryManagement.tsx` - global media/resource admin surface.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` - existing indoor rule governance UI to aggregate rather than duplicate.
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardRuleCenter.tsx` - existing reward rule center to reuse for reward/effect synchronization.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - mini-program Lottie baseline component for future runtime consumption.

### Database and Seed Scripts
- `scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql` - story content block and Lottie/media schema expansion.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - experience orchestration, binding, override, and exploration element foundation.

### External Platform References
- `https://github.com/wechat-miniprogram/lottie-miniprogram` - official WeChat mini-program Lottie player reference.
- `https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api` - Aliyun Model Studio CosyVoice non-realtime TTS API reference for later story audio generation.
- `https://help.aliyun.com/zh/model-studio/cosyvoice-clone-api` - Aliyun Model Studio CosyVoice voice clone API reference for later custom voice assets.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ExperienceOrchestrationWorkbench.tsx`: already provides a first admin UI slice for templates, flows, bindings, overrides, exploration elements, and governance; Phase 29-31 should extend it or split it into focused workbenches rather than rebuild from scratch.
- `AdminExperienceOrchestrationController` and `AdminExperienceOrchestrationServiceImpl`: already establish admin CRUD/query patterns for the new experience tables.
- `ExperienceController` and `PublicExperienceServiceImpl`: already establish public runtime endpoints for POI experience, storyline runtime, event reporting, story sessions, and user exploration summary.
- `StoryContentBlockManagement` and `MediaLibraryManagement`: existing story content and global media surfaces should be reused for content blocks, Lottie, audio, video, and attachment selection.
- `IndoorRuleCenter`, `MapTileManagement`, and indoor node behavior types: existing indoor interaction rules should be bridged into the generalized template/governance model.
- `RewardRuleCenter`, `GameRewardManagement`, honor/badge/reward components: reward grants, titles, presentations, and acquisition conditions should be linked rather than modeled again.
- `LottieAssetPlayer`: existing mini-program Lottie baseline should remain the consumption target for story/runtime DTOs.

### Established Patterns
- Admin UI uses explicit routes in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`, feature pages under `src/pages`, Axios helpers in `src/services/api.ts`, and Ant Design components.
- Admin backend and public backend both use Spring Boot, MyBatis-Plus mapper interfaces, DTO request/response packages, service interfaces, and `ApiResponse<T>` wrappers.
- Local MySQL init scripts under `scripts/local/mysql/init` are the canonical way to seed schema and fixture data for local verification.
- The repo has a known dirty worktree; implementation must stay scoped and avoid reverting unrelated changes.
- Chinese text and seed content must be written via UTF-8/utf8mb4-safe files or SQL imports, not inline PowerShell Chinese literals.

### Integration Points
- Admin routes under `故事與內容管理` should expose dedicated experience, POI, chapter, template, governance, exploration, and material-package workbenches as phases progress.
- Public runtime APIs should return compiled flow DTOs; the mini-program should not need to interpret raw admin form payloads.
- Content relation links should remain the cross-domain binding layer where possible, with relation types for default experience flows, story anchors, overrides, reward bindings, and exploration element bindings.
- COS asset metadata in `content_assets` remains the common media source for Lottie, image, audio, video, and fallback assets.

</code_context>

<specifics>
## Specific Ideas

- The user's POI example for 媽閣廟 should be representable as a default POI flow: click intro popup, "前往探索該地" button, route drawing, traffic/story/nearby recommendations, proximity full-screen animation, check-in tasks, cyber incense gameplay, post-game animation, coin/title/badge rewards, and dwell-time hidden achievement.
- Story chapters should inherit that POI flow and selectively override it. For example, a story chapter can disable 媽閣廟's default arrival animation and replace it with a story-specific immersive animation while adding story-only title and item rewards.
- "東西方文明的戰火與共生" must eventually support five chapters with mainline interactions, side pickups, hidden challenges, reward configurations, and honor titles:
  - 鏡海初戰：中葡首次海防對峙 - 媽閣廟.
  - 南灣防線：葡人築城的邊界博弈 - 亞婆井前地.
  - 山城戒備：教會與軍防的雙重佈局 - 崗頂前地.
  - 炮台硝煙：荷澳戰役的生死反擊 - 大炮台.
  - 烽煙落幕：從對峙到文明共生 - 議事亭前地.
- Exploration weights should be operator-configured semantically (`少量`, `中量`, `大量`, `核心`) and mapped to system weights centrally rather than stored as direct percentage changes.
- Material production should use a manifest-first pipeline so image boards, cut assets, audio scripts, Lottie specs, COS keys, and database asset ids stay traceable.

</specifics>

<deferred>
## Deferred Ideas

- Complete mini-program story gameplay, AR/photo recognition, speech input, route coverage, puzzle games, cannon defense, and rich story-mode map rendering are future runtime phases after admin/backend contracts are stable.
- Full AI generation of image/audio/Lottie assets and COS upload for the flagship story package belongs to the material-production phase, not the foundation phase.
- Publishing approval workflow beyond immediate status/lifecycle controls remains future work unless it becomes necessary for v3.0 closure.

</deferred>

---

*Phase: 28-story-and-content-control-plane-completion*
*Context gathered: 2026-04-28*
