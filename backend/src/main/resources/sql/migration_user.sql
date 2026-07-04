-- 增量迁移：新增 sys_user 表 + 普通用户管理权限节点
-- 适用场景：在已运行的环境中追加普通用户管理模块，无需重建数据库
SET NAMES utf8mb4;

-- 1. 创建 sys_user 表（前台普通用户，与 sys_admin 分表存储）
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `username`        VARCHAR(50)  NOT NULL                 COMMENT 'Login username',
    `password`        VARCHAR(100) NOT NULL                 COMMENT 'BCrypt hashed password',
    `nickname`        VARCHAR(50)           DEFAULT NULL     COMMENT 'Nickname',
    `email`           VARCHAR(100)          DEFAULT NULL     COMMENT 'Email address',
    `phone`           VARCHAR(20)           DEFAULT NULL     COMMENT 'Phone number',
    `gender`          TINYINT      NOT NULL DEFAULT 0       COMMENT 'Gender: 0=unknown, 1=male, 2=female',
    `avatar`          VARCHAR(255)          DEFAULT NULL     COMMENT 'Avatar URL',
    `status`          TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=active, 0=disabled',
    `last_login_time` DATETIME              DEFAULT NULL     COMMENT 'Last successful login time',
    `last_login_ip`   VARCHAR(50)           DEFAULT NULL     COMMENT 'Last successful login IP',
    `create_time`     DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time`     DATETIME              DEFAULT NULL     COMMENT 'Update time',
    `deleted`         TINYINT      NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Front-end regular user accounts';

-- 2. 插入普通用户管理权限节点（先清理避免重复执行报错）
DELETE FROM `sys_role_permission` WHERE `permission_id` IN (34, 35, 36, 37, 38);
DELETE FROM `sys_permission` WHERE `id` IN (34, 35, 36, 37, 38);

INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_code`, `perm_name`, `perm_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(34, 23, 'system:user',        '普通用户管理',   1, '/system/user',      'System/User',      'UserFilled',    11, 1, NOW(), NOW(), 0),
(35, 23, 'system:user:create', '新增用户',       2, NULL,                 NULL,               NULL,            12, 1, NOW(), NOW(), 0),
(36, 23, 'system:user:update', '编辑用户',       2, NULL,                 NULL,               NULL,            13, 1, NOW(), NOW(), 0),
(37, 23, 'system:user:delete', '删除用户',       2, NULL,                 NULL,               NULL,            14, 1, NOW(), NOW(), 0),
(38, 23, 'system:user:reset',  '重置用户密码',   2, NULL,                 NULL,               NULL,            15, 1, NOW(), NOW(), 0);

-- 3. 给 SUPER_ADMIN (role_id=1) 授权新权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 34), (1, 35), (1, 36), (1, 37), (1, 38);

-- 迁移完成
SELECT 'Migration completed: sys_user table + user management permissions added' AS result;
