-- 插入测试商品数据

-- 零食
INSERT INTO tb_product (product_name, product_type, category, price, rental_unit, stock_quantity, stock_threshold, description, is_available) 
VALUES 
('薯片-原味', 'SNACK', '膨化食品', 8.00, 'NONE', 15, 5, '乐事原味薯片，经典口味', 1),
('薯片-黄瓜味', 'SNACK', '膨化食品', 8.00, 'NONE', 12, 5, '乐事黄瓜味薯片，清新爽口', 1),
('巧克力', 'SNACK', '糖果巧克力', 15.00, 'NONE', 8, 5, '德芙丝滑牛奶巧克力', 1),
('奥利奥饼干', 'SNACK', '饼干', 10.00, 'NONE', 10, 5, '奥利奥夹心饼干', 1),
('牛肉干', 'SNACK', '肉制品', 25.00, 'NONE', 6, 5, '内蒙古风味牛肉干', 1),
('辣条', 'SNACK', '膨化食品', 5.00, 'NONE', 20, 5, '卫龙亲嘴烧辣条', 1),
('果冻布丁', 'SNACK', '果冻', 6.00, 'NONE', 18, 5, '多种口味果冻组合', 1),
('泡面', 'SNACK', '方便食品', 12.00, 'NONE', 10, 5, '康师傅红烧牛肉面', 1);

-- 饮料
INSERT INTO tb_product (product_name, product_type, category, price, rental_unit, stock_quantity, stock_threshold, description, is_available) 
VALUES 
('可口可乐', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 30, 5, '经典可口可乐 330ml', 1),
('雪碧', 'BEVERAGE', '碳酸饮料', 5.00, 'NONE', 25, 5, '清爽雪碧 330ml', 1),
('红牛', 'BEVERAGE', '功能饮料', 8.00, 'NONE', 20, 5, '红牛能量饮料 250ml', 1),
('脉动', 'BEVERAGE', '功能饮料', 6.00, 'NONE', 22, 5, '脉动维生素饮料 600ml', 1),
('矿泉水', 'BEVERAGE', '饮用水', 3.00, 'NONE', 50, 5, '农夫山泉天然矿泉水 550ml', 1),
('冰红茶', 'BEVERAGE', '茶饮料', 5.00, 'NONE', 28, 5, '统一冰红茶 500ml', 1),
('咖啡', 'BEVERAGE', '咖啡', 10.00, 'NONE', 15, 5, '星巴克即饮咖啡 200ml', 1),
('酸奶', 'BEVERAGE', '乳制品', 8.00, 'NONE', 12, 5, '安慕希希腊酸奶 200g', 1),
('果汁', 'BEVERAGE', '果汁', 7.00, 'NONE', 18, 5, '农夫果园混合果汁 420ml', 1);

-- 外设（库存较少，测试低库存预警）
INSERT INTO tb_product (product_name, product_type, category, price, rental_unit, stock_quantity, stock_threshold, description, is_available) 
VALUES 
('机械键盘', 'PERIPHERAL', '键盘', 20.00, 'NONE', 3, 5, '樱桃轴机械键盘，按次租赁', 1),
('游戏鼠标', 'PERIPHERAL', '鼠标', 15.00, 'NONE', 4, 5, '罗技游戏鼠标，按次租赁', 1),
('游戏耳机', 'PERIPHERAL', '耳机', 15.00, 'NONE', 2, 5, '赛睿7.1环绕声耳机，按次租赁', 1),
('高清摄像头', 'PERIPHERAL', '摄像头', 10.00, 'NONE', 3, 5, '罗技高清摄像头，按次租赁', 1),
('手柄', 'PERIPHERAL', '游戏手柄', 10.00, 'NONE', 6, 5, 'Xbox无线手柄，按次租赁', 1),
('显示器', 'PERIPHERAL', '显示器', 30.00, 'NONE', 2, 5, '27寸2K显示器，按次租赁', 1);

SELECT '商品数据插入完成';

-- 查看插入结果
SELECT product_type, COUNT(*) as count, SUM(stock_quantity) as total_stock 
FROM tb_product 
WHERE is_available = 1
GROUP BY product_type;
