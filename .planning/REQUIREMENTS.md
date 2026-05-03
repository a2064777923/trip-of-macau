# Requirements: Trip of Macau

**Defined:** 2026-04-30
**Milestone:** v3.1 Material Production and Mini-program Experience Acceptance
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v3.1 Requirements

### Material Production and Asset Promotion

- [ ] **MAT-01**: Operators can generate or import real flagship story still images from the Phase 33 manifest, including story covers, chapter hero art, pickup icons, honor/title icons, and fallback posters, while preserving prompt provenance and UTF-8 metadata.
- [ ] **MAT-02**: Operators can use a material-board workflow where one generated image can contain multiple aligned assets, then crop/slice selected regions into reusable child assets with local files, COS object keys, and `content_assets` records.
- [ ] **MAT-03**: Operators can synthesize narration, short sound cues, and chapter audio from approved scripts through configured providers such as CosyVoice, with language selection, voice selection, task history, preview, retry, and COS upload.
- [ ] **MAT-04**: Operators can assemble simple story videos from one or more still images using pan/zoom/motion, narration/audio, subtitles or captions where needed, and export them as COS-backed video assets.
- [ ] **MAT-05**: Operators can promote manifest materials from `planned` to `generated`, `uploaded`, `approved`, or `published`, with rollback to previous asset versions and clear links back to the Phase 33 story material package.

### Admin Material QA and Reuse

- [x] **QA-01**: Operators can inspect generated images, sliced child assets, Lottie files, audio, and videos in one Traditional Chinese material production workspace with status, provenance, dimensions, duration, file size, cost, and usage targets.
- [x] **QA-02**: Operators can reject, regenerate, replace, or approve generated assets without breaking existing story, content block, reward, or runtime bindings.
- [x] **QA-03**: Operators can compare planned manifest entries against actual local files, COS objects, `content_assets`, and story package item rows, with warnings for missing, stale, oversized, or wrong-kind assets.
- [ ] **QA-04**: Operators can reuse approved assets from the story material package in content blocks, rewards, pickups, POI/story overlays, and public runtime DTOs through the existing media picker components.

### Public Runtime Asset Consumption

- [x] **RUN-01**: Public story runtime APIs return published generated assets, posters, fallbacks, Lottie metadata, audio/video metadata, and manifest usage hints without exposing admin-only prompt, local path, or provider-secret data.
- [x] **RUN-02**: Public runtime gracefully handles missing, unpublished, or rejected generated assets by falling back to poster/fallback assets or clear unsupported-media placeholders.
- [x] **RUN-03**: Public event endpoints can record story media read/play completion, pickup interaction, task baseline completion, and reward acquisition events needed by dynamic exploration progress.
- [x] **RUN-04**: Public runtime smoke verifies the flagship story can load generated/published assets and that lifecycle filtering still hides unpublished content.

### Mini-program Story Experience Acceptance

- [ ] **MP-01**: The mini-program story page can load the flagship runtime from the public backend and render story introduction, chapter list, content blocks, generated images, Lottie, audio, video, and fallback states without mock-only assumptions.
- [ ] **MP-02**: Story mode map behavior can draw the route, highlight the current chapter, gray out inactive route segments, and display current destination details from public runtime DTOs.
- [ ] **MP-03**: The mini-program can report core story events including chapter start, content viewed, media played, pickup tapped, task baseline completed, and story session exit with idempotency.
- [ ] **MP-04**: Baseline interactive objects for the flagship story can appear, be tapped, grant pickups/rewards/titles where configured, and update dynamic exploration progress through the public backend.
- [ ] **MP-05**: Unsupported complex gameplay such as AR recognition, speech input, and puzzle minigames degrades to clear Traditional Chinese placeholders while still preserving story flow and progress integrity.

### Verification and Operations Acceptance

- [ ] **ACC-01**: A repeatable local smoke can verify generation metadata, asset promotion, COS URL availability, public runtime asset consumption, and mini-program build compatibility.
- [ ] **ACC-02**: A WeChat DevTools/device UAT checklist exists for the flagship story journey, including route display, chapter progression, media playback, pickups, rewards, and exit/restart behavior.
- [ ] **ACC-03**: Operators can view production cost and generation job history for the material package without exposing API keys or provider secrets.
- [ ] **ACC-04**: The milestone ships with clear evidence of what was fully implemented, what was manually accepted, and what remains deferred for complex gameplay engines.

## Future Requirements

- Full production-grade AR photo recognition and indoor visual positioning.
- Voice-input NPC dialogue and speech-triggered gameplay.
- Puzzle/minigame engines beyond baseline task placeholders.
- Full approval workflow with multi-step reviewer roles.
- `.lottie` package upload, sprite sheets, and sequence-frame animation pipelines.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Replacing the existing Phase 28-35 story/runtime schema | v3.1 should consume and extend the existing control plane, not rewrite it. |
| Committing provider keys or COS secrets | Secrets must stay in local environment or encrypted runtime config. |
| Treating generated assets as published without QA | Operators need review, rollback, and clear status transitions before runtime exposure. |
| Full AR/speech/puzzle gameplay implementation | v3.1 focuses on story journey acceptance and baseline interaction; complex engines remain future scope. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| MAT-01 | Phase 36 | Complete |
| MAT-02 | Phase 36 | Complete |
| MAT-03 | Phase 36 | Complete |
| MAT-04 | Phase 36 | Complete: five chapter MP4s were assembled from generated stills plus narration, promoted to COS as `video/mp4`, and retain UTF-8 caption metadata; burned-in subtitles degraded to external captions on this workstation |
| MAT-05 | Phase 36 | Complete: image, board-slice, audio, video, publish, rollback, version-history, and COS evidence pass |
| QA-01 | Phase 37 | Complete |
| QA-02 | Phase 37 | Complete |
| QA-03 | Phase 37 | Complete |
| QA-04 | Phase 37 | Pending |
| RUN-01 | Phase 38 | Complete |
| RUN-02 | Phase 38 | Complete |
| RUN-03 | Phase 38 | Complete |
| RUN-04 | Phase 38 | Complete |
| MP-01 | Phase 39 | Pending |
| MP-02 | Phase 39 | Pending |
| MP-03 | Phase 39 | Pending |
| MP-04 | Phase 39 | Pending |
| MP-05 | Phase 39 | Pending |
| ACC-01 | Phase 40 | Pending |
| ACC-02 | Phase 40 | Pending |
| ACC-03 | Phase 40 | Pending |
| ACC-04 | Phase 40 | Pending |

**Coverage:**
- v3.1 requirements: 22 total
- Mapped to phases: 22
- Unmapped: 0

---
*Requirements defined: 2026-04-30*
*Last updated: 2026-05-03 after Phase 38 public runtime asset and authenticated event smoke verification*
