package com.gdut.rpcstudy.demo.framework.serialize.handler;

import com.gdut.rpcstudy.demo.framework.serialize.serializer.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:16
 * 把字节转为实体类,如byte->client供后续处理
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> target;

    public RpcDecoder(Class<?> target) {
        this.target = target;
    }

    /**
     * byte转实体
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        System.out.println("收到字节");
        //如果小于一个int的长度，不作处理
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        //获取数据长度
        int dataLength = byteBuf.readInt();
        //写入byte数组
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object res = JsonSerializer.getInstance().deserialize(data, target);
        list.add(res);

    }
}
