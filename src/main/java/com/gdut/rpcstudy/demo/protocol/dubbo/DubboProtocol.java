package com.gdut.rpcstudy.demo.protocol.dubbo;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.URL;

/**
 * @author lulu
 * @Date 2019/11/15 23:13
 */
public class DubboProtocol implements Protocol {
    @Override
    public void start(URL url) {
        NettyServer nettyServer=new NettyServer();
        try {
            nettyServer.start(url.getHostname(),url.getPort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String send(URL url, Invocation invocation) {
        NettyClient nettyClient=new NettyClient();
        String res = nettyClient.send(url, invocation);
        return res;
    }
}
