package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.TeamRequest;
import com.esports.hotel.dto.TeamResponse;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 战队服务
 */
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final GuestMapper guestMapper;
    private final GamingProfileMapper gamingProfileMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    
    /**
     * 创建战队
     */
    @Transactional
    public TeamResponse createTeam(Long captainId, TeamRequest request) {
        // 验证队长是否存在
        Guest captain = guestMapper.selectById(captainId);
        if (captain == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        // 验证队长是否已入住
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getGuestId, captainId)
                     .eq(CheckInRecord::getIsGamingAuthActive, true)
                     .isNull(CheckInRecord::getActualCheckout)
                     .orderByDesc(CheckInRecord::getActualCheckin)
                     .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
        
        if (checkInRecord == null) {
            throw new RuntimeException("您尚未入住，无法创建战队");
        }
        
        // 验证队长是否已在活跃的战队中
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getGuestId, captainId)
                     .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember existingMember = teamMemberMapper.selectOne(memberWrapper);
        
        if (existingMember != null) {
            throw new RuntimeException("您已在一个战队中，无法创建新战队");
        }
        
        // 创建战队
        Team team = new Team();
        team.setTeamName(request.getTeamName());
        team.setCaptainId(captainId);
        team.setGameType(request.getGameType());
        team.setCreateTime(LocalDateTime.now());
        team.setStatus("ACTIVE");
        team.setTotalPlaytimeMinutes(0);
        
        teamMapper.insert(team);
        
        // 队长自动加入战队
        TeamMember captainMember = new TeamMember();
        captainMember.setTeamId(team.getTeamId());
        captainMember.setGuestId(captainId);
        captainMember.setJoinTime(LocalDateTime.now());
        captainMember.setStatus("ACTIVE");
        
        teamMemberMapper.insert(captainMember);
        
        return convertToResponse(team);
    }
    
    /**
     * 加入战队
     */
    @Transactional
    public void joinTeam(Long teamId, Long guestId) {
        // 验证战队是否存在且活跃
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        if (!"ACTIVE".equals(team.getStatus())) {
            throw new RuntimeException("战队已解散");
        }
        
        // 验证Guest是否存在
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        // 验证Guest是否已入住
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getGuestId, guestId)
                     .eq(CheckInRecord::getIsGamingAuthActive, true)
                     .isNull(CheckInRecord::getActualCheckout)
                     .orderByDesc(CheckInRecord::getActualCheckin)
                     .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
        
        if (checkInRecord == null) {
            throw new RuntimeException("您尚未入住，无法加入战队");
        }
        
        // 验证Guest是否已在活跃的战队中
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getGuestId, guestId)
                     .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember existingMember = teamMemberMapper.selectOne(memberWrapper);
        
        if (existingMember != null) {
            throw new RuntimeException("您已在一个战队中");
        }
        
        // 加入战队
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setGuestId(guestId);
        member.setJoinTime(LocalDateTime.now());
        member.setStatus("ACTIVE");
        
        teamMemberMapper.insert(member);
    }
    
    /**
     * 离开战队
     */
    @Transactional
    public void leaveTeam(Long teamId, Long guestId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        
        // 队长不能直接离开，需要先解散战队
        if (team.getCaptainId().equals(guestId)) {
            throw new RuntimeException("队长不能离开战队，请先解散战队或转让队长");
        }
        
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getGuestId, guestId)
               .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember member = teamMemberMapper.selectOne(wrapper);
        
        if (member == null) {
            throw new RuntimeException("您不在该战队中");
        }
        
        member.setStatus("LEFT");
        member.setLeaveTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }
    
    /**
     * 踢出成员（仅队长可操作）
     */
    @Transactional
    public void kickMember(Long teamId, Long memberId, Long captainId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("只有队长可以踢出成员");
        }
        
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getGuestId, memberId)
               .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember member = teamMemberMapper.selectOne(wrapper);
        
        if (member == null) {
            throw new RuntimeException("该成员不在战队中");
        }
        
        if (memberId.equals(captainId)) {
            throw new RuntimeException("队长不能踢出自己");
        }
        
        member.setStatus("KICKED");
        member.setLeaveTime(LocalDateTime.now());
        teamMemberMapper.updateById(member);
    }
    
    /**
     * 解散战队（仅队长可操作）
     */
    @Transactional
    public void disbandTeam(Long teamId, Long captainId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("只有队长可以解散战队");
        }
        
        if (!"ACTIVE".equals(team.getStatus())) {
            throw new RuntimeException("战队已解散");
        }
        
        // 解散战队
        team.setStatus("DISBANDED");
        team.setDisbandTime(LocalDateTime.now());
        teamMapper.updateById(team);
        
        // 更新所有活跃成员状态
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId)
               .eq(TeamMember::getStatus, "ACTIVE");
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        for (TeamMember member : members) {
            member.setStatus("LEFT");
            member.setLeaveTime(now);
            teamMemberMapper.updateById(member);
        }
    }
    
    /**
     * 获取战队详情
     */
    public TeamResponse getTeam(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        return convertToResponse(team);
    }
    
    /**
     * 获取我的战队
     * @return 战队信息，如果未加入任何战队则返回null
     */
    public TeamResponse getMyTeam(Long guestId) {
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getGuestId, guestId)
                     .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember member = teamMemberMapper.selectOne(memberWrapper);
        
        if (member == null) {
            // 未加入任何战队，返回null而不是抛异常
            return null;
        }
        
        Team team = teamMapper.selectById(member.getTeamId());
        if (team == null) {
            // 战队不存在，返回null
            return null;
        }
        
        return convertToResponse(team);
    }
    
    /**
     * 更新战队游戏时长
     */
    @Transactional
    public void updatePlaytime(Long teamId, Integer additionalMinutes) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("战队不存在");
        }
        
        if (!"ACTIVE".equals(team.getStatus())) {
            throw new RuntimeException("战队已解散");
        }
        
        team.setTotalPlaytimeMinutes(team.getTotalPlaytimeMinutes() + additionalMinutes);
        teamMapper.updateById(team);
    }
    
    /**
     * 转换为响应DTO
     */
    private TeamResponse convertToResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setTeamId(team.getTeamId());
        response.setTeamName(team.getTeamName());
        response.setCaptainId(team.getCaptainId());
        response.setGameType(team.getGameType());
        response.setCreateTime(team.getCreateTime());
        response.setDisbandTime(team.getDisbandTime());
        response.setStatus(team.getStatus());
        response.setTotalPlaytimeMinutes(team.getTotalPlaytimeMinutes());
        
        // 填充队长信息
        Guest captain = guestMapper.selectById(team.getCaptainId());
        if (captain != null) {
            response.setCaptainName(captain.getRealName());
        }
        
        // 填充成员列表
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, team.getTeamId())
               .eq(TeamMember::getStatus, "ACTIVE");
        List<TeamMember> members = teamMemberMapper.selectList(wrapper);
        
        List<TeamResponse.TeamMemberInfo> memberInfos = new ArrayList<>();
        for (TeamMember member : members) {
            TeamResponse.TeamMemberInfo info = new TeamResponse.TeamMemberInfo();
            info.setGuestId(member.getGuestId());
            info.setJoinTime(member.getJoinTime());
            info.setStatus(member.getStatus());
            info.setIsCaptain(member.getGuestId().equals(team.getCaptainId())); // 设置是否为队长
            
            Guest guest = guestMapper.selectById(member.getGuestId());
            if (guest != null) {
                info.setGuestName(guest.getRealName());
                
                // 查询游戏档案
                LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
                profileWrapper.eq(GamingProfile::getGuestId, member.getGuestId())
                             .eq(GamingProfile::getGameType, team.getGameType())
                             .orderByDesc(GamingProfile::getCreatedAt)
                             .last("LIMIT 1");
                GamingProfile profile = gamingProfileMapper.selectOne(profileWrapper);
                
                if (profile != null) {
                    info.setRank(profile.getRank());
                    info.setPosition(profile.getPreferredPosition());
                }
            }
            
            memberInfos.add(info);
        }
        
        response.setMembers(memberInfos);
        
        return response;
    }
}
