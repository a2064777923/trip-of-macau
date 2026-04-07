# 澳小遊（Trip of Macau）后台管理系统技术方案
**版本：V1.0****日期：2026年4月****状态：设计阶段**
---

## 一、概述
### 1.1 项目背景
澳小遊后台管理系统是为「澳小遊」微信小程序提供的内容管理、用户管理、运营支持和系统配置的综合性管理平台。该系统面向运营人员、内容编辑、测试人员和技术管理员，支持日常内容更新、用户数据分析、活动运营和系统配置等功能。

### 1.2 设计目标
- **快速迭代**：支持内容快速更新，无需发布新版本即可上线新内容
- **数据驱动**：提供完善的数据统计和分析能力，支撑运营决策
- **测试友好**：提供完整的测试账号管理和数据调整功能
- **安全可靠**：完善的权限管理和操作审计，保障数据安全

### 1.3 技术选型原则
- **成熟稳定**：选择经过生产环境验证的技术栈
- **生态丰富**：优先选择社区活跃、文档完善的技术
- **团队熟悉**：考虑团队现有技术储备和学习成本

---

## 二、技术架构
### 2.1 技术栈选型

| 层级 | 技术选型 | 版本 | 说明 |
|------|----------|------|------|
| **前端框架** | React 18 | 18.2.x | 主流前端框架，生态丰富 |
| **UI组件库** | Ant Design Pro | 5.x | 中后台成熟方案，开箱即用 |
| **状态管理** | Zustand | 4.x | 轻量级状态管理，学习成本低 |
| **服务端状态** | React Query / TanStack Query | 4.x | 服务端状态缓存，自动刷新 |
| **路由** | React Router | 6.x | React官方路由方案 |
| **构建工具** | Vite | 4.x | 快速构建，开发体验好 |
| **TypeScript** | TypeScript | 5.x | 类型安全，提高代码质量 |
| **后端服务** | Spring Boot | 3.x (Java 17) | 与小程序后端统一技术栈 |
| **数据库** | MySQL | 8.0 | 关系型数据库 |
| **缓存** | Redis | 7.x | 高性能缓存 |
| **对象存储** | 阿里云OSS / 腾讯云COS | - | 文件存储 |
| **CDN** | 阿里云CDN / 腾讯云CDN | - | 静态资源加速 |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         客户端层 (浏览器)                             │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │              React 18 + Ant Design Pro 前端应用                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │   │
│  │  │ 内容管理  │  │ 用户管理  │  │ 运营管理  │  │ 系统配置  │     │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS / REST API
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                         API 网关层                                   │
│                   (腾讯云 API Gateway / Nginx)                       │
│              职责：认证鉴权、限流、负载均衡、日志                     │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                      业务服务层 (Spring Boot)                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ 内容服务  │  │ 用户服务  │  │ 定位服务  │  │ 游戏服务  │  │ 运营服务  │   │
│  │ (CMS)    │  │ (User)   │  │ (GPS)    │  │ (Stamp)  │  │ (Ops)    │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐               │
│  │ 故事服务  │  │ 社交服务  │  │ 奖励服务  │  │ 系统服务  │               │
│  │ (Story)  │  │ (Social) │  │ (Reward) │  │ (Admin)  │               │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘               │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                        数据/存储层                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │   MySQL  │  │  Redis   │  │   OSS    │  │   CDN    │             │
│  │ (主数据)  │  │  (缓存)  │  │ (文件)   │  │ (加速)   │             │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.3 部署架构

```
生产环境 (Production)
├── 腾讯云 / 阿里云
│   ├── ECS / CVM (2核4G x 2) - 应用服务器
│   ├── RDS MySQL (1核2G) - 数据库
│   ├── Redis (256MB) - 缓存
│   ├── OSS / COS - 对象存储
│   ├── CDN - 静态加速
│   └── API Gateway - 网关
│
└── 域名 & HTTPS
    ├── admin.tripofmacau.com (后台管理)
    ├── api.tripofmacau.com (API接口)
    └── cdn.tripofmacau.com (静态资源)

测试环境 (Staging)
├── 单实例部署
│   ├── 1台 ECS / CVM (2核4G)
│   └── 内嵌 MySQL + Redis
│
└── 测试数据隔离
    └── 自动同步生产结构

开发环境 (Development)
├── 本地 Docker Compose
│   ├── Spring Boot 本地启动
│   ├── MySQL (Docker)
│   ├── Redis (Docker)
│   └── MinIO (模拟 OSS)
│
└── 前端 Vite 本地开发服务器
    └── 代理到本地后端
```

---

## 三、核心功能模块

### 3.1 模块划分

| 模块 | 功能描述 | 主要用户 | 优先级 |
|------|----------|----------|--------|
| **内容管理 (CMS)** | POI管理、故事线管理、地图瓦片管理、多媒体资源管理 | 内容编辑、运营人员 | P0 |
| **用户管理** | 用户列表、用户详情、测试账号管理、数据调整 | 运营人员、测试人员 | P0 |
| **运营管理** | 活动发布、广告管理、奖励配置、数据统计 | 运营人员、市场人员 | P1 |
| **系统管理** | 账号管理、角色权限、操作日志、系统配置 | 系统管理员 | P1 |
| **测试工具** | 模拟定位、数据重置、批量操作 | 测试人员 | P0 |

### 3.2 功能模块详细设计

#### 3.2.1 内容管理 (CMS)

**POI 管理**
- POI 列表（支持搜索、筛选、分页）
- POI 新增/编辑/删除
- 批量导入（Excel 模板）
- 批量导出
- POI 分类管理
- POI 与故事线关联
- 地图预览（在地图上查看POI位置）

**故事线管理**
- 故事线列表
- 故事线新增/编辑/删除
- 章节管理（增删改查、排序）
- 章节内容编辑（富文本编辑器）
- 多媒体资源关联（音频、视频、图片）
- 解锁条件配置

**地图瓦片管理**
- 瓦片上传（支持批量拖拽上传）
- 瓦片预览
- 瓦片版本管理
- 瓦片 CDN 刷新
- 控制点配置（地图坐标校准）

**多媒体资源管理**
- 资源列表（图片、音频、视频）
- 资源上传（支持大文件分片上传）
- 资源预览
- 资源分类管理
- 资源引用统计

#### 3.2.2 用户管理

**用户列表**
- 用户基本信息展示（头像、昵称、OpenID、注册时间等）
- 高级搜索（按注册时间、印章数量、等级等筛选）
- 用户详情查看
- 用户数据导出

**测试账号管理（重点功能）**
- 测试账号标记/取消标记
- 测试账号列表（快速筛选）
- 测试数据面板（核心功能）

**测试数据调整功能（测试人员专用）**
- **模拟定位**：设置测试账号的虚拟位置（无需真实去澳门），支持地图点选和坐标输入
- **印章操作**：
  - 快速获得指定印章
  - 批量获得多个印章
  - 删除指定印章
  - 清空所有印章
- **进度重置**：
  - 重置所有游戏进度（印章、等级、成就）
  - 重置故事线进度
  - 重置到指定初始状态
- **等级调整**：直接设置用户等级和头衔
- **触发模拟**：模拟到达指定地点，测试触发弹窗逻辑
- **批量操作**：对多个测试账号执行相同操作
- **操作日志**：记录所有测试数据调整操作，支持回滚

**用户行为查看**
- 用户操作日志（时间线形式）
- 用户位置轨迹（地图展示）
- 用户印章收集时间线
- 用户故事线完成进度

#### 3.2.3 运营管理

**活动发布**
- 活动列表（进行中/已结束）
- 活动创建/编辑/删除
- 活动时间配置
- 活动范围配置（全量/部分用户/测试账号）
- 活动内容编辑（富文本）
- 活动效果统计

**广告管理**
- 广告位管理（位置定义）
- 广告内容管理
- 广告投放时间配置
- 广告点击统计
- A/B测试支持

**奖励配置**
- 奖励类型管理
- 奖励规则配置
- 奖励库存管理
- 奖励领取统计
- 奖励发放记录

**数据统计（Dashboard）**
- 核心指标看板（实时/日/周/月）
  - 活跃用户数（DAU/MAU）
  - 新增用户数
  - 用户留存率
  - 平均使用时长
  - 印章收集数
  - 故事线完成率
- 地理位置热力图
- 用户行为漏斗分析
- 印章收集排行
- 热门POI统计
- 用户反馈统计

#### 3.2.4 系统管理

**账号管理（管理员账号）**
- 管理员列表
- 管理员新增/编辑/删除
- 管理员密码重置
- 管理员状态管理（启用/禁用）

**角色权限管理（RBAC）**
- 角色列表
- 角色新增/编辑/删除
- 权限配置（菜单权限、按钮权限、数据权限）
- 角色-用户关联

**操作日志**
- 登录日志（IP、时间、设备、结果）
- 操作日志（谁在什么时间做了什么操作）
- 数据变更日志（字段级变更记录）
- 异常日志
- 日志查询与导出

**系统配置**
- 基础配置（系统名称、Logo、版权信息等）
- 定位配置（触发半径、GPS精度阈值等）
- 缓存配置
- 通知配置（邮件、短信）
- 第三方服务配置（OSS、CDN、地图等）

#### 3.2.5 测试工具（专用模块）

**模拟定位工具**
- 地图点选定位
- 坐标输入定位
- 批量定位设置
- 定位轨迹模拟（按时间序列移动）

**数据生成工具**
- 批量生成测试账号
- 批量生成模拟数据（印章、进度等）
- 数据模板导入

**触发测试工具**
- 模拟到达指定POI
- 批量触发测试
- 触发结果验证
- 触发日志分析

**性能测试工具**
- 并发用户模拟
- 接口压力测试
- 地图瓦片加载测试
- 定位服务压力测试

---

## 四、技术架构

### 4.1 后端技术栈

| 技术 | 选型 | 版本 | 说明 |
|------|------|------|------|
| **开发语言** | Java | 17 LTS | 长期支持版本，性能优异 |
| **基础框架** | Spring Boot | 3.2.x | 简化配置，快速开发 |
| **Web框架** | Spring MVC | 6.x | RESTful API 开发 |
| **ORM框架** | MyBatis-Plus | 3.5.x | 高效CRUD，代码生成 |
| **数据库** | MySQL | 8.0 | 主数据存储 |
| **缓存** | Redis | 7.x | 会话缓存、热点数据 |
| **安全框架** | Spring Security | 6.x | 认证鉴权 |
| **JWT** | Java-JWT | 4.x | Token生成与验证 |
| **API文档** | SpringDoc OpenAPI | 2.x | 自动生成API文档 |
| **任务调度** | XXL-Job | 2.4.x | 分布式任务调度 |
| **对象存储** | 阿里云OSS SDK | 3.x | 文件上传下载 |
| **地图服务** | 腾讯地图SDK | Java版 | 地理编码、逆编码 |
| **JSON处理** | Jackson | 2.x | 高性能JSON处理 |
| **工具类** | Hutool | 5.x | Java工具集 |
| **日志** | SLF4J + Logback | - | 日志记录 |
| **单元测试** | JUnit 5 + Mockito | 5.x | 单元测试 |

### 4.2 项目结构

```
aoxiaoyou-admin/
├── aoxiaoyou-admin-api/          # API接口模块（对外暴露）
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/api/
│   │       ├── controller/         # 控制器层
│   │       ├── dto/               # 数据传输对象
│   │       ├── vo/                # 视图对象
│   │       └── converter/         # 对象转换器
│   └── src/main/resources/
│       └── application-api.yml    # API模块配置
│
├── aoxiaoyou-admin-service/       # 业务逻辑模块
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/service/
│   │       ├── service/           # 业务接口
│   │       ├── impl/              # 业务实现
│   │       ├── bo/                # 业务对象
│   │       └── event/             # 业务事件
│   └── src/main/resources/
│       └── application-service.yml
│
├── aoxiaoyou-admin-dao/           # 数据访问模块
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/dao/
│   │       ├── mapper/              # MyBatis Mapper接口
│   │       ├── entity/              # 数据库实体
│   │       └── handler/             # 类型处理器
│   └── src/main/resources/
│       └── mapper/                  # Mapper XML文件
│
├── aoxiaoyou-admin-common/         # 公共模块
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/common/
│   │       ├── constant/            # 常量定义
│   │       ├── enums/               # 枚举定义
│   │       ├── exception/           # 异常定义
│   │       ├── utils/               # 工具类
│   │       ├── result/              # 统一返回结果
│   │       └── base/                # 基础类
│   └── src/main/resources/
│
├── aoxiaoyou-admin-security/        # 安全模块
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/security/
│   │       ├── config/              # 安全配置
│   │       ├── filter/              # 安全过滤器
│   │       ├── handler/             # 安全处理器
│   │       ├── provider/            # 认证提供者
│   │       └── utils/               # 安全工具
│   └── src/main/resources/
│
├── aoxiaoyou-admin-start/           # 启动模块
│   ├── src/main/java/
│   │   └── com/aoxiaoyou/admin/
│   │       └── AdminApplication.java
│   └── src/main/resources/
│       ├── application.yml          # 主配置文件
│       ├── application-dev.yml      # 开发环境
│       ├── application-test.yml     # 测试环境
│       └── application-prod.yml     # 生产环境
│
├── pom.xml                          # 父POM
└── README.md
```

### 2.3 后端核心依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.aoxiaoyou</groupId>
    <artifactId>aoxiaoyou-admin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- 依赖版本 -->
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jwt.version>4.4.0</jwt.version>
        <hutool.version>5.8.23</hutool.version>
        <springdoc.version>2.3.0</springdoc.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- 内部模块 -->
            <dependency>
                <groupId>com.aoxiaoyou</groupId>
                <artifactId>aoxiaoyou-admin-api</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.aoxiaoyou</groupId>
                <artifactId>aoxiaoyou-admin-service</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.aoxiaoyou</groupId>
                <artifactId>aoxiaoyou-admin-dao</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.aoxiaoyou</groupId>
                <artifactId>aoxiaoyou-admin-common</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.aoxiaoyou</groupId>
                <artifactId>aoxiaoyou-admin-security</artifactId>
                <version>${project.version}</version>
            </dependency>
            
            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            
            <!-- JWT -->
            <dependency>
                <groupId>com.auth0</groupId>
                <artifactId>java-jwt</artifactId>
                <version>${jwt.version}</version>
            </dependency>
            
            <!-- Hutool -->
            <dependency>
                <groupId>cn.hutool</groupId>
                <artifactId>hutool-all</artifactId>
                <version>${hutool.version}</version>
            </dependency>
            
            <!-- SpringDoc OpenAPI -->
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

---


---

## 五、数据库设计

### 5.1 后台管理专用表

```sql
-- ==================== 管理员相关表 ====================

-- 管理员表
CREATE TABLE sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    password VARCHAR(128) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(64) COMMENT '昵称',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(256) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(256) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 管理员-角色关联表
CREATE TABLE sys_admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_role (admin_id, role_id),
    KEY idx_admin_id (admin_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';

-- 权限表
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID，0-顶级',
    permission_type TINYINT NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(64) NOT NULL COMMENT '权限名称',
    path VARCHAR(256) COMMENT '前端路径/接口路径',
    icon VARCHAR(64) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_parent_id (parent_id),
    KEY idx_type (permission_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色-权限关联表
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- ==================== 操作日志表 ====================

-- 登录日志表
CREATE TABLE sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT COMMENT '管理员ID',
    username VARCHAR(64) COMMENT '登录账号',
    login_type TINYINT DEFAULT 1 COMMENT '登录类型：1-账号密码',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    ip_location VARCHAR(128) COMMENT 'IP归属地',
    user_agent VARCHAR(512) COMMENT 'User-Agent',
    browser VARCHAR(64) COMMENT '浏览器',
    os VARCHAR(64) COMMENT '操作系统',
    device VARCHAR(64) COMMENT '设备类型',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg VARCHAR(256) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_id (admin_id),
    KEY idx_username (username),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT COMMENT '管理员ID',
    username VARCHAR(64) COMMENT '操作人账号',
    module VARCHAR(64) COMMENT '操作模块',
    operation VARCHAR(64) COMMENT '操作类型',
    description VARCHAR(256) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(256) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    execute_time INT COMMENT '执行时长(ms)',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_id (admin_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ==================== 内容管理表 ====================

-- POI分类表
CREATE TABLE poi_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon VARCHAR(256) COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POI分类表';

-- POI表（业务表，后台可管理）
CREATE TABLE poi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_zh VARCHAR(128) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(128) COMMENT '英文名称',
    name_zht VARCHAR(128) COMMENT '繁体名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    description_zh TEXT COMMENT '中文描述',
    description_en TEXT COMMENT '英文描述',
    description_zht TEXT COMMENT '繁体描述',
    latitude DECIMAL(10,8) NOT NULL COMMENT '纬度',
    longitude DECIMAL(11,8) NOT NULL COMMENT '经度',
    address_zh VARCHAR(256) COMMENT '中文地址',
    address_en VARCHAR(256) COMMENT '英文地址',
    importance ENUM('normal', 'important', 'very_important') DEFAULT 'normal' COMMENT '重要性',
    base_trigger_radius INT DEFAULT 30 COMMENT '基础触发半径(米)',
    is_indoor TINYINT DEFAULT 0 COMMENT '是否室内：0-否，1-是',
    stamp_type VARCHAR(32) COMMENT '印章类型',
    story_line_id BIGINT COMMENT '所属故事线ID',
    media_urls JSON COMMENT '多媒体资源URL列表',
    opening_hours_zh VARCHAR(256) COMMENT '开放时间',
    phone VARCHAR(32) COMMENT '联系电话',
    website VARCHAR(256) COMMENT '官方网站',
    tags JSON COMMENT '标签',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-下线',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_category_id (category_id),
    KEY idx_story_line_id (story_line_id),
    KEY idx_location (latitude, longitude),
    KEY idx_status (status),
    FULLTEXT KEY ft_name_desc (name_zh, description_zh)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POI表';

-- 故事线表
CREATE TABLE story_line (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    line_code VARCHAR(64) NOT NULL COMMENT '故事线编码',
    name_zh VARCHAR(64) NOT NULL COMMENT '中文名称',
    name_en VARCHAR(64) COMMENT '英文名称',
    description_zh TEXT COMMENT '中文描述',
    description_en TEXT COMMENT '英文描述',
    cover_image VARCHAR(256) COMMENT '封面图',
    total_chapters INT DEFAULT 0 COMMENT '总章节数',
    difficulty TINYINT DEFAULT 1 COMMENT '难度：1-5',
    estimated_time INT COMMENT '预计完成时间(分钟)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-下线',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_line_code (line_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故事线表';

-- 故事章节表
CREATE TABLE story_chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    story_line_id BIGINT NOT NULL COMMENT '故事线ID',
    chapter_order INT NOT NULL COMMENT '章节序号',
    title_zh VARCHAR(128) NOT NULL COMMENT '中文标题',
    title_en VARCHAR(128) COMMENT '英文标题',
    subtitle_zh VARCHAR(256) COMMENT '中文副标题',
    subtitle_en VARCHAR(256) COMMENT '英文副标题',
    content_zh TEXT COMMENT '中文内容',
    content_en TEXT COMMENT '英文内容',
    script_zh TEXT COMMENT '中文剧本（语音播报用）',
    script_en TEXT COMMENT '英文剧本',
    media_type ENUM('animation', 'audio', 'video', 'image', 'mixed') NOT NULL COMMENT '媒体类型',
    media_urls JSON COMMENT '媒体资源URL',
    cover_image VARCHAR(256) COMMENT '封面图',
    related_poi_id BIGINT COMMENT '关联POI ID',
    unlock_type ENUM('auto', 'distance', 'task', 'story') DEFAULT 'auto' COMMENT '解锁方式',
    unlock_condition JSON COMMENT '解锁条件',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_story_order (story_line_id, chapter_order),
    KEY idx_story_line_id (story_line_id),
    KEY idx_poi_id (related_poi_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故事章节表';

-- 地图瓦片配置表
CREATE TABLE map_tile_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    map_id VARCHAR(64) NOT NULL COMMENT '地图ID',
    map_name VARCHAR(64) NOT NULL COMMENT '地图名称',
    style ENUM('cartoon', 'retro', 'azulejo', 'neon') DEFAULT 'cartoon' COMMENT '地图风格',
    cdn_base_url VARCHAR(256) NOT NULL COMMENT 'CDN基础URL',
    thumbnail_url VARCHAR(256) COMMENT '缩略图URL',
    min_zoom INT DEFAULT 1 COMMENT '最小缩放级别',
    max_zoom INT DEFAULT 4 COMMENT '最大缩放级别',
    tile_size INT DEFAULT 256 COMMENT '瓦片大小',
    tile_format VARCHAR(10) DEFAULT 'jpg' COMMENT '瓦片格式',
    center_lat DECIMAL(10,8) COMMENT '地图中心纬度',
    center_lng DECIMAL(11,8) COMMENT '地图中心经度',
    bounds JSON COMMENT '地图边界 [minLng, minLat, maxLng, maxLat]',
    control_points JSON COMMENT '控制点配置',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地图',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_map_id (map_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图瓦片配置表';

-- 多媒体资源表
CREATE TABLE media_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type ENUM('image', 'audio', 'video', 'document') NOT NULL COMMENT '资源类型',
    resource_name VARCHAR(128) NOT NULL COMMENT '资源名称',
    original_name VARCHAR(256) COMMENT '原始文件名',
    oss_url VARCHAR(256) NOT NULL COMMENT 'OSS存储URL',
    cdn_url VARCHAR(256) NOT NULL COMMENT 'CDN访问URL',
    file_size BIGINT COMMENT '文件大小(字节)',
    mime_type VARCHAR(64) COMMENT 'MIME类型',
    width INT COMMENT '图片宽度',
    height INT COMMENT '图片高度',
    duration INT COMMENT '音视频时长(秒)',
    tags JSON COMMENT '标签',
    description VARCHAR(512) COMMENT '描述',
    category_id BIGINT COMMENT '分类ID',
    reference_count INT DEFAULT 0 COMMENT '引用次数',
    reference_ids JSON COMMENT '引用位置记录',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_type (resource_type),
    KEY idx_category (category_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多媒体资源表';

-- ==================== 测试管理表 ====================

-- 测试账号标记表
CREATE TABLE test_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '小程序用户ID',
    user_open_id VARCHAR(64) NOT NULL COMMENT '用户OpenID',
    nickname VARCHAR(64) COMMENT '昵称',
    remark VARCHAR(256) COMMENT '备注说明',
    test_group VARCHAR(64) DEFAULT 'default' COMMENT '测试分组',
    mock_location_lat DECIMAL(10,8) COMMENT '模拟位置纬度',
    mock_location_lng DECIMAL(11,8) COMMENT '模拟位置经度',
    is_mock_enabled TINYINT DEFAULT 0 COMMENT '是否启用模拟定位',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_test_group (test_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试账号标记表';

-- 测试操作日志表
CREATE TABLE test_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '操作人',
    username VARCHAR(64) COMMENT '操作人账号',
    test_user_id BIGINT NOT NULL COMMENT '测试用户ID',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(256) COMMENT '操作描述',
    before_data JSON COMMENT '操作前数据',
    after_data JSON COMMENT '操作后数据',
    request_params JSON COMMENT '请求参数',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin_id (admin_id),
    KEY idx_test_user_id (test_user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试操作日志表';

-- ==================== 运营管理表 ====================

-- 活动表
CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_code VARCHAR(64) NOT NULL COMMENT '活动编码',
    title_zh VARCHAR(128) NOT NULL COMMENT '中文标题',
    title_en VARCHAR(128) COMMENT '英文标题',
    description_zh TEXT COMMENT '中文描述',
    description_en TEXT COMMENT '英文描述',
    cover_image VARCHAR(256) COMMENT '封面图',
    content_zh TEXT COMMENT '活动内容（富文本）',
    content_en TEXT COMMENT '活动内容（英文）',
    activity_type ENUM('global', 'limited', 'targeted') DEFAULT 'global' COMMENT '活动类型',
    target_users JSON COMMENT '目标用户配置',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    sort_order INT DEFAULT 0 COMMENT '排序',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    participate_count INT DEFAULT 0 COMMENT '参与次数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-进行中，2-已结束，3-已下线',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_activity_code (activity_code),
    KEY idx_status (status),
    KEY idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 广告位表
CREATE TABLE ad_position (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    position_code VARCHAR(64) NOT NULL COMMENT '广告位编码',
    position_name VARCHAR(64) NOT NULL COMMENT '广告位名称',
    position_desc VARCHAR(256) COMMENT '广告位描述',
    position_type ENUM('banner', 'popup', 'float', 'native') NOT NULL COMMENT '广告位类型',
    max_ad_count INT DEFAULT 1 COMMENT '最大广告数量',
    width INT COMMENT '宽度',
    height INT COMMENT '高度',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_position_code (position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告位表';

-- 广告内容表
CREATE TABLE ad_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    position_id BIGINT NOT NULL COMMENT '广告位ID',
    ad_name VARCHAR(128) COMMENT '广告名称',
    title VARCHAR(256) COMMENT '广告标题',
    image_url VARCHAR(256) COMMENT '图片URL',
    link_url VARCHAR(256) COMMENT '跳转链接',
    link_type ENUM('url', 'page', 'mini_program') DEFAULT 'url' COMMENT '链接类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    priority INT DEFAULT 0 COMMENT '优先级',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    view_count INT DEFAULT 0 COMMENT '展示次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_by BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    KEY idx_position_id (position_id),
    KEY idx_status_time (status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告内容表';

-- 系统配置表
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type ENUM('string', 'int', 'boolean', 'json') DEFAULT 'string' COMMENT '配置类型',
    description VARCHAR(256) COMMENT '配置说明',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

---

## 六、核心API接口设计

### 6.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000,
  "traceId": "a1b2c3d4e5f6g7h8"
}
```

### 6.2 核心API列表

#### 认证相关

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | /api/v1/auth/login | 账号密码登录 |
| 登出 | POST | /api/v1/auth/logout | 退出登录 |
| 刷新Token | POST | /api/v1/auth/refresh | 刷新Access Token |
| 获取当前用户 | GET | /api/v1/auth/current | 获取登录用户信息 |

#### 管理员管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 管理员列表 | GET | /api/v1/admins | 分页查询管理员列表 |
| 管理员详情 | GET | /api/v1/admins/{id} | 获取管理员详情 |
| 创建管理员 | POST | /api/v1/admins | 创建管理员 |
| 更新管理员 | PUT | /api/v1/admins/{id} | 更新管理员 |
| 删除管理员 | DELETE | /api/v1/admins/{id} | 删除管理员 |
| 重置密码 | PUT | /api/v1/admins/{id}/password/reset | 重置密码 |
| 修改密码 | PUT | /api/v1/admins/password | 修改自己密码 |

#### 角色权限管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 角色列表 | GET | /api/v1/roles | 获取角色列表 |
| 角色详情 | GET | /api/v1/roles/{id} | 获取角色详情 |
| 创建角色 | POST | /api/v1/roles | 创建角色 |
| 更新角色 | PUT | /api/v1/roles/{id} | 更新角色 |
| 删除角色 | DELETE | /api/v1/roles/{id} | 删除角色 |
| 权限树 | GET | /api/v1/permissions/tree | 获取权限树 |
| 角色权限 | GET | /api/v1/roles/{id}/permissions | 获取角色权限 |
| 分配权限 | PUT | /api/v1/roles/{id}/permissions | 分配权限 |

#### POI管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| POI列表 | GET | /api/v1/pois | 分页查询POI列表 |
| POI详情 | GET | /api/v1/pois/{id} | 获取POI详情 |
| 创建POI | POST | /api/v1/pois | 创建POI |
| 更新POI | PUT | /api/v1/pois/{id} | 更新POI |
| 删除POI | DELETE | /api/v1/pois/{id} | 删除POI |
| 批量导入 | POST | /api/v1/pois/import | Excel批量导入 |
| 批量导出 | GET | /api/v1/pois/export | Excel导出 |
| POI分类 | GET | /api/v1/poi-categories | 获取POI分类 |

#### 故事线管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 故事线列表 | GET | /api/v1/story-lines | 分页查询故事线列表 |
| 故事线详情 | GET | /api/v1/story-lines/{id} | 获取故事线详情 |
| 创建故事线 | POST | /api/v1/story-lines | 创建故事线 |
| 更新故事线 | PUT | /api/v1/story-lines/{id} | 更新故事线 |
| 删除故事线 | DELETE | /api/v1/story-lines/{id} | 删除故事线 |
| 章节列表 | GET | /api/v1/story-lines/{id}/chapters | 获取章节列表 |
| 创建章节 | POST | /api/v1/story-lines/{id}/chapters | 创建章节 |
| 更新章节 | PUT | /api/v1/chapters/{id} | 更新章节 |
| 删除章节 | DELETE | /api/v1/chapters/{id} | 删除章节 |
| 章节排序 | PUT | /api/v1/chapters/sort | 调整章节顺序 |

#### 用户管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 用户列表 | GET | /api/v1/users | 分页查询用户列表 |
| 用户详情 | GET | /api/v1/users/{id} | 获取用户详情 |
| 用户日志 | GET | /api/v1/users/{id}/logs | 获取用户操作日志 |
| 用户轨迹 | GET | /api/v1/users/{id}/tracks | 获取用户位置轨迹 |
| 标记测试账号 | POST | /api/v1/users/{id}/test-mark | 标记为测试账号 |
| 取消测试标记 | DELETE | /api/v1/users/{id}/test-mark | 取消测试标记 |

#### 测试账号工具（核心功能）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 测试账号列表 | GET | /api/v1/test-accounts | 获取测试账号列表 |
| 设置模拟位置 | PUT | /api/v1/test-accounts/{id}/mock-location | 设置模拟位置 |
| 启用模拟定位 | PUT | /api/v1/test-accounts/{id}/mock-enable | 启用模拟定位 |
| 禁用模拟定位 | PUT | /api/v1/test-accounts/{id}/mock-disable | 禁用模拟定位 |
| 快速获得印章 | POST | /api/v1/test-accounts/{id}/stamps/grant | 快速获得印章 |
| 批量获得印章 | POST | /api/v1/test-accounts/{id}/stamps/grant-batch | 批量获得印章 |
| 删除印章 | DELETE | /api/v1/test-accounts/{id}/stamps/{stampId} | 删除指定印章 |
| 清空印章 | DELETE | /api/v1/test-accounts/{id}/stamps/clear | 清空所有印章 |
| 调整等级 | PUT | /api/v1/test-accounts/{id}/level | 调整用户等级 |
| 重置进度 | POST | /api/v1/test-accounts/{id}/reset | 重置所有进度 |
| 模拟触发 | POST | /api/v1/test-accounts/{id}/simulate-trigger | 模拟到达触发 |
| 批量操作 | POST | /api/v1/test-accounts/batch-operation | 批量操作 |
| 测试操作日志 | GET | /api/v1/test-accounts/{id}/logs | 获取测试操作日志 |

#### 多媒体资源管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 资源列表 | GET | /api/v1/media | 分页查询资源列表 |
| 上传资源 | POST | /api/v1/media/upload | 上传资源文件 |
| 删除资源 | DELETE | /api/v1/media/{id} | 删除资源 |
| 获取上传签名 | GET | /api/v1/media/upload-signature | 获取OSS直传签名 |

#### 数据统计

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 核心指标 | GET | /api/v1/statistics/core | 获取核心指标 |
| 用户趋势 | GET | /api/v1/statistics/user-trend | 用户增长趋势 |
| 活跃用户 | GET | /api/v1/statistics/active-users | 活跃用户统计 |
| 留存率 | GET | /api/v1/statistics/retention | 用户留存率 |
| 印章统计 | GET | /api/v1/statistics/stamps | 印章收集统计 |
| POI热度 | GET | /api/v1/statistics/poi-hot | POI热度排行 |
| 地理位置分布 | GET | /api/v1/statistics/geo-distribution | 地理位置分布 |

#### 系统管理

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录日志 | GET | /api/v1/logs/login | 查询登录日志 |
| 操作日志 | GET | /api/v1/logs/operation | 查询操作日志 |
| 配置列表 | GET | /api/v1/configs | 获取系统配置列表 |
| 更新配置 | PUT | /api/v1/configs/{key} | 更新系统配置 |

---

## 七、安全措施

### 7.1 认证鉴权

- **JWT Token**：采用 Access Token + Refresh Token 双Token机制
- **Token有效期**：Access Token 2小时，Refresh Token 7天
- **密码加密**：BCrypt算法（强度10）
- **登录失败锁定**：5次失败锁定30分钟
- **异地登录提醒**：检测异常IP登录

### 7.2 权限控制

- **RBAC模型**：基于角色的访问控制
- **数据权限**：支持按数据范围控制（全部/本部门/本人）
- **接口权限**：每个接口都有对应的权限编码
- **按钮权限**：前端按钮级权限控制

### 7.3 安全防护

- **HTTPS强制**：所有接口必须使用HTTPS
- **CORS配置**：只允许指定域名访问
- **XSS防护**：输入参数过滤，输出转义
- **SQL注入**：MyBatis参数化查询
- **CSRF防护**：Token验证
- **接口限流**：基于Token/IP的限流（100次/分钟）

### 7.4 审计日志

- **敏感操作记录**：所有数据修改操作记录前后值
- **登录日志**：记录IP、设备、时间、结果
- **异常监控**：登录失败、权限拒绝等异常告警

---

## 八、前端对接约定

### 8.1 接口规范

- **请求方式**：RESTful API
- **请求格式**：JSON
- **字段命名**：snake_case
- **时间格式**：ISO 8601 / RFC 3339
- **分页参数**：page_num, page_size
- **分页返回**：total, pages, list

### 8.2 错误码规范

| 错误码 | 描述 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证（Token无效或过期） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 8.3 前端开发建议

- **UI框架**：Ant Design Pro 5.x
- **路由**：React Router 6.x
- **状态管理**：Zustand
- **数据请求**：React Query / TanStack Query
- **构建工具**：Vite 4.x

---

## 九、部署与运维

### 9.1 环境配置

```yaml
# 开发环境配置
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aoxiaoyou_admin_dev?useUnicode=true&characterEncoding=utf8
    username: root
    password: root
  
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0

oss:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: aoxiaoyou

jwt:
  secret: dev-secret-key-do-not-use-in-production
  access-token-expire: 7200000  # 2小时
  refresh-token-expire: 604800000  # 7天
```

### 9.2 启动脚本

```bash
#!/bin/bash
# 启动脚本

# 1. 编译
mvn clean package -DskipTests

# 2. 启动
java -jar \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  aoxiaoyou-admin-start/target/aoxiaoyou-admin-start-1.0.0-SNAPSHOT.jar
```

---

*后台管理系统技术方案 V1.0 完成*

作者：後端工程師瑟希斯
日期：2026年4月

