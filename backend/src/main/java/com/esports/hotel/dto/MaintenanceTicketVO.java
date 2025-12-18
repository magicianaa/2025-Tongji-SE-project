package com.esports.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单视图对象（包含房间号信息）
 */
@Data
public class MaintenanceTicketVO {

    private Long ticketId;

    private Long roomId;
    
    /**
     * 房间号（如201、301）
     */
    private String roomNo;

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
