---
status: foundation-handoff
phase: 30-storyline-mode-and-chapter-override-workbench
requirements: [STORY-02, STORY-04, LINK-02]
updated: 2026-04-29T10:02:20+08:00
---

# Phase 30 Handoff

Phase 30 delivers the story-mode and chapter override foundation: admin authors a storyline route strategy, chapter anchor, inherited POI/default flow relationship, override policy, replacement/append steps, and the public backend compiles that into a mini-program-facing runtime DTO.

## Delivered Artifacts

- `AdminStorylineModeController` and `AdminStorylineModeServiceImpl` expose `/api/admin/v1/storylines/{storylineId}/mode-workbench`.
- `ExperienceRuntimeResponse.StorylineRuntime` and `PublicExperienceServiceImpl.getStorylineRuntime` expose compiled public runtime data at `/api/v1/storylines/{storylineId}/runtime`.
- `StorylineModeWorkbench` gives operators a Traditional Chinese workbench for story mode settings, route arrangement, chapter anchors, inherited flow preview, and override authoring.
- `41-phase-30-storyline-mode-overrides.sql` seeds the first acceptance slice for `東西方文明的戰火與共生`.
- `smoke-phase-30-storyline-mode.ps1` verifies admin snapshot, public runtime, story override compilation, public no-status fields, and A-Ma POI runtime regression.

## Carried-Forward Contracts

| Contract | Consumer |
| --- | --- |
| Storyline mode config | Phase 34 mini-program route/runtime consumption |
| Chapter inherited flow and overrides | Phase 31 governance can inspect conflicts and usage |
| Story-specific pickups/challenges/rewards as steps | Phase 33 flagship content package can expand |
| Semantic exploration weights on story steps | Phase 32 dynamic progress denominator |
| Public runtime preview | Phase 34 mini-program baseline |

## Data Slice

- Storyline code: `east_west_war_and_coexistence`.
- Storyline title: `東西方文明的戰火與共生`.
- Chapter 1: `鏡海初戰：中葡首次海防對峙`.
- Anchor: `poi` -> `ama_temple`.
- Inherited default flow: `poi_ama_default_walk_in`.
- Story chapter flow: `story_east_west_ch01_flow`.
- Replacement override: `arrival_intro_media` -> `story_ch01_arrival_immersive_media`.
- Append steps: `story_ch01_mainline_overlays`, `story_ch01_side_pickups`, `story_ch01_hidden_challenge`, `story_ch01_reward_titles`.

## Non-Claims

- Phase 30 does not build global interaction/task governance; Phase 31 owns it.
- Phase 30 does not build traveler progress inspection; Phase 32 owns it.
- Phase 30 does not generate the complete five-chapter assets; Phase 33 owns it.
- Phase 30 does not claim full mini-program experiential acceptance; Phase 34 or later owns it.

## Operational Notes

- Local services were restarted from the current worktree and are listening on `8080` and `8081`.
- Logs are under `logs/local/phase30-public-8080.out.log` and `logs/local/phase30-admin-8081.out.log`.
- The smoke reads auth only from environment variables or ignored `tmp-admin-login.json`.
- Keep all SQL, JSON, CSV, and scripted Chinese content UTF-8 / utf8mb4; avoid inline PowerShell Chinese literals for data writes.

