package com.gdut.rpcstudy.demo.consumer.web;

import com.gdut.rpcstudy.demo.consumer.HelloService;
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
    public String get2() {
            return helloService.sayHello("hi");
    }

    @GetMapping("/t3")
    public String get3() {
            return helloService.sayHello2("hi");
    }
}
