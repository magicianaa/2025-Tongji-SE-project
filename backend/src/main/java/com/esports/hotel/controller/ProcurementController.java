package com.esports.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.ProcurementRequest;
import com.esports.hotel.entity.Procurement;
import com.esports.hotel.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 进货控制器
 */
@Slf4j
@RestController
@RequestMapping("/procurement")
@RequiredArgsConstructor
@Tag(name = "进货管理", description = "商品进货相关接口")
public class ProcurementController {

    private final ProcurementService procurementService;

    @Operation(summary = "创建进货记录", description = "管理员创建商品进货记录")
    @PostMapping
    public Result<Void> createProcurement(
            @RequestBody ProcurementRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("收到进货请求: productId={}, quantity={}, unitCost={}", 
                request.getProductId(), request.getQuantity(), request.getUnitCost());
        procurementService.createProcurement(request, token);
        return Result.success(null, "进货成功");
    }

    @Operation(summary = "获取进货记录列表", description = "查询所有进货记录")
    @GetMapping
    public Result<List<Procurement>> getProcurementList() {
        List<Procurement> list = procurementService.getProcurementList();
        return Result.success(list);
    }

    @Operation(summary = "分页获取进货记录列表", description = "分页查询进货记录")
    @GetMapping("/list")
    public Result<Page<Procurement>> getProcurementListPaged(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询进货记录: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<Procurement> page = procurementService.getProcurementListPaged(pageNum, pageSize);
        return Result.success(page);
    }
}
