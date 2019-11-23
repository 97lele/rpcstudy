package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.register.zk.RegisterForClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author lulu
 * @Date 2019/11/18 22:29
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private final static int MAX_IN_ACTIVE_COUNT = 3;
    private final static int COUNT_MINUTE = 2;
    private final static int MIN_RE_ACTIVE_COUNT = 3;

    //维护channelId和具体地址的map，当发生变化时对其进行删除
    private  ConcurrentHashMap<String, ChannelStatus> channelUrlMap ;


    public HeartbeatHandler(ConcurrentHashMap<String,ChannelStatus> map) {
        channelUrlMap=map;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String url = msg.toString();
        String id = ctx.channel().id().asShortText();
        System.out.println("收到channelId：" + id + "发来信息：" + url);
        ChannelStatus status;
        if ((status = channelUrlMap.get(url)) == null) {
            status = new ChannelStatus();
            status.setChannelId(id);
            channelUrlMap.put(url, status);
        } else {
            //如果收到不活跃的节点重连发来的信息,
            if (!status.isActive()) {
                System.out.println(url+"尝试重连");
                int i = status.getReActiveCount().incrementAndGet();
                if (i == 1) {
                    String s = ctx.channel().id().asShortText();
                 status.setChannelId(s);
                    status.setReActive(System.currentTimeMillis());
                } else if (i>=MIN_RE_ACTIVE_COUNT) {
                    long minute=(System.currentTimeMillis() - status.getReActive()) / (1000 * 60 )+1;
                    if (minute >= COUNT_MINUTE) {
                        status.setActive(true);
                        status.setInActiveCount(new AtomicInteger(0));
                        // 通知连接池重新加入该节点
                        updateOrRemove(url, ctx, true, ZKConsts.REACTIVE);
                    }
                }
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            IdleStateEvent state = (IdleStateEvent) evt;
            //在一定时间内读写空闲才会关闭链接
            if (state.state().equals(IdleState.ALL_IDLE)) {
                String s = ctx.channel().id().asShortText();
                Integer inActiveCount = 0;
                ChannelStatus channelStatus = null;
                String url = null;
                Object[] objects = getStatusValuesByChannelId(s);
                Assert.isTrue(objects != null && objects.length > 0, "该channelId没有东西");
                inActiveCount = (Integer) objects[0];
                channelStatus = (ChannelStatus) objects[1];
                url = (String) objects[2];
                if (inActiveCount == 1) {
                    channelStatus.setInActive(System.currentTimeMillis());
                }
                //1分钟内出现2次以上不活跃现象，有的话就把它去掉
                long minute = (System.currentTimeMillis() - channelStatus.getInActive()) / (1000 * 60 )+1;
                System.out.printf("第%s次不活跃,当前分钟%d%n",channelStatus.getInActiveCount().get(),minute);
                if (inActiveCount >= MAX_IN_ACTIVE_COUNT&&minute <= COUNT_MINUTE) {
                        System.out.println("移除不活跃的ip" + channelStatus.toString());
                        channelStatus.setActive(false);
                        updateOrRemove(url, ctx, true, ZKConsts.INACTIVE);
                } else {
                    //重新计算,是活跃的状态
                    if (minute > COUNT_MINUTE) {
//                        System.out.println("新周期开始");
                        channelStatus.setActive(true);
                        channelStatus.setInActive(0);
                        channelStatus.setInActiveCount(new AtomicInteger(0));
                    }
                }

            }

        }
    }

    public Object[] getStatusValuesByChannelId(String channelId) {
        Iterator<Map.Entry<String, ChannelStatus>> iterator = channelUrlMap.entrySet().iterator();
        Integer inActiveCount = 0;
        ChannelStatus channelStatus = null;
        String url = null;
        System.out.println();
        while (iterator.hasNext()) {
            Map.Entry<String, ChannelStatus> next = iterator.next();
            ChannelStatus status = next.getValue();
            if (status.getChannelId().equals(channelId)) {
                channelStatus = status;
                url = next.getKey();
                inActiveCount = channelStatus.getInActiveCount().incrementAndGet();
                return new Object[]{inActiveCount, channelStatus, url};
            }
        }
        return null;
    }

    /**
     * 通过ID获取地址，并删除zk上相关的，用于心跳监听的类
     *
     * @param ctx
     */
    private void updateOrRemove(String url, ChannelHandlerContext ctx, Boolean update, String data) {
        //移除不活跃的节点
        RegisterForClient.getInstance().removeOrUpdate(url, update, data);
        //如果不为重新唤醒，则断开连接并且做相应的通知
        if (!data.equals(ZKConsts.REACTIVE)) {
            channelUrlMap.get(url).setChannelId(null);
            ctx.channel().close();
        }

    }


    //当出现异常时关闭链接
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Object[] values = getStatusValuesByChannelId(ctx.channel().id().asShortText());
        updateOrRemove((String) values[2], ctx, false, null);
    }


}
