package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;

/**
 * @author lulu
 * @Date 2019/11/23 22:48
 */
public class RegisterForServer {

    private CuratorFramework client = null;

    private static class Holder {
        private static final RegisterForServer j = new RegisterForServer();
    }

    public static RegisterForServer getInstance() {
        return RegisterForServer.Holder.j;
    }

    //注册接口、对应服务ip及其实现类
    public void register(String serviceName, URL url) {
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    //临时节点
                    .withMode(CreateMode.EPHEMERAL)
                    //任何人都可以访问
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(ZkUtils.getPath(serviceName, url.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serverOn(String serviceName, URL url) {
        try {
            client.setData().forPath(ZkUtils.getPath(serviceName, url.toString()), ZKConsts.ACTIVE.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        ZkUtils.closeZKClient(client);
    }
}
