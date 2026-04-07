# 澳小遊后台管理系统 Swagger API 设计 V1.0

<callout emoji="🏗️" background-color="light-blue">
本版在模板基础上补充了接口分层、权限建议、错误码规范、联调顺序和字段设计原则，用于指导后台管理系统前后端并行开发。
</callout>

## 1. 设计目标

后台管理系统不是展示页，而是运营与测试的控制中枢。接口设计必须满足四个目标：

1. **稳定**：接口契约尽量一次定型，减少联调阶段反复改字段
2. **安全**：测试能力与正式运营能力隔离，所有敏感操作可审计
3. **可扩展**：后续商户、评论审核、优惠券核销可平滑接入
4. **高效联调**：前端可基于 OpenAPI 先做 Mock，不等待后端全部完成

---

## 2. 模块划分

### 2.1 Auth
- 后台管理员登录
- 获取当前用户信息
- Token 刷新（建议下一版加入）
- 登出（建议下一版加入）

### 2.2 User
- 查询小程序用户列表
- 查看用户详情
- 标记/取消测试账号
- 查询用户游戏进度（建议下一版加入）

### 2.3 Poi
- POI 列表、详情、新增、编辑、上下线
- 标签、区域、封面、坐标管理

### 2.4 Storyline
- 故事线列表、详情、新增、编辑、发布
- 绑定 POI 顺序与触发关系

### 2.5 TestConsole
- 模拟定位
- 发放 / 删除 / 清空印章
- 重置进度
- 调整等级
- 批量测试操作（后续再加）

### 2.6 Campaign
- 活动配置
- 广告位配置
- 曝光时间窗与排序

### 2.7 Reward
- 奖励列表、详情、启停
- 奖励类型配置（优惠券 / 虚拟奖励 / 实物占位）

### 2.8 AuditLog
- 审计日志查询
- 操作结果、操作者、时间、目标对象追踪

---

## 3. 权限模型建议

| 角色 | 说明 | 关键权限 |
|------|------|----------|
| SUPER_ADMIN | 超级管理员 | 全部权限 |
| CONTENT_OPERATOR | 内容运营 | POI、故事线、奖励配置 |
| ACTIVITY_OPERATOR | 活动运营 | Campaign、Reward |
| CUSTOMER_SERVICE | 客服 | 用户查询、有限工具 |
| TESTER | 测试人员 | TestConsole、测试账号能力 |
| ANALYST_READONLY | 数据只读 | 仅读取报表与日志 |

### 权限控制原则

1. **查询权限** 与 **修改权限** 分离
2. **测试控制台** 不允许普通运营直接访问
3. **批量操作** 默认高危，需要更高权限或二次确认
4. **所有写接口** 必须写入审计日志

---

## 4. 统一接口规范

### 4.1 请求头

```text
Authorization: Bearer {token}
Content-Type: application/json
X-Trace-Id: {uuid}  // 建议
```

### 4.2 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 4.3 分页格式

```json
{
  "list": [],
  "page": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

### 4.4 时间格式

统一返回 ISO 8601：

```text
2026-04-06T23:30:00+08:00
```

### 4.5 状态枚举建议

- `DRAFT`：草稿
- `PUBLISHED`：已发布
- `OFFLINE`：已下线

---

## 5. 错误码规范建议

| 错误码 | 含义 | 说明 |
|------|------|------|
| 0 | success | 成功 |
| 400 | bad_request | 参数错误 |
| 401 | unauthorized | 未登录或 token 失效 |
| 403 | forbidden | 无权限 |
| 404 | not_found | 资源不存在 |
| 409 | conflict | 状态冲突 / 重复操作 |
| 422 | business_validation_failed | 业务校验失败 |
| 500 | internal_error | 系统异常 |

### 建议增加业务错误前缀

| 业务码 | 场景 |
|------|------|
| U1001 | 用户不存在 |
| U1002 | 非测试账号，不允许执行测试操作 |
| P2001 | POI 不存在 |
| P2002 | 坐标不合法 |
| S3001 | 故事线不存在 |
| T4001 | 测试权限不足 |
| T4002 | 模拟定位失败 |
| R5001 | 奖励配置无效 |
| A6001 | 广告时间窗冲突 |

---

## 6. 核心实体字段建议

### 6.1 POI

| 字段 | 类型 | 说明 |
|------|------|------|
| poiId | long | 主键 |
| name | string | 名称 |
| subtitle | string | 副标题 |
| regionCode | string | 区域编码 |
| latitude | double | 纬度 |
| longitude | double | 经度 |
| coverImageUrl | string | 封面图 |
| status | enum | 发布状态 |
| tags | string[] | 标签 |
| updatedAt | datetime | 更新时间 |

### 6.2 Storyline

| 字段 | 类型 | 说明 |
|------|------|------|
| storylineId | long | 主键 |
| name | string | 故事线名称 |
| description | string | 描述 |
| status | enum | 发布状态 |
| poiIds | long[] | 关联 POI |
| updatedAt | datetime | 更新时间 |

### 6.3 User

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| openId | string | 微信 openId / 平台标识 |
| nickname | string | 昵称 |
| phone | string | 手机号 |
| testFlag | boolean | 是否测试账号 |
| level | int | 用户等级 |
| createdAt | datetime | 注册时间 |

---

## 7. 推荐联调顺序

### 第一阶段：基础链路
1. `/auth/login`
2. `/auth/me`
3. 菜单权限渲染

### 第二阶段：内容管理主链路
4. `/pois`
5. `/pois/{poiId}`
6. `/storylines`

### 第三阶段：测试控制台
7. `/users`
8. `/users/{userId}/test-flag`
9. `/test-console/users/{userId}/location-mock`
10. `/test-console/users/{userId}/stamps/grant`

### 第四阶段：运营配置与审计
11. `/campaigns`
12. `/rewards`
13. `/audit-logs`

这个顺序的好处是：
- 先把框架和权限打通
- 再打通主业务对象
- 最后再处理高风险测试能力与运营能力

---

## 8. 前后端协作建议

### 后端负责
- DTO / VO / 错误码落地
- Springdoc 注解与 Swagger 生成
- 权限中间件与审计日志
- Mock 示例返回

### 前端负责
- 基于 OpenAPI 生成/手写请求层
- 按字段直接做表单与列表页
- 先基于 Mock 联调页面流程
- 记录联调差异，不私自改字段含义

### 协作原则
- 契约先行，代码并行
- 字段改动必须先更新文档
- 不允许“前端先猜、后端再改名”这种反复拉扯

---

## 9. 建议补充接口（下一轮）

### Auth
- `POST /auth/refresh-token`
- `POST /auth/logout`

### User
- `GET /users/{userId}`
- `GET /users/{userId}/progress`
- `POST /test-console/users/{userId}/progress/reset`
- `POST /test-console/users/{userId}/level/adjust`

### Poi
- `PATCH /pois/{poiId}/status`
- `DELETE /pois/{poiId}`（如业务允许）

### Storyline
- `GET /storylines/{storylineId}`
- `PUT /storylines/{storylineId}`
- `PATCH /storylines/{storylineId}/status`

### Reward / Campaign
- 新增、编辑、上下线接口

---

## 10. 结论

当前这份 Swagger V1 适合做三件事：

1. 作为后端接口实现骨架
2. 作为前端 Mock 联调契约
3. 作为项目协作中的统一边界文档

<callout emoji="📌" background-color="light-yellow">
建议下一步直接把 YAML 纳入项目仓库 docs/api 目录，并由后端在 Spring Boot 项目内同步维护，避免“文档一套、代码一套”。
</callout>
