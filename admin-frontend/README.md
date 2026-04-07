# 澳小遊后台管理系统

基于 React 18 + TypeScript + Umi 4 + Ant Design Pro 5.x 开发。

## 技术栈

- **框架**: React 18 + TypeScript
- **脚手架**: Umi 4
- **UI 组件**: Ant Design 5.x + Ant Design Pro 5.x
- **Mock 数据**: Umi Mock

## 项目结构

```
├── config/                 # 配置文件夹
│   └── config.ts          # Umi 配置
├── mock/                  # Mock 数据
│   └── auth.ts           # 认证相关 Mock
├── public/               # 静态资源
├── src/                  # 源码目录
│   ├── app.tsx          # 运行时配置
│   ├── pages/           # 页面组件
│   │   ├── Login/      # 登录页
│   │   ├── Dashboard/  # Dashboard 首页
│   │   ├── Content/    # 内容管理
│   │   │   └── POI/    # POI 管理
│   │   ├── User/       # 用户管理
│   │   │   └── TestAccount/  # 测试账号管理
│   │   ├── Operation/  # 运营管理
│   │   └── System/     # 系统管理
│   ├── components/      # 公共组件
│   ├── services/       # API 服务
│   ├── types/          # TypeScript 类型
│   └── utils/          # 工具函数
├── package.json
├── tsconfig.json
└── README.md
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

## 默认账号

- **用户名**: admin
- **密码**: 123456
- **验证码**: a3b5

## 功能模块

### 已实现

1. **登录页** - 用户认证、验证码
2. **Dashboard** - 数据概览、快捷操作
3. **POI 管理** - POI 列表、新增、编辑、删除
4. **测试账号管理** - 测试账号列表、模拟定位、印章操作
5. **Mock 数据** - 完整的 Mock API

### 开发中

- 故事线管理
- 地图瓦片管理
- 多媒体资源管理
- 用户列表
- 活动发布
- 广告管理
- 数据统计
- 系统配置

## API 接口

详见 `mock/` 目录下的 Mock 数据定义。

## 注意事项

1. 本项目使用 Mock 数据，无需真实后端服务
2. 默认验证码为 `a3b5`
3. 测试账号工具功能为重点开发模块

## License

MIT