package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.BookingRequest;
import com.esports.hotel.dto.BookingResponse;
import com.esports.hotel.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预订控制器
 */
@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "预订管理", description = "房间预订相关接口")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "创建预订", description = "住客预订房间")
    @PostMapping
    public Result<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String token) {
        log.info("收到预订请求: roomId={}, plannedCheckin={}, mainGuestName={}", 
                request.getRoomId(), request.getPlannedCheckin(), request.getMainGuestName());
        log.info("Controller收到的token: [{}], 长度: {}, 前20字符: [{}]", 
                token, token.length(), token.length() > 20 ? token.substring(0, 20) : token);
        BookingResponse response = bookingService.createBooking(request, token);
        return Result.success(response, "预订成功");
    }

    @Operation(summary = "获取我的预订列表", description = "查询当前用户的所有预订")
    @GetMapping("/my")
    public Result<List<BookingResponse>> getMyBookings(
            @RequestHeader("Authorization") String token) {
        List<BookingResponse> bookings = bookingService.getMyBookings(token);
        return Result.success(bookings);
    }

    @Operation(summary = "取消预订", description = "取消指定的预订")
    @DeleteMapping("/{bookingId}")
    public Result<Void> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String token) {
        bookingService.cancelBooking(bookingId, token);
        return Result.success(null, "预订已取消");
    }

    @Operation(summary = "根据手机号查询今日预订", description = "前台员工根据预订者手机号查询今日预订信息")
    @GetMapping("/by-phone/{phone}")
    public Result<BookingResponse> getBookingByPhone(@PathVariable String phone) {
        BookingResponse booking = bookingService.getBookingByPhone(phone);
        return Result.success(booking);
    }
}
