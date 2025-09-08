# 动态路由系统说明

## 概述

本系统实现了真正的动态路由功能，路由配置完全从数据库中的菜单表生成，而不是硬编码在前端代码中。

## 工作原理

### 1. 数据库菜单表结构

```sql
sys_menu (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),        -- 菜单名称
    logo VARCHAR(100),       -- 菜单图标
    path VARCHAR(200),       -- 路由路径 (重要！)
    component VARCHAR(200),  -- 组件名称 (重要！)
    type INT,               -- 菜单类型：0=目录 1=页面 2=按钮
    perms VARCHAR(200),     -- 权限标识
    parent_id BIGINT        -- 父级菜单ID
)
```

### 2. 菜单类型说明

- **type = 0 (目录)**：仅用于菜单分组，不生成路由
- **type = 1 (页面)**：会生成前端路由，需要设置 `path` 和 `component`
- **type = 2 (按钮)**：用于按钮级权限控制，不生成路由

### 3. 动态路由生成流程

#### 后端流程：
1. 用户登录成功后，根据用户角色从数据库查询可访问的菜单
2. 构建菜单树结构，包含路由路径和组件信息
3. 通过 `/api/auth/userinfo` 接口返回给前端

#### 前端流程：
1. 用户信息获取成功后，调用 `addDynamicRoutes(userMenus)` 函数
2. 递归遍历菜单树，筛选 `type = 1` 的页面类型菜单
3. 根据菜单的 `path` 和 `component` 生成路由配置
4. 从组件映射表 `componentMap` 中获取实际的 Vue 组件
5. 动态添加路由到 Vue Router

### 4. 组件映射表

```javascript
const componentMap = {
  'User': () => import('@/views/User.vue'),
  'Role': () => import('@/views/Role.vue'),
  'Menu': () => import('@/views/Menu.vue'),
  'Dashboard': () => import('@/views/Dashboard.vue')
}
```

### 5. 示例数据

```sql
-- 系统管理目录
INSERT INTO sys_menu VALUES (1, '系统管理', 'Settings', NULL, NULL, 0, NULL, NULL);

-- 用户管理页面
INSERT INTO sys_menu VALUES (2, '用户管理', 'Person', '/user', 'user:list', 1, 'User', 1);

-- 角色管理页面  
INSERT INTO sys_menu VALUES (3, '角色管理', 'Shield', '/role', 'role:list', 1, 'Role', 1);

-- 菜单管理页面
INSERT INTO sys_menu VALUES (4, '菜单管理', 'Menu', '/menu', 'menu:list', 1, 'Menu', 1);
```

## 如何添加新页面

### 1. 创建 Vue 组件

```bash
# 在 ui/src/views/ 目录下创建新组件
touch ui/src/views/NewPage.vue
```

### 2. 更新组件映射表

```javascript
// ui/src/router/index.js
const componentMap = {
  'User': () => import('@/views/User.vue'),
  'Role': () => import('@/views/Role.vue'),
  'Menu': () => import('@/views/Menu.vue'),
  'NewPage': () => import('@/views/NewPage.vue'), // 新增
}
```

### 3. 在数据库中添加菜单记录

```sql
INSERT INTO sys_menu (name, logo, path, perms, type, component, parent_id) 
VALUES ('新页面', 'Document', '/newpage', 'newpage:list', 1, 'NewPage', 1);
```

### 4. 为角色分配菜单权限

```sql
-- 为角色ID=1添加新菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, [新菜单的ID]);
```

## 权限控制

1. **路由级权限**：通过用户角色关联的菜单控制可访问的路由
2. **菜单显示权限**：侧边栏菜单根据用户权限动态生成
3. **按钮级权限**：通过 `type = 2` 的按钮菜单控制页面内按钮显示

## 优势

1. **完全动态**：无需修改前端代码即可添加新页面和路由
2. **权限精确**：基于数据库的细粒度权限控制
3. **易于维护**：路由配置集中在数据库中管理
4. **扩展性强**：支持多级菜单和复杂的权限结构

## 注意事项

1. 新增页面时必须更新前端的组件映射表
2. 菜单的 `component` 字段必须与组件映射表中的 key 一致
3. 页面类型菜单（type=1）必须设置 `path` 和 `component` 字段
4. 路由路径 `path` 应该保持唯一性