package com.gdut.rpcstudy.demo.framework.protocol.netty;

import com.gdut.rpcstudy.demo.framework.serialize.handler.BaseCodec;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


/**
 * @author lulu
 * @Date 2019/11/15 22:40
 */
public class NettyClient {


    public RpcResponse send(URL url, RpcRequest rpcRequest) {
        //用来保存调用结果的handler
        NettyClientHandler res = new NettyClientHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    //true保证实时性，默认为false会累积到一定的数据量才发送
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    /*//把request实体变为字节
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    //把返回的response字节变为对象
                                    .addLast(new RpcDecoder(RpcResponse.class))*/
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 0))
                                    .addLast(new BaseCodec(RpcResponse.class))
                                    .addLast(res);

                        }
                    });
            //connect是异步的，但调用其future的sync则是同步等待连接成功
            ChannelFuture future = bootstrap.connect(url.getHostname(), url.getPort()).sync();
            System.out.println("链接成功!" + "host:" + url.getHostname() + " port:" + url.getPort());
            //同步等待调用信息发送成功
            future.channel().writeAndFlush(rpcRequest).sync();
            //同步等待NettyClientHandler的channelRead0被触发后（意味着收到了调用结果）关闭连接
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