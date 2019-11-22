package com.gdut.rpcstudy.demo.provider;

import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.ProtocolFactory;
import com.gdut.rpcstudy.demo.framework.URL;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:58
 */
@Deprecated
public class Provider {

    public static void main(String[] args) throws UnknownHostException {
        //这里多个接口的话，都要注册上去
        String hostAddress = InetAddress.getLocalHost().getHostName();
        URL url=new URL(hostAddress,8080);
//        ZkRegister.register(HelloService.class.getName(),url,HelloServiceImpl.class);
        Protocol server= ProtocolFactory.netty();
        server.start(url,null);

    }

}
