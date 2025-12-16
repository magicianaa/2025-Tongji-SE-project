package com.esports.hotel.annotation;

import java.lang.annotation.*;

/**
 * 客房权限注解
 * 标注需要二次鉴权的接口（必须有有效入住记录）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoomAuthRequired {
    /**
     * 权限描述
     */
    String value() default "客房服务权限";
}
