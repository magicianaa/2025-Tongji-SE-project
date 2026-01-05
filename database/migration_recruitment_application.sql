-- ============================================
-- 招募申请表 - 用于存储组队申请记录
-- ============================================

-- 创建招募申请表
CREATE TABLE IF NOT EXISTS `tb_recruitment_application` (
  `application_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `recruitment_id` BIGINT NOT NULL COMMENT '关联招募信息',
  `applicant_id` BIGINT NOT NULL COMMENT '申请者（Guest）',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态：待处理/已同意/已拒绝',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`application_id`),
  KEY `idx_recruitment_id` (`recruitment_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `uk_recruitment_applicant` (`recruitment_id`, `applicant_id`),
  CONSTRAINT `fk_application_recruitment` FOREIGN KEY (`recruitment_id`) REFERENCES `tb_recruitment` (`recruitment_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_application_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `tb_guest` (`guest_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招募申请表';
