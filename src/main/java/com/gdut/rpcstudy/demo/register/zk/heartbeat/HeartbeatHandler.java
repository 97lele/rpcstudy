package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import com.gdut.rpcstudy.demo.register.zk.ZkRegister;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.util.HashMap;


/**
 * @author lulu
 * @Date 2019/11/18 22:29
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private HashMap<String,String> map;

    public HeartbeatHandler(HashMap<String,String> map){
        this.map=map;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String url=msg.toString();
        String id = ctx.channel().id().asShortText();
        System.out.println("收到channelId"+id+"发来信息"+url);
        if(map.get(id)==null){
            map.put(id,url);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent state = (IdleStateEvent) evt;
            if (state.state().equals(IdleStateEvent.ALL_IDLE_STATE_EVENT)) {
             removeAndClose(ctx);
            }
        }
    }
    private void removeAndClose(ChannelHandlerContext ctx){
        String id=ctx.channel().id().asShortText();
        String url = map.get(id);
       // ZkRegister.remove(url);
        map.remove(id);
        ctx.channel().closeFuture();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
       removeAndClose(ctx);
    }
}
