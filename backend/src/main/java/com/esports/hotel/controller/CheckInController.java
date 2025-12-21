package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.CheckInRequest;
import com.esports.hotel.dto.CheckInResponse;
import com.esports.hotel.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 入住登记控制器
 */
@Slf4j
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Tag(name = "入住登记管理", description = "前台办理入住登记")
public class CheckInController {

    private final CheckInService checkInService;

    /**
     * 办理入住登记（支持多人入住）
     * 注意：仅前台工作人员（STAFF）和管理员（ADMIN）可访问，由JWT验证控制
     */
    @PostMapping
    @Operation(summary = "办理入住登记", description = "前台人员为住客办理入住，支持一房多人")
    public Result<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        log.info("收到入住登记请求: roomNo={}, 住客人数={}", 
                request.getRoomNo(), request.getGuests().size());
        CheckInResponse response = checkInService.checkIn(request);
        return Result.success(response);
    }

    /**
     * 获取入住记录详情
     */
    @GetMapping("/records/{recordId}")
    @Operation(summary = "获取入住记录详情", description = "根据recordId获取入住记录的详细信息")
    public Result<CheckInResponse> getCheckInRecord(@PathVariable Long recordId) {
        log.info("获取入住记录详情: recordId={}", recordId);
        CheckInResponse response = checkInService.getCheckInRecordDetail(recordId);
        return Result.success(response);
    }
}
