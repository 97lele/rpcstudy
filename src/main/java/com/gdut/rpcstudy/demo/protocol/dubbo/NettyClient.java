package com.gdut.rpcstudy.demo.protocol.dubbo;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.URL;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


/**
 * @author lulu
 * @Date 2019/11/15 22:40
 */
public class NettyClient {


    public String send(URL url, Invocation invocation) {
        NettyClientHandler res = new NettyClientHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder());
                            //反序列化对象时指定类解析器，null表示使用默认的类加载器
                            ch.pipeline().addLast(new ObjectDecoder(1024 * 64, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(res);

                        }
                    });
            //connect是异步的，但调用其future的sync则是同步等待连接成功
            ChannelFuture future = bootstrap.connect(url.getHostname(), url.getPort()).sync();
            System.out.println("链接成功!" + "host:" + url.getHostname() + "port:" + url.getPort());
            //同步等待调用信息发送成功
            future.channel().writeAndFlush(invocation).sync();
            //同步等待NettyClientHandler的channelRead被触发后（意味着收到了调用结果）
            future.channel().closeFuture().sync();
            return res.getResult();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        return null;
    }
}