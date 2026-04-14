---
phase: 05-cos-media-pipeline
created: 2026-04-12
status: active
---

# Phase 5 Context

## Goal

Add backend-managed Tencent COS upload and asset-resolution flows so admin-managed media can power the live mini-program.

## Brownfield Reality

- Phase 2 already established `content_assets` as the canonical MySQL asset metadata table and exposed admin CRUD for it, but asset records are still created manually instead of through real file uploads.
- The admin UI already has an Assets tab inside `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`, yet it only edits metadata fields and cannot upload files through the backend.
- The public backend already resolves `content_assets.canonical_url` into multiple traveler-facing DTOs, so the main missing gap is not public read semantics but a trustworthy admin-side upload pipeline.
- The mini-program currently hardcodes several Tencent COS asset URLs in `packages/client/src/constants/assetUrls.ts`, which suggests the target bucket is intended to remain public-readable after upload.
- COS credentials were provided by the user for runtime verification, but they must be consumed via local environment/runtime configuration only and must not be committed into tracked files.

## Execution Decision

Phase 5 will execute as one admin-first media pipeline cutover:

1. Add Tencent COS SDK/configuration support to the admin backend with environment-backed settings, deterministic object-key generation, content hashing, and canonical URL construction.
2. Implement authenticated admin upload endpoints that store files in COS and persist canonical metadata into `content_assets`.
3. Extend the admin UI Assets tab so operators can upload files directly, inspect the stored metadata/URL, and reuse uploaded assets across the existing content forms.
4. Keep public asset consumption on the existing `canonical_url` contract unless a real integration gap is found during smoke verification.
5. Verify the live chain locally against the real admin backend, MySQL, and COS bucket with repeatable smoke coverage.

## Acceptance Focus

- Admin uploads go through backend APIs into Tencent COS with automatic object-key generation and stable canonical URLs.
- `content_assets` rows are created automatically with bucket, region, object key, MIME type, size, checksum, etag, and publish-status metadata.
- Admin responses and existing public DTOs expose usable canonical asset URLs without reintroducing mock-only asset handling.
