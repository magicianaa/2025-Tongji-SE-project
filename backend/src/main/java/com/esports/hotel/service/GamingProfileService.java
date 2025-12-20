package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.GamingProfileRequest;
import com.esports.hotel.dto.GamingProfileResponse;
import com.esports.hotel.entity.CheckInRecord;
import com.esports.hotel.entity.GamingProfile;
import com.esports.hotel.entity.Guest;
import com.esports.hotel.entity.Room;
import com.esports.hotel.mapper.CheckInRecordMapper;
import com.esports.hotel.mapper.GamingProfileMapper;
import com.esports.hotel.mapper.GuestMapper;
import com.esports.hotel.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏档案服务
 */
@Service
@RequiredArgsConstructor
public class GamingProfileService {
    
    private final GamingProfileMapper gamingProfileMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final GuestMapper guestMapper;
    private final RoomMapper roomMapper;
    
    /**
     * 创建或更新游戏档案
     */
    @Transactional
    public GamingProfileResponse createOrUpdateProfile(Long guestId, GamingProfileRequest request) {
        // 验证Guest是否存在
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        // 查询Guest当前的入住记录
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getGuestId, guestId)
                     .eq(CheckInRecord::getIsGamingAuthActive, true)
                     .isNull(CheckInRecord::getActualCheckout)
                     .orderByDesc(CheckInRecord::getActualCheckin)
                     .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
        
        if (checkInRecord == null) {
            throw new RuntimeException("您尚未入住，无法创建游戏档案");
        }
        
        // 查询是否已存在该入住记录的游戏档案
        LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(GamingProfile::getRecordId, checkInRecord.getRecordId())
                     .eq(GamingProfile::getGameType, request.getGameType());
        GamingProfile existingProfile = gamingProfileMapper.selectOne(profileWrapper);
        
        if (existingProfile != null) {
            // 更新现有档案
            existingProfile.setGameAccount(request.getGameAccount());
            existingProfile.setRank(request.getRank());
            existingProfile.setPreferredPosition(request.getPreferredPosition());
            existingProfile.setPlayStyle(request.getPlayStyle());
            existingProfile.setIsLookingForTeam(request.getIsLookingForTeam());
            gamingProfileMapper.updateById(existingProfile);
            return convertToResponse(existingProfile);
        } else {
            // 创建新档案
            GamingProfile profile = new GamingProfile();
            profile.setGuestId(guestId);
            profile.setRecordId(checkInRecord.getRecordId());
            profile.setGameType(request.getGameType());
            profile.setGameAccount(request.getGameAccount());
            profile.setRank(request.getRank());
            profile.setPreferredPosition(request.getPreferredPosition());
            profile.setPlayStyle(request.getPlayStyle());
            profile.setIsLookingForTeam(request.getIsLookingForTeam());
            profile.setCreatedAt(LocalDateTime.now());
            gamingProfileMapper.insert(profile);
            return convertToResponse(profile);
        }
    }
    
    /**
     * 获取游戏档案详情
     */
    public GamingProfileResponse getProfile(Long profileId) {
        GamingProfile profile = gamingProfileMapper.selectById(profileId);
        if (profile == null) {
            throw new RuntimeException("游戏档案不存在");
        }
        return convertToResponse(profile);
    }
    
    /**
     * 获取Guest当前游戏档案
     */
    public GamingProfileResponse getCurrentProfile(Long guestId, String gameType) {
        // 查询Guest当前的入住记录
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getGuestId, guestId)
                     .eq(CheckInRecord::getIsGamingAuthActive, true)
                     .isNull(CheckInRecord::getActualCheckout)
                     .orderByDesc(CheckInRecord::getActualCheckin)
                     .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
        
        if (checkInRecord == null) {
            throw new RuntimeException("您尚未入住");
        }
        
        // 查询游戏档案
        LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(GamingProfile::getRecordId, checkInRecord.getRecordId())
                     .eq(GamingProfile::getGameType, gameType);
        GamingProfile profile = gamingProfileMapper.selectOne(profileWrapper);
        
        if (profile == null) {
            throw new RuntimeException("游戏档案不存在");
        }
        
        return convertToResponse(profile);
    }
    
    /**
     * 获取所有在线玩家列表（在线大厅）
     */
    public List<GamingProfileResponse> getOnlinePlayers(String gameType, String rank) {
        LambdaQueryWrapper<GamingProfile> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选指定游戏类型
        if (gameType != null && !gameType.isEmpty()) {
            wrapper.eq(GamingProfile::getGameType, gameType);
        }
        
        // 筛选段位
        if (rank != null && !rank.isEmpty()) {
            wrapper.like(GamingProfile::getRank, rank);
        }
        
        wrapper.orderByDesc(GamingProfile::getCreatedAt);
        
        List<GamingProfile> profiles = gamingProfileMapper.selectList(wrapper);
        
        // 过滤出仍在入住中的玩家
        return profiles.stream()
                .filter(profile -> {
                    CheckInRecord record = checkInRecordMapper.selectById(profile.getRecordId());
                    return record != null && 
                           Boolean.TRUE.equals(record.getIsGamingAuthActive()) && 
                           record.getActualCheckout() == null;
                })
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 删除游戏档案
     */
    @Transactional
    public void deleteProfile(Long profileId, Long guestId) {
        GamingProfile profile = gamingProfileMapper.selectById(profileId);
        if (profile == null) {
            throw new RuntimeException("游戏档案不存在");
        }
        
        if (!profile.getGuestId().equals(guestId)) {
            throw new RuntimeException("无权删除此游戏档案");
        }
        
        gamingProfileMapper.deleteById(profileId);
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
}
