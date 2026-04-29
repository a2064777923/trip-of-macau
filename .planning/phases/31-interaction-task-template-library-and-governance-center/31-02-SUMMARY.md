---
phase: 31-interaction-task-template-library-and-governance-center
plan: 31-02
subsystem: admin-backend
tags: [spring-boot, governance, conflicts, cross-domain]
requires:
  - phase: 31-01
    provides: template presets and usage contracts
provides:
  - Unified experience governance facade
  - Paginated governance item listing
  - Governance detail and conflict check endpoints
affects: [phase-31-ui, phase-32-progress, phase-34-runtime]
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminExperienceGovernanceService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceGovernanceServiceImpl.java
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminExperienceRequest.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminExperienceResponse.java
requirements-completed: [OPS-03, LINK-01]
completed: 2026-04-29
---

# Phase 31-02 Summary

Added a unified governance facade over reusable templates, flow steps, story overrides, indoor behavior references, and reward rules.

## Accomplishments

- Added paginated governance item query with filters for owner, template type, trigger, effect, reward type, status, story override, high risk, and conflict-only views.
- Added governance detail lookup with usage refs and conflict summaries.
- Added deterministic conflict checks for overlapping fullscreen effects, duplicate reward grants, disabled required steps, shared pickup policy mismatch, and high-risk template usage guard issues.
- Preserved existing indoor and reward governance centers; the new center aggregates rather than replaces them.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-31-template-governance.ps1` passed.

## Notes

- Conflict detection is an authoring-time safety signal, not a geospatial runtime collision simulator.
