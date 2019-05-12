package com.lhl.springframework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})//类上使用
@Retention(RetentionPolicy.RUNTIME)//运行时
@Documented
public @interface Service {
    String value() default "";
}
