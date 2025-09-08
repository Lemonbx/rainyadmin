

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) COMMENT '昵称',
    loginname VARCHAR(50) UNIQUE COMMENT '登录名',
    password VARCHAR(255) COMMENT '密码',
    salt VARCHAR(255) COMMENT '盐值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID'
) COMMENT '用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) COMMENT '角色名称',
    `key` VARCHAR(50) UNIQUE COMMENT '角色标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID'
) COMMENT '角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) COMMENT '菜单名称',
    logo VARCHAR(100) COMMENT '菜单图标',
    path VARCHAR(200) COMMENT '路由路径',
    perms VARCHAR(200) COMMENT '权限标识',
    type INT DEFAULT 0 COMMENT '菜单类型：0目录 1页面 2按钮',
    component VARCHAR(200) COMMENT '组件路径',
    sort INT DEFAULT 0 COMMENT '排序',
    parent_id BIGINT COMMENT '父级菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    FOREIGN KEY (parent_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) COMMENT '菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) COMMENT '用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT,
    menu_id BIGINT,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) COMMENT '角色菜单关联表';

-- 初始化数据

-- 插入默认菜单
INSERT INTO sys_menu (id, name, logo, path, perms, type, component, parent_id) VALUES
(1, '系统管理', 'Settings', NULL, NULL, 0, NULL, NULL),
(2, '用户管理', 'Person', '/user', 'user:list', 1, 'User', 1),
(3, '角色管理', 'Shield', '/role', 'role:list', 1, 'Role', 1),
(4, '菜单管理', 'Menu', '/menu', 'menu:list', 1, 'Menu', 1),
(5, '新增用户', NULL, NULL, 'user:create', 2, NULL, 2),
(6, '编辑用户', NULL, NULL, 'user:update', 2, NULL, 2),
(7, '删除用户', NULL, NULL, 'user:delete', 2, NULL, 2),
(8, '新增角色', NULL, NULL, 'role:create', 2, NULL, 3),
(9, '编辑角色', NULL, NULL, 'role:update', 2, NULL, 3),
(10, '删除角色', NULL, NULL, 'role:delete', 2, NULL, 3),
(11, '新增菜单', NULL, NULL, 'menu:create', 2, NULL, 4),
(12, '编辑菜单', NULL, NULL, 'menu:update', 2, NULL, 4),
(13, '删除菜单', NULL, NULL, 'menu:delete', 2, NULL, 4);

-- 插入默认角色
INSERT INTO sys_role (id, name, `key`) VALUES
(1, '超级管理员', 'admin'),
(2, '普通用户', 'user');

-- 插入默认用户 (密码: admin123)
INSERT INTO sys_user (id, nickname, loginname, password, salt) VALUES
(1, '管理员', 'admin', '$2a$10$GgQqrzm/Ss9NOwCVmGAyl.fsXrC76ygvj4KVI/IZDHXMVgKKrLgWG', 'abcdef');

-- 关联超级管理员角色到所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13);

-- 关联普通用户角色到基本菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2);

-- 关联用户到角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1);