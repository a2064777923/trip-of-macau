# 澳小遊后台管理系统 - 前后端接口对接文档

**版本**: V1.0  
**日期**: 2026年4月7日  
**作者**: 墨涅塔 (frontend-dev)  
**协议**: HTTP RESTful API + JSON  
**编码**: UTF-8

---

## 目录

1. [通用规范](#一通用规范)
2. [认证相关接口](#二认证相关接口)
3. [用户管理接口](#三用户管理接口)
4. [测试账号工具接口](#四测试账号工具接口) ⭐ 重点
5. [POI管理接口](#五poi管理接口)
6. [故事线管理接口](#六故事线管理接口)
7. [数据统计接口](#七数据统计接口)
8. [系统管理接口](#八系统管理接口)

---

## 一、通用规范

### 1.1 通信协议

| 项目 | 规范 |
|------|------|
| 协议 | HTTPS |
| 请求方式 | GET / POST / PUT / DELETE / PATCH |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| Content-Type | `application/json` |

### 1.2 接口地址规范

```
https://api.admin.tripofmacau.com/api/v1/{模块}/{资源}/{动作}
```

示例:
- `GET /api/v1/users` - 获取用户列表
- `GET /api/v1/users/123` - 获取用户详情
- `POST /api/v1/users` - 创建用户
- `PUT /api/v1/users/123` - 更新用户
- `DELETE /api/v1/users/123` - 删除用户

### 1.3 统一响应格式

**成功响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1704067200000,
  "traceId": "a1b2c3d4e5f6g7h8"
}
```

**失败响应:**
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "errors": [
      { "field": "username", "message": "用户名不能为空" },
      { "field": "password", "message": "密码长度不能少于6位" }
    ]
  },
  "timestamp": 1704067200000,
  "traceId": "a1b2c3d4e5f6g7h8"
}
```

### 1.4 错误码定义

| 错误码 | 描述 | 说明 |
|--------|------|------|
| 200 | 成功 | 请求处理成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | 未认证 | Token无效或过期 |
| 403 | 无权限 | 没有操作权限 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 405 | 请求方法不允许 | HTTP方法不支持 |
| 409 | 资源冲突 | 数据重复或冲突 |
| 429 | 请求过于频繁 | 触发限流 |
| 500 | 服务器内部错误 | 系统异常 |
| 503 | 服务不可用 | 服务暂时不可用 |

### 1.5 分页规范

**请求参数:**
```json
{
  "pageNum": 1,        // 当前页码，从1开始
  "pageSize": 20,      // 每页条数，默认20，最大100
  "sortField": "createTime",  // 排序字段
  "sortOrder": "descend"      // 排序方式: ascend/descend
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [ ... ],         // 数据列表
    "total": 1234,           // 总条数
    "pages": 62,             // 总页数
    "pageNum": 1,            // 当前页码
    "pageSize": 20,          // 每页条数
    "hasNextPage": true,     // 是否有下一页
    "hasPreviousPage": false // 是否有上一页
  }
}
```

### 1.6 认证方式

使用 **JWT (JSON Web Token)** 进行身份认证

**Token 结构:**
```
Authorization: Bearer <access_token>
```

**Token 有效期:**
- Access Token: 2小时
- Refresh Token: 7天

**Token 刷新:**
当 Access Token 过期时，使用 Refresh Token 换取新的 Token 对。

---

## 二、认证相关接口

### 2.1 用户登录

**接口信息:**
- **URL**: `POST /api/v1/auth/login`
- **权限**: 公开

**请求参数:**
```json
{
  "username": "admin",           // 登录账号，必填
  "password": "123456",          // 密码，必填
  "captcha": "a3b5",             // 验证码，必填
  "captchaKey": "uuid-string"    // 验证码标识，必填
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": "https://...",
      "roles": ["super_admin"],
      "permissions": ["*"]
    }
  }
}
```

**错误响应:**
```json
// 400 - 参数错误
{
  "code": 400,
  "message": "请求参数错误",
  "data": {
    "errors": [
      { "field": "username", "message": "用户名不能为空" }
    ]
  }
}

// 401 - 认证失败
{
  "code": 401,
  "message": "用户名或密码错误"
}

// 429 - 登录过于频繁
{
  "code": 429,
  "message": "登录失败次数过多，请30分钟后再试"
}
```

---

### 2.2 用户登出

**接口信息:**
- **URL**: `POST /api/v1/auth/logout`
- **权限**: 需要登录

**请求参数:** 无

**请求头:**
```
Authorization: Bearer <access_token>
```

**响应数据:**
```json
{
  "code": 200,
  "message": "登出成功"
}
```

---

### 2.3 刷新 Token

**接口信息:**
- **URL**: `POST /api/v1/auth/refresh`
- **权限**: 需要有效的 Refresh Token

**请求参数:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",  // 新的 Access Token
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...", // 新的 Refresh Token
    "expiresIn": 7200
  }
}
```

---

### 2.4 获取当前用户信息

**接口信息:**
- **URL**: `GET /api/v1/auth/current`
- **权限**: 需要登录

**请求参数:** 无

**请求头:**
```
Authorization: Bearer <access_token>
```

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "email": "admin@tripofmacau.com",
    "phone": "+853-8888-8888",
    "avatar": "https://cdn.tripofmacau.com/avatars/admin.png",
    "roles": [
      {
        "id": 1,
        "roleCode": "super_admin",
        "roleName": "超级管理员"
      }
    ],
    "permissions": ["*"],
    "lastLoginTime": "2026-04-07T08:30:00+08:00",
    "lastLoginIp": "192.168.1.100",
    "createTime": "2026-01-01T00:00:00+08:00"
  }
}
```

---

## 三、用户管理接口

### 3.1 获取用户列表

**接口信息:**
- **URL**: `GET /api/v1/users`
- **权限**: `user:list`

**请求参数 (Query):**
```
?pageNum=1&pageSize=20&keyword=小明&startDate=2026-01-01&endDate=2026-04-07&sortField=createTime&sortOrder=descend
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键词搜索(昵称/ID) |
| startDate | string | 否 | 注册开始日期 |
| endDate | string | 否 | 注册结束日期 |
| isTestAccount | boolean | 否 | 是否测试账号 |
| sortField | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序方式 |

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 10001,
        "openId": "ou_xxxxxxxxxxxxxxxxx",
        "nickname": "小明",
        "avatar": "https://thirdwx.qlogo.cn/...",
        "phone": "+86-138****8888",
        "country": "中国",
        "province": "广东",
        "city": "珠海",
        "gender": 1,
        "stampCount": 5,
        "totalStamps": 12,
        "level": 3,
        "levelName": "澳门探索者",
        "experience": 80,
        "isTestAccount": false,
        "lastLoginTime": "2026-04-07T08:30:00+08:00",
        "lastLoginIp": "119.123.123.123",
        "createTime": "2026-04-06T10:20:00+08:00",
        "updateTime": "2026-04-07T08:30:00+08:00"
      }
    ],
    "total": 12345,
    "pages": 618,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 3.2 获取用户详情

**接口信息:**
- **URL**: `GET /api/v1/users/{id}`
- **权限**: `user:detail`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10001,
    "openId": "ou_xxxxxxxxxxxxxxxxx",
    "nickname": "小明",
    "avatar": "https://thirdwx.qlogo.cn/...",
    "phone": "+86-13888888888",
    "email": "xiaoming@example.com",
    "country": "中国",
    "province": "广东",
    "city": "珠海",
    "gender": 1,
    "language": "zh_CN",
    "stampCount": 5,
    "totalStamps": 12,
    "collectedStamps": [
      { "id": 1, "name": "大三巴", "collectTime": "2026-04-06T14:30:00+08:00" },
      { "id": 2, "name": "威尼斯人", "collectTime": "2026-04-06T15:20:00+08:00" }
    ],
    "level": 3,
    "levelName": "澳门探索者",
    "experience": 80,
    "nextLevelExp": 200,
    "achievements": [
      { "id": 1, "name": "初次打卡", "icon": "🏅", "unlockTime": "2026-04-06T14:30:00+08:00" }
    ],
    "currentStoryLine": {
      "lineId": 1,
      "lineName": "大三巴传奇",
      "progress": 3,
      "totalChapters": 5
    },
    "isTestAccount": false,
    "testGroup": null,
    "lastLoginTime": "2026-04-07T08:30:00+08:00",
    "lastLoginIp": "119.123.123.123",
    "loginCount": 15,
    "createTime": "2026-04-06T10:20:00+08:00",
    "updateTime": "2026-04-07T08:30:00+08:00"
  }
}
```

---

### 3.3 标记测试账号

**接口信息:**
- **URL**: `POST /api/v1/users/{id}/test-mark`
- **权限**: `test:account:mark`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**请求体:**
```json
{
  "remark": "测试员小明",      // 备注说明
  "testGroup": "group_a"       // 测试分组，默认default
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "标记成功",
  "data": {
    "id": 10001,
    "isTestAccount": true,
    "testGroup": "group_a",
    "remark": "测试员小明",
    "markTime": "2026-04-07T10:30:00+08:00"
  }
}
```

---

### 3.4 取消测试账号标记

**接口信息:**
- **URL**: `DELETE /api/v1/users/{id}/test-mark`
- **权限**: `test:account:unmark`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 用户ID |

**响应数据:**
```json
{
  "code": 200,
  "message": "取消标记成功",
  "data": {
    "id": 10001,
    "isTestAccount": false
  }
}
```

---

## 四、测试账号工具接口 ⭐ 重点

### 4.1 获取测试账号列表

**接口信息:**
- **URL**: `GET /api/v1/test-accounts`
- **权限**: `test:account:list`

**请求参数 (Query):**
```
?pageNum=1&pageSize=20&keyword=test&testGroup=group_a&hasMockLocation=true
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20 |
| keyword | string | 否 | 关键词(昵称/备注) |
| testGroup | string | 否 | 测试分组 |
| hasMockLocation | boolean | 否 | 是否有模拟位置 |
| isMockEnabled | boolean | 否 | 是否启用模拟定位 |

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 10001,
        "userId": 10001,
        "openId": "ou_xxxxxxxxxxxxxxxxx",
        "nickname": "测试员01",
        "avatar": "https://thirdwx.qlogo.cn/...",
        "remark": "主要测试人员",
        "testGroup": "group_a",
        "mockLocation": {
          "latitude": 22.1973,
          "longitude": 113.5408,
          "address": "澳门半岛大三巴附近"
        },
        "isMockEnabled": true,
        "createTime": "2026-04-05T10:00:00+08:00",
        "updateTime": "2026-04-07T09:30:00+08:00",
        "lastOperationTime": "2026-04-07T09:30:00+08:00",
        "operationCount": 15
      }
    ],
    "total": 15,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 4.2 设置模拟定位 ⭐ 核心功能

**接口信息:**
- **URL**: `PUT /api/v1/test-accounts/{id}/mock-location`
- **权限**: `test:mock:location`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "latitude": 22.1973,           // 纬度，必填，范围22.1-22.2
  "longitude": 113.5408,         // 经度，必填，范围113.5-113.6
  "address": "澳门半岛大三巴牌坊", // 地址描述，可选
  "enable": true                 // 是否立即启用，默认true
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "模拟定位设置成功",
  "data": {
    "id": 10001,
    "mockLocation": {
      "latitude": 22.1973,
      "longitude": 113.5408,
      "address": "澳门半岛大三巴牌坊"
    },
    "isMockEnabled": true,
    "updateTime": "2026-04-07T10:30:00+08:00"
  }
}
```

**业务说明:**
- 设置模拟定位后，测试账号在小程序端获取的位置将返回设定的模拟坐标
- 需要配合小程序端的相关接口使用
- 操作会被记录到测试操作日志中

---

### 4.3 启用/禁用模拟定位

**启用模拟定位:**
- **URL**: `PUT /api/v1/test-accounts/{id}/mock-enable`
- **权限**: `test:mock:enable`

**禁用模拟定位:**
- **URL**: `PUT /api/v1/test-accounts/{id}/mock-disable`
- **权限**: `test:mock:disable`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**响应数据:**
```json
{
  "code": 200,
  "message": "模拟定位已启用/禁用",
  "data": {
    "id": 10001,
    "isMockEnabled": true/false,
    "updateTime": "2026-04-07T10:35:00+08:00"
  }
}
```

---

### 4.4 快速获得印章 ⭐ 核心功能

**接口信息:**
- **URL**: `POST /api/v1/test-accounts/{id}/stamps/grant`
- **权限**: `test:stamp:grant`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "stampId": 1,                    // 印章ID，必填
  "triggerLocation": {              // 触发位置，可选，默认为当前位置
    "latitude": 22.1973,
    "longitude": 113.5408
  },
  "triggerTime": "2026-04-07T10:30:00+08:00",  // 触发时间，可选，默认当前时间
  "note": "测试快速获得印章"        // 备注，可选
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "印章获得成功",
  "data": {
    "testAccountId": 10001,
    "stamp": {
      "id": 1,
      "name": "大三巴",
      "icon": "https://cdn.tripofmacau.com/stamps/dasaba.png",
      "poiId": 101,
      "poiName": "大三巴牌坊",
      "acquireTime": "2026-04-07T10:30:00+08:00",
      "triggerLocation": {
        "latitude": 22.1973,
        "longitude": 113.5408,
        "address": "澳门半岛大三巴牌坊"
      }
    },
    "currentProgress": {
      "stampCount": 6,
      "totalStamps": 12,
      "level": 3,
      "experience": 100,
      "newAchievements": [
        { "id": 2, "name": "印章收集者", "icon": "🏅" }
      ]
    }
  }
}
```

**业务说明:**
- 该接口模拟用户到达POI并触发获得印章的完整流程
- 会自动计算经验值、等级变化、成就解锁等
- 操作会被完整记录到测试操作日志中

---

### 4.5 批量获得印章

**接口信息:**
- **URL**: `POST /api/v1/test-accounts/{id}/stamps/grant-batch`
- **权限**: `test:stamp:grant-batch`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "stampIds": [1, 2, 3, 4, 5],    // 印章ID列表，必填
  "sameLocation": true,            // 是否使用同一位置，默认true
  "triggerLocation": {               // 当sameLocation为true时使用
    "latitude": 22.1973,
    "longitude": 113.5408
  },
  "note": "批量获得多个印章"         // 备注，可选
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "批量获得印章成功",
  "data": {
    "testAccountId": 10001,
    "grantResults": [
      { "stampId": 1, "stampName": "大三巴", "success": true, "message": "获得成功" },
      { "stampId": 2, "stampName": "威尼斯人", "success": true, "message": "获得成功" },
      { "stampId": 3, "stampName": "官也街", "success": true, "message": "获得成功" },
      { "stampId": 4, "stampName": "妈阁庙", "success": false, "message": "已拥有该印章" },
      { "stampId": 5, "stampName": "新葡京", "success": true, "message": "获得成功" }
    ],
    "summary": {
      "total": 5,
      "success": 4,
      "failed": 1,
      "alreadyHad": 1
    },
    "currentProgress": {
      "stampCount": 9,
      "totalStamps": 12,
      "level": 4,
      "levelName": "澳门达人"
    }
  }
}
```

---

### 4.6 删除印章

**接口信息:**
- **URL**: `DELETE /api/v1/test-accounts/{id}/stamps/{stampId}`
- **权限**: `test:stamp:delete`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |
| stampId | long | 是 | 印章ID |

**请求体:**
```json
{
  "reason": "测试删除印章功能"   // 删除原因，可选
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "印章删除成功",
  "data": {
    "testAccountId": 10001,
    "deletedStamp": {
      "id": 1,
      "name": "大三巴"
    },
    "currentProgress": {
      "stampCount": 8,
      "totalStamps": 12
    }
  }
}
```

---

### 4.7 清空所有印章

**接口信息:**
- **URL**: `DELETE /api/v1/test-accounts/{id}/stamps/clear`
- **权限**: `test:stamp:clear`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "confirm": true,              // 确认清空，必须传true
  "reason": "重置测试数据"         // 清空原因
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "印章清空成功",
  "data": {
    "testAccountId": 10001,
    "clearedCount": 8,
    "currentProgress": {
      "stampCount": 0,
      "totalStamps": 12
    }
  }
}
```

---

### 4.8 调整等级

**接口信息:**
- **URL**: `PUT /api/v1/test-accounts/{id}/level`
- **权限**: `test:level:adjust`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "level": 5,                     // 目标等级，必填
  "experience": 250,              // 目标经验值，可选，默认该等级初始值
  "keepProgress": false           // 是否保留进度(如保留已收集的印章)，默认false
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "等级调整成功",
  "data": {
    "testAccountId": 10001,
    "before": {
      "level": 3,
      "levelName": "澳门探索者",
      "experience": 80
    },
    "after": {
      "level": 5,
      "levelName": "澳门通",
      "experience": 250,
      "nextLevelExp": 500
    },
    "unlockedFeatures": [
      "高级徽章展示",
      "专属头衔"
    ]
  }
}
```

---

### 4.9 重置游戏进度

**接口信息:**
- **URL**: `POST /api/v1/test-accounts/{id}/reset`
- **权限**: `test:progress:reset`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "resetType": "all",             // 重置类型: all(全部)/stamps(仅印章)/level(仅等级)/story(仅故事线)
  "keepTestMark": true,           // 是否保留测试账号标记，默认true
  "reason": "重置测试环境"          // 重置原因
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "游戏进度重置成功",
  "data": {
    "testAccountId": 10001,
    "resetType": "all",
    "clearedData": {
      "stamps": 8,
      "experience": 250,
      "achievements": 5,
      "storyProgress": {
        "lineId": 1,
        "completedChapters": 3
      }
    },
    "currentState": {
      "stampCount": 0,
      "level": 1,
      "experience": 0,
      "levelName": "新手游客"
    },
    "testAccount": {
      "isTestAccount": true,        // 保留测试标记
      "testGroup": "group_a"
    }
  }
}
```

---

### 4.10 模拟触发测试

**接口信息:**
- **URL**: `POST /api/v1/test-accounts/{id}/simulate-trigger`
- **权限**: `test:simulate:trigger`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求体:**
```json
{
  "poiId": 101,                   // POI ID，必填
  "triggerLocation": {            // 触发位置，可选，默认为POI位置
    "latitude": 22.1973,
    "longitude": 113.5408
  },
  "triggerTime": "2026-04-07T10:30:00+08:00",  // 触发时间，可选，默认当前
  "checkDistance": true           // 是否校验距离，默认true
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "模拟触发成功",
  "data": {
    "testAccountId": 10001,
    "triggerResult": {
      "success": true,
      "poiId": 101,
      "poiName": "大三巴牌坊",
      "triggerType": "stamp_grant",
      "triggerData": {
        "stampId": 1,
        "stampName": "大三巴",
        "isNew": true,              // 是否首次获得
        "experienceGained": 20,    // 获得经验值
        "levelUp": true,             // 是否升级
        "newLevel": 4,
        "newLevelName": "澳门达人",
        "unlockedAchievements": [
          { "id": 2, "name": "大三巴探索者" }
        ]
      }
    },
    "distanceCheck": {
      "checked": true,
      "actualDistance": 12,          // 实际距离(米)
      "threshold": 30,               // 触发阈值(米)
      "passed": true
    },
    "currentProgress": {
      "stampCount": 9,
      "totalStamps": 12,
      "level": 4,
      "experience": 100,
      "nextLevelExp": 300
    }
  }
}
```

---

### 4.11 批量操作测试账号

**接口信息:**
- **URL**: `POST /api/v1/test-accounts/batch-operation`
- **权限**: `test:batch:operate`

**请求体:**
```json
{
  "accountIds": [10001, 10002, 10003],  // 测试账号ID列表，必填
  "operation": "grant_stamps",          // 操作类型，必填
  "operationData": {                     // 操作数据，根据operation不同而不同
    "stampIds": [1, 2, 3]
  },
  "reason": "批量获得印章"                 // 操作原因
}
```

**支持的批量操作类型:**

| operation | 说明 | operationData |
|-----------|------|---------------|
| `grant_stamps` | 批量获得印章 | `{ stampIds: [1,2,3] }` |
| `clear_stamps` | 清空印章 | `{ confirm: true }` |
| `set_level` | 设置等级 | `{ level: 5, experience: 250 }` |
| `reset_progress` | 重置进度 | `{ resetType: 'all' }` |
| `set_mock_location` | 设置模拟位置 | `{ latitude: 22.19, longitude: 113.54 }` |
| `enable_mock` | 启用模拟定位 | `{}` |
| `disable_mock` | 禁用模拟定位 | `{}` |

**响应数据:**
```json
{
  "code": 200,
  "message": "批量操作成功",
  "data": {
    "operation": "grant_stamps",
    "total": 3,
    "success": 3,
    "failed": 0,
    "results": [
      {
        "accountId": 10001,
        "success": true,
        "message": "操作成功",
        "data": { ... }
      },
      {
        "accountId": 10002,
        "success": true,
        "message": "操作成功",
        "data": { ... }
      },
      {
        "accountId": 10003,
        "success": true,
        "message": "操作成功",
        "data": { ... }
      }
    ]
  }
}
```

---

### 4.12 获取测试操作日志

**接口信息:**
- **URL**: `GET /api/v1/test-accounts/{id}/logs`
- **权限**: `test:log:view`

**路径参数:**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | long | 是 | 测试账号ID |

**请求参数 (Query):**
```
?pageNum=1&pageSize=20&startTime=2026-04-01&endTime=2026-04-07&operationType=grant_stamp
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页条数 |
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |
| operationType | string | 否 | 操作类型筛选 |

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1001,
        "testAccountId": 10001,
        "operationType": "grant_stamp",
        "operationTypeName": "获得印章",
        "operationDesc": "快速获得印章 '大三巴'",
        "adminId": 1,
        "adminName": "管理员小王",
        "operationData": {
          "stampId": 1,
          "stampName": "大三巴",
          "triggerLocation": { "latitude": 22.1973, "longitude": 113.5408 }
        },
        "beforeData": { "stampCount": 5, "experience": 60 },
        "afterData": { "stampCount": 6, "experience": 80, "levelUp": false },
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0...",
        "createTime": "2026-04-07T10:30:00+08:00"
      }
    ],
    "total": 50,
    "pages": 3,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

## 五、POI管理接口

### 5.1 获取POI列表

**接口信息:**
- **URL**: `GET /api/v1/pois`
- **权限**: `poi:list`

**请求参数 (Query):**
```
?pageNum=1&pageSize=20&keyword=大三巴&categoryId=1&status=1&sortField=createTime&sortOrder=descend
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页条数 |
| keyword | string | 否 | 关键词搜索 |
| categoryId | long | 否 | 分类ID |
| status | int | 否 | 状态: 0草稿 1已发布 2下线 |
| importance | string | 否 | 重要性: normal/important/very_important |
| storyLineId | long | 否 | 所属故事线ID |

**响应数据:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 101,
        "nameZh": "大三巴牌坊",
        "nameEn": "Ruins of St. Paul's",
        "nameZht": "大三巴牌坊",
        "categoryId": 1,
        "categoryName": "历史文化",
        "descriptionZh": "大三巴牌坊，正式名称为圣保禄大教堂遗址...",
        "latitude": 22.1973,
        "longitude": 113.5408,
        "addressZh": "澳门特别行政区风顺堂区",
        "importance": "very_important",
        "baseTriggerRadius": 30,
        "isIndoor": false,
        "stampType": "dasaba",
        "storyLineId": 1,
        "storyLineName": "大三巴传奇",
        "mediaUrls": [
          "https://cdn.tripofmacau.com/pois/101/1.jpg",
          "https://cdn.tripofmacau.com/pois/101/2.jpg"
        ],
        "openingHoursZh": "全天开放",
        "phone": "+853-12345678",
        "website": "https://www.macaotourism.gov.mo",
        "tags": ["世界遗产", "必打卡", "历史"],
        "status": 1,
        "createBy": 1,
        "createByName": "管理员",
        "createTime": "2026-01-01T00:00:00+08:00",
        "updateBy": 1,
        "updateTime": "2026-04-07T10:00:00+08:00"
      }
    ],
    "total": 128,
    "pages": 7,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

由于篇幅限制，以下接口提供简要说明。完整文档可参考技术方案。

## 六、故事线管理接口

| 接口 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 获取故事线列表 | GET | /api/v1/story-lines | story:list |
| 获取故事线详情 | GET | /api/v1/story-lines/{id} | story:detail |
| 创建故事线 | POST | /api/v1/story-lines | story:create |
| 更新故事线 | PUT | /api/v1/story-lines/{id} | story:update |
| 删除故事线 | DELETE | /api/v1/story-lines/{id} | story:delete |
| 获取章节列表 | GET | /api/v1/story-lines/{id}/chapters | story:chapter:list |
| 创建章节 | POST | /api/v1/story-lines/{id}/chapters | story:chapter:create |

## 七、数据统计接口

| 接口 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 获取核心指标 | GET | /api/v1/statistics/core | statistics:core |
| 获取用户趋势 | GET | /api/v1/statistics/user-trend | statistics:user-trend |
| 获取活跃用户 | GET | /api/v1/statistics/active-users | statistics:active-users |
| 获取留存率 | GET | /api/v1/statistics/retention | statistics:retention |
| 获取印章统计 | GET | /api/v1/statistics/stamps | statistics:stamps |
| 获取POI热度 | GET | /api/v1/statistics/poi-hot | statistics:poi-hot |
| 获取地理位置分布 | GET | /api/v1/statistics/geo-distribution | statistics:geo-distribution |

## 八、系统管理接口

| 接口 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 获取管理员列表 | GET | /api/v1/admins | admin:list |
| 创建管理员 | POST | /api/v1/admins | admin:create |
| 更新管理员 | PUT | /api/v1/admins/{id} | admin:update |
| 删除管理员 | DELETE | /api/v1/admins/{id} | admin:delete |
| 获取角色列表 | GET | /api/v1/roles | role:list |
| 创建角色 | POST | /api/v1/roles | role:create |
| 分配权限 | PUT | /api/v1/roles/{id}/permissions | role:assign-perm |
| 获取操作日志 | GET | /api/v1/logs/operation | log:operation |
| 获取登录日志 | GET | /api/v1/logs/login | log:login |

---

## 附录

### A. 接口调用示例

#### 前端 Axios 封装

```typescript
// services/request.ts
import axios from 'axios';
import { message } from 'antd';

const request = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message: msg, data } = response.data;
    
    if (code === 200) {
      return data;
    }
    
    // 处理业务错误
    message.error(msg || '操作失败');
    return Promise.reject(response.data);
  },
  (error) => {
    const { response } = error;
    
    if (response) {
      switch (response.status) {
        case 401:
          // Token过期，刷新Token或重新登录
          message.error('登录已过期，请重新登录');
          localStorage.clear();
          window.location.href = '/login';
          break;
        case 403:
          message.error('没有操作权限');
          break;
        case 404:
          message.error('请求的资源不存在');
          break;
        case 500:
          message.error('服务器内部错误');
          break;
        default:
          message.error(response.data?.message || '网络错误');
      }
    } else {
      message.error('网络连接失败');
    }
    
    return Promise.reject(error);
  }
);

export default request;
```

#### API 调用示例

```typescript
// services/api/testAccount.ts
import request from '../request';

export const testAccountApi = {
  // 获取测试账号列表
  getList: (params: any) => {
    return request.get('/test-accounts', { params });
  },
  
  // 设置模拟定位
  setMockLocation: (id: number, data: any) => {
    return request.put(`/test-accounts/${id}/mock-location`, data);
  },
  
  // 启用模拟定位
  enableMock: (id: number) => {
    return request.put(`/test-accounts/${id}/mock-enable`);
  },
  
  // 禁用模拟定位
  disableMock: (id: number) => {
    return request.put(`/test-accounts/${id}/mock-disable`);
  },
  
  // 快速获得印章
  grantStamp: (id: number, data: any) => {
    return request.post(`/test-accounts/${id}/stamps/grant`, data);
  },
  
  // 批量获得印章
  grantStampsBatch: (id: number, data: any) => {
    return request.post(`/test-accounts/${id}/stamps/grant-batch`, data);
  },
  
  // 删除印章
  deleteStamp: (id: number, stampId: number, reason?: string) => {
    return request.delete(`/test-accounts/${id}/stamps/${stampId}`, {
      data: { reason }
    });
  },
  
  // 清空印章
  clearStamps: (id: number, data: any) => {
    return request.delete(`/test-accounts/${id}/stamps/clear`, { data });
  },
  
  // 调整等级
  adjustLevel: (id: number, data: any) => {
    return request.put(`/test-accounts/${id}/level`, data);
  },
  
  // 重置进度
  resetProgress: (id: number, data: any) => {
    return request.post(`/test-accounts/${id}/reset`, data);
  },
  
  // 模拟触发
  simulateTrigger: (id: number, data: any) => {
    return request.post(`/test-accounts/${id}/simulate-trigger`, data);
  },
  
  // 批量操作
  batchOperation: (data: any) => {
    return request.post('/test-accounts/batch-operation', data);
  },
  
  // 获取操作日志
  getLogs: (id: number, params: any) => {
    return request.get(`/test-accounts/${id}/logs`, { params });
  }
};

export default testAccountApi;
```

#### 页面中使用示例

```typescript
// pages/TestAccount/index.tsx
import React, { useState } from 'react';
import { message, Modal } from 'antd';
import { testAccountApi } from '@/services/api';

const TestAccountPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  
  // 设置模拟定位
  const handleSetMockLocation = async (accountId: number, location: any) => {
    setLoading(true);
    try {
      const res = await testAccountApi.setMockLocation(accountId, {
        latitude: location.lat,
        longitude: location.lng,
        address: location.address,
        enable: true
      });
      message.success('模拟定位设置成功');
      return res;
    } catch (error) {
      message.error('设置失败');
      throw error;
    } finally {
      setLoading(false);
    }
  };
  
  // 快速获得印章
  const handleGrantStamp = async (accountId: number, stampId: number) => {
    setLoading(true);
    try {
      const res = await testAccountApi.grantStamp(accountId, {
        stampId,
        note: '快速获得印章'
      });
      
      const { currentProgress, triggerResult } = res;
      
      // 显示结果
      Modal.success({
        title: '获得印章成功',
        content: (
          <div>
            <p>印章: {triggerResult.triggerData.stampName}</p>
            <p>经验值: +{triggerResult.triggerData.experienceGained}</p>
            {triggerResult.triggerData.levelUp && (
              <p>🎉 升级到 Lv.{triggerResult.triggerData.newLevel}!</p>
            )}
            <p>当前进度: {currentProgress.stampCount}/{currentProgress.totalStamps}</p>
          </div>
        )
      });
      
      return res;
    } catch (error) {
      message.error('操作失败');
      throw error;
    } finally {
      setLoading(false);
    }
  };
  
  // 更多方法...
  
  return (
    <div>
      {/* 页面内容 */}
    </div>
  );
};

export default TestAccountPage;
```

---

## 附录 B: 权限码清单

| 权限码 | 名称 | 说明 |
|--------|------|------|
| `auth:login` | 登录 | 用户登录权限 |
| `user:list` | 用户列表 | 查看用户列表 |
| `user:detail` | 用户详情 | 查看用户详情 |
| `user:mark-test` | 标记测试账号 | 标记/取消测试账号 |
| `poi:list` | POI列表 | 查看POI列表 |
| `poi:detail` | POI详情 | 查看POI详情 |
| `poi:create` | 创建POI | 新增POI |
| `poi:update` | 更新POI | 编辑POI |
| `poi:delete` | 删除POI | 删除POI |
| `story:list` | 故事线列表 | 查看故事线列表 |
| `story:detail` | 故事线详情 | 查看故事线详情 |
| `story:create` | 创建故事线 | 新增故事线 |
| `story:update` | 更新故事线 | 编辑故事线 |
| `test:account:list` | 测试账号列表 | 查看测试账号列表 |
| `test:account:detail` | 测试账号详情 | 查看测试账号详情 |
| `test:mock:location` | 设置模拟位置 | 设置模拟定位坐标 |
| `test:mock:enable` | 启用模拟定位 | 启用模拟定位功能 |
| `test:mock:disable` | 禁用模拟定位 | 禁用模拟定位功能 |
| `test:stamp:grant` | 获得印章 | 快速获得指定印章 |
| `test:stamp:grant-batch` | 批量获得印章 | 批量获得多个印章 |
| `test:stamp:delete` | 删除印章 | 删除指定印章 |
| `test:stamp:clear` | 清空印章 | 清空所有印章 |
| `test:level:adjust` | 调整等级 | 调整用户等级 |
| `test:progress:reset` | 重置进度 | 重置游戏进度 |
| `test:simulate:trigger` | 模拟触发 | 模拟到达触发 |
| `test:batch:operate` | 批量操作 | 批量操作多个账号 |
| `test:log:view` | 查看操作日志 | 查看测试操作日志 |
| `statistics:core` | 核心指标 | 查看核心统计数据 |
| `statistics:user-trend` | 用户趋势 | 查看用户趋势 |
| `statistics:active-users` | 活跃用户 | 查看活跃用户统计 |
| `statistics:retention` | 留存率 | 查看留存率 |
| `admin:list` | 管理员列表 | 查看管理员列表 |
| `admin:create` | 创建管理员 | 新增管理员 |
| `admin:update` | 更新管理员 | 编辑管理员 |
| `admin:delete` | 删除管理员 | 删除管理员 |
| `role:list` | 角色列表 | 查看角色列表 |
| `role:create` | 创建角色 | 新增角色 |
| `role:assign-perm` | 分配权限 | 为角色分配权限 |
| `log:operation` | 操作日志 | 查看操作日志 |
| `log:login` | 登录日志 | 查看登录日志 |

---

## 附录 C: 测试工具前端组件设计

### 测试工具面板组件

```typescript
// components/TestToolPanel/index.tsx
import React from 'react';
import { Tabs } from 'antd';
import MockLocationPanel from './MockLocationPanel';
import StampManager from './StampManager';
import LevelControl from './LevelControl';
import ProgressReset from './ProgressReset';
import OperationLog from './OperationLog';

interface TestToolPanelProps {
  testAccountId: number;
  testAccountInfo: any;
  onOperationSuccess?: () => void;
}

const TestToolPanel: React.FC<TestToolPanelProps> = ({
  testAccountId,
  testAccountInfo,
  onOperationSuccess
}) => {
  return (
    <Tabs
      type="card"
      items={[
        {
          key: 'mock-location',
          label: '📍 模拟定位',
          children: (
            <MockLocationPanel
              testAccountId={testAccountId}
              mockLocation={testAccountInfo?.mockLocation}
              isMockEnabled={testAccountInfo?.isMockEnabled}
              onSuccess={onOperationSuccess}
            />
          )
        },
        {
          key: 'stamps',
          label: '🏆 印章管理',
          children: (
            <StampManager
              testAccountId={testAccountId}
              currentStamps={testAccountInfo?.stamps}
              onSuccess={onOperationSuccess}
            />
          )
        },
        {
          key: 'level',
          label: '📊 等级调整',
          children: (
            <LevelControl
              testAccountId={testAccountId}
              currentLevel={testAccountInfo?.level}
              currentExperience={testAccountInfo?.experience}
              onSuccess={onOperationSuccess}
            />
          )
        },
        {
          key: 'reset',
          label: '🔄 进度重置',
          children: (
            <ProgressReset
              testAccountId={testAccountId}
              onSuccess={onOperationSuccess}
            />
          )
        },
        {
          key: 'logs',
          label: '📝 操作日志',
          children: (
            <OperationLog
              testAccountId={testAccountId}
            />
          )
        }
      ]}
    />
  );
};

export default TestToolPanel;
```

---

**文档结束**

如有任何问题或需要进一步细化某个接口，请随时告知。
