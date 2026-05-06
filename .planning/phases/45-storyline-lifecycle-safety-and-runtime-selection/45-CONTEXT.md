# Phase 45: Storyline Lifecycle Safety and Runtime Selection - Context

**Gathered:** 2026-05-06
**Status:** Ready for planning
**Source:** User report, local admin/public backend code audit, public API smoke checks, and MySQL dependency snapshot

<domain>
## Phase Boundary

Phase 45 is a focused hotfix phase for duplicate storylines and unsafe deletion behavior. It does not add new story gameplay mechanics. It makes the admin storyline lifecycle safe enough for real configured content, ensures the old duplicate storyline can be removed from traveler-facing surfaces without orphaning data, and makes the mini-program select the intended flagship story `東西方文明的戰火與共生`.

The immediate duplicate records are:
- Old duplicate: `id=8`, `code=macau_fire_route`, `name=濠江烽煙：東西方文明的戰火與共生`, `status=published`, `sort_order=0`.
- New intended story: `id=9`, `code=east_west_war_and_coexistence`, `name=東西方文明的戰火與共生`, `status=published`, `sort_order=3300`.

Observed public behavior:
- `GET /api/v1/storylines?locale=zh-Hant` returns both records, with `id=8` first because of `sort_order=0`.
- `GET /api/v1/storylines/8/runtime?locale=zh-Hant` returns success.
- `GET /api/v1/storylines/9/runtime?locale=zh-Hant` returns success.
- `GET /api/v1/storylines/10/runtime?locale=zh-Hant` returns `4042 Storyline not found`.
- Mini-program `pages/story/index.tsx` currently treats both `macau_fire_route` and `east_west_war_and_coexistence` as flagship codes and chooses the first matching story, so it can prefer the old duplicate.
</domain>

<findings>
## Investigation Findings

### Current Delete Logic

- Admin UI has a delete button in `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx`.
- The UI confirmation only says the story will need chapters and related content re-authored.
- UI calls `deleteAdminStoryline(storylineId)`.
- `deleteAdminStoryline` calls `DELETE /api/admin/v1/storylines/{storylineId}`.
- `AdminStoryLineController.delete` delegates to `adminStoryLineService.delete(storylineId)`.
- `AdminStoryLineServiceImpl.delete` currently only does:
  - `requireStoryline(storylineId)`
  - `storyLineMapper.deleteById(storylineId)`

This is an unsafe physical delete. It has no dependency impact check, no soft archive path, no user-session protection, no relation cleanup, and no audit record.

### Dependency Snapshot

The old duplicate `id=8` is not an empty record:
- `story_chapters`: 5 chapters.
- `content_relation_links`: city/sub-map/attachment links and many target-side storyline bindings from activities, badges, collectibles, rewards, game rewards, redeemable prizes, and material packages.
- `exploration_elements`: 6 elements.
- `user_storyline_sessions`: 4 sessions observed, including started sessions.

The new story `id=9` also has active runtime data:
- `story_chapters`: 5 chapters.
- `exploration_elements`: 37 elements.
- `user_storyline_sessions`: 19 sessions observed.

Hard deleting either record would leave orphaned cross-domain references unless all dependencies are explicitly evaluated and handled.
</findings>

<decisions>
## Implementation Decisions

- **D45-01:** Published or previously-played storylines must not be hard-deleted directly from the normal admin UI. The primary operation should be `archive` / `unpublish from public runtime`.
- **D45-02:** Physical deletion is allowed only for draft storylines with no chapters, no runtime flows/bindings/overrides, no relation links, no exploration elements, no material package usage, and no user sessions/events.
- **D45-03:** The old duplicate `macau_fire_route` should be archived/unpublished first, not hard-deleted, so old user sessions and audit/debug traces remain inspectable.
- **D45-04:** The public story list must not expose archived/deleted/unpublished storylines, and the mini-program should no longer include `macau_fire_route` in flagship selection.
- **D45-05:** If a stale mini-program route references an archived/missing storyline, the traveler UI should show a controlled Traditional Chinese "故事線已下線或不可用" message and let the user return to the current story list instead of a generic server failure.
- **D45-06:** Any one-off data correction must be captured as an idempotent UTF-8 SQL/script under `scripts/local` or a documented admin API operation. Do not mutate Chinese data through inline PowerShell literals.
</decisions>

<canonical_refs>
## Canonical References

### Admin Storyline Lifecycle
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryLineController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminStoryLineService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStoryLineServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement/index.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`
- `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`

### Runtime Selection and Public Story APIs
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java`
- `packages/client/src/pages/story/index.tsx`
- `packages/client/src/pages/index/index.tsx`
- `packages/client/src/services/gameService.ts`
- `packages/client/src/services/api.ts`

### Data Tables To Inspect
- `storylines`
- `story_chapters`
- `story_chapter_block_links`
- `story_content_blocks`
- `content_relation_links`
- `content_asset_links`
- `experience_flows`
- `experience_flow_steps`
- `experience_bindings`
- `experience_overrides`
- `exploration_elements`
- `user_exploration_events`
- `user_storyline_sessions`
- `story_material_packages`
- `story_material_package_items`
- `story_material_package_item_versions`
- reward, badge, collectible, activity, prize binding tables or relation-link rows
</canonical_refs>

<specifics>
## Specific Requirements

- Add a backend impact-check response before destructive storyline actions.
- Change the admin delete button into a lifecycle action drawer/modal that explains:
  - public status impact,
  - dependency counts,
  - whether hard delete is allowed,
  - recommended action,
  - exact next status.
- Provide `Archive / 下線` as the default action for published storylines.
- Require typed confirmation only for allowed hard delete.
- Ensure `macau_fire_route` is no longer treated as the flagship story in mini-program selection.
- Ensure new story `east_west_war_and_coexistence` is selected when no explicit story ID is provided.
- Add smoke checks for:
  - old story archived/unpublished no longer appears in public list,
  - new story appears and runtime endpoint succeeds,
  - stale old story runtime/detail returns controlled non-success without generic crash,
  - admin impact check blocks hard delete when dependencies exist.
</specifics>

<deferred>
## Deferred Ideas

- Full content migration/merge tooling between storylines is not required in this hotfix.
- User progress transfer from old storyline to new storyline is not automatic in this phase unless explicitly added after review.
- Full approval workflow remains future release governance work.
- Advanced mini-program story gameplay presentation remains in existing/future traveler runtime phases.
</deferred>

---

*Phase: 45-storyline-lifecycle-safety-and-runtime-selection*
*Context gathered: 2026-05-06*
