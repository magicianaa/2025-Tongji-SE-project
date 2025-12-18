package com.esports.hotel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.PointsRedemptionVO;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.PointsProduct;
import com.esports.hotel.entity.PointsRedemption;
import com.esports.hotel.entity.User;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.PointsProductMapper;
import com.esports.hotel.mapper.UserMapper;
import com.esports.hotel.service.PointsShopService;
import com.esports.hotel.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分商城控制器
 */
@Tag(name = "7. 积分商城", description = "积分商品管理、兑换")
@RestController
@RequestMapping("/api/points-shop")
@RequiredArgsConstructor
public class PointsShopController {

    private final PointsProductMapper pointsProductMapper;
    private final PointsShopService pointsShopService;
    private final UserMapper userMapper;
    private final GuestMapper guestMapper;
    private final JwtUtil jwtUtil;

    // ==================== 商品管理（员工端） ====================

    @Operation(summary = "获取所有积分商品", description = "查询积分商城商品列表")
    @GetMapping("/products")
    public Result<List<PointsProduct>> getAllProducts(
            @Parameter(description = "是否仅显示上架商品") @RequestParam(required = false, defaultValue = "true") Boolean availableOnly) {
        LambdaQueryWrapper<PointsProduct> wrapper = new LambdaQueryWrapper<>();
        if (availableOnly) {
            wrapper.eq(PointsProduct::getIsAvailable, true);
        }
        wrapper.orderByAsc(PointsProduct::getPointsRequired);
        return Result.success(pointsProductMapper.selectList(wrapper));
    }

    @Operation(summary = "上架积分商品", description = "员工添加新商品到积分商城")
    @PostMapping("/products")
    public Result<PointsProduct> createProduct(@RequestBody PointsProduct product) {
        product.setProductType("PHYSICAL");  // 仅支持实物商品
        product.setIsAvailable(true);
        pointsProductMapper.insert(product);
        return Result.success(product, "商品上架成功");
    }

    @Operation(summary = "更新积分商品", description = "修改商品信息或库存")
    @PutMapping("/products/{productId}")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @RequestBody PointsProduct product) {
        product.setPointsProductId(productId);
        pointsProductMapper.updateById(product);
        return Result.success(null, "商品更新成功");
    }

    @Operation(summary = "下架积分商品", description = "将商品设为不可兑换")
    @DeleteMapping("/products/{productId}")
    public Result<Void> deleteProduct(@Parameter(description = "商品ID") @PathVariable Long productId) {
        PointsProduct product = pointsProductMapper.selectById(productId);
        if (product != null) {
            product.setIsAvailable(false);
            pointsProductMapper.updateById(product);
        }
        return Result.success(null, "商品已下架");
    }

    // ==================== 商品兑换（住客端） ====================

    @Operation(summary = "兑换积分商品", description = "住客使用积分兑换商品")
    @PostMapping("/redeem")
    public Result<PointsRedemption> redeemProduct(
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "商品ID") @RequestParam Long pointsProductId) {
        // 从 token 中获取用户ID
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 通过userId查询guestId
        User user = userMapper.selectById(userId);
        Guest guest = guestMapper.selectOne(
            new LambdaQueryWrapper<Guest>().eq(Guest::getUserId, user.getUserId())
        );
        
        PointsRedemption redemption = pointsShopService.redeemProduct(guest.getGuestId(), pointsProductId);
        return Result.success(redemption, "兑换成功，请等待配送");
    }

    @Operation(summary = "获取我的兑换记录", description = "住客查看自己的兑换历史")
    @GetMapping("/my-redemptions")
    public Result<List<PointsRedemptionVO>> getMyRedemptions(
            @RequestHeader("Authorization") String authHeader) {
        // 从 token 中获取用户ID
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 通过userId查询guestId
        User user = userMapper.selectById(userId);
        Guest guest = guestMapper.selectOne(
            new LambdaQueryWrapper<Guest>().eq(Guest::getUserId, user.getUserId())
        );
        
        return Result.success(pointsShopService.getGuestRedemptions(guest.getGuestId()));
    }

    // ==================== 兑换订单管理（员工端） ====================

    @Operation(summary = "获取所有兑换订单", description = "查询兑换记录")
    @GetMapping("/redemptions")
    public Result<List<PointsRedemptionVO>> getAllRedemptions(
            @Parameter(description = "订单状态筛选（PENDING/FULFILLED/CANCELLED）") @RequestParam(required = false) String status) {
        return Result.success(pointsShopService.getRedemptionsWithDetails(status));
    }

    @Operation(summary = "更新兑换订单状态", description = "标记订单已配送或取消")
    @PutMapping("/redemptions/{redemptionId}/status")
    public Result<Void> updateRedemptionStatus(
            @Parameter(description = "兑换记录ID") @PathVariable Long redemptionId,
            @Parameter(description = "新状态（FULFILLED/CANCELLED）") @RequestParam String status,
            @Parameter(description = "备注") @RequestParam(required = false) String notes) {
        pointsShopService.updateRedemptionStatus(redemptionId, status, notes);
        return Result.success(null, "订单状态已更新");
    }
}
