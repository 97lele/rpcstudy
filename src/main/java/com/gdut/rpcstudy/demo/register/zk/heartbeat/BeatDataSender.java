package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import com.gdut.rpcstudy.demo.register.zk.ZkRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;

import java.util.Random;
import java.util.concurrent.*;


/**
 * @author lulu
 * @Date 2019/11/18 23:30
 */
@Getter
public class BeatDataSender {
    private int active = 1;


    private ScheduledExecutorService service;
    private ScheduledExecutorService retryConnect;
    private Channel channel;

    public BeatDataSender(String localAddress, String remoteIp, Integer remotePort, String serviceName) {
        service = Executors.newSingleThreadScheduledExecutor();
        retryConnect = Executors.newSingleThreadScheduledExecutor();
        this.send(localAddress, remoteIp, remotePort, serviceName);
        //如果重连了尝试重新发送心跳包
        retryConnect.scheduleAtFixedRate(() -> {
            if (active == ZkRegister.INACTIVE) {
                send(localAddress, remoteIp, remotePort, serviceName);
                active = 1;
            }
        }, 10, 10, TimeUnit.MINUTES);
    }

    public void close() {
        this.service.shutdown();
        this.retryConnect.shutdown();
    }


    public void send(String localAddress, String remoteIp, Integer remotePort, String serviceName) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture connect = bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringEncoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            active = ZkRegister.INACTIVE;
                                            System.out.println("由于不活跃次数在5分钟内超过2次,链接被关闭");
                                            ctx.channel().close();
                                            channel = null;
                                        }
                                    });

                        }
                    })
                    .connect(remoteIp, remotePort).sync();
            System.out.println("心跳客户端绑定" + "hostname:" + remoteIp + "remotePort:" + remotePort);
            this.channel = connect.channel();

            this.channel.writeAndFlush(serviceName + "@" + localAddress);
            //这里只是演示心跳机制不活跃的情况下重连，普通的做法只需要定时发送本机地址即可
            service.scheduleAtFixedRate(() -> {
                if (connect.channel().isActive()) {
                    int time = new Random().nextInt(5);
                    System.out.println(time);
                    if (time > 3) {
                        System.out.println("发送本机地址：" + localAddress);
                        connect.channel().writeAndFlush(serviceName + "@" + localAddress);
                    }
                }
            }, 60, 60, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
