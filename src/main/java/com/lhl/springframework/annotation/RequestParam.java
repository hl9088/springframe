package com.lhl.springframework.annotation;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})//参数使用
@Retention(RetentionPolicy.RUNTIME)//运行时
@Documented
public @interface RequestParam {
    String value() default "";
}
