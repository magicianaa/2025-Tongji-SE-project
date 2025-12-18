package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.HardwareTelemetryDTO;
import com.esports.hotel.dto.MaintenanceTicketVO;
import com.esports.hotel.entity.AlertLog;
import com.esports.hotel.entity.MaintenanceTicket;
import com.esports.hotel.entity.Room;
import com.esports.hotel.mapper.AlertLogMapper;
import com.esports.hotel.mapper.MaintenanceTicketMapper;
import com.esports.hotel.mapper.RoomMapper;
import com.esports.hotel.service.HardwareSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 硬件监控控制器
 */
@Tag(name = "3. 硬件监控", description = "硬件状态、报警、维修工单")
@RestController
@RequestMapping("/hardware")
@RequiredArgsConstructor
public class HardwareController {

    private final HardwareSimulationService hardwareSimulationService;
    private final AlertLogMapper alertLogMapper;
    private final MaintenanceTicketMapper maintenanceTicketMapper;
    private final RoomMapper roomMapper;

    @Operation(summary = "获取所有房间硬件状态", description = "用于监控大屏展示")
    @GetMapping("/status")
    public Result<List<HardwareTelemetryDTO>> getAllRoomStatus() {
        return Result.success(hardwareSimulationService.getAllRoomStatus());
    }

    @Operation(summary = "手动触发故障模拟", description = "测试用：模拟指定房间硬件过热")
    @PostMapping("/trigger-failure/{roomId}")
    public Result<Void> triggerManualFailure(
            @Parameter(description = "房间ID") @PathVariable Long roomId) {
        hardwareSimulationService.triggerManualFailure(roomId);
        return Result.success(null, "故障模拟已触发");
    }

    @Operation(summary = "获取报警列表", description = "查询报警日志，支持limit参数")
    @GetMapping("/alerts")
    public Result<List<AlertLog>> getAlerts(
            @Parameter(description = "数量限制") @RequestParam(required = false, defaultValue = "50") Integer limit) {
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AlertLog::getAlertTime)
               .last("LIMIT " + limit);
        return Result.success(alertLogMapper.selectList(wrapper));
    }

    @Operation(summary = "获取未处理报警列表", description = "管理端查看待处理报警")
    @GetMapping("/alerts/unhandled")
    public Result<List<AlertLog>> getUnhandledAlerts() {
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertLog::getIsHandled, false)
               .orderByDesc(AlertLog::getAlertTime);
        return Result.success(alertLogMapper.selectList(wrapper));
    }

    @Operation(summary = "标记报警已处理", description = "处理完成后调用")
    @PutMapping("/alerts/{alertId}/handle")
    public Result<Void> handleAlert(
            @Parameter(description = "报警ID") @PathVariable Long alertId,
            @Parameter(description = "处理人ID") @RequestParam(required = false) Long handlerId) {
        AlertLog alert = alertLogMapper.selectById(alertId);
        if (alert != null) {
            alert.setIsHandled(true);
            if (handlerId != null) {
                alert.setHandlerId(handlerId);
            }
            alert.setHandleTime(java.time.LocalDateTime.now());
            alertLogMapper.updateById(alert);
        }
        return Result.success(null, "报警已标记为已处理");
    }

    @Operation(summary = "标记报警已处理(POST兼容)", description = "处理完成后调用")
    @PostMapping("/alerts/{alertId}/handle")
    public Result<Void> handleAlertPost(
            @Parameter(description = "报警ID") @PathVariable Long alertId,
            @Parameter(description = "处理人ID") @RequestParam(required = false) Long handlerId) {
        return handleAlert(alertId, handlerId);
    }

    @Operation(summary = "获取所有维修工单", description = "工单管理页面")
    @GetMapping("/tickets")
    public Result<List<MaintenanceTicketVO>> getAllTickets(
            @Parameter(description = "状态筛选（可选）") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<MaintenanceTicket> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MaintenanceTicket::getStatus, status);
        }
        wrapper.orderByDesc(MaintenanceTicket::getCreateTime);
        List<MaintenanceTicket> tickets = maintenanceTicketMapper.selectList(wrapper);
        
        // 转换为 VO 并填充房间号
        List<MaintenanceTicketVO> voList = new ArrayList<>();
        for (MaintenanceTicket ticket : tickets) {
            MaintenanceTicketVO vo = new MaintenanceTicketVO();
            BeanUtils.copyProperties(ticket, vo);
            
            // 查询房间号
            Room room = roomMapper.selectById(ticket.getRoomId());
            if (room != null) {
                vo.setRoomNo(room.getRoomNo());
            }
            voList.add(vo);
        }
        return Result.success(voList);
    }

    @Operation(summary = "创建维修工单", description = "住客报修或系统自动生成")
    @PostMapping("/tickets")
    public Result<MaintenanceTicket> createTicket(
            @Parameter(description = "房间ID") @RequestParam Long roomId,
            @Parameter(description = "报修人ID") @RequestParam(required = false) Long reporterId,
            @Parameter(description = "诉求类型: REPAIR, CHANGE_ROOM") @RequestParam String requestType,
            @Parameter(description = "故障描述") @RequestParam String description,
            @Parameter(description = "优先级: LOW, MEDIUM, HIGH, URGENT") @RequestParam(required = false, defaultValue = "MEDIUM") String priority) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setRoomId(roomId);
        ticket.setReporterId(reporterId);
        ticket.setRequestType(requestType);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus("OPEN");
        maintenanceTicketMapper.insert(ticket);
        return Result.success(ticket, "工单创建成功");
    }

    @Operation(summary = "更新工单状态", description = "工单流转（指派、完成等）")
    @PutMapping("/tickets/{ticketId}/status")
    public Result<Void> updateTicketStatus(
            @Parameter(description = "工单ID") @PathVariable Long ticketId,
            @Parameter(description = "新状态") @RequestParam String status,
            @Parameter(description = "处理备注（可选）") @RequestParam(required = false) String notes) {
        MaintenanceTicket ticket = maintenanceTicketMapper.selectById(ticketId);
        if (ticket != null) {
            ticket.setStatus(status);
            if (notes != null) {
                ticket.setResolutionNotes(notes);
            }
            if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
                ticket.setResolveTime(java.time.LocalDateTime.now());
            }
            maintenanceTicketMapper.updateById(ticket);
        }
        return Result.success(null, "工单状态已更新");
    }

    @Operation(summary = "获取指定工单详情", description = "查看单个工单")
    @GetMapping("/tickets/{ticketId}")
    public Result<MaintenanceTicketVO> getTicketDetail(@PathVariable Long ticketId) {
        MaintenanceTicket ticket = maintenanceTicketMapper.selectById(ticketId);
        if (ticket == null) {
            return Result.success(null);
        }
        
        // 转换为 VO 并填充房间号
        MaintenanceTicketVO vo = new MaintenanceTicketVO();
        BeanUtils.copyProperties(ticket, vo);
        
        // 查询房间号
        Room room = roomMapper.selectById(ticket.getRoomId());
        if (room != null) {
            vo.setRoomNo(room.getRoomNo());
        }
        return Result.success(vo);
    }
}
