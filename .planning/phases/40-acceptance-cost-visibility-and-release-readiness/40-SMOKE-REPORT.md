# Phase 40 Smoke Report

Generated: 2026-05-03T21:24:21
Mode: Quick=True; IncludeBuilds=False; IncludeLive=False; IncludeAdminQaActions=False

| Area | Check | Status | Evidence | Command |
| --- | --- | --- | --- | --- |
| planning | required artifacts | PASS | Required planning and verification files exist. |  |
| phase 36 | material production validate-only | PASS | Command exited 0. | & .\scripts\local\smoke-phase-36-material-production.ps1 -ValidateOnly |
| phase 36 | live material and COS | SKIP | set PHASE40_INCLUDE_LIVE=true to enable provider/COS live checks. |  |
| phase 37 | material QA read-only | PASS | Command exited 0. | & .\scripts\local\smoke-phase-37-material-qa.ps1 -SkipActions |
| phase 37 | material QA actions | SKIP | set PHASE40_INCLUDE_ADMIN_QA_ACTIONS=true to enable QA mutation checks. |  |
| phase 38 | public runtime assets | PASS | Command exited 0. | & .\scripts\local\smoke-phase-38-public-runtime-assets.ps1 |
| phase 39 | story mode smoke | PASS | Command exited 0. | & .\scripts\local\smoke-phase-39-mini-program-story-mode.ps1 |
| client | phase 39 npm smoke alias | PASS | Command exited 0. | npm run smoke:phase39:story-mode |
| admin ai | observability API | PASS | Overview available; jobs=5; logs=5. |  |
| client | mini-program build | SKIP | set PHASE40_INCLUDE_BUILDS=true or omit -Quick to run npm run build:weapp. |  |
| admin ui | build | SKIP | set PHASE40_INCLUDE_BUILDS=true to run npm run build. |  |
| public backend | compile | SKIP | set PHASE40_INCLUDE_BUILDS=true to run Maven compile. |  |
| admin backend | compile | SKIP | set PHASE40_INCLUDE_BUILDS=true to run Maven compile. |  |

Final outcome: PASS_WITH_SKIPS_ALLOWED

