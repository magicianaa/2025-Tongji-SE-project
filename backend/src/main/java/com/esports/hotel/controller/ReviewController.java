package com.esports.hotel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.FollowUpRequest;
import com.esports.hotel.dto.ReviewResponse;
import com.esports.hotel.dto.ReviewSubmitRequest;
import com.esports.hotel.entity.Review;
import com.esports.hotel.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价管理控制器
 */
@Tag(name = "评价管理", description = "评价提交、查询、回访、回复")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ==================== 住客端 ====================

    /**
     * 提交评价
     * 需要客房权限验证
     */
    @Operation(summary = "提交评价", description = "住客退房后提交评价")
    @PostMapping
    @RoomAuthRequired
    public Result<Review> submitReview(
            @Parameter(hidden = true) @RequestAttribute("guestId") Long guestId,
            @Valid @RequestBody ReviewSubmitRequest request) {
        Review review = reviewService.submitReview(guestId, request);
        return Result.success(review, "评价提交成功，感谢您的宝贵意见！");
    }

    /**
     * 获取我的评价记录
     */
    @Operation(summary = "获取我的评价", description = "查询当前住客的所有评价记录")
    @GetMapping("/my")
    @RoomAuthRequired
    public Result<List<ReviewResponse>> getMyReviews(
            @Parameter(hidden = true) @RequestAttribute("guestId") Long guestId) {
        List<ReviewResponse> reviews = reviewService.getGuestReviews(guestId);
        return Result.success(reviews);
    }

    /**
     * 检查入住记录是否已评价
     */
    @Operation(summary = "检查是否已评价", description = "验证某个入住记录是否已提交评价")
    @GetMapping("/check")
    public Result<Boolean> hasReviewed(
            @Parameter(description = "入住记录ID", required = true) @RequestParam Long recordId) {
        boolean hasReviewed = reviewService.hasReviewed(recordId);
        return Result.success(hasReviewed);
    }

    /**
     * 更新评价
     */
    @Operation(summary = "更新评价", description = "住客更新自己的评价")
    @PutMapping("/{reviewId}")
    @RoomAuthRequired
    public Result<Review> updateReview(
            @Parameter(hidden = true) @RequestAttribute("guestId") Long guestId,
            @Parameter(description = "评价ID", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody ReviewSubmitRequest request) {
        Review review = reviewService.updateReview(guestId, reviewId, request);
        return Result.success(review, "评价更新成功");
    }

    /**
     * 删除评价
     */
    @Operation(summary = "删除评价", description = "住客删除自己的评价")
    @DeleteMapping("/{reviewId}")
    @RoomAuthRequired
    public Result<Void> deleteReview(
            @Parameter(hidden = true) @RequestAttribute("guestId") Long guestId,
            @Parameter(description = "评价ID", required = true) @PathVariable Long reviewId) {
        reviewService.deleteReview(guestId, reviewId);
        return Result.success(null, "评价删除成功");
    }

    // ==================== 管理端 ====================

    /**
     * 获取评价列表（分页）
     * 支持筛选：低分预警、回访状态
     */
    @Operation(summary = "获取评价列表", description = "管理员查看所有评价，支持分页和筛选")
    @GetMapping
    public Result<Page<ReviewResponse>> getReviews(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "仅显示低分评价") @RequestParam(required = false) Boolean lowScoreOnly,
            @Parameter(description = "回访状态筛选") @RequestParam(required = false) String followUpStatus) {
        Page<ReviewResponse> reviewPage = reviewService.getReviews(page, size, lowScoreOnly, followUpStatus);
        return Result.success(reviewPage);
    }

    /**
     * 获取低分预警列表（专用）
     */
    @Operation(summary = "获取低分预警", description = "查询所有低分评价（评分<3星）")
    @GetMapping("/low-score")
    public Result<List<ReviewResponse>> getLowScoreReviews() {
        List<ReviewResponse> reviews = reviewService.getLowScoreReviews();
        return Result.success(reviews);
    }

    /**
     * 获取评价详情
     */
    @Operation(summary = "获取评价详情", description = "查询单条评价的完整信息")
    @GetMapping("/{reviewId}")
    public Result<ReviewResponse> getReviewById(
            @Parameter(description = "评价ID", required = true) @PathVariable Long reviewId) {
        ReviewResponse review = reviewService.getReviewById(reviewId);
        return Result.success(review);
    }

    /**
     * 更新回访信息
     */
    @Operation(summary = "回访登记", description = "管理员登记回访状态和备注")
    @PutMapping("/{reviewId}/follow-up")
    public Result<Void> updateFollowUp(
            @Parameter(description = "评价ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "处理人ID", required = true) @RequestParam Long handlerId,
            @Valid @RequestBody FollowUpRequest request) {
        reviewService.updateFollowUp(reviewId, handlerId, request);
        return Result.success(null, "回访信息更新成功");
    }

    /**
     * 回复评价
     */
    @Operation(summary = "回复评价", description = "管理员对评价进行公开回复")
    @PutMapping("/{reviewId}/reply")
    public Result<Void> replyReview(
            @Parameter(description = "评价ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "回复内容", required = true) @RequestBody String reply) {
        reviewService.replyReview(reviewId, reply);
        return Result.success(null, "回复成功");
    }
}
