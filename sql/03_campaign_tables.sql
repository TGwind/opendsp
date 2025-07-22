-- 广告投放相关表初始化脚本
USE opendsp;

-- 创建DSP平台广告位表
CREATE TABLE IF NOT EXISTS `ad_slot` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键，广告位id',
    `name` VARCHAR(200) NOT NULL COMMENT '广告位名称',
    `description` VARCHAR(500) COMMENT '广告位描述',
    `ad_type` INT NOT NULL COMMENT '广告类型：1-Banner/图片,2-视频,3-原生广告位，4-激励视频，5-音频广告',
    `width` INT COMMENT '图片/视频宽',
    `height` INT COMMENT '图片/视频高',
    `wratio` INT COMMENT '宽高比-宽',
    `hratio` INT COMMENT '宽高比->高',
    `wmin` INT COMMENT '在使用宽高比匹配广告的时候，限制最小宽度',
    `minduration` INT COMMENT '视频广告最小时长（秒）',
    `maxduration` INT COMMENT '视频广告最长时长（秒）',
    `native_ad_spec_id` INT COMMENT '原生广告规格id',
    `floor_price` BIGINT DEFAULT 0 COMMENT '广告位底价（单位：分）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(100) COMMENT '创建人',
    `update_user` VARCHAR(100) COMMENT '更新人',
    `status` INT NOT NULL DEFAULT 1 COMMENT '广告位状态： 0-无效, 1-正常',
    KEY `idx_ad_type` (`ad_type`),
    KEY `idx_native_ad_spec_id` (`native_ad_spec_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='DSP平台广告位表';

-- 创建原生广告规格定义表
CREATE TABLE IF NOT EXISTS `native_ad_spec` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `ad_slot_id` INT NOT NULL COMMENT '广告位id',
    `asset_type` INT NOT NULL COMMENT '广告组件类型: 1-文本,2-图片,3-视频',
    `asset_name` VARCHAR(100) NOT NULL COMMENT '名称，比如：主图、logo、描述等',
    `asset_key` VARCHAR(50) NOT NULL COMMENT '英文名',
    `width` INT COMMENT '图片组件宽',
    `height` INT COMMENT '图片组件高',
    `len` INT COMMENT '文本组件长度限制',
    `minduration` INT COMMENT '视频组件最小时长（秒）',
    `maxduration` INT COMMENT '视频组件最长时长（秒）',
    `mimes` VARCHAR(200) COMMENT '支持媒体类型',
    `required` TINYINT(1) DEFAULT 0 COMMENT '是否必填：0-否，1-是',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_ad_slot_id` (`ad_slot_id`),
    KEY `idx_asset_type` (`asset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='原生广告规格定义表';

-- 创建ADX广告位表
CREATE TABLE IF NOT EXISTS `adx_ad_slot` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `adx_id` VARCHAR(50) NOT NULL COMMENT 'ADX平台标识',
    `adx_slot_id` VARCHAR(100) NOT NULL COMMENT 'ADX广告位id',
    `slot_name` VARCHAR(200) COMMENT '广告位名称',
    `ad_type` INT NOT NULL COMMENT '广告类型：1-Banner,2-视频,3-原生',
    `width` INT COMMENT '宽度',
    `height` INT COMMENT '高度',
    `floor_price` BIGINT DEFAULT 0 COMMENT '底价（单位：分）',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-无效,1-正常',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_adx_slot` (`adx_id`, `adx_slot_id`),
    KEY `idx_adx_id` (`adx_id`),
    KEY `idx_ad_type` (`ad_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ADX广告位表';

-- 创建推广计划表
CREATE TABLE IF NOT EXISTS `campaign` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name` VARCHAR(200) NOT NULL COMMENT '推广计划名称',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `budget` BIGINT NOT NULL COMMENT '每日预算（单位：分）',
    `start_time` TIMESTAMP NOT NULL COMMENT '推广开始时间',
    `end_time` TIMESTAMP NOT NULL COMMENT '推广结束时间',
    `delivery_mode` INT NOT NULL DEFAULT 1 COMMENT '投放方式： 1-正常投放,2-匀速投放',
    `promotion_type` INT NOT NULL COMMENT '推广类型：1-品牌推广,2-效果推广',
    `imp_tracking_urls` TEXT COMMENT '默认曝光监测地址，下层广告组如果设置了会覆盖这个配置',
    `click_tracking_urls` TEXT COMMENT '默认点击监测地址，下层设置会覆盖',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(100) COMMENT '格式： 用户id:用户名称',
    `update_user` VARCHAR(100) COMMENT '同create_user字段',
    `status` INT NOT NULL DEFAULT 1 COMMENT '推广计划状态： 0-无效,1-正常,2-预算超限',
    KEY `idx_advertiser_id` (`advertiser_id`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推广计划表';

-- 创建广告组表
CREATE TABLE IF NOT EXISTS `ad_group` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name` VARCHAR(200) NOT NULL COMMENT '广告组名称',
    `campaign_id` INT NOT NULL COMMENT '活动id',
    `advertiser_id` INT NOT NULL COMMENT '广告主id',
    `ad_slot_id` INT COMMENT '广告位id',
    `begin_date` TIMESTAMP NOT NULL COMMENT '投放开始日期',
    `end_date` TIMESTAMP NOT NULL COMMENT '投放结束日期',
    `time_slots` VARCHAR(200) COMMENT '投放时间段，格式：0-23,表示0点到23点',
    `budget` BIGINT NOT NULL COMMENT '每日投放预算（单位：分）',
    `delivery_mode` INT NOT NULL DEFAULT 1 COMMENT '投放模式：1-正常 2-匀速',
    `bidding_method` INT NOT NULL COMMENT '出价方式： 1-CPM,2-CPC,3-CPA,4-oCPM,5-oCPC',
    `bid_price` BIGINT NOT NULL COMMENT '广告出价（单位：分）',
    `landing_url` VARCHAR(500) COMMENT '落地页地址',
    `bundle` VARCHAR(100) COMMENT '推广应用的appid or bundle',
    `promotion_type` INT COMMENT '推广类型, 参见sys_dict表中的dict_type为promotion_type的字典项',
    `freq_capping` JSON COMMENT '频次控制, 格式：{"period":"day","freq":3}',
    `deal_id` VARCHAR(100) COMMENT '私有竞价标识，包括PDB,PD,PA',
    `target_adx` VARCHAR(200) COMMENT '定向交易平台，比如定向爱奇艺',
    `target_media` VARCHAR(500) COMMENT '媒体定向，多个媒体id按逗号分隔',
    `target_region` VARCHAR(1000) COMMENT '地域定向，多个逗号分隔',
    `target_geo_location` JSON COMMENT '地理位置定向，json数组格式',
    `target_os` VARCHAR(200) COMMENT '操作系统定向，多个逗号分隔，操作系统枚举',
    `target_carrier` VARCHAR(200) COMMENT '电信运营商定向，多个逗号分隔',
    `target_device_type` VARCHAR(200) COMMENT '设备定向，多个逗号分隔',
    `target_device_make` VARCHAR(200) COMMENT '定向设备生产商， 品牌枚举',
    `target_device_model` VARCHAR(200) COMMENT '定向设备型号',
    `target_connection_type` VARCHAR(200) COMMENT '网络定向: wifi,2G,3G,4G,5G',
    `imp_tracking_urls` TEXT COMMENT '曝光监测地址',
    `click_tracking_urls` TEXT COMMENT '点击监测地址',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
    `create_user` VARCHAR(100) COMMENT '创建用户',
    `update_user` VARCHAR(100) COMMENT '更新用户',
    `status` INT NOT NULL DEFAULT 1 COMMENT '广告组状态：0-无效,1-正常,2-预算超限',
    KEY `idx_campaign_id` (`campaign_id`),
    KEY `idx_advertiser_id` (`advertiser_id`),
    KEY `idx_ad_slot_id` (`ad_slot_id`),
    KEY `idx_begin_date` (`begin_date`),
    KEY `idx_end_date` (`end_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告组-广告投放策略设置表';

-- 创建广告创意表
CREATE TABLE IF NOT EXISTS `creative` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `ad_group_id` INT NOT NULL COMMENT '广告组id',
    `ad_slot_id` INT COMMENT '广告位id',
    `creative_name` VARCHAR(200) NOT NULL COMMENT '创意名称',
    `creative_url` VARCHAR(500) COMMENT '创意url, 图片或视频广告素材地址',
    `creative_type` INT NOT NULL DEFAULT 1 COMMENT '创意类型：1-图片,2-视频,3-原生',
    `width` INT COMMENT '创意宽度',
    `height` INT COMMENT '创意高度',
    `duration` INT COMMENT '视频时长（秒）',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `native_ad_content` JSON COMMENT '原生广告内容，JSON格式',
    `audit_status` INT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核,1-审核通过,2-审核不通过',
    `audit_comments` VARCHAR(500) COMMENT '审核意见',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日期',
    `create_user` VARCHAR(100) COMMENT '创建用户',
    `update_user` VARCHAR(100) COMMENT '更新用户',
    `status` INT NOT NULL DEFAULT 1 COMMENT '广告创意状态：0-无效,1-正常',
    KEY `idx_ad_group_id` (`ad_group_id`),
    KEY `idx_ad_slot_id` (`ad_slot_id`),
    KEY `idx_creative_type` (`creative_type`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告创意表';

-- 创建广告创意-adx平台关联表
CREATE TABLE IF NOT EXISTS `creative_adx` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `creative_id` INT NOT NULL COMMENT '创意id',
    `adx_creative_id` VARCHAR(100) COMMENT 'adx创意id',
    `adx_id` VARCHAR(50) NOT NULL COMMENT 'adx唯一标识符',
    `audit_status` INT NOT NULL DEFAULT 0 COMMENT 'adx审核状态：0-待审核,1-审核通过,2-审核不通过',
    `audit_comments` VARCHAR(500) COMMENT '审核批注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_creative_adx` (`creative_id`, `adx_id`),
    KEY `idx_creative_id` (`creative_id`),
    KEY `idx_adx_id` (`adx_id`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告创意-adx平台关联表'; 