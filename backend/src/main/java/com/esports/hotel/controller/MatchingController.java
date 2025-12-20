package com.esports.hotel.controller;

import com.esports.hotel.annotation.RoomAuthRequired;
import com.esports.hotel.common.Result;
import com.esports.hotel.dto.GamingProfileResponse;
import com.esports.hotel.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 匹配推荐控制器
 */
@Tag(name = "11. 匹配推荐", description = "基于段位和位置的智能推荐算法")
@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    
    private final MatchingService matchingService;
    
    @Operation(summary = "推荐合适的队友", description = "根据段位、位置等因素智能推荐队友")
    @GetMapping("/recommend")
    @RoomAuthRequired
    public Result<List<GamingProfileResponse>> recommendTeammates(
            HttpServletRequest request,
            @RequestParam String gameType,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long guestId = (Long) request.getAttribute("guestId");
        return Result.success(matchingService.recommendTeammates(guestId, gameType, limit));
    }
}
