-- ============================================
-- 修复用户积分和经验值不一致的问题
-- ============================================

-- 针对用户 13800138000，将经验值设置为与当前积分相同
-- 因为业务逻辑是：每增加1积分，同时增加1经验值
-- 如果当前积分是17150，经验值应该至少是17150（如果中间有消耗积分，经验值会更高）

-- 查看当前状态
SELECT 
    u.username,
    g.guest_id,
    g.current_points AS '当前积分',
    g.experience_points AS '当前经验值',
    g.member_level AS '会员等级'
FROM tb_user u
JOIN tb_guest g ON u.user_id = g.user_id
WHERE u.username = '13800138000';

-- 修复方案：将经验值设置为当前积分值
-- 这样可以确保经验值至少等于积分（符合业务逻辑）
UPDATE tb_guest g
JOIN tb_user u ON g.user_id = u.user_id
SET g.experience_points = g.current_points
WHERE u.username = '13800138000';

-- 根据经验值重新计算会员等级
-- 青铜: 0-999
-- 白银: 1000-4999
-- 黄金: 5000-9999
-- 铂金: 10000+
UPDATE tb_guest g
JOIN tb_user u ON g.user_id = u.user_id
SET g.member_level = CASE
    WHEN g.experience_points >= 10000 THEN 'PLATINUM'
    WHEN g.experience_points >= 5000 THEN 'GOLD'
    WHEN g.experience_points >= 1000 THEN 'SILVER'
    ELSE 'BRONZE'
END
WHERE u.username = '13800138000';

-- 验证修复结果
SELECT 
    u.username,
    g.guest_id,
    g.current_points AS '当前积分',
    g.experience_points AS '当前经验值',
    g.member_level AS '会员等级'
FROM tb_user u
JOIN tb_guest g ON u.user_id = g.user_id
WHERE u.username = '13800138000';

-- 如果需要修复所有用户的数据，可以取消下面注释：
/*
-- 全局修复：将所有用户的经验值设置为不低于当前积分
UPDATE tb_guest
SET experience_points = GREATEST(experience_points, current_points);

-- 全局重新计算会员等级
UPDATE tb_guest
SET member_level = CASE
    WHEN experience_points >= 10000 THEN 'PLATINUM'
    WHEN experience_points >= 5000 THEN 'GOLD'
    WHEN experience_points >= 1000 THEN 'SILVER'
    ELSE 'BRONZE'
END;
*/
