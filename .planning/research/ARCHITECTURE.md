# v3.0 Research - Architecture

## Integration Points

### Admin UI

- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
  New route ownership for story/content, user/progress, and testing/operations pages must stay explicit and not reuse unrelated modules.
- Existing page groups under `src/pages`
  New domain pages should follow the established functional-page pattern and reuse the existing request layer and auth store.
- Existing indoor, map, reward, and AI surfaces
  The new pages should consume shared selectors and reference contracts instead of redefining local entity models.

### Admin Backend

- Story/content services
  Need canonical CRUD plus composition endpoints for storylines, chapters, content blocks, preview payloads, and linked bindings.
- User/progress services
  Need traveler drill-down endpoints, progress projections, recompute actions, and timeline/history queries.
- Testing/ops services
  Need lifecycle mutation endpoints, dashboard summaries, smoke actions, and failure/activity summaries.
- Shared domain references
  Need canonical lookup services reused across story, reward, indoor, map, and operations flows.

### Public Backend

- Story/runtime read contracts
  Must consume any new live story-domain data that already affects traveler runtime behavior.
- User-progress contracts
  Must stay aligned with any recompute logic or availability logic that becomes canonical in admin.

## Recommended Build Order

1. Finish story/content authoring model and UI ownership.
2. Build user/progress projections and drill-down surfaces on top of canonical source data.
3. Build testing/operations and lifecycle controls after the above domains expose canonical states.
4. Align shared linkage and public contracts once the domain shapes are no longer moving.
5. Seed meaningful examples and verify the linked stack end to end.

## Architectural Risk

The main architectural risk is not complexity of any single page. It is allowing each new admin domain to invent its own linkage model, status semantics, and query shape. `v3.0` should treat shared references and derived-state ownership as core architecture, not cleanup.
