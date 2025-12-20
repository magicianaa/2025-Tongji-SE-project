-- ============================================
-- 智慧电竞酒店管理系统 - 数据库设计 (MySQL 8.0)
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------
-- 1. 用户认证与权限模块
-- --------------------------------------------

-- 用户基础表（抽象父表）
CREATE TABLE `tb_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名（手机号）',
  `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt加密后的密码',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（需加密存储）',
  `user_type` ENUM('GUEST', 'STAFF', 'ADMIN') NOT NULL COMMENT '用户类型',
  `status` ENUM('ACTIVE', 'DISABLED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '账号状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';

-- 住客扩展表
CREATE TABLE `tb_guest` (
  `guest_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关联用户表',
  `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名（入住时绑定）',
  `identity_card` VARCHAR(255) DEFAULT NULL COMMENT '身份证号（入住时绑定，AES加密）',
  `member_level` ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE' COMMENT '会员等级',
  `experience_points` INT DEFAULT 0 COMMENT '经验值（用于升级）',
  `current_points` INT DEFAULT 0 COMMENT '可用积分余额',
  `total_checkin_nights` INT DEFAULT 0 COMMENT '累计入住天数',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`guest_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_guest_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住客信息表（身份信息在首次入住时绑定）';

-- 员工扩展表
CREATE TABLE `tb_staff` (
  `staff_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `employee_id` VARCHAR(32) NOT NULL COMMENT '工号',
  `role` ENUM('RECEPTIONIST', 'MANAGER', 'MAINTENANCE', 'ADMIN') NOT NULL COMMENT '角色',
  `department` VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_employee_id` (`employee_id`),
  CONSTRAINT `fk_staff_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- --------------------------------------------
-- 2. 客房与入住管理模块 (PMS)
-- --------------------------------------------

-- 房间主表
CREATE TABLE `tb_room` (
  `room_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_no` VARCHAR(16) NOT NULL COMMENT '房间号（如301）',
  `room_type` ENUM('SINGLE', 'DOUBLE', 'FIVE_PLAYER', 'VIP') NOT NULL COMMENT '房型',
  `floor` TINYINT NOT NULL COMMENT '楼层',
  `status` ENUM('VACANT', 'OCCUPIED', 'CLEANING', 'MAINTENANCE') DEFAULT 'VACANT' COMMENT '房态',
  `price_per_day` DECIMAL(10, 2) NOT NULL COMMENT '每天价格',
  `max_occupancy` TINYINT NOT NULL DEFAULT 1 COMMENT '最大容纳人数（单人1、双人2、五黑5）',
  `current_occupancy` TINYINT NOT NULL DEFAULT 0 COMMENT '当前入住人数',
  `facility_config` JSON DEFAULT NULL COMMENT '设施配置（如：{"display":"4K", "chair":"赛车椅"}）',
  `is_premium` TINYINT(1) DEFAULT 0 COMMENT '是否高级房型（需高等级会员预订）',
  PRIMARY KEY (`room_id`),
  UNIQUE KEY `uk_room_no` (`room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客房基础信息表';

-- 预订表
CREATE TABLE `tb_booking` (
  `booking_id` BIGINT NOT NULL AUTO_INCREMENT,
  `guest_id` BIGINT NOT NULL,
  `room_id` BIGINT NOT NULL,
  `booking_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预订时间',
  `planned_checkin` DATETIME NOT NULL COMMENT '计划入住时间',
  `planned_checkout` DATETIME NOT NULL COMMENT '计划离店时间',
  `status` ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CANCELLED') DEFAULT 'PENDING',
  `deposit_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '预付押金',
  `discount_rate` DECIMAL(3, 2) DEFAULT 1.00 COMMENT '折扣率（会员享受）',
  `main_guest_name` VARCHAR(50) DEFAULT NULL COMMENT '主住客姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `special_requests` TEXT DEFAULT NULL COMMENT '特殊要求',
  PRIMARY KEY (`booking_id`),
  KEY `idx_guest_id` (`guest_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_planned_checkin` (`planned_checkin`),
  CONSTRAINT `fk_booking_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`),
  CONSTRAINT `fk_booking_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预订记录表';

-- 入住记录表（核心：二次鉴权的验证依据）
CREATE TABLE `tb_checkin_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT DEFAULT NULL COMMENT '关联预订（Walk-in时为NULL）',
  `room_id` BIGINT NOT NULL,
  `guest_id` BIGINT NOT NULL COMMENT '主住客ID',
  `actual_checkin` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '实际入住时间',
  `actual_checkout` DATETIME DEFAULT NULL COMMENT '实际退房时间',
  `expected_checkout` DATETIME NOT NULL COMMENT '预期退房时间',
  `is_gaming_auth_active` TINYINT(1) DEFAULT 1 COMMENT '是否拥有客房权限（二次鉴权状态）',
  `room_fee` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '房费小计',
  `damage_compensation` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '赔偿金',
  `points_deduction` INT DEFAULT 0 COMMENT '使用积分抵扣金额',
  `final_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '最终应付金额',
  `payment_status` ENUM('UNPAID', 'PAID') DEFAULT 'UNPAID',
  `payment_method` ENUM('CASH', 'WECHAT', 'ALIPAY', 'CARD') DEFAULT NULL,
  `notes` TEXT DEFAULT NULL COMMENT '备注（如换房记录）',
  PRIMARY KEY (`record_id`),
  KEY `idx_guest_id` (`guest_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_actual_checkin` (`actual_checkin`),
  KEY `idx_is_gaming_auth` (`is_gaming_auth_active`),
  CONSTRAINT `fk_checkin_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`),
  CONSTRAINT `fk_checkin_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`),
  CONSTRAINT `fk_checkin_booking` FOREIGN KEY (`booking_id`) REFERENCES `tb_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入住记录表（二次鉴权核心表）';

-- 换房历史表
CREATE TABLE `tb_room_change_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL COMMENT '关联入住记录',
  `old_room_id` BIGINT NOT NULL,
  `new_room_id` BIGINT NOT NULL,
  `reason` VARCHAR(255) NOT NULL COMMENT '换房原因（如：硬件故障）',
  `change_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `operator_id` BIGINT NOT NULL COMMENT '操作员工ID',
  PRIMARY KEY (`log_id`),
  KEY `idx_record_id` (`record_id`),
  CONSTRAINT `fk_change_record` FOREIGN KEY (`record_id`) REFERENCES `tb_checkin_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换房操作日志';

-- --------------------------------------------
-- 3. 销售点模块 (POS)
-- --------------------------------------------

-- 商品主表
CREATE TABLE `tb_product` (
  `product_id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_name` VARCHAR(128) NOT NULL,
  `product_type` ENUM('SNACK', 'BEVERAGE', 'PERIPHERAL') NOT NULL COMMENT '商品类型',
  `category` VARCHAR(64) DEFAULT NULL COMMENT '分类（如：键盘、鼠标）',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '售价/租赁单价',
  `rental_unit` ENUM('NONE', 'HOURLY', 'DAILY', 'PER_TIME') DEFAULT 'NONE' COMMENT '租赁计费单位',
  `stock_quantity` INT DEFAULT 0 COMMENT '当前库存',
  `stock_threshold` INT DEFAULT 5 COMMENT '库存预警阈值',
  `image_url` VARCHAR(255) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `is_available` TINYINT(1) DEFAULT 1 COMMENT '是否上架',
  PRIMARY KEY (`product_id`),
  KEY `idx_product_type` (`product_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

-- POS订单表
CREATE TABLE `tb_pos_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL COMMENT '关联入住记录（挂账依据）',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `order_type` ENUM('PURCHASE', 'RENTAL') NOT NULL COMMENT '订单类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status` ENUM('PENDING', 'DELIVERED', 'RETURNED', 'CANCELLED') DEFAULT 'PENDING',
  `total_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '订单总金额',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '配送完成时间',
  `return_time` DATETIME DEFAULT NULL COMMENT '归还时间（仅租赁）',
  `operator_id` BIGINT DEFAULT NULL COMMENT '配送员工ID',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_record_id` (`record_id`),
  CONSTRAINT `fk_pos_order_record` FOREIGN KEY (`record_id`) REFERENCES `tb_checkin_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POS订单表';

-- 订单明细表
CREATE TABLE `tb_order_item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL COMMENT '数量',
  `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '单价快照',
  `subtotal` DECIMAL(10, 2) NOT NULL COMMENT '小计',
  `rental_start` DATETIME DEFAULT NULL COMMENT '租赁开始时间',
  `rental_end` DATETIME DEFAULT NULL COMMENT '租赁结束时间',
  `damage_status` ENUM('NORMAL', 'DAMAGED') DEFAULT 'NORMAL' COMMENT '归还时设备状态',
  PRIMARY KEY (`item_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `tb_pos_order` (`order_id`),
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- --------------------------------------------
-- 4. 硬件模拟与监控模块
-- --------------------------------------------

-- 硬件状态表（实时数据，高频更新）
CREATE TABLE `tb_hardware_status` (
  `status_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `cpu_temp` FLOAT DEFAULT NULL COMMENT 'CPU温度（°C）',
  `gpu_temp` FLOAT DEFAULT NULL COMMENT 'GPU温度（°C）',
  `network_latency` INT DEFAULT NULL COMMENT '网络延迟（ms）',
  `peripheral_status` JSON DEFAULT NULL COMMENT '外设连接状态（如：{"keyboard":true, "mouse":true}）',
  `health_level` ENUM('GREEN', 'YELLOW', 'RED') DEFAULT 'GREEN' COMMENT '健康等级',
  `last_update` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`status_id`),
  UNIQUE KEY `uk_room_id` (`room_id`),
  CONSTRAINT `fk_hardware_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件实时状态表';

-- 硬件日志表（归档数据，用于趋势分析）
CREATE TABLE `tb_device_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `cpu_temp` FLOAT DEFAULT NULL,
  `gpu_temp` FLOAT DEFAULT NULL,
  `network_latency` INT DEFAULT NULL,
  `log_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_room_time` (`room_id`, `log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件监控日志（时序数据）';

-- 维修工单表
CREATE TABLE `tb_maintenance_ticket` (
  `ticket_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `reporter_id` BIGINT DEFAULT NULL COMMENT '报修人（住客/员工）',
  `request_type` ENUM('REPAIR', 'CHANGE_ROOM') NOT NULL COMMENT '诉求类型',
  `description` TEXT NOT NULL COMMENT '故障描述',
  `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
  `status` ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
  `assigned_to` BIGINT DEFAULT NULL COMMENT '指派维修员工',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `resolve_time` DATETIME DEFAULT NULL,
  `resolution_notes` TEXT DEFAULT NULL COMMENT '处理结果',
  `cost` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '维修成本（用于损耗分析）',
  PRIMARY KEY (`ticket_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_ticket_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单表';

-- 报警记录表
CREATE TABLE `tb_alert_log` (
  `alert_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `alert_type` ENUM('OVERHEAT', 'NETWORK_FAIL', 'DEVICE_OFFLINE') NOT NULL,
  `alert_level` ENUM('WARNING', 'CRITICAL') NOT NULL,
  `trigger_value` VARCHAR(64) DEFAULT NULL COMMENT '触发值（如：98°C）',
  `alert_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_handled` TINYINT(1) DEFAULT 0 COMMENT '是否已处理',
  `handler_id` BIGINT DEFAULT NULL,
  `handle_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`alert_id`),
  KEY `idx_room_time` (`room_id`, `alert_time`),
  CONSTRAINT `fk_alert_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件报警日志';

-- --------------------------------------------
-- 5. 社交匹配模块
-- --------------------------------------------

-- 游戏档案表（入住期间有效）
CREATE TABLE `tb_gaming_profile` (
  `profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `guest_id` BIGINT NOT NULL,
  `record_id` BIGINT NOT NULL COMMENT '关联本次入住记录',
  `game_type` VARCHAR(64) NOT NULL COMMENT '游戏名称（如：LOL）',
  `game_account` VARCHAR(128) DEFAULT NULL COMMENT '游戏账号',
  `rank` VARCHAR(64) DEFAULT NULL COMMENT '段位（如：黄金IV）',
  `preferred_position` VARCHAR(64) DEFAULT NULL COMMENT '擅长位置（如：辅助）',
  `play_style` VARCHAR(64) DEFAULT NULL COMMENT '风格标签（如：稳健型）',
  `is_looking_for_team` TINYINT(1) DEFAULT 1 COMMENT '是否寻找队友',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`profile_id`),
  KEY `idx_guest_record` (`guest_id`, `record_id`),
  KEY `idx_rank` (`rank`),
  CONSTRAINT `fk_profile_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`),
  CONSTRAINT `fk_profile_record` FOREIGN KEY (`record_id`) REFERENCES `tb_checkin_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏档案表（入住期间）';

-- 招募信息表
CREATE TABLE `tb_recruitment` (
  `recruitment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `publisher_id` BIGINT NOT NULL COMMENT '发布人',
  `game_type` VARCHAR(64) NOT NULL,
  `required_rank` VARCHAR(64) DEFAULT NULL COMMENT '要求段位',
  `required_position` VARCHAR(64) DEFAULT NULL COMMENT '缺位置',
  `description` VARCHAR(255) DEFAULT NULL,
  `max_members` TINYINT DEFAULT 5,
  `status` ENUM('OPEN', 'FULL', 'CLOSED') DEFAULT 'OPEN',
  `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `expire_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`recruitment_id`),
  KEY `idx_publisher` (`publisher_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_recruitment_guest` FOREIGN KEY (`publisher_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组队招募信息表';

-- 临时战队表
CREATE TABLE `tb_team` (
  `team_id` BIGINT NOT NULL AUTO_INCREMENT,
  `team_name` VARCHAR(64) DEFAULT NULL,
  `captain_id` BIGINT NOT NULL COMMENT '队长',
  `game_type` VARCHAR(64) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `disband_time` DATETIME DEFAULT NULL,
  `status` ENUM('ACTIVE', 'DISBANDED') DEFAULT 'ACTIVE',
  `total_playtime_minutes` INT DEFAULT 0 COMMENT '共同游戏时长（用于积分奖励）',
  PRIMARY KEY (`team_id`),
  KEY `idx_captain` (`captain_id`),
  CONSTRAINT `fk_team_captain` FOREIGN KEY (`captain_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临时战队表';

-- 战队成员关联表
CREATE TABLE `tb_team_member` (
  `member_id` BIGINT NOT NULL AUTO_INCREMENT,
  `team_id` BIGINT NOT NULL,
  `guest_id` BIGINT NOT NULL,
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `leave_time` DATETIME DEFAULT NULL,
  `status` ENUM('ACTIVE', 'LEFT', 'KICKED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`member_id`),
  KEY `idx_team_guest` (`team_id`, `guest_id`),
  CONSTRAINT `fk_member_team` FOREIGN KEY (`team_id`) REFERENCES `tb_team` (`team_id`),
  CONSTRAINT `fk_member_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战队成员表';

-- --------------------------------------------
-- 6. 游戏化与积分模块
-- --------------------------------------------

-- 任务模板表
CREATE TABLE `tb_task` (
  `task_id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_name` VARCHAR(128) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `task_type` ENUM('AUTO', 'MANUAL_AUDIT') NOT NULL COMMENT '自动检测/人工审核',
  `reward_points` INT NOT NULL COMMENT '奖励积分',
  `reward_exp` INT DEFAULT 0 COMMENT '奖励经验值',
  `condition_config` JSON DEFAULT NULL COMMENT '完成条件（如：{"type":"playtime", "value":240}）',
  `is_repeatable` TINYINT(1) DEFAULT 0 COMMENT '是否可重复完成',
  `is_active` TINYINT(1) DEFAULT 1,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

-- 任务完成记录表
CREATE TABLE `tb_task_record` (
  `task_record_id` BIGINT NOT NULL AUTO_INCREMENT,
  `task_id` BIGINT NOT NULL,
  `guest_id` BIGINT NOT NULL,
  `record_id` BIGINT NOT NULL COMMENT '关联入住记录',
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `proof_image_url` VARCHAR(255) DEFAULT NULL COMMENT '凭证截图URL',
  `proof_description` TEXT DEFAULT NULL,
  `audit_status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核员工',
  `audit_time` DATETIME DEFAULT NULL,
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`task_record_id`),
  KEY `idx_guest_task` (`guest_id`, `task_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `fk_task_record_task` FOREIGN KEY (`task_id`) REFERENCES `tb_task` (`task_id`),
  CONSTRAINT `fk_task_record_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务完成记录表';

-- 积分流水表
CREATE TABLE `tb_point_transaction` (
  `transaction_id` BIGINT NOT NULL AUTO_INCREMENT,
  `guest_id` BIGINT NOT NULL,
  `amount` INT NOT NULL COMMENT '积分变动数（正为获得，负为消耗）',
  `transaction_type` ENUM('TASK_REWARD', 'REDEMPTION', 'REFUND', 'ADMIN_ADJUST') NOT NULL,
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID（如任务ID、兑换订单ID）',
  `description` VARCHAR(255) DEFAULT NULL,
  `balance_after` INT NOT NULL COMMENT '变动后余额快照',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `idx_guest_time` (`guest_id`, `create_time`),
  CONSTRAINT `fk_transaction_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- 积分商城商品表
CREATE TABLE `tb_points_product` (
  `points_product_id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_name` VARCHAR(128) NOT NULL,
  `product_type` ENUM('PHYSICAL', 'SERVICE_PRIVILEGE') NOT NULL COMMENT '实物/服务权益',
  `points_required` INT NOT NULL COMMENT '所需积分',
  `stock` INT DEFAULT 0,
  `description` TEXT DEFAULT NULL,
  `image_url` VARCHAR(255) DEFAULT NULL,
  `is_available` TINYINT(1) DEFAULT 1,
  `related_product_id` BIGINT DEFAULT NULL COMMENT '关联POS商品ID（实物类）',
  PRIMARY KEY (`points_product_id`),
  KEY `idx_product_type` (`product_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商城商品表';

-- 积分兑换记录表
CREATE TABLE `tb_points_redemption` (
  `redemption_id` BIGINT NOT NULL AUTO_INCREMENT,
  `guest_id` BIGINT NOT NULL,
  `points_product_id` BIGINT NOT NULL,
  `points_cost` INT NOT NULL,
  `redemption_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status` ENUM('PENDING', 'FULFILLED', 'CANCELLED') DEFAULT 'PENDING',
  `fulfillment_notes` TEXT DEFAULT NULL,
  PRIMARY KEY (`redemption_id`),
  KEY `idx_guest_id` (`guest_id`),
  CONSTRAINT `fk_redemption_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`),
  CONSTRAINT `fk_redemption_product` FOREIGN KEY (`points_product_id`) REFERENCES `tb_points_product` (`points_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- --------------------------------------------
-- 7. 评价管理模块
-- --------------------------------------------

-- 评价表
CREATE TABLE `tb_review` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `record_id` BIGINT NOT NULL COMMENT '关联入住记录',
  `guest_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL COMMENT '评分（1-5星）',
  `comment` TEXT DEFAULT NULL COMMENT '文字评价',
  `review_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_low_score` TINYINT(1) GENERATED ALWAYS AS (IF(`score` < 3, 1, 0)) STORED COMMENT '低分标识（虚拟列）',
  `reply` TEXT DEFAULT NULL COMMENT '酒店回复',
  `follow_up_status` ENUM('NONE', 'CONTACTED', 'RESOLVED') DEFAULT 'NONE' COMMENT '回访状态',
  `follow_up_notes` TEXT DEFAULT NULL COMMENT '回访记录',
  `handler_id` BIGINT DEFAULT NULL COMMENT '回访处理人',
  PRIMARY KEY (`review_id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_is_low_score` (`is_low_score`),
  CONSTRAINT `fk_review_record` FOREIGN KEY (`record_id`) REFERENCES `tb_checkin_record` (`record_id`),
  CONSTRAINT `fk_review_guest` FOREIGN KEY (`guest_id`) REFERENCES `tb_guest` (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- --------------------------------------------
-- 8. 系统配置与日志
-- --------------------------------------------

-- 系统配置表
CREATE TABLE `tb_system_config` (
  `config_id` INT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(64) NOT NULL COMMENT '配置键（如：points_to_rmb_rate）',
  `config_value` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
CREATE TABLE `tb_operation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `operation_type` VARCHAR(64) NOT NULL COMMENT '操作类型（如：LOGIN, CHECKIN, UPDATE_ROOM_STATUS）',
  `target_table` VARCHAR(64) DEFAULT NULL COMMENT '操作目标表',
  `target_id` BIGINT DEFAULT NULL,
  `operation_detail` TEXT DEFAULT NULL COMMENT '操作详情（JSON格式）',
  `ip_address` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化基础数据（示例）
-- ============================================

-- 插入系统配置
INSERT INTO `tb_system_config` (`config_key`, `config_value`, `description`) VALUES
('points_to_rmb_rate', '100', '积分兑换人民币汇率（100积分=1元）'),
('hardware_check_interval', '5', '硬件数据采集间隔（秒）'),
('temp_alert_threshold', '95', 'CPU/GPU温度报警阈值（°C）'),
('member_upgrade_bronze_to_silver', '1000', '青铜->白银所需经验值'),
('member_upgrade_silver_to_gold', '5000', '白银->黄金所需经验值');
max_occupancy`, `is_premium`) VALUES
('201', 'SINGLE', 2, 15.00, 1, 0),
('202', 'DOUBLE', 2, 25.00, 2, 0),
('301', 'VIP', 3, 50.00, 2, 1),
('302', 'FIVE_PLAYER', 3, 80.00, 5
('301', 'VIP', 3, 50.00, 1),
('302', 'FIVE_PLAYER', 3, 80.00, 1);

-- 插入示例任务模板
INSERT INTO `tb_task` (`task_name`, `task_type`, `reward_points`, `reward_exp`, `condition_config`) VALUES
('连续入住4小时', 'AUTO', 50, 20, '{"type":"playtime", "value":240}'),
('上传五杀截图', 'MANUAL_AUDIT', 100, 50, '{"type":"screenshot", "keyword":"pentakill"}'),
('给予五星好评', 'AUTO', 200, 100, '{"type":"review", "min_score":5}');

-- 插入示例积分商品
INSERT INTO `tb_points_product` (`product_name`, `product_type`, `points_required`, `description`) VALUES
('延迟退房1小时', 'SERVICE_PRIVILEGE', 500, '可在原定退房时间基础上延长1小时'),
('房费抵扣券(10元)', 'SERVICE_PRIVILEGE', 1000, '退房时自动抵扣10元房费');
