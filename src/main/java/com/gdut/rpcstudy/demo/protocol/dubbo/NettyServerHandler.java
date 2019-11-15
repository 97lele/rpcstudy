package com.gdut.rpcstudy.demo.protocol.dubbo;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.register.MapRegister;
import io.netty.channel.*;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Invocation> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation invocation) throws Exception {
        Class serviceImpl= MapRegister.get(invocation.getInterfaceName(),new URL("localhost",8080));
        Method method=serviceImpl.getMethod(invocation.getMethodName(),invocation.getParamsTypes());
        Object result=method.invoke(serviceImpl.newInstance(),invocation.getParams());
        System.out.println("结果-------"+result);
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);

    }
}
