-- ============================================
-- 任务1.3：PMS业务逻辑修复 - 数据库迁移脚本
-- ============================================

USE esports_hotel_db;

-- 1. 修改tb_room表：添加容纳人数字段
ALTER TABLE `tb_room` 
ADD COLUMN `max_occupancy` TINYINT NOT NULL DEFAULT 1 COMMENT '最大容纳人数' AFTER `price_per_hour`,
ADD COLUMN `current_occupancy` TINYINT NOT NULL DEFAULT 0 COMMENT '当前入住人数' AFTER `max_occupancy`;

-- 2. 更新现有房间的max_occupancy字段
UPDATE `tb_room` SET `max_occupancy` = 1 WHERE `room_type` = 'SINGLE';
UPDATE `tb_room` SET `max_occupancy` = 2 WHERE `room_type` = 'DOUBLE';
UPDATE `tb_room` SET `max_occupancy` = 5 WHERE `room_type` = 'FIVE_PLAYER';
UPDATE `tb_room` SET `max_occupancy` = 2 WHERE `room_type` = 'VIP';

-- 3. tb_guest表的real_name和identity_card已经是NULL，无需修改
-- 验证字段定义
SELECT 
    COLUMN_NAME, 
    IS_NULLABLE, 
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_guest' 
  AND COLUMN_NAME IN ('real_name', 'identity_card');

-- 4. 查看迁移结果
SELECT 
    room_no, 
    room_type, 
    max_occupancy, 
    current_occupancy, 
    status 
FROM tb_room 
ORDER BY room_no;

-- 迁移完成提示
SELECT '数据库迁移完成：房间表已添加容纳人数字段' AS status;
