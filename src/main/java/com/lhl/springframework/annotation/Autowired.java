package com.lhl.springframework.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})//字段上使用
@Retention(RetentionPolicy.RUNTIME)//运行时
@Documented
public @interface Autowired {
    String value() default "";
}
