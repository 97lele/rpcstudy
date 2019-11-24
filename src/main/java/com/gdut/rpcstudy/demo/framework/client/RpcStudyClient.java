package com.gdut.rpcstudy.demo.framework.client;

import com.gdut.rpcstudy.demo.framework.connect.FetchPolicy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: lele
 * @date: 2019/11/20 下午2:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

//用于接口上，name为服务名，zk则在注册服务改为 服务名/ip,服务端通过传来的接口名通过反射获取类，或者通过给spring托管获取其class
public @interface RpcStudyClient {
    String name();
    //结果返回是异步还是同步模式
    int mode() default sync;
    int fetch() default FetchPolicy.polling;
    int sync=0;
    int asyn=1;

}
