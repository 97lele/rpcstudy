package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Random;
import java.util.concurrent.*;


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
                                    .addLast(new StringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("由于不活跃次数在5分钟内超过2次,链接被关闭");
                                        }
                                    });

                        }
                    })
                    .connect(hostName, port).sync();
            System.out.println("心跳客户端绑定" + "hostname:" + hostName + "port:" + port);
            //这里只是演示心跳机制不活跃的情况下重连，普通的做法只需要定时发送本机地址即可
            service.scheduleAtFixedRate(() -> {
                if (connect.channel().isActive()) {
                    int time = new Random().nextInt(5);
                    System.out.println(time);
                    if(time >3){
                        System.out.println("发送本机地址：" + url);
                        connect.channel().writeAndFlush(url);
                    }
                }
            }, 60, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
