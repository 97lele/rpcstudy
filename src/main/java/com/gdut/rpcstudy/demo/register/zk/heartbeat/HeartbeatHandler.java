package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import com.gdut.rpcstudy.demo.register.zk.ZkRegister;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author lulu
 * @Date 2019/11/18 22:29
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    //维护channelId和具体地址的map，当发生变化时对其进行删除
    private static ConcurrentHashMap<String, String> channelUrlMap;


    //活跃次数
    private int inActiveCount = 0;
    //开始计数时间
    private long start;


    public HeartbeatHandler(ConcurrentHashMap<String, String> map) {
       HeartbeatHandler.channelUrlMap = map;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String url = msg.toString();
        String id = ctx.channel().id().asShortText();
        System.out.println("收到channelId：" + id + "发来信息：" + url);
        if (channelUrlMap.get(id) == null) {
            channelUrlMap.put(id, url);
        }


    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            IdleStateEvent state = (IdleStateEvent) evt;
            if (state.state().equals(IdleState.READER_IDLE)) {
                System.out.println("读空闲");
            } else if (state.state().equals(IdleState.WRITER_IDLE)) {
                System.out.println("写空闲");
            }
            //在一定时间内读写空闲才会关闭链接
            else if (state.state().equals(IdleState.ALL_IDLE)) {
                if (++inActiveCount == 1) {
                    start = System.currentTimeMillis();
                }
                int minute = (int) ((System.currentTimeMillis() - start) / (60 * 1000))+1;
                System.out.printf("第%d次读写都空闲,计时分钟数%d%n", inActiveCount,minute);
                //5分钟内出现2次以上不活跃现象，有的话就把它去掉
                if (inActiveCount > 2 && minute <= 5) {
                    System.out.println("移除不活跃的ip");
                    removeAndClose(ctx);
                } else {
                    //重新计算
                    if (minute >= 5) {
                        System.out.println("新周期开始");
                        start = 0;
                        inActiveCount = 0;
                    }
                }

            }

        }
    }

    //通过ID获取地址，并删除zk上相关的
    private void removeAndClose(ChannelHandlerContext ctx) {
        String id = ctx.channel().id().asShortText();
        String url = channelUrlMap.get(id);
        //移除不活跃的节点
        ZkRegister.remove(url);
        channelUrlMap.remove(id);
        ctx.channel().close();
    }

    //当出现异常时关闭链接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        removeAndClose(ctx);
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "注册");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().id().asShortText() + "注销");
    }
}
