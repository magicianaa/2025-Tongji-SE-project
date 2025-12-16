package com.esports.hotel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单表实体
 */
@Data
@TableName("tb_maintenance_ticket")
public class MaintenanceTicket implements Serializable {

    @TableId(value = "ticket_id", type = IdType.AUTO)
    private Long ticketId;

    private Long roomId;

    /**
     * 报修人（住客/员工）
     */
    private Long reporterId;

    /**
     * 诉求类型：REPAIR, CHANGE_ROOM
     */
    private String requestType;

    /**
     * 故障描述
     */
    private String description;

    /**
     * 优先级：LOW, MEDIUM, HIGH, URGENT
     */
    private String priority;

    /**
     * 状态：OPEN, IN_PROGRESS, RESOLVED, CLOSED
     */
    private String status;

    /**
     * 指派维修员工
     */
    private Long assignedTo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime resolveTime;

    /**
     * 处理结果
     */
    private String resolutionNotes;

    /**
     * 维修成本（用于损耗分析）
     */
    private BigDecimal cost;
}
