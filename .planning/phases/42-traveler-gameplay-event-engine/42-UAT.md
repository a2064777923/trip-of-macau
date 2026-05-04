# Phase 42 Gameplay Event Engine UAT

Generated: 2026-05-04T15:52:52
Mode: Quick=False; IncludeBuild=True; RequireBackend=False
Locale: zh-Hant
Storyline code: east_west_war_and_coexistence

| Area | Check | Status | Evidence | Command |
| --- | --- | --- | --- | --- |
| public backend | health | PASS | health=UP | GET http://127.0.0.1:8080/actuator/health |
| auth | dev-bypass login | PASS | identity=phase42-gameplay-smoke; credential redacted | POST /user/login/dev-bypass |
| story runtime | storyline discovery | PASS | storyId=9; code=east_west_war_and_coexistence | GET /story-lines |
| story runtime | compiled runtime step | PASS | chapters=5; chapterId=311006; step=tap_intro; event=click_interacted | GET /storylines/{id}/runtime |
| session | start story session | PASS | storyId=9; sessionId=redacted | POST /storylines/{id}/sessions/start |
| event | submit gameplay event | PASS | eventType=click_interacted; status=synced; outcome=click | POST /storylines/{id}/sessions/{sessionId}/events |
| duplicate | idempotent duplicate submit | PASS | duplicate=True; eventStatus=already_synced | POST same clientEventId |
| exploration | dynamic exploration summary | PASS | progressPercent=0.0; completed=0; available=37 | GET /users/me/exploration |
| exit | exit story session | PASS | status=exited; exitClearedTemporaryState=True | POST /storylines/{id}/sessions/{sessionId}/exit |
| client build | mini-program build | PASS | npm run build:weapp exited 0. | npm run build:weapp |

Final outcome: PASS
