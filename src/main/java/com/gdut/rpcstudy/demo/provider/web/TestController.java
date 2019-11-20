package com.gdut.rpcstudy.demo.provider.web;

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
    public String get(){
        String qq = helloService.qq();
        System.out.println(qq);
        return qq;
    }
    @GetMapping("/t2")
    public String get2(){
        String hi = helloService.sayHello("hi");
        return hi;
    }
}
