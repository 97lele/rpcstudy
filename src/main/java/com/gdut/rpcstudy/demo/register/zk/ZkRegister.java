package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.connect.ConnectManager;
import com.gdut.rpcstudy.demo.framework.connect.NodeChangeListener;
import com.gdut.rpcstudy.demo.framework.connect.NodeChangePublisher;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author lulu
 * @Date 2019/11/18 21:17
 * 负责实现注册中心具体的业务功能
 */
public class ZkRegister implements NodeChangePublisher {


    public static final Integer INACTIVE = 0;

    private CuratorFramework client = null;

    private List<PathChildrenCache> nodeListenList = new ArrayList<>();

    private List<NodeChangeListener> nodeChangeListeners = new ArrayList<>();


    private static class Holder {
        private static final ZkRegister j = new ZkRegister();
    }

    private ZkRegister() {
        client = ZkUtils.getClient();
        this.addListener(new NodeChangeListener.AddServer());
        this.addListener(new NodeChangeListener.RemoveServer());
        this.addListener(new NodeChangeListener.InactiveServer());
    }

    public static ZkRegister getInstance() {

        return Holder.j;
    }

    public URL random(String serviceName) {

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

    //hostname:port,遍历所有interface节点，把对应的url节点去掉,或者标记为不活跃的状态
    public void removeOrUpdate(String sl, Boolean update) {
        String[] serviceUrl=sl.split("@");
        try {
            String url=serviceUrl[1];
            String anInterface=serviceUrl[0];
                List<String> urlList = client.getChildren().forPath(ZkUtils.getPath(anInterface));
                for (String s : urlList) {
                    if (s.equals(url)) {
                        if (update) {
                            client.setData().forPath(ZkUtils.getPath(anInterface, url), INACTIVE.toString().getBytes());
                        } else {
                            client.delete().forPath(ZkUtils.getPath(anInterface, url));
                        }
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有的url
     * @return
     */
    public Map<String, List<URL>> getAllURL() {
        Map<String, List<URL>> mapList = null;
        try {
            List<String> servcieList = client.getChildren().forPath("/");
            mapList = new HashMap<>(servcieList.size());
            for (String s : servcieList) {
                //返回对应的service及其可用的url
                mapList.put(s, getService(s));
                //为每个服务添加监听
                addListenerForService(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapList;
    }

    private void addListenerForService(String serviceName) throws Exception {
        //设置监听，监听所有服务下的节点变化，连接管理收到通知后移除相应的节点
        final PathChildrenCache childrenCache = new PathChildrenCache(client, ZkUtils.getPath(serviceName), true);
        nodeListenList.add(childrenCache);
        //同步初始监听节点
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
                    return;
                }

                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    String path = event.getData().getPath();
                    //格式 service:url
                    System.out.println("删除子节点:" + path);
                    notifyListener(NodeChangeListener.remove, path);
                }
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    String path = event.getData().getPath();
                    System.out.println("新增子节点事件" + path);
                    notifyListener(NodeChangeListener.add, path);
                }
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    String path = event.getData().getPath();
                    System.out.println("修改子节点");
                    if(new String(event.getData().getData()).equals(INACTIVE)){
                        notifyListener(NodeChangeListener.inactive, path);
                    }
                }
            }
        });
    }


    public List<URL> getService(String serviceName) {
        List<URL> urls = null;
        try {
            List<String> urlList = client.getChildren().forPath(ZkUtils.getPath(serviceName));
            if (urlList != null) {
                urls = new ArrayList<>(urlList.size());
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

    public void close() {
        ZkUtils.closeZKClient(client);
        nodeListenList.forEach(e -> {
            try {
                e.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }


    @Override
    public void addListener(NodeChangeListener listener) {
        nodeChangeListeners.add(listener);
    }

    @Override
    public void removeListener(NodeChangeListener listener) {
        nodeChangeListeners.remove(listener);
    }

    @Override
    public void notifyListener(int state, String path) {
        int i = path.lastIndexOf("/");
        String serviceName = path.substring(1, i);
        String[] split = path.substring(i + 1).split(":");
        URL url = new URL(split[0], Integer.valueOf(split[1]));
        for (NodeChangeListener nodeChangeListener : nodeChangeListeners) {
            nodeChangeListener.change(state, url, serviceName);
        }
    }
}
