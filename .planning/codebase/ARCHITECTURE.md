# Architecture

## High-Level Topology

- The repository contains two user-facing application surfaces and two Java backend services.
- `packages/client` is the traveler-facing WeChat mini-program.
- `packages/server` is the public API for the traveler experience.
- `packages/admin/aoxiaoyou-admin-ui` is the operations/admin SPA.
- `packages/admin/aoxiaoyou-admin-backend` is the admin API and management backend.

## Core Architectural Style

- Both Java services follow a conventional Spring layered architecture:
  - Controller layer in `controller`
  - DTOs in `dto/request` and `dto/response`
  - Domain records in `entity`
  - Persistence adapters in `mapper`
  - Business logic in `service` and `service/impl`
  - Shared web/config/error code in `common`
- Both services use MyBatis-Plus for data access rather than Spring Data repositories.
- Both services wrap responses in a common response envelope via `common/api/ApiResponse.java` and `PageResponse.java`.

## Public App Flow

- App bootstrap begins in `packages/client/src/app.ts`.
- Navigation and page registration are centralized in `packages/client/src/app.config.ts`.
- Game state is primarily local-first and centralized in `packages/client/src/services/gameService.ts`.
- UI pages pull derived state from `gameService.ts` instead of directly hitting a backend for every user action.
- `packages/client/src/components/PageShell.tsx` applies cross-page display preferences such as interface mode, font scaling, and high-contrast class names.

## Map and Gameplay Flow

- The map screen in `packages/client/src/pages/map/index.tsx` is the heaviest interaction surface.
- It combines:
  - location acquisition through Taro
  - local city unlocking and progression state
  - POI marker derivation
  - indoor navigation handoff
  - check-in and reward flows
- Indoor navigation is a separate page in `packages/client/src/pages/map/indoor/index.tsx` that renders tile images and simulates AR localization.
- Asset indirection for remote marker/tabbar assets is centralized in `packages/client/src/constants/assetUrls.ts`.

## Admin UI Flow

- Admin entry is `packages/admin/aoxiaoyou-admin-ui/src/main.tsx`.
- Routing is defined in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`.
- The app uses a protected route gate that:
  - reads cached auth state
  - calls `getCurrentAdmin()`
  - redirects to `/login` on missing or invalid credentials
- Layout composition is handled by `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx`.
- Feature screens are grouped by business domain under `packages/admin/aoxiaoyou-admin-ui/src/pages`.

## Admin Backend Flow

- Admin authentication is implemented as a custom MVC interceptor in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java`.
- Spring Security exists but is reduced to stateless plus `permitAll()` in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/SecurityConfig.java`.
- The actual authorization boundary therefore depends on MVC path interception configured in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/WebConfig.java`.
- Domain services then execute query-and-transform logic, often mapping entities directly to DTOs inside service implementations such as `AdminCityServiceImpl.java` and `AdminAiServiceImpl.java`.

## Public Backend Flow

- Public API controllers in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller` call service interfaces.
- Service implementations such as `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PoiServiceImpl.java` use MyBatis-Plus query wrappers directly against mapper interfaces.
- Cross-cutting concerns include pagination, JSON serialization, global exception handling, CORS, and OpenAPI config under `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common`.

## State Management Patterns

- Mini-program UI state mixes React local state with persisted gameplay state in `gameService.ts`.
- There is also a Zustand store in `packages/client/src/store/userStore.ts`, but much of the current behavior still flows through manual `wx` storage reads/writes.
- Admin UI uses lightweight Zustand auth state in `packages/admin/aoxiaoyou-admin-ui/src/stores/auth.ts`.
- Admin server-side state remains request-scoped and database-backed, with JWT-derived user context attached to the request.

## Architectural Oddities

- The public backend `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` references `AdminAuthInterceptor`, but no matching class exists under `packages/server/src/main/java`; that coupling appears accidental or incomplete.
- The mini-program currently mixes live API wrappers in `packages/client/src/services/api.ts` with extensive mock/stateful simulation in `packages/client/src/services/gameService.ts`, so the effective runtime architecture is partly local app simulation and partly remote-service-ready scaffolding.
