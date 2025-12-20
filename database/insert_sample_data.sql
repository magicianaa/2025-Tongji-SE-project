-- ============================================
-- 智慧电竞酒店管理系统 - 示例测试数据
-- ============================================
-- 使用说明：确保已执行 schema.sql 后再执行本文件
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 清理现有数据（按依赖关系倒序删除）
DELETE FROM `tb_device_log`;
DELETE FROM `tb_task_record`;
DELETE FROM `tb_point_transaction`;
DELETE FROM `tb_order_item`;
DELETE FROM `tb_pos_order`;
DELETE FROM `tb_maintenance_ticket`;
DELETE FROM `tb_hardware_status`;
DELETE FROM `tb_booking`;
DELETE FROM `tb_checkin_record`;
DELETE FROM `tb_guest`;
DELETE FROM `tb_staff`;
DELETE FROM `tb_user`;
DELETE FROM `tb_product`;
DELETE FROM `tb_points_product`;
DELETE FROM `tb_room`;

-- 重置自增ID
ALTER TABLE `tb_user` AUTO_INCREMENT = 1;
ALTER TABLE `tb_guest` AUTO_INCREMENT = 1;
ALTER TABLE `tb_staff` AUTO_INCREMENT = 1;
ALTER TABLE `tb_room` AUTO_INCREMENT = 1;
ALTER TABLE `tb_product` AUTO_INCREMENT = 1;
ALTER TABLE `tb_booking` AUTO_INCREMENT = 1;
ALTER TABLE `tb_checkin_record` AUTO_INCREMENT = 1;
ALTER TABLE `tb_pos_order` AUTO_INCREMENT = 1;
ALTER TABLE `tb_order_item` AUTO_INCREMENT = 1;
ALTER TABLE `tb_hardware_status` AUTO_INCREMENT = 1;
ALTER TABLE `tb_maintenance_ticket` AUTO_INCREMENT = 1;
ALTER TABLE `tb_task_record` AUTO_INCREMENT = 1;
ALTER TABLE `tb_point_transaction` AUTO_INCREMENT = 1;
ALTER TABLE `tb_points_product` AUTO_INCREMENT = 1;
ALTER TABLE `tb_device_log` AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------
-- 1. 房间数据（必须最先插入，其他数据依赖房间）
-- --------------------------------------------

INSERT INTO `tb_room` (`room_no`, `room_type`, `floor`, `status`, `price_per_day`, `max_occupancy`, `current_occupancy`, `is_premium`) VALUES
('201', 'SINGLE', 2, 'VACANT', 150.00, 1, 0, 0),
('202', 'DOUBLE', 2, 'VACANT', 250.00, 2, 0, 0),
('203', 'DOUBLE', 2, 'VACANT', 250.00, 2, 0, 0),
('204', 'SINGLE', 2, 'VACANT', 150.00, 1, 0, 0),
('301', 'VIP', 3, 'VACANT', 500.00, 2, 0, 1),
('302', 'FIVE_PLAYER', 3, 'VACANT', 800.00, 5, 0, 1),
('303', 'VIP', 3, 'VACANT', 500.00, 2, 0, 1),
('401', 'FIVE_PLAYER', 4, 'VACANT', 800.00, 5, 0, 1);

-- --------------------------------------------
-- 2. 用户数据（密码统一为：123456，BCrypt加密）
-- --------------------------------------------

-- 插入基础用户（3个住客 + 3个员工 + 1个管理员）
INSERT INTO `tb_user` (`username`, `password_hash`, `phone`, `user_type`, `status`) VALUES
-- 住客账号
('13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800138001', 'GUEST', 'ACTIVE'),
('13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800138002', 'GUEST', 'ACTIVE'),
('13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13800138003', 'GUEST', 'ACTIVE'),
-- 员工账号
('staff001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900139001', 'STAFF', 'ACTIVE'),
('staff002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900139002', 'STAFF', 'ACTIVE'),
('staff003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13900139003', 'STAFF', 'ACTIVE'),
-- 管理员账号
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EE4nN6N.sQpX4T8YLFKo6.', '13700137000', 'ADMIN', 'ACTIVE');

-- 插入住客扩展信息
INSERT INTO `tb_guest` (`user_id`, `real_name`, `identity_card`, `member_level`, `experience_points`, `current_points`, `total_checkin_nights`) VALUES
(1, '张三', '320106199001011234', 'SILVER', 1500, 2000, 15),
(2, '李四', '320106199102022345', 'BRONZE', 500, 800, 5),
(3, '王五', '320106199203033456', 'GOLD', 6000, 5000, 35);

-- 插入员工扩展信息
INSERT INTO `tb_staff` (`user_id`, `employee_id`, `role`, `department`) VALUES
(4, 'E001', 'RECEPTIONIST', 'Front Desk'),
(5, 'E002', 'MAINTENANCE', 'Maintenance'),
(6, 'E003', 'MANAGER', 'Operations');

-- --------------------------------------------
-- 2. 商品数据（POS系统）
-- --------------------------------------------

-- 零食类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`) VALUES
('Potato Chips (Original)', 'SNACK', 'Chips', 6.00, 'NONE', 50, 10, 1),
('Lays Chips (Cucumber)', 'SNACK', 'Chips', 6.50, 'NONE', 45, 10, 1),
('Oreo Cookies', 'SNACK', 'Cookies', 8.00, 'NONE', 60, 15, 1),
('Dove Chocolate', 'SNACK', 'Chocolate', 12.00, 'NONE', 40, 10, 1),
('Spicy Strips', 'SNACK', 'Spicy', 3.50, 'NONE', 80, 20, 1),
('Mixed Nuts', 'SNACK', 'Nuts', 15.00, 'NONE', 30, 8, 1);

-- 饮料类
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`) VALUES
('Coca Cola (330ml)', 'BEVERAGE', 'Soda', 3.50, 'NONE', 100, 20, 1),
('Sprite (330ml)', 'BEVERAGE', 'Soda', 3.50, 'NONE', 90, 20, 1),
('Red Bull (250ml)', 'BEVERAGE', 'Energy Drink', 6.00, 'NONE', 70, 15, 1),
('Mineral Water (550ml)', 'BEVERAGE', 'Water', 2.00, 'NONE', 150, 30, 1),
('Ice Tea', 'BEVERAGE', 'Tea', 3.00, 'NONE', 80, 15, 1),
('Mizone (Blueberry)', 'BEVERAGE', 'Sports Drink', 4.50, 'NONE', 60, 12, 1),
('Starbucks Latte', 'BEVERAGE', 'Coffee', 12.00, 'NONE', 40, 8, 1);

-- 外设类（租赁商品）
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `is_available`) VALUES
('Logitech G502 Mouse', 'PERIPHERAL', 'Mouse', 10.00, 'HOURLY', 15, 3, 1),
('Razer Keyboard', 'PERIPHERAL', 'Keyboard', 15.00, 'HOURLY', 12, 3, 1),
('SteelSeries Headset', 'PERIPHERAL', 'Headset', 8.00, 'HOURLY', 20, 5, 1),
('HTC VR Glasses', 'PERIPHERAL', 'VR', 50.00, 'DAILY', 5, 1, 1),
('Logitech Racing Wheel', 'PERIPHERAL', 'Racing Wheel', 30.00, 'DAILY', 3, 1, 1),
('Nintendo Switch', 'PERIPHERAL', 'Console', 100.00, 'DAILY', 4, 1, 1);

-- --------------------------------------------
-- 4. 预订数据（示例）
-- --------------------------------------------

-- 张三（会员等级：银卡）预订301 VIP房（room_id=5）
INSERT INTO `tb_booking` (`guest_id`, `room_id`, `planned_checkin`, `planned_checkout`, `status`, `deposit_amount`, `discount_rate`) VALUES
(1, 5, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 'CONFIRMED', 500.00, 0.95);

-- 李四（会员等级：铜卡）预订202双人房（room_id=2）
INSERT INTO `tb_booking` (`guest_id`, `room_id`, `planned_checkin`, `planned_checkout`, `status`, `deposit_amount`, `discount_rate`) VALUES
(2, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'PENDING', 250.00, 1.00);

-- --------------------------------------------
-- 5. 硬件状态数据（为已有房间初始化）
-- --------------------------------------------

INSERT INTO `tb_hardware_status` (`room_id`, `cpu_temp`, `gpu_temp`, `network_latency`, `peripheral_status`, `health_level`) VALUES
(1, 55.2, 58.6, 25, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(2, 52.8, 56.3, 22, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(3, 54.5, 57.9, 28, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(4, 56.1, 59.2, 24, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(5, 58.3, 61.5, 30, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(6, 53.7, 55.8, 21, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(7, 57.9, 60.4, 27, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN'),
(8, 54.2, 58.1, 23, '{"keyboard":true, "mouse":true, "display":true, "headset":true}', 'GREEN');

-- --------------------------------------------
-- 6. 入住记录（模拟已入住的情况）
-- --------------------------------------------

-- 王五（金卡会员）已入住302五黑房（room_id=6）
INSERT INTO `tb_checkin_record` (`room_id`, `guest_id`, `actual_checkin`, `expected_checkout`, `is_gaming_auth_active`, `payment_status`) VALUES
(6, 3, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 16 HOUR), 1, 'UNPAID');

-- 更新房间状态为已入住
UPDATE `tb_room` SET `status` = 'OCCUPIED', `current_occupancy` = 1 WHERE `room_id` = 6;

-- --------------------------------------------
-- 7. POS订单数据（为已入住的王五创建订单）
-- --------------------------------------------

-- 王五点了零食和饮料
INSERT INTO `tb_pos_order` (`record_id`, `order_no`, `order_type`, `status`, `total_amount`) VALUES
(1, CONCAT('POS', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '001'), 'PURCHASE', 'DELIVERED', 32.50);

-- 订单明细
INSERT INTO `tb_order_item` (`order_id`, `product_id`, `quantity`, `unit_price`, `subtotal`) VALUES
(1, 1, 2, 6.00, 12.00),   -- 可比克薯片 x2
(1, 7, 3, 3.50, 10.50),   -- 可口可乐 x3
(1, 9, 1, 6.00, 6.00),    -- 红牛 x1
(1, 5, 1, 3.50, 3.50);    -- 卫龙辣条 x1

-- 标记订单为已配送
UPDATE `tb_pos_order` SET `delivery_time` = NOW() WHERE `order_id` = 1;

-- --------------------------------------------
-- 8. 积分商城额外商品
-- --------------------------------------------

INSERT INTO `tb_points_product` (`product_name`, `product_type`, `points_required`, `stock`, `is_available`) VALUES
('Free 1 Night Extension', 'SERVICE_PRIVILEGE', 5000, 99, 1),
('VIP Room Upgrade', 'SERVICE_PRIVILEGE', 3000, 50, 1),
('Logitech G502 Mouse', 'PHYSICAL', 8000, 10, 1),
('Razer Mechanical Keyboard', 'PHYSICAL', 12000, 8, 1),
('50 RMB Voucher', 'SERVICE_PRIVILEGE', 4500, 100, 1),
('Priority Check-in', 'SERVICE_PRIVILEGE', 1500, 99, 1);

-- --------------------------------------------
-- 9. 任务模板数据
-- --------------------------------------------

INSERT INTO `tb_task` (`task_name`, `description`, `task_type`, `reward_points`, `reward_exp`, `condition_config`, `is_repeatable`, `is_active`) VALUES
('Stay 4 Hours', 'Stay in the hotel for 4 consecutive hours', 'AUTO', 50, 20, '{"type":"playtime", "value":240}', 0, 1),
('Pentakill Screenshot', 'Upload pentakill screenshot', 'MANUAL_AUDIT', 100, 50, '{"type":"screenshot", "keyword":"pentakill"}', 0, 1),
('5-Star Review', 'Give a 5-star review', 'AUTO', 200, 100, '{"type":"review", "min_score":5}', 0, 1);

-- --------------------------------------------
-- 10. 维修工单（示例）
-- --------------------------------------------

-- 203房间有一个已关闭的维修工单
INSERT INTO `tb_maintenance_ticket` (`room_id`, `reporter_id`, `request_type`, `description`, `priority`, `status`, `assigned_to`, `resolve_time`, `resolution_notes`, `cost`) VALUES
(3, 5, 'REPAIR', 'Keyboard keys malfunction', 'HIGH', 'RESOLVED', 5, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'Replaced with new Logitech G413', 389.00);

-- 204房间有一个进行中的维修工单
INSERT INTO `tb_maintenance_ticket` (`room_id`, `reporter_id`, `request_type`, `description`, `priority`, `status`, `assigned_to`) VALUES
(4, 5, 'REPAIR', 'Monitor flickering issue', 'MEDIUM', 'IN_PROGRESS', 5);

-- --------------------------------------------
-- 11. 任务完成记录（为王五添加）
-- --------------------------------------------

-- 王五已经入住超过4小时，自动完成任务1
INSERT INTO `tb_task_record` (`task_id`, `guest_id`, `record_id`, `audit_status`) VALUES
(1, 3, 1, 'APPROVED');

-- 添加积分流水记录
INSERT INTO `tb_point_transaction` (`guest_id`, `amount`, `transaction_type`, `related_id`, `description`, `balance_after`) VALUES
(3, 50, 'TASK_REWARD', 1, 'Task completed: 4 hours stay', 5050);

-- 更新王五的积分余额
UPDATE `tb_guest` SET `current_points` = 5050, `experience_points` = 6020 WHERE `guest_id` = 3;

-- --------------------------------------------
-- 12. 硬件日志数据（时序数据示例）
-- --------------------------------------------

-- 为302房间添加最近1小时的监控数据
INSERT INTO `tb_device_log` (`room_id`, `cpu_temp`, `gpu_temp`, `network_latency`, `log_time`) VALUES
(6, 53.2, 55.8, 21, DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
(6, 54.5, 57.2, 23, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(6, 55.8, 58.9, 25, DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(6, 57.1, 60.3, 27, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(6, 58.3, 61.5, 30, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(6, 56.9, 59.8, 28, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(6, 55.6, 58.4, 24, NOW());

-- ============================================
-- 数据插入完成
-- ============================================

-- 统计信息查询
SELECT '========== 数据插入统计 ==========' AS '';
SELECT '用户数量' AS '类别', COUNT(*) AS '数量' FROM tb_user
UNION ALL
SELECT '住客数量', COUNT(*) FROM tb_guest
UNION ALL
SELECT '员工数量', COUNT(*) FROM tb_staff
UNION ALL
SELECT '房间数量', COUNT(*) FROM tb_room
UNION ALL
SELECT '商品数量', COUNT(*) FROM tb_product
UNION ALL
SELECT '预订数量', COUNT(*) FROM tb_booking
UNION ALL
SELECT '入住记录', COUNT(*) FROM tb_checkin_record
UNION ALL
SELECT 'POS订单', COUNT(*) FROM tb_pos_order
UNION ALL
SELECT '任务模板', COUNT(*) FROM tb_task
UNION ALL
SELECT '积分商品', COUNT(*) FROM tb_points_product
UNION ALL
SELECT '维修工单', COUNT(*) FROM tb_maintenance_ticket;

SELECT '' AS '';
SELECT '========== 测试账号信息 ==========' AS '';
SELECT '账号类型' AS '类型', '用户名' AS '用户名', '密码' AS '密码', '说明' AS '说明'
UNION ALL
SELECT 'GUEST', '13800138001', '123456', '银卡会员（张三），有积分'
UNION ALL
SELECT 'GUEST', '13800138002', '123456', '铜卡会员（李四），新用户'
UNION ALL
SELECT 'GUEST', '13800138003', '123456', '金卡会员（王五），已入住302房间'
UNION ALL
SELECT 'STAFF', 'staff001', '123456', '前台接待员'
UNION ALL
SELECT 'STAFF', 'staff002', '123456', '维修人员'
UNION ALL
SELECT 'STAFF', 'staff003', '123456', '运营经理'
UNION ALL
SELECT 'ADMIN', 'admin', '123456', '系统管理员';
