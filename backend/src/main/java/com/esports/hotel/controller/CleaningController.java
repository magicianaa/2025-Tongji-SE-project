package com.esports.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.CleaningRequest;
import com.esports.hotel.entity.CleaningRecord;
import com.esports.hotel.service.CleaningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 清扫控制器
 */
@Slf4j
@RestController
@RequestMapping("/cleaning")
@RequiredArgsConstructor
@Tag(name = "清扫管理", description = "房间清扫相关接口")
public class CleaningController {

    private final CleaningService cleaningService;

    @Operation(summary = "获取清扫记录列表", description = "分页查询清扫记录")
    @GetMapping("/list")
    public Result<Page<CleaningRecord>> getCleaningRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String cleaningType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("查询清扫记录: pageNum={}, pageSize={}, roomNumber={}, cleaningType={}", 
                pageNum, pageSize, roomNumber, cleaningType);
        Page<CleaningRecord> page = cleaningService.getCleaningRecords(
                pageNum, pageSize, roomNumber, cleaningType, startDate, endDate);
        return Result.success(page);
    }

    @Operation(summary = "完成清扫", description = "员工完成房间清扫并记录物资消耗")
    @PostMapping("/complete")
    public Result<Void> completeCleaning(
            @RequestBody CleaningRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("收到清扫完成请求: roomNo={}, cleaningType={}", 
                request.getRoomNo(), request.getCleaningType());
        cleaningService.completeCleaning(request, token);
        return Result.success(null, "清扫完成");
    }
}
