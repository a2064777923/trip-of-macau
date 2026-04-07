# 澳小遊小程序前端 (Trip of Macau Client)

## 项目简介

澳小遊是一款基于 Taro 3.x + React 18 开发的微信小程序，为全球首个「卡通手绘地图 + 故事解谜 + 游戏化探索」的澳门文旅小程序提供前端支持。

## 核心功能

- 🗺️ **手绘地图系统** - Canvas 2D 渲染，CDN 瓦片加载
- 📍 **GPS+Wi-Fi 定位** - 多源定位，动态触发半径
- 🎯 **印章收集系统** - 足迹/故事/任务/秘密/组队印章
- 📖 **故事线系统** - 多章节叙事，多媒体内容
- 👴 **长者模式** - 大字+语音+极简操作
- 🎁 **奖励兑换** - 印章兑换实物奖励

## 技术栈

- **框架**: Taro 3.6.23 + React 18
- **状态管理**: Zustand
- **样式**: SCSS
- **构建**: Webpack 5
- **API**: RESTful + Mock (开发阶段)

## 项目结构

```
src/
├── components/          # 公共组件
│   ├── Map/            # 地图组件
│   ├── StampCard/      # 印章卡片
│   ├── StoryCard/      # 故事卡片
│   └── Loading/        # 加载组件
├── pages/              # 页面
│   ├── index/          # 首页
│   ├── map/            # 地图
│   ├── profile/        # 我的
│   ├── story/          # 故事线
│   ├── stamps/         # 印章
│   ├── rewards/        # 奖励
│   └── settings/       # 设置
├── store/              # 状态管理
│   └── userStore.ts    # 用户状态
├── services/           # API服务
│   ├── api.ts          # API封装
│   ├── mock.ts         # Mock数据
│   └── mockApi.ts      # Mock服务
├── utils/              # 工具函数
│   ├── location.ts     # 位置工具
│   └── common.ts       # 通用工具
├── styles/             # 样式
│   ├── variables.scss  # 变量
│   ├── mixins.scss     # 混合
│   └── index.scss      # 入口
├── app.ts              # 应用入口
└── app.config.ts       # 应用配置
```

## 开发环境

### 前置要求

- Node.js >= 18
- npm >= 8
- 微信开发者工具

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
# 微信小程序
npm run dev:weapp

# H5
npm run dev:h5
```

### 构建

```bash
# 微信小程序
npm run build:weapp

# H5
npm run build:h5
```

## API 说明

开发阶段使用 Mock API，生产环境对接后端服务。

### 请求示例

```typescript
import { api } from '@/services/api'

// 获取用户印章
const stamps = await api.stamps.getUserStamps()

// 上报位置
await api.map.updateLocation({
  latitude: 22.1972,
  longitude: 113.5408,
  accuracy: 10
})
```

## 状态管理

使用 Zustand 进行状态管理。

```typescript
import { useUserStore } from '@/store/userStore'

// 在组件中使用
const { userInfo, setUserInfo } = useUserStore()

// 设置用户信息
setUserInfo({
  nickname: '小明',
  avatarUrl: '...'
})
```

## 样式规范

### 颜色使用

```scss
// 主色调
.text-primary { color: $primary; }
.bg-primary { background-color: $primary; }

// 功能色
.text-success { color: $success; }
.text-warning { color: $warning; }
.text-error { color: $error; }
```

### 间距使用

```scss
// 内边距
.p-md { padding: $spacing-md; }
.pt-md { padding-top: $spacing-md; }
.px-md { padding-left: $spacing-md; padding-right: $spacing-md; }

// 外边距
.m-md { margin: $spacing-md; }
.mt-md { margin-top: $spacing-md; }
.mx-md { margin-left: $spacing-md; margin-right: $spacing-md; }
```

## 开发规范

### 代码规范

- 使用 TypeScript 编写代码
- 组件使用函数式组件 + Hooks
- 使用绝对路径导入模块 (`@/components/...`)
- 保持组件小而专注

### 提交规范

```
feat: 新功能
fix: 修复问题
docs: 文档变更
style: 代码格式（不影响功能）
refactor: 重构
test: 测试
chore: 构建/工具
```

## 调试技巧

### 使用微信开发者工具

1. 打开项目中的 `dist` 目录
2. 在控制台查看日志输出
3. 使用 Network 面板查看请求
4. 使用 Storage 面板查看本地存储

### 常用调试命令

```javascript
// 查看当前页面路径
console.log(getCurrentPages())

// 查看系统信息
console.log(wx.getSystemInfoSync())

// 查看存储数据
console.log(wx.getStorageInfoSync())
```

## 部署

### 预览

在微信开发者工具中点击「预览」，扫描二维码在真机上测试。

### 上传

1. 执行构建命令: `npm run build:weapp`
2. 在微信开发者工具中点击「上传」
3. 填写版本号和项目备注
4. 登录微信公众平台提交审核

## 贡献指南

1. Fork 项目仓库
2. 创建功能分支: `git checkout -b feature/my-feature`
3. 提交更改: `git commit -m 'feat: add some feature'`
4. 推送分支: `git push origin feature/my-feature`
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

- 项目主页: https://github.com/a2064777923/trip-of-macau
- 问题反馈: https://github.com/a2064777923/trip-of-macau/issues

---

Made with ❤️ by 澳小遊团队
