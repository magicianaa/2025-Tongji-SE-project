package com.esports.hotel.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，禁止访问"),
    NOT_FOUND(404, "请求资源不存在"),
    CONFLICT(409, "资源冲突"),
    
    // 业务错误 4xx
    USER_NOT_FOUND(4001, "用户不存在"),
    PASSWORD_ERROR(4002, "密码错误"),
    USER_DISABLED(4003, "账号已被禁用"),
    PHONE_ALREADY_EXISTS(4004, "手机号已被注册"),
    USERNAME_ALREADY_EXISTS(4005, "用户名已存在"),
    
    // 二次鉴权相关
    NO_CHECKIN_RECORD(4101, "未找到有效的入住记录"),
    ROOM_AUTH_EXPIRED(4102, "客房权限已过期，请联系前台"),
    ROOM_AUTH_REQUIRED(4103, "该操作需要客房权限"),
    
    // PMS 业务错误
    ROOM_NOT_AVAILABLE(4201, "房间不可用"),
    BOOKING_NOT_FOUND(4202, "预订记录不存在"),
    BOOKING_ALREADY_CANCELLED(4203, "预订已取消"),
    CHECKIN_ALREADY_EXISTS(4204, "该房间已有人入住"),
    CHECKOUT_FAILED(4205, "退房失败"),
    
    // POS 业务错误
    PRODUCT_NOT_FOUND(4301, "商品不存在"),
    INSUFFICIENT_STOCK(4302, "库存不足"),
    ORDER_NOT_FOUND(4303, "订单不存在"),
    
    // 积分相关
    INSUFFICIENT_POINTS(4401, "积分余额不足"),
    TASK_NOT_FOUND(4402, "任务不存在"),
    TASK_ALREADY_COMPLETED(4403, "任务已完成"),
    
    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    private final Integer code;
    private final String message;
}
