package com.gdut.rpcstudy.demo.protocol.netty;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.framework.server.RpcStudyRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.rmi.registry.Registry;

/**
 * @author lulu
 * @Date 2019/11/15 23:13
 * 具体协议实现类
 */

public class NettyProtocol implements Protocol {

    @Override
    public void start(URL url) {
        NettyServer nettyServer=new NettyServer();
        try {
            nettyServer.start(url.getHostname(),url.getPort(),RpcStudyRegister.serviceMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RpcResponse send(URL url, RpcRequest rpcRequest) {
        NettyClient nettyClient=new NettyClient();
        RpcResponse res = nettyClient.send(url, rpcRequest);
        return res;
    }
}
