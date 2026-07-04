-- ============================================================================
-- RelicAdmin - Cultural Heritage Admin Panel
-- MySQL Database Initialization Script
-- Database: relic_admin
-- Charset: utf8mb4
-- ============================================================================

-- 确保客户端连接使用 utf8mb4 字符集，避免中文乱码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. Create Database
-- ----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS relic_admin
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE relic_admin;

-- ----------------------------------------------------------------------------
-- 2. Drop Existing Tables (in dependency order, for re-runnable script)
-- ----------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_log`;
DROP TABLE IF EXISTS `backup_record`;
DROP TABLE IF EXISTS `knowledge_graph`;
DROP TABLE IF EXISTS `artifact`;
DROP TABLE IF EXISTS `sys_role_permission`;
DROP TABLE IF EXISTS `sys_permission`;
DROP TABLE IF EXISTS `sys_admin`;
DROP TABLE IF EXISTS `sys_role`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 3. Table Definitions
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 3.1 sys_role - Roles
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `role_code`   VARCHAR(50)  NOT NULL                 COMMENT 'Role code, e.g. SUPER_ADMIN, AUDITOR, DATA_ADMIN',
    `role_name`   VARCHAR(50)  NOT NULL                 COMMENT 'Role display name',
    `description` VARCHAR(200)          DEFAULT NULL     COMMENT 'Role description',
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=active, 0=disabled',
    `create_time` DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time` DATETIME              DEFAULT NULL     COMMENT 'Update time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

-- ----------------------------------------------------------------------------
-- 3.2 sys_admin - Admin accounts (separate from front-end users)
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_admin` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `username`        VARCHAR(50)  NOT NULL                 COMMENT 'Login username',
    `password`        VARCHAR(100) NOT NULL                 COMMENT 'BCrypt hashed password',
    `real_name`       VARCHAR(50)           DEFAULT NULL     COMMENT 'Real name',
    `email`           VARCHAR(100)          DEFAULT NULL     COMMENT 'Email address',
    `phone`           VARCHAR(20)           DEFAULT NULL     COMMENT 'Phone number',
    `avatar`          VARCHAR(255)          DEFAULT NULL     COMMENT 'Avatar URL',
    `role_id`         BIGINT       NOT NULL                 COMMENT 'Role ID, references sys_role(id)',
    `status`          TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=active, 0=disabled',
    `last_login_time` DATETIME              DEFAULT NULL     COMMENT 'Last successful login time',
    `last_login_ip`   VARCHAR(50)           DEFAULT NULL     COMMENT 'Last successful login IP',
    `create_time`     DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time`     DATETIME              DEFAULT NULL     COMMENT 'Update time',
    `deleted`         TINYINT      NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Admin accounts';

-- ----------------------------------------------------------------------------
-- 3.2.1 sys_user - Front-end regular user accounts (separate from sys_admin)
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_user` (
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

-- ----------------------------------------------------------------------------
-- 3.3 sys_permission - Permissions (menu + button level)
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_permission` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0       COMMENT 'Parent permission ID, 0=root',
    `perm_code`   VARCHAR(100) NOT NULL                 COMMENT 'Permission code, e.g. artifact:list, artifact:create',
    `perm_name`   VARCHAR(50)  NOT NULL                 COMMENT 'Permission display name',
    `perm_type`   TINYINT      NOT NULL                 COMMENT 'Permission type: 1=menu, 2=button',
    `path`        VARCHAR(200)          DEFAULT NULL     COMMENT 'Frontend route path',
    `component`   VARCHAR(200)          DEFAULT NULL     COMMENT 'Frontend component path',
    `icon`        VARCHAR(50)           DEFAULT NULL     COMMENT 'Menu icon',
    `sort_order`  INT          NOT NULL DEFAULT 0       COMMENT 'Sort order, ascending',
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=active, 0=disabled',
    `create_time` DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time` DATETIME              DEFAULT NULL     COMMENT 'Update time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_perm_type` (`perm_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System permissions (menu + button)';

-- ----------------------------------------------------------------------------
-- 3.4 sys_role_permission - Role-Permission mapping
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `role_id`       BIGINT NOT NULL                 COMMENT 'Role ID',
    `permission_id` BIGINT NOT NULL                 COMMENT 'Permission ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-permission mapping';

-- ----------------------------------------------------------------------------
-- 3.5 artifact - Cultural relics data
-- ----------------------------------------------------------------------------
CREATE TABLE `artifact` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `name`        VARCHAR(100)  NOT NULL                 COMMENT 'Artifact name (文物名称)',
    `era`         VARCHAR(50)            DEFAULT NULL     COMMENT 'Era / dynasty (年代)',
    `category`    VARCHAR(50)            DEFAULT NULL     COMMENT 'Category (类别)',
    `description` TEXT                                  COMMENT 'Description (描述)',
    `image_url`   VARCHAR(500)           DEFAULT NULL     COMMENT 'Main image URL (OSS)',
    `images`      VARCHAR(2000)          DEFAULT NULL     COMMENT 'Multiple image URLs, JSON array string',
    `status`      TINYINT       NOT NULL DEFAULT 1       COMMENT 'Status: 1=normal, 0=hidden',
    `create_time` DATETIME               DEFAULT NULL     COMMENT 'Create time',
    `update_time` DATETIME               DEFAULT NULL     COMMENT 'Update time',
    `deleted`     TINYINT       NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_era` (`era`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cultural relics data';

-- ----------------------------------------------------------------------------
-- 3.6 knowledge_graph - Knowledge graph triples (simplified, MySQL-based)
-- ----------------------------------------------------------------------------
CREATE TABLE `knowledge_graph` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `subject_entity` VARCHAR(100) NOT NULL                 COMMENT 'Subject entity (主体实体)',
    `relation`       VARCHAR(100) NOT NULL                 COMMENT 'Relation (关系)',
    `object_entity`  VARCHAR(100) NOT NULL                 COMMENT 'Object entity (客体实体)',
    `artifact_id`    BIGINT                DEFAULT NULL     COMMENT 'Related artifact ID',
    `create_time`    DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time`    DATETIME              DEFAULT NULL     COMMENT 'Update time',
    `deleted`        TINYINT      NOT NULL DEFAULT 0       COMMENT 'Logical delete: 0=not deleted, 1=deleted',
    PRIMARY KEY (`id`),
    KEY `idx_subject_entity` (`subject_entity`),
    KEY `idx_object_entity` (`object_entity`),
    KEY `idx_relation` (`relation`),
    KEY `idx_artifact_id` (`artifact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Knowledge graph triples';

-- ----------------------------------------------------------------------------
-- 3.7 backup_record - Backup records
-- ----------------------------------------------------------------------------
CREATE TABLE `backup_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `filename`      VARCHAR(200) NOT NULL                 COMMENT 'Backup file name',
    `file_path`     VARCHAR(500) NOT NULL                 COMMENT 'Backup file absolute path',
    `file_size`     BIGINT                DEFAULT NULL     COMMENT 'File size in bytes',
    `backup_type`   VARCHAR(20)           DEFAULT NULL     COMMENT 'Backup type, e.g. FULL',
    `is_encrypted`  TINYINT      NOT NULL DEFAULT 0       COMMENT 'Encrypted: 0=no, 1=yes',
    `operator_id`   BIGINT                DEFAULT NULL     COMMENT 'Admin ID who triggered the backup',
    `operator_name` VARCHAR(50)           DEFAULT NULL     COMMENT 'Admin name who triggered the backup',
    `status`        TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=success, 0=failed',
    `remark`        VARCHAR(500)          DEFAULT NULL     COMMENT 'Remark',
    `create_time`   DATETIME              DEFAULT NULL     COMMENT 'Create time',
    `update_time`   DATETIME              DEFAULT NULL     COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY `idx_backup_type` (`backup_type`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Backup records';

-- ----------------------------------------------------------------------------
-- 3.8 sys_log - Unified log table (operation + system + security)
-- ----------------------------------------------------------------------------
CREATE TABLE `sys_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary key',
    `log_type`        VARCHAR(20)  NOT NULL                 COMMENT 'Log type: OPERATION, SYSTEM, SECURITY',
    `operator_id`     BIGINT                DEFAULT NULL     COMMENT 'Operator admin ID',
    `operator_name`   VARCHAR(50)           DEFAULT NULL     COMMENT 'Operator admin name',
    `operation_type`  VARCHAR(20)           DEFAULT NULL     COMMENT 'Operation type: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, BACKUP, RESTORE, etc.',
    `operation_target` VARCHAR(100)         DEFAULT NULL     COMMENT 'Operation target: table or module name',
    `method`          VARCHAR(200)          DEFAULT NULL     COMMENT 'Java method name',
    `request_url`     VARCHAR(500)          DEFAULT NULL     COMMENT 'Request URL',
    `request_method`  VARCHAR(10)           DEFAULT NULL     COMMENT 'HTTP method: GET, POST, PUT, DELETE',
    `request_params`  TEXT                                     COMMENT 'Request parameters',
    `before_data`     TEXT                                     COMMENT 'Data before operation, JSON',
    `after_data`      TEXT                                     COMMENT 'Data after operation, JSON',
    `ip`              VARCHAR(50)           DEFAULT NULL     COMMENT 'Request IP',
    `status`          TINYINT      NOT NULL DEFAULT 1       COMMENT 'Status: 1=success, 0=error',
    `error_msg`       TEXT                                     COMMENT 'Error message if failed',
    `cost_time`       BIGINT                DEFAULT NULL     COMMENT 'Cost time in milliseconds',
    `create_time`     DATETIME              DEFAULT NULL     COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Unified system log (operation + system + security)';

-- ============================================================================
-- 4. Initial Data
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 4.1 Roles
-- ----------------------------------------------------------------------------
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '全部操作权限',                       1, NOW(), NOW(), 0),
(2, 'AUDITOR',     '内容审核员', '仅内容审核权限',                     1, NOW(), NOW(), 0),
(3, 'DATA_ADMIN',  '数据管理员', '文物数据增删改查权限',               1, NOW(), NOW(), 0);

-- ----------------------------------------------------------------------------
-- 4.2 Admins
-- NOTE: The password column stores BCrypt hashes. The placeholder hash below
--       ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
--       is a valid BCrypt-formatted string used only to satisfy the NOT NULL
--       constraint and column length. The application's DataInitializer will
--       re-hash the real passwords (Admin@123, Audi@123, Data@123) on first
--       startup, replacing these placeholder values with correct hashes.
-- ----------------------------------------------------------------------------
INSERT INTO `sys_admin` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `avatar`, `role_id`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'admin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', 'admin@relicadmin.cn',     '13800000001', NULL, 1, 1, NOW(), NOW(), 0),
(2, 'auditor',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '内容审核员', 'auditor@relicadmin.cn',   '13800000002', NULL, 2, 1, NOW(), NOW(), 0),
(3, 'dataadmin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '数据管理员', 'dataadmin@relicadmin.cn', '13800000003', NULL, 3, 1, NOW(), NOW(), 0);

-- ----------------------------------------------------------------------------
-- 4.3 Permissions (menu tree)
-- perm_type: 1=menu, 2=button
-- ----------------------------------------------------------------------------
INSERT INTO `sys_permission` (`id`, `parent_id`, `perm_code`, `perm_name`, `perm_type`, `path`, `component`, `icon`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`) VALUES
-- 1. Dashboard
(1,  0,  'dashboard',          '仪表盘',         1, '/dashboard',         'Dashboard/Index',  'Odometer',      1, 1, NOW(), NOW(), 0),

-- 2. Artifact Management
(2,  0,  'artifact',           '文物管理',       1, '/artifact',          'Layout',           'Picture',       2, 1, NOW(), NOW(), 0),
(3,  2,  'artifact:list',      '文物列表',       1, '/artifact/list',     'Artifact/List',    'List',          1, 1, NOW(), NOW(), 0),
(4,  2,  'artifact:create',    '新增文物',       2, NULL,                 NULL,               NULL,            2, 1, NOW(), NOW(), 0),
(5,  2,  'artifact:update',    '编辑文物',       2, NULL,                 NULL,               NULL,            3, 1, NOW(), NOW(), 0),
(6,  2,  'artifact:delete',    '删除文物',       2, NULL,                 NULL,               NULL,            4, 1, NOW(), NOW(), 0),
(7,  2,  'artifact:import',    '导入文物',       2, NULL,                 NULL,               NULL,            5, 1, NOW(), NOW(), 0),
(8,  2,  'artifact:export',    '导出文物',       2, NULL,                 NULL,               NULL,            6, 1, NOW(), NOW(), 0),

-- 3. Knowledge Graph
(9,  0,  'knowledge',          '知识图谱',       1, '/knowledge',         'Layout',           'Share',         3, 1, NOW(), NOW(), 0),
(10, 9,  'knowledge:list',     '三元组列表',     1, '/knowledge/list',    'Knowledge/List',   NULL,            1, 1, NOW(), NOW(), 0),
(11, 9,  'knowledge:create',   '新增三元组',     2, NULL,                 NULL,               NULL,            2, 1, NOW(), NOW(), 0),
(12, 9,  'knowledge:update',   '编辑三元组',     2, NULL,                 NULL,               NULL,            3, 1, NOW(), NOW(), 0),
(13, 9,  'knowledge:delete',   '删除三元组',     2, NULL,                 NULL,               NULL,            4, 1, NOW(), NOW(), 0),

-- 4. Backup Management
(14, 0,  'backup',             '备份管理',       1, '/backup',            'Layout',           'FolderOpened',  4, 1, NOW(), NOW(), 0),
(15, 14, 'backup:list',        '备份列表',       1, '/backup/list',       'Backup/List',      NULL,            1, 1, NOW(), NOW(), 0),
(16, 14, 'backup:create',      '创建备份',       2, NULL,                 NULL,               NULL,            2, 1, NOW(), NOW(), 0),
(17, 14, 'backup:restore',     '恢复备份',       2, NULL,                 NULL,               NULL,            3, 1, NOW(), NOW(), 0),
(18, 14, 'backup:download',    '下载备份',       2, NULL,                 NULL,               NULL,            4, 1, NOW(), NOW(), 0),

-- 5. Log Management
(19, 0,  'log',                '日志管理',       1, '/log',               'Layout',           'Document',      5, 1, NOW(), NOW(), 0),
(20, 19, 'log:operation',      '操作日志',       1, '/log/operation',     'Log/Operation',    NULL,            1, 1, NOW(), NOW(), 0),
(21, 19, 'log:security',       '安全日志',       1, '/log/security',      'Log/Security',     NULL,            2, 1, NOW(), NOW(), 0),
(22, 19, 'log:system',         '系统日志',       1, '/log/system',        'Log/System',       NULL,            3, 1, NOW(), NOW(), 0),

-- 6. System Management
(23, 0,  'system',             '系统管理',       1, '/system',            'Layout',           'Setting',       6, 1, NOW(), NOW(), 0),
(24, 23, 'system:admin',       '管理员管理',     1, '/system/admin',      'System/Admin',     NULL,            1, 1, NOW(), NOW(), 0),
(25, 23, 'system:admin:create','新增管理员',     2, NULL,                 NULL,               NULL,            2, 1, NOW(), NOW(), 0),
(26, 23, 'system:admin:update','编辑管理员',     2, NULL,                 NULL,               NULL,            3, 1, NOW(), NOW(), 0),
(27, 23, 'system:admin:delete','删除管理员',     2, NULL,                 NULL,               NULL,            4, 1, NOW(), NOW(), 0),
(28, 23, 'system:admin:reset', '重置密码',       2, NULL,                 NULL,               NULL,            5, 1, NOW(), NOW(), 0),
(29, 23, 'system:role',        '角色管理',       1, '/system/role',       'System/Role',      NULL,            6, 1, NOW(), NOW(), 0),
(30, 23, 'system:role:create', '新增角色',       2, NULL,                 NULL,               NULL,            7, 1, NOW(), NOW(), 0),
(31, 23, 'system:role:update', '编辑角色',       2, NULL,                 NULL,               NULL,            8, 1, NOW(), NOW(), 0),
(32, 23, 'system:role:delete', '删除角色',       2, NULL,                 NULL,               NULL,            9, 1, NOW(), NOW(), 0),
(33, 23, 'system:role:perm',   '角色授权',       2, NULL,                 NULL,               NULL,            10, 1, NOW(), NOW(), 0),
-- 7. Regular User Management (under System Management)
(34, 23, 'system:user',        '普通用户管理',   1, '/system/user',      'System/User',      'UserFilled',    11, 1, NOW(), NOW(), 0),
(35, 23, 'system:user:create', '新增用户',       2, NULL,                 NULL,               NULL,            12, 1, NOW(), NOW(), 0),
(36, 23, 'system:user:update', '编辑用户',       2, NULL,                 NULL,               NULL,            13, 1, NOW(), NOW(), 0),
(37, 23, 'system:user:delete', '删除用户',       2, NULL,                 NULL,               NULL,            14, 1, NOW(), NOW(), 0),
(38, 23, 'system:user:reset',  '重置用户密码',   2, NULL,                 NULL,               NULL,            15, 1, NOW(), NOW(), 0);

-- ----------------------------------------------------------------------------
-- 4.4 Role-Permission Mapping
-- ----------------------------------------------------------------------------

-- Role 1 (SUPER_ADMIN): ALL permissions (1 - 38)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13),
(1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
(1, 19), (1, 20), (1, 21), (1, 22),
(1, 23), (1, 24), (1, 25), (1, 26), (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32), (1, 33),
(1, 34), (1, 35), (1, 36), (1, 37), (1, 38);

-- Role 2 (AUDITOR): dashboard, artifact(list), knowledge(list), log(operation, security)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1),    -- dashboard
(2, 2),    -- artifact (parent menu)
(2, 3),    -- artifact:list
(2, 9),    -- knowledge (parent menu)
(2, 10),   -- knowledge:list
(2, 19),   -- log (parent menu)
(2, 20),   -- log:operation
(2, 21);   -- log:security

-- Role 3 (DATA_ADMIN): dashboard, artifact(all), knowledge(all), backup(list, download)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 1),    -- dashboard
(3, 2),    -- artifact (parent menu)
(3, 3),    -- artifact:list
(3, 4),    -- artifact:create
(3, 5),    -- artifact:update
(3, 6),    -- artifact:delete
(3, 7),    -- artifact:import
(3, 8),    -- artifact:export
(3, 9),    -- knowledge (parent menu)
(3, 10),   -- knowledge:list
(3, 11),   -- knowledge:create
(3, 12),   -- knowledge:update
(3, 13),   -- knowledge:delete
(3, 14),   -- backup (parent menu)
(3, 15),   -- backup:list
(3, 18);   -- backup:download

-- ----------------------------------------------------------------------------
-- 4.5 Artifact Test Data (15 cultural relics)
-- ----------------------------------------------------------------------------
INSERT INTO `artifact` (`id`, `name`, `era`, `category`, `description`, `image_url`, `images`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, '青铜鼎', '商代', '青铜器',
   '商代晚期青铜礼器，三足两耳，器形厚重，纹饰以饕餮纹为主，是商代贵族祭祀与权力象征的重要礼器，具有较高的历史与艺术价值。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20ding%20tripod%20shang%20dynasty&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20ding%20side%20view&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20ding%20pattern%20detail&image_size=square"]',
   1, NOW(), NOW(), 0),

(2, '唐三彩马', '唐代', '陶瓷',
   '唐代三彩釉陶马，造型雄健，釉色以黄、绿、白为主，是唐三彩代表作之一，反映了唐代社会繁荣与中西文化交流的盛况。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20sancai%20three%20color%20horse&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20sancai%20horse%20front&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20sancai%20horse%20glaze%20detail&image_size=square"]',
   1, NOW(), NOW(), 0),

(3, '宋代青瓷碗', '宋代', '陶瓷',
   '宋代青瓷碗，釉色温润如玉，器形简洁典雅，代表了宋代极简美学与制瓷工艺的巅峰水平，为宋代五大名窑之一的产品。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=song%20celadon%20green%20bowl&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=celadon%20bowl%20interior&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=celadon%20bowl%20glaze%20close%20up&image_size=square"]',
   1, NOW(), NOW(), 0),

(4, '明代青花瓷瓶', '明代', '陶瓷',
   '明代青花瓷瓶，胎质细腻，釉色莹润，青花纹饰层次分明，绘制山水人物，是明代景德镇官窑代表作品，具有极高的艺术与收藏价值。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ming%20blue%20and%20white%20porcelain%20vase&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ming%20vase%20pattern&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ming%20vase%20base&image_size=square"]',
   1, NOW(), NOW(), 0),

(5, '战国玉璧', '战国', '玉器',
   '战国时期玉璧，玉质温润，色泽青绿，璧面雕琢谷纹排列规整，是战国礼玉与佩玉的重要代表，体现了当时高超的琢玉工艺。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=warring%20states%20jade%20bi%20disc&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=jade%20bi%20pattern&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=jade%20bi%20edge&image_size=square"]',
   1, NOW(), NOW(), 0),

(6, '汉代陶俑', '汉代', '陶器',
   '汉代彩绘陶俑，人物形象生动写实，服饰细节清晰可辨，反映了汉代社会生活与丧葬习俗，是研究汉代社会的重要实物资料。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=han%20dynasty%20pottery%20figurine&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=han%20figurine%20front&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=han%20figurine%20back&image_size=square"]',
   1, NOW(), NOW(), 0),

(7, '清代翡翠如意', '清代', '玉器',
   '清代翡翠如意，质地通透，色泽翠绿，雕琢云纹与灵芝纹，寓意吉祥如意，是清代宫廷陈设与赏赐之佳品。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=qing%20jadeite%20ruyi%20scepter&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=jadeite%20ruyi%20head&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=jadeite%20ruyi%20handle&image_size=square"]',
   1, NOW(), NOW(), 0),

(8, '元代青花大盘', '元代', '陶瓷',
   '元代青花大盘，口径较大，盘心绘缠枝牡丹纹，构图饱满，青料发色浓艳，是元代外销瓷的典型器物，具有重要的历史与艺术价值。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=yuan%20blue%20and%20white%20large%20plate&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=yuan%20plate%20center&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=yuan%20plate%20rim&image_size=square"]',
   1, NOW(), NOW(), 0),

(9, '商代甲骨文', '商代', '文献',
   '商代甲骨文，刻于龟甲兽骨之上，内容多为占卜记录，是中国已知最早的成熟文字系统，对研究商代历史与汉字起源具有不可替代的价值。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=shang%20oracle%20bone%20inscription&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=oracle%20bone%20front&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=oracle%20bone%20inscription%20close%20up&image_size=square"]',
   1, NOW(), NOW(), 0),

(10, '唐代金银器', '唐代', '金银器',
   '唐代金银器，捶揲錾刻工艺精湛，造型多样，纹饰以花卉、飞禽为主，反映了唐代金银器制作的繁荣与中西文化的交融。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20gold%20silver%20ware&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20silver%20cup&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tang%20gold%20bowl%20pattern&image_size=square"]',
   1, NOW(), NOW(), 0),

(11, '宋代书画卷轴', '宋代', '书画',
   '宋代书画卷轴，笔墨精妙，气韵生动，山水意境深远，代表了中国文人画的最高成就之一，具有极高的艺术与文献价值。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=song%20dynasty%20painting%20scroll&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=song%20painting%20landscape&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=song%20calligraphy%20detail&image_size=square"]',
   1, NOW(), NOW(), 0),

(12, '明代掐丝珐琅', '明代', '珐琅器',
   '明代掐丝珐琅器，又称景泰蓝，以铜胎掐丝填釉烧制而成，色彩绚丽，纹饰繁复，是明代宫廷工艺的代表之作。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ming%20cloisonne%20enamel%20ware&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cloisonne%20pattern%20detail&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cloisonne%20vessel%20side&image_size=square"]',
   1, NOW(), NOW(), 0),

(13, '战国青铜剑', '战国', '青铜器',
   '战国青铜剑，剑身修长，刃部锋利，剑格与剑首装饰精美，合金配比科学，历经两千余年仍寒光逼人，是战国兵器之精品。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=warring%20states%20bronze%20sword&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20sword%20blade&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20sword%20hilt&image_size=square"]',
   1, NOW(), NOW(), 0),

(14, '汉代铜镜', '汉代', '青铜器',
   '汉代铜镜，镜面光洁，背面铸有四神、瑞兽与铭文，纹饰精美，寓意吉祥，是汉代日常生活与铜镜铸造工艺的重要见证。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=han%20bronze%20mirror&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20mirror%20back%20pattern&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bronze%20mirror%20inscription&image_size=square"]',
   1, NOW(), NOW(), 0),

(15, '清代紫砂壶', '清代', '陶瓷',
   '清代紫砂壶，泥料细腻，造型古朴典雅，壶身刻有诗文铭款，集实用与艺术于一体，是清代宜兴紫砂工艺的代表作品。',
   'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=qing%20zisha%20teapot&image_size=square',
   '["https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=zisha%20teapot%20side&image_size=square","https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=zisha%20teapot%20inscription&image_size=square"]',
   1, NOW(), NOW(), 0);

-- ============================================================================
-- 5. End of Script
-- ============================================================================
