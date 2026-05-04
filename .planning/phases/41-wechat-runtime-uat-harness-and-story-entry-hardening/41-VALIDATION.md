# Phase 41 Validation Strategy

**Created:** 2026-05-04  
**Source:** 41-RESEARCH.md validation architecture

## Validation Architecture

### Layer 1: Public Backend and Runtime

Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -Quick
```

Expected:

- Public backend health is `PASS` or explicitly `BLOCKED`.
- Flagship story runtime is found by code `east_west_war_and_coexistence` or title containing `東西方文明`.
- Runtime contains at least 5 chapters, at least one compiled step, and at least one content block.
- Media asset counts and availability states are recorded.

### Layer 2: Mini-program Build

Command:

```powershell
cd packages/client
npm run build:weapp
```

Expected:

- Command exits 0, or records only accepted known warning.
- `dist/app.js` exists.
- `dist/app.json` exists.
- `dist/pages/story/index.js` exists.
- `dist/project.config.json` exists.

### Layer 3: WeChat DevTools Evidence

Command:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -IncludeBuild -OpenDevTools
```

Expected:

- If DevTools CLI opens the project, record `PASS`.
- If DevTools CLI is missing or returns a workstation/permission error, record `BLOCKED`.
- If DevTools launch is not attempted, record `SKIP`.
- Do not record manual physical-device UAT as `PASS` unless actually performed.

## Requirement Mapping

| Requirement | Validation |
|-------------|------------|
| UAT-01 | Harness checks backend, runtime, build artifacts, and DevTools launch readiness. |
| PLAY-01 | Harness and story page diagnostics verify flagship story runtime entry and rendered story metadata. |
| PLAY-05 | Harness counts media availability/fallback states; story page and renderers show fallback/unavailable media states. |

## Stop Conditions

- Do not proceed to Phase 42 execution if `npm run build:weapp` fails from Phase 41 changes.
- Do not claim full WeChat/device UAT passed if DevTools could not open or no physical-device pass was actually observed.
- Do not commit secrets, tokens, provider payloads, or local absolute media paths into `41-UAT.md` or `41-VERIFICATION.md`.

---

*Phase: 41-wechat-runtime-uat-harness-and-story-entry-hardening*
