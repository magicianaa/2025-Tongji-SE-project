package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.Result;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.PointTransaction;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.PointTransactionMapper;
import com.esports.hotel.mapper.UserMapper;
import com.esports.hotel.service.PointsService;
import com.esports.hotel.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分查询控制器
 */
@Tag(name = "8. 积分管理", description = "积分查询、流水记录")
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final PointsService pointsService;
    private final JwtUtil jwtUtil;

    @Data
    public static class PointsBalanceResponse {
        private Integer currentPoints;
        private Integer experiencePoints;
        private String memberLevel;
        private Double discountRate;
    }

    @Operation(summary = "查询积分余额", description = "获取住客当前积分和会员信息")
    @GetMapping("/balance")
    public Result<PointsBalanceResponse> getPointsBalance(
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

        PointsBalanceResponse response = new PointsBalanceResponse();
        response.setCurrentPoints(guest.getCurrentPoints());
        response.setExperiencePoints(guest.getExperiencePoints());
        response.setMemberLevel(guest.getMemberLevel());
        response.setDiscountRate(pointsService.getMemberDiscountRate(guest.getMemberLevel()));

        return Result.success(response);
    }

    @Operation(summary = "查询积分流水", description = "获取积分变动历史")
    @GetMapping("/transactions")
    public Result<List<PointTransaction>> getTransactions(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "查询数量限制") @RequestParam(required = false, defaultValue = "50") Integer limit) {
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
        
        LambdaQueryWrapper<PointTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointTransaction::getGuestId, guest.getGuestId())
               .orderByDesc(PointTransaction::getCreateTime)
               .last("LIMIT " + limit);
        return Result.success(pointTransactionMapper.selectList(wrapper));
    }
}
