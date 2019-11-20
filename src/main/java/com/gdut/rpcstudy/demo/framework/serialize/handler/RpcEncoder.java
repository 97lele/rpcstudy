package com.gdut.rpcstudy.demo.framework.serialize.handler;

import com.gdut.rpcstudy.demo.framework.serialize.serializer.JsonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:16
 * 把实体变为字节,可用于req->byte、response->byte
 */
public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> entity;

    public RpcEncoder(Class<?> entity) {
        this.entity = entity;
    }
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        System.out.println("转成字节");

        if (entity.equals(o.getClass())) {
            byte[] data=JsonSerializer.getInstance().serialize(o);
            //写入消息长度,这里还可以写入版本号、魔数等协议信息
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
