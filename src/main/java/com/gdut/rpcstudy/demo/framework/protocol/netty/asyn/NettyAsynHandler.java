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
 * 异步模式下的处理
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyAsynHandler extends SimpleChannelInboundHandler<RpcResponse> {
    //key:requestId,value自定义future
    private ConcurrentHashMap<String, RpcFuture> resultMap = new ConcurrentHashMap<>();

    private volatile Channel channel;

    //对应的远端URL
    private final URL url;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public void close() {
        this.channel.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse s) throws Exception {
        System.out.println("收到结果：" + s);
        String requestId = s.getRequestId();
        //设置完成并移除future
        RpcFuture future = resultMap.get(requestId);
        if (s != null) {
            future.done(s);
            resultMap.remove(requestId);
        }
    }


    public RpcFuture sendRequest(RpcRequest rpcRequest) {
        //防止并发调用sendRequest，一次只允许一个请求线程进入
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture future = new RpcFuture(rpcRequest);
        //放到请求列表里面
        resultMap.put(rpcRequest.getRequestId(), future);
        //发送请求
        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("发送了消息" + rpcRequest.toString());
                latch.countDown();
            }
        });
        return future;
    }
}
