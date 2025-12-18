-- 查询用户 13800138000 的详细信息

USE esports_hotel;

-- 1. 查询用户信息
SELECT '=== 用户信息 ===' as Info;
SELECT 
    user_id, username, phone, user_type, status
FROM tb_user 
WHERE phone = '13800138000';

-- 2. 查询住客信息
SELECT '=== 住客信息 ===' as Info;
SELECT 
    g.guest_id, g.user_id, g.real_name, g.member_level, g.current_points
FROM tb_guest g
JOIN tb_user u ON g.user_id = u.user_id
WHERE u.phone = '13800138000';

-- 3. 查询入住记录
SELECT '=== 入住记录 ===' as Info;
SELECT 
    c.record_id, 
    c.room_id, 
    c.guest_id,
    c.actual_checkin,
    c.expected_checkout,
    c.actual_checkout,
    c.is_gaming_auth_active,
    c.payment_status
FROM tb_checkin_record c
JOIN tb_guest g ON c.guest_id = g.guest_id
JOIN tb_user u ON g.user_id = u.user_id
WHERE u.phone = '13800138000'
ORDER BY c.actual_checkin DESC
LIMIT 5;

-- 4. 查询当前有效的入住记录
SELECT '=== 当前有效入住记录 ===' as Info;
SELECT 
    c.record_id, 
    c.room_id, 
    c.guest_id,
    c.actual_checkin,
    c.expected_checkout,
    c.is_gaming_auth_active,
    r.room_no,
    NOW() as current_time,
    CASE 
        WHEN c.actual_checkin <= NOW() 
         AND c.expected_checkout >= NOW()
         AND c.is_gaming_auth_active = 1
         AND c.actual_checkout IS NULL
        THEN 'VALID'
        ELSE 'INVALID'
    END as status
FROM tb_checkin_record c
JOIN tb_guest g ON c.guest_id = g.guest_id
JOIN tb_user u ON g.user_id = u.user_id
JOIN tb_room r ON c.room_id = r.room_id
WHERE u.phone = '13800138000'
  AND c.actual_checkout IS NULL
ORDER BY c.actual_checkin DESC
LIMIT 1;
