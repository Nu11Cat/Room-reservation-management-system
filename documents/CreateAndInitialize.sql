
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
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) COMMENT '配置类型',
    description VARCHAR(200) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '系统配置表';

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
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, NOW(), NOW(), 0),
(2, '普通用户', 'USER', '普通用户，可以预约会议室', 1, NOW(), NOW(), 0),
(3, '审批人', 'APPROVER', '审批人，负责审批预约申请', 1, NOW(), NOW(), 0);

-- 2. 初始化权限表（保持不变）
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
-- 普通用户权限
(2, 7, NOW(), NOW(), 0),  -- 会议室管理
(2, 8, NOW(), NOW(), 0),  -- 查看会议室
(2, 12, NOW(), NOW(), 0), -- 预约管理
(2, 13, NOW(), NOW(), 0), -- 查看预约
(2, 14, NOW(), NOW(), 0), -- 添加预约
(2, 15, NOW(), NOW(), 0), -- 修改预约
(2, 16, NOW(), NOW(), 0), -- 取消预约
-- 审批人权限
(3, 7, NOW(), NOW(), 0),  -- 会议室管理
(3, 8, NOW(), NOW(), 0),  -- 查看会议室
(3, 12, NOW(), NOW(), 0), -- 预约管理
(3, 13, NOW(), NOW(), 0), -- 查看预约
(3, 17, NOW(), NOW(), 0); -- 审批预约

-- 4. 初始化系统配置表
INSERT INTO system_config (id, config_key, config_value, config_type, description, create_time, update_time, is_deleted) VALUES
(1, 'BOOKING_CONFLICT_RULE', '{"allowOverlap": false, "minInterval": 30}', 'BOOKING_RULE', '预约冲突规则配置', NOW(), NOW(), 0),
(2, 'BOOKING_MIN_INTERVAL', '15', 'BOOKING_RULE', '最小预约间隔(分钟)', NOW(), NOW(), 0),
(3, 'BOOKING_MAX_DURATION', '480', 'BOOKING_RULE', '最大预约时长(分钟)', NOW(), NOW(), 0),
(4, 'APPROVAL_REQUIRED', 'true', 'APPROVAL_RULE', '是否需要审批', NOW(), NOW(), 0),
(5, 'APPROVAL_AUTO_APPROVE', 'false', 'APPROVAL_RULE', '是否自动审批', NOW(), NOW(), 0),
(6, 'APPROVAL_NOTIFY_EMAIL', 'true', 'APPROVAL_RULE', '审批结果是否邮件通知', NOW(), NOW(), 0),
(7, 'SYSTEM_NAME', '会议室预约管理系统', 'SYSTEM_PARAM', '系统名称', NOW(), NOW(), 0),
(8, 'SYSTEM_LOGO', '/static/images/logo.png', 'SYSTEM_PARAM', '系统Logo', NOW(), NOW(), 0),
(9, 'SYSTEM_EMAIL_SERVER', 'smtp.example.com', 'SYSTEM_PARAM', '邮件服务器地址', NOW(), NOW(), 0),
(10, 'SYSTEM_EMAIL_PORT', '465', 'SYSTEM_PARAM', '邮件服务器端口', NOW(), NOW(), 0),
(11, 'NOTIFY_APPROVAL_TEMPLATE', '您的会议室预约申请已提交，等待审批', 'NOTIFY_TEMPLATE', '审批提交通知模板', NOW(), NOW(), 0),
(12, 'NOTIFY_APPROVED_TEMPLATE', '您的会议室预约申请已通过审批', 'NOTIFY_TEMPLATE', '审批通过通知模板', NOW(), NOW(), 0),
(13, 'NOTIFY_REJECTED_TEMPLATE', '您的会议室预约申请未通过审批', 'NOTIFY_TEMPLATE', '审批拒绝通知模板', NOW(), NOW(), 0);

-- 5. 初始化超级管理员账号
INSERT INTO user (id, username, password, real_name, email, phone, avatar, role_id, status, create_time, update_time, is_deleted) VALUES
(1, 'admin', '1234', '超级管理员', 'admin@example.com', '13800138000', '/avatar/default.png', 1, 1, NOW(), NOW(), 0);



-- 清空已有数据（如需要的话）
TRUNCATE TABLE permission;
TRUNCATE TABLE role_permission;
-- 角色表保留，避免影响已有用户

-- 1. 初始化权限表
INSERT INTO permission (id, parent_id, name, code, type, path, component, icon, sort, status, create_time, update_time, is_deleted) VALUES
-- 系统管理
(1, 0, '系统管理', 'system', 1, '/system', 'Layout', 'setting', 1, 1, NOW(), NOW(), 0),
-- 用户管理
(2, 1, '用户管理', 'system:user', 1, '/system/user', 'system/User', 'user', 1, 1, NOW(), NOW(), 0),
(3, 2, '查看用户', 'system:user:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(4, 2, '添加用户', 'system:user:add', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),
(5, 2, '编辑用户', 'system:user:edit', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0),
(6, 2, '删除用户', 'system:user:delete', 2, NULL, NULL, NULL, 4, 1, NOW(), NOW(), 0),
-- 系统配置
(7, 1, '系统配置', 'system:config', 1, '/system/config', 'system/Config', 'config', 2, 1, NOW(), NOW(), 0),
(8, 7, '查看配置', 'system:config:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(9, 7, '编辑配置', 'system:config:edit', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),

-- 会议室管理
(10, 0, '会议室管理', 'room', 1, '/room', 'Layout', 'office-building', 2, 1, NOW(), NOW(), 0),
(11, 10, '会议室列表', 'room:list', 1, '/room/list', 'room/List', 'list', 1, 1, NOW(), NOW(), 0),
(12, 11, '查看会议室', 'room:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(13, 11, '添加会议室', 'room:add', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),
(14, 11, '编辑会议室', 'room:edit', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0),
(15, 11, '删除会议室', 'room:delete', 2, NULL, NULL, NULL, 4, 1, NOW(), NOW(), 0),

-- 预订管理
(16, 0, '预订管理', 'booking', 1, '/booking', 'Layout', 'calendar', 3, 1, NOW(), NOW(), 0),
(17, 16, '预订列表', 'booking:list', 1, '/booking/list', 'booking/List', 'list', 1, 1, NOW(), NOW(), 0),
(18, 17, '查看预订', 'booking:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(19, 17, '添加预订', 'booking:add', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),
(20, 17, '编辑预订', 'booking:edit', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0),
(21, 17, '取消预订', 'booking:cancel', 2, NULL, NULL, NULL, 4, 1, NOW(), NOW(), 0),
(22, 17, '管理预订', 'booking:manage', 2, NULL, NULL, NULL, 5, 1, NOW(), NOW(), 0),
(23, 16, '审批预订', 'booking:approve', 1, '/booking/approval', 'booking/Approval', 'audit', 2, 1, NOW(), NOW(), 0),

-- 通知管理
(24, 0, '通知管理', 'notification', 1, '/notification', 'Layout', 'bell', 4, 1, NOW(), NOW(), 0),
(25, 24, '通知列表', 'notification:list', 1, '/notification/list', 'notification/List', 'list', 1, 1, NOW(), NOW(), 0),
(26, 25, '查看通知', 'notification:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(27, 25, '发送通知', 'notification:send', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),
(28, 25, '管理通知', 'notification:manage', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0),

-- 统计报表
(29, 0, '统计报表', 'report', 1, '/report', 'Layout', 'chart', 5, 1, NOW(), NOW(), 0),
(30, 29, '使用统计', 'report:view', 1, '/report/usage', 'report/Usage', 'data-analysis', 1, 1, NOW(), NOW(), 0);

-- 2. 角色-权限关联
-- 超级管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id, create_time, update_time, is_deleted)
SELECT 1, id, NOW(), NOW(), 0 FROM permission WHERE is_deleted = 0;

-- 普通用户权限
INSERT INTO role_permission (role_id, permission_id, create_time, update_time, is_deleted) VALUES
-- 会议室查看
(2, 10, NOW(), NOW(), 0), -- 会议室管理
(2, 11, NOW(), NOW(), 0), -- 会议室列表
(2, 12, NOW(), NOW(), 0), -- 查看会议室
-- 预订相关
(2, 16, NOW(), NOW(), 0), -- 预订管理
(2, 17, NOW(), NOW(), 0), -- 预订列表
(2, 18, NOW(), NOW(), 0), -- 查看预订
(2, 19, NOW(), NOW(), 0), -- 添加预订
(2, 20, NOW(), NOW(), 0), -- 编辑预订（自己的）
(2, 21, NOW(), NOW(), 0), -- 取消预订（自己的）
-- 通知
(2, 24, NOW(), NOW(), 0), -- 通知管理
(2, 25, NOW(), NOW(), 0), -- 通知列表
(2, 26, NOW(), NOW(), 0), -- 查看通知
-- 统计报表
(2, 29, NOW(), NOW(), 0), -- 统计报表
(2, 30, NOW(), NOW(), 0); -- 使用统计

-- 审批人权限（包含普通用户权限，再加上审批权限）
INSERT INTO role_permission (role_id, permission_id, create_time, update_time, is_deleted) VALUES
-- 普通用户已有的权限不重复插入

-- 额外拥有的审批权限
(3, 23, NOW(), NOW(), 0); -- 审批预订
(3, 17, NOW(), NOW(), 0); -- 查看未审批




ALTER TABLE booking 
   ADD COLUMN is_start_notified TINYINT(1) DEFAULT 0 COMMENT '是否已发送开始通知',
   ADD COLUMN is_end_notified TINYINT(1) DEFAULT 0 COMMENT '是否已发送结束通知';


INSERT INTO system_config (id, config_key, config_value, config_type, description, create_time, update_time, is_deleted) VALUES
(14, 'BOOKING_START_TIME', '08:00', 'BOOKING_RULE', '每日可预约开始时间', NOW(), NOW(), 0),
(15, 'BOOKING_END_TIME', '21:00', 'BOOKING_RULE', '每日可预约结束时间', NOW(), NOW(), 0);

   INSERT INTO system_config (id, config_key, config_value, config_type, description, create_time, update_time, is_deleted) VALUES
   (16, 'BOOKING_TIME_INTERVAL', '15', 'BOOKING_RULE', '预约时间粒度(分钟)', NOW(), NOW(), 0);



 -- 清空原有配置（可选）
 DELETE FROM system_config WHERE 1=1;

-- 重置自增值（可选）
 ALTER TABLE system_config AUTO_INCREMENT = 1;

-- 插入新的核心配置项
INSERT INTO system_config (id, config_key, config_value, config_type, description, create_time, update_time, is_deleted) VALUES
-- 预约时间规则
(1, 'BOOKING_START_TIME', '08:00', 'BOOKING_RULE', '每日可预约开始时间', NOW(), NOW(), 0),
(2, 'BOOKING_END_TIME', '21:00', 'BOOKING_RULE', '每日可预约结束时间', NOW(), NOW(), 0),
(3, 'BOOKING_TIME_INTERVAL', '30', 'BOOKING_RULE', '预约时间粒度(分钟)，可选值：10,15,30,60', NOW(), NOW(), 0),
(4, 'BOOKING_MAX_DURATION', '480', 'BOOKING_RULE', '最大预约时长(分钟)', NOW(), NOW(), 0),
(5, 'BOOKING_MIN_INTERVAL', '15', 'BOOKING_RULE', '最小预约间隔(分钟)', NOW(), NOW(), 0),

-- 预约业务规则
(6, 'BOOKING_MAX_ADVANCE_DAYS', '30', 'BOOKING_RULE', '最大提前预约天数', NOW(), NOW(), 0),
(7, 'BOOKING_CANCEL_LIMIT', '120', 'BOOKING_RULE', '提前取消时限(分钟)，预约开始前多久可以取消', NOW(), NOW(), 0),

-- 审批规则
(8, 'APPROVAL_REQUIRED', 'true', 'APPROVAL_RULE', '是否需要审批', NOW(), NOW(), 0),

-- 系统参数
(9, 'SYSTEM_NAME', '会议室预约管理系统', 'SYSTEM_PARAM', '系统名称', NOW(), NOW(), 0),
(10, 'ADMIN_CONTACT', '管理员：admin@example.com | 电话：123-4567-8900', 'SYSTEM_PARAM', '管理员联系信息', NOW(), NOW(), 0);


-- 添加角色管理权限
INSERT INTO permission (id, parent_id, name, code, type, path, component, icon, sort, status, create_time, update_time, is_deleted) VALUES
-- 角色管理
(31, 1, '角色管理', 'system:role', 1, '/system/role', 'system/Role', 'role', 3, 1, NOW(), NOW(), 0),
(32, 31, '查看角色', 'system:role:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0),
(33, 31, '添加角色', 'system:role:add', 2, NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0),
(34, 31, '编辑角色', 'system:role:edit', 2, NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0),
(35, 31, '删除角色', 'system:role:delete', 2, NULL, NULL, NULL, 4, 1, NOW(), NOW(), 0),

-- 权限管理
(36, 1, '权限管理', 'system:permission', 1, '/system/permission', 'system/Permission', 'permission', 4, 1, NOW(), NOW(), 0),
(37, 36, '查看权限', 'system:permission:view', 2, NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0);

-- 为管理员角色(ID=1)添加角色和权限管理相关权限
INSERT INTO role_permission (role_id, permission_id, create_time, update_time, is_deleted) VALUES
-- 角色管理权限
(1, 31, NOW(), NOW(), 0),
(1, 32, NOW(), NOW(), 0),
(1, 33, NOW(), NOW(), 0),
(1, 34, NOW(), NOW(), 0),
(1, 35, NOW(), NOW(), 0),
-- 权限管理权限
(1, 36, NOW(), NOW(), 0),
(1, 37, NOW(), NOW(), 0);










-- 1.0+版本，新增用户类型字段
-- 在用户表中添加字段
ALTER TABLE user ADD COLUMN identity VARCHAR(20) DEFAULT "学生" COMMENT '用户身份类型：admin-管理员，teacher-老师，student-学生';
-- 在用户表中添加信誉积分字段
ALTER TABLE user ADD COLUMN credit_score INT DEFAULT 100 
  CHECK (credit_score >= 0 AND credit_score <= 100) 
  COMMENT '用户信誉值，范围0-100，默认初始值100';
--1. 用户评价表设计
CREATE TABLE user_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    booking_id BIGINT NOT NULL COMMENT '预约记录ID',
    reviewer_id BIGINT NOT NULL COMMENT '评价人ID',
    reviewed_user_id BIGINT NOT NULL COMMENT '被评价用户ID',
    review_score INT NOT NULL CHECK (review_score BETWEEN 1 AND 5) COMMENT '评分(1-5分)',
    review_type INT NOT NULL COMMENT '评价类型：1-清洁度,2-守时,3-设备使用,4-噪音,5-其他',
    review_content VARCHAR(500) COMMENT '评价内容',
    evidence_urls JSON COMMENT '证据图片URL',
    is_processed TINYINT(1) DEFAULT 0 COMMENT '是否已处理',
    credit_impact INT DEFAULT 0 COMMENT '对信誉分的影响值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_booking (booking_id),
    INDEX idx_room (room_id),
    INDEX idx_reviewer (reviewer_id),
    INDEX idx_reviewed (reviewed_user_id)
) COMMENT='用户会议室使用评价表';
--2. 不文明行为类型表
CREATE TABLE misconduct_type (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL COMMENT '不文明行为类型名称',
    description VARCHAR(200) COMMENT '行为描述',
    default_score_impact INT NOT NULL COMMENT '默认信誉分影响值',
    severity_level INT NOT NULL COMMENT '严重程度(1-5)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='不文明行为类型表';

-- 插入一些预设的不文明行为类型
INSERT INTO misconduct_type (type_name, description, default_score_impact, severity_level) VALUES
('会议室卫生不打扫', '离开后未清理会议室垃圾、白板等', -5, 2),
('超时使用', '超出预约时间仍占用会议室', -10, 3),
('损坏设备', '使用过程中损坏会议室设备', -15, 4),
('噪音干扰', '会议过程中噪音过大影响他人', -5, 2),
('未按时到场', '预约后未按时到场使用', -10, 3),
('私自取消预约', '临时取消预约但未通知系统', -8, 3),
('违规使用设备', '不按规定使用会议室设备', -5, 2),
('人数超额', '实际使用人数超过申报人数', -3, 1);
--3. 评价处理记录表
CREATE TABLE review_processing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL COMMENT '评价ID',
    processor_id BIGINT NOT NULL COMMENT '处理人ID',
    processing_result TINYINT NOT NULL COMMENT '处理结果：1-有效,2-无效,3-需调查',
    final_credit_impact INT COMMENT '最终信誉分变动',
    processing_comment VARCHAR(500) COMMENT '处理意见',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_review (review_id),
    INDEX idx_processor (processor_id)
) COMMENT='评价处理记录表';
--4. 信誉积分变动记录表
CREATE TABLE credit_score_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    score_change INT NOT NULL COMMENT '分数变动值',
    previous_score INT NOT NULL COMMENT '变动前分数',
    current_score INT NOT NULL COMMENT '当前分数',
    change_reason VARCHAR(100) NOT NULL COMMENT '变动原因',
    related_id BIGINT COMMENT '关联ID(评价ID或其他)',
    operator_id BIGINT COMMENT '操作人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user (user_id),
    INDEX idx_time (create_time)
) COMMENT='信誉积分变动记录表';
-- 特殊时间段预约调整表
CREATE TABLE special_time_period (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,             -- 唯一ID
    week_start_date DATE NOT NULL,                    -- 本周的开始日期（即本周的周一）
    teacher_reserved_count INT NOT NULL DEFAULT 0,    -- 本周教师预约的会议室数量
    teacher_available_count INT NOT NULL,             -- 本周教师可以预约的会议室数量
    teacher_min_reserve_count INT NOT NULL DEFAULT 5, -- 最少预留给教师的会议室数量（可以设定默认值）
    student_available_count INT NOT NULL,             -- 本周学生可以预约的会议室数量
    total_rooms_count INT NOT NULL,                   -- 总会议室数
    adjustment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 数据调整时间
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,     -- 创建时间
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    INDEX idx_week_start (week_start_date),           -- 按照周的开始日期索引
    INDEX idx_adjustment_date (adjustment_date)      -- 按照调整时间索引
) COMMENT='特殊时间段预约调整表';


-- 修改user_review表，添加不文明行为类型字段和信誉分影响值字段
ALTER TABLE user_review
-- 添加不文明行为类型数组字段
    ADD COLUMN misconduct_types JSON COMMENT '不文明行为类型ID数组',
-- 修改评分字段的约束
    MODIFY COLUMN review_score TINYINT CHECK (review_score BETWEEN 1 AND 5) COMMENT '整体评价分数（1-5分）',
-- 修改信用影响字段的注释
    MODIFY COLUMN credit_impact INT COMMENT '信用分影响（多个不文明行为类型的累计影响）';