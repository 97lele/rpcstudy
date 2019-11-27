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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: lele
 * @date: 2019/11/21 下午4:07
 * 异步模式下的处理
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyAsynHandler extends SimpleChannelInboundHandler<RpcResponse> implements Comparable {
    //key:requestId,value自定义future
    private ConcurrentHashMap<String, RpcFuture> resultMap = new ConcurrentHashMap<>();

    private volatile Channel channel;

    //对应的远端URL
    private final URL url;

    private long inActiveTime;
    //请求数
    private AtomicInteger requestCount=new AtomicInteger(0);

    private Integer weight = 5;

    public NettyAsynHandler(URL url) {
        this.url = url;
    }

    public NettyAsynHandler(URL url, Integer weight) {
        this.url = url;
        if (weight != null) {
            this.weight = weight;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    public void close() {
        this.channel.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse s) throws Exception {

        String requestId = s.getRequestId();
        //设置完成并移除future
        RpcFuture future = resultMap.get(requestId);
        if (s != null) {
            future.done(s);
            resultMap.remove(requestId);
        }
    }


    public RpcFuture sendRequest(RpcRequest rpcRequest) {
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFuture future = new RpcFuture(rpcRequest);
        //放到请求列表里面
        resultMap.put(rpcRequest.getRequestId(), future);
        //发送请求
        this.requestCount.getAndIncrement();
        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                latch.countDown();
            }
        });
        try {
            //等待结果
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return future;
    }

    //检查任务时使用
    @Override
    public int compareTo(Object o) {
        if (o instanceof NettyAsynHandler) {
            long current = System.currentTimeMillis();
            NettyAsynHandler other = (NettyAsynHandler) o;
            //其他的超时时间
            long otherInActiveTime = current - other.inActiveTime;
            //自己的超时时间
            long thisInActiveTime = current - this.inActiveTime;
            //超时时间越大的排序反而越小，优先队列为小顶堆
            return thisInActiveTime > otherInActiveTime ? -1 : thisInActiveTime == otherInActiveTime ? 0 : 1;
        } else {
            throw new UnsupportedOperationException("类型异常!");
        }

    }
}
