package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.connect.ConnectManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author lulu
 * @Date 2019/11/18 21:17
 * 负责实现注册中心具体的业务功能
 */
public class ZkRegister {



    //这次改动为/服务名/ip
    private static CuratorFramework client = null;


    //通过静态代码块初始化
    static{
        init();
    }



    //初始化链接客户端
    private static void init() {
        RetryPolicy retryPolicy = new RetryNTimes(ZKConsts.RETRYTIME, ZKConsts.SLEEP_MS_BEWTEENR_RETRY);
        client = CuratorFrameworkFactory.builder()
                .connectString(ZKConsts.ZK_SERVER_PATH)
                .sessionTimeoutMs(ZKConsts.SESSION_TIMEOUT_MS).retryPolicy(retryPolicy)
                .namespace(ZKConsts.WORK_SPACE).build();
        client.start();
    }


    //注册接口、对应服务ip及其实现类
    public static void register(String serviceName, URL url) {
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(getPath(serviceName, url.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //hostname:port,遍历所有interface节点，把对应的url节点去掉
    public static void remove(String url) {
        try {
            List<String> interfaces = client.getChildren().forPath("/");
            for (String anInterface : interfaces) {
                List<String> urlList = client.getChildren().forPath(getPath(anInterface));
                for (String s : urlList) {
                    if (s.equals(url)) {
                        client.delete().forPath(getPath(anInterface, url));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String,List<URL>> getAllURL(){
        Map<String,List<URL>> mapList=null;
        try {
            List<String> servcieList=client.getChildren().forPath("/");
            mapList=new HashMap<>(servcieList.size());
            for (String s : servcieList) {
                mapList.put(s,getService(s));
            }
           /* //设置监听，监听所有服务下的节点变化，当删除由于不活跃移除节点时，连接管理收到通知后移除相应的节点
            final PathChildrenCache childrenCache = new PathChildrenCache(client, "/user", true);
            //同步初始监听节点
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                        System.out.println("删除子节点:" + event.getData().getPath());
                    }
                }
            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }



    public static List<URL> getService(String serviceName){
        List<URL> urls=null;
        try {
           List<String> urlList = client.getChildren().forPath(getPath(serviceName));
           if(urlList!=null){
               urls=new ArrayList<>(urlList.size());
           }
            for (String s : urlList) {
                String[] url = s.split(":");
                urls.add(new URL(url[0], Integer.valueOf(url[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    //通过服务名获取具体的url
    public static URL random(String serviceName) {
        try {

            List<String> urlList = client.getChildren().forPath(getPath(serviceName));
            String[] url = urlList.get(0).split(":");
            return new URL(url[0], Integer.valueOf(url[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //生成节点方法
    private static String getPath(String... args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append("/").append(arg);
        }
        return builder.toString();
    }


    public static void closeZKClient() {
        if (client != null) {
            client.close();
        }
    }


}
