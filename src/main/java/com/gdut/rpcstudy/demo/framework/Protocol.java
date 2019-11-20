package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import org.aopalliance.intercept.Invocation;

/**
 * @author lulu
 * @Date 2019/11/15 23:12
 * 协议的接口
 */
public interface Protocol {

    //服务提供方启动的方法
    void start(URL url);

    //发送请求

    RpcResponse send(URL url, RpcRequest rpcRequest);


}
