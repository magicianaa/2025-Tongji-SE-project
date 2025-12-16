package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.HardwareTelemetryDTO;
import com.esports.hotel.entity.AlertLog;
import com.esports.hotel.entity.MaintenanceTicket;
import com.esports.hotel.mapper.AlertLogMapper;
import com.esports.hotel.mapper.MaintenanceTicketMapper;
import com.esports.hotel.service.HardwareSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<List<MaintenanceTicket>> getAllTickets(
            @Parameter(description = "状态筛选（可选）") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<MaintenanceTicket> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MaintenanceTicket::getStatus, status);
        }
        wrapper.orderByDesc(MaintenanceTicket::getCreateTime);
        return Result.success(maintenanceTicketMapper.selectList(wrapper));
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
}
