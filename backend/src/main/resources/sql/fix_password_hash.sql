-- 修复新账号的密码哈希值
-- 使用与13800138000账号相同的BCrypt哈希（密码：123456）

USE esports_hotel_db;

UPDATE tb_user 
SET password_hash = '$2a$10$bzHP.b9DCoYHglGZYksHXuCgJgUN5yNMHEDsqzAdmGy3AnXStt8mu'
WHERE phone IN ('13900000001', '13900000002', '13900000003', '13900000004');

-- 验证结果
SELECT username, LEFT(password_hash, 30) as hash_check, user_type 
FROM tb_user 
WHERE phone LIKE '139%' OR phone = '13800138000'
ORDER BY user_type;
