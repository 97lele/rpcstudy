package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.framework.protocol.netty.NettyProtocol;
import com.gdut.rpcstudy.demo.framework.protocol.http.HttpProtocol;

/**
 * @author lulu
 * @Date 2019/11/15 23:35
 * 协议工厂模式
 */
public class ProtocolFactory {
    public static HttpProtocol http() {
        return new HttpProtocol();
    }
    public static NettyProtocol netty(){
        return new NettyProtocol();
    }
}
