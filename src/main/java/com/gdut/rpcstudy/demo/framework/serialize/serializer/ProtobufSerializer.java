package com.gdut.rpcstudy.demo.framework.serialize.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: lele
 * @date: 2019/11/20 下午5:23
 */
public class ProtobufSerializer  {
    private static Map<Class, Schema> schemaMap = new HashMap<Class, Schema>();

    // objenesis是一个小型Java类库用来实例化一个特定class的对象。
    private static Objenesis objenesis = new ObjenesisStd(true);

    // 存储模式对象映射
    private static Schema getSchema(Class cls) {
        Schema schema = schemaMap.get(cls);
        if (null == schema) {
            schema = RuntimeSchema.createFrom(cls);
            if (null != schema) {
                schemaMap.put(cls, schema);
            }
        }
        return schema;
    }

    public byte[] serialize(Object target) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        Class cls = target.getClass();

        try {
            Schema schema = getSchema(cls);
            byte[] bytes = ProtobufIOUtil.toByteArray(target, schema, buffer);
            return bytes;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            buffer.clear();
        }
    }

    public <T> T deserialize(byte[] target, Class<T> clazz) throws Exception {
        try {
            T instance = objenesis.newInstance(clazz);
            Schema schema = getSchema(clazz);
            ProtobufIOUtil.mergeFrom(target, instance, schema);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    //单例
    private static class Holder {
        private static final ProtobufSerializer j = new ProtobufSerializer();
    }

    public static ProtobufSerializer getInstance() {
        return ProtobufSerializer.Holder.j;
    }
}
