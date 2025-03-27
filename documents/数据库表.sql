
-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码（加密存储）',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（1：正常，0：禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（1：正常，0：禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` bigint(20) DEFAULT NULL COMMENT '父权限ID',
    `name` varchar(50) NOT NULL COMMENT '权限名称',
    `code` varchar(50) NOT NULL COMMENT '权限编码',
    `type` tinyint(1) NOT NULL COMMENT '类型（1：菜单，2：按钮）',
    `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
    `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
    `icon` varchar(50) DEFAULT NULL COMMENT '图标',
    `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（1：正常，0：禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 会议室表
CREATE TABLE IF NOT EXISTS `room` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '会议室名称',
    `location` varchar(200) NOT NULL COMMENT '位置',
    `capacity` int(11) NOT NULL COMMENT '容纳人数',
    `equipment` varchar(500) DEFAULT NULL COMMENT '设备配置（JSON格式）',
    `description` text DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（1：可用，0：维护中）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- 预约表
CREATE TABLE IF NOT EXISTS `booking` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_id` bigint(20) NOT NULL COMMENT '会议室ID',
    `user_id` bigint(20) NOT NULL COMMENT '预约用户ID',
    `title` varchar(200) NOT NULL COMMENT '会议主题',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    `attendees` varchar(500) DEFAULT NULL COMMENT '参会人员（JSON格式）',
    `description` text DEFAULT NULL COMMENT '会议描述',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态（0：待审批，1：已通过，2：已拒绝，3：已取消）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 审批表
CREATE TABLE IF NOT EXISTS `approval` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `booking_id` bigint(20) NOT NULL COMMENT '预约ID',
    `approver_id` bigint(20) DEFAULT NULL COMMENT '审批人ID',
    `status` tinyint(1) NOT NULL COMMENT '审批状态（1：通过，2：拒绝）',
    `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_booking_id` (`booking_id`),
    KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` text NOT NULL COMMENT '通知内容',
    `type` tinyint(1) NOT NULL COMMENT '通知类型（1：预约状态变更，2：预约提醒，3：系统公告）',
    `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读（0：未读，1：已读）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` varchar(50) NOT NULL COMMENT '配置键',
    `config_value` text NOT NULL COMMENT '配置值',
    `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '操作用户ID',
    `module` varchar(50) NOT NULL COMMENT '操作模块',
    `operation` varchar(50) NOT NULL COMMENT '操作类型',
    `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
    `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_operation` (`operation`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';





-- 1. 初始化角色表
INSERT INTO role (id, role_name, role_code, description, status, create_time, update_time, is_deleted) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员', 1, NOW(), NOW(), 0),
(2, '普通用户', 'USER', '普通用户', 1, NOW(), NOW(), 0);

-- 2. 初始化权限表
INSERT INTO permission (id, parent_id, name, code, type, path, sort, create_time, update_time, is_deleted) VALUES
-- 系统管理
(1, 0, '系统管理', 'system', 1, '/system', 1, NOW(), NOW(), 0),
(2, 1, '用户管理', 'system:user', 1, '/system/user', 1, NOW(), NOW(), 0),
(3, 2, '查看用户', 'system:user:view', 2, '', 1, NOW(), NOW(), 0),
(4, 2, '添加用户', 'system:user:add', 2, '', 2, NOW(), NOW(), 0),
(5, 2, '修改用户', 'system:user:edit', 2, '', 3, NOW(), NOW(), 0),
(6, 2, '删除用户', 'system:user:delete', 2, '', 4, NOW(), NOW(), 0),
-- 会议室管理
(7, 0, '会议室管理', 'room', 1, '/room', 2, NOW(), NOW(), 0),
(8, 7, '查看会议室', 'room:view', 2, '', 1, NOW(), NOW(), 0),
(9, 7, '添加会议室', 'room:add', 2, '', 2, NOW(), NOW(), 0),
(10, 7, '修改会议室', 'room:edit', 2, '', 3, NOW(), NOW(), 0),
(11, 7, '删除会议室', 'room:delete', 2, '', 4, NOW(), NOW(), 0),
-- 预约管理
(12, 0, '预约管理', 'booking', 1, '/booking', 3, NOW(), NOW(), 0),
(13, 12, '查看预约', 'booking:view', 2, '', 1, NOW(), NOW(), 0),
(14, 12, '添加预约', 'booking:add', 2, '', 2, NOW(), NOW(), 0),
(15, 12, '修改预约', 'booking:edit', 2, '', 3, NOW(), NOW(), 0),
(16, 12, '取消预约', 'booking:cancel', 2, '', 4, NOW(), NOW(), 0),
(17, 12, '审批预约', 'booking:approve', 2, '', 5, NOW(), NOW(), 0);

-- 3. 初始化角色权限关联表
INSERT INTO role_permission (role_id, permission_id, create_time, update_time, is_deleted) VALUES
-- 超级管理员拥有所有权限
(1, 1, NOW(), NOW(), 0),
(1, 2, NOW(), NOW(), 0),
(1, 3, NOW(), NOW(), 0),
(1, 4, NOW(), NOW(), 0),
(1, 5, NOW(), NOW(), 0),
(1, 6, NOW(), NOW(), 0),
(1, 7, NOW(), NOW(), 0),
(1, 8, NOW(), NOW(), 0),
(1, 9, NOW(), NOW(), 0),
(1, 10, NOW(), NOW(), 0),
(1, 11, NOW(), NOW(), 0),
(1, 12, NOW(), NOW(), 0),
(1, 13, NOW(), NOW(), 0),
(1, 14, NOW(), NOW(), 0),
(1, 15, NOW(), NOW(), 0),
(1, 16, NOW(), NOW(), 0),
(1, 17, NOW(), NOW(), 0),
-- 普通用户只有部分权限
(2, 7, NOW(), NOW(), 0),
(2, 8, NOW(), NOW(), 0),
(2, 12, NOW(), NOW(), 0),
(2, 13, NOW(), NOW(), 0),
(2, 14, NOW(), NOW(), 0),
(2, 15, NOW(), NOW(), 0),
(2, 16, NOW(), NOW(), 0);

-- 4. 初始化系统配置表
INSERT INTO system_config (id, config_key, config_value, description, create_time, update_time, is_deleted) VALUES
(1, 'SYSTEM_NAME', '会议室预约管理系统', '系统名称', NOW(), NOW(), 0),
(2, 'SYSTEM_LOGO', '/logo.png', '系统Logo', NOW(), NOW(), 0),
(3, 'BOOKING_START_TIME', '09:00:00', '预约开始时间', NOW(), NOW(), 0),
(4, 'BOOKING_END_TIME', '18:00:00', '预约结束时间', NOW(), NOW(), 0),
(5, 'BOOKING_MIN_DURATION', '30', '最小预约时长(分钟)', NOW(), NOW(), 0),
(6, 'BOOKING_MAX_DURATION', '240', '最大预约时长(分钟)', NOW(), NOW(), 0);

-- 5. 初始化超级管理员账号
INSERT INTO user (id, username, password, real_name, email, phone, avatar, role_id, status, create_time, update_time, is_deleted) VALUES
(1, 'admin', '1234', '超级管理员', 'admin@example.com', '13800138000', '/avatar/default.png', 1, 1, NOW(), NOW(), 0);





