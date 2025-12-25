-- =============================================
-- 清扫物资管理功能迁移脚本
-- =============================================

-- 1. 修改商品类型，添加"其他"类别
ALTER TABLE `tb_product` 
MODIFY COLUMN `product_type` ENUM('SNACK', 'BEVERAGE', 'PERIPHERAL', 'OTHER') NOT NULL COMMENT '商品类型';

-- 2. 添加物品使用寿命配置字段（用于床单、被子等可重复使用物品）
ALTER TABLE `tb_product`
ADD COLUMN `max_usage_times` INT DEFAULT NULL COMMENT '最大使用次数（用于床单、被子等可重复使用物品）',
ADD COLUMN `current_usage_times` INT DEFAULT 0 COMMENT '当前已使用次数（用于跟踪个别物品）';

-- 3. 创建清扫记录表
CREATE TABLE IF NOT EXISTS `tb_cleaning_record` (
  `cleaning_id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL COMMENT '房间ID',
  `staff_id` BIGINT NOT NULL COMMENT '清扫员工ID',
  `cleaning_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '清扫完成时间',
  `cleaning_type` ENUM('CHECKOUT', 'DAILY', 'DEEP') DEFAULT 'CHECKOUT' COMMENT '清扫类型：退房清扫、日常清扫、深度清扫',
  `notes` TEXT DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`cleaning_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_cleaning_time` (`cleaning_time`),
  CONSTRAINT `fk_cleaning_room` FOREIGN KEY (`room_id`) REFERENCES `tb_room` (`room_id`),
  CONSTRAINT `fk_cleaning_staff` FOREIGN KEY (`staff_id`) REFERENCES `tb_staff` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='清扫记录表';

-- 4. 创建物资消耗记录表（记录清扫时消耗的一次性物品）
CREATE TABLE IF NOT EXISTS `tb_supply_usage` (
  `usage_id` BIGINT NOT NULL AUTO_INCREMENT,
  `cleaning_id` BIGINT NOT NULL COMMENT '关联清扫记录',
  `product_id` BIGINT NOT NULL COMMENT '物资ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '消耗数量',
  `usage_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消耗时间',
  PRIMARY KEY (`usage_id`),
  KEY `idx_cleaning_id` (`cleaning_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_usage_cleaning` FOREIGN KEY (`cleaning_id`) REFERENCES `tb_cleaning_record` (`cleaning_id`),
  CONSTRAINT `fk_usage_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资消耗记录表';

-- 5. 创建进货记录表
CREATE TABLE IF NOT EXISTS `tb_procurement` (
  `procurement_id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL COMMENT '进货数量',
  `unit_cost` DECIMAL(10, 2) NOT NULL COMMENT '进货单价',
  `total_cost` DECIMAL(10, 2) NOT NULL COMMENT '总成本',
  `supplier` VARCHAR(128) DEFAULT NULL COMMENT '供应商',
  `procurement_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '进货时间',
  `operator_id` BIGINT NOT NULL COMMENT '操作员工ID',
  `notes` TEXT DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`procurement_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_procurement_time` (`procurement_time`),
  KEY `idx_operator_id` (`operator_id`),
  CONSTRAINT `fk_procurement_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`),
  CONSTRAINT `fk_procurement_operator` FOREIGN KEY (`operator_id`) REFERENCES `tb_staff` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进货记录表';

-- 6. 插入示例"其他"类别商品数据
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`) VALUES
('一次性牙刷', 'OTHER', '一次性用品', 2.00, 'NONE', 500, 50, '一次性牙刷套装', 1, NULL),
('一次性拖鞋', 'OTHER', '一次性用品', 3.00, 'NONE', 300, 30, '一次性无纺布拖鞋', 1, NULL),
('一次性毛巾', 'OTHER', '一次性用品', 5.00, 'NONE', 200, 20, '一次性纯棉毛巾', 1, NULL),
('床单', 'OTHER', '可重复使用', 80.00, 'NONE', 50, 10, '纯棉床单，可重复使用', 1, 30),
('被套', 'OTHER', '可重复使用', 120.00, 'NONE', 50, 10, '纯棉被套，可重复使用', 1, 30),
('枕套', 'OTHER', '可重复使用', 25.00, 'NONE', 100, 20, '纯棉枕套，可重复使用', 1, 50),
('浴巾', 'OTHER', '可重复使用', 60.00, 'NONE', 80, 15, '纯棉浴巾，可重复使用', 1, 40);
