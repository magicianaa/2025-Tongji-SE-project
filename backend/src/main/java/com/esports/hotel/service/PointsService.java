package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.PointTransaction;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.PointTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final GuestMapper guestMapper;
    private final PointTransactionMapper pointTransactionMapper;

    /**
     * 增加积分（任务奖励、退房奖励等）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long guestId, Integer points, String transactionType, Long relatedId, String description) {
        if (points <= 0) {
            log.warn("积分数量必须大于0");
            return;
        }

        // 获取住客信息
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("住客不存在");
        }

        // 更新积分余额
        int oldBalance = guest.getCurrentPoints();
        int newBalance = oldBalance + points;
        guest.setCurrentPoints(newBalance);

        // 更新经验值（用于等级升级）
        int oldExp = guest.getExperiencePoints();
        int newExp = oldExp + points;
        guest.setExperiencePoints(newExp);

        // 检查并升级会员等级
        upgradeMemberLevel(guest, newExp);

        guestMapper.updateById(guest);

        // 记录积分流水
        PointTransaction transaction = new PointTransaction();
        transaction.setGuestId(guestId);
        transaction.setAmount(points);
        transaction.setTransactionType(transactionType);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transaction.setBalanceAfter(newBalance);
        pointTransactionMapper.insert(transaction);

        log.info("住客{}获得{}积分，类型：{}，当前余额：{}", guestId, points, transactionType, newBalance);
    }

    /**
     * 扣减积分（兑换商品等）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long guestId, Integer points, String transactionType, Long relatedId, String description) {
        if (points <= 0) {
            throw new RuntimeException("扣减积分数量必须大于0");
        }

        // 获取住客信息
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("住客不存在");
        }

        // 检查余额
        int oldBalance = guest.getCurrentPoints();
        if (oldBalance < points) {
            throw new RuntimeException("积分余额不足，当前余额：" + oldBalance + "，需要：" + points);
        }

        // 更新积分余额
        int newBalance = oldBalance - points;
        guest.setCurrentPoints(newBalance);
        guestMapper.updateById(guest);

        // 记录积分流水
        PointTransaction transaction = new PointTransaction();
        transaction.setGuestId(guestId);
        transaction.setAmount(-points);  // 负数表示扣减
        transaction.setTransactionType(transactionType);
        transaction.setRelatedId(relatedId);
        transaction.setDescription(description);
        transaction.setBalanceAfter(newBalance);
        pointTransactionMapper.insert(transaction);

        log.info("住客{}消费{}积分，类型：{}，当前余额：{}", guestId, points, transactionType, newBalance);
    }

    /**
     * 检查并升级会员等级
     * 青铜->白银: 1000经验，折扣5%
     * 白银->黄金: 5000经验，折扣10%
     * 黄金->铂金: 10000经验，折扣15%
     */
    private void upgradeMemberLevel(Guest guest, int totalExp) {
        String currentLevel = guest.getMemberLevel();
        String newLevel = currentLevel;

        if (totalExp >= 10000 && !"PLATINUM".equals(currentLevel)) {
            newLevel = "PLATINUM";
        } else if (totalExp >= 5000 && "BRONZE".equals(currentLevel) || "SILVER".equals(currentLevel)) {
            newLevel = "GOLD";
        } else if (totalExp >= 1000 && "BRONZE".equals(currentLevel)) {
            newLevel = "SILVER";
        }

        if (!newLevel.equals(currentLevel)) {
            guest.setMemberLevel(newLevel);
            log.info("住客{}会员等级升级：{} -> {}，总经验：{}", guest.getGuestId(), currentLevel, newLevel, totalExp);
        }
    }

    /**
     * 获取会员折扣率
     * 青铜: 1.00 (无折扣)
     * 白银: 0.95 (5%折扣)
     * 黄金: 0.90 (10%折扣)
     * 铂金: 0.85 (15%折扣)
     */
    public double getMemberDiscountRate(String memberLevel) {
        return switch (memberLevel) {
            case "SILVER" -> 0.95;
            case "GOLD" -> 0.90;
            case "PLATINUM" -> 0.85;
            default -> 1.00;  // BRONZE
        };
    }

    /**
     * 获取住客当前积分余额
     */
    public Integer getPointsBalance(Long guestId) {
        Guest guest = guestMapper.selectById(guestId);
        return guest != null ? guest.getCurrentPoints() : 0;
    }
}
