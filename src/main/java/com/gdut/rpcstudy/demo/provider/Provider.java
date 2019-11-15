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

/**
 * @author: lele
 * @date: 2019/11/15 下午6:58
 */

public class Provider {

    public static void main(String[] args) {
        URL url=new URL("127.0.0.1",8080);
        MapRegister.register(HelloService.class.getName(),url,HelloServiceImpl.class);
        Protocol server= ProtocolFactory.dubbo();
        server.start(url);

    }

}
