---
status: testing
phase: 15-indoor-interaction-rule-authoring
source:
  - 15-01-SUMMARY.md
  - 15-02-SUMMARY.md
  - 15-03-SUMMARY.md
started: 2026-04-16T07:38:00+08:00
updated: 2026-04-16T09:02:00+08:00
---

## Current Test

number: 2
name: Trigger chain authoring
expected: |
  In the same indoor workspace, create one behavior profile with at least two trigger steps.
  Make step 2 depend on step 1 by selecting `dependsOnTriggerId` in the trigger-chain editor.
  Confirm the UI remains understandable without switching to raw JSON mode.
  Save and reload the node, then verify the prerequisite chain still points to the correct prior trigger.
awaiting: user response
notes: |
  During Test 2 the user reported two UI regressions:
  1. `新增行為` button appeared to do nothing in the indoor rule editor.
  2. selected-asset cards let long paths overflow their card bounds.
  Both were patched on 2026-04-16 and now require user retest in the live admin UI.
  Internal headless verification on 2026-04-16 confirmed:
  - clicking `新增行為` now reveals the editor and advances from `behavior-2` to `behavior-3`
  - switching the behavior selector back to `互動行為 1` updates the visible `behaviorCode` field back to `behavior-1`

## Tests

### 1. Schedule-gated overlay
expected: Open `/admin#/space/map-tiles`, switch to `lisboeta_macau / 1F`, create or edit one node with `presentationMode = overlay` and `overlayType = polygon`, add a `schedule_window` rule through the structured appearance editor, draw or refine the overlay polygon from the floor canvas helper, save successfully, then reopen the same node and confirm the presentation mode, overlay geometry, and schedule rule all round-trip correctly.
result: pass

### 2. Trigger chain authoring
expected: In the same indoor workspace, create one behavior profile with at least two trigger steps. Make step 2 depend on step 1 by selecting `dependsOnTriggerId` in the trigger-chain editor. Confirm the UI remains understandable without switching to raw JSON mode. Save and reload the node, then verify the prerequisite chain still points to the correct prior trigger.
result: [pending]

### 3. Motion path persistence
expected: Add one effect profile that includes `path_motion`, arm path picking, click at least four points on the floor canvas, save the resulting motion path, then reopen the node and confirm `points`, `durationMs`, `holdMs`, `loop`, and `easing` all persist, while the current draft path preview is rendered on the canvas before saving.
result: [pending]

### 4. Draft recovery
expected: Start editing a node and change at least one structured rule field plus one geometry or path field, leave the page or refresh before saving, then return to the floor workspace and restore the auto-saved draft from the draft area. The restored draft should still contain the structured rule profile, geometry, and marker basics.
result: [pending]

### 5. CSV rich preview
expected: Download the CSV template from the indoor workspace, confirm the template includes `presentationMode`, `appearancePresetCode`, `triggerTemplateCode`, `effectTemplateCode`, and `inheritMode`, then upload a CSV preview file and confirm those fields are visible in the preview table before import.
result: [pending]

### 6. Raw JSON is secondary
expected: Verify that appearance, trigger, effect, and path authoring can be completed through structured editors, confirm raw JSON is not required for the primary authoring path, and confirm any remaining JSON fields are explicitly secondary or advanced metadata rather than the main interaction-rule workflow.
result: [pending]

## Summary

total: 6
passed: 1
issues: 0
pending: 5
skipped: 0
blocked: 0

## Gaps

None.
