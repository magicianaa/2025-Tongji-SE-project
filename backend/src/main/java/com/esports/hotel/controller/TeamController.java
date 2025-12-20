package com.esports.hotel.controller;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.TeamRequest;
import com.esports.hotel.dto.TeamResponse;
import com.esports.hotel.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 战队管理控制器
 */
@Tag(name = "10. 战队管理", description = "创建战队、加入战队、解散战队")
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    
    @Operation(summary = "创建战队", description = "Guest创建新战队并成为队长")
    @PostMapping
    @RoomAuthRequired
    public Result<TeamResponse> createTeam(
            HttpServletRequest request,
            @Valid @RequestBody TeamRequest requestBody) {
        Long captainId = (Long) request.getAttribute("guestId");
        return Result.success(teamService.createTeam(captainId, requestBody));
    }
    
    @Operation(summary = "加入战队", description = "Guest加入指定战队")
    @PostMapping("/{teamId}/join")
    @RoomAuthRequired
    public Result<Void> joinTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long guestId = (Long) request.getAttribute("guestId");
        teamService.joinTeam(teamId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "离开战队", description = "Guest主动离开战队")
    @PostMapping("/{teamId}/leave")
    @RoomAuthRequired
    public Result<Void> leaveTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long guestId = (Long) request.getAttribute("guestId");
        teamService.leaveTeam(teamId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "踢出成员", description = "队长踢出指定成员")
    @PostMapping("/{teamId}/kick")
    @RoomAuthRequired
    public Result<Void> kickMember(
            HttpServletRequest request,
            @PathVariable Long teamId,
            @RequestParam Long memberId) {
        Long captainId = (Long) request.getAttribute("guestId");
        teamService.kickMember(teamId, memberId, captainId);
        return Result.success();
    }
    
    @Operation(summary = "解散战队", description = "队长解散战队")
    @DeleteMapping("/{teamId}")
    @RoomAuthRequired
    public Result<Void> disbandTeam(
            HttpServletRequest request,
            @PathVariable Long teamId) {
        Long captainId = (Long) request.getAttribute("guestId");
        teamService.disbandTeam(teamId, captainId);
        return Result.success();
    }
    
    @Operation(summary = "获取战队详情", description = "根据战队ID获取详情")
    @GetMapping("/{teamId}")
    public Result<TeamResponse> getTeam(@PathVariable Long teamId) {
        return Result.success(teamService.getTeam(teamId));
    }
    
    @Operation(summary = "获取我的战队", description = "获取Guest当前所在的战队")
    @GetMapping("/my")
    @RoomAuthRequired
    public Result<TeamResponse> getMyTeam(HttpServletRequest request) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(teamService.getMyTeam(guestId));
    }
    
    @Operation(summary = "更新战队游戏时长", description = "增加战队的游戏时长（分钟）")
    @PutMapping("/{teamId}/playtime")
    public Result<Void> updatePlaytime(
            @PathVariable Long teamId,
            @RequestParam Integer additionalMinutes) {
        teamService.updatePlaytime(teamId, additionalMinutes);
        return Result.success();
    }
}
