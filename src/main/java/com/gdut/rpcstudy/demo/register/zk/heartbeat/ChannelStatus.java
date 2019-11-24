package com.gdut.rpcstudy.demo.register.zk.heartbeat;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于表示serverchannel的状态类
 */
@Data
public class ChannelStatus {

    //重新活跃的时间
    private volatile long reActive;
    //是否活跃
    private volatile boolean active = true;
    //持续重新活跃的次数
    private AtomicInteger reActiveCount = new AtomicInteger(0);
    //持续不活跃的次数
    private AtomicInteger inActiveCount = new AtomicInteger(0);
    //对应的channelId
    private String channelId;

    //开始计数时间
    private volatile long InActive;

    public ChannelStatus() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelStatus that = (ChannelStatus) o;
        return reActive == that.reActive &&
                active == that.active &&
                InActive == that.InActive &&
                Objects.equals(reActiveCount, that.reActiveCount) &&
                Objects.equals(inActiveCount, that.inActiveCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reActive, active, reActiveCount, inActiveCount, InActive);
    }

}