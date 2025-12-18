package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esports.hotel.dto.TaskRecordVO;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskRecordMapper taskRecordMapper;
    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final PointsService pointsService;

    /**
     * 住客提交任务完成凭证
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskRecord submitTask(Long guestId, Long taskId, String proofImageUrl, String proofDescription) {
        // 检查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getIsActive()) {
            throw new RuntimeException("任务不存在或已下架");
        }

        // 获取住客当前入住记录
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, guestId)
               .isNull(CheckInRecord::getActualCheckout)
               .orderByDesc(CheckInRecord::getActualCheckin)
               .last("LIMIT 1");
        CheckInRecord checkInRecord = checkInRecordMapper.selectOne(wrapper);
        
        if (checkInRecord == null) {
            throw new RuntimeException("未找到有效的入住记录，请先入住");
        }

        // 如果任务不可重复，检查是否已提交过
        if (!task.getIsRepeatable()) {
            LambdaQueryWrapper<TaskRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(TaskRecord::getTaskId, taskId)
                        .eq(TaskRecord::getGuestId, guestId)
                        .in(TaskRecord::getAuditStatus, "PENDING", "APPROVED");
            long count = taskRecordMapper.selectCount(recordWrapper);
            if (count > 0) {
                throw new RuntimeException("该任务不可重复提交");
            }
        }

        // 创建任务记录
        TaskRecord record = new TaskRecord();
        record.setTaskId(taskId);
        record.setGuestId(guestId);
        record.setRecordId(checkInRecord.getRecordId());
        record.setProofImageUrl(proofImageUrl);
        record.setProofDescription(proofDescription);
        record.setAuditStatus("PENDING");
        taskRecordMapper.insert(record);

        log.info("住客{}提交任务{}完成凭证", guestId, taskId);
        return record;
    }

    /**
     * 员工审核任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditTask(Long taskRecordId, Long auditorId, boolean approved, String rejectReason) {
        TaskRecord record = taskRecordMapper.selectById(taskRecordId);
        if (record == null) {
            throw new RuntimeException("任务记录不存在");
        }

        if (!"PENDING".equals(record.getAuditStatus())) {
            throw new RuntimeException("该任务已审核，无法重复操作");
        }

        // 更新审核状态
        record.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        record.setRejectReason(rejectReason);
        taskRecordMapper.updateById(record);

        // 如果审核通过，发放积分奖励
        if (approved) {
            Task task = taskMapper.selectById(record.getTaskId());
            if (task != null && task.getRewardPoints() > 0) {
                pointsService.addPoints(
                    record.getGuestId(),
                    task.getRewardPoints(),
                    "TASK_REWARD",
                    taskRecordId,
                    "完成任务：" + task.getTaskName()
                );
            }
        }

        log.info("员工{}审核任务记录{}：{}", auditorId, taskRecordId, approved ? "通过" : "拒绝");
    }

    /**
     * 获取任务记录列表（带住客信息）
     */
    public List<TaskRecordVO> getTaskRecordsWithDetails(String auditStatus) {
        LambdaQueryWrapper<TaskRecord> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(TaskRecord::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(TaskRecord::getSubmitTime);
        List<TaskRecord> records = taskRecordMapper.selectList(wrapper);

        List<TaskRecordVO> voList = new ArrayList<>();
        for (TaskRecord record : records) {
            TaskRecordVO vo = new TaskRecordVO();
            BeanUtils.copyProperties(record, vo);

            // 填充任务信息
            Task task = taskMapper.selectById(record.getTaskId());
            if (task != null) {
                vo.setTaskName(task.getTaskName());
                vo.setRewardPoints(task.getRewardPoints());
            }

            // 填充住客信息
            Guest guest = guestMapper.selectById(record.getGuestId());
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
     * 获取住客的任务提交记录
     */
    public List<TaskRecordVO> getGuestTaskRecords(Long guestId) {
        LambdaQueryWrapper<TaskRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskRecord::getGuestId, guestId)
               .orderByDesc(TaskRecord::getSubmitTime);
        List<TaskRecord> records = taskRecordMapper.selectList(wrapper);

        List<TaskRecordVO> voList = new ArrayList<>();
        for (TaskRecord record : records) {
            TaskRecordVO vo = new TaskRecordVO();
            BeanUtils.copyProperties(record, vo);

            // 填充任务信息
            Task task = taskMapper.selectById(record.getTaskId());
            if (task != null) {
                vo.setTaskName(task.getTaskName());
                vo.setRewardPoints(task.getRewardPoints());
            }

            voList.add(vo);
        }

        return voList;
    }
}
