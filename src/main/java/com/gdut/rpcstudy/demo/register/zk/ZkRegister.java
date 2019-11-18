package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.framework.URL;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import java.util.List;


/**
 * @author lulu
 * @Date 2019/11/18 21:17
 *
 */
public class ZkRegister {
    //{接口：{URL:实现类名}},这里可以为每个接口建立子节点，节点名为url地址，值为className
    private static CuratorFramework client = null;

    private ZkRegister(){
        init();
    }
    public static void init() {
        RetryPolicy retryPolicy = new RetryNTimes(ZKConsts.RETRYTIME, ZKConsts.SLEEP_MS_BEWTEENR_RETRY);
        client = CuratorFrameworkFactory.builder()
                .connectString(ZKConsts.ZK_SERVER_PATH)
                .sessionTimeoutMs(ZKConsts.SESSION_TIMEOUT_MS).retryPolicy(retryPolicy)
                .namespace(ZKConsts.WORK_SPACE).build();
        client.start();
    }

    public static void register(String interfaceName, URL url, Class implClass) {
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath("/" + interfaceName + "/" + url.toString(), implClass.getCanonicalName().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //hostname:port
    public static void remove(String url){
        try {
            List<String> interfaces = client.getChildren().forPath("/");
            for (String anInterface : interfaces) {
                List<String> urlList = client.getChildren().forPath("/" + anInterface );
                for (String s : urlList) {
                    if(s.equals(url)){
                        client.delete().forPath("/"+anInterface+"/"+url);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String interfaceName, URL url) {
        String res = null;
        try {
            byte[] bytes = client.getData().forPath("/" + interfaceName + "/" + url.toString());
            res = new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;

    }

    public static URL random(String interfaceName) {
        try {
            List<String> urlList=client.getChildren().forPath("/"+interfaceName);
            String urlStr= String.valueOf(client.getData().forPath("/"+interfaceName+"/"+urlList.get(0)));
            String[] url=urlStr.split(":");
            return new URL(url[0],Integer.valueOf(url[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void closeZKClient() {
        if (client != null) {
            client.close();
        }
    }


}
