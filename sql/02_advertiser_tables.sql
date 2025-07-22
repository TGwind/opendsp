-- 广告主相关表初始化脚本
USE opendsp;

-- 创建代理商信息表
CREATE TABLE IF NOT EXISTS `agency` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键，代理商id',
    `name` VARCHAR(100) NOT NULL COMMENT '代理商名称',
    `password` VARCHAR(255) NOT NULL COMMENT '代理商登录密码',
    `sys_user_id` INT COMMENT '代理商对应的用户id',
    `company_name` VARCHAR(200) COMMENT '营业执照上名称',
    `province` INT COMMENT '省',
    `city` INT COMMENT '市',
    `county` INT COMMENT '区县',
    `address` VARCHAR(500) COMMENT '详细地址',
    `email` VARCHAR(100) COMMENT '邮件',
    `site_url` VARCHAR(200) COMMENT '企业网址',
    `contact` VARCHAR(100) COMMENT '联系人',
    `tel` VARCHAR(50) COMMENT '联系电话',
    `create_user` VARCHAR(100) COMMENT '创建人信息，格式： 用户id:用户名称',
    `update_user` VARCHAR(100) COMMENT '更新人信息',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用,1-正常,2-余额不足',
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sys_user_id` (`sys_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代理商信息表';

-- 创建广告主信息表
CREATE TABLE IF NOT EXISTS `advertiser` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键，广告主id',
    `name` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '广告主登录密码',
    `audit_status` INT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核,1-审核通过,2-审核不通过',
    `sys_user_id` INT COMMENT '广告主对应的用户id',
    `agency_id` INT COMMENT '代理商id',
    `audit_user` VARCHAR(100) COMMENT '审核人信息，格式： 用户id:用户名称',
    `audit_time` TIMESTAMP NULL COMMENT '审核时间',
    `audit_comments` VARCHAR(500) COMMENT '审核备注',
    `company_name` VARCHAR(200) COMMENT '营业执照上名称',
    `province` INT COMMENT '省',
    `city` INT COMMENT '市',
    `county` INT COMMENT '区县',
    `address` VARCHAR(500) COMMENT '详细地址',
    `email` VARCHAR(100) COMMENT '邮件',
    `site_url` VARCHAR(200) COMMENT '企业网址',
    `contact` VARCHAR(100) COMMENT '联系人',
    `tel` VARCHAR(50) COMMENT '联系方式',
    `org_code` VARCHAR(100) COMMENT '组织机构代码',
    `business_license` VARCHAR(200) COMMENT '营业执照url',
    `create_user` VARCHAR(100) COMMENT '创建人信息，格式： 用户id:用户名称',
    `update_user` VARCHAR(100) COMMENT '更新人信息',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用,1-正常,2-余额不足',
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sys_user_id` (`sys_user_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告主信息表';

-- 创建广告主-adx关联表
CREATE TABLE IF NOT EXISTS `advertiser_adx` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `adx_advertiser_id` VARCHAR(100) COMMENT 'adx平台广告主id',
    `adx_id` VARCHAR(50) NOT NULL COMMENT 'adx唯一标识',
    `audit_status` INT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核,1-审核通过,2-审核不通过',
    `audit_comments` VARCHAR(500) COMMENT '审核备注/不通过原因',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_advertiser_adx` (`advertiser_id`, `adx_id`),
    KEY `idx_advertiser_id` (`advertiser_id`),
    KEY `idx_adx_id` (`adx_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告主-adx关联表';

-- 创建广告主资质表
CREATE TABLE IF NOT EXISTS `advertiser_qualification` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `qualification_type` INT NOT NULL COMMENT '资质类型：1-营业执照,2-行业资质,3-其他',
    `qualification_name` VARCHAR(200) NOT NULL COMMENT '资质名称',
    `qualification_url` VARCHAR(500) NOT NULL COMMENT '资质文件url',
    `audit_status` INT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核,1-审核通过,2-审核不通过',
    `audit_comments` VARCHAR(500) COMMENT '审核备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_advertiser_id` (`advertiser_id`),
    KEY `idx_qualification_type` (`qualification_type`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告主资质表';

-- 创建广告主充值记录表
CREATE TABLE IF NOT EXISTS `advertiser_recharge` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `type` INT NOT NULL COMMENT '充值类型： 1-充值，2-提现/退款',
    `amount` BIGINT NOT NULL COMMENT '充值金额（单位：分）， 充值金额为正，退款为负',
    `balance_before` BIGINT NOT NULL DEFAULT 0 COMMENT '充值前余额（单位：分）',
    `balance_after` BIGINT NOT NULL DEFAULT 0 COMMENT '充值后余额（单位：分）',
    `payment_method` VARCHAR(50) COMMENT '支付方式',
    `transaction_id` VARCHAR(100) COMMENT '交易流水号',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '充值日期',
    KEY `idx_advertiser_id` (`advertiser_id`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告主充值记录表';

-- 创建广告主余额表
CREATE TABLE IF NOT EXISTS `advertiser_balance` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `balance` BIGINT NOT NULL DEFAULT 0 COMMENT '账户余额（单位：分）',
    `frozen_balance` BIGINT NOT NULL DEFAULT 0 COMMENT '冻结余额（单位：分）',
    `total_recharge` BIGINT NOT NULL DEFAULT 0 COMMENT '总充值金额（单位：分）',
    `total_consume` BIGINT NOT NULL DEFAULT 0 COMMENT '总消费金额（单位：分）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁',
    UNIQUE KEY `uk_advertiser_id` (`advertiser_id`),
    KEY `idx_balance` (`balance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告主余额表'; 