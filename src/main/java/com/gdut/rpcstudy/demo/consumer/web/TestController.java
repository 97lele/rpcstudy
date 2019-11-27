package com.gdut.rpcstudy.demo.consumer.web;

import com.gdut.rpcstudy.demo.consumer.HelloService;
import com.gdut.rpcstudy.demo.framework.client.requestlimit.RateLimitPolicy;
import com.gdut.rpcstudy.demo.framework.client.requestlimit.RequestLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lele
 * @date: 2019/11/20 下午3:34
 */
@RestController
public class TestController {

    @Autowired
    private HelloService helloService;

    @GetMapping("/t1")
    public String get() {
            return helloService.qq();
    }

    @GetMapping("/t2")
    @RequestLimit(mode = RateLimitPolicy.COUNT_RATE_LIMITER
    )
    public String get2() {
            return helloService.sayHello("hi");
    }

    @GetMapping("/t3")
    @RequestLimit(mode = RateLimitPolicy.TOKEN_BUCKET_LIMITER)
    public String get3() {
            return helloService.sayHello2("hi");
    }
}
