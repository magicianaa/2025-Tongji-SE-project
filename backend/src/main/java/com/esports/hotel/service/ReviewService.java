package com.esports.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.dto.CreateReviewRequest;
import com.esports.hotel.dto.FollowUpRequest;
import com.esports.hotel.dto.ReviewResponse;
import com.esports.hotel.dto.ReviewSubmitRequest;
import com.esports.hotel.entity.*;
import com.esports.hotel.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final GuestMapper guestMapper;
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;

    /**
     * 提交评价
     */
    @Transactional(rollbackFor = Exception.class)
    public Review submitReview(Long guestId, ReviewSubmitRequest request) {
        // 验证入住记录是否存在
        CheckInRecord checkInRecord = checkInRecordMapper.selectById(request.getRecordId());
        if (checkInRecord == null) {
            throw new RuntimeException("入住记录不存在");
        }

        // 验证是否为本人的入住记录
        if (!checkInRecord.getGuestId().equals(guestId)) {
            throw new RuntimeException("无权评价此入住记录");
        }

        // 验证是否已评价过
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRecordId, request.getRecordId());
        long count = reviewMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("您已评价过此次入住，不能重复评价");
        }

        // 创建评价记录
        Review review = new Review();
        review.setRecordId(request.getRecordId());
        review.setGuestId(guestId);
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setReviewTime(LocalDateTime.now());
        review.setFollowUpStatus("NONE");
        
        reviewMapper.insert(review);
        
        log.info("住客{}提交评价，入住记录ID:{}, 评分:{}", guestId, request.getRecordId(), request.getScore());
        
        return review;
    }

    /**
     * 获取评价列表（分页）
     */
    public Page<ReviewResponse> getReviews(int page, int size, Boolean lowScoreOnly, String followUpStatus) {
        Page<Review> reviewPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        
        // 筛选低分评价
        if (lowScoreOnly != null && lowScoreOnly) {
            wrapper.eq(Review::getIsLowScore, true);
        }
        
        // 筛选回访状态
        if (StringUtils.hasText(followUpStatus)) {
            wrapper.eq(Review::getFollowUpStatus, followUpStatus);
        }
        
        // 按评价时间倒序，低分评价优先
        wrapper.orderByDesc(Review::getIsLowScore)
               .orderByDesc(Review::getReviewTime);
        
        Page<Review> result = reviewMapper.selectPage(reviewPage, wrapper);
        
        // 转换为响应DTO
        Page<ReviewResponse> responsePage = new Page<>(page, size, result.getTotal());
        List<ReviewResponse> responseList = new ArrayList<>();
        
        for (Review review : result.getRecords()) {
            ReviewResponse response = convertToResponse(review);
            responseList.add(response);
        }
        
        responsePage.setRecords(responseList);
        return responsePage;
    }

    /**
     * 获取低分评价列表（专用）
     */
    public List<ReviewResponse> getLowScoreReviews() {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getIsLowScore, true)
               .orderByAsc(Review::getFollowUpStatus) // 未处理的排在前面
               .orderByDesc(Review::getReviewTime);
        
        List<Review> reviews = reviewMapper.selectList(wrapper);
        List<ReviewResponse> responseList = new ArrayList<>();
        
        for (Review review : reviews) {
            ReviewResponse response = convertToResponse(review);
            responseList.add(response);
        }
        
        return responseList;
    }

    /**
     * 根据ID获取评价详情
     */
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        return convertToResponse(review);
    }

    /**
     * 更新回访信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowUp(Long reviewId, Long handlerId, FollowUpRequest request) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }

        // 验证回访状态
        if (!"CONTACTED".equals(request.getFollowUpStatus()) && 
            !"RESOLVED".equals(request.getFollowUpStatus())) {
            throw new RuntimeException("回访状态只能是CONTACTED或RESOLVED");
        }

        // 更新回访信息
        review.setFollowUpStatus(request.getFollowUpStatus());
        review.setFollowUpNotes(request.getFollowUpNotes());
        review.setHandlerId(handlerId);
        
        // 如果提供了回复内容，一并更新
        if (StringUtils.hasText(request.getReply())) {
            review.setReply(request.getReply());
        }
        
        reviewMapper.updateById(review);
        
        log.info("管理员{}更新评价{}的回访状态为{}", handlerId, reviewId, request.getFollowUpStatus());
    }

    /**
     * 回复评价
     */
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long reviewId, String reply) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }

        if (!StringUtils.hasText(reply)) {
            throw new RuntimeException("回复内容不能为空");
        }

        review.setReply(reply);
        reviewMapper.updateById(review);
        
        log.info("管理员回复评价{}", reviewId);
    }

    /**
     * 获取住客的评价记录
     */
    public List<ReviewResponse> getGuestReviews(Long guestId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getGuestId, guestId)
               .orderByDesc(Review::getReviewTime);
        
        List<Review> reviews = reviewMapper.selectList(wrapper);
        List<ReviewResponse> responseList = new ArrayList<>();
        
        for (Review review : reviews) {
            ReviewResponse response = convertToResponse(review);
            responseList.add(response);
        }
        
        return responseList;
    }

    /**
     * 检查入住记录是否已评价
     */
    public boolean hasReviewed(Long recordId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRecordId, recordId);
        return reviewMapper.selectCount(wrapper) > 0;
    }

    /**
     * 更新评价
     */
    public Review updateReview(Long guestId, Long reviewId, ReviewSubmitRequest request) {
        // 查询评价记录
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }

        // 验证是否为本人的评价
        if (!review.getGuestId().equals(guestId)) {
            throw new RuntimeException("无权修改此评价");
        }

        // 更新评价信息
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setReviewTime(LocalDateTime.now());

        // 重新计算是否为低分评价
        review.setIsLowScore(request.getScore() < 3);

        reviewMapper.updateById(review);

        log.info("住客{}更新评价，评价ID:{}, 新评分:{}", guestId, reviewId, request.getScore());

        return review;
    }

    /**
     * 删除评价
     */
    public void deleteReview(Long guestId, Long reviewId) {
        // 查询评价记录
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }

        // 验证是否为本人的评价
        if (!review.getGuestId().equals(guestId)) {
            throw new RuntimeException("无权删除此评价");
        }

        reviewMapper.deleteById(reviewId);

        log.info("住客{}删除评价，评价ID:{}", guestId, reviewId);
    }

    /**
     * 转换为响应DTO
     */
    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        BeanUtils.copyProperties(review, response);
        
        // 获取入住记录信息
        CheckInRecord checkInRecord = checkInRecordMapper.selectById(review.getRecordId());
        if (checkInRecord != null) {
            response.setCheckInTime(checkInRecord.getActualCheckin());
            response.setCheckOutTime(checkInRecord.getActualCheckout());
            
            // 获取房间信息
            Room room = roomMapper.selectById(checkInRecord.getRoomId());
            if (room != null) {
                response.setRoomNo(room.getRoomNo());
                response.setRoomType(room.getRoomType());
            }
        }
        
        // 获取住客信息
        Guest guest = guestMapper.selectById(review.getGuestId());
        if (guest != null) {
            response.setGuestName(guest.getRealName());
            User user = userMapper.selectById(guest.getUserId());
            if (user != null) {
                response.setGuestPhone(user.getPhone());
            }
        }
        
        // 获取处理人信息
        if (review.getHandlerId() != null) {
            User handler = userMapper.selectById(review.getHandlerId());
            if (handler != null) {
                response.setHandlerName(handler.getUsername());
            }
        }
        
        return response;
    }

    /**
     * 获取用户的入住历史记录
     */
    public Page<CheckInRecord> getMyCheckinHistory(Long guestId, Integer pageNum, Integer pageSize) {
        Page<CheckInRecord> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getGuestId, guestId)
               .orderByDesc(CheckInRecord::getActualCheckin);
        
        Page<CheckInRecord> resultPage = checkInRecordMapper.selectPage(page, wrapper);
        
        // 填充关联数据
        for (CheckInRecord record : resultPage.getRecords()) {
            // 填充房间信息
            Room room = roomMapper.selectById(record.getRoomId());
            if (room != null) {
                record.setRoomNo(room.getRoomNo());
                record.setRoomType(room.getRoomType());
            }
            
            // 检查是否已评价
            LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
            reviewWrapper.eq(Review::getRecordId, record.getRecordId())
                        .eq(Review::getGuestId, guestId);
            Review review = reviewMapper.selectOne(reviewWrapper);
            
            if (review != null) {
                record.setHasReviewed(true);
                record.setReview(review);
            } else {
                record.setHasReviewed(false);
            }
        }
        
        return resultPage;
    }

    /**
     * 创建评价
     */
    @Transactional(rollbackFor = Exception.class)
    public void createReview(CreateReviewRequest request, Long guestId) {
        // 1. 验证入住记录存在且属于当前用户
        CheckInRecord record = checkInRecordMapper.selectById(request.getRecordId());
        if (record == null) {
            throw new RuntimeException("入住记录不存在");
        }
        
        if (!record.getGuestId().equals(guestId)) {
            throw new RuntimeException("只能评价自己的入住记录");
        }
        
        // 2. 检查是否已退房
        if (record.getActualCheckout() == null) {
            throw new RuntimeException("只能评价已完成的入住记录");
        }
        
        // 3. 检查是否已经评价过
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRecordId, request.getRecordId())
               .eq(Review::getGuestId, guestId);
        Review existingReview = reviewMapper.selectOne(wrapper);
        
        if (existingReview != null) {
            throw new RuntimeException("已经评价过该入住记录");
        }
        
        // 4. 创建评价
        Review review = new Review();
        review.setRecordId(request.getRecordId());
        review.setGuestId(guestId);
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setReviewTime(LocalDateTime.now());
        review.setFollowUpStatus("NONE");
        
        reviewMapper.insert(review);
        
        log.info("用户 {} 对入住记录 {} 进行了评价，评分：{}", guestId, request.getRecordId(), request.getScore());
    }

    /**
     * 获取房间的评价列表
     */
    public Page<Review> getRoomReviews(Long roomId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        
        // 查询该房间的所有已完成入住记录
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getRoomId, roomId)
                    .isNotNull(CheckInRecord::getActualCheckout);
        List<CheckInRecord> records = checkInRecordMapper.selectList(recordWrapper);
        
        if (records.isEmpty()) {
            return page; // 没有入住记录，返回空
        }
        
        // 提取所有记录ID
        List<Long> recordIds = records.stream()
                .map(CheckInRecord::getRecordId)
                .collect(Collectors.toList());
        
        // 查询这些记录的评价
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.in(Review::getRecordId, recordIds)
                    .orderByDesc(Review::getReviewTime);
        
        Page<Review> resultPage = reviewMapper.selectPage(page, reviewWrapper);
        
        // 填充关联数据
        for (Review review : resultPage.getRecords()) {
            // 填充评价人信息
            Guest guest = guestMapper.selectById(review.getGuestId());
            if (guest != null) {
                User user = userMapper.selectById(guest.getUserId());
                if (user != null) {
                    // 脱敏处理用户名
                    String username = user.getUsername();
                    if (username.length() > 3) {
                        review.setGuestName(username.substring(0, 3) + "***");
                    } else {
                        review.setGuestName(username.charAt(0) + "**");
                    }
                }
            }
            
            // 填充入住时间信息
            CheckInRecord record = checkInRecordMapper.selectById(review.getRecordId());
            if (record != null) {
                review.setCheckinTime(record.getActualCheckin());
                review.setCheckoutTime(record.getActualCheckout());
                
                Room room = roomMapper.selectById(record.getRoomId());
                if (room != null) {
                    review.setRoomNo(room.getRoomNo());
                }
            }
        }
        
        return resultPage;
    }

    /**
     * 获取房间的平均评分
     */
    public Double getAverageScoreByRoom(Long roomId) {
        // 查询该房间的所有已完成入住记录
        LambdaQueryWrapper<CheckInRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(CheckInRecord::getRoomId, roomId)
                    .isNotNull(CheckInRecord::getActualCheckout);
        List<CheckInRecord> records = checkInRecordMapper.selectList(recordWrapper);
        
        if (records.isEmpty()) {
            return null;
        }
        
        List<Long> recordIds = records.stream()
                .map(CheckInRecord::getRecordId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.in(Review::getRecordId, recordIds);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);
        
        if (reviews.isEmpty()) {
            return null;
        }
        
        return reviews.stream()
                .mapToInt(Review::getScore)
                .average()
                .orElse(0.0);
    }
}
