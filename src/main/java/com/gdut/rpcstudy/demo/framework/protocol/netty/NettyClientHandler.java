package com.gdut.rpcstudy.demo.framework.protocol.netty;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.channel.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author lulu
 * @Date 2019/11/15 22:41
 * 同步模式下的处理，只是用来保存结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
   private RpcResponse result;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse s) throws Exception {

        System.out.println("收到结果："+s);
        this.result = s;
        System.out.println(channelHandlerContext.channel().remoteAddress());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
    }
}
