package com.gdut.rpcstudy.demo.consumer;

import com.gdut.rpcstudy.demo.framework.ProxyFactory;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:17
 */
@Deprecated
public class Comsumer {
    public static void main(String[] args) {
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result = helloService.qq();
        System.out.println(result);

    }
}
