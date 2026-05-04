# Phase 41: WeChat Runtime UAT Harness and Story Entry Hardening - Context

**Gathered:** 2026-05-04  
**Status:** Ready for planning  
**Source:** Direct `/gsd-plan-phase 41` using v3.2 roadmap and requirements

<domain>
## Phase Boundary

Phase 41 makes the existing mini-program story-mode baseline runnable and diagnosable in WeChat DevTools/local runtime before adding deeper gameplay engines in Phase 42.

This phase should harden entry/config/media fallback and produce a repeatable UAT harness. It must not claim full gameplay completion, AR/photo recognition, speech gameplay, puzzle engines, or production indoor positioning.
</domain>

<decisions>
## Implementation Decisions

### Locked Scope
- Use the flagship story `東西方文明的戰火與共生` as the primary UAT target.
- Verify the story route against live `packages/server` runtime data rather than mock-only state.
- Support image, audio, video, and Lottie rendering/fallback behavior in the story content path.
- Keep WeChat DevTools/device evidence truthful: automated build smoke is not the same as manual device UAT.
- Document local/devtools/experience environment behavior so dev-bypass or backend target mismatches are visible.

### the agent's Discretion
- Exact script names and harness file locations may follow existing repo conventions.
- The planner may split work across client config, story runtime page, scripts, and documentation as needed.
- If WeChat DevTools automation still cannot bind to its CLI port on this workstation, the plan must capture a manual evidence path instead of pretending automation passed.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Planning
- `.planning/PROJECT.md` - Current v3.2 scope and cross-milestone decisions.
- `.planning/REQUIREMENTS.md` - Requirements UAT-01, PLAY-01, PLAY-05.
- `.planning/ROADMAP.md` - Phase 41 goal and success criteria.
- `.planning/STATE.md` - Current milestone state and known blockers.

### Mini-program Runtime
- `packages/client/package.json` - Taro build scripts including `build:weapp`.
- `packages/client/config/dev.js` - Development build constants and API targeting.
- `packages/client/config/prod.js` - Production/experience build constants.
- `packages/client/src/app.config.ts` - WeChat page registry and permissions.
- `packages/client/src/pages/story/index.tsx` - Story page runtime consumption path.
- `packages/client/src/services/api.ts` - Public API request wrapper.
- `packages/client/src/services/storyRuntime.ts` - Story runtime DTO mapping if present.

### Public Backend Runtime
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller` - Public story/runtime/event controllers.
- `packages/server/src/main/resources/application.yml` - Local public backend configuration.

### Prior Evidence
- `.planning/milestones/v3.1-ROADMAP.md` - Archived v3.1 caveats and Phase 39/40 closure context.
- `.planning/milestones/v3.1-REQUIREMENTS.md` - Archived baseline story-mode requirements.
</canonical_refs>

<specifics>
## Specific Ideas

- Add or update a repeatable smoke/UAT script that checks public backend health, flagship story runtime payload, mini-program build output, and DevTools launch command readiness.
- Ensure generated media asset availability/fallback data is visible in the mini-program story path rather than causing blank screens.
- Record exact commands and expected outputs for `npm run build:weapp`, public backend health, and flagship runtime quick check.
</specifics>

<deferred>
## Deferred Ideas

- Full gameplay event interpreter belongs to Phase 42.
- Admin progress/reward support workflows belong to Phase 43.
- Admin IA and final release acceptance packaging belong to Phase 44.
- Advanced AR/photo recognition, speech input, route coverage games, puzzle/cannon-defense minigames, and production indoor visual positioning remain future requirements.
</deferred>

---

*Phase: 41-wechat-runtime-uat-harness-and-story-entry-hardening*  
*Context gathered: 2026-05-04 via direct plan-phase*
