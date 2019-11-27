package com.gdut.rpcstudy.demo.framework.client.requestlimit;

import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * @author: lele
 * @date: 2019/11/27 下午3:49
 */
public interface RateLimitPolicy {

    Object filter(ProceedingJoinPoint point, RequestLimit limit, String key);

    int COUNT_RATE_LIMITER = 1;

    int TOKEN_BUCKET_LIMITER = 2;

    ConcurrentHashMap<Integer, RateLimitPolicy> ratePolicyMap = new ConcurrentHashMap<>();

    static ConcurrentHashMap<Integer, RateLimitPolicy> getRatePolicyMap() {
        ratePolicyMap.putIfAbsent(COUNT_RATE_LIMITER, new CountRateLimiter());
        ratePolicyMap.putIfAbsent(TOKEN_BUCKET_LIMITER, new TokenBucketLimiter());
        return ratePolicyMap;
    }

    class TokenBucketLimiter implements RateLimitPolicy {

        private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<String, RateLimiter>();

        @Override
        public Object filter(ProceedingJoinPoint point, RequestLimit limit, String key) {


            RateLimiter rateLimiter = rateLimiters.get(key);
            if (null == rateLimiter) {
                // 获取限定的流量
                // 为了防止并发
                rateLimiters.putIfAbsent(key, RateLimiter.create(limit.value()));
                rateLimiter = rateLimiters.get(key);
            }
            // 消耗一个令牌
            if (rateLimiter.tryAcquire(limit.timeOut(), limit.timeUnit())) {
                try {
                    return point.proceed();
                } catch (Throwable throwable) {

                }
            }
            return "too many connection";
        }
    }


    class CountRateLimiter implements RateLimitPolicy {
        private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<String, Semaphore>();

        @Override
        public Object filter(ProceedingJoinPoint point, RequestLimit requestLimit, String key) {
            // 获取限定的流量
            Semaphore semaphore = semaphores.get(key);

            if (null == semaphore) {
                //为了预防并发
                semaphores.putIfAbsent(key, new Semaphore(requestLimit.value()));
                semaphore = semaphores.get(key);
            }

            try {
                if (semaphore.tryAcquire(requestLimit.timeOut(), requestLimit.timeUnit())) {
                    return point.proceed();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                // 释放令牌
                if (null != semaphore) {
                    semaphore.release();
                }
            }
            return "too many connection!";
        }
    }


}
