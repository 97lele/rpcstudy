package com.gdut.rpcstudy.demo.framework.serialize;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:15
 * 对象-字节转换接口
 */
public interface RpcSerializer {
    /**
     * 序列化
     * @param target
     * @return
     */
    byte[] serialize(Object target);

    /**
     * 反序列化
     * @param target
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T deserialize(byte[] target,Class<T> clazz) throws Exception;
}
