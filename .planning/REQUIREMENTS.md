# Requirements: Trip of Macau v3.2

**Defined:** 2026-05-04  
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v3.2 Requirements

### Device Acceptance

- [x] **UAT-01**: Operator can build and open the mini-program in WeChat DevTools against the local public backend without route-load, auth, or config mismatch failures.
- [ ] **UAT-02**: Operator can run a documented flagship story smoke on a real device or DevTools simulator and capture the exact pass/fail evidence.
- [ ] **UAT-03**: Operator can distinguish automated smoke, DevTools checks, physical-device checks, and accepted caveats in one release-readiness report.

### Traveler Gameplay Runtime

- [x] **PLAY-01**: Traveler can enter the `東西方文明的戰火與共生` story mode from the mini-program and see the correct introduction, chapters, current route, and generated media.
- [x] **PLAY-02**: Traveler can trigger compiled POI/story runtime steps for click, proximity, content-completion, pickup, task, and reward events without relying on mock-only state.
- [x] **PLAY-03**: Traveler can receive visible Traditional Chinese feedback for pickups, task progress, medals, titles, coins, and unsupported advanced gameplay.
- [x] **PLAY-04**: Traveler can exit and re-enter story mode with session progress, permanent exploration events, and already-earned rewards behaving according to backend state.
- [x] **PLAY-05**: Traveler-facing media playback supports image, audio, video, and Lottie assets with poster/fallback behavior and no blank-screen failure when an asset is unavailable.

### User Progress and Operations

- [ ] **OPS-01**: Operator can inspect a traveler story session, exploration events, pickups, rewards, titles, and backpack state from admin without querying the database manually.
- [ ] **OPS-02**: Operator can search/filter progress and reward records by user, story, chapter, POI/map, status, event type, and time range.
- [ ] **OPS-03**: Operator can preview and execute safe support actions such as recompute progress, resend reward, void duplicate event, or annotate an issue with audit trail.
- [ ] **OPS-04**: Operator can see why a traveler did or did not receive a configured reward/title by tracing from runtime event to rule, condition, and result.

### Management System Polish

- [ ] **ADMIN-01**: Operator-facing story/gameplay/admin navigation has no misleading shell-only entries, duplicate missions, or wrong redirects for v3.2-owned workflows.
- [ ] **ADMIN-02**: Operator can use the story/gameplay operations pages with stable layout, readable columns, responsive panels, and Traditional Chinese labels.
- [ ] **ADMIN-03**: Operator can open relevant media/material/detail drawers without broken previews, overlong URL overflow, or dead package-version links.
- [ ] **ADMIN-04**: Operator can access concise inline explanations for interaction templates, governance checks, and runtime-state actions so the pages are usable without reading raw JSON.

## Future Requirements

### Advanced Gameplay

- **ADV-01**: Traveler can complete production-grade AR/photo recognition tasks with visual-anchor confidence and fallback review.
- **ADV-02**: Traveler can use speech-input NPC or voice-command interactions in real gameplay flows.
- **ADV-03**: Traveler can complete route-coverage, puzzle, cannon-defense, or bespoke minigames as native mini-program interactions.
- **ADV-04**: Traveler can use production indoor visual-positioning and floor-level route guidance beyond the current indoor baseline.

### Release Platform

- **REL-01**: Operator can run a full approval workflow before content or reward changes go live.
- **REL-02**: Operator can compare staging, experience, and production runtime snapshots before publishing.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Full production AR/photo recognition | Requires device camera/vision validation, model tuning, privacy review, and fallback UX beyond this acceptance milestone. |
| Speech-input NPC gameplay | Requires audio permission handling, speech recognition, latency controls, and moderation guardrails not yet proven on device. |
| Bespoke complex minigame engines | v3.2 focuses on reusable runtime event handling and safe placeholders; heavy gameplay engines should be phased after real device feedback. |
| Full approval workflow | Direct lifecycle controls already exist; approval chains need a separate operations-policy milestone. |
| New AI generation workbench | The user explicitly wants story-specific production runs rather than exposing image generation broadly to all operators. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| UAT-01 | Phase 41 | Verified |
| UAT-02 | Phase 44 | Pending |
| UAT-03 | Phase 44 | Pending |
| PLAY-01 | Phase 41 | Verified |
| PLAY-02 | Phase 42 | Complete |
| PLAY-03 | Phase 42 | Complete |
| PLAY-04 | Phase 42 | Complete |
| PLAY-05 | Phase 41 | Verified |
| OPS-01 | Phase 43 | Pending |
| OPS-02 | Phase 43 | Pending |
| OPS-03 | Phase 43 | Pending |
| OPS-04 | Phase 43 | Pending |
| ADMIN-01 | Phase 44 | Pending |
| ADMIN-02 | Phase 44 | Pending |
| ADMIN-03 | Phase 44 | Pending |
| ADMIN-04 | Phase 44 | Pending |

**Coverage:**
- v3.2 requirements: 16 total
- Mapped to phases: 16
- Unmapped: 0

---
*Requirements defined: 2026-05-04*  
*Last updated: 2026-05-04 after Phase 41 execution*
