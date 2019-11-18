package com.gdut.rpcstudy.demo.protocol.netty;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.register.MapRegister;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Invocation> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation invocation) throws Exception {
        String hostAddress = InetAddress.getLocalHost().getHostName();
        //这里的port按照本地的端口，可以用其他变量指示
        Class serviceImpl= MapRegister.get(invocation.getInterfaceName(),new URL(hostAddress,8080));
        Method method=serviceImpl.getMethod(invocation.getMethodName(),invocation.getParamsTypes());
        Object result=method.invoke(serviceImpl.newInstance(),invocation.getParams());
        System.out.println("结果-------"+result);
        //由于操作异步，确保发送消息后才关闭连接
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.CLOSE);
//        ReferenceCountUtil.release(invocation); 默认实现，如果只是普通的adapter则需要释放对象
    }
}
