package com.gdut.rpcstudy.demo.framework.serialize.serializer;

import com.gdut.rpcstudy.demo.framework.serialize.RpcSerializer;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:16
 */
public class JsonSerializer implements RpcSerializer {
    private static final Gson gson = new Gson();

    @Override
    public byte[] serialize(Object target) {

        return gson.toJson(target).getBytes();
    }

    //字节转对象
    @Override
    public <T> T deserialize(byte[] target, Class<T> clazz)  {
        String s = new String(target);
        //如果是可迭代的，额外处理
        if (clazz.isInstance(Iterable.class)) {
            return gson.fromJson(s, new TypeToken<T>() {
            }.getType());
        }
        return gson.fromJson(s, clazz);
    }

    private static class Holder {
        private static final JsonSerializer j = new JsonSerializer();
    }

    public static JsonSerializer getInstance() {
        return Holder.j;
    }
}
