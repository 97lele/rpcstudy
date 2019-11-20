package com.gdut.rpcstudy.demo.protocol.netty;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.register.zk.ZkRegister;
import io.netty.channel.*;

import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        String hostAddress = InetAddress.getLocalHost().getHostName();
        System.out.println("接受请求");
        //这里的port按照本地的端口，可以用其他变量指示
        String serviceImplName= ZkRegister.get(rpcRequest.getInterfaceName(),new URL(hostAddress,8080));
        Class<?> serviceImpl = Class.forName(serviceImplName);
        Method method=serviceImpl.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
        Object result=method.invoke(serviceImpl.newInstance(), rpcRequest.getParams());
        System.out.println("结果-------"+result);
        RpcResponse response=new RpcResponse();
        response.setResult(result);
        response.setRequestId(rpcRequest.getRequestId());
        //由于操作异步，确保发送消息后才关闭连接
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("发送了结果"+response);
                ctx.channel().close();
            }
        });
//        ReferenceCountUtil.release(rpcRequest); 默认实现，如果只是普通的adapter则需要释放对象
    }
}
