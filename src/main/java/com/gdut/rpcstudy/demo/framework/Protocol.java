package com.gdut.rpcstudy.demo.framework;

/**
 * @author lulu
 * @Date 2019/11/15 23:12
 * 协议的接口
 */
public interface Protocol {

    //服务提供方启动的方法
    void start(URL url);

    //发送请求
    String send(URL url, Invocation invocation);
}
