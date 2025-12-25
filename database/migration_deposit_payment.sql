-- 添加订金支付相关字段到预订表
ALTER TABLE `tb_booking`
ADD COLUMN `deposit_payment_status` ENUM('UNPAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID' COMMENT '订金支付状态' AFTER `special_requests`,
ADD COLUMN `deposit_payment_method` ENUM('CASH', 'WECHAT', 'ALIPAY', 'CARD') DEFAULT NULL COMMENT '订金支付方式' AFTER `deposit_payment_status`,
ADD COLUMN `deposit_payment_time` DATETIME DEFAULT NULL COMMENT '订金支付时间' AFTER `deposit_payment_method`;
