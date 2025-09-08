注意：本仓库的代码目前 90% 以上的代码由ai生成，风险自己把控。
包括这个README 除了前两行，剩余部分也是ai写的

# 管理员后台系统

基于Vue 3 + Naive UI + Spring Boot + Jimmer + Sa-Token的后台管理系统。

## 技术栈

### 后端
- Spring Boot 3.5.4
- Kotlin 2.2.0
- Jimmer 0.9.99 (ORM框架)
- Sa-Token 1.38.0 (权限框架)
- MySQL 8.0+

### 前端
- Vue 3.5.18
- Naive UI 2.42.0
- Vite 7.0.6
- Axios 1.11.0
- Pinia 3.0.3

## 功能特性

- 🔐 **登录认证**: 基于Sa-Token的JWT认证
- 👥 **用户管理**: 用户的增删改查，密码加密
- 🛡️ **角色管理**: 角色权限配置，支持多角色
- 📱 **菜单管理**: 动态菜单配置，支持树形结构
- 🎨 **权限控制**: 基于角色的权限控制，动态路由
- 📋 **Tab栏**: 类似浏览器的标签页功能
- 📱 **响应式**: 支持桌面端和移动端

## 快速开始

### 1. 环境要求

- JDK 21+
- Node.js 20.19.0+
- MySQL 8.0+

### 2. 数据库准备

```sql
-- 执行 sql/init.sql 文件初始化数据库
mysql -u root -p < sql/init.sql
```

### 3. 后端启动

```bash
# 修改 src/main/resources/application.yml 中的数据库配置
# 启动后端服务
./gradlew bootRun
```

### 4. 前端启动

```bash
cd ui
yarn install
yarn dev
```

### 5. 访问系统

- 前端地址: http://localhost:2333
- 后端API: http://localhost:12346/api
- 默认账号: admin / admin123

## 项目结构

```
matchmaker-admin-kt/
├── src/main/kotlin/me/inory/
│   ├── framework/          # 框架配置
│   │   ├── config/         # 配置类
│   │   ├── exception/      # 异常处理
│   │   └── ext/           # 扩展函数
│   ├── orm/               # ORM相关
│   ├── project/           # 业务代码
│   │   ├── controller/    # 控制器
│   │   ├── dto/          # 数据传输对象
│   │   ├── model/        # 实体模型
│   │   └── service/      # 业务服务
│   └── util/             # 工具类
├── ui/                   # 前端项目
│   ├── src/
│   │   ├── api/         # API接口
│   │   ├── layout/      # 布局组件
│   │   ├── router/      # 路由配置
│   │   ├── stores/      # 状态管理
│   │   ├── utils/       # 工具函数
│   │   └── views/       # 页面组件
│   └── public/
└── sql/                 # 数据库脚本
```

## 核心功能

### 1. 登录认证
- JWT Token认证
- 密码BCrypt加密
- 自动token刷新
- 登录状态持久化

### 2. 权限管理
- 基于角色的权限控制(RBAC)
- 动态路由加载
- 菜单权限过滤
- 按钮级权限控制

### 3. 用户管理
- 用户基本信息管理
- 角色分配
- 密码重置
- 用户状态管理

### 4. 角色管理
- 角色创建/编辑/删除
- 菜单权限分配
- 角色成员管理

### 5. 菜单管理
- 树形菜单结构
- 菜单类型：目录/页面/按钮
- 权限标识配置
- 路由路径配置

## API接口

### 认证相关
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/userinfo` - 获取用户信息
- `POST /api/auth/logout` - 用户退出

### 用户管理
- `GET /api/user` - 获取用户列表
- `POST /api/user` - 创建用户
- `PUT /api/user` - 更新用户
- `DELETE /api/user/{id}` - 删除用户

### 角色管理
- `GET /api/role` - 获取角色列表
- `POST /api/role` - 创建角色
- `PUT /api/role` - 更新角色
- `DELETE /api/role/{id}` - 删除角色

### 菜单管理
- `GET /api/menu` - 获取菜单列表
- `GET /api/menu/tree` - 获取菜单树
- `POST /api/menu` - 创建菜单
- `PUT /api/menu` - 更新菜单
- `DELETE /api/menu/{id}` - 删除菜单

## 开发说明

### 后端开发
1. 使用Jimmer作为ORM框架，支持强类型查询
2. 使用Sa-Token进行权限控制
3. 继承BaseController获得统一的响应处理
4. 使用JimmerExt扩展进行分页查询

### 前端开发
1. 使用Naive UI组件库
2. 使用Pinia进行状态管理
3. 使用Vue Router进行路由管理
4. 使用Axios进行HTTP请求

## 部署说明

### 生产环境配置
1. 修改数据库连接配置
2. 配置Redis（如需要）
3. 配置日志级别
4. 配置JWT密钥

### Docker部署
```bash
# 构建后端镜像
./gradlew bootBuildImage

# 构建前端镜像
cd ui && docker build -t ui .
```

## 许可证

MIT License