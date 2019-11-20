package com.gdut.rpcstudy.demo.protocol.netty;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.connection.RpcFuture;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 * 用来保存结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
   private RpcResponse result;

   //key:requestId,value自定义future
   private ConcurrentHashMap<String,RpcFuture> resultMap=new ConcurrentHashMap<>();

   private volatile Channel channel;

   private URL url;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse s) throws Exception {

        System.out.println("收到结果："+s);
        this.result = s;
        System.out.println(channelHandlerContext.channel().remoteAddress());

    }


}
