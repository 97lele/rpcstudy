package com.gdut.rpcstudy.demo.framework;

import com.gdut.rpcstudy.demo.framework.client.RpcStudyClient;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.RpcFuture;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.register.zk.RegisterForClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:48
 * 代理工厂，对传入的类进行代理对具体执行的方法进行封装然后发送给服务端进行执行
 */
public class ProxyFactory {
    //用于执行future回调
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }


    public static <T> T getProxy(Class interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //指定所用协议
                Protocol protocol = ProtocolFactory.netty();
                //通过注册中心获取可用链接,这里使用zk
                RpcStudyClient annotation = (RpcStudyClient) interfaceClass.getAnnotation(RpcStudyClient.class);
                URL url = RegisterForClient.getInstance().random(annotation.name());
                String requestId = UUID.randomUUID().toString().replace("-", "");
                //封装方法参数
                RpcRequest rpcRequest = new RpcRequest(requestId, interfaceClass.getName(), method.getName(), args, method.getParameterTypes(), annotation.mode());
                //发送请求
                RpcResponse res = protocol.send(url, rpcRequest);
                if (res.getError() != null) {
                    throw new RuntimeException(res.getError());
                } else {
                    return res.getResult();
                }
            }
        });

    }

    public static <T> T getAsyncProxy(Class interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //指定所用协议
                Protocol protocol = ProtocolFactory.netty();
                RpcStudyClient annotation = (RpcStudyClient) interfaceClass.getAnnotation(RpcStudyClient.class);
                String requestId = UUID.randomUUID().toString().replace("-", "");
                //封装方法参数
                RpcRequest rpcRequest = new RpcRequest(requestId, interfaceClass.getName(), method.getName(), args, method.getParameterTypes(), annotation.mode());
                //发送请求
                //这里的管理连接池通过服务名去访问zk，获取可用的url
                RpcFuture res = protocol.sendFuture(annotation.name(), rpcRequest);
                //先尝试一次
                if (res.isDone()) {
                    return returnResult(res);
                }
                //不行就自旋等待
                while (!res.isDone()) {

                }
                return returnResult(res);
            }
        });
    }

    /**
     * 具体的处理异步返回的方法
     * @param res
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Object returnResult(RpcFuture res) throws ExecutionException, InterruptedException {
        RpcResponse response = (RpcResponse) res.get();
        if (response.getError() != null) {
            throw new RuntimeException(response.getError());
        } else {
            return response.getResult();
        }
    }


}
