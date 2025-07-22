-- OpenDSP数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS opendsp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE opendsp;

-- 设置时区
SET time_zone = '+08:00';

-- 创建系统用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '用户名',
    `type` INT NOT NULL DEFAULT 1 COMMENT '用户类型，1-广告主; 2-代理商；3-普通用户，4-管理员',
    `password` VARCHAR(255) NOT NULL COMMENT '登录密码，加密保存',
    `email` VARCHAR(100) COMMENT '用户邮箱',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `status` INT NOT NULL DEFAULT 1 COMMENT '用户状态，1-有效;0-无效',
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '角色id',
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `remark` VARCHAR(500) COMMENT '描述',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态，1-有效;0-无效',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '权限id',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `key` VARCHAR(100) NOT NULL COMMENT '权限key',
    `description` VARCHAR(500) COMMENT '权限描述',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态，1-有效;0-无效',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_key` (`key`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 创建菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单id',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `parent_id` INT DEFAULT 0 COMMENT '父菜单id',
    `url` VARCHAR(200) COMMENT '菜单url',
    `icon` VARCHAR(100) COMMENT '菜单图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态，1-有效;0-无效',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `user_id` INT NOT NULL COMMENT '用户id',
    `role_id` INT NOT NULL COMMENT '角色id',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `role_id` INT NOT NULL COMMENT '角色id',
    `permission_id` INT NOT NULL COMMENT '权限id',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 创建角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `role_id` INT NOT NULL COMMENT '角色id',
    `menu_id` INT NOT NULL COMMENT '菜单id',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 创建通用字典表
CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型（如 status, region, user_type等）',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典项键值（如 male, 1, 100000）',
    `dict_name` VARCHAR(200) NOT NULL COMMENT '显示值（如 男、启用、北京市）',
    `parent_value` VARCHAR(100) COMMENT '父字典key（支持树结构）',
    `entry_type` INT NOT NULL DEFAULT 2 COMMENT 'entry类型： 1-类型,2-字典项',
    `sort_order` INT DEFAULT 0 COMMENT '排序字段',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_dict_value` (`dict_value`),
    KEY `idx_parent_value` (`parent_value`),
    KEY `idx_entry_type` (`entry_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用字典表（支持类型和树结构)'; 