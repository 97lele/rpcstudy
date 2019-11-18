package com.gdut.rpcstudy.demo.framework;

/**
 * @author lulu
 * @Date 2019/11/15 23:12
 * 协议的接口
 */
public interface Protocol {

   void start(URL url);

   String send(URL url,Invocation invocation);
}
