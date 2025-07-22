-- 初始数据脚本
USE opendsp;

-- 初始化字典数据
-- 用户类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('user_type', 'user_type', '用户类型', 1, 1, 1),
('user_type', '1', '广告主', 2, 1, 1),
('user_type', '2', '代理商', 2, 2, 1),
('user_type', '3', '普通用户', 2, 3, 1),
('user_type', '4', '管理员', 2, 4, 1);

-- 出价方式字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('bidding_method', 'bidding_method', '出价方式', 1, 2, 1),
('bidding_method', '1', 'CPM', 2, 1, 1),
('bidding_method', '2', 'CPC', 2, 2, 1),
('bidding_method', '3', 'CPA', 2, 3, 1),
('bidding_method', '4', 'oCPM', 2, 4, 1),
('bidding_method', '5', 'oCPC', 2, 5, 1);

-- 推广类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('promotion_type', 'promotion_type', '推广类型', 1, 3, 1),
('promotion_type', '1', '品牌推广', 2, 1, 1),
('promotion_type', '2', '效果推广', 2, 2, 1),
('promotion_type', '3', '电商推广', 2, 3, 1),
('promotion_type', '4', '应用推广', 2, 4, 1);

-- 广告交易平台字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('ad_exchange', 'ad_exchange', '广告交易平台', 1, 4, 1),
('ad_exchange', 'baidu', '百度ADX', 2, 1, 1),
('ad_exchange', 'tencent', '腾讯ADX', 2, 2, 1),
('ad_exchange', 'alibaba', '阿里ADX', 2, 3, 1),
('ad_exchange', 'bytedance', '字节跳动ADX', 2, 4, 1),
('ad_exchange', 'iqiyi', '爱奇艺ADX', 2, 5, 1),
('ad_exchange', 'youku', '优酷ADX', 2, 6, 1);

-- 网络类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('network_type', 'network_type', '网络类型', 1, 5, 1),
('network_type', 'wifi', 'WiFi', 2, 1, 1),
('network_type', '2g', '2G', 2, 2, 1),
('network_type', '3g', '3G', 2, 3, 1),
('network_type', '4g', '4G', 2, 4, 1),
('network_type', '5g', '5G', 2, 5, 1);

-- 操作系统字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('os', 'os', '操作系统', 1, 6, 1),
('os', 'android', 'Android', 2, 1, 1),
('os', 'ios', 'iOS', 2, 2, 1),
('os', 'windows', 'Windows', 2, 3, 1),
('os', 'macos', 'macOS', 2, 4, 1),
('os', 'linux', 'Linux', 2, 5, 1);

-- 设备类型字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('device_type', 'device_type', '设备类型', 1, 7, 1),
('device_type', '1', '手机', 2, 1, 1),
('device_type', '2', '平板', 2, 2, 1),
('device_type', '3', '桌面电脑', 2, 3, 1),
('device_type', '4', '智能电视', 2, 4, 1),
('device_type', '5', '其他', 2, 5, 1);

-- 行业字典
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('industry', 'industry', '行业分类', 1, 8, 1),
('industry', '1', '电商', 2, 1, 1),
('industry', '2', '游戏', 2, 2, 1),
('industry', '3', '教育', 2, 3, 1),
('industry', '4', '金融', 2, 4, 1),
('industry', '5', '汽车', 2, 5, 1),
('industry', '6', '房产', 2, 6, 1),
('industry', '7', '旅游', 2, 7, 1),
('industry', '8', '医疗', 2, 8, 1),
('industry', '9', '其他', 2, 9, 1);

-- 地区字典（部分省份）
INSERT INTO `sys_dict` (`dict_type`, `dict_value`, `dict_name`, `entry_type`, `sort_order`, `status`) VALUES
('region', 'region', '地区', 1, 9, 1),
('region', '110000', '北京市', 2, 1, 1),
('region', '120000', '天津市', 2, 2, 1),
('region', '130000', '河北省', 2, 3, 1),
('region', '140000', '山西省', 2, 4, 1),
('region', '150000', '内蒙古', 2, 5, 1),
('region', '210000', '辽宁省', 2, 6, 1),
('region', '220000', '吉林省', 2, 7, 1),
('region', '230000', '黑龙江', 2, 8, 1),
('region', '310000', '上海市', 2, 9, 1),
('region', '320000', '江苏省', 2, 10, 1),
('region', '330000', '浙江省', 2, 11, 1),
('region', '340000', '安徽省', 2, 12, 1),
('region', '350000', '福建省', 2, 13, 1),
('region', '360000', '江西省', 2, 14, 1),
('region', '370000', '山东省', 2, 15, 1),
('region', '410000', '河南省', 2, 16, 1),
('region', '420000', '湖北省', 2, 17, 1),
('region', '430000', '湖南省', 2, 18, 1),
('region', '440000', '广东省', 2, 19, 1),
('region', '450000', '广西', 2, 20, 1),
('region', '460000', '海南省', 2, 21, 1),
('region', '500000', '重庆市', 2, 22, 1),
('region', '510000', '四川省', 2, 23, 1),
('region', '520000', '贵州省', 2, 24, 1),
('region', '530000', '云南省', 2, 25, 1),
('region', '540000', '西藏', 2, 26, 1),
('region', '610000', '陕西省', 2, 27, 1),
('region', '620000', '甘肃省', 2, 28, 1),
('region', '630000', '青海省', 2, 29, 1),
('region', '640000', '宁夏', 2, 30, 1),
('region', '650000', '新疆', 2, 31, 1);

-- 创建默认管理员用户
INSERT INTO `sys_user` (`name`, `type`, `password`, `email`, `status`) VALUES
('admin', 4, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iEhePLcXSMl6dYwWz/hUCYNRqOk2', 'admin@opendsp.com', 1);

-- 创建默认角色
INSERT INTO `sys_role` (`name`, `remark`, `status`) VALUES
('管理员', '系统管理员，拥有所有权限', 1),
('广告主', '广告主用户，可以投放广告', 1),
('代理商', '代理商用户，可以管理广告主', 1),
('普通用户', '普通用户，只有查看权限', 1);

-- 创建默认权限
INSERT INTO `sys_permission` (`name`, `key`, `description`, `status`) VALUES
('广告主管理', 'advertiser:manage', '广告主增删改查权限', 1),
('代理商管理', 'agency:manage', '代理商增删改查权限', 1),
('推广计划管理', 'campaign:manage', '推广计划增删改查权限', 1),
('广告组管理', 'adgroup:manage', '广告组增删改查权限', 1),
('广告创意管理', 'creative:manage', '广告创意增删改查权限', 1),
('广告位管理', 'adslot:manage', '广告位增删改查权限', 1),
('用户管理', 'user:manage', '用户增删改查权限', 1),
('角色管理', 'role:manage', '角色增删改查权限', 1),
('权限管理', 'permission:manage', '权限增删改查权限', 1),
('字典管理', 'dict:manage', '字典增删改查权限', 1),
('统计报表', 'report:view', '统计报表查看权限', 1),
('财务管理', 'finance:manage', '财务管理权限', 1);

-- 创建默认菜单
INSERT INTO `sys_menu` (`name`, `parent_id`, `url`, `icon`, `sort_order`, `status`) VALUES
('广告管理', 0, '/ad', 'ad', 1, 1),
('广告主管理', 1, '/ad/advertiser', 'advertiser', 1, 1),
('代理商管理', 1, '/ad/agency', 'agency', 2, 1),
('推广计划', 1, '/ad/campaign', 'campaign', 3, 1),
('广告组', 1, '/ad/adgroup', 'adgroup', 4, 1),
('广告创意', 1, '/ad/creative', 'creative', 5, 1),
('广告位', 1, '/ad/adslot', 'adslot', 6, 1),
('系统管理', 0, '/system', 'system', 2, 1),
('用户管理', 8, '/system/user', 'user', 1, 1),
('角色管理', 8, '/system/role', 'role', 2, 1),
('权限管理', 8, '/system/permission', 'permission', 3, 1),
('字典管理', 8, '/system/dict', 'dict', 4, 1),
('统计报表', 0, '/report', 'report', 3, 1),
('数据概览', 13, '/report/overview', 'overview', 1, 1),
('广告统计', 13, '/report/ad-stats', 'stats', 2, 1),
('财务报表', 13, '/report/finance', 'finance', 3, 1),
('财务管理', 0, '/finance', 'finance', 4, 1),
('充值管理', 17, '/finance/recharge', 'recharge', 1, 1),
('结算管理', 17, '/finance/settlement', 'settlement', 2, 1);

-- 给管理员角色分配所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12);

-- 给管理员角色分配所有菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19);

-- 给管理员用户分配管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 创建示例广告位
INSERT INTO `ad_slot` (`name`, `description`, `ad_type`, `width`, `height`, `floor_price`, `create_user`, `status`) VALUES
('移动Banner 320x50', '移动端横幅广告位', 1, 320, 50, 100, 'admin', 1),
('移动Banner 320x100', '移动端横幅广告位', 1, 320, 100, 150, 'admin', 1),
('移动Banner 320x250', '移动端横幅广告位', 1, 320, 250, 300, 'admin', 1),
('桌面Banner 728x90', '桌面端横幅广告位', 1, 728, 90, 200, 'admin', 1),
('桌面Banner 300x250', '桌面端横幅广告位', 1, 300, 250, 300, 'admin', 1),
('视频广告 16:9', '视频广告位', 2, 1920, 1080, 1000, 'admin', 1),
('原生广告位', '原生广告位', 3, 0, 0, 500, 'admin', 1);

-- 创建示例ADX广告位
INSERT INTO `adx_ad_slot` (`adx_id`, `adx_slot_id`, `slot_name`, `ad_type`, `width`, `height`, `floor_price`, `status`) VALUES
('baidu', 'baidu_banner_320x50', '百度移动Banner', 1, 320, 50, 80, 1),
('baidu', 'baidu_banner_320x100', '百度移动Banner', 1, 320, 100, 120, 1),
('tencent', 'tencent_banner_320x50', '腾讯移动Banner', 1, 320, 50, 90, 1),
('tencent', 'tencent_video_16_9', '腾讯视频广告', 2, 1920, 1080, 800, 1),
('bytedance', 'bytedance_native', '字节跳动原生', 3, 0, 0, 400, 1),
('iqiyi', 'iqiyi_video_pre_roll', '爱奇艺前贴片', 2, 1920, 1080, 1200, 1);

-- 创建示例原生广告规格
INSERT INTO `native_ad_spec` (`ad_slot_id`, `asset_type`, `asset_name`, `asset_key`, `width`, `height`, `len`, `required`) VALUES
(7, 2, '主图', 'main_image', 1200, 628, NULL, 1),
(7, 1, '标题', 'title', NULL, NULL, 30, 1),
(7, 1, '描述', 'description', NULL, NULL, 100, 1),
(7, 2, '图标', 'icon', 100, 100, NULL, 0),
(7, 1, '行动号召', 'cta', NULL, NULL, 10, 1); 