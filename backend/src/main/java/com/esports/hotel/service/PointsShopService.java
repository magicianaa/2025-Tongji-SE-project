package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.PointsRedemptionVO;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 积分商城服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsShopService {

    private final PointsProductMapper pointsProductMapper;
    private final PointsRedemptionMapper pointsRedemptionMapper;
    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;

    /**
     * 兑换商品
     */
    @Transactional(rollbackFor = Exception.class)
    public PointsRedemption redeemProduct(Long guestId, Long pointsProductId) {
        // 检查商品是否存在且可用
        PointsProduct product = pointsProductMapper.selectById(pointsProductId);
        if (product == null || !product.getIsAvailable()) {
            throw new RuntimeException("商品不存在或已下架");
        }

        // 检查库存
        if (product.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        // 检查住客积分余额
        Integer balance = pointsService.getPointsBalance(guestId);
        if (balance < product.getPointsRequired()) {
            throw new RuntimeException("积分余额不足，需要" + product.getPointsRequired() + "积分，当前" + balance + "积分");
        }

        // 扣减积分
        pointsService.deductPoints(
            guestId,
            product.getPointsRequired(),
            "REDEMPTION",
            pointsProductId,
            "兑换商品：" + product.getProductName()
        );

        // 扣减库存
        product.setStock(product.getStock() - 1);
        pointsProductMapper.updateById(product);

        // 创建兑换记录
        PointsRedemption redemption = new PointsRedemption();
        redemption.setGuestId(guestId);
        redemption.setPointsProductId(pointsProductId);
        redemption.setPointsCost(product.getPointsRequired());
        redemption.setStatus("PENDING");
        pointsRedemptionMapper.insert(redemption);

        log.info("住客{}兑换商品{}，消费{}积分", guestId, product.getProductName(), product.getPointsRequired());
        return redemption;
    }

    /**
     * 获取兑换记录列表（带详情）
     */
    public List<PointsRedemptionVO> getRedemptionsWithDetails(String status) {
        LambdaQueryWrapper<PointsRedemption> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PointsRedemption::getStatus, status);
        }
        wrapper.orderByDesc(PointsRedemption::getRedemptionTime);
        List<PointsRedemption> redemptions = pointsRedemptionMapper.selectList(wrapper);

        List<PointsRedemptionVO> voList = new ArrayList<>();
        for (PointsRedemption redemption : redemptions) {
            PointsRedemptionVO vo = new PointsRedemptionVO();
            BeanUtils.copyProperties(redemption, vo);

            // 填充商品信息
            PointsProduct product = pointsProductMapper.selectById(redemption.getPointsProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
            }

            // 填充住客信息
            Guest guest = guestMapper.selectById(redemption.getGuestId());
            if (guest != null) {
                User user = userMapper.selectById(guest.getUserId());
                if (user != null) {
                    vo.setGuestUsername(user.getUsername());
                }
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 获取住客的兑换记录
     */
    public List<PointsRedemptionVO> getGuestRedemptions(Long guestId) {
        LambdaQueryWrapper<PointsRedemption> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRedemption::getGuestId, guestId)
               .orderByDesc(PointsRedemption::getRedemptionTime);
        List<PointsRedemption> redemptions = pointsRedemptionMapper.selectList(wrapper);

        List<PointsRedemptionVO> voList = new ArrayList<>();
        for (PointsRedemption redemption : redemptions) {
            PointsRedemptionVO vo = new PointsRedemptionVO();
            BeanUtils.copyProperties(redemption, vo);

            // 填充商品信息
            PointsProduct product = pointsProductMapper.selectById(redemption.getPointsProductId());
            if (product != null) {
                vo.setProductName(product.getProductName());
            }

            voList.add(vo);
        }

        return voList;
    }

    /**
     * 更新兑换订单状态（履约完成）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRedemptionStatus(Long redemptionId, String status, String fulfillmentNotes) {
        PointsRedemption redemption = pointsRedemptionMapper.selectById(redemptionId);
        if (redemption == null) {
            throw new RuntimeException("兑换记录不存在");
        }

        redemption.setStatus(status);
        redemption.setFulfillmentNotes(fulfillmentNotes);
        pointsRedemptionMapper.updateById(redemption);

        log.info("更新兑换记录{}状态为：{}", redemptionId, status);
    }
}
