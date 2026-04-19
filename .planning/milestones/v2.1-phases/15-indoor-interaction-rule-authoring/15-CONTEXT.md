# Phase 15: Indoor Interaction Rule Authoring - Context

**Gathered:** 2026-04-15
**Status:** Ready for planning
**Source:** v2.1 roadmap scope, the existing Phase 12 indoor foundation, and the user's indoor-interaction authoring requirements from the current milestone

<domain>
## Phase Boundary

This phase turns the current indoor marker editor into a real authoring platform for indoor interaction rules.

It covers:
- canonical authoring and persistence for indoor appearance conditions, trigger chains, effect definitions, and path-based motion metadata
- structured admin UX for markers and overlays so operators no longer author rule behavior through raw JSON only
- validation-safe storage that the next phase can execute through the public backend and mini-program runtime
- showcase seed content and verification for authoring persistence on the existing Lisboa indoor demo

It does not cover:
- public runtime evaluation of the authored rules
- mini-program execution of trigger and effect rules
- final support guarantees for every possible trigger type in live runtime
- unrelated indoor building or tile-import redesign outside the rule-authoring scope
</domain>

<decisions>
## Implementation Decisions

### Authoring Boundary
- Phase 15 stops at authoring, validation, persistence, and admin proof. Actual runtime evaluation belongs to Phase 16.
- The authoring model must distinguish between "authorable now" and "runtime-supported in Phase 16" so the admin can store richer future-facing rules without pretending the mini-program already executes them.

### Canonical Rule Model
- Indoor interaction authoring must stop relying on raw `tagsJson`, `popupConfigJson`, `displayConfigJson`, and `metadataJson` as the primary operator model.
- The canonical rule model must represent:
  - appearance conditions
  - trigger conditions
  - prerequisite trigger chains
  - effect lists
  - path geometry and motion timing
  - inheritance from linked entities such as task, activity, collectible, badge, or event
- Raw JSON may remain as an advanced escape hatch, but only after the structured editors and typed validators exist.

### Marker And Overlay Scope
- Indoor nodes must support both marker-style and overlay-style authoring on the same floor workspace instead of assuming every authored object is only a point marker.
- Marker and overlay authoring must share the same canonical rule concepts so later runtime evaluation does not need a second rule system.
- Overlay geometry must be stored in normalized floor coordinates so it stays compatible with the existing tile-manifest contract from Phase 12.

### Condition, Trigger, And Effect Coverage
- Structured appearance condition categories must cover at least:
  - schedule window
  - recurring calendar
  - user-progress or account-attribute predicates
  - dwell-time or scene-stay predicates
  - proximity predicates
  - always-on or manual visibility
- Structured trigger condition categories must cover at least:
  - click or tap
  - proximity
  - dwell duration
  - drag interaction
  - voice or shout placeholder configuration
  - custom future-safe trigger hooks
- Structured effect categories must cover at least:
  - popup or bubble presentation
  - media playback
  - path-based motion animation
  - reward, collectible, badge, task, or account-state mutation hooks
  - authored data adjustments or chained reveal behavior

### Authoring Experience
- The indoor editor must remain visual-first: floor thumbnail selection, draft recovery, and preview-first CSV flows from Phase 12 stay in place and are extended rather than replaced.
- Path-based behaviors must be drawable on the floor preview instead of authored as freehand coordinate JSON only.
- Presets and templates must be reusable from the editor so common interaction patterns are easier to compose than raw custom payloads.

### Showcase And Verification
- The Lisboa indoor demo already in the repo remains the showcase baseline for Phase 15 seed data and authoring verification.
- Verification must prove that authored rule graphs round-trip through admin persistence and reload correctly in the indoor editor before the project proceeds to runtime evaluation.

### The Agent's Discretion
- Whether the canonical rule graph is stored through one behavior-profile table, several behavior tables, or a hybrid additive JSON-plus-typed-DTO approach
- Exact controller and DTO naming, provided the indoor rule model remains additive and future runtime-safe
- The exact preset catalog shipped in Phase 15, provided the major authoring categories above are covered
</decisions>

<canonical_refs>
## Canonical References

**Downstream planners and executors should read these first.**

### Roadmap and requirements
- `.planning/ROADMAP.md` - v2.1 phase goal, requirement mapping, and success criteria
- `.planning/REQUIREMENTS.md` - `RULE-01`, `RULE-02`, and the authoring/runtime split with `RULE-03`

### Indoor baseline from the archived milestone
- `.planning/milestones/v2.0-phases/12-indoor-map-authoring-basics/12-01-SUMMARY.md` - indoor building and floor authoring baseline
- `.planning/milestones/v2.0-phases/12-indoor-map-authoring-basics/12-02-SUMMARY.md` - tile import and zoom-derivation baseline
- `.planning/milestones/v2.0-phases/12-indoor-map-authoring-basics/12-03-SUMMARY.md` - marker CRUD, CSV preview, minimap picking, and public indoor baseline
- `scripts/local/mysql/init/18-phase-12-indoor-markers-and-runtime.sql` - current canonical indoor marker schema extension

### Current admin backend indoor authoring surface
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/IndoorNode.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminIndoorMarkerUpsertRequest.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/IndoorMarkerAuthoringService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminIndoorController.java`

### Current admin UI indoor authoring surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/MapTileManagement.tsx` - current marker editor, CSV preview, and minimap picking workspace

### Current public and mini-program indoor runtime boundary
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/IndoorMarkerResponse.java`
- `packages/client/src/pages/map/indoor/index.tsx`
</canonical_refs>

<specifics>
## Specific Ideas

- Author at least three meaningful Lisboa showcase behaviors during execution:
  - a schedule-gated concierge or performance overlay
  - a dwell-triggered story or collectible reveal
  - a chained interaction with a path-based motion effect
- Keep the current floor canvas as the main authoring surface and add dedicated sections for:
  - node basics
  - appearance
  - triggers
  - effects
  - path and geometry
- Surface authoring warnings for rules that are stored in Phase 15 but not yet executable in Phase 16.
</specifics>

<deferred>
## Deferred Ideas

- Public API delivery of executable indoor rule graphs and any runtime-safe rule filtering belongs to Phase 16.
- Mini-program trigger evaluation, animation playback, and account-state side effects belong to Phase 16.
- Full AI-assisted indoor authoring belongs to later work, not this phase.
</deferred>

---

*Phase: 15-indoor-interaction-rule-authoring*
*Context gathered: 2026-04-15 from the current indoor baseline and milestone requirements*
