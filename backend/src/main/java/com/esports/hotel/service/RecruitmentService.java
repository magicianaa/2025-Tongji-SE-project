package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.dto.*;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 招募服务
 */
@Service
@RequiredArgsConstructor
public class RecruitmentService {
    
    private final RecruitmentMapper recruitmentMapper;
    private final GamingProfileMapper gamingProfileMapper;
    private final GuestMapper guestMapper;
    private final TeamService teamService;
    private final TeamMemberMapper teamMemberMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final RoomMapper roomMapper;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket消息发送
    
    /**
     * 发布招募信息
     */
    @Transactional
    public RecruitmentResponse publishRecruitment(Long guestId, RecruitmentRequest request) {
        // 验证Guest是否存在
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        // 创建招募信息
        Recruitment recruitment = new Recruitment();
        recruitment.setPublisherId(guestId);
        recruitment.setGameType(request.getGameType());
        recruitment.setRequiredRank(request.getRequiredRank());
        recruitment.setRequiredPosition(request.getRequiredPosition());
        recruitment.setDescription(request.getDescription());
        recruitment.setMaxMembers(request.getMaxMembers());
        recruitment.setStatus("OPEN");
        recruitment.setPublishTime(LocalDateTime.now());
        recruitment.setExpireTime(request.getExpireTime() != null ? request.getExpireTime() : LocalDateTime.now().plusDays(7));
        
        recruitmentMapper.insert(recruitment);
        return convertToResponse(recruitment);
    }
    
    /**
     * 查询招募列表（分页+筛选）
     */
    public Page<RecruitmentResponse> searchRecruitments(MatchingQuery query) {
        Page<Recruitment> page = new Page<>(query.getPage(), query.getSize());
        
        LambdaQueryWrapper<Recruitment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recruitment::getStatus, "OPEN");
        
        if (StringUtils.hasText(query.getGameType())) {
            wrapper.eq(Recruitment::getGameType, query.getGameType());
        }
        
        if (StringUtils.hasText(query.getRank())) {
            wrapper.like(Recruitment::getRequiredRank, query.getRank());
        }
        
        if (StringUtils.hasText(query.getPosition())) {
            wrapper.like(Recruitment::getRequiredPosition, query.getPosition());
        }
        
        // 过滤未过期的招募
        wrapper.gt(Recruitment::getExpireTime, LocalDateTime.now());
        wrapper.orderByDesc(Recruitment::getPublishTime);
        
        Page<Recruitment> recruitmentPage = recruitmentMapper.selectPage(page, wrapper);
        
        // 转换为响应DTO
        Page<RecruitmentResponse> responsePage = new Page<>();
        responsePage.setCurrent(recruitmentPage.getCurrent());
        responsePage.setSize(recruitmentPage.getSize());
        responsePage.setTotal(recruitmentPage.getTotal());
        
        List<RecruitmentResponse> records = recruitmentPage.getRecords()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);
        
        return responsePage;
    }
    
    /**
     * 获取招募详情
     */
    public RecruitmentResponse getRecruitment(Long recruitmentId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        return convertToResponse(recruitment);
    }
    
    /**
     * 关闭招募
     */
    @Transactional
    public void closeRecruitment(Long recruitmentId, Long guestId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!recruitment.getPublisherId().equals(guestId)) {
            throw new RuntimeException("无权关闭此招募");
        }
        
        recruitment.setStatus("CLOSED");
        recruitmentMapper.updateById(recruitment);
    }
    
    /**
     * 删除招募
     */
    @Transactional
    public void deleteRecruitment(Long recruitmentId, Long guestId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!recruitment.getPublisherId().equals(guestId)) {
            throw new RuntimeException("无权删除此招募");
        }
        
        recruitmentMapper.deleteById(recruitmentId);
    }
    
    /**
     * 申请加入招募（实时推送给发布者）
     */
    public void applyToRecruitment(Long recruitmentId, Long guestId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!"OPEN".equals(recruitment.getStatus())) {
            throw new RuntimeException("该招募已关闭");
        }
        
        if (recruitment.getPublisherId().equals(guestId)) {
            throw new RuntimeException("不能申请自己发布的招募");
        }
        
        // 验证Guest是否存在
        Guest guest = guestMapper.selectById(guestId);
        if (guest == null) {
            throw new RuntimeException("Guest不存在");
        }
        
        // 获取申请者房间号
        String roomNumber = null;
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, guestId)
               .isNull(CheckInRecord::getActualCheckout)
               .orderByDesc(CheckInRecord::getActualCheckin)
               .last("LIMIT 1");
        CheckInRecord record = checkInRecordMapper.selectOne(wrapper);
        if (record != null && record.getRoomId() != null) {
            Room room = roomMapper.selectById(record.getRoomId());
            if (room != null) {
                roomNumber = room.getRoomNo();
            }
        }
        
        // 构建通知消息
        RecruitmentNotification notification = new RecruitmentNotification();
        notification.setType("NEW_APPLICATION");
        notification.setRecruitmentId(recruitment.getRecruitmentId());
        notification.setApplicantId(guestId);
        notification.setApplicantName(guest.getRealName());
        notification.setApplicantRoom(roomNumber);
        notification.setGameType(recruitment.getGameType());
        notification.setMessage(guest.getRealName() + " 申请加入您的招募");
        notification.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 通过WebSocket实时推送给发布者
        String targetUser = recruitment.getPublisherId().toString();
        
        System.out.println("=== WebSocket推送详情 ===");
        System.out.println("招募ID: " + recruitmentId);
        System.out.println("发布者ID (publisherId): " + recruitment.getPublisherId());
        System.out.println("申请者ID (guestId): " + guestId);
        System.out.println("申请者姓名: " + guest.getRealName());
        System.out.println("目标用户: " + targetUser);
        System.out.println("通知类型: " + notification.getType());
        System.out.println("通知内容: " + notification.getMessage());
        System.out.println("订阅目标: /user/" + targetUser + "/queue/recruitment");
        
        // 使用convertAndSendToUser，让Spring自动处理用户路由
        // Spring会自动添加 /user/ 前缀，所以只需要指定队列名称
        try {
            messagingTemplate.convertAndSendToUser(targetUser, "/queue/recruitment", notification);
            System.out.println("✓ 消息已通过convertAndSendToUser发送到用户: " + targetUser);
        } catch (Exception e) {
            System.err.println("✗ WebSocket推送失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("========================");
        
        System.out.println("Guest [" + guest.getRealName() + "] 申请加入招募 [" + recruitmentId + "]，已通过WebSocket推送");
    }
    
    /**
     * 同意申请并创建/加入战队
     */
    @Transactional
    public void approveApplication(Long recruitmentId, Long applicantId, Long captainId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!recruitment.getPublisherId().equals(captainId)) {
            throw new RuntimeException("只有发布者才能同意申请");
        }
        
        // 创建或加入战队
        createOrJoinTeam(recruitment, applicantId);
        
        // 通过WebSocket通知申请者结果
        Guest applicant = guestMapper.selectById(applicantId);
        if (applicant != null) {
            RecruitmentNotification notification = new RecruitmentNotification();
            notification.setType("APPLICATION_RESULT");
            notification.setRecruitmentId(recruitmentId);
            notification.setApproved(true);
            notification.setMessage("您的申请已被同意，已加入战队！");
            notification.setGameType(recruitment.getGameType());
            notification.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            messagingTemplate.convertAndSendToUser(
                applicantId.toString(), 
                "/queue/recruitment", 
                notification
            );
        }
    }
    
    /**
     * 拒绝申请（实时通知）
     */
    public void rejectApplication(Long recruitmentId, Long applicantId, Long captainId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!recruitment.getPublisherId().equals(captainId)) {
            throw new RuntimeException("只有发布者才能拒绝申请");
        }
        
        // 通过WebSocket通知申请者被拒绝
        Guest applicant = guestMapper.selectById(applicantId);
        if (applicant != null) {
            RecruitmentNotification notification = new RecruitmentNotification();
            notification.setType("APPLICATION_RESULT");
            notification.setRecruitmentId(recruitmentId);
            notification.setApproved(false);
            notification.setMessage("您的申请已被拒绝");
            notification.setGameType(recruitment.getGameType());
            notification.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            messagingTemplate.convertAndSendToUser(
                applicantId.toString(), 
                "/queue/recruitment", 
                notification
            );
        }
    }
    
    /**
     * 创建战队或加入现有战队
     */
    private void createOrJoinTeam(Recruitment recruitment, Long newMemberId) {
        Long captainId = recruitment.getPublisherId();
        
        // 检查队长是否已有战队
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getGuestId, captainId)
                     .eq(TeamMember::getStatus, "ACTIVE");
        TeamMember captainMember = teamMemberMapper.selectOne(memberWrapper);
        
        Long teamId;
        
        if (captainMember != null) {
            // 队长已有战队，直接加入
            teamId = captainMember.getTeamId();
        } else {
            // 队长没有战队，创建新战队
            TeamRequest teamRequest = new TeamRequest();
            teamRequest.setTeamName(recruitment.getPublisherId() + "的战队");
            teamRequest.setGameType(recruitment.getGameType());
            
            TeamResponse teamResponse = teamService.createTeam(captainId, teamRequest);
            teamId = teamResponse.getTeamId();
        }
        
        // 添加新成员到战队
        TeamMember newMember = new TeamMember();
        newMember.setTeamId(teamId);
        newMember.setGuestId(newMemberId);
        newMember.setJoinTime(LocalDateTime.now());
        newMember.setStatus("ACTIVE");
        
        teamMemberMapper.insert(newMember);
        
        System.out.println("Guest [" + newMemberId + "] 已加入战队 [" + teamId + "]");
    }
    
    /**
     * 获取我发布的招募列表
     */
    public List<RecruitmentResponse> getMyRecruitments(Long guestId) {
        LambdaQueryWrapper<Recruitment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recruitment::getPublisherId, guestId)
               .orderByDesc(Recruitment::getPublishTime);
        
        List<Recruitment> recruitments = recruitmentMapper.selectList(wrapper);
        return recruitments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应DTO
     */
    private RecruitmentResponse convertToResponse(Recruitment recruitment) {
        RecruitmentResponse response = new RecruitmentResponse();
        response.setRecruitmentId(recruitment.getRecruitmentId());
        response.setPublisherId(recruitment.getPublisherId());
        response.setGameType(recruitment.getGameType());
        response.setRequiredRank(recruitment.getRequiredRank());
        response.setRequiredPosition(recruitment.getRequiredPosition());
        response.setDescription(recruitment.getDescription());
        response.setMaxMembers(recruitment.getMaxMembers());
        response.setStatus(recruitment.getStatus());
        response.setPublishTime(recruitment.getPublishTime());
        response.setExpireTime(recruitment.getExpireTime());
        
        // 填充发布者信息
        Guest publisher = guestMapper.selectById(recruitment.getPublisherId());
        if (publisher != null) {
            response.setPublisherName(publisher.getRealName());
            
            // 查询发布者的游戏档案
            LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(GamingProfile::getGuestId, recruitment.getPublisherId())
                         .eq(GamingProfile::getGameType, recruitment.getGameType())
                         .orderByDesc(GamingProfile::getCreatedAt)
                         .last("LIMIT 1");
            GamingProfile profile = gamingProfileMapper.selectOne(profileWrapper);
            
            if (profile != null) {
                response.setPublisherRank(profile.getRank());
                response.setPublisherPosition(profile.getPreferredPosition());
            }
        }
        
        // 这里可以添加已应征人数统计（需要后续实现应征功能）
        response.setCurrentApplicants(0);
        
        return response;
    }
}