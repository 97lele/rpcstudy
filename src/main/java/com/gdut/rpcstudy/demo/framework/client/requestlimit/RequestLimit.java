package com.gdut.rpcstudy.demo.framework.client.requestlimit;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author: lele
 * @date: 2019/11/27 下午3:48
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestLimit {
    int mode() default RateLimitPolicy.TOKEN_BUCKET_LIMITER;
    int value() default 200;
    int timeOut() default 100;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
