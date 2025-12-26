-- ============================================
-- 服务器测试数据插入脚本
-- 执行日期: 2025-12-26
-- ============================================

SET NAMES utf8mb4;

-- --------------------------------------------
-- 1. 插入用户数据
-- 密码统一为：123456 (BCrypt加密)
-- --------------------------------------------

-- 住客用户
INSERT INTO `tb_user` (`username`, `password_hash`, `phone`, `user_type`, `status`) VALUES
('guest001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800001001', 'GUEST', 'ACTIVE'),
('guest002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800001002', 'GUEST', 'ACTIVE'),
('guest003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800001003', 'GUEST', 'ACTIVE'),
('guest004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800001004', 'GUEST', 'ACTIVE'),
('guest005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800001005', 'GUEST', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=username;

-- 员工用户
INSERT INTO `tb_user` (`username`, `password_hash`, `phone`, `user_type`, `status`) VALUES
('receptionist01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900001001', 'STAFF', 'ACTIVE'),
('receptionist02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900001002', 'STAFF', 'ACTIVE'),
('maintenance01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900001003', 'STAFF', 'ACTIVE'),
('manager01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900001004', 'STAFF', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=username;

-- 管理员用户
INSERT INTO `tb_user` (`username`, `password_hash`, `phone`, `user_type`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13700000000', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=username;

-- --------------------------------------------
-- 2. 插入住客扩展信息
-- --------------------------------------------

INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`)
SELECT u.user_id, '测试用户A', '320101199001011234', 'GOLD', 5500, 8000, 30
FROM `tb_user` u WHERE u.username = 'guest001'
ON DUPLICATE KEY UPDATE real_name=real_name;

INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`)
SELECT u.user_id, '测试用户B', '320101199102022345', 'SILVER', 2000, 3500, 12
FROM `tb_user` u WHERE u.username = 'guest002'
ON DUPLICATE KEY UPDATE real_name=real_name;

INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`)
SELECT u.user_id, '测试用户C', '320101199203033456', 'BRONZE', 600, 1200, 5
FROM `tb_user` u WHERE u.username = 'guest003'
ON DUPLICATE KEY UPDATE real_name=real_name;

INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`)
SELECT u.user_id, '测试用户D', '320101199304044567', 'BRONZE', 300, 500, 2
FROM `tb_user` u WHERE u.username = 'guest004'
ON DUPLICATE KEY UPDATE real_name=real_name;

INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`)
SELECT u.user_id, '测试用户E', '320101199405055678', 'PLATINUM', 15000, 20000, 80
FROM `tb_user` u WHERE u.username = 'guest005'
ON DUPLICATE KEY UPDATE real_name=real_name;

-- --------------------------------------------
-- 3. 插入员工扩展信息
-- --------------------------------------------

INSERT INTO `tb_staff` (`user_id`, `employee_id`, `role`, `department`)
SELECT u.user_id, 'EMP001', 'RECEPTIONIST', '前台接待部'
FROM `tb_user` u WHERE u.username = 'receptionist01'
ON DUPLICATE KEY UPDATE employee_id=employee_id;

INSERT INTO `tb_staff` (`user_id`, `employee_id`, `role`, `department`)
SELECT u.user_id, 'EMP002', 'RECEPTIONIST', '前台接待部'
FROM `tb_user` u WHERE u.username = 'receptionist02'
ON DUPLICATE KEY UPDATE employee_id=employee_id;

INSERT INTO `tb_staff` (`user_id`, `employee_id`, `role`, `department`)
SELECT u.user_id, 'EMP003', 'MAINTENANCE', '维修部'
FROM `tb_user` u WHERE u.username = 'maintenance01'
ON DUPLICATE KEY UPDATE employee_id=employee_id;

INSERT INTO `tb_staff` (`user_id`, `employee_id`, `role`, `department`)
SELECT u.user_id, 'EMP004', 'MANAGER', '运营管理部'
FROM `tb_user` u WHERE u.username = 'manager01'
ON DUPLICATE KEY UPDATE employee_id=employee_id;

-- --------------------------------------------
-- 4. 插入房间数据
-- --------------------------------------------

INSERT INTO `tb_room` (`room_no`, `room_type`, `floor`, `status`, `price_per_day`, `max_occupancy`, `current_occupancy`, `is_premium`, `facility_config`) VALUES
('201', 'SINGLE', 2, 'VACANT', 180.00, 1, 0, 0, '{"display":"27寸2K显示器","cpu":"Intel i5","gpu":"GTX 1660","chair":"人体工学椅"}'),
('202', 'SINGLE', 2, 'VACANT', 180.00, 1, 0, 0, '{"display":"27寸2K显示器","cpu":"Intel i5","gpu":"GTX 1660","chair":"人体工学椅"}'),
('203', 'DOUBLE', 2, 'VACANT', 280.00, 2, 0, 0, '{"display":"双27寸2K显示器","cpu":"Intel i7","gpu":"RTX 3060","chair":"电竞椅"}'),
('204', 'DOUBLE', 2, 'VACANT', 280.00, 2, 0, 0, '{"display":"双27寸2K显示器","cpu":"Intel i7","gpu":"RTX 3060","chair":"电竞椅"}'),
('301', 'VIP', 3, 'VACANT', 580.00, 2, 0, 1, '{"display":"双32寸4K显示器","cpu":"Intel i9","gpu":"RTX 4080","chair":"赛车椅","extra":"独立卫生间+小吧台"}'),
('302', 'VIP', 3, 'VACANT', 580.00, 2, 0, 1, '{"display":"双32寸4K显示器","cpu":"Intel i9","gpu":"RTX 4080","chair":"赛车椅","extra":"独立卫生间+小吧台"}'),
('303', 'FIVE_PLAYER', 3, 'VACANT', 880.00, 5, 0, 1, '{"display":"5x27寸2K显示器","cpu":"5x Intel i7","gpu":"5x RTX 3070","chair":"5x电竞椅","extra":"团队语音设备"}'),
('401', 'FIVE_PLAYER', 4, 'VACANT', 880.00, 5, 0, 1, '{"display":"5x27寸2K显示器","cpu":"5x Intel i7","gpu":"5x RTX 3070","chair":"5x电竞椅","extra":"团队语音设备"}'),
('402', 'VIP', 4, 'VACANT', 580.00, 2, 0, 1, '{"display":"双32寸4K显示器","cpu":"Intel i9","gpu":"RTX 4080","chair":"赛车椅","extra":"独立卫生间+小吧台"}'),
('403', 'DOUBLE', 4, 'VACANT', 280.00, 2, 0, 0, '{"display":"双27寸2K显示器","cpu":"Intel i7","gpu":"RTX 3060","chair":"电竞椅"}')
ON DUPLICATE KEY UPDATE room_no=room_no;

-- --------------------------------------------
-- 5. 插入商品数据 (POS系统)
-- --------------------------------------------

-- 零食类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`, `description`) VALUES
('乐事薯片-原味', 'SNACK', '膨化食品', 8.00, 'NONE', 100, 20, 1, '经典原味薯片，70g装'),
('乐事薯片-黄瓜味', 'SNACK', '膨化食品', 8.00, 'NONE', 80, 20, 1, '清新黄瓜味，70g装'),
('奥利奥饼干', 'SNACK', '饼干', 9.50, 'NONE', 120, 25, 1, '经典夹心饼干，116g装'),
('德芙巧克力', 'SNACK', '巧克力', 15.00, 'NONE', 60, 15, 1, '丝滑牛奶巧克力，80g装'),
('卫龙辣条', 'SNACK', '辣味小吃', 5.00, 'NONE', 150, 30, 1, '香辣过瘾，106g装'),
('三只松鼠坚果', 'SNACK', '坚果', 25.00, 'NONE', 50, 10, 1, '每日坚果大礼包，750g装'),
('旺仔牛奶', 'BEVERAGE', '乳制品', 6.00, 'NONE', 200, 40, 1, '经典旺仔牛奶，245ml装'),
('红牛能量饮料', 'BEVERAGE', '功能饮料', 8.00, 'NONE', 150, 30, 1, '提神醒脑，250ml装')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 饮料类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`, `description`) VALUES
('可口可乐', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 300, 50, 1, '经典可乐，500ml装'),
('雪碧', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 250, 50, 1, '清凉柠檬味，500ml装'),
('农夫山泉', 'BEVERAGE', '饮用水', 3.00, 'NONE', 500, 100, 1, '天然矿泉水，550ml装'),
('统一冰红茶', 'BEVERAGE', '茶饮料', 5.50, 'NONE', 180, 40, 1, '冰爽红茶，500ml装'),
('脉动', 'BEVERAGE', '功能饮料', 7.00, 'NONE', 120, 30, 1, '维生素饮料，600ml装'),
('东鹏特饮', 'BEVERAGE', '功能饮料', 7.50, 'NONE', 100, 25, 1, '提神能量饮料，500ml装')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 外设租赁类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`, `description`) VALUES
('罗技G502鼠标', 'PERIPHERAL', '鼠标', 20.00, 'DAILY', 15, 3, 1, '专业游戏鼠标，按天租赁'),
('雷蛇黑寡妇键盘', 'PERIPHERAL', '键盘', 30.00, 'DAILY', 12, 3, 1, '机械游戏键盘，按天租赁'),
('赛睿耳机', 'PERIPHERAL', '耳机', 25.00, 'DAILY', 20, 5, 1, '专业电竞耳机，按天租赁'),
('Xbox手柄', 'PERIPHERAL', '手柄', 15.00, 'DAILY', 10, 2, 1, '无线游戏手柄，按天租赁')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 一次性用品类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`, `description`) VALUES
('一次性牙刷套装', 'OTHER', '一次性用品', 3.00, 'NONE', 300, 50, 1, '含牙刷、牙膏'),
('一次性拖鞋', 'OTHER', '一次性用品', 2.50, 'NONE', 400, 80, 1, '舒适防滑拖鞋'),
('一次性毛巾', 'OTHER', '一次性用品', 4.00, 'NONE', 250, 50, 1, '纯棉柔软毛巾'),
('湿纸巾', 'OTHER', '清洁用品', 5.00, 'NONE', 200, 40, 1, '清洁消毒，80片装')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- 可重复使用物资（用于客房清洁）
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`, `max_usage_times`, `current_usage_times`, `description`) VALUES
('床单套装', 'OTHER', '可重复使用', 0.00, 'NONE', 50, 10, 1, 100, 0, '酒店床单，可重复清洗使用'),
('被套枕套', 'OTHER', '可重复使用', 0.00, 'NONE', 50, 10, 1, 100, 0, '酒店被套枕套，可重复清洗使用'),
('浴巾', 'OTHER', '可重复使用', 0.00, 'NONE', 80, 15, 1, 150, 0, '酒店浴巾，可重复清洗使用')
ON DUPLICATE KEY UPDATE product_name=product_name;

-- --------------------------------------------
-- 6. 插入积分商城商品
-- --------------------------------------------

INSERT INTO `tb_points_product` (`product_name`, `product_type`, `points_required`, `stock`, `description`, `is_available`) VALUES
('延迟退房1小时', 'SERVICE_PRIVILEGE', 500, 999, '可在原定退房时间基础上延长1小时，不另收费用', 1),
('延迟退房2小时', 'SERVICE_PRIVILEGE', 900, 999, '可在原定退房时间基础上延长2小时，不另收费用', 1),
('房费抵扣券10元', 'SERVICE_PRIVILEGE', 1000, 999, '退房结算时自动抵扣10元房费', 1),
('房费抵扣券30元', 'SERVICE_PRIVILEGE', 3000, 999, '退房结算时自动抵扣30元房费', 1),
('房费抵扣券50元', 'SERVICE_PRIVILEGE', 5000, 999, '退房结算时自动抵扣50元房费', 1),
('免费可乐1瓶', 'PHYSICAL', 500, 100, '兑换后可到前台免费领取可口可乐1瓶', 1),
('免费零食大礼包', 'PHYSICAL', 1500, 50, '包含薯片、饼干、巧克力等多种零食', 1),
('游戏外设升级', 'SERVICE_PRIVILEGE', 2000, 999, '免费升级使用高级游戏鼠标键盘1天', 1)
ON DUPLICATE KEY UPDATE product_name=product_name;

-- --------------------------------------------
-- 7. 插入任务模板
-- --------------------------------------------

INSERT INTO `tb_task` (`task_name`, `description`, `task_type`, `reward_points`, `reward_exp`, `condition_config`, `is_repeatable`, `is_active`) VALUES
('首次入住', '欢迎首次入住本酒店', 'AUTO', 100, 50, '{"type":"first_checkin"}', 0, 1),
('连续入住4小时', '在房间内持续游戏4小时', 'AUTO', 50, 30, '{"type":"playtime","value":240}', 1, 1),
('连续入住8小时', '在房间内持续游戏8小时', 'AUTO', 120, 80, '{"type":"playtime","value":480}', 1, 1),
('上传五杀截图', '上传游戏内五杀精彩瞬间', 'MANUAL_AUDIT', 150, 100, '{"type":"screenshot","keyword":"pentakill"}', 1, 1),
('给予五星好评', '退房时给予五星满分评价', 'AUTO', 200, 150, '{"type":"review","min_score":5}', 0, 1),
('推荐好友入住', '成功推荐好友入住酒店', 'MANUAL_AUDIT', 500, 300, '{"type":"referral"}', 1, 1),
('组队开黑', '成功组建或加入临时战队', 'AUTO', 80, 50, '{"type":"join_team"}', 1, 1),
('消费满100元', '在POS系统消费累计达100元', 'AUTO', 100, 60, '{"type":"spending","amount":100}', 0, 1)
ON DUPLICATE KEY UPDATE task_name=task_name;

-- --------------------------------------------
-- 8. 插入系统配置
-- --------------------------------------------

INSERT INTO `tb_system_config` (`config_key`, `config_value`, `description`) VALUES
('points_to_rmb_rate', '100', '积分兑换人民币汇率（100积分=1元）'),
('hardware_check_interval', '5', '硬件数据采集间隔（秒）'),
('temp_alert_threshold', '95', 'CPU/GPU温度报警阈值（°C）'),
('member_upgrade_bronze_to_silver', '1000', '青铜升级白银所需经验值'),
('member_upgrade_silver_to_gold', '5000', '白银升级黄金所需经验值'),
('member_upgrade_gold_to_platinum', '15000', '黄金升级铂金所需经验值'),
('checkout_time_default', '12:00', '默认退房时间'),
('deposit_rate', '0.3', '预订押金比例（房费的30%）')
ON DUPLICATE KEY UPDATE config_key=config_key;

-- --------------------------------------------
-- 完成提示
-- --------------------------------------------

SELECT 'Server test data insertion completed!' AS Status;
SELECT COUNT(*) AS user_count FROM tb_user;
SELECT COUNT(*) AS guest_count FROM tb_guest;
SELECT COUNT(*) AS staff_count FROM tb_staff;
SELECT COUNT(*) AS room_count FROM tb_room;
SELECT COUNT(*) AS product_count FROM tb_product;
SELECT COUNT(*) AS points_product_count FROM tb_points_product;
SELECT COUNT(*) AS task_count FROM tb_task;
