package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author lulu
 * @Date 2019/11/18 23:30
 */
public class BeatDataSender {
    private BeatDataSender() {

    }

    public static void send(String url, String hostName, Integer port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture connect = bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringEncoder())
                                    .addLast(new StringEncoder());
                        }
                    })
                    .connect(hostName, port).sync();
            System.out.println("心跳客户端绑定"+"hostname:"+hostName+"port:"+port);

            connect.channel().writeAndFlush(url);

                service.scheduleAtFixedRate(() -> {
                    if(connect.channel().isActive()){
                        System.out.println("发送本机地址"+url);
                        connect.channel().writeAndFlush(url);
                    }

                }, 62, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
