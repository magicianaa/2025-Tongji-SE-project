-- ==========================================
-- 清理积分商品与普通商品的重复数据
-- ==========================================
-- 问题说明：
-- 1. tb_product 表内有大量重复的商品名称
-- 2. tb_points_product 表内也有重复的商品名称
-- 3. 两个表之间也存在重复商品
-- 4. tb_product 有外键约束（tb_order_item, tb_procurement, tb_supply_usage）
-- 解决方案：
-- 1. 更新外键引用，将重复记录的引用指向保留的记录
-- 2. 删除重复记录
-- ==========================================

-- 步骤0：临时禁用外键检查（谨慎使用）
SET FOREIGN_KEY_CHECKS = 0;

-- 步骤1：清理 tb_product 表内重复数据
-- 策略：保留每个商品名称的最小 product_id，删除其他重复记录

-- 1.1 先更新外键引用（如果需要）
-- 将 tb_order_item 中的重复商品ID更新为最小ID
UPDATE tb_order_item oi
SET oi.product_id = (
    SELECT MIN(p.product_id) 
    FROM tb_product p 
    WHERE p.product_name = (
        SELECT p2.product_name 
        FROM tb_product p2 
        WHERE p2.product_id = oi.product_id
    )
)
WHERE oi.product_id NOT IN (
    SELECT * FROM (
        SELECT MIN(product_id) FROM tb_product GROUP BY product_name
    ) AS temp
);

-- 将 tb_procurement 中的重复商品ID更新为最小ID
UPDATE tb_procurement pr
SET pr.product_id = (
    SELECT MIN(p.product_id) 
    FROM tb_product p 
    WHERE p.product_name = (
        SELECT p2.product_name 
        FROM tb_product p2 
        WHERE p2.product_id = pr.product_id
    )
)
WHERE pr.product_id NOT IN (
    SELECT * FROM (
        SELECT MIN(product_id) FROM tb_product GROUP BY product_name
    ) AS temp
);

-- 将 tb_supply_usage 中的重复商品ID更新为最小ID
UPDATE tb_supply_usage su
SET su.product_id = (
    SELECT MIN(p.product_id) 
    FROM tb_product p 
    WHERE p.product_name = (
        SELECT p2.product_name 
        FROM tb_product p2 
        WHERE p2.product_id = su.product_id
    )
)
WHERE su.product_id NOT IN (
    SELECT * FROM (
        SELECT MIN(product_id) FROM tb_product GROUP BY product_name
    ) AS temp
);

-- 1.2 删除 tb_product 中的重复记录（保留最小ID）
DELETE FROM tb_product
WHERE product_id NOT IN (
    SELECT * FROM (
        SELECT MIN(product_id)
        FROM tb_product
        GROUP BY product_name
    ) AS temp
);

-- 步骤2：清理 tb_points_product 表内重复数据
-- 保留每个商品名称的最小 points_product_id，删除其他重复记录

DELETE FROM tb_points_product
WHERE points_product_id NOT IN (
    SELECT * FROM (
        SELECT MIN(points_product_id)
        FROM tb_points_product
        GROUP BY product_name
    ) AS temp
);

-- 步骤3：清理两个表之间的重复商品
-- 删除 tb_points_product 中与 tb_product 重复的商品

DELETE FROM tb_points_product
WHERE product_name IN (
    SELECT product_name FROM tb_product
);

-- 步骤4：重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 验证清理结果
-- ==========================================

-- 1. 检查 tb_product 表内是否还有重复
SELECT '========== tb_product 表内重复检查 ==========' AS check_info;
SELECT product_name, COUNT(*) as count 
FROM tb_product 
GROUP BY product_name 
HAVING count > 1;

-- 2. 检查 tb_points_product 表内是否还有重复
SELECT '========== tb_points_product 表内重复检查 ==========' AS check_info;
SELECT product_name, COUNT(*) as count 
FROM tb_points_product 
GROUP BY product_name 
HAVING count > 1;

-- 3. 检查两个表之间是否还有重复
SELECT '========== tb_product 与 tb_points_product 表间重复检查 ==========' AS check_info;
SELECT COUNT(*) as duplicate_count
FROM tb_product p 
INNER JOIN tb_points_product pp ON p.product_name = pp.product_name;

-- 4. 显示清理后的统计信息
SELECT '========== 清理后统计信息 ==========' AS check_info;
SELECT 
    (SELECT COUNT(*) FROM tb_product) as product_count,
    (SELECT COUNT(DISTINCT product_name) FROM tb_product) as unique_product_names,
    (SELECT COUNT(*) FROM tb_points_product) as points_product_count,
    (SELECT COUNT(DISTINCT product_name) FROM tb_points_product) as unique_points_product_names;

-- 预期结果：
-- 1. tb_product 表内无重复（查询应返回空）
-- 2. tb_points_product 表内无重复（查询应返回空）
-- 3. 两表之间无重复（duplicate_count = 0）
-- 4. 每个表的记录数等于唯一名称数
