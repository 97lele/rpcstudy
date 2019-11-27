package com.gdut.rpcstudy.demo.framework.serialize.handler;

import com.gdut.rpcstudy.demo.framework.serialize.serializer.ProtobufSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * @author: lele
 * @date: 2019/11/22 下午2:39
 * 含有编码解码的功能
 */
public class BaseCodec extends ByteToMessageCodec {
   private ProtobufSerializer serializer=ProtobufSerializer.getInstance();

    private Class<?> in;

    public BaseCodec(Class in){
        this.in=in;
    }

    /**
     * 出站，编码
     * @param channelHandlerContext
     * @param o
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] data=serializer.serialize(o);
        //写入消息长度,这里还可以写入版本号、魔数等协议信息
        //消息长度
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }

    /**
     * 入站处理，解码
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List list) throws Exception {

        //获取数据长度
        int dataLength = byteBuf.readInt();
        //重置读取的index
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        //写入byte数组
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        //解码转成对象
        Object res = serializer.deserialize(data, in);
        //给后面的handler处理
        list.add(res);
    }
}
