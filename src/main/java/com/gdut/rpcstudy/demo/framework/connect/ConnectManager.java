package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.NettyAsynHandler;
import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcDecoder;
import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcEncoder;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.register.zk.RegisterForClient;
import com.gdut.rpcstudy.demo.utils.RpcThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: lele
 * @date: 2019/11/21 上午11:58
 * 管理连接池
 * 服务端在注册后，不一定可以获得，因为还没提供服务，需要zk设置节点 状态为Active
 * //todo 定时更新链接
 */

public class ConnectManager  {


    private Boolean isShutDown = false;

    private ScheduledExecutorService removeInactiveTask;

    /**
     * 客户端链接服务端超时时间
     */
    private long connectTimeoutMillis = 6000;

    /**
     * 不活跃的链接存活时间，单位ms,这里表示5分钟不活跃就去掉,如果想查看演示效果可减少这里的时间以及下面执行任务的周期时间
     */
    private long maxInActiveTime = 1000 * 60 * 5;

    /**
     * 自定义6个线程组用于客户端服务
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(6);
    /**
     * 标示非init状态下，addServerAfter才能起作用
     */
    private CountDownLatch serverInitCountDownLatch;

    /**
     * 存放服务对应的访问数，用于轮询
     */
    private Map<String, AtomicInteger> pollingMap = new ConcurrentHashMap<>();

    /**
     * 对于每个服务都有一个锁，每个锁都有一个条件队列，用于控制链接获取以及添加链接
     */
    private Map<String, Object[]> serviceCondition = new ConcurrentHashMap<>();


    /**
     * 新增/删除链接时的锁
     */
    private Map<String, ReentrantLock[]> addOrRemoveConnectionLock = new ConcurrentHashMap<>();

    /**
     * 新增/删除不活跃链接时的锁
     */
    private Map<String, ReentrantLock[]> addOrRemoveInactiveLock = new ConcurrentHashMap<>();


    /**
     * 存放服务端地址和handler的关系
     */
    private Map<String, List<NettyAsynHandler>> serverClientMap = new ConcurrentHashMap<>();


    /**
     * 存放不活跃的服务端地址和handler的关系，当活跃时添加回正式的handler
     */
    private Map<String, PriorityQueue<NettyAsynHandler>> inactiveClientMap = new ConcurrentHashMap<>();

    /**
     * 用来初始化客户端
     */
    private ThreadPoolExecutor clientBooter = new ThreadPoolExecutor(
            16, 16, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024)
            , new RpcThreadFactoryBuilder().setNamePrefix("clientBooter").build(), new ThreadPoolExecutor.AbortPolicy());


    private static class Holder {
        private static final ConnectManager j = new ConnectManager();
    }

    private ConnectManager() {
        //初始化时把所有的url加进去,这里可能没有可用链接，所以需要添加对节点的监听

        Map<String, List<URL>> allURL = RegisterForClient.getInstance().getAllURL();
        for (String s : allURL.keySet()) {
            //为每个服务添加锁和条件队列，通过条件队列控制客户端链接获取
            addLockToService(s);
        }
        addServerInit(allURL);
        //定时清理不用的链接
        removeInactiveTask = Executors.newSingleThreadScheduledExecutor();
        removeInactiveTask.scheduleAtFixedRate(() -> removeInactiveURL(), 5, 5, TimeUnit.MINUTES);
    }

    //为每个服务添加对应的锁
    private void addLockToService(String serviceName) {
        ReentrantLock lock = new ReentrantLock();
        Condition getConnection = lock.newCondition();
        //获取可用客户端的链接及条件队列
        serviceCondition.put(serviceName, new Object[]{lock, getConnection});
        //为创建客户端链接添加锁
        ReentrantLock addConnection = new ReentrantLock();
        ReentrantLock removeConnection = new ReentrantLock();
        addOrRemoveConnectionLock.put(serviceName, new ReentrantLock[]{addConnection, removeConnection});

        ReentrantLock addInactive = new ReentrantLock();
        ReentrantLock removeInactive = new ReentrantLock();
        addOrRemoveInactiveLock.put(serviceName, new ReentrantLock[]{addInactive, removeInactive});
    }

    public static ConnectManager getInstance() {

        return Holder.j;
    }


    /**
     * 添加该服务对应的链接和handler
     *
     * @param serviceName
     * @param handler     由于创建客户端链接的线程都会访问这段代码，这里也会存在并发情况，不然会导致多个server上线后，获取异常
     */
    public void addConnection(String serviceName, NettyAsynHandler handler) {
        ReentrantLock lock = addOrRemoveConnectionLock.get(serviceName)[0];
        lock.lock();
        List<NettyAsynHandler> nettyAsynHandlers;
        if (!serverClientMap.containsKey(serviceName)) {
            nettyAsynHandlers = new ArrayList<>();
        } else {
            nettyAsynHandlers = serverClientMap.get(serviceName);
        }
        nettyAsynHandlers.add(handler);
        //添加服务名和对应的url:客户端链接
        serverClientMap.put(serviceName, nettyAsynHandlers);
        //如果处于初始化状态，则countdown防止新增节点事件再次新增客户端
        if (serverInitCountDownLatch.getCount() != 0) {
            System.out.println("连接池初始化新建客户端链接:" + handler.getUrl());
            serverInitCountDownLatch.countDown();
        } else {
            System.out.println("连接池初始化后新建客户端链接:" + handler.getUrl());
        }

        //唤醒等待客户端链接的线程
        signalAvailableHandler(serviceName);
        lock.unlock();
    }

//通过对应的负载均衡策略挑选可用客户端连接
public NettyAsynHandler chooseHandler(String serviceName,Integer mode){
    List<NettyAsynHandler> handlers = mayWaitBeforeGetConnection(serviceName);
    NettyAsynHandler choose = FetchPolicy.getPolicyMap().get(mode).choose(serviceName, handlers);
    return choose;
}

//等待可用的客户端连接
  private  List<NettyAsynHandler> mayWaitBeforeGetConnection(String serviceName) {
        List<NettyAsynHandler> nettyAsynHandlers = serverClientMap.get(serviceName);
        int size = 0;
        //先尝试获取
        if (nettyAsynHandlers != null) {
            size = nettyAsynHandlers.size();
        }
        //不行就自选等待
        while (!isShutDown && size <= 0) {
            try {
                //自旋等待可用服务出现，因为客户端与服务链接需要一定的时间，如果直接返回会出现空指针异常
                boolean available = waitingForHandler(serviceName);
                if (available) {
                    nettyAsynHandlers = serverClientMap.get(serviceName);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("出错", e);
            }
        }
        return nettyAsynHandlers;
    }





    /**
     * 等待一定时间，等handler和相应的server建立建立链接，用条件队列控制
     *
     * @param serviceName
     * @return
     * @throws InterruptedException
     */
    private boolean waitingForHandler(String serviceName) throws InterruptedException {
        Object[] objects = serviceCondition.get(serviceName);
        ReentrantLock lock = (ReentrantLock) objects[0];
        lock.lock();
        Condition condition = (Condition) objects[1];
        try {
            return condition.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 去掉所有与该url链接的客户端,并且关闭客户端链接
     *
     * @param url
     */
    public void removeURL(URL url, String serviceName, boolean close) {
        ReentrantLock lock = addOrRemoveConnectionLock.get(serviceName)[1];
        lock.lock();
        NettyAsynHandler target = null;
        //倒序遍历删除对应的handler
        List<NettyAsynHandler> nettyAsynHandlers = serverClientMap.get(serviceName);
        for (int i = nettyAsynHandlers.size() - 1; i >= 0; i--) {
            if ((target = nettyAsynHandlers.get(i)).getUrl().equals(url)) {
                nettyAsynHandlers.remove(i);
                if (close) {
                    target.close();
                }
            }
        }
        System.out.println("active:" + serverClientMap.get(serviceName).toString());
        lock.unlock();
    }

    /**
     * 定时清除不活跃的链接
     */
    public void removeInactiveURL() {
        /**
         * 移除不活跃列表
         */
        System.out.println("执行移除不活跃server任务");
        Collection<PriorityQueue<NettyAsynHandler>> values = inactiveClientMap.values();
        Iterator<PriorityQueue<NettyAsynHandler>> iterator = values.iterator();
        while (iterator.hasNext()) {
            PriorityQueue<NettyAsynHandler> list = iterator.next();
            //遍历所有客户端并根据超时时间删除
            NettyAsynHandler target;
            long current = System.currentTimeMillis();
            while ((current - (target = list.peek()).getInActiveTime()) > maxInActiveTime) {
                NettyAsynHandler poll = list.poll();
                URL url = poll.getUrl();
                System.out.println("移除:"+url.toString());
                target.close();
            }
        }


    }


    /**
     * 去掉可用的服务，把他加入到不活跃的列表
     * 由于是通过线程异步操作，可能存在并发问题
     *
     * @param url
     */
    public void addInactiveURL(URL url, String serviceName) {
        ReentrantLock lock = addOrRemoveInactiveLock.get(serviceName)[0];
        lock.lock();
        System.out.println("不活跃链接加入_" + url.toString());
        List<NettyAsynHandler> nettyAsynHandlers = serverClientMap.get(serviceName);
        NettyAsynHandler inActive = null;
        for (NettyAsynHandler nettyAsynHandler : nettyAsynHandlers) {
            if (nettyAsynHandler.getUrl().equals(url)) {
                nettyAsynHandler.setInActiveTime(System.currentTimeMillis());
                inActive = nettyAsynHandler;
                break;
            }
        }
        PriorityQueue<NettyAsynHandler> inActiveHandlers = null;
        if ((inActiveHandlers = inactiveClientMap.get(serviceName)) == null) {
            inActiveHandlers = new PriorityQueue<>();
        }
        inActiveHandlers.offer(inActive);
        inactiveClientMap.put(serviceName, inActiveHandlers);
        System.out.println("inactive:" + inactiveClientMap.get(serviceName).toString());

        lock.unlock();
        //删除url
        removeURL(url, serviceName, false);
    }

    /**
     * 重新添加进活跃队列
     *
     * @param url
     * @param serviceName
     */
    public void reAddActiveURL(URL url, String serviceName) {
        ReentrantLock lock = addOrRemoveInactiveLock.get(serviceName)[1];
        lock.lock();
        PriorityQueue<NettyAsynHandler> list;
        if ((list = inactiveClientMap.get(serviceName)) != null) {
            Iterator<NettyAsynHandler> iterator = list.iterator();
            NettyAsynHandler nettyAsynHandler;
            while (iterator.hasNext()) {
                nettyAsynHandler = iterator.next();
                if (nettyAsynHandler.getUrl().equals(url)) {
                    nettyAsynHandler.setInActiveTime(0);
                    addConnection(serviceName, nettyAsynHandler);
                    list.remove(nettyAsynHandler);

                    System.out.printf("%s服务下的%s重新添加进活跃队列%n", serviceName, nettyAsynHandler.toString());
                    break;
                }
            }
        }
        lock.unlock();
    }


    /**
     * 释放对应服务的条件队列,代表有客户端链接可用了
     *
     * @param serviceName
     */
    private void signalAvailableHandler(String serviceName) {
        Object[] objects = serviceCondition.get(serviceName);
        ReentrantLock lock = (ReentrantLock) objects[0];
        lock.lock();
        Condition condition = (Condition) objects[1];
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 添加server，并启动对应的服务器
     *
     * @param allURL
     */
    public void addServerInit(Map<String, List<URL>> allURL) {
        Collection<List<URL>> values = allURL.values();

        Iterator<List<URL>> iterator = values.iterator();
        int res = 0;
        while (iterator.hasNext()) {
            List<URL> next = iterator.next();
            res += next.size();
        }
        serverInitCountDownLatch = new CountDownLatch(res);
        for (String s : allURL.keySet()) {
            pollingMap.put(s, new AtomicInteger(0));
            List<URL> urls = allURL.get(s);
            for (URL url : urls) {
                //提交创建任务
                clientBooter.submit(new Runnable() {
                    @Override
                    public void run() {
                        createClient(s, eventLoopGroup, url);
                    }
                });
            }
        }
    }

    /**
     * 当新节点出现后添加，但这里有个隐患，就是当client尚未监听完所有节点时
     * addServerAfter是不允许操作的
     *
     * @param url
     * @param serviceName
     */
    public void addServerAfter(URL url, String serviceName) {

        if (serverInitCountDownLatch.getCount() == 0) {
            //如果还没监听完，就不可以加链接
            List<NettyAsynHandler> list = null;
            if ((list = serverClientMap.get(serviceName)) == null) {
                list = new ArrayList<>();
                serverClientMap.put(serviceName, list);
                addLockToService(serviceName);
            } else {
                boolean exists = list.stream().filter(e -> e.getUrl().equals(url)).findFirst().isPresent();
                if (exists) {
                    return;
                }
            }
            clientBooter.submit(new Runnable() {
                @Override
                public void run() {
                    createClient(serviceName, eventLoopGroup, url);
                }
            });
        }

    }


    /**
     * 创建客户端,持久化链接
     *
     * @param serviceName
     * @param eventLoopGroup
     * @param url
     */
    public void createClient(String serviceName, EventLoopGroup eventLoopGroup, URL url) {
        System.out.println(Thread.currentThread().getName() + "准备新建客户端");
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler((new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                //把request实体变为字节
                                .addLast(new RpcEncoder(RpcRequest.class))
                                //把返回的response字节变为对象
                                .addLast(new RpcDecoder(RpcResponse.class))
                                .addLast(new NettyAsynHandler(url));
                    }
                }));

        ChannelFuture channelFuture = b.connect(url.getHostname(), url.getPort());


        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //链接成功后的操作，把相应的url地址和客户端链接存入
                if (channelFuture.isSuccess()) {
                    NettyAsynHandler handler = channelFuture.channel().pipeline().get(NettyAsynHandler.class);
                    addConnection(serviceName, handler);
                }
            }
        });
    }


    /**
     * 关闭方法，关闭每个客户端链接，释放所有锁，关掉创建链接的线程池，和客户端的处理器
     */
    public void stop() {
        isShutDown = true;
        serverClientMap.values().forEach(e -> e.forEach(k -> k.close()));
        inactiveClientMap.values().forEach(e -> e.forEach(k -> k.close()));
        for (String s : serviceCondition.keySet()) {
            signalAvailableHandler(s);
        }

        clientBooter.shutdown();
        eventLoopGroup.shutdownGracefully();
    }


}
