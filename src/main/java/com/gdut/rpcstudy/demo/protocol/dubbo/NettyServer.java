package com.gdut.rpcstudy.demo.protocol.dubbo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author lulu
 * @Date 2019/11/15 22:41
 */
public class NettyServer {
    public void start(String hostName,int port) throws InterruptedException {
        final ServerBootstrap bootstrap=new ServerBootstrap();
        NioEventLoopGroup bossGroup=new NioEventLoopGroup();
        NioEventLoopGroup workerGroup=new NioEventLoopGroup();
      try {
          bootstrap.group(bossGroup, workerGroup)
                  .channel(NioServerSocketChannel.class)
                  .option(ChannelOption.SO_BACKLOG, 128)
                  .childHandler(new ChannelInitializer<SocketChannel>() {
                      @Override
                      protected void initChannel(SocketChannel socketChannel) throws Exception {
                          ChannelPipeline pipeline = socketChannel.pipeline();
                          pipeline.addLast("decoder", new ObjectDecoder(
                                  ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())
                          ));
                          pipeline.addLast("encoder", new ObjectEncoder());
                          pipeline.addLast("handler", new NettyServerHandler());
                      }
                  });
          ChannelFuture future = bootstrap.bind(hostName, port).sync();
          future.channel().closeFuture().sync();
      }catch (Exception e){
          e.printStackTrace();
      }finally {
          bossGroup.shutdownGracefully();
          workerGroup.shutdownGracefully();
      }
    }


}
