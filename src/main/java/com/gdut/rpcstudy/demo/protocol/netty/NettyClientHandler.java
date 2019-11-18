package com.gdut.rpcstudy.demo.protocol.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 * 用来保存结果
 */
@Data
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
   private String result;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        this.result = s;
    }
}
