package com.gdut.rpcstudy.demo.framework.protocol.netty;

import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcDecoder;
import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcEncoder;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;


/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServer {


    public void start(String hostName, int port, Map<String,Object> serviceMap) throws InterruptedException {
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
                            //解码器
                            ch.pipeline()
                                    //把request转为字节
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    //把本地执行的response对象转为字节
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new NettyServerHandler(serviceMap));

                        }
                    });
            //bind初始化端口是异步的，但调用sync则会同步阻塞等待端口绑定成功
            ChannelFuture future = bootstrap.bind(hostName, port).sync();
            //添加发送心跳
//            BeatDataSender.send(hostName + ":" + port, "127.0.0.1", 8888);

            System.out.println("绑定成功!" + "host:" + hostName + " port:" + port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
