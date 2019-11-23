package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.NettyAsynHandler;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:24
 * 获取链接机制,轮询、随机、权重
 */
public interface FetchPolicy {

    NettyAsynHandler random(String serviceName);
    NettyAsynHandler polling(String serviceName);
    NettyAsynHandler weight(String serviceName);




}
