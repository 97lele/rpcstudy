package com.gdut.rpcstudy.demo.protocol.dubbo;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.register.MapRegister;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServerHandler extends ChannelHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Invocation invocation=(Invocation)msg;
//        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        Class serviceImpl= MapRegister.get(invocation.getInterfaceName(),new URL("127.0.0.1",8080));
        Method method=serviceImpl.getMethod(invocation.getMethodName(),invocation.getParamsTypes());
        Object result=method.invoke(serviceImpl.newInstance(),invocation.getParams());
        System.out.println("结果-------"+result);
        ctx.writeAndFlush(result);
    }
}
