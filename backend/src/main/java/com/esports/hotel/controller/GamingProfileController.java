package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.GamingProfileRequest;
import com.esports.hotel.dto.GamingProfileResponse;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.service.GamingProfileService;
import com.esports.hotel.util.JwtUtil;
import io.jsonwebtoken.Claims;
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
    private final JwtUtil jwtUtil;
    private final GuestMapper guestMapper;
    
    /**
     * 从请求中获取guestId
     * 如果请求中已有guestId属性（来自RoomAuthInterceptor），直接使用
     * 否则从JWT token中提取userId，再查询对应的guestId
     */
    private Long getGuestIdFromRequest(HttpServletRequest request) {
        // 优先使用RoomAuthInterceptor设置的guestId
        Long guestId = (Long) request.getAttribute("guestId");
        if (guestId != null) {
            return guestId;
        }
        
        // 从JWT token中提取userId
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未提供认证令牌");
        }
        
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        
        if (userId == null) {
            throw new RuntimeException("无效的认证令牌");
        }
        
        // 根据userId查询guestId
        LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Guest::getUserId, userId);
        Guest guest = guestMapper.selectOne(wrapper);
        
        if (guest == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        return guest.getGuestId();
    }
    
    @Operation(summary = "创建或更新游戏档案", description = "创建或更新当前Guest的游戏档案（无需入住）")
    @PostMapping
    public Result<GamingProfileResponse> createOrUpdateProfile(
            HttpServletRequest request,
            @Valid @RequestBody GamingProfileRequest requestBody) {
        Long guestId = getGuestIdFromRequest(request);
        return Result.success(gamingProfileService.createOrUpdateProfile(guestId, requestBody));
    }
    
    @Operation(summary = "获取游戏档案详情", description = "根据档案ID获取游戏档案")
    @GetMapping("/{profileId}")
    public Result<GamingProfileResponse> getProfile(@PathVariable Long profileId) {
        return Result.success(gamingProfileService.getProfile(profileId));
    }
    
    @Operation(summary = "获取当前游戏档案", description = "获取Guest的指定游戏类型档案（无需入住）")
    @GetMapping("/current")
    public Result<GamingProfileResponse> getCurrentProfile(
            HttpServletRequest request,
            @RequestParam String gameType) {
        Long guestId = getGuestIdFromRequest(request);
        return Result.success(gamingProfileService.getCurrentProfile(guestId, gameType));
    }
    
    @Operation(summary = "删除游戏档案", description = "删除指定的游戏档案")
    @DeleteMapping("/{profileId}")
    public Result<Void> deleteProfile(
            HttpServletRequest request,
            @PathVariable Long profileId) {
        Long guestId = getGuestIdFromRequest(request);
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
