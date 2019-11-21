package com.gdut.rpcstudy.demo.framework.protocol.netty;

import com.gdut.rpcstudy.demo.framework.connect.ConnectManager;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.IAsynCallBack;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.NettyAsynHandler;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.RpcFuture;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.framework.server.RpcStudyRegister;

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

    @Override
    public RpcFuture sendFuture(String serviceName, RpcRequest request) {
        NettyAsynHandler handler=ConnectManager.getInstance().getConnectionWithPolling(serviceName);
        RpcFuture future = handler.sendRequest(request);
        return future;
    }


}
