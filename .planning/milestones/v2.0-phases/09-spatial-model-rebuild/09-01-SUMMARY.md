# Phase 9 Wave 1 Summary

## Outcome

Wave 1 completed the backend and schema foundation for the rebuilt spatial model.

- MySQL canonical schema now models top-level `cities`, hierarchical `sub_maps`, spatially extended `pois`, and reusable ordered `content_asset_links`.
- Admin backend now supports coordinate normalization, metadata suggestion, city/sub-map CRUD, POI city/sub-map validation, and spatial attachment synchronization.
- Public backend now exposes the expanded city/sub-map/POI contract required by later admin UI and mini-program waves.

## Delivered Artifacts

- `scripts/local/mysql/init/02-live-backend-foundation.sql`
- `scripts/local/mysql/init/04-admin-control-plane-alignment.sql`
- `scripts/local/mysql/init/06-live-backend-mock-migration.sql`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/enums/CoordinateSystem.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/spatial/CoordinateNormalizationService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSubMapController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSpatialSupportController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/SubMapController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/CatalogFoundationServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- `mvn -q -DskipTests compile -f packages/server/pom.xml`
- `mvn -q test -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- `mvn -q test -f packages/server/pom.xml`

## Notes

- Added `mock-maker-subclass` under both backend test resource trees to avoid the Windows/JDK inline Byte Buddy attach failure.
- Adjusted `CatalogFoundationServiceImplTest` to initialize MyBatis-Plus lambda metadata before asserting generated wrapper SQL segments.
- Admin context tests still log Mongo unauthorized bootstrap warnings under the local profile, but they no longer fail the test run.
