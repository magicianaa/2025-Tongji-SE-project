-- ============================================
-- 房间计费方式修改 - 数据库迁移脚本
-- 修改日期: 2025-12-18
-- ============================================

USE esports_hotel_db;

-- 1. 修改房间表：将price_per_hour改为price_per_day
ALTER TABLE `tb_room` 
CHANGE COLUMN `price_per_hour` `price_per_day` DECIMAL(10, 2) NOT NULL 
COMMENT '每天价格';

-- 2. 更新现有房间价格（小时价格 * 24 = 天价格）
-- 注意：如果已经执行过此更新，请跳过此步骤
-- UPDATE `tb_room` SET `price_per_day` = `price_per_day` * 24;

-- 3. 验证修改结果
SELECT 
    room_no,
    room_type,
    price_per_day,
    status
FROM tb_room 
ORDER BY room_no;

-- 4. 查看字段定义
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_room' 
  AND COLUMN_NAME = 'price_per_day';

-- 迁移完成提示
SELECT '房间计费方式已修改为按天计费' AS status;

-- ============================================
-- 变更说明:
-- 1. 房间计费从按小时改为按天计费
-- 2. 计费规则：进1制，不满1天按1天计算
-- 3. 新增账单管理功能：
--    - GET  /api/billing/detail/{recordId} - 获取账单详情
--    - POST /api/billing/settle/{recordId} - 账单清付
-- 4. 账单包含：房费 + 当前房间所有住客的POS订单消费
-- ============================================
