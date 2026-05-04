# Mini-program WeChat UAT

## Local Backend

Start the public backend on `8080` before running the story runtime checks. Verify health:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
```

Discover the flagship story id by `east_west_war_and_coexistence`:

```powershell
Invoke-RestMethod "http://127.0.0.1:8080/api/v1/story-lines?locale=zh-Hant"
```

Fetch the runtime using the discovered id:

```powershell
Invoke-RestMethod "http://127.0.0.1:8080/api/v1/storylines/{id}/runtime?locale=zh-Hant"
```

Local/devtools may use dev-bypass only when explicitly enabled. Experience/production must not use dev-bypass.

## Mini-program Build

Build the WeChat mini-program output:

```powershell
cd D:\Archive\trip-of-macau\packages\client
npm run build:weapp
```

The expected output is `packages/client/dist`, including `app.js`, `app.json`, `pages/story/index.js`, and `project.config.json`.

## WeChat DevTools

Open the built project in WeChat DevTools:

```powershell
D:/Software/微信web开发者工具/cli.bat open --project D:/Archive/trip-of-macau/packages/client/dist --lang zh
```

If the CLI reports a local port or permission error, record it as `BLOCKED` rather than `PASS`. Phase 41 only treats DevTools as passed when the project is actually opened.

## Physical Device Notes

Physical devices usually cannot access host `127.0.0.1`. Use a LAN IP, a local proxy, a tunnel, or a deployed backend and set `API_BASE_URL` / `PHASE41_PUBLIC_BASE_URL` accordingly.

For experience and production builds, real WeChat login must be used. Do not rely on local dev-bypass for production acceptance.

## Environment Matrix

| Environment | Backend target | Auth expectation | Notes |
| --- | --- | --- | --- |
| Local browser/devtools | `127.0.0.1` or LAN backend | Optional explicit dev-bypass | Only for developer verification. |
| WeChat DevTools | Local, LAN, tunnel, or deployed backend | Real login or explicit local dev-bypass | Use diagnostics strip on the story page to confirm source. |
| Experience build | Deployed backend | Real WeChat auth only | No dev-bypass. |
| Production | Production backend | Real WeChat auth only | No dev-bypass. |

## Phase 41 Acceptance Commands

Run the automated Phase 41 UAT harness:

```powershell
cd D:\Archive\trip-of-macau\packages\client
npm run smoke:phase41:wechat-runtime
```

Run with a client build check:

```powershell
cd D:\Archive\trip-of-macau
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -IncludeBuild
```

Optionally launch WeChat DevTools after build:

```powershell
cd D:\Archive\trip-of-macau
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -IncludeBuild -OpenDevTools
```

## Caveats

Phase 41 verifies the story runtime entry, media fallback visibility, WeChat build readiness, and DevTools launch readiness. It does not prove advanced AR, speech, puzzle gameplay, indoor visual positioning, or the full future gameplay engine.
