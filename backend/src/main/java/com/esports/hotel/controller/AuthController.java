package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import com.esports.hotel.dto.LoginRequest;
import com.esports.hotel.dto.LoginResponse;
import com.esports.hotel.dto.RegisterRequest;
import com.esports.hotel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "1. 认证管理", description = "用户注册、登录、验证码")
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "发送短信验证码", description = "注册时需要先调用此接口获取验证码")
    @PostMapping("/sms/send")
    public Result<String> sendSmsCode(
            @RequestParam @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        String code = authService.sendSmsCode(phone);
        return Result.success(code, "验证码已发送（模拟项目，直接返回验证码）");
    }

    @Operation(summary = "用户注册", description = "住客注册账号")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success(null, "注册成功");
    }

    @Operation(summary = "用户登录", description = "账号密码登录，获得普通访问Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response, "登录成功");
    }

    @Operation(summary = "检查入住状态", description = "检查当前登录用户是否有有效入住记录")
    @GetMapping("/check-in-status")
    public Result<?> checkInStatus(@RequestHeader("Authorization") String authHeader) {
        return authService.checkInStatus(authHeader);
    }

    @Operation(summary = "测试接口", description = "测试服务是否正常运行")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
