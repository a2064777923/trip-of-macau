---
phase: 38-public-runtime-asset-consumption
status: passed
verified: 2026-05-03
requirements:
  - RUN-01
  - RUN-02
  - RUN-03
  - RUN-04
---

# Phase 38 Verification

## Scope

Phase 38 verified the public backend runtime contract for the flagship `east_west_war_and_coexistence` story:

- **RUN-01:** runtime media DTOs expose traveler-safe asset delivery data, fallback/poster/Lottie metadata, and usage hints without admin production provenance.
- **RUN-02:** unpublished or unavailable media stays visible as fallback or unsupported media instead of deleting the chapter/block/step.
- **RUN-03:** authenticated public event endpoints accept baseline story media, pickup, task, reward, exploration, and session-exit flows with idempotency.
- **RUN-04:** a repeatable local smoke loads the flagship runtime and checks lifecycle filtering plus privacy.

## Automated Evidence

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -q -DskipTests compile -f packages/server/pom.xml` | Passed | Public backend compiles with Phase 38 runtime DTO/event changes. |
| `mvn -q -Dtest=PublicRuntimeAssetServiceTest test -f packages/server/pom.xml` | Passed | Covers available, fallback, unsupported, Lottie metadata, safe usage hints, and rejected package filtering. |
| `mvn -q -Dtest=PublicExperienceEventServiceTest test -f packages/server/pom.xml` | Passed | Covers event allowlist, invalid/oversized payload rejection, duplicate `clientEventId`, media completion, reward events, and session-exit idempotency. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-38-public-runtime-assets.ps1` | Passed | Anonymous runtime smoke loaded the flagship story and confirmed asset/privacy checks. |
| `$env:PHASE38_TRAVELER_DEV_IDENTITY='phase38-smoke-traveler'; powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-38-public-runtime-assets.ps1` | Passed | Local/dev authenticated smoke used dev-bypass without printing the bearer token and exercised events, exploration, and session exit. |

The local public backend was restarted on `2026-05-03` with profile `local` on port `8080` before the smoke evidence was recorded.

## Runtime Privacy Checks

The Phase 38 smoke verifies `GET /api/v1/storylines/{id}/runtime?locale=zh-Hant` for:

- `runtimeVersion = "v1"`.
- At least five chapters.
- At least one compiled flow step.
- At least one content block with a public media object.
- Media objects with `availability` and image/audio/video coverage.
- Lottie-capable metadata when Lottie blocks exist.
- No banned field names in serialized runtime JSON: `promptText`, `scriptText`, `localPath`, `providerApiKey`, `apiKey`, `secret`, `estimatedCost`, `actualCost`, `qaNote`, or `cosObjectKey`.

The seed was adjusted so the flagship finale includes `ch05_finale_recap_video`, a published content block pointing at the existing draft placeholder video asset. This intentionally verifies RUN-02: the public DTO keeps a video media object but marks it unavailable/unsupported instead of leaking an unpublished URL as playable content or dropping the block.

## Event and Session Checks

With `PHASE38_TRAVELER_DEV_IDENTITY` set and the local public backend running with `WECHAT_DEV_BYPASS_ENABLED=true`, the smoke verifies:

- `POST /api/v1/storylines/{id}/sessions/start` returns a `sessionId`.
- `POST /api/v1/storylines/{id}/sessions/{sessionId}/events` accepts `media_completed`.
- Repeating the same `media_completed` with the same `clientEventId` returns idempotent metadata.
- `pickup_interacted`, `task_completed`, and `reward_acquired` are accepted.
- `GET /api/v1/users/me/exploration?scopeType=storyline&scopeId={id}` returns `scopeType = "storyline"`.
- `POST /api/v1/storylines/{id}/sessions/{sessionId}/exit` is retry-safe on repeated exit.

## Caveats

- The authenticated smoke uses the local/dev-only dev-bypass login path. This is valid for Phase 38 local verification of event/session behavior, but it is not a WeChat production login UAT.
- Mini-program rendering and WeChat DevTools/device acceptance remain Phase 39/40 scope.
- The smoke does not perform network `HEAD` checks against every COS or third-party media URL; Phase 38 verifies DTO lifecycle sanitation and runtime linkage, not CDN availability.
- No provider keys, COS secrets, bearer tokens, prompt text, local paths, costs, or QA notes were printed or written to this verification file.
