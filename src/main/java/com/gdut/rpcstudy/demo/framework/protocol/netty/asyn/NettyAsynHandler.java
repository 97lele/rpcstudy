package com.gdut.rpcstudy.demo.framework.protocol.netty.asyn;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.RpcFuture;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author: lele
 * @date: 2019/11/21 下午4:07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyAsynHandler extends SimpleChannelInboundHandler<RpcResponse> {
    //key:requestId,value自定义future
    private ConcurrentHashMap<String, RpcFuture> resultMap = new ConcurrentHashMap<>();

    private volatile Channel channel;

    private URL url;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel=ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse s) throws Exception {
        System.out.println("收到结果：" + s);
        String requestId=s.getRequestId();
        RpcFuture future = resultMap.get(requestId);
        if(s!=null){
            future.done(s);
            resultMap.remove(requestId);
        }
        System.out.println(channelHandlerContext.channel().remoteAddress());

    }


    public RpcFuture sendRequest(RpcRequest rpcRequest) {
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture future = new RpcFuture(rpcRequest);
        resultMap.put(rpcRequest.getRequestId(), future);
        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("发送了消息"+rpcRequest.toString());
                latch.countDown();
            }
        });
        return future;
    }
}
