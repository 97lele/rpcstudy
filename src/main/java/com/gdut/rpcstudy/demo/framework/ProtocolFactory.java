package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.protocol.dubbo.DubboProtocol;
import com.gdut.rpcstudy.demo.protocol.http.HttpProtocol;

/**
 * @author lulu
 * @Date 2019/11/15 23:35
 */
public class ProtocolFactory {
    public static HttpProtocol http() {
        return new HttpProtocol();
    }
    public static DubboProtocol dubbo(){
        return new DubboProtocol();
    }
}
