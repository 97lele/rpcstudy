package com.gdut.rpcstudy.demo.framework.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: lele
 * @date: 2019/11/20 下午2:54
 * 标记服务类，该注解的接口实现类注册到
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcStudyService {
    // 用来指定实现的接口
    Class<?> value();
}
