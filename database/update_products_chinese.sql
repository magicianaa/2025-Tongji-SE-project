-- 更新商品数据为中文（UTF-8编码）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 更新现有商品为中文名称
UPDATE `tb_product` SET `product_name` = '乐事薯片（原味）', `category` = '膨化食品', `description` = '乐事原味薯片 70g' WHERE `product_id` = 1;
UPDATE `tb_product` SET `product_name` = '乐事薯片（黄瓜味）', `category` = '膨化食品', `description` = '乐事黄瓜味薯片 70g' WHERE `product_id` = 2;
UPDATE `tb_product` SET `product_name` = '奥利奥饼干', `category` = '饼干', `description` = '奥利奥夹心饼干' WHERE `product_id` = 3;
UPDATE `tb_product` SET `product_name` = '德芙巧克力', `category` = '糖果巧克力', `description` = '德芙丝滑牛奶巧克力 80g' WHERE `product_id` = 4;
UPDATE `tb_product` SET `product_name` = '卫龙辣条', `category` = '辣味零食', `description` = '卫龙亲嘴烧辣条' WHERE `product_id` = 5;
UPDATE `tb_product` SET `product_name` = '可口可乐', `category` = '碳酸饮料', `description` = '经典可口可乐 330ml' WHERE `product_id` = 6;
UPDATE `tb_product` SET `product_name` = '雪碧', `category` = '碳酸饮料', `description` = '清爽雪碧 330ml' WHERE `product_id` = 7;
UPDATE `tb_product` SET `product_name` = '红牛', `category` = '功能饮料', `description` = '红牛能量饮料 250ml' WHERE `product_id` = 8;

-- 插入其他类型商品（清扫用品）- 如果不存在
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '一次性牙刷' AS `product_name`, 'OTHER' AS `product_type`, '洗漱用品' AS `category`, 2.00 AS `price`, 'NONE' AS `rental_unit`, 100 AS `stock_quantity`, 20 AS `stock_threshold`, '独立包装一次性牙刷牙膏套装' AS `description`, 1 AS `is_available`, NULL AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '一次性牙刷');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '一次性拖鞋' AS `product_name`, 'OTHER' AS `product_type`, '洗漱用品' AS `category`, 3.00 AS `price`, 'NONE' AS `rental_unit`, 80 AS `stock_quantity`, 20 AS `stock_threshold`, '无纺布一次性拖鞋' AS `description`, 1 AS `is_available`, NULL AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '一次性拖鞋');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '毛巾' AS `product_name`, 'OTHER' AS `product_type`, '布草用品' AS `category`, 15.00 AS `price`, 'NONE' AS `rental_unit`, 50 AS `stock_quantity`, 10 AS `stock_threshold`, '纯棉毛巾 35x75cm' AS `description`, 1 AS `is_available`, 50 AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '毛巾');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '床单' AS `product_name`, 'OTHER' AS `product_type`, '布草用品' AS `category`, 80.00 AS `price`, 'NONE' AS `rental_unit`, 30 AS `stock_quantity`, 5 AS `stock_threshold`, '纯棉床单 200x230cm' AS `description`, 1 AS `is_available`, 30 AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '床单');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '被套' AS `product_name`, 'OTHER' AS `product_type`, '布草用品' AS `category`, 120.00 AS `price`, 'NONE' AS `rental_unit`, 30 AS `stock_quantity`, 5 AS `stock_threshold`, '纯棉被套 200x230cm' AS `description`, 1 AS `is_available`, 30 AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '被套');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '枕套' AS `product_name`, 'OTHER' AS `product_type`, '布草用品' AS `category`, 20.00 AS `price`, 'NONE' AS `rental_unit`, 60 AS `stock_quantity`, 10 AS `stock_threshold`, '纯棉枕套 48x74cm' AS `description`, 1 AS `is_available`, 40 AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '枕套');

INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`, `max_usage_times`, `current_usage_times`) 
SELECT * FROM (
  SELECT '消毒液' AS `product_name`, 'OTHER' AS `product_type`, '清洁用品' AS `category`, 8.00 AS `price`, 'NONE' AS `rental_unit`, 40 AS `stock_quantity`, 10 AS `stock_threshold`, '84消毒液 500ml' AS `description`, 1 AS `is_available`, NULL AS `max_usage_times`, 0 AS `current_usage_times`
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `tb_product` WHERE `product_name` = '消毒液');
