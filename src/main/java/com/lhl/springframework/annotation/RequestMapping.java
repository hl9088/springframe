package com.lhl.springframework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})//类上、方法使用
@Retention(RetentionPolicy.RUNTIME)//运行时
@Documented
public @interface RequestMapping {
    String value() default "";
}
