-- 修改游戏档案表，允许未入住时创建档案
-- 将record_id改为可选字段，移除外键约束

USE esports_hotel;

-- 1. 删除外键约束
ALTER TABLE `tb_gaming_profile` DROP FOREIGN KEY `fk_profile_record`;

-- 2. 修改record_id字段为可空
ALTER TABLE `tb_gaming_profile` MODIFY COLUMN `record_id` BIGINT NULL COMMENT '关联本次入住记录（未入住时为NULL）';

-- 3. 重新添加外键约束（允许NULL）
ALTER TABLE `tb_gaming_profile` 
ADD CONSTRAINT `fk_profile_record` 
FOREIGN KEY (`record_id`) REFERENCES `tb_checkin_record` (`record_id`)
ON DELETE SET NULL;

-- 4. 修改索引（允许NULL值）
ALTER TABLE `tb_gaming_profile` DROP KEY `idx_guest_record`;
ALTER TABLE `tb_gaming_profile` ADD KEY `idx_guest_record` (`guest_id`, `record_id`);

-- 验证修改
SHOW CREATE TABLE `tb_gaming_profile`;
