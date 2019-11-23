package com.gdut.rpcstudy.demo.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lulu
 * @Date 2019/11/22 23:43
 */
public final class RpcThreadFactoryBuilder {


    private String namePrefix="default";

    private int priority=5;

    private boolean daemon=false;

    private String groupName="rpc";

    public RpcThreadFactoryBuilder setNamePrefix(String namePrefix){
        this.namePrefix=namePrefix;
        return this;
    }

    public RpcThreadFactoryBuilder setPriority(int priority){
        if(priority>10||priority<0){
            throw new UnsupportedOperationException("线程优先级设置不正确");
        }
        this.priority=priority;
        return this;
    }

    public RpcThreadFactoryBuilder setDaemon(boolean daemon){
        this.daemon=daemon;
        return this;
    }

    public RpcThreadFactoryBuilder setGroupName(String groupName){
        this.groupName=groupName;
        return this;
    }

    public ThreadFactory build(){
        return new BaseThreadFactory(this);
    }

    /**
     * 启动客户端链接的自定义线程工厂
     */
    static class BaseThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        BaseThreadFactory(RpcThreadFactoryBuilder builder) {
            group = new ThreadGroup(builder.groupName);
            group.setDaemon(builder.daemon);
            group.setMaxPriority(builder.priority);
            namePrefix = builder.namePrefix+"-"+
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            return t;
        }
    }

}
