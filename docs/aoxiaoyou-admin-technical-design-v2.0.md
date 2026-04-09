# 澳小遊后台管理系统 — 技术设计 v2.0

> **版本**: v2.0  
> **日期**: 2026-04-08  
> **状态**: 正式底座（已部署验证）

---

## 一、系统架构概览

### 1.1 整体架构

```
┌──────────────────────────────────────────────────────┐
│                    前端 (React + Vite)                  │
│   ProLayout / ProTable / Ant Design / HashRouter     │
├──────────────────────────────────────────────────────┤
│                    后端 (Spring Boot 3.x)              │
│   MyBatis-Plus / JWT / REST API                     │
├──────────────────────────────────────────────────────┤
│               CloudBase 基础设施                       │
│   MySQL (40+ 表) | 静态托管 (/admin) | Cloud Run     │
└──────────────────────────────────────────────────────┘
```

### 1.2 信息架构（六大域分组导航）

| 分组 | 路由前缀 | 子模块 |
|------|----------|--------|
| 地图与空间管理 | `/space` | 城市、瓦片地图、POI、室内建筑、AI 导航 |
| 故事与内容管理 | `/content` | 故事线、章节、活动、媒体库 |
| 收集与激励管理 | `/collection` | 收集物、徽章、奖励 |
| 用户与进度管理 | `/users` | 用户总览、城市/故事线/收集进度、触发日志 |
| 测试与运营管理 | `/ops` | 测试控制台、活动运营、沙盒 |
| 系统与权限管理 | `/system` | 管理员、角色、权限矩阵、配置、审计日志 |

---

## 二、模块边界与接口设计

### 2.1 地图与空间管理域

#### 城市管理 (`AdminMapController`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/v1/map/cities` | 城市列表分页 |
| GET | `/api/admin/v1/map/cities/{id}` | 城市详情 |
| POST | `/api/admin/v1/map/cities` | 创建城市 |
| PUT | `/api/admin/v1/map/cities/{id}` | 更新城市 |
| PUT | `/api/admin/v1/map/cities/{id}/publish` | 发布城市 |

#### 室内地图 (`AdminIndoorController`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/v1/map/indoor/buildings` | 建筑物列表 |
| POST | `/api/admin/v1/map/indoor/buildings` | 创建建筑 |
| PUT | `/api/admin/v1/map/indoor/buildings/{id}` | 更新建筑 |

### 2.2 故事与内容管理域

| 模块 | Controller | 核心接口 |
|------|------------|----------|
| 故事线 | `AdminStoryLineController` | CRUD + 章节嵌套 |
| 章节 | `AdminStoryChapterController` | CRUD + 锚点绑定 |
| 活动 | `AdminOperationsController` | 列表 |

### 2.3 收集与激励管理域 (`AdminCollectibleController`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/v1/collectibles/items` | 收集物列表 |
| POST | `/api/admin/v1/collectibles/items` | 新增收集物 |
| GET | `/api/admin/v1/collectibles/badges` | 徽章列表 |
| POST | `/api/admin/v1/collectibles/badges` | 新增徽章 |
| GET/POST/DELETE | `/api/admin/v1/system/rewards` | 奖励 CRUD（已存在） |

### 2.4 RBAC 权限系统 (`AdminRbacController`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/v1/system/admin-users` | 管理员列表 |
| GET/POST | `/api/admin/v1/system/roles` | 角色列表/创建 |
| GET | `/api/admin/v1/system/permissions` | 权限点列表 |
| GET | `/api/admin/v1/system/roles/{id}/permissions` | 角色权限 |
| PUT | `/api/admin/v1/system/roles/{id}/permissions` | 更新角色权限 |

### 2.5 测试控制台 (`AdminTestConsoleController`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/PUT | `.../mock-location` | 模拟位置 |
| POST | `.../level` | 等级调整 |
| POST | `.../stamps/grant` | 发放印章 |
| POST | `.../stamps/batch-grant` | 批量发章 |
| DELETE | `.../stamps` | 清空印章 |
| GET | `.../stamps/summary` | 印章概览 |
| POST | `.../progress/reset` | 重置进度 |

---

## 三、数据库模型

### 3.1 已建表清单（Phase 1-5 全部完成）

**基础设施表**：
- `cities`, `poi_types`, `poi_categories`
- `map_tile_configs`（修复了 500 问题）
- `buildings`, `indoor_maps`, `indoor_floors`, `indoor_nodes`

**RBAC 表**：
- `roles`, `permissions`, `role_permissions`, `admin_roles`

**故事内容表**：
- `storyline_city_relations`, `storyline_anchor_bindings`
- `chapter_branches`, `chapter_tasks`, `campaigns`

**收集激励表**：
- `collectibles`, `collectible_series`, `badges`, `badge_rules`

**用户进度表**：
- `user_city_progress`, `user_story_progress`
- `user_collectible_progress`, `user_badges`, `user_trigger_logs`

**AI 配置表**：
- `ai_provider_configs`, `ai_navigation_policies`

### 3.2 升级的现有表

- `pois`: 新增 `city_code`, `parent_poi_id`, `map_tile_x/y/z`, `indoor_building_id/floor_id/node_id`
- `rewards`: 保持兼容，预留 `reward_type`, `rarity`, `valid_from/until`

### 3.3 种子数据

- 3 个城市：澳门(已发布)、香港(草稿)、珠海(草稿)
- 4 个 POI 类型：景点、触发物、控制点、标记点
- 5 个角色：超级管理员、内容编辑、运营人员、只读查看、测试人员
- 20 个权限点（覆盖六大域的菜单/API/按钮）
- 2 条地图瓦片配置记录

完整 ER 图和字段定义参见 [`数据库设计-v2.0.md`](./数据库设计-v2.0.md)。

---

## 四、部署信息

| 组件 | 详情 |
|------|------|
| 环境 ID | `macau-trip-2gn2zm5jefa4a987`（别名: macau-trip） |
| 域名 | ap-shanghai |
| 前端 URL | `https://macau-trip-2gn2zm5jefa4a987-1301163924.tcloudbaseapp.com/admin/#/login` |
| 后端 URL | `https://aoxiaoyou-admin-api-243434-4-1301163924.sh.run.tcloudbase.com` |
| 路由模式 | HashRouter（解决 SPA 直连 404） |
| 数据库 | CloudBase MySQL，40+ 张表 |
| 登录帐号 | admin / admin123 |

---

## 五、已知问题与后续计划

### 已修复
- ✅ SPA 直连路由 404 → 改用 HashRouter
- ✅ map-tiles 500 → 创建缺失的 `map_tile_configs` 表并写入示例数据
- ✅ MapTileConfig 实体与线上表结构不对齐 → 重写字段映射

### 待完善（下一批次）
- [ ] RBAC 权限拦截中间件实现
- [ ] 角色权限矩阵页面（树形选择）
- [ ] POI 层级关系父子联动 UI
- [ ] 室内地图楼层切换预览组件
- [ ] AI 导航配置测试面板
- [ ] 媒体资源库与引用追踪
- [ ] 批量导入导出功能
- [ ] 操作审计日志增强
- [ ] Swagger/OpenAPI 文档同步更新

---

## 六、文件索引

### 后端新增核心文件
```
aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/
├── controller/
│   ├── AdminMapController.java          ← 城市管理
│   ├── AdminIndoorController.java        ← 室内建筑
│   ├── AdminCollectibleController.java   ← 收集物/徽章
│   └── AdminRbacController.java         ← RBAC 权限
├── entity/
│   ├── City.java                        ← 城市实体
│   ├── Role.java                        ← 角色实体
│   ├── Permission.java                  ← 权限实体
│   ├── AdminUser.java                   ← 管理员实体
│   ├── Collectible.java                 ← 收集物实体
│   ├── Badge.java                       ← 徽章实体
│   ├── Building.java                    ← 建筑实体
│   └── MapTileConfig.java               ← 修复后的瓦片配置
├── mapper/
│   ├── CityMapper / RoleMapper / PermissionMapper
│   ├── AdminUserMapper / CollectibleMapper
│   ├── BadgeMapper / BuildingMapper
│   └── ... （全部已有）
├── service/
│   ├── AdminCityService.java            ← 城市服务
│   ├── AdminRbacService.java            ← RBAC 服务
│   ├── AdminCollectibleService.java     ← 收集服务
│   ├── AdminIndoorService.java          ← 室内服务
│   └── impl/*                           ← 全部实现
└── dto/
    ├── request/AdminCityUpsertRequest.java
    └── response/
        ├── AdminCityResponse.java
        ├── RoleResponse / PermissionResponse / AdminUserResponse
        ├── CollectibleResponse / BadgeResponse / BuildingResponse
        └── ...
```

### 前端新增页面
```
aoxiaoyou-admin-ui/src/
├── pages/
│   ├── MapSpace/
│   │   ├── CityManagement.tsx           ← 城市管理页
│   │   └── IndoorBuildingManagement.tsx  ← 室内建筑页
│   └── Collectibles/
│       ├── CollectibleManagement.tsx     ← 收集物管理页
│       └── BadgeManagement.tsx           ← 徽章管理页
├── services/api.ts                       ← 扩展城市/RBAC/收集物/建筑接口
├── App.tsx                              ← 接入真实页面替换占位符
├── main.tsx                             ← 改为 HashRouter
└── layouts/DefaultLayout.tsx            ← 六大域分组导航
```

### 文档
```
docs/
├── 数据库设计-v2.0.md                    ← 完整数据库模型（新建）
├── 后台现状与缺口清单_2026-04-08.md      ← 盘点报告（新建）
└── ... （原有文档待同步更新）
```
