package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.register.zk.heartbeat.ChannelStatus;
import com.gdut.rpcstudy.demo.register.zk.heartbeat.HeartbeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lulu
 * @Date 2019/11/18 22:17
 * 注册中心心跳检查服务器，通过查看心跳来查看各server是否存活
 */
public class KeepAliveMonitor {

    public static void main(String[] args) {
    final ConcurrentHashMap<String, ChannelStatus> channelUrlMap = new ConcurrentHashMap<>();

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    //存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //启用心跳保活
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //string编码器
                            ch.pipeline().addLast(new StringEncoder())
                            //string解码器
                            .addLast(new StringDecoder())
                                    //链接空闲时间
                            .addLast(new IdleStateHandler(0,0,30))
                           //hearbeat处理器
                            .addLast(new HeartbeatHandler(channelUrlMap));
                        }
                    });
            //bind初始化端口是异步的，但调用sync则会同步阻塞等待端口绑定成功
            ChannelFuture future = bootstrap.bind(ZKConsts.KEEPALIVEMONITOR_ADDRESS,ZKConsts.KEEPALIVEMONITOR_PORT).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
