# 澳小遊后台管理系统 API 设计 V1.1

> 版本：V1.1  
> 更新日期：2026-04-06  
> 更新内容：补充微信小程序字段、新增接口分层、完善核心业务实体

---

## 目录
1. [接口分层规范](#接口分层规范)
2. [通用规范](#通用规范)
3. [模块清单](#模块清单)
4. [实体字段详解](#实体字段详解)
5. [接口详细设计](#接口详细设计)
6. [微信小程序专属字段](#微信小程序专属字段)

---

## 接口分层规范

为确保后台管理系统与小程序客户端的职责清晰，所有接口按以下前缀分层：

| 层级 | 前缀 | 说明 | 访问角色 |
|------|------|------|----------|
| **Admin** | `/api/admin/v1/*` | 后台管理系统专用接口 | 运营人员、管理员、测试人员 |
| **App** | `/api/app/v1/*` | 小程序客户端接口 | 普通用户、测试用户 |
| **Public** | `/api/public/v1/*` | 公开接口（无需登录） | 任何人 |
| **Internal** | `/api/internal/v1/*` | 内部服务间调用 | 微服务内部 |

### 重要约定
1. **Admin 与 App 数据隔离**：Admin 看到的是运营视角（全量数据、测试账号标记），App 看到的是用户视角（仅自己的数据）
2. **测试账号能力**：TestConsole 的接口只允许通过 `isTestAccount=true` 的账号调用，或在 Admin 后台由 TESTER/SUPER_ADMIN 角色发起
3. **权限校验**：所有接口需校验 JWT Token 中的角色权限

---

## 通用规范

### 请求头
```http
Authorization: Bearer {jwt_token}
Content-Type: application/json
X-Request-Id: {uuid}           // 请求追踪ID
X-Client-Version: 1.0.0        // 客户端版本（App接口）
```

### 统一响应格式
```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "uuid",
  "timestamp": "2026-04-06T23:30:00+08:00"
}
```

### 分页结构
```json
{
  "list": [],
  "pagination": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrev": false
  }
}
```

### 时间格式
统一使用 ISO 8601 格式，带时区：`2026-04-06T23:30:00+08:00`

---

## 模块清单

| 模块 | 说明 | 接口数量 |
|------|------|----------|
| **Admin-Auth** | 后台登录、权限 | 4 |
| **Admin-User** | 用户管理（含测试账号标记） | 6 |
| **Admin-POI** | POI点位管理 | 7 |
| **Admin-Storyline** | 故事线管理 | 6 |
| **Admin-Campaign** | 活动与广告配置 | 5 |
| **Admin-Reward** | 奖励配置 | 5 |
| **Admin-TestConsole** | 测试控制台（模拟定位、印章发放） | 8 |
| **Admin-AuditLog** | 操作审计日志 | 3 |
| **App-Auth** | 小程序登录 | 3 |
| **App-User** | 用户个人信息、进度 | 4 |
| **App-CheckIn** | 打卡核心接口 | 3 |
| **App-Storyline** | 故事线查询（带解锁状态） | 4 |
| **App-Reward** | 背包、奖励领取 | 3 |
| **App-Config** | 全局配置 | 2 |

---

## 实体字段详解

### 1. User（用户）

**Admin视角（后台管理）**
```yaml
userId: string          # 内部用户ID
openId: string          # 微信openid
unionId: string         # 微信unionId（多应用打通）
platform: string        # 平台：wechat_miniprogram
nickname: string        # 微信昵称
avatarUrl: string       # 微信头像URL
phone: string           # 手机号（加密存储）
isTestAccount: boolean # 是否测试账号
accountStatus: enum     # 账号状态：active / suspended / deleted
createdAt: datetime
updatedAt: datetime
lastLoginAt: datetime
loginCount: integer
```

**App视角（小程序端）**
```yaml
userId: string
nickname: string
avatarUrl: string
phone: string  # 脱敏显示
isTestAccount: boolean  # 前端显示测试模式标识
level: integer
currentExp: integer
nextLevelExp: integer
totalStamps: integer
unlockedStorylines: integer
badges: array
```

### 2. UserProgress（用户进度）- 新增核心实体

```yaml
progressId: string
userId: string
storylineId: string
currentPoiId: string           # 当前进行到的POI
completedPoiIds: string[]       # 已完成的POI列表
isCompleted: boolean            # 故事线是否完成
completedAt: datetime
completionTimeSeconds: integer  # 完成耗时（秒）
rewardClaimed: boolean          # 奖励是否已领取
claimedAt: datetime
createdAt: datetime
updatedAt: datetime
```

### 3. POI（点位）

```yaml
# 基础信息
poiId: string
name: string
subtitle: string
description: string
regionCode: string        # 区域编码：macau_central / macau_taipa / macau_cotai

# 地理位置（核心）
latitude: number          # WGS84坐标
longitude: number
gcj02Latitude: number   # 国测局坐标（小程序地图使用）
gcj02Longitude: number
address: string

# 地理围栏（小程序打卡必需）
geofenceRadius: number          # 打卡半径（米），默认50米
checkInMethod: enum             # 打卡方式
  - gps_only          # 仅GPS
  - bluetooth_beacon  # 蓝牙信标
  - qr_code           # 扫码
  - manual_admin      # 管理员手动（TestConsole）
beaconInfo: object              # iBeacon信息（如果checkInMethod=bluetooth_beacon）
  uuid: string
  major: number
  minor: number

# 媒体
coverImageUrl: string
imageUrls: string[]
audioGuideUrl: string      # 语音导览
videoUrl: string
arContentUrl: string       # AR内容（如有）

# 业务属性
poiType: enum
  - landmark      # 地标
  - shop          # 商铺
  - restaurant    # 餐饮
  - museum        # 博物馆
  - story_point   # 故事点
  - event_venue   # 活动场地
tags: string[]
difficulty: enum            # 打卡难度
  - easy
  - medium
  - hard

# 时间
openTime: string            # 开放时间描述，如"09:00-18:00"
suggestedVisitMinutes: number

# 状态
status: enum
  - draft        # 草稿
  - pending      # 待审核
  - published    # 已发布
  - offline      # 已下线
  - deleted      # 已删除

# 统计
checkInCount: number        # 打卡次数
favoriteCount: number       # 收藏次数

# 元数据
createdBy: string
createdAt: datetime
updatedBy: string
updatedAt: datetime
```

### 4. Storyline（故事线）

```yaml
storylineId: string
name: string
description: string
coverImageUrl: string
bannerImageUrl: string

# 关联POI
poiSequence: object[]      # POI顺序及解锁条件
  - poiId: string
    sequence: number        # 顺序号
    unlockCondition: object   # 解锁条件
      type: enum
        - none             # 无限制
        - complete_prev    # 完成上一个POI
        - wait_hours       # 等待N小时
        - specific_time    # 特定时间
      params: object
    rewardStampId: string   # 完成此POI获得的印章

# 故事线奖励
completionReward: object
  stampId: string
  badgeId: string
  couponId: string
  points: number

difficulty: enum            # 整体难度
  - easy
  - medium
  - hard
estimatedDurationMinutes: number   # 预计耗时

tags: string[]
category: enum              # 故事线分类
  - historical      # 历史
  - cultural        # 文化
  - food            # 美食
  - adventure       # 探险
  - family          # 亲子
  - romance         # 情侣

# 状态
status: enum
  - draft
  - pending
  - published
  - offline
  - archived

# 时间控制
publishAt: datetime         # 定时发布
startAt: datetime          # 活动开始时间
endAt: datetime            # 活动结束时间

# 统计
participationCount: number    # 参与人数
completionCount: number       # 完成人数
averageCompletionTime: number  # 平均完成时间（秒）

# 元数据
createdBy: string
createdAt: datetime
updatedBy: string
updatedAt: datetime
```

### 5. CheckInRecord（打卡记录）- 新增核心实体

```yaml
checkInId: string
userId: string
poiId: string
storylineId: string       # 当前进行的故事线（可选）

# 打卡信息
checkInType: enum
  - gps          # GPS定位打卡
  - beacon       # 蓝牙信标打卡
  - qr_code      # 扫码打卡
  - mock         # 模拟定位（TestConsole）
  - manual       # 管理员手动补录

# 位置信息（GPS打卡时）
userLatitude: number
userLongitude: number
userAccuracy: number       # 定位精度（米）
distanceToPoi: number     # 与POI的距离（米）

# 蓝牙信息（beacon打卡时）
beaconUuid: string
beaconMajor: number
beaconMinor: number
beaconRssi: number         # 信号强度

# 扫码信息（qr_code打卡时）
qrCodeData: string

# 设备信息
deviceInfo: object
  brand: string
  model: string
  system: string
  platform: string         # 平台：ios / android / devtools
  sdkVersion: string       # 微信基础库版本

# 验证结果
isValid: boolean          # 是否验证通过
validationError: string   # 验证失败原因（如有）

# 奖励
rewardGranted: object
  stampId: string
  badgeId: string
  points: number

# 元数据
createdAt: datetime
ipAddress: string          # 记录IP（安全审计）
```

### 6. Reward（奖励）

```yaml
rewardId: string
name: string
description: string

# 奖励类型
rewardType: enum
  - stamp        # 印章/徽章
  - badge        # 成就徽章
  - coupon       # 优惠券
  - gift         # 实物礼品
  - points       # 积分
  - virtual_item # 虚拟道具

# 奖励配置（根据类型不同）
rewardConfig: object
  # stamp/badge 类型
  iconUrl: string
  rarity: enum    # common / rare / epic / legendary
  
  # coupon 类型
  discountType: enum    # percentage / fixed_amount
  discountValue: number
  minOrderAmount: number
  validDays: number
  
  # gift 类型
  skuId: string
  stock: number
  
  # points 类型
  points: number

# 获取条件
acquisitionCondition: object
  type: enum
    - check_in_specific_poi    # 在指定POI打卡
    - complete_storyline       # 完成故事线
    - collect_all_stamps       # 收集所有印章
    - reach_level              # 达到等级
    - consecutive_check_in     # 连续打卡
  params: object

# 有效期
validityType: enum
  - permanent      # 永久有效
  - fixed_period # 固定时间段
  - relative_days # 获得后N天内有效
validityStart: datetime
validityEnd: datetime
validityDays: number

# 状态
status: enum
  - draft
  - active
  - paused
  - expired
  - archived

# 库存（实物类奖励）
stock: object
  total: number
  remaining: number
  claimed: number

# 统计
claimCount: number
useCount: number

# 元数据
createdBy: string
createdAt: datetime
updatedBy: string
updatedAt: datetime
```

### 7. Stamp（印章/徽章）- 用户背包实例

```yaml
stampId: string
userId: string

# 关联信息
rewardId: string        # 关联的奖励定义
sourceType: enum        # 获得来源
  - check_in           # 打卡获得
  - storyline_complete # 完成故事线
  - manual_grant       # 手动发放（TestConsole）
  - campaign           # 活动赠送
  - system_compensation # 系统补偿
sourceId: string        # 来源ID（如checkInId、storylineId）

# 印章状态
status: enum
  - active       # 有效
  - used         # 已使用
  - expired      # 已过期
  - revoked      # 已撤销

# 时间
acquiredAt: datetime
usedAt: datetime
expiredAt: datetime
revokedAt: datetime
revokeReason: string

# 元数据
createdAt: datetime
updatedAt: datetime
```

---

## 接口详细设计

### 接口分层总览

```yaml
# Admin 接口（后台管理）
/api/admin/v1/auth/*
/api/admin/v1/users/*
/api/admin/v1/pois/*
/api/admin/v1/storylines/*
/api/admin/v1/rewards/*
/api/admin/v1/campaigns/*
/api/admin/v1/test-console/*
/api/admin/v1/audit-logs/*

# App 接口（小程序客户端）
/api/app/v1/auth/*
/api/app/v1/users/*
/api/app/v1/check-in/*
/api/app/v1/storylines/*
/api/app/v1/rewards/*
/api/app/v1/config

# Public 接口（公开）
/api/public/v1/health
/api/public/v1/config
```

---

### Admin 接口详情

#### 1. Auth 模块

```yaml
# POST /api/admin/v1/auth/login
# 后台管理员登录
Request:
  username: string
  password: string
  captchaToken: string    # 图形验证码token
  captchaCode: string     # 验证码答案

Response:
  token: string           # JWT Token
  refreshToken: string
  expiresIn: number       # 过期时间（秒）
  user:
    userId: string
    username: string
    realName: string
    roles: string[]       # SUPER_ADMIN, CONTENT_OPERATOR, etc.
    permissions: string[] # 权限列表

# GET /api/admin/v1/auth/me
# 获取当前登录用户信息
Response: 同上 user 对象

# POST /api/admin/v1/auth/refresh
# 刷新Token
Request:
  refreshToken: string

Response:
  token: string
  refreshToken: string
  expiresIn: number

# POST /api/admin/v1/auth/logout
# 登出
Response: { success: boolean }
```

#### 2. User 模块

```yaml
# GET /api/admin/v1/users
# 查询用户列表（带分页、筛选）
Query:
  keyword: string         # 搜索：昵称、手机号、ID
  isTestAccount: boolean  # 是否测试账号
  accountStatus: string   # active / suspended / deleted
  storylineId: string     # 参与的故事线
  createdAtStart: date
  createdAtEnd: date
  pageNum: number
  pageSize: number

Response:
  list:
    - userId: string
      openId: string
      unionId: string
      nickname: string
      avatarUrl: string
      phone: string  # 脱敏
      isTestAccount: boolean
      accountStatus: string
      level: number
      totalStamps: number
      currentStorylineId: string
      currentStorylineName: string
      createdAt: datetime
      lastLoginAt: datetime
  pagination: {...}

# GET /api/admin/v1/users/{userId}
# 查看用户详情
Response:
  # 基本信息（同上）
  basicInfo: {...}
  
  # 游戏进度
  progress:
    level: number
    currentExp: number
    nextLevelExp: number
    totalStamps: number
    totalBadges: number
    unlockedStorylines: number
    completedStorylines: number
  
  # 当前进行中的故事线
  activeStorylines:
    - storylineId: string
      name: string
      currentPoiId: string
      currentPoiName: string
      completedPoiCount: number
      totalPoiCount: number
      progressPercent: number
      startedAt: datetime
  
  # 最近打卡记录
  recentCheckIns:
    - checkInId: string
      poiName: string
      checkInType: string
      rewardGranted: boolean
      createdAt: datetime

# POST /api/admin/v1/users/{userId}/test-flag
# 标记/取消测试账号
Request:
  isTestAccount: boolean
  reason: string          # 操作原因

Response:
  userId: string
  isTestAccount: boolean
  updatedAt: datetime

# GET /api/admin/v1/users/{userId}/progress
# 查看用户详细进度（与App端字段对齐）
Response:
  userId: string
  progress: 同上
  storylines:
    - storylineId: string
      status: string      # active / completed / locked
      progressDetail:
        completedPoiIds: string[]
        currentPoiId: string
        unlockedAt: datetime
        completedAt: datetime
  stamps:
    - stampId: string
      name: string
      iconUrl: string
      acquiredAt: datetime
      sourceType: string
```

#### 3. POI 模块

```yaml
# GET /api/admin/v1/pois
# POI列表查询
Query:
  keyword: string
  regionCode: string
  status: string
  poiType: string
  tags: string[]
  storylineId: string    # 属于某故事线的POI
  createdAtStart: date
  pageNum: number
  pageSize: number

Response:
  list:
    - poiId: string
      name: string
      subtitle: string
      regionCode: string
      regionName: string
      poiType: string
      latitude: number
      longitude: number
      gcj02Latitude: number
      gcj02Longitude: number
      coverImageUrl: string
      tags: string[]
      status: string
      checkInCount: number
      createdAt: datetime
  pagination: {...}

# GET /api/admin/v1/pois/{poiId}
# POI详情
Response:
  poiId: string
  name: string
  subtitle: string
  description: string
  regionCode: string
  regionName: string
  
  # 地理位置
  latitude: number              # WGS84
  longitude: number
  gcj02Latitude: number         # 国测局坐标
  gcj02Longitude: number
  address: string
  
  # 地理围栏（新增核心字段）
  geofenceRadius: number        # 电子围栏半径（米），默认50
  checkInMethod: string         # gps_only / bluetooth_beacon / qr_code
  beaconInfo: object            # 蓝牙信标信息
    uuid: string
    major: number
    minor: number
  
  # 媒体
  coverImageUrl: string
  imageUrls: string[]
  audioGuideUrl: string
  videoUrl: string
  arContentUrl: string
  
  # 业务属性
  poiType: string              # landmark / shop / restaurant / museum / story_point / event_venue
  tags: string[]
  difficulty: string            # easy / medium / hard
  openTime: string
  suggestedVisitMinutes: number
  
  # 状态
  status: string               # draft / pending / published / offline / deleted
  
  # 统计
  checkInCount: number
  favoriteCount: number
  
  # 关联故事线
  linkedStorylines:
    - storylineId: string
      name: string
      sequence: number   # 在该故事线中的顺序
  
  # 元数据
  createdBy: string
  createdAt: datetime
  updatedBy: string
  updatedAt: datetime

# POST /api/admin/v1/pois
# 创建POI
Request: 同 Response 去掉 poiId 和元数据字段
Response:
  poiId: string
  createdAt: datetime

# PUT /api/admin/v1/pois/{poiId}
# 更新POI
Request: 同 Response 去掉 poiId 和元数据字段
Response:
  poiId: string
  updatedAt: datetime

# PATCH /api/admin/v1/pois/{poiId}/status
# 更新POI状态
Request:
  status: string    # draft / pending / published / offline / deleted
  reason: string    # 状态变更原因

# DELETE /api/admin/v1/pois/{poiId}
# 删除POI（软删除）
Response:
  deleted: boolean
  deletedAt: datetime
```

#### 4. Storyline 模块

```yaml
# GET /api/admin/v1/storylines
Query:
  keyword: string
  status: string
  category: string
  difficulty: string
  createdAtStart: date
  pageNum: number
  pageSize: number

Response:
  list:
    - storylineId: string
      name: string
      description: string
      coverImageUrl: string
      category: string
      difficulty: string
      status: string
      poiCount: number
      participationCount: number
      completionCount: number
      createdAt: datetime
  pagination: {...}

# GET /api/admin/v1/storylines/{storylineId}
Response:
  storylineId: string
  name: string
  description: string
  coverImageUrl: string
  bannerImageUrl: string
  
  # POI序列
  poiSequence:
    - poiId: string
      poiName: string
      sequence: number
      unlockCondition:
        type: string    # none / complete_prev / wait_hours / specific_time
        params: object
      rewardStampId: string
      rewardStampName: string
  
  # 完成奖励
  completionReward:
    stampId: string
    badgeId: string
    couponId: string
    points: number
  
  difficulty: string
  estimatedDurationMinutes: number
  tags: string[]
  category: string
  
  status: string
  
  publishAt: datetime
  startAt: datetime
  endAt: datetime
  
  participationCount: number
  completionCount: number
  averageCompletionTime: number
  
  createdBy: string
  createdAt: datetime
  updatedBy: string
  updatedAt: datetime

# POST /api/admin/v1/storylines
# PUT /api/admin/v1/storylines/{storylineId}
# PATCH /api/admin/v1/storylines/{storylineId}/status
# DELETE /api/admin/v1/storylines/{storylineId}
```

#### 5. TestConsole 模块（测试控制台）

```yaml
# POST /api/admin/v1/test-console/users/{userId}/location-mock
# 模拟定位
Request:
  targetPoiId: string     # 目标POI
  latitude: number        # 模拟的纬度
  longitude: number       # 模拟的经度
  accuracy: number        # 定位精度（米）
  durationMinutes: number # 模拟持续时长（0表示永久）
  reason: string          # 操作原因

Response:
  mockId: string
  userId: string
  targetPoiId: string
  poiName: string
  status: string          # active / expired / cancelled
  createdAt: datetime
  expiresAt: datetime

# POST /api/admin/v1/test-console/users/{userId}/location-mock/cancel
# 取消模拟定位
Response:
  cancelled: boolean
  cancelledAt: datetime

# POST /api/admin/v1/test-console/users/{userId}/stamps/grant
# 发放印章
Request:
  stampRewardId: string   # 印章奖励定义ID
  storylineId: string     # 关联故事线（可选）
  poiId: string           # 关联POI（可选）
  reason: string
  expireAt: datetime      # 过期时间（可选）

Response:
  userStampId: string
  stampName: string
  iconUrl: string
  acquiredAt: datetime
  expiresAt: datetime

# POST /api/admin/v1/test-console/users/{userId}/stamps/revoke
# 撤销印章
Request:
  userStampId: string
  reason: string

# POST /api/admin/v1/test-console/users/{userId}/stamps/clear
# 清空所有印章（危险操作）
Request:
  reason: string
  confirmCode: string     # 二次确认码

# POST /api/admin/v1/test-console/users/{userId}/progress/reset
# 重置进度
Request:
  storylineId: string     # 指定故事线，不传则全部重置
  resetType: enum
    - full               # 完全重置（所有进度清零）
    - to_poi             # 重置到指定POI
    - keep_rewards       # 保留已获得奖励，仅重置进度
  targetPoiId: string     # resetType=to_poi时使用
  reason: string

# POST /api/admin/v1/test-console/users/{userId}/level/adjust
# 调整等级
Request:
  targetLevel: number
  targetExp: number
  reason: string

Response:
  userId: string
  oldLevel: number
  newLevel: number
  oldExp: number
  newExp: number
  adjustedAt: datetime

# POST /api/admin/v1/test-console/check-in/mock
# 直接模拟一次打卡（不经过真实定位）
Request:
  userId: string
  poiId: string
  checkInType: enum     # 默认mock
  rewardGranted: boolean  # 是否发放奖励
  reason: string
```

#### 6. Campaign、Reward、AuditLog 模块

```yaml
# Campaign 模块
GET    /api/admin/v1/campaigns
GET    /api/admin/v1/campaigns/{campaignId}
POST   /api/admin/v1/campaigns
PUT    /api/admin/v1/campaigns/{campaignId}
PATCH  /api/admin/v1/campaigns/{campaignId}/status
DELETE /api/admin/v1/campaigns/{campaignId}

# Reward 模块
GET    /api/admin/v1/rewards
GET    /api/admin/v1/rewards/{rewardId}
POST   /api/admin/v1/rewards
PUT    /api/admin/v1/rewards/{rewardId}
PATCH  /api/admin/v1/rewards/{rewardId}/status
DELETE /api/admin/v1/rewards/{rewardId}

# AuditLog 模块
GET /api/admin/v1/audit-logs
Query:
  operatorId: string
  operationType: string   # CREATE / UPDATE / DELETE / LOGIN / LOGOUT
  resourceType: string    # USER / POI / STORYLINE / REWARD / TEST_CONSOLE
  resourceId: string
  startTime: datetime
  endTime: datetime
  pageNum: number
  pageSize: number

Response:
  list:
    - logId: string
      operatorId: string
      operatorName: string
      operationType: string
      resourceType: string
      resourceId: string
      resourceName: string
      operationDetail: object    # 操作详情（前后对比）
      ipAddress: string
      userAgent: string
      createdAt: datetime
  pagination: {...}

GET /api/admin/v1/audit-logs/{logId}
# 查看单条审计日志详情
```

---

### App 接口详情（小程序客户端）

#### 1. Auth 模块

```yaml
# POST /api/app/v1/auth/login
# 小程序登录（code2session）
Request:
  code: string            # wx.login 获取的 code
  encryptedData: string   # getUserProfile 获取（可选）
  iv: string
  signature: string
  rawData: string

Response:
  token: string           # JWT Token
  refreshToken: string
  expiresIn: number
  isNewUser: boolean      # 是否新用户
  user:
    userId: string
    nickname: string
    avatarUrl: string
    isTestAccount: boolean
    level: number

# POST /api/app/v1/auth/refresh
# 刷新Token
Request:
  refreshToken: string

Response: 同上

# POST /api/app/v1/auth/logout
# 登出
Response: { success: boolean }
```

#### 2. User 模块

```yaml
# GET /api/app/v1/users/me
# 获取当前用户信息
Response:
  userId: string
  nickname: string
  avatarUrl: string
  phone: string  # 脱敏
  isTestAccount: boolean
  level: number
  currentExp: number
  nextLevelExp: number
  totalStamps: number
  totalBadges: number
  unlockedStorylines: number
  completedStorylines: number

# GET /api/app/v1/users/me/progress
# 获取用户游戏进度
Response:
  level: number
  currentExp: number
  nextLevelExp: number
  totalStamps: number
  stamps:                 # 最近获得的印章
    - stampId: string
      name: string
      iconUrl: string
      acquiredAt: datetime
  totalBadges: number
  badges:
    - badgeId: string
      name: string
      iconUrl: string
      acquiredAt: datetime
  activeStorylines:
    - storylineId: string
      name: string
      coverImageUrl: string
      currentPoiId: string
      currentPoiName: string
      completedPoiCount: number
      totalPoiCount: number
      progressPercent: number
      startedAt: datetime

# PUT /api/app/v1/users/me
# 更新用户信息
Request:
  nickname: string
  avatarUrl: string

# GET /api/app/v1/users/me/rewards
# 获取我的背包
Response:
  stamps: [...]
  badges: [...]
  coupons: [...]
  gifts: [...]
```

#### 3. CheckIn 模块（核心）

```yaml
# POST /api/app/v1/check-in/prepare
# 打卡前准备（获取POI打卡配置）
Request:
  poiId: string

Response:
  poiId: string
  name: string
  poiType: string
  
  # 打卡配置
  checkInConfig:
    checkInMethod: string       # gps_only / bluetooth_beacon / qr_code
    geofenceRadius: number      # 打卡半径（米）
    
    # GPS打卡配置
    gpsConfig:
      requiredAccuracy: number  # 要求的定位精度（米）
      maxDistance: number        # 最大允许距离（米）
    
    # 蓝牙打卡配置
    beaconConfig:
      uuid: string
      major: number
      minor: number
      requiredRssi: number      # 要求的信号强度
    
    # 扫码打卡配置
    qrCodeConfig:
      qrCodeData: string        # 二维码内容（如果是固定码）
  
  # 用户在该POI的打卡状态
  userCheckInStatus:
    canCheckIn: boolean
    alreadyCheckedIn: boolean
    lastCheckInAt: datetime
    isCurrentTarget: boolean    # 是否是当前故事线的目标POI
    unlockedStorylineId: string
  
  # 奖励预览
  rewardPreview:
    stampId: string
    stampName: string
    stampIconUrl: string
    badgeId: string
    badgeName: string
    points: number

# POST /api/app/v1/check-in
# 执行打卡（GPS方式）
Request:
  poiId: string
  checkInType: string       # gps
  location:
    latitude: number        # WGS84
    longitude: number
    accuracy: number        # 定位精度（米）
    altitude: number
    verticalAccuracy: number
  deviceInfo:               # 设备信息
    brand: string
    model: string
    system: string
    platform: string        # ios / android / devtools
    sdkVersion: string      # 微信基础库版本

Response:
  checkInId: string
  success: boolean
  
  # 打卡结果
  checkInResult:
    poiId: string
    poiName: string
    checkInType: string
    distance: number        # 与POI的实际距离（米）
    isWithinGeofence: boolean
    
  # 获得奖励
  rewardsGranted:
    stamps:
      - stampId: string
        name: string
        iconUrl: string
        acquiredAt: datetime
    badges:
      - badgeId: string
        name: string
        iconUrl: string
        acquiredAt: datetime
    points: number
    
  # 进度更新
  progressUpdate:
    level: number
    currentExp: number
    nextLevelExp: number
    totalStamps: number
    storylineProgress:
      storylineId: string
      completedPoiId: string
      nextPoiId: string
      isStorylineCompleted: boolean

# POST /api/app/v1/check-in/beacon
# 蓝牙信标打卡
Request:
  poiId: string
  checkInType: string       # beacon
  beaconData:
    uuid: string
    major: number
    minor: number
    rssi: number          # 信号强度
    proximity: string     # immediate / near / far
  deviceInfo: {...}

Response: 同上

# POST /api/app/v1/check-in/qr
# 扫码打卡
Request:
  poiId: string
  checkInType: string       # qr_code
  qrData: string            # 扫码结果
  deviceInfo: {...}

Response: 同上

# GET /api/app/v1/check-in/history
# 打卡历史记录
Query:
  poiId: string
  storylineId: string
  pageNum: number
  pageSize: number

Response:
  list:
    - checkInId: string
      poiId: string
      poiName: string
      poiCoverImageUrl: string
      storylineId: string
      storylineName: string
      checkInType: string
      rewardsGranted: {...}
      createdAt: datetime
  pagination: {...}
```

#### 4. Storyline 模块（App端）

```yaml
# GET /api/app/v1/storylines
# 获取故事线列表（带解锁状态）
Query:
  status: string          # 筛选状态：active / completed / locked
  category: string
  pageNum: number
  pageSize: number

Response:
  list:
    - storylineId: string
      name: string
      description: string
      coverImageUrl: string
      bannerImageUrl: string
      category: string
      difficulty: string
      estimatedDurationMinutes: number
      poiCount: number
      
      # 用户解锁状态（核心）
      userStatus:
        status: string            # locked / unlocked / active / completed
        unlockedAt: datetime
        startedAt: datetime
        completedAt: datetime
        progressPercent: number
        currentPoiId: string
        currentPoiName: string
        completedPoiCount: number
      
      # 奖励预览
      completionReward:
        stampId: string
        stampIconUrl: string
        badgeId: string
        badgeIconUrl: string
        points: number
      
      # 时间
      startAt: datetime
      endAt: datetime
  pagination: {...}

# GET /api/app/v1/storylines/{storylineId}
# 故事线详情（带POI解锁状态）
Response:
  storylineId: string
  name: string
  description: string
  coverImageUrl: string
  bannerImageUrl: string
  category: string
  difficulty: string
  estimatedDurationMinutes: number
  
  # 用户状态
  userStatus:
    status: string
    unlockedAt: datetime
    startedAt: datetime
    completedAt: datetime
    progressPercent: number
  
  # POI序列（带解锁状态）
  poiSequence:
    - poiId: string
      name: string
      subtitle: string
      coverImageUrl: string
      poiType: string
      gcj02Latitude: number
      gcj02Longitude: number
      
      # 序列信息
      sequence: number
      unlockCondition:
        type: string          # none / complete_prev / wait_hours / specific_time
        description: string   # 解锁条件描述
        params: object
      
      # 用户解锁状态
      userStatus:
        status: string        # locked / unlocked / completed
        unlockedAt: datetime
        completedAt: datetime
        checkInId: string     # 完成打卡的ID
      
      # 奖励
      rewardStamp:
        stampId: string
        name: string
        iconUrl: string
      
      # 统计
      checkInCount: number
      averageCompletionTime: number
  
  # 完成奖励
  completionReward:
    stampId: string
    stampName: string
    stampIconUrl: string
    badgeId: string
    badgeName: string
    badgeIconUrl: string
    points: number
  
  # 时间
  startAt: datetime
  endAt: datetime
  
  # 统计
  participationCount: number
  completionCount: number
  averageCompletionTime: number
  completionRate: number    # 完成率

# GET /api/app/v1/storylines/{storylineId}/progress
# 获取我在这个故事线的详细进度
Response:
  storylineId: string
  userId: string
  status: string            # locked / unlocked / active / completed
  startedAt: datetime
  completedAt: datetime
  progressPercent: number
  
  # POI进度明细
  poiProgress:
    - poiId: string
      sequence: number
      status: string        # locked / unlocked / completed
      unlockedAt: datetime
      completedAt: datetime
      checkInId: string
      rewardAcquired: boolean
  
  # 已获得奖励
  rewardsAcquired:
    stamps: [...]
    badges: [...]
    points: number
  
  # 下一个目标
  nextTarget:
    poiId: string
    poiName: string
    unlockCondition:
      type: string
      description: string
      remainingMinutes: number  # 如果是wait_hours类型

# POST /api/app/v1/storylines/{storylineId}/start
# 开始这个故事线（解锁第一个POI）
Response:
  storylineId: string
  startedAt: datetime
  firstPoi:
    poiId: string
    name: string
    gcj02Latitude: number
    gcj02Longitude: number
```

#### 5. Reward 模块（App端 - 背包）

```yaml
# GET /api/app/v1/rewards/my
# 获取我的背包
Query:
  type: string          # stamp / badge / coupon / gift / all
  status: string        # active / used / expired
  pageNum: number
  pageSize: number

Response:
  stamps:
    - userStampId: string
      stampId: string
      name: string
      iconUrl: string
      rarity: string      # common / rare / epic / legendary
      description: string
      sourceType: string  # check_in / storyline_complete / manual_grant / campaign
      sourceName: string  # 来源描述，如"完成大三巴故事线"
      acquiredAt: datetime
      expiresAt: datetime
      isNew: boolean      # 是否新获得（24小时内）
  
  badges:
    - userBadgeId: string
      badgeId: string
      name: string
      iconUrl: string
      description: string
      rarity: string
      acquiredAt: datetime
  
  coupons:
    - userCouponId: string
      couponId: string
      name: string
      description: string
      discountType: string
      discountValue: number
      minOrderAmount: number
      validStart: datetime
      validEnd: datetime
      status: string      # active / used / expired
      usedAt: datetime
      usedOrderId: string
  
  gifts:
    - userGiftId: string
      giftId: string
      name: string
      imageUrl: string
      status: string      # active / shipped / received
      shippingInfo: object
        trackingNumber: string
        courier: string
        shippedAt: datetime

# GET /api/app/v1/rewards/{userRewardId}
# 查看单个奖励详情

# POST /api/app/v1/rewards/coupons/{userCouponId}/use
# 使用优惠券
Request:
  orderId: string   # 关联的订单

Response:
  success: boolean
  usedAt: datetime
```

#### 6. Config 模块（全局配置）

```yaml
# GET /api/app/v1/config
# 获取小程序全局配置（启动时拉取）
Response:
  # 版本控制
  minClientVersion: string    # 最低客户端版本
  latestClientVersion: string # 最新客户端版本
  forceUpdate: boolean        # 是否强制更新
  updateMessage: string       # 更新提示文案
  
  # 地图配置
  map:
    defaultCenter:
      latitude: number        # GCJ02坐标
      longitude: number
    defaultZoom: number
    minZoom: number
    maxZoom: number
    poiClusterZoom: number    # POI聚合显示的zoom级别
  
  # 打卡配置
  checkIn:
    minAccuracy: number       # 最小定位精度（米）
    defaultGeofenceRadius: number  # 默认电子围栏半径（米）
    cooldownMinutes: number   # 同一POI打卡冷却时间（分钟）
    dailyCheckInLimit: number # 每日最大打卡次数
    enableMockCheckIn: boolean  # 是否允许模拟定位（测试模式）
  
  # 故事线配置
  storyline:
    maxConcurrentStorylines: number  # 最大并行故事线数
    enableAutoUnlock: boolean       # 是否自动解锁下一POI
    unlockWaitTimeMinutes: number   # 解锁等待时间（分钟）
  
  # 奖励配置
  reward:
    stampExpireDays: number       # 印章过期天数（0表示永久）
    couponDefaultValidDays: number  # 优惠券默认有效期（天）
    enablePoints: boolean           # 是否启用积分系统
  
  # 微信小程序特有配置
  wechat:
    enableGetPhoneNumber: boolean   # 是否引导获取手机号
    enableShareTimeline: boolean      # 是否启用分享到朋友圈
    enableOfficialAccount: boolean  # 是否关注公众号
    customerServiceUrl: string      # 客服链接
  
  # 功能开关
  featureFlags:
    enableAR: boolean
    enableAudioGuide: boolean
    enable3DMap: boolean
    enableSocialShare: boolean
    enableLeaderboard: boolean
  
  # 维护模式
  maintenance:
    enabled: boolean
    message: string
    estimatedRecoveryTime: datetime

# GET /api/app/v1/config/check-in-rules
# 获取打卡规则说明（用于展示给用户）
Response:
  rules:
    - icon: string
      title: string
      description: string
  tips:
    - "请在POI附近50米范围内打卡"
    - "保持GPS开启以获得更好体验"
    - "部分POI支持蓝牙信标打卡"
```

---

## 微信小程序专属字段汇总

### 用户认证相关
| 字段名 | 类型 | 说明 | 来源 |
|--------|------|------|------|
| `openId` | string | 微信小程序openid | 微信服务器 |
| `unionId` | string | 微信unionid（多个应用打通） | 微信服务器 |
| `avatarUrl` | string | 微信头像URL | getUserProfile |
| `nickname` | string | 微信昵称 | getUserProfile |
| `phone` | string | 手机号（需要用户授权） | getPhoneNumber |
| `sdkVersion` | string | 微信基础库版本 | wx.getSystemInfo |

### 地理位置相关
| 字段名 | 类型 | 说明 |
|--------|------|------|
| `latitude` / `longitude` | number | WGS84坐标（原始GPS） |
| `gcj02Latitude` / `gcj02Longitude` | number | 国测局坐标（小程序地图使用） |
| `accuracy` | number | 定位精度（米） |
| `geofenceRadius` | number | 电子围栏半径（米） |
| `beaconUuid` / `major` / `minor` | string/number | iBeacon蓝牙信标信息 |

### 设备信息相关
| 字段名 | 类型 | 说明 |
|--------|------|------|
| `brand` | string | 设备品牌 |
| `model` | string | 设备型号 |
| `system` | string | 操作系统版本 |
| `platform` | string | 平台：ios / android / devtools |
| `screenWidth` / `screenHeight` | number | 屏幕尺寸 |
| `windowWidth` / `windowHeight` | number | 可使用窗口尺寸 |

---

## 错误码补充

### 业务错误码（补充原有错误码）

```yaml
# 用户相关 U1000-U1999
U1001: 用户不存在
U1002: 非测试账号，不允许执行测试操作
U1003: 用户已存在
U1004: 用户账号已被封禁
U1005: 需要先完成实名认证
U1006: 手机号已绑定其他账号

# POI相关 P2000-P2999
P2001: POI不存在
P2002: 坐标不合法
P2003: POI不在服务区域内
P2004: POI当前不可打卡（未发布/已下线）
P2005: POI需要预约才能访问

# 故事线相关 S3000-S3999
S3001: 故事线不存在
S3002: 故事线当前不可参与（未开始/已结束）
S3003: 需要先解锁前置故事线
S3004: 故事线参与人数已满
S3005: 用户已达到并行故事线上限

# 打卡相关 C4000-C4999
C4001: 不在POI打卡范围内
C4002: 定位精度不足，请尝试移动位置
C4003: 距离POI太远，无法打卡
C4004: 该POI今日打卡次数已达上限
C4005: 需要完成前置POI才能解锁
C4006: 打卡冷却中，请稍后再试
C4007: 蓝牙信标未检测到
C4008: 二维码已失效或不存在
C4009: 该POI需要使用特定方式打卡

# 奖励相关 R5000-R5999
R5001: 奖励配置无效
R5002: 奖励已领完
R5003: 奖励已过期
R5004: 不符合领取条件
R5005: 已达到该奖励领取上限
R5006: 背包已满

# 测试控制台相关 T6000-T6999
T6001: 测试权限不足
T6002: 模拟定位失败
T6003: 目标用户不是测试账号
T6004: 印章发放失败
T6005: 进度重置失败
T6006: 不能对非测试账号执行此操作

# 系统相关 E7000-E7999
E7001: 系统维护中
E7002: 请求过于频繁，请稍后再试
E7003: 服务端错误，请稍后重试
E7004: 第三方服务异常
```

---

## 数据字典

### 枚举值定义

```yaml
# AccountStatus 账号状态
ACTIVE: 正常
SUSPENDED: 封禁
DELETED: 已注销

# PoiStatus POI状态
DRAFT: 草稿
PENDING: 待审核
PUBLISHED: 已发布
OFFLINE: 已下线
DELETED: 已删除

# StorylineStatus 故事线状态
DRAFT: 草稿
PENDING: 待审核
PUBLISHED: 已发布
OFFLINE: 已下线
ARCHIVED: 已归档

# CheckInMethod 打卡方式
GPS_ONLY: 仅GPS
BLUETOOTH_BEACON: 蓝牙信标
QR_CODE: 扫码
MANUAL_ADMIN: 管理员手动

# RewardType 奖励类型
STAMP: 印章
BADGE: 成就徽章
COUPON: 优惠券
GIFT: 实物礼品
POINTS: 积分
VIRTUAL_ITEM: 虚拟道具

# UserRole 用户角色
SUPER_ADMIN: 超级管理员
CONTENT_OPERATOR: 内容运营
ACTIVITY_OPERATOR: 活动运营
CUSTOMER_SERVICE: 客服
TESTER: 测试人员
ANALYST_READONLY: 数据只读
```

---

## 接口开发优先级

### P0 - 核心链路（MVP必须）

**Admin端：**
1. `POST /api/admin/v1/auth/login` - 登录
2. `GET /api/admin/v1/auth/me` - 获取当前用户
3. `GET /api/admin/v1/pois` - POI列表
4. `GET /api/admin/v1/pois/{poiId}` - POI详情
5. `POST /api/admin/v1/pois` - 创建POI
6. `PUT /api/admin/v1/pois/{poiId}` - 更新POI
7. `GET /api/admin/v1/storylines` - 故事线列表
8. `GET /api/admin/v1/storylines/{storylineId}` - 故事线详情
9. `POST /api/admin/v1/storylines` - 创建故事线
10. `GET /api/admin/v1/users` - 用户列表
11. `GET /api/admin/v1/users/{userId}` - 用户详情

**TestConsole（P0）：**
12. `POST /api/admin/v1/test-console/users/{userId}/location-mock` - 模拟定位
13. `POST /api/admin/v1/test-console/users/{userId}/stamps/grant` - 发放印章
14. `POST /api/admin/v1/test-console/users/{userId}/progress/reset` - 重置进度

**App端：**
15. `POST /api/app/v1/auth/login` - 小程序登录
16. `GET /api/app/v1/auth/me` - 获取当前用户
17. `GET /api/app/v1/users/me` - 我的信息
18. `GET /api/app/v1/users/me/progress` - 我的进度
19. `GET /api/app/v1/storylines` - 故事线列表（带解锁状态）
20. `GET /api/app/v1/storylines/{storylineId}` - 故事线详情
21. `POST /api/app/v1/check-in/prepare` - 打卡准备
22. `POST /api/app/v1/check-in` - 执行打卡
23. `GET /api/app/v1/config` - 全局配置

### P1 - 重要功能（MVP建议）

**Admin端：**
- POI/Storyline 状态切换、删除
- Reward 模块完整CRUD
- Campaign 模块完整CRUD
- AuditLog 审计日志
- TestConsole 其他操作（清空印章、调整等级等）

**App端：**
- 打卡历史记录
- 背包完整展示
- 扫码打卡、蓝牙打卡
- 故事线开始接口

### P2 - 增强功能（后续迭代）

- 用户数据分析报表
- 排行榜/排行榜
- 社交分享增强
- AR功能
- 多语言支持

---

## 总结

本V1.1版本API文档相对于V1.0的升级点：

1. **接口分层**：明确区分 `/api/admin/v1/*`、`/api/app/v1/*` 两层
2. **微信小程序字段完整对齐**：补充 `unionId`、`avatarUrl`、`gcj02Latitude` 等字段
3. **核心业务实体补充**：新增 UserProgress、CheckInRecord、Stamp 等实体
4. **打卡核心链路完善**：补充 prepare、beacon、qr 等多种打卡方式
5. **错误码完整覆盖**：新增业务错误码 100+ 个
6. **开发优先级明确**：P0/P1/P2 分层，指导MVP开发

下一步行动建议：
1. 瑟希斯：基于本文档更新Swagger YAML，补充Controller层代码
2. 墨涅塔：基于App端接口设计，完成小程序前端页面和Mock对接
3. 玖妖丸：Review字段设计，确认是否符合产品需求
