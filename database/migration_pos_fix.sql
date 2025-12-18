-- ============================================
-- POS系统修复 - 数据库迁移脚本
-- 修复日期: 2025-12-18
-- ============================================

USE esports_hotel_db;

-- 1. 修改rental_unit枚举，添加PER_TIME选项（按次计费）
-- 原有: ENUM('NONE', 'HOURLY', 'DAILY')
-- 修改后: ENUM('NONE', 'HOURLY', 'DAILY', 'PER_TIME')

ALTER TABLE `tb_product` 
MODIFY COLUMN `rental_unit` ENUM('NONE', 'HOURLY', 'DAILY', 'PER_TIME') DEFAULT 'NONE' 
COMMENT '租赁计费单位';

-- 2. 验证修改结果
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'esports_hotel_db' 
  AND TABLE_NAME = 'tb_product' 
  AND COLUMN_NAME = 'rental_unit';

-- 迁移完成提示
SELECT 'POS系统数据库修复完成：rental_unit字段已添加PER_TIME选项' AS status;

-- ============================================
-- 变更说明:
-- 1. 添加PER_TIME租赁单位选项，用于支持按次计费的外设租赁
-- 2. 后端已实现物理删除（deleteProduct）和软删除（disableProduct）两个接口
--    - DELETE /api/products/{id} - 物理删除
--    - PUT /api/products/{id}/disable - 下架（软删除）
-- ============================================
