package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 清扫记录实体
 */
@Data
@TableName("tb_cleaning_record")
public class CleaningRecord implements Serializable {

    @TableId(value = "cleaning_id", type = IdType.AUTO)
    private Long cleaningId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 清扫员工ID
     */
    private Long staffId;

    /**
     * 清扫完成时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime cleaningTime;

    /**
     * 清扫类型：CHECKOUT(退房清扫), DAILY(日常清扫), DEEP(深度清扫)
     */
    private String cleaningType;

    /**
     * 备注
     */
    private String notes;
    
    // ========== 以下字段不存储在数据库，仅用于返回给前端 ==========
    
    /**
     * 房间号（临时字段，不存储在数据库）
     */
    @TableField(exist = false)
    private String roomNumber;
    
    /**
     * 员工姓名（临时字段，不存储在数据库）
     */
    @TableField(exist = false)
    private String staffName;
    
    /**
     * 物资消耗列表（临时字段，不存储在数据库）
     */
    @TableField(exist = false)
    private List<SupplyUsage> supplyUsages;
}
