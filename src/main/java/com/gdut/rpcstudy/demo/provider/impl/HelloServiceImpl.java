package com.gdut.rpcstudy.demo.provider.impl;

import com.gdut.rpcstudy.demo.consumer.HelloService;
import com.gdut.rpcstudy.demo.framework.server.RpcStudyService;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:41
 */
@RpcStudyService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String userName) {
        return "hello"+userName;
    }

    @Override
    public String qq() {
        return "qq";
    }
}
