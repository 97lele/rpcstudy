package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.register.MapRegister;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:48
 * 代理工厂，对传入的类进行代理对具体执行的方法进行封装然后发送给服务端进行执行
 */
public class ProxyFactory {

    public static <T> T getProxy(Class interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //指定所用协议
              Protocol protocol=ProtocolFactory.http();
              //通过注册中心获取可用链接
                URL url= MapRegister.random(interfaceClass.getName());
                //封装方法参数
                Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                //发送请求
                String res = protocol.send(url,invocation);
                return res;
            }
       });

    }
}
