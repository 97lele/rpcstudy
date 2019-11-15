package com.gdut.rpcstudy.demo.protocol.dubbo;

import com.gdut.rpcstudy.demo.framework.Invocation;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
@Data
public class NettyClientHandler extends ChannelHandlerAdapter {
   private String result;



    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        setResult(msg.toString());
        ctx.close();
    }
}
