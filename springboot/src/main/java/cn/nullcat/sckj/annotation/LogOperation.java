package cn.nullcat.sckj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {
    String module() default "";      // 操作模块
    String operation() default "";   // 操作类型
    String description() default ""; // 操作描述
}