# 澳小遊后台管理系统 Swagger API 文档模板

## 1. 文档定位

这是一份用于 **澳小遊后台管理系统** 的 Swagger / OpenAPI 模板，用于前后端并行开发阶段的接口契约对齐。

建议协作方式：
- 后端：基于本模板落地 Spring Boot + Springdoc
- 前端：基于 schemas / example 先做 Mock 联调
- 产品 / 测试：基于接口和错误码补充验收清单

---

## 2. 当前建议覆盖的 MVP 模块

1. Auth：登录、当前用户信息
2. User：用户列表、测试账号标记
3. Poi：POI 管理
4. Storyline：故事线管理
5. TestConsole：模拟定位、印章发放、进度重置
6. Campaign：活动与广告位配置
7. Reward：奖励配置
8. AuditLog：操作审计日志

---

## 3. 目录落地建议

如果项目使用 Spring Boot 3 + springdoc-openapi，建议：

```text
backend/
  src/main/java/.../
    controller/
    dto/
    vo/
    config/
  src/main/resources/
    openapi/
      aoxiaoyou-admin-openapi-v1.yaml
```

仓库文档目录建议同时保留一份：

```text
docs/
  api/
    澳小遊后台管理系统_Swagger_API模板_V1.0.yaml
    澳小遊后台管理系统_API说明_V1.0.md
```

---

## 4. 统一约束建议

### 4.1 响应包装

统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 4.2 分页结构

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

### 4.3 安全要求

- 后台接口统一 JWT Bearer Token
- 所有写操作必须记审计日志
- 测试控制台仅允许 TESTER / SUPER_ADMIN 访问
- 测试账号能力必须与正式账号能力隔离

---

## 5. 联调优先级

建议按下面顺序联调：

1. `/auth/login`
2. `/auth/me`
3. `/pois` 列表 / 新增 / 编辑
4. `/storylines` 列表 / 新增
5. `/test-console/users/{userId}/location-mock`
6. `/test-console/users/{userId}/stamps/grant`
7. `/audit-logs`

原因很简单：先打通登录和核心主链路，再补运营和配置能力。

---

## 6. 下一步建议

下一轮可以继续补充：
- 错误码表
- RBAC 权限矩阵
- 数据字典
- 详细 DTO/VO 字段说明
- 运营后台模块级接口清单
- 小程序端真实依赖的管理接口映射

---

## 7. 附件

本次已生成 OpenAPI YAML 模板文件，可直接导入 Swagger Editor / Apifox / Postman：

- `swagger-template-aoxiaoyou-admin-v1.yaml`

如需要，我下一步可以继续直接补成：
**可落地的完整 V1 接口文档**，包括字段、错误码、权限、示例响应。