# Roadmap: v3.3 Mini-program Gameplay UX and Device Acceptance

**Milestone:** v3.3
**Status:** Planned
**Defined:** 2026-05-06

## Milestone Goal

Make the live WeChat mini-program behave like the intended story-driven Macau gameplay experience, not an operator information display, and verify it against real backend services through WeChat DevTools / device evidence.

## Phases

### Phase 46: Live Mini-program Service Wiring and Diagnostics

**Goal:** Make local public/admin/client services start reliably and prove the mini-program is consuming live backend data without mock fallback masking.

**Requirements:** LIVE-01, LIVE-02, LIVE-03, LIVE-04

**Success criteria:**

1. A documented local startup flow opens the public backend, admin backend, admin UI, and Taro/WeChat client with separate visible logs.
2. Mini-program API calls for cities, maps, POIs, story runtime, media, sessions, and progress hit the configured public backend in live mode.
3. Backend/auth/config failures show user-safe messages and developer console diagnostics, with no hidden mock fallback.
4. A diagnostics-only operator view or log path confirms backend base URL, auth/test-mode state, and selected story runtime.

### Phase 47: Map Spatial UX, Coordinate Alignment, and Marker Assets

**Goal:** Repair the traveler-facing map experience so large-map switching, coordinates, story routes, and 2.5D marker icons match admin-managed data.

**Requirements:** MAP-01, MAP-02, MAP-03, MAP-04, MAP-05

**Success criteria:**

1. Traveler-facing home/map entry supports the intended large-map switching without exposing operator-only binding fields.
2. Stored AMap coordinates are correctly transformed or normalized for the active WeChat/Tencent map runtime.
3. Flagship POIs render with transparent 2.5D marker icons managed by admin/COS.
4. Story mode highlights the current chapter target and route while future route segments and unrelated map content follow story-mode display rules.
5. A repeatable smoke captures backend coordinate data, mini-program marker positions, and screenshots for acceptance review.

### Phase 48: Story Entry, Introduction, Chapters, and Route Preview

**Goal:** Replace the story page's configuration-report feel with a traveler-facing story entry and chapter selection flow.

**Requirements:** STORY-01, STORY-02, STORY-03, STORY-04, STORY-05

**Success criteria:**

1. The `東西方文明的戰火與共生` entry shows correct title handling, generated cover/media, and no overflow.
2. Opening the story displays an immersive introduction and offers only the traveler actions to start or view chapters.
3. The chapter list shows names, locations, route order, and optional branch insertion choices without admin-only labels.
4. Starting story mode transitions into a route/map-led experience instead of static runtime configuration cards.
5. Exiting story mode uses clear Traditional Chinese copy for temporary session reset and permanent exploration/reward retention.

### Phase 49: Progressive Chapter Gameplay and Reward Feedback

**Goal:** Make the first flagship chapter playable step by step, then generalize the runtime pattern for later chapters.

**Requirements:** PLAY-01, PLAY-02, PLAY-03, PLAY-04, PLAY-05, PLAY-06

**Success criteria:**

1. Chapter 1 at A-Ma Temple triggers core story media and then reveals only the next objective.
2. Mainline overlays appear and resolve in configured order, unlocking story content progressively.
3. Side pickups appear only after configured conditions and produce backpack/progress feedback.
4. Hidden challenges unlock only after conditions are met, with unsupported engines using polished traveler-facing placeholders.
5. Reward, title, medal, coin, fragment, and next-chapter feedback is presented as gameplay, not admin rule text.
6. Operator debugging can trace every visible step back to public runtime DTOs and admin configuration.

### Phase 50: Story Media Playback, Lottie Lifecycle, and Cache Stability

**Goal:** Make story images, audio, video, and Lottie assets visible and stable while controlling memory/cache growth.

**Requirements:** MEDIA-01, MEDIA-02, MEDIA-03, MEDIA-04, MEDIA-05

**Success criteria:**

1. Configured story images, audio, videos, and Lottie assets render or play in the mini-program when available.
2. Poster, fallback, unsupported, and broken-media states are visually acceptable and do not white-screen.
3. Lottie uses static canvas/network paths and destroys animation instances on page exit or asset switch.
4. Repeated story entry/exit, media playback, and map navigation do not grow cache/memory without bounds.
5. Bundle-size warnings are either reduced through splitting/lazy loading or documented with exact remaining budget and next work.

### Phase 51: WeChat DevTools MCP and Device Acceptance Evidence

**Goal:** Capture milestone-grade WeChat acceptance evidence instead of inferring mini-program success from backend/browser smoke.

**Requirements:** UAT-01, UAT-02, UAT-03, UAT-04

**Success criteria:**

1. MCP or documented manual steps open the WeChat DevTools project and inspect pages, logs, screenshots, and network behavior.
2. The flagship story smoke runs in WeChat DevTools against the live public backend and records pass/fail evidence for home, map, story entry, first chapter, media, and rewards.
3. Physical-device GPS/proximity checks are either executed with evidence or explicitly left as a named caveat without being marked PASS.
4. Final acceptance docs distinguish automated checks, admin/browser checks, DevTools checks, physical-device checks, caveats, and future-scope gameplay.

## Progress

| Phase | Name | Requirements | Status |
|-------|------|--------------|--------|
| 46 | Live Mini-program Service Wiring and Diagnostics | LIVE-01..LIVE-04 | Not started |
| 47 | Map Spatial UX, Coordinate Alignment, and Marker Assets | MAP-01..MAP-05 | Not started |
| 48 | Story Entry, Introduction, Chapters, and Route Preview | STORY-01..STORY-05 | Not started |
| 49 | Progressive Chapter Gameplay and Reward Feedback | PLAY-01..PLAY-06 | Not started |
| 50 | Story Media Playback, Lottie Lifecycle, and Cache Stability | MEDIA-01..MEDIA-05 | Not started |
| 51 | WeChat DevTools MCP and Device Acceptance Evidence | UAT-01..UAT-04 | Not started |

## Coverage

- Requirements: 29
- Mapped requirements: 29
- Unmapped requirements: 0
- Starting phase: 46
- Previous milestone: v3.2, phases 41-45

## Next Step

Start with `/gsd-plan-phase 46`.
