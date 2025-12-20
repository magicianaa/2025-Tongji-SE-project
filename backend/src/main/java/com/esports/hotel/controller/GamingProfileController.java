package com.esports.hotel.controller;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.GamingProfileRequest;
import com.esports.hotel.dto.GamingProfileResponse;
import com.esports.hotel.service.GamingProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 游戏档案控制器
 */
@Tag(name = "8. 游戏档案管理", description = "游戏档案的增删改查")
@RestController
@RequestMapping("/gaming-profiles")
@RequiredArgsConstructor
public class GamingProfileController {
    
    private final GamingProfileService gamingProfileService;
    
    @Operation(summary = "创建或更新游戏档案", description = "创建或更新当前入住Guest的游戏档案")
    @PostMapping
    @RoomAuthRequired
    public Result<GamingProfileResponse> createOrUpdateProfile(
            HttpServletRequest request,
            @Valid @RequestBody GamingProfileRequest requestBody) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(gamingProfileService.createOrUpdateProfile(guestId, requestBody));
    }
    
    @Operation(summary = "获取游戏档案详情", description = "根据档案ID获取游戏档案")
    @GetMapping("/{profileId}")
    public Result<GamingProfileResponse> getProfile(@PathVariable Long profileId) {
        return Result.success(gamingProfileService.getProfile(profileId));
    }
    
    @Operation(summary = "获取当前游戏档案", description = "获取Guest当前入住期间的指定游戏类型档案")
    @GetMapping("/current")
    @RoomAuthRequired
    public Result<GamingProfileResponse> getCurrentProfile(
            HttpServletRequest request,
            @RequestParam String gameType) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(gamingProfileService.getCurrentProfile(guestId, gameType));
    }
    
    @Operation(summary = "删除游戏档案", description = "删除指定的游戏档案")
    @DeleteMapping("/{profileId}")
    @RoomAuthRequired
    public Result<Void> deleteProfile(
            HttpServletRequest request,
            @PathVariable Long profileId) {
        Long guestId = (Long) request.getAttribute("guestId");
        gamingProfileService.deleteProfile(profileId, guestId);
        return Result.success();
    }
    
    @Operation(summary = "获取在线玩家列表（在线大厅）", description = "浏览店内所有在线玩家，支持按游戏类型和段位筛选")
    @GetMapping("/online")
    public Result<java.util.List<GamingProfileResponse>> getOnlinePlayers(
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) String rank) {
        return Result.success(gamingProfileService.getOnlinePlayers(gameType, rank));
    }
}
