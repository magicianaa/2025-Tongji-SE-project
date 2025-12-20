-- ============================================
-- Migration Script: Add Contact Fields to tb_booking
-- Date: 2025-12-20
-- Description: 添加主住客姓名和联系电话字段到预订表
-- ============================================

USE esports_hotel_db;

-- 检查并添加 main_guest_name 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_booking' 
  AND COLUMN_NAME = 'main_guest_name';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE tb_booking ADD COLUMN main_guest_name VARCHAR(50) NULL COMMENT ''主住客姓名'' AFTER discount_rate',
    'SELECT ''Column main_guest_name already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 contact_phone 字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_booking' 
  AND COLUMN_NAME = 'contact_phone';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE tb_booking ADD COLUMN contact_phone VARCHAR(20) NULL COMMENT ''联系电话'' AFTER main_guest_name',
    'SELECT ''Column contact_phone already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段添加结果
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_booking'
  AND COLUMN_NAME IN ('main_guest_name', 'contact_phone')
ORDER BY ORDINAL_POSITION;

-- ============================================
-- 说明：
-- 1. 此脚本会检查字段是否已存在，避免重复执行报错
-- 2. main_guest_name: 用于存储预订时的主住客姓名，在办理入住时需要与实际入住姓名匹配
-- 3. contact_phone: 用于存储预订时的联系电话，用于查询预订信息
-- 4. 两个字段均允许为NULL，兼容旧数据
-- ============================================
