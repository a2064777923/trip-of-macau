# 澳小遊后台管理系统 — 技术设计 v2.1（AI 能力中心扩展）

> **版本**: v2.1  
> **日期**: 2026-04-08  
> **状态**: 在 v2.0 正式底座上扩展 AI 中台能力

---

## 一、AI 设计目标升级

原先 `AI 导航配置` 仅覆盖室内导航场景，现已升级为统一的 **AI 能力中心**，用于支撑以下四类核心产品能力：

1. **行程推荐规划**
   - 根据天数、偏好、预算、体力、出发点、是否亲子出行等条件
   - 输出多天多时段的游玩路线与 POI 推荐顺序

2. **旅行问答**
   - 回答景点、玩法、故事线、活动、交通、美食相关问题
   - 后续可接知识库和内容库增强回答准确度

3. **拍照识别室内定位**
   - 基于视觉锚点图片、参考物、楼层锚点、店铺/电梯/楼梯等信息
   - 推断用户所在建筑、楼层、朝向与附近目标点

4. **NPC 介绍语音对话生成**
   - 生成适合语音播报的景点讲解词、NPC 互动对话、多语言介绍
   - 为后续 TTS/角色人设系统预留接入点

---

## 二、AI 中台架构

```text
AI 能力中心
├── 供应商管理
│   └── ai_provider_configs
├── 场景策略管理
│   └── ai_navigation_policies（扩展为通用策略表）
├── 请求日志
│   └── ai_request_logs
└── 前端管理页
    └── /space/ai-navigation → AI 能力中心
```

### 2.1 供应商层
用于配置：
- 供应商名 / 显示名
- API Base URL
- 默认模型
- 能力开关（text / vision / multimodal / speech / voice）
- 超时、重试、日配额、成本

### 2.2 策略层
`ai_navigation_policies` 已扩展，不再仅表示导航：
- `scenario_code`: `itinerary_planning`, `travel_qa`, `indoor_vision_positioning`, `npc_voice_dialogue`
- `scenario_group`: `planning`, `qa`, `vision`, `dialogue`, `navigation`
- `multimodal_enabled`
- `voice_enabled`
- `prompt_template`
- `system_prompt`
- `response_schema`
- `post_process_rules`

### 2.3 日志层
新增 `ai_request_logs`：
- 记录场景、耗时、token、成本、是否成功、错误信息
- 为后续调试、成本控制、失败追踪、灰度评估提供基础

---

## 三、当前已实现的 AI 场景种子策略

| 场景 | strategy_name | 能力说明 |
|------|---------------|----------|
| 行程规划 | `澳门智能行程规划` | 多天路线规划、地理动线、偏好约束 |
| 旅行问答 | `澳门旅行问答` | 景点/交通/玩法知识问答 |
| 拍照识别定位 | `室内拍照识别定位` | 图像 + 锚点 + 楼层推断 |
| NPC 对话 | `NPC语音讲解对话` | 讲解词、多轮互动、TTS 文案 |

默认供应商种子：
- `hunyuan` / 腾讯混元
- 默认模型：`hunyuan-2.0-instruct-20251111`

---

## 四、后端接口

### 4.1 已实现
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/v1/ai/providers` | 供应商列表 |
| GET | `/api/admin/v1/ai/policies` | 策略列表，可按 `scenarioGroup` 过滤 |

### 4.2 下一步建议
- `POST /api/admin/v1/ai/providers` 新增供应商
- `PUT /api/admin/v1/ai/providers/{id}` 更新供应商
- `POST /api/admin/v1/ai/policies` 新增策略
- `PUT /api/admin/v1/ai/policies/{id}` 更新策略
- `POST /api/admin/v1/ai/test` 场景测试
- `GET /api/admin/v1/ai/logs` 请求日志查询

---

## 五、前端页面

页面入口：`#/space/ai-navigation`

当前真实页面能力：
- AI 场景卡片（规划 / 问答 / 视觉定位 / NPC 对话）
- 供应商表格
- 策略矩阵表格

后续升级方向：
- Prompt 编辑器
- 场景测试面板
- 成本/调用统计图
- 失败日志明细
- 多模型对比测试

---

## 六、与产品能力的对应关系

| 产品功能 | AI 场景 | 数据依赖 |
|----------|---------|----------|
| 智能行程规划 | planning | cities / pois / storylines / activities |
| 旅行问答 | qa | 内容库 / 知识库 / 活动 / 交通信息 |
| 室内拍照定位 | vision | indoor_visual_anchors / indoor_nodes / buildings / floors |
| NPC 讲解对话 | dialogue | storyline_chapters / media / npc 角色设定（后续） |
| 导航辅助 | navigation | indoor_edges / anchors / target points |

---

## 七、当前结论

AI 在本项目中应被视为 **统一能力层**，而不是仅仅“导航附属功能”。

因此后台设计采用：
- **供应商中心**
- **场景策略中心**
- **日志与成本中心**
- **测试调试中心（下一批）**

这能更好支撑后续的小程序产品能力扩张，而不需要每新增一种 AI 用法就重新搭一个孤立后台模块。
