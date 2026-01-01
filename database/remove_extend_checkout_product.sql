-- 删除积分商城中"延迟退房1小时"和"延迟退房2小时"的商品
-- 将这些商品标记为下架状态（不显示在积分商城中）

-- 方法1：通过商品名称精确匹配（推荐）
UPDATE `tb_points_product` 
SET `is_available` = 0 
WHERE `product_name` IN ('延迟退房1小时', '延迟退房2小时');

-- 方法2：通过商品ID精确更新（已执行）
-- 本地数据库：商品ID: 7, 8, 15, 16
-- 服务器数据库：商品ID: 7, 8, 15, 16
UPDATE `tb_points_product` SET `is_available` = 0 WHERE `points_product_id` IN (7, 8, 15, 16);

-- 如果需要完全删除该商品记录，可以使用以下SQL（需先删除相关的兑换记录）
-- DELETE FROM `tb_points_redemption` WHERE `points_product_id` IN (7, 8, 15, 16);
-- DELETE FROM `tb_points_product` WHERE `points_product_id` IN (7, 8, 15, 16);

-- 验证结果
-- SELECT points_product_id, product_name, is_available FROM tb_points_product WHERE product_name LIKE '%延迟退房%';

-- 服务器执行命令示例：
-- ssh root@120.26.18.86 "docker exec esports-hotel-mysql mysql -uroot -proot esports_hotel_db -e 'UPDATE tb_points_product SET is_available = 0 WHERE points_product_id IN (7, 8, 15, 16);'"
