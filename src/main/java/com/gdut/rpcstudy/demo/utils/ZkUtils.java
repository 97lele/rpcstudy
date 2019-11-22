package com.gdut.rpcstudy.demo.utils;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.stereotype.Component;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:37
 */
public class ZkUtils {

    private static CuratorFramework client = null;

    static {
        init();
    }

    //初始化链接客户端
    private static CuratorFramework init() {
        RetryPolicy retryPolicy = new RetryNTimes(ZKConsts.RETRYTIME, ZKConsts.SLEEP_MS_BEWTEENR_RETRY);
        client = CuratorFrameworkFactory.builder()
                .connectString(ZKConsts.ZK_SERVER_PATH)
                .sessionTimeoutMs(ZKConsts.SESSION_TIMEOUT_MS).retryPolicy(retryPolicy)
                .namespace(ZKConsts.WORK_SPACE).build();
        client.start();
        return client;
    }

    public static CuratorFramework getClient() {
        return client;
    }

    public static void closeZKClient(CuratorFramework client) {
        if (client != null) {
            client.close();
        }
    }

    //生成节点方法
    public static String getPath(String... args) {

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append("/").append(arg);
        }
        return builder.toString();
    }
}
