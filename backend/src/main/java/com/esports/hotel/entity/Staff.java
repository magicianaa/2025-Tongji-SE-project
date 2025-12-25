package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工信息实体
 */
@Data
@TableName("tb_staff")
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 员工ID
     */
    @TableId(value = "staff_id", type = IdType.AUTO)
    private Long staffId;

    /**
     * 用户ID（关联tb_user）
     */
    private Long userId;

    /**
     * 工号
     */
    private String employeeId;

    /**
     * 角色：RECEPTIONIST-前台, MANAGER-经理, MAINTENANCE-维修, ADMIN-管理员
     */
    private String role;

    /**
     * 部门
     */
    private String department;
}
