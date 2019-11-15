package com.gdut.rpcstudy.demo.provider;

import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.ProtocolFactory;
import com.gdut.rpcstudy.demo.framework.ProxyFactory;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.protocol.dubbo.DubboProtocol;
import com.gdut.rpcstudy.demo.protocol.http.HttpServer;
import com.gdut.rpcstudy.demo.provider.api.HelloService;
import com.gdut.rpcstudy.demo.provider.impl.HelloServiceImpl;
import com.gdut.rpcstudy.demo.register.MapRegister;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:58
 */

public class Provider {

    public static void main(String[] args) throws UnknownHostException {
        URL url=new URL("localhost",8080);
        MapRegister.register(HelloService.class.getName(),url,HelloServiceImpl.class);
        Protocol server= ProtocolFactory.dubbo();
        server.start(url);

    }

}
