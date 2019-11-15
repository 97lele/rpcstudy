package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.protocol.dubbo.DubboProtocol;
import com.gdut.rpcstudy.demo.protocol.http.HttpClient;
import com.gdut.rpcstudy.demo.provider.api.HelloService;
import com.gdut.rpcstudy.demo.register.MapRegister;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:48
 */
public class ProxyFactory {

    public static <T> T getProxy(Class interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              Protocol protocol=ProtocolFactory.dubbo();
                URL url= MapRegister.random(interfaceClass.getName());
                Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                String res = protocol.send(url,invocation);
                return res;
            }
       });

    }
}
