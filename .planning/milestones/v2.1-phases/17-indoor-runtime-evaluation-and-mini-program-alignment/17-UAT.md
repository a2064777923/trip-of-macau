# Phase 17 UAT

Status: Pending manual verification in WeChat DevTools

## Preconditions

- Public backend is running on `http://127.0.0.1:8080`
- Admin backend is running on `http://127.0.0.1:8081`
- Lisboa indoor fixture exists and `1F` is published
- Mini-program is built with live runtime code from Phase 17

## Checkpoints

- [ ] Open the Lisboa indoor page on floor `1F` and confirm the runtime snapshot loads from `/api/v1/indoor/floors/{floorId}/runtime` instead of falling back to a static-marker-only screen.
- [ ] Confirm floor switching shows a loading guard or mask and no visible flicker, stale dwell state, or old-path residue leaks across floors.
- [ ] Wait for the night schedule window or mock the time window if needed, then confirm `night-market-schedule-overlay` becomes visible with the authored overlay geometry and popup content.
- [ ] Trigger the dwell interaction for `royal-palace-dwell-reveal` and confirm the page surfaces a guarded/auth-required response rather than freezing or silently failing.
- [ ] Trigger `zipcity-guiding-path` and confirm the path-motion guidance animates along the authored route and the bubble or popup hint appears.
- [ ] Confirm at least one popup or bubble effect renders with coherent content and does not break the indoor viewport layout.
- [ ] While anonymous, attempt a guarded indoor interaction and confirm the mini-program routes to the existing auth wall instead of mutating silently.
- [ ] After real login, retry the guarded indoor interaction and confirm the behavior progresses through the supported/blocked contract without a broken reload path.
- [ ] Confirm unsupported authored categories degrade with a visible explanation tied to `blockedReason` instead of a crash.
- [ ] Confirm multilingual runtime labels remain coherent, especially `zh-Hant`, on floor title, node names, and behavior prompts.

## Notes

- Canonical showcase markers:
  - `1f-phase15-night-market-overlay`
  - `1f-phase15-royal-palace-dwell`
  - `1f-phase15-zipcity-path`
- Canonical showcase behaviors:
  - `night-market-schedule-overlay`
  - `royal-palace-dwell-reveal`
  - `zipcity-guiding-path`
