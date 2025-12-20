package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.GamingProfileResponse;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.GamingProfile;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Room;
import com.esports.hotel.mapper.CheckInRecordMapper;
import com.esports.hotel.mapper.GamingProfileMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.RoomMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 匹配推荐服务
 */
@Service
@RequiredArgsConstructor
public class MatchingService {
    
    private final GamingProfileMapper gamingProfileMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final GuestMapper guestMapper;
    private final RoomMapper roomMapper;
    
    // 段位权重映射（示例）
    private static final Map<String, Integer> RANK_WEIGHTS = new HashMap<>();
    
    static {
        // 以LOL为例
        RANK_WEIGHTS.put("IRON", 1);
        RANK_WEIGHTS.put("BRONZE", 2);
        RANK_WEIGHTS.put("SILVER", 3);
        RANK_WEIGHTS.put("GOLD", 4);
        RANK_WEIGHTS.put("PLATINUM", 5);
        RANK_WEIGHTS.put("DIAMOND", 6);
        RANK_WEIGHTS.put("MASTER", 7);
        RANK_WEIGHTS.put("GRANDMASTER", 8);
        RANK_WEIGHTS.put("CHALLENGER", 9);
    }
    
    /**
     * 为Guest推荐合适的队友
     */
    public List<GamingProfileResponse> recommendTeammates(Long guestId, String gameType, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 获取当前Guest的游戏档案
        GamingProfile myProfile = getCurrentProfile(guestId, gameType);
        if (myProfile == null) {
            throw new RuntimeException("请先创建游戏档案");
        }
        
        // 查询所有正在寻找队友的档案（排除自己）
        LambdaQueryWrapper<GamingProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GamingProfile::getGameType, gameType)
               .eq(GamingProfile::getIsLookingForTeam, true)
               .ne(GamingProfile::getGuestId, guestId);
        
        List<GamingProfile> candidates = gamingProfileMapper.selectList(wrapper);
        
        // 过滤出仍在入住中的Guest
        candidates = candidates.stream()
                .filter(profile -> isGuestCheckedIn(profile.getGuestId()))
                .collect(Collectors.toList());
        
        // 计算匹配度并排序
        List<ProfileScore> scoredProfiles = new ArrayList<>();
        for (GamingProfile candidate : candidates) {
            double score = calculateMatchScore(myProfile, candidate);
            scoredProfiles.add(new ProfileScore(candidate, score));
        }
        
        // 按匹配度降序排序
        scoredProfiles.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // 取前N个推荐
        return scoredProfiles.stream()
                .limit(limit)
                .map(ps -> convertToResponse(ps.getProfile()))
                .collect(Collectors.toList());
    }
    
    /**
     * 计算两个档案的匹配分数
     */
    private double calculateMatchScore(GamingProfile myProfile, GamingProfile candidateProfile) {
        double score = 0.0;
        
        // 1. 段位相近度 (权重: 0.6)
        double rankScore = calculateRankSimilarity(myProfile.getRank(), candidateProfile.getRank());
        score += rankScore * 0.6;
        
        // 2. 位置互补度 (权重: 0.3)
        double positionScore = calculatePositionCompatibility(myProfile.getPreferredPosition(), candidateProfile.getPreferredPosition());
        score += positionScore * 0.3;
        
        // 3. 游戏风格相似度 (权重: 0.1)
        double styleScore = calculateStyleSimilarity(myProfile.getPlayStyle(), candidateProfile.getPlayStyle());
        score += styleScore * 0.1;
        
        return score;
    }
    
    /**
     * 计算段位相似度
     */
    private double calculateRankSimilarity(String rank1, String rank2) {
        if (!StringUtils.hasText(rank1) || !StringUtils.hasText(rank2)) {
            return 0.5; // 缺失段位信息，返回中等匹配度
        }
        
        // 提取段位主等级（如 "GOLD_III" -> "GOLD"）
        String mainRank1 = rank1.split("_")[0].toUpperCase();
        String mainRank2 = rank2.split("_")[0].toUpperCase();
        
        Integer weight1 = RANK_WEIGHTS.getOrDefault(mainRank1, 5);
        Integer weight2 = RANK_WEIGHTS.getOrDefault(mainRank2, 5);
        
        int diff = Math.abs(weight1 - weight2);
        
        // 差距越小，相似度越高
        if (diff == 0) return 1.0;      // 同段位
        if (diff == 1) return 0.8;      // 相邻段位
        if (diff == 2) return 0.6;      // 相差2个段位
        if (diff == 3) return 0.4;      // 相差3个段位
        return 0.2;                     // 差距过大
    }
    
    /**
     * 计算位置互补度
     */
    private double calculatePositionCompatibility(String pos1, String pos2) {
        if (!StringUtils.hasText(pos1) || !StringUtils.hasText(pos2)) {
            return 0.5;
        }
        
        // 如果位置不同，说明可以互补，分数较高
        if (!pos1.equalsIgnoreCase(pos2)) {
            return 1.0;
        }
        
        // 位置相同，互补度较低
        return 0.3;
    }
    
    /**
     * 计算游戏风格相似度
     */
    private double calculateStyleSimilarity(String style1, String style2) {
        if (!StringUtils.hasText(style1) || !StringUtils.hasText(style2)) {
            return 0.5;
        }
        
        // 简单的字符串相似度判断
        if (style1.equalsIgnoreCase(style2)) {
            return 1.0;
        }
        
        // 可以进一步实现更复杂的风格匹配逻辑
        return 0.5;
    }
    
    /**
     * 获取Guest当前的游戏档案
     */
    private GamingProfile getCurrentProfile(Long guestId, String gameType) {
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getGuestId, guestId)
                     .eq(CheckInRecord::getIsGamingAuthActive, true)
                     .isNull(CheckInRecord::getActualCheckout)
                     .orderByDesc(CheckInRecord::getActualCheckin)
                     .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
        
        if (checkInRecord == null) {
            return null;
        }
        
        LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(GamingProfile::getRecordId, checkInRecord.getRecordId())
                     .eq(GamingProfile::getGameType, gameType);
        return gamingProfileMapper.selectOne(profileWrapper);
    }
    
    /**
     * 检查Guest是否仍在入住中
     */
    private boolean isGuestCheckedIn(Long guestId) {
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, guestId)
               .eq(CheckInRecord::getIsGamingAuthActive, true)
               .isNull(CheckInRecord::getActualCheckout);
        return checkInRecordMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 转换为响应DTO
     */
    private GamingProfileResponse convertToResponse(GamingProfile profile) {
        GamingProfileResponse response = new GamingProfileResponse();
        response.setProfileId(profile.getProfileId());
        response.setGuestId(profile.getGuestId());
        response.setRecordId(profile.getRecordId());
        response.setGameType(profile.getGameType());
        response.setGameAccount(profile.getGameAccount());
        response.setRank(profile.getRank());
        response.setPreferredPosition(profile.getPreferredPosition());
        response.setPlayStyle(profile.getPlayStyle());
        response.setIsLookingForTeam(profile.getIsLookingForTeam());
        response.setCreatedAt(profile.getCreatedAt());
        
        // 填充Guest和Room信息
        Guest guest = guestMapper.selectById(profile.getGuestId());
        if (guest != null) {
            response.setGuestName(guest.getRealName());
        }
        
        CheckInRecord record = checkInRecordMapper.selectById(profile.getRecordId());
        if (record != null) {
            Room room = roomMapper.selectById(record.getRoomId());
            if (room != null) {
                response.setRoomNumber(room.getRoomNo());
            }
        }
        
        return response;
    }
    
    /**
     * 内部类：档案+匹配分数
     */
    @Data
    private static class ProfileScore {
        private GamingProfile profile;
        private double score;
        
        public ProfileScore(GamingProfile profile, double score) {
            this.profile = profile;
            this.score = score;
        }
    }
}
