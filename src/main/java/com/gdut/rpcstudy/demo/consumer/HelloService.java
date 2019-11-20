package com.gdut.rpcstudy.demo.consumer;

import com.gdut.rpcstudy.demo.framework.client.RpcStudyClient;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:41
 */
@RpcStudyClient(name="user")
public interface HelloService {
    String sayHello(String userName);
    String qq();
}
