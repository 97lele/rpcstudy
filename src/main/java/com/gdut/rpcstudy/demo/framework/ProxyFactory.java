package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.protocol.http.HttpClient;
import com.gdut.rpcstudy.demo.provider.api.HelloService;

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
                HttpClient client = new HttpClient();
                Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                String res = client.post("127.0.0.1", 8080, invocation);
                return res;
            }
       });

    }
}
