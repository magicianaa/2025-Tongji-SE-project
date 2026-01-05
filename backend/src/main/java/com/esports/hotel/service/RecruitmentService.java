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
import java.util.ArrayList;
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
    private final RecruitmentApplicationMapper recruitmentApplicationMapper;  // 申请记录Mapper
    
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
     * 申请加入招募（保存申请记录 + 实时推送给发布者）
     */
    @Transactional
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
        
        // 检查是否已经申请过（状态为PENDING）
        LambdaQueryWrapper<RecruitmentApplication> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(RecruitmentApplication::getRecruitmentId, recruitmentId)
                   .eq(RecruitmentApplication::getApplicantId, guestId)
                   .eq(RecruitmentApplication::getStatus, "PENDING");
        RecruitmentApplication existApplication = recruitmentApplicationMapper.selectOne(existWrapper);
        if (existApplication != null) {
            throw new RuntimeException("您已申请过该招募，请等待处理");
        }
        
        // 保存申请记录到数据库
        RecruitmentApplication application = new RecruitmentApplication();
        application.setRecruitmentId(recruitmentId);
        application.setApplicantId(guestId);
        application.setStatus("PENDING");
        application.setApplyTime(LocalDateTime.now());
        recruitmentApplicationMapper.insert(application);
        
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
        
        System.out.println("Guest [" + guest.getRealName() + "] 申请加入招募 [" + recruitmentId + "]，申请已保存并通过WebSocket推送");
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
        
        // 更新申请记录状态为已同意
        LambdaQueryWrapper<RecruitmentApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(RecruitmentApplication::getRecruitmentId, recruitmentId)
                  .eq(RecruitmentApplication::getApplicantId, applicantId)
                  .eq(RecruitmentApplication::getStatus, "PENDING");
        RecruitmentApplication application = recruitmentApplicationMapper.selectOne(appWrapper);
        if (application != null) {
            application.setStatus("APPROVED");
            application.setHandleTime(LocalDateTime.now());
            recruitmentApplicationMapper.updateById(application);
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
    @Transactional
    public void rejectApplication(Long recruitmentId, Long applicantId, Long captainId) {
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        if (!recruitment.getPublisherId().equals(captainId)) {
            throw new RuntimeException("只有发布者才能拒绝申请");
        }
        
        // 更新申请记录状态为已拒绝
        LambdaQueryWrapper<RecruitmentApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(RecruitmentApplication::getRecruitmentId, recruitmentId)
                  .eq(RecruitmentApplication::getApplicantId, applicantId)
                  .eq(RecruitmentApplication::getStatus, "PENDING");
        RecruitmentApplication application = recruitmentApplicationMapper.selectOne(appWrapper);
        if (application != null) {
            application.setStatus("REJECTED");
            application.setHandleTime(LocalDateTime.now());
            recruitmentApplicationMapper.updateById(application);
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
     * 获取招募的申请列表（发布者查看）
     */
    public List<RecruitmentApplicationResponse> getRecruitmentApplications(Long recruitmentId, Long guestId) {
        // 验证招募存在
        Recruitment recruitment = recruitmentMapper.selectById(recruitmentId);
        if (recruitment == null) {
            throw new RuntimeException("招募信息不存在");
        }
        
        // 验证是发布者本人
        if (!recruitment.getPublisherId().equals(guestId)) {
            throw new RuntimeException("只有发布者才能查看申请列表");
        }
        
        // 查询申请列表
        LambdaQueryWrapper<RecruitmentApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecruitmentApplication::getRecruitmentId, recruitmentId)
               .orderByDesc(RecruitmentApplication::getApplyTime);
        List<RecruitmentApplication> applications = recruitmentApplicationMapper.selectList(wrapper);
        
        // 转换为响应DTO
        List<RecruitmentApplicationResponse> responses = new ArrayList<>();
        for (RecruitmentApplication app : applications) {
            RecruitmentApplicationResponse response = new RecruitmentApplicationResponse();
            response.setApplicationId(app.getApplicationId());
            response.setRecruitmentId(app.getRecruitmentId());
            response.setApplicantId(app.getApplicantId());
            response.setStatus(app.getStatus());
            response.setApplyTime(app.getApplyTime());
            response.setHandleTime(app.getHandleTime());
            
            // 填充申请者信息
            Guest applicant = guestMapper.selectById(app.getApplicantId());
            if (applicant != null) {
                response.setApplicantName(applicant.getRealName());
            }
            
            // 获取申请者房间号
            LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(CheckInRecord::getGuestId, app.getApplicantId())
                        .isNull(CheckInRecord::getActualCheckout)
                        .orderByDesc(CheckInRecord::getActualCheckin)
                        .last("LIMIT 1");
            CheckInRecord checkInRecord = checkInRecordMapper.selectOne(recordWrapper);
            if (checkInRecord != null && checkInRecord.getRoomId() != null) {
                Room room = roomMapper.selectById(checkInRecord.getRoomId());
                if (room != null) {
                    response.setApplicantRoom(room.getRoomNo());
                }
            }
            
            // 获取申请者游戏档案（对应招募的游戏类型）
            LambdaQueryWrapper<GamingProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(GamingProfile::getGuestId, app.getApplicantId())
                         .eq(GamingProfile::getGameType, recruitment.getGameType())
                         .orderByDesc(GamingProfile::getCreatedAt)
                         .last("LIMIT 1");
            GamingProfile profile = gamingProfileMapper.selectOne(profileWrapper);
            if (profile != null) {
                response.setApplicantRank(profile.getRank());
                response.setApplicantPosition(profile.getPreferredPosition());
            }
            
            responses.add(response);
        }
        
        return responses;
    }
    
    /**
     * 获取我发布的所有招募的待处理申请数量
     */
    public int getPendingApplicationsCount(Long guestId) {
        // 获取我的所有招募ID
        LambdaQueryWrapper<Recruitment> recruitmentWrapper = new LambdaQueryWrapper<>();
        recruitmentWrapper.eq(Recruitment::getPublisherId, guestId);
        List<Recruitment> myRecruitments = recruitmentMapper.selectList(recruitmentWrapper);
        
        if (myRecruitments.isEmpty()) {
            return 0;
        }
        
        List<Long> recruitmentIds = myRecruitments.stream()
                .map(Recruitment::getRecruitmentId)
                .collect(Collectors.toList());
        
        // 统计待处理申请数量
        LambdaQueryWrapper<RecruitmentApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.in(RecruitmentApplication::getRecruitmentId, recruitmentIds)
                  .eq(RecruitmentApplication::getStatus, "PENDING");
        Long count = recruitmentApplicationMapper.selectCount(appWrapper);
        return count != null ? count.intValue() : 0;
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
        
        // 统计待处理申请人数
        LambdaQueryWrapper<RecruitmentApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(RecruitmentApplication::getRecruitmentId, recruitment.getRecruitmentId())
                  .eq(RecruitmentApplication::getStatus, "PENDING");
        Long pendingCount = recruitmentApplicationMapper.selectCount(appWrapper);
        response.setCurrentApplicants(pendingCount != null ? pendingCount.intValue() : 0);
        
        return response;
    }
}