package com.gdut.rpcstudy.demo.framework.client.requestlimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: lele
 * @date: 2019/11/27 下午4:34
 */

@Component
@Aspect
public class LimiterAspect {

    ConcurrentHashMap<Integer, RateLimitPolicy> map = RateLimitPolicy.getRatePolicyMap();

    @Pointcut("@annotation(com.gdut.rpcstudy.demo.framework.client.requestlimit.RequestLimit)")
    public void limitPoint() {
    }

    ;

    @Around("limitPoint()")
    public Object doAroundAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        RequestLimit annotation = signature.getMethod().getAnnotation(RequestLimit.class);
        int mode = annotation.mode();
        RateLimitPolicy rateLimitPolicy = map.get(mode);
        //获取接口名
        String targetName = proceedingJoinPoint.getTarget().getClass().getName();
        //获取执行的方法
        Method method = signature.getMethod();
        // 以 class + method + parameters为key，避免重载、重写带来的混乱
        String key = targetName + "." + method.getName() + Arrays.toString(method.getGenericParameterTypes());
        return rateLimitPolicy.filter(proceedingJoinPoint, annotation, key);
    }
}
