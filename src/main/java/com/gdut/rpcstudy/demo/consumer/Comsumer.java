package com.gdut.rpcstudy.demo.consumer;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.ProxyFactory;
import com.gdut.rpcstudy.demo.protocol.http.HttpClient;
import com.gdut.rpcstudy.demo.provider.api.HelloService;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:17
 */
public class Comsumer {
    public static void main(String[] args) {
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result = helloService.qq();
        System.out.println(result);

    }
}
