# Phase 17 Research: Indoor Runtime Evaluation and Mini-Program Alignment

## Objective

Research how to execute the authored Phase 15 and Phase 16 indoor interaction rules safely through `packages/server` and the mini-program indoor runtime without inventing a second rule model or bypassing the real-auth constraints already locked in Phase 7.

## Current Starting Point

- Phase 15 already established the canonical authoring model:
  - additive `indoor_nodes` fields for presentation mode, overlay type, overlay geometry, and runtime support
  - `indoor_node_behaviors` as the typed behavior-profile table
  - structured payloads in `AdminIndoorNodeBehaviorPayload`
  - validation rules in `IndoorMarkerAuthoringService`
- Phase 16 already established operator-scale tooling:
  - a dedicated rule workbench
  - a governance center with conflict visibility
  - deterministic Lisboa showcase behaviors on `lisboeta_macau`
- The public backend is still static for indoor data:
  - `IndoorController` only exposes building, floor, and marker endpoints
  - `PublicIndoorServiceImpl` returns `IndoorBuildingResponse`, `IndoorFloorResponse`, and `IndoorMarkerResponse`
  - `packages/server` does not yet expose `indoor_node_behaviors` or any indoor runtime-evaluation endpoint
- The mini-program indoor page is also still static:
  - `packages/client/src/pages/map/indoor/index.tsx` renders tiles and static markers only
  - there is no behavior visibility engine, trigger chain engine, path-motion playback contract, or effect dispatcher
  - `packages/client/src/services/api.ts` only knows static floor and marker DTOs
- Auth constraints are already locked:
  - anonymous users may browse read-only content
  - interactive or stateful actions must require WeChat login through `requireAuth(...)`
- The repository already has local proof infrastructure patterns:
  - server tests in `packages/server/src/test/java`
  - admin-side smoke harnesses such as `scripts/local/smoke-phase-12-indoor.ps1`, `scripts/local/smoke-phase-15-indoor-authoring.ps1`, and `scripts/local/smoke-phase-16-indoor-rule-governance.ps1`

## What Phase 17 Must Resolve

1. Project the authored indoor behavior graph into a safe public runtime contract.
2. Decide what the backend evaluates authoritatively versus what the mini-program evaluates locally for responsiveness.
3. Honor the anonymous-read-only rule when indoor effects want to mutate user state.
4. Keep the Lisboa showcase fixtures deterministic so smoke and UAT can prove real runtime behavior.
5. Avoid drifting from the Phase 15 and Phase 16 write model while still making the runtime executable.

## Key Gaps

### 1. Public schema gap

`packages/server` does not yet have:
- an `IndoorNodeBehavior` entity
- a mapper for `indoor_node_behaviors`
- public DTOs for behavior profiles, path graphs, or overlay geometry

This means the public backend cannot currently read the canonical authored runtime graph at all.

### 2. Public contract gap

The current indoor public contract is optimized for static rendering:
- building metadata
- floor tile metadata
- marker coordinates and visual config

It cannot express:
- per-behavior runtime support state
- typed appearance rules
- typed trigger chains
- typed effect definitions
- interaction results or blocked reasons

### 3. Runtime-evaluation gap

The mini-program has no dedicated indoor runtime engine. The current page does not:
- evaluate schedule-based visibility
- track trigger progress or prerequisite chains
- start dwell timers
- dispatch effect results
- call any authoritative indoor interaction endpoint

### 4. Auth and mutation gap

The current system has no public indoor interaction endpoint that can:
- gate stateful behavior execution by auth state
- record indoor interaction logs
- deduplicate repeat trigger submissions
- return deterministic blocked reasons for unsupported or auth-required effects

### 5. Verification gap

There is no Phase 17 proof path yet that hits both:
- the public backend on `8080`
- the mini-program runtime build path in `packages/client`

Phase 17 needs live proof, not a plan-only assumption.

## Recommended Runtime Architecture

### 1. Keep the Phase 15 and Phase 16 admin model as the single source of truth

Phase 17 should not redesign the rule schema again.

Recommended rule:
- keep `indoor_nodes` plus `indoor_node_behaviors` as the canonical authored persistence model
- project from that model into public runtime DTOs
- leave the admin workbench and governance center as the only authoring surfaces

Why:
- avoids rule-shape drift between admin and public
- lets Phase 16 governance remain valid against the same behavior IDs the public runtime executes
- keeps migration risk low

### 2. Add a dedicated public runtime read model instead of overloading the static marker DTO

Recommended public endpoint:
- `GET /api/v1/indoor/floors/{floorId}/runtime`

Recommended response shape:
- floor envelope
  - `floorId`
  - `floorCode`
  - `buildingId`
  - `buildingCode`
  - `runtimeVersion`
  - tile and zoom metadata already needed by the page
  - `nodes`
- runtime node
  - `nodeId`
  - `markerCode`
  - `nodeType`
  - `presentationMode`
  - `overlayType`
  - localized `name` and `description`
  - `relativeX` / `relativeY`
  - `iconUrl` / `animationUrl`
  - `overlayGeometryJson`
  - `popupConfigJson`
  - `displayConfigJson`
  - `linkedEntityType`
  - `linkedEntityId`
  - `sortOrder`
  - `behaviors`
- runtime behavior
  - `behaviorId`
  - `behaviorCode`
  - localized `name`
  - `sortOrder`
  - `status`
  - `runtimeSupportLevel`
  - `supported`
  - `requiresAuth`
  - typed `appearanceRules`
  - typed `triggerRules`
  - typed `effectRules`
  - `pathGraph`
  - `overlayGeometry`

This should be a new runtime endpoint, not a breaking mutation of the existing static `getFloor` response.

### 3. Split evaluation into server-authoritative normalization plus client-side interaction flow

The clean split for Phase 17 is:

- server-authoritative responsibilities
  - only return `published` nodes and behaviors
  - strip authoring-only values that should not reach the client
  - exclude unsupported or storage-only behaviors from executable output
  - resolve localized text
  - pre-sort nodes and behaviors deterministically
  - apply time-safe visibility normalization for server-known categories such as `schedule_window`
  - decide whether a behavior is executable for anonymous versus authenticated users
- mini-program responsibilities
  - render the returned runtime snapshot
  - evaluate lightweight client events such as `tap`, `proximity`, and `dwell`
  - maintain transient session state such as dwell timers, completed local triggers, active path motions, and open popup state
  - submit authoritative interaction events back to the server for guarded evaluation and audit

This avoids putting the entire runtime engine only on the client while still keeping the UI responsive.

### 4. Add an authoritative indoor interaction endpoint for safe effect resolution

Recommended endpoint:
- `POST /api/v1/indoor/runtime/interactions`

Recommended request shape:
- `floorId`
- `nodeId`
- `behaviorId`
- `triggerId`
- `eventType`
- `eventTimestamp`
- optional `relativeX`
- optional `relativeY`
- optional `dwellMs`
- optional `clientSessionId`
- optional `locale`

Recommended response shape:
- `interactionAccepted`
- `visible`
- `matchedTriggerId`
- `blockedReason`
- `requiresAuth`
- `effects`
- `interactionLogId`
- `cooldownUntil`

Why this matters:
- the backend can reject anonymous state mutations consistently
- the backend can produce deterministic blocked reasons
- the client does not have to fake success for unsupported effects
- the project can leave behind a real audit trail

### 5. Do not reuse the current `trigger_logs` table directly

`trigger_logs` is POI-checkin-shaped:
- `poi_id`
- `trigger_type`
- `distance`
- `gps_accuracy`
- `wifi_used`

That is too POI-specific for indoor rule chains.

Recommended approach:
- reuse the service pattern and audit intent
- add a dedicated `indoor_runtime_logs` table for:
  - `floor_id`
  - `node_id`
  - `behavior_id`
  - `trigger_id`
  - `event_type`
  - `user_id`
  - `client_session_id`
  - `blocked_reason`
  - `effect_categories_json`
  - `created_at`

This keeps indoor rule execution auditable without corrupting the meaning of the POI trigger log history.

### 6. Use an explicit supported-runtime matrix for Phase 17

Phase 15 allowed many categories, including placeholders. Phase 17 should execute a safe subset first and degrade predictably for the rest.

Recommended supported matrix:

- server-normalized appearance categories
  - `always_on`
  - `manual`
  - `schedule_window`
- client-detected trigger categories
  - `tap`
  - `proximity`
  - `dwell`
- client-rendered effects
  - `popup`
  - `bubble`
  - `media`
  - `path_motion`
- guarded or blocked categories for this phase
  - `collectible_grant`
  - `badge_grant`
  - `task_update`
  - `account_adjustment`
  - `reward_grant`
  - `voice_placeholder`
  - any unsupported appearance or trigger category not listed above

Important rule:
- blocked or unsupported categories must return deterministic `blockedReason` values such as `auth_required`, `unsupported_effect`, or `unsupported_trigger`
- they must never silently succeed

That still satisfies Phase 17 because authored data is evaluated safely and predictably instead of being ignored or executed unsafely.

### 7. Promote deterministic Lisboa fixtures into a Phase 17-supported showcase

The current Phase 15 showcase seed uses:
- `night-market-schedule-overlay`
- `royal-palace-dwell-reveal`
- `zipcity-guiding-path`

Those are the right runtime proof fixtures.

Recommended Phase 17 fixture policy:
- keep `lisboeta_macau` 1F as the deterministic runtime showcase
- promote the supported showcase behaviors from `phase15_storage_only` to `phase17_supported`
- keep any unsupported effect categories in the data, but force the runtime to return blocked results instead of fake grants

This makes smoke and UAT reproducible.

### 8. Keep the mini-program runtime engine in a dedicated service module, not inside the page component

Recommended client structure:
- `packages/client/src/services/indoorRuntime.ts`
  - runtime snapshot normalization
  - visible-behavior evaluation
  - local event routing
  - dwell timer helpers
  - interaction submission helper
  - effect dispatch helpers
- `packages/client/src/pages/map/indoor/index.tsx`
  - page orchestration and rendering only
- `packages/client/src/services/api.ts`
  - public runtime DTOs and endpoint wrappers

This prevents `pages/map/indoor/index.tsx` from repeating the Phase 16 admin-page complexity problem on the mini-program side.

### 9. Honor the Phase 7 auth wall inside indoor runtime flows

Anonymous users can browse the indoor page, but any stateful interactive effect must route through the existing auth wall.

Recommended rule:
- read-only effects such as popup, bubble, media, and path-motion can render for anonymous users
- any effect that would mutate user state must call `requireAuth(...)` before final execution
- if the server interaction response says `requiresAuth = true` or `blockedReason = auth_required`, the client must show the indoor-specific login prompt and stop further mutation attempts

This keeps indoor interactions aligned with the already-implemented auth contract.

## Recommended Verification Strategy

### Backend

- add server tests for public runtime projection and interaction evaluation
- verify unpublished nodes and `phase15_storage_only` behaviors do not leak into executable runtime payloads
- verify behavior ordering is deterministic
- verify anonymous interaction submissions get `auth_required` for stateful effects
- verify unsupported categories get explicit blocked reasons instead of silent success

### Mini-program

- keep the runtime logic in a pure service module so at least parts of it remain unit-testable later
- treat `npm run build:weapp` as the baseline regression gate for the client
- manually verify tap, dwell, path-motion, popup, and auth gating in WeChat DevTools against real runtime data

### Smoke

Create `scripts/local/smoke-phase-17-indoor-runtime.ps1` that:
- ensures the deterministic Lisboa runtime fixture is present
- optionally uses admin `8081` to check or reseed the fixture
- calls the public runtime endpoint on `8080`
- asserts exact behavior fields and support flags
- posts interaction events for tap, dwell, and anonymous blocked flows
- runs `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` in `packages/server`
- runs `npm run build:weapp` in `packages/client`

### Manual proof

Create a Phase 17 UAT checklist for:
- loading the Lisboa indoor runtime snapshot
- seeing the schedule-based overlay at the right time window
- triggering the Zipcity path behavior through tap
- triggering the Royal Palace dwell behavior through dwell timing
- confirming anonymous users hit the auth wall on guarded effects
- confirming unsupported categories fail safely instead of crashing or claiming success

## Risks and Mitigations

### Risk: Public runtime leaks admin-only rule details

Mitigation:
- use dedicated public runtime DTOs
- only expose published and runtime-supported behavior payloads
- never mirror admin governance warnings or storage-only rows into public runtime responses

### Risk: Client and server evaluate different subsets

Mitigation:
- define one supported-runtime matrix in the public backend
- return `supported` and `blockedReason` explicitly
- keep the client focused on local event routing and rendering, not full rule-authoring interpretation

### Risk: Anonymous users mutate state through indoor interactions

Mitigation:
- route all stateful effect execution through the public interaction endpoint
- require auth before mutation
- return `auth_required` deterministically

### Risk: Unsupported categories crash the mini-program

Mitigation:
- degrade to blocked or informational states
- never assume all authored categories are executable
- isolate the runtime engine in a service module with defensive parsing

### Risk: Verification proves only static payloads, not actual runtime flow

Mitigation:
- smoke must hit both the public runtime read endpoint and the interaction endpoint
- manual UAT must confirm tap, dwell, and auth-gated flows inside the mini-program experience

## Recommendation Summary

- Reuse the Phase 15 and Phase 16 authored rule model as the only source of truth.
- Add a new public indoor runtime snapshot endpoint plus an authoritative interaction endpoint.
- Add a dedicated indoor runtime log model instead of overloading the POI trigger log table.
- Execute a clear supported subset for Phase 17 and return explicit blocked reasons for the rest.
- Keep mini-program runtime logic in a dedicated service module and honor the existing auth wall for stateful actions.
- Prove the phase through live `8080` + `8081` smoke and `build:weapp`, using Lisboa 1F as the deterministic showcase.

## Validation Architecture

- Use JUnit 5 plus Spring Boot Test in `packages/server` for runtime snapshot projection, interaction gating, and blocked-reason behavior.
- Use `npm run build:weapp` in `packages/client` as the baseline mini-program regression gate.
- Add a dedicated Phase 17 PowerShell smoke harness for local `8080` public runtime reads and interaction submissions, with optional admin `8081` fixture preparation.
- Keep Phase 17 validation runtime-focused:
  - quick: targeted public server tests
  - full: public server tests, `build:weapp`, and the Phase 17 smoke harness
- Manual UAT remains necessary for tap cadence, dwell timing clarity, and auth-wall ergonomics inside WeChat DevTools.

## Phase 17 Output Implications

Phase 17 plans should therefore include:
- public server entities, mappers, DTOs, and runtime endpoints for indoor behaviors
- a deterministic Phase 17 Lisboa runtime fixture
- a dedicated mini-program indoor runtime engine and page integration
- auth-safe interaction submission and fallback handling
- smoke and UAT artifacts for runtime execution on the live local stack

They should not attempt to finish:
- every possible authored effect category
- every downstream domain mutation for collectibles, badges, and tasks in one step
- AI capability-center work from Phase 18
