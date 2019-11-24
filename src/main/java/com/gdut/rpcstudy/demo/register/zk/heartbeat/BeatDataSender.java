package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;

import java.util.concurrent.*;


/**
 * @author lulu
 * @Date 2019/11/18 23:30
 * 服务端的发送心跳包类
 */
@Getter
public class BeatDataSender {
    //状态
    private String activeStatus;


    //负责定期发送心跳包的线程池
    private ScheduledExecutorService service;
    //失败后重连的线程池
    private ScheduledExecutorService retryConnect;
    private boolean reconnect = false;

    public BeatDataSender(String localAddress, String remoteIp, Integer remotePort, String serviceName) {
        service = Executors.newSingleThreadScheduledExecutor();
        retryConnect = Executors.newSingleThreadScheduledExecutor();
        this.send(localAddress, remoteIp, remotePort, serviceName);
        //如果重连了尝试重新发送心跳包
        retryConnect.scheduleAtFixedRate(() -> {
            if (activeStatus == ZKConsts.INACTIVE) {
                System.out.println("server尝试重连监控器");
                send(localAddress, remoteIp, remotePort, serviceName);
                activeStatus = ZKConsts.REACTIVE;
                reconnect = true;
            }
        }, 3, 3, TimeUnit.MINUTES);
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
                                            activeStatus = ZKConsts.INACTIVE;
                                            System.out.println("由于不活跃次数在2分钟内超过3次,链接被关闭");
                                            ctx.channel().close();
                                        }
                                    });

                        }
                    })
                    .connect(remoteIp, remotePort).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                System.out.println("心跳客户端绑定" + "hostname:" + remoteIp + "remotePort:" + remotePort);
                                future.channel().writeAndFlush(serviceName + "@" + localAddress);
                                //这里只是演示心跳机制不活跃的情况下重连，普通的做法只需要定时发送本机地址即可
                                //进入重连状态后，就稳定发送心跳包
                                service.scheduleAtFixedRate(() -> {
                                    if (future.channel().isActive()) {
                                        if (reconnect) {
                                            future.channel().writeAndFlush(serviceName + "@" + localAddress);
                                        }
                                    }
                                }, 30, 30, TimeUnit.SECONDS);
                            } else {
                                System.out.println("3s后重连");
                                TimeUnit.SECONDS.sleep(3);
                                //重新发送
                                send(localAddress, remoteIp, remotePort, serviceName);
                            }

                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
