-- 智慧电竞酒店管理系统 - RBAC测试账号
-- 用途：为三种角色创建测试账号，用于验证RBAC功能

-- 注意：密码均为 123456，经过BCrypt加密
-- BCrypt密码哈希值（从系统中已有的可用账号复制，确保兼容）
-- 哈希值：$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu

USE esports_hotel_db;

-- 1. 插入管理员账号（ADMIN）
-- 注意：username必须等于phone，因为登录时前端发送phone，后端用username查询
INSERT INTO tb_user (username, password_hash, phone, user_type, status, created_at, updated_at)
VALUES 
('13900000001', '$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu', '13900000001', 'ADMIN', 'ACTIVE', NOW(), NOW());

-- 2. 插入前台员工账号（STAFF）
INSERT INTO tb_user (username, password_hash, phone, user_type, status, created_at, updated_at)
VALUES 
('13900000002', '$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu', '13900000002', 'STAFF', 'ACTIVE', NOW(), NOW()),
('13900000003', '$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu', '13900000003', 'STAFF', 'ACTIVE', NOW(), NOW());

-- 3. 插入测试住客账号（GUEST）- 已有13800138000，再添加一个
INSERT INTO tb_user (username, password_hash, phone, user_type, status, created_at, updated_at)
VALUES 
('13900000004', '$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu', '13900000004', 'GUEST', 'ACTIVE', NOW(), NOW());

-- 4. 为新住客创建扩展信息
INSERT INTO tb_guest (user_id, real_name, member_level, experience_points, current_points, total_checkin_nights)
SELECT user_id, '测试用户', 'BRONZE', 0, 0, 0 
FROM tb_user 
WHERE phone = '13900000004';

-- 5. 查询所有测试账号
SELECT user_id, username, phone, user_type, status, created_at
FROM tb_user
WHERE phone LIKE '139000000%' OR phone = '13800138000'
ORDER BY user_type, user_id;

-- 测试账号列表：
-- +-------------+-----------+-----------+---------+
-- | 手机号      | user_type | password  | 说明    |
-- +-------------+-----------+-----------+---------+
-- | 13900000001 | ADMIN     | 123456    | 管理员  |
-- | 13900000002 | STAFF     | 123456    | 前台1   |
-- | 13900000003 | STAFF     | 123456    | 前台2   |
-- | 13900000004 | GUEST     | 123456    | 住客2   |
-- | 13800138000 | GUEST     | 123456    | 住客1   |
-- +-------------+-----------+-----------+---------+
-- 注意：username字段已设置为手机号，方便前端登录（前端发送phone字段）
