# Requirements: v3.3 Mini-program Gameplay UX and Device Acceptance

**Defined:** 2026-05-06
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v3.3 Requirements

### Live Runtime Wiring

- [ ] **LIVE-01**: Operator can start the public backend, admin backend, admin UI, and mini-program dev build with one documented local flow and see separate logs for each service.
- [ ] **LIVE-02**: Traveler mini-program requests city, map, POI, story, runtime, media, session, and progress data only from the real public backend in live mode.
- [ ] **LIVE-03**: Traveler mini-program surfaces backend/auth/config failures clearly in user-safe copy and developer console logs without silently switching to mock data.
- [ ] **LIVE-04**: Operator can verify the current mini-program backend base URL, auth/test-mode state, and active story runtime from logs or a diagnostics panel without exposing this information to travelers.

### Map and Spatial UX

- [ ] **MAP-01**: Traveler can switch the active large map from the home entry or intended traveler-facing map selector without exposing operator-only map binding labels.
- [ ] **MAP-02**: Traveler sees Macau POIs at correct positions after converting or normalizing stored AMap coordinates for the active WeChat/Tencent map runtime.
- [ ] **MAP-03**: Traveler sees admin-managed transparent 2.5D marker icons for flagship POIs instead of generic or misaligned markers.
- [ ] **MAP-04**: Traveler in story mode sees the current story route, current chapter target highlight, future route segments in a subdued style, and unrelated map content hidden or softened according to story-mode rules.
- [ ] **MAP-05**: Operator can run a coordinate/icon smoke that compares backend POI coordinates, mini-program marker positions, and visible map screenshots.

### Story Entry and Chapter UX

- [ ] **STORY-01**: Traveler can discover `東西方文明的戰火與共生` from the intended story entry and sees the correct generated cover/media without long-title overflow.
- [ ] **STORY-02**: Traveler opening the story sees an immersive introduction with image/video/Lottie/audio content and two clear actions: start story or view chapters.
- [ ] **STORY-03**: Traveler viewing chapters sees chapter names, bound traveler-facing locations, route order, and optional branch insertion choices without operator-only fields such as anchor type, bindings, workbench labels, or flow JSON details.
- [ ] **STORY-04**: Traveler starting story mode sees a route/map-focused experience rather than a static configuration report.
- [ ] **STORY-05**: Traveler can exit story mode with clear copy explaining temporary session progress behavior while permanent exploration/reward records remain.

### Progressive Gameplay

- [ ] **PLAY-01**: Traveler entering the first chapter at A-Ma Temple receives the core story media at the intended proximity or simulated proximity trigger, then sees only the next actionable objective.
- [ ] **PLAY-02**: Traveler can interact with mainline overlays in sequence and unlock related story content progressively instead of seeing all chapter tasks upfront.
- [ ] **PLAY-03**: Traveler can collect side pickups after their appearance conditions are satisfied and receives backpack/progress/reward feedback.
- [ ] **PLAY-04**: Traveler can trigger the hidden challenge only after configured conditions are met, with unsupported advanced gameplay degraded into an explicit traveler-facing placeholder.
- [ ] **PLAY-05**: Traveler receives medals, titles, coins, fragments, and next-chapter unlock feedback as game rewards, not as admin rule text.
- [ ] **PLAY-06**: Operator can trace each visible gameplay step back to the public runtime DTO and admin configuration when debugging.

### Media and Performance

- [ ] **MEDIA-01**: Traveler can preview or play generated story images, audio, videos, and Lottie assets where those assets are configured and available.
- [ ] **MEDIA-02**: Traveler sees poster/fallback/unsupported media states that are visually acceptable and do not white-screen.
- [ ] **MEDIA-03**: Mini-program Lottie usage follows WeChat constraints: network path, static canvas, cleanup on page exit, and no dynamic-canvas memory leak.
- [ ] **MEDIA-04**: Mini-program cache and memory usage stays bounded during repeated story entry, media playback, map navigation, and story exit.
- [ ] **MEDIA-05**: Story page bundle-size warnings are reduced or explicitly documented with a measurable next-step split plan.

### WeChat Acceptance

- [ ] **UAT-01**: Operator can use MCP or documented manual steps to open the WeChat DevTools project, collect screenshots, inspect logs, and navigate each mini-program page.
- [ ] **UAT-02**: Operator can run the flagship story smoke in WeChat DevTools against the live public backend and capture pass/fail evidence for home, map, story entry, first chapter, media, and rewards.
- [ ] **UAT-03**: Operator can run or prepare a physical-device smoke checklist for GPS/proximity-sensitive behavior without treating simulator-only checks as device PASS.
- [ ] **UAT-04**: Release acceptance documentation separates automated checks, browser/admin checks, WeChat DevTools checks, physical-device checks, caveats, and future-scope gameplay engines.

## Future Requirements

### Advanced Gameplay Engines

- **ADV-01**: Traveler can complete production AR/photo recognition tasks with server-side validation.
- **ADV-02**: Traveler can use speech-input NPC interactions in story chapters.
- **ADV-03**: Traveler can play full puzzle, route-coverage, cannon-defense, and indoor-positioning gameplay engines instead of baseline placeholders.

### Release Governance

- **REL-01**: Operator can run formal publish approval workflow with reviewer gates, runtime snapshots, and rollback comparison.
- **REL-02**: Operator can compare story/runtime snapshots across versions before publishing.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Full production AR/photo recognition engine | Requires device-camera permissions, model/API validation, and separate gameplay design beyond v3.3 baseline acceptance. |
| Speech-input NPC gameplay | Requires audio permission UX, latency handling, content safety, and provider-specific runtime work. |
| Full puzzle/cannon-defense/route-coverage engines | v3.3 focuses on progressive story UX and safe placeholders before implementing complex engines. |
| Formal approval workflow | Important but secondary to proving the traveler-facing mini-program experience. |
| End-user AI assistant pages | Existing AI capability center is admin-facing; traveler AI UX needs separate product design. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| LIVE-01 | Phase 46 | Pending |
| LIVE-02 | Phase 46 | Pending |
| LIVE-03 | Phase 46 | Pending |
| LIVE-04 | Phase 46 | Pending |
| MAP-01 | Phase 47 | Pending |
| MAP-02 | Phase 47 | Pending |
| MAP-03 | Phase 47 | Pending |
| MAP-04 | Phase 47 | Pending |
| MAP-05 | Phase 47 | Pending |
| STORY-01 | Phase 48 | Pending |
| STORY-02 | Phase 48 | Pending |
| STORY-03 | Phase 48 | Pending |
| STORY-04 | Phase 48 | Pending |
| STORY-05 | Phase 48 | Pending |
| PLAY-01 | Phase 49 | Pending |
| PLAY-02 | Phase 49 | Pending |
| PLAY-03 | Phase 49 | Pending |
| PLAY-04 | Phase 49 | Pending |
| PLAY-05 | Phase 49 | Pending |
| PLAY-06 | Phase 49 | Pending |
| MEDIA-01 | Phase 50 | Pending |
| MEDIA-02 | Phase 50 | Pending |
| MEDIA-03 | Phase 50 | Pending |
| MEDIA-04 | Phase 50 | Pending |
| MEDIA-05 | Phase 50 | Pending |
| UAT-01 | Phase 51 | Pending |
| UAT-02 | Phase 51 | Pending |
| UAT-03 | Phase 51 | Pending |
| UAT-04 | Phase 51 | Pending |

**Coverage:**
- v3.3 requirements: 29 total
- Mapped to phases: 29
- Unmapped: 0

---
*Requirements defined: 2026-05-06*
*Last updated: 2026-05-06 after starting v3.3 milestone*
