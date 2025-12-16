package com.esports.hotel.controller;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.dto.CheckOutResponse;
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

    @Operation(summary = "查询所有房间", description = "查看房态看板（管理端）")
    @GetMapping
    public Result<List<Room>> getAllRooms() {
        return Result.success(roomService.getAllRooms());
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

    @Operation(summary = "办理入住", description = "前台办理入住，生成入住记录并发放客房权限Token")
    @PostMapping("/checkin")
    public Result<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = roomService.checkIn(request);
        return Result.success(response, "入住成功");
    }

    @Operation(summary = "办理入住(兼容旧接口)", description = "前台办理入住，生成入住记录并发放客房权限Token")
    @PostMapping("/check-in")
    public Result<CheckInResponse> checkInCompat(@Valid @RequestBody CheckInRequest request) {
        CheckInResponse response = roomService.checkIn(request);
        return Result.success(response, "入住成功");
    }

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
}
