package com.esports.hotel.controller;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.dto.CheckOutResponse;
import com.esports.hotel.dto.RoomVO;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.Room;
import com.esports.hotel.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客房管理控制器
 */
@Tag(name = "2. 客房管理(PMS)", description = "房态管理、入住、退房")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "查询所有房间（包含预订信息）", description = "查看房态看板，包含预订状态")
    @GetMapping
    public Result<List<RoomVO>> getAllRooms() {
        return Result.success(roomService.getAllRoomsWithBooking());
    }

    @Operation(summary = "查询空闲房间", description = "预订时选择房间")
    @GetMapping("/vacant")
    public Result<List<Room>> getVacantRooms() {
        return Result.success(roomService.getVacantRooms());
    }

    @Operation(summary = "根据状态筛选房间", description = "根据房间状态筛选")
    @GetMapping("/status/{status}")
    public Result<List<Room>> getRoomsByStatus(@PathVariable String status) {
        return Result.success(roomService.getRoomsByStatus(status));
    }

    /**
     * 旧的单人入住接口已废弃
     * 新的多人入住功能请使用 POST /api/checkin (CheckInController)
     * 
     * @deprecated 已由 CheckInController.checkIn() 替代
     */
    /*
    @Operation(summary = "办理入住（已废弃）", description = "请使用 POST /api/checkin")
    @Deprecated
    @PostMapping("/checkin")
    public Result<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        throw new BusinessException("此接口已废弃，请使用 POST /api/checkin 多人入住接口");
    }
    */

    @Operation(summary = "办理退房", description = "前台办理退房，汇总账单并回收客房权限")
    @PostMapping("/checkout/{recordId}")
    public Result<CheckOutResponse> checkOut(
            @Parameter(description = "入住记录ID") @PathVariable Long recordId,
            @Parameter(description = "支付方式") @RequestParam String paymentMethod) {
        CheckOutResponse response = roomService.checkOut(recordId, paymentMethod);
        return Result.success(response, "退房成功");
    }

    @Operation(summary = "办理退房(兼容旧接口)", description = "前台办理退房，汇总账单并回收客房权限")
    @PostMapping("/check-out/{recordId}")
    public Result<CheckOutResponse> checkOutCompat(
            @Parameter(description = "入住记录ID") @PathVariable Long recordId,
            @Parameter(description = "支付方式") @RequestParam String paymentMethod) {
        CheckOutResponse response = roomService.checkOut(recordId, paymentMethod);
        return Result.success(response, "退房成功");
    }

    @Operation(summary = "获取用户的入住记录", description = "查询当前登录用户的入住记录")
    @GetMapping("/check-in-records")
    public Result<List<CheckInRecord>> getCheckInRecords() {
        // TODO: 从token中获取当前用户ID，这里暂时返回所有未退房的记录
        return Result.success(roomService.getActiveCheckInRecords());
    }

    @Operation(summary = "测试客房权限接口", description = "需要携带 Room-Auth-Token 才能访问")
    @RoomAuthRequired
    @GetMapping("/test-room-auth")
    public Result<String> testRoomAuth() {
        return Result.success("客房权限验证通过！您可以访问房间控制、点餐等服务");
    }

    @Operation(summary = "标记房间已打扫", description = "前台标记房间清洁完成，状态从CLEANING变为VACANT")
    @PostMapping("/{roomId}/mark-cleaned")
    public Result<Void> markCleaned(@Parameter(description = "房间ID") @PathVariable Long roomId) {
        roomService.markCleaned(roomId);
        return Result.success(null, "已标记为打扫完毕");
    }

    @Operation(summary = "标记房间已维修", description = "前台标记房间维修完成，状态从MAINTENANCE变为VACANT")
    @PostMapping("/{roomId}/mark-repaired")
    public Result<Void> markRepaired(@Parameter(description = "房间ID") @PathVariable Long roomId) {
        roomService.markRepaired(roomId);
        return Result.success(null, "已标记为维修完成");
    }

    @Operation(summary = "设置房间为维修中", description = "前台设置房间为维修状态")
    @PostMapping("/{roomId}/mark-maintenance")
    public Result<Void> markMaintenance(
            @Parameter(description = "房间ID") @PathVariable Long roomId,
            @Parameter(description = "维修原因") @RequestParam(required = false) String reason) {
        roomService.markMaintenance(roomId, reason);
        return Result.success(null, "已设置为维修中");
    }
}
