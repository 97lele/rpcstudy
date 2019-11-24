package com.gdut.rpcstudy.demo.register.zk;

import com.gdut.rpcstudy.demo.consts.ZKConsts;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.connect.NodeChangeListener;
import com.gdut.rpcstudy.demo.framework.connect.NodeChangePublisher;
import com.gdut.rpcstudy.demo.utils.RpcThreadFactoryBuilder;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.gdut.rpcstudy.demo.consts.ZKConsts.ACTIVE;

/**
 * @author lulu
 * @Date 2019/11/23 22:49
 *
 */
public class RegisterForClient implements NodeChangePublisher {

    private CuratorFramework client = null;

    private List<PathChildrenCache> nodeListenList = new ArrayList<>();

    private List<NodeChangeListener> nodeChangeListeners = new ArrayList<>();

    private ThreadPoolExecutor notifyPool = new ThreadPoolExecutor(
            16, 16, 5, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1024)
            , new RpcThreadFactoryBuilder().setNamePrefix("notifyPool").build()
    );




    private static class Holder {
        private static final RegisterForClient j = new RegisterForClient();
    }

    public static RegisterForClient getInstance() {
        return Holder.j;
    }

    //添加监听者
    private RegisterForClient() {
        client = ZkUtils.getClient();
        this.addListener(new NodeChangeListener.AddServer());
        this.addListener(new NodeChangeListener.RemoveServer());
        this.addListener(new NodeChangeListener.InactiveServer());
        this.addListener(new NodeChangeListener.ReActiveServer());
    }



    /**
     * 获取所有的url
     *
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
                    //建立完监听
                    return;
                }
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    String path = event.getData().getPath();
                    notifyPool.submit(() -> {
                        System.out.println("删除远程服务端节点:" + path);
                        notifyListener(NodeChangePublisher.remove, path);
                    });
                }
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                    String path = event.getData().getPath();
                    notifyPool.submit(() -> {
                        byte[] status = event.getData().getData();
                        String serverStatus= new String(status);
                        if (serverStatus.equals(ACTIVE)) {
                            notifyPool.submit(() -> {
                                System.out.println("远程服务端上线事件:" + NodeChangePublisher.add + path);
                                notifyListener(NodeChangePublisher.add, path);
                            });
                        } else if (serverStatus.equals(ZKConsts.INACTIVE)) {
                            //失效事件
                            notifyPool.submit(() -> {
                                System.out.println("远程服务端下线事件：" + NodeChangePublisher.inactive + path);
                                notifyListener(NodeChangePublisher.inactive, path);
                            });
                        } else if (serverStatus.equals(ZKConsts.REACTIVE)) {
                            notifyPool.submit(() -> {
                                System.out.println("远程服务端重新上线事件：" + path);
                                notifyListener(NodeChangePublisher.reactive, path);
                            });
                        }
                    });

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

    //hostname:port,遍历所有interface节点，把对应的url节点去掉,或者标记为不活跃的状态
    public void removeOrUpdate(String sl, Boolean update, String data) {
        String[] serviceUrl = sl.split("@");
        try {
            String url = serviceUrl[1];
            String anInterface = serviceUrl[0];
            List<String> urlList = client.getChildren().forPath(ZkUtils.getPath(anInterface));
            for (String s : urlList) {
                if (s.equals(url)) {
                    if (update) {
                        client.setData().forPath(ZkUtils.getPath(anInterface, url), data.getBytes());
                    } else {
                        client.delete().forPath(ZkUtils.getPath(anInterface, url));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //同步模式下使用，可以当作废弃
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
