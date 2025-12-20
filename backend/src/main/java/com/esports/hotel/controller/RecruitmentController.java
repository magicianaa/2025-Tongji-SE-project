package com.esports.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.MatchingQuery;
import com.esports.hotel.dto.RecruitmentRequest;
import com.esports.hotel.dto.RecruitmentResponse;
import com.esports.hotel.service.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招募信息控制器
 */
@Tag(name = "9. 组队招募", description = "发布招募、查询招募、关闭招募")
@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {
    
    private final RecruitmentService recruitmentService;
    
    @Operation(summary = "发布招募信息", description = "发布新的组队招募")
    @PostMapping
    @RoomAuthRequired
    public Result<RecruitmentResponse> publishRecruitment(
            HttpServletRequest request,
            @Valid @RequestBody RecruitmentRequest requestBody) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(recruitmentService.publishRecruitment(guestId, requestBody));
    }
    
    @Operation(summary = "查询招募列表（分页+筛选）", description = "根据游戏类型、段位、位置筛选招募信息")
    @GetMapping("/search")
    public Result<Page<RecruitmentResponse>> searchRecruitments(@ModelAttribute MatchingQuery query) {
        return Result.success(recruitmentService.searchRecruitments(query));
    }
    
    @Operation(summary = "获取招募详情", description = "根据招募ID获取详情")
    @GetMapping("/{recruitmentId}")
    public Result<RecruitmentResponse> getRecruitment(@PathVariable Long recruitmentId) {
        return Result.success(recruitmentService.getRecruitment(recruitmentId));
    }
    
    @Operation(summary = "关闭招募", description = "发布者关闭自己的招募")
    @PutMapping("/{recruitmentId}/close")
    @RoomAuthRequired
    public Result<Void> closeRecruitment(
            HttpServletRequest request,
            @PathVariable Long recruitmentId) {
        Long guestId = (Long) request.getAttribute("guestId");
        recruitmentService.closeRecruitment(recruitmentId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "删除招募", description = "发布者删除自己的招募")
    @DeleteMapping("/{recruitmentId}")
    @RoomAuthRequired
    public Result<Void> deleteRecruitment(
            HttpServletRequest request,
            @PathVariable Long recruitmentId) {
        Long guestId = (Long) request.getAttribute("guestId");
        recruitmentService.deleteRecruitment(recruitmentId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "获取我发布的招募", description = "获取Guest发布的所有招募信息")
    @GetMapping("/my")
    @RoomAuthRequired
    public Result<List<RecruitmentResponse>> getMyRecruitments(HttpServletRequest request) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(recruitmentService.getMyRecruitments(guestId));
    }
    
    @Operation(summary = "申请加入招募", description = "通过WebSocket实时推送给发布者")
    @PostMapping("/{recruitmentId}/apply")
    @RoomAuthRequired
    public Result<Void> applyToRecruitment(
            HttpServletRequest request,
            @PathVariable Long recruitmentId) {
        Long guestId = (Long) request.getAttribute("guestId");
        recruitmentService.applyToRecruitment(recruitmentId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "同意申请", description = "发布者同意申请并加入战队")
    @PostMapping("/{recruitmentId}/approve")
    @RoomAuthRequired
    public Result<Void> approveApplication(
            HttpServletRequest request,
            @PathVariable Long recruitmentId,
            @RequestParam Long applicantId) {
        Long captainId = (Long) request.getAttribute("guestId");
        recruitmentService.approveApplication(recruitmentId, applicantId, captainId);
        return Result.success();
    }
    
    @Operation(summary = "拒绝申请", description = "发布者拒绝申请")
    @PostMapping("/{recruitmentId}/reject")
    @RoomAuthRequired
    public Result<Void> rejectApplication(
            HttpServletRequest request,
            @PathVariable Long recruitmentId,
            @RequestParam Long applicantId) {
        Long captainId = (Long) request.getAttribute("guestId");
        recruitmentService.rejectApplication(recruitmentId, applicantId, captainId);
        return Result.success();
    }
}
