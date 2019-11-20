package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.framework.client.RpcStudyClient;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.register.zk.ZkRegister;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

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
              Protocol protocol=ProtocolFactory.netty();
              //通过注册中心获取可用链接,这里使用zk
                RpcStudyClient annotation = (RpcStudyClient) interfaceClass.getAnnotation(RpcStudyClient.class);
                URL url= ZkRegister.random(annotation.name());
                String requestId=UUID.randomUUID().toString().replace("-","");
                //封装方法参数
                RpcRequest rpcRequest = new RpcRequest(requestId,interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                //发送请求
                RpcResponse res = protocol.send(url, rpcRequest);
                return res.getResult();
            }
       });

    }
}
