package com.gdut.rpcstudy.demo.framework.protocol.netty;

import com.gdut.rpcstudy.demo.framework.client.RpcStudyClient;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.channel.*;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

   private ExecutorService serverTask;

    //存放服务的map
    Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap, ExecutorService service) {
        this.serviceMap = serviceMap;
        this.serverTask=service;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "注册");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "注销");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "活跃");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "不活跃");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        serverTask.submit(()->{
            //这里的port按照本地的端口，可以用其他变量指示
            Object serviceImpl = serviceMap.get(rpcRequest.getInterfaceName());
      /*  Method method= serviceImpl.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
        Object result=method.invoke(serviceImpl, rpcRequest.getParams());*/
            FastClass serviceFastClass = FastClass.create(serviceImpl.getClass());
            int methodIndex = serviceFastClass.getIndex(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
            Object result = null;
            try {
                result = serviceFastClass.invoke(methodIndex, serviceImpl, rpcRequest.getParams());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            RpcResponse response = new RpcResponse();
            response.setResult(result);
            response.setRequestId(rpcRequest.getRequestId());
            //由于操作异步，确保发送消息后才关闭连接
            ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (rpcRequest.getMode() == RpcStudyClient.sync) {
                        ctx.channel().close();
                    }
//   当异步模式时，不关闭链接
                }
            });
        });

//        ReferenceCountUtil.release(rpcRequest); 默认实现，如果只是普通的adapter则需要释放对象
    }
}
