-- 插入商品数据（UTF-8编码）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 插入饮料类商品
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`) VALUES
('可口可乐', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 30, 5, '经典可口可乐 330ml', 1),
('雪碧', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 25, 5, '清爽雪碧 330ml', 1),
('红牛', 'BEVERAGE', '功能饮料', 8.00, 'NONE', 20, 5, '红牛能量饮料 250ml', 1);

-- 插入零食类商品
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`) VALUES
('薯片', 'SNACK', '膨化食品', 8.00, 'NONE', 15, 5, '乐事原味薯片', 1),
('巧克力', 'SNACK', '糖果巧克力', 15.00, 'NONE', 8, 5, '德芙丝滑牛奶巧克力', 1);

-- 插入外设租赁类商品
INSERT INTO `tb_product` (`product_name`, `product_type`, `category`, `price`, `rental_unit`, `stock_quantity`, `stock_threshold`, `description`, `is_available`) VALUES
('机械键盘', 'PERIPHERAL', '键盘', 20.00, 'NONE', 3, 5, '樱桃轴机械键盘，按次租赁', 1),
('游戏鼠标', 'PERIPHERAL', '鼠标', 15.00, 'NONE', 4, 5, '罗技游戏鼠标，按次租赁', 1),
('游戏耳机', 'PERIPHERAL', '耳机', 15.00, 'NONE', 2, 5, '赛睿7.1环绕声耳机，按次租赁', 1);
