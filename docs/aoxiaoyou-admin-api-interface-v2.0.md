# 澳小遊后台管理系统 - API 接口设计 V2.0

**版本**: V2.0  
**日期**: 2026-04-08  
**状态**: 与当前后台代码实现同步的正式底座版

---

## 一、统一约定

- 基础前缀：`/api/admin/v1`
- 认证方式：`Authorization: Bearer <token>`
- 响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页结构：

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "total": 100,
  "totalPages": 5,
  "list": []
}
```

---

## 二、已实现接口域

### 2.1 认证
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /auth/me`
- `POST /auth/logout`

### 2.2 地图与空间管理

#### 城市管理
- `GET /map/cities`
- `GET /map/cities/{id}`
- `POST /map/cities`
- `PUT /map/cities/{id}`
- `PUT /map/cities/{id}/publish`

#### 室内建筑
- `GET /map/indoor/buildings`
- `POST /map/indoor/buildings`
- `PUT /map/indoor/buildings/{id}`

#### 地图瓦片
- `GET /system/map-tiles`

**地图瓦片响应字段（当前已实现）**
- `id`
- `mapId`
- `style`
- `cdnBase`
- `controlPointsUrl`
- `poisUrl`
- `zoomMin`
- `zoomMax`
- `defaultZoom`
- `centerLat`
- `centerLng`
- `version`
- `status`
- `updatedAt`

### 2.3 故事与内容管理
- `GET /storylines`
- `GET /storylines/{id}`
- `POST /storylines`
- `PUT /storylines/{id}`
- `DELETE /storylines/{id}`
- `GET /storylines/{storylineId}/chapters`
- `POST /storylines/{storylineId}/chapters`
- `PUT /storylines/{storylineId}/chapters/{chapterId}`
- `DELETE /storylines/{storylineId}/chapters/{chapterId}`
- `GET /operations/activities`

### 2.4 收集与激励管理

#### 收集物
- `GET /collectibles/items`
- `POST /collectibles/items`

#### 徽章
- `GET /collectibles/badges`
- `POST /collectibles/badges`

#### 奖励
- `GET /system/rewards`
- `POST /system/rewards`
- `PUT /system/rewards/{rewardId}`
- `DELETE /system/rewards/{rewardId}`

### 2.5 用户与进度
- `GET /users`
- `GET /users/{id}`
- `POST /users/{id}/test-flag`

### 2.6 测试控制台
- `GET /test-console/accounts`
- `PUT /test-console/accounts/{id}/mock`
- `POST /test-console/accounts/{id}/level`
- `POST /test-console/accounts/{id}/stamps/grant`
- `POST /test-console/accounts/{id}/stamps/batch-grant`
- `DELETE /test-console/accounts/{id}/stamps`
- `GET /test-console/accounts/{id}/stamps/summary`
- `POST /test-console/accounts/{id}/progress/reset`
- `GET /test-console/accounts/{id}/logs`

### 2.7 系统与权限管理（当前已实现）

#### 管理员账号
- `GET /system/admin-users`

#### 角色与权限
- `GET /system/roles`
- `POST /system/roles`
- `GET /system/permissions`
- `GET /system/roles/{roleId}/permissions`
- `PUT /system/roles/{roleId}/permissions`

#### 配置与审计
- `GET /system/configs`
- `GET /system/audit-logs`

---

## 三、重点请求示例

### 3.1 新建角色

`POST /api/admin/v1/system/roles`

```json
{
  "roleCode": "city_editor",
  "roleName": "城市编辑",
  "description": "负责城市内容与地图配置",
  "sortOrder": 10,
  "isSystem": 0,
  "status": "1"
}
```

### 3.2 保存角色权限矩阵

`PUT /api/admin/v1/system/roles/2/permissions`

```json
[2,3,4,5,8,9,10]
```

### 3.3 新建城市

`POST /api/admin/v1/map/cities`

```json
{
  "code": "hongkong",
  "nameZh": "香港",
  "nameEn": "Hong Kong",
  "countryCode": "HK",
  "centerLat": 22.3193,
  "centerLng": 114.1694,
  "defaultZoom": 13,
  "unlockType": "condition",
  "coverImageUrl": "https://.../hongkong-cover.png",
  "descriptionZh": "国际大都市"
}
```

---

## 四、待补齐但已建模的接口

以下接口已在数据库和页面信息架构中预留，下一阶段继续实现：

- 地图校准：`/map/calibrations/*`
- 控制点管理：`/map/control-points/*`
- 室内楼层与节点：`/map/indoor/floors/*`, `/map/indoor/nodes/*`
- AI 导航配置：`/map/ai-config/*`
- 用户城市/故事进度：`/users/progress/*`
- 徽章规则引擎：`/collectibles/badge-rules/*`
- 媒体资源库：`/content/media/*`
- 批量导入导出：`/imports/*`, `/exports/*`

---

## 五、与旧版文档差异

相比 V1.0：
- 菜单结构已从扁平模块升级为六大域分组式结构
- 新增地图与空间、收集与激励、系统与权限三大新接口簇
- `map-tiles` 已从异常接口恢复为可用接口
- 新增角色权限矩阵和管理员列表能力
