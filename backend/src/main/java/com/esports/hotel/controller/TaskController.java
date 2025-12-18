package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.TaskRecordVO;
import com.esports.hotel.dto.TaskSubmissionRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Task;
import com.esports.hotel.entity.TaskRecord;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.TaskMapper;
import com.esports.hotel.service.TaskService;
import com.esports.hotel.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@Tag(name = "6. 任务系统", description = "任务发布、提交、审核")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskMapper taskMapper;
    private final TaskService taskService;
    private final GuestMapper guestMapper;
    private final JwtUtil jwtUtil;

    // ==================== 任务管理（员工端） ====================

    @Operation(summary = "获取所有任务", description = "查询任务列表")
    @GetMapping
    public Result<List<Task>> getAllTasks(
            @Parameter(description = "是否仅显示启用的任务") @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (activeOnly) {
            wrapper.eq(Task::getIsActive, true);
        }
        wrapper.orderByDesc(Task::getTaskId);
        return Result.success(taskMapper.selectList(wrapper));
    }

    @Operation(summary = "发布新任务", description = "员工创建游戏任务")
    @PostMapping
    public Result<Task> createTask(@RequestBody Task task) {
        task.setIsActive(true);
        taskMapper.insert(task);
        return Result.success(task, "任务发布成功");
    }

    @Operation(summary = "更新任务", description = "修改任务信息")
    @PutMapping("/{taskId}")
    public Result<Void> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody Task task) {
        task.setTaskId(taskId);
        taskMapper.updateById(task);
        return Result.success(null, "任务更新成功");
    }

    @Operation(summary = "删除/下架任务", description = "将任务设为不可用")
    @DeleteMapping("/{taskId}")
    public Result<Void> deleteTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setIsActive(false);
            taskMapper.updateById(task);
        }
        return Result.success(null, "任务已下架");
    }

    // ==================== 任务提交（住客端） ====================

    @Operation(summary = "提交任务完成凭证", description = "住客提交任务截图和说明")
    @PostMapping("/submit")
    public Result<TaskRecord> submitTask(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TaskSubmissionRequest request) {
        // 从 token 中获取用户ID
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(401, "未授权");
        }
        
        // 根据userId查询guestId
        Guest guest = guestMapper.selectOne(new LambdaQueryWrapper<Guest>().eq(Guest::getUserId, userId));
        if (guest == null) {
            return Result.fail(404, "住客不存在");
        }
        
        TaskRecord record = taskService.submitTask(
            guest.getGuestId(),
            request.getTaskId(),
            request.getProofImageUrl(),
            request.getProofDescription()
        );
        return Result.success(record, "任务提交成功，等待审核");
    }

    @Operation(summary = "获取我的任务记录", description = "住客查看自己的任务提交历史")
    @GetMapping("/my-records")
    public Result<List<TaskRecordVO>> getMyTaskRecords(
            @RequestHeader("Authorization") String authHeader) {
        // 从 token 中获取用户ID
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(401, "未授权");
        }
        
        // 根据userId查询guestId
        Guest guest = guestMapper.selectOne(new LambdaQueryWrapper<Guest>().eq(Guest::getUserId, userId));
        if (guest == null) {
            return Result.fail(404, "住客不存在");
        }
        
        return Result.success(taskService.getGuestTaskRecords(guest.getGuestId()));
    }

    // ==================== 任务审核（员工端） ====================

    @Operation(summary = "获取待审核任务列表", description = "查询所有任务提交记录")
    @GetMapping("/records")
    public Result<List<TaskRecordVO>> getTaskRecords(
            @Parameter(description = "审核状态筛选（PENDING/APPROVED/REJECTED）") @RequestParam(required = false) String auditStatus) {
        return Result.success(taskService.getTaskRecordsWithDetails(auditStatus));
    }

    @Operation(summary = "审核任务", description = "员工审核任务，通过则自动发放积分")
    @PutMapping("/records/{taskRecordId}/audit")
    public Result<Void> auditTask(
            @Parameter(description = "任务记录ID") @PathVariable Long taskRecordId,
            @Parameter(description = "审核员工ID") @RequestParam Long auditorId,
            @Parameter(description = "是否通过") @RequestParam Boolean approved,
            @Parameter(description = "拒绝原因（拒绝时必填）") @RequestParam(required = false) String rejectReason) {
        taskService.auditTask(taskRecordId, auditorId, approved, rejectReason);
        return Result.success(null, approved ? "审核通过，积分已发放" : "审核拒绝");
    }
}
