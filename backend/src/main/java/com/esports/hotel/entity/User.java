package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户基础表实体
 */
@Data
@TableName("tb_user")
public class User implements Serializable {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;

    private String passwordHash;

    private String phone;

    /**
     * 用户类型：GUEST, STAFF, ADMIN
     */
    private String userType;

    /**
     * 账号状态：ACTIVE, DISABLED, DELETED
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
