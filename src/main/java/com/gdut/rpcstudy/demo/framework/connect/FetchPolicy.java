package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:24
 * 获取链接机制,轮询、随机、权重
 */
public interface FetchPolicy {

    URL get(String serviceName, CuratorFramework client);


    class RandomPolicy implements FetchPolicy{

        @Override
        public URL get(String serviceName, CuratorFramework client) {
            //通过服务名获取具体的url
                try {
                    List<String> urlList = client.getChildren().forPath(ZkUtils.getPath(serviceName));
                    String[] url = urlList.get(0).split(":");
                    return new URL(url[0], Integer.valueOf(url[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

        }
    }
}
