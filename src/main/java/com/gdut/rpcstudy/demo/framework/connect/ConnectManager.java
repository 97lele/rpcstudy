package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.NettyAsynHandler;
import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcDecoder;
import com.gdut.rpcstudy.demo.framework.serialize.handler.RpcEncoder;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import com.gdut.rpcstudy.demo.register.zk.ZkRegister;
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
 */
public class ConnectManager {


    //当服务还未链接时，无法获取可用handler，此时通过锁来锁定管理
    private ReentrantLock lock = new ReentrantLock();

    //条件锁，等待可用的客户端出现
    private Condition connected = lock.newCondition();

    private Boolean isShutDown = false;

    //客户端链接服务端超时时间
    private long connectTimeoutMillis = 5000;

    //自定义6个线程组用于客户端服务
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(6);

    //存放服务对应的访问数，用于轮询
    private Map<String, AtomicInteger> pollingMap = new ConcurrentHashMap<>();


    //存放服务端地址和handler的关系
    private Map<String, Map<URL, NettyAsynHandler>> serverClientMap = new ConcurrentHashMap<>();

    //用来初始化链接
    private ThreadPoolExecutor clientBooter = new ThreadPoolExecutor(
            16, 16, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1024)
            , new BooterThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    private static class Holder {
        private static final ConnectManager j = new ConnectManager();
    }

    private ConnectManager() {
        Map<String, List<URL>> allURL = ZkRegister.getAllURL();
        addServer(allURL);
    }

    public static ConnectManager getInstance() {

        return Holder.j;
    }

    //添加该服务对应的链接和handler
    public void addConnection(String serviceName, URL url, NettyAsynHandler handler) {
        Map<URL, NettyAsynHandler> handlerMap;
        if (!serverClientMap.containsKey(serviceName)) {
            handlerMap = new HashMap<>();
        } else {
            handlerMap = serverClientMap.get(serviceName);
        }
        handlerMap.put(url, handler);
        serverClientMap.put(serviceName, handlerMap);
        signalAvailableHandler();
    }

    //获取对应服务下的handler，通过轮询获取
    public NettyAsynHandler getConnectionWithPolling(String servicName) {
        Map<URL, NettyAsynHandler> urlNettyAsynHandlerMap = serverClientMap.get(servicName);
        int size = 0;
        //先尝试获取
        if (urlNettyAsynHandlerMap != null) {
            size = urlNettyAsynHandlerMap.size();
        }
        //不行就自选等待
        while (!isShutDown && size <= 0) {
            try {
                //自旋等待可用服务出现，因为客户端与服务链接需要一定的时间，如果直接返回会出现空指针异常
                boolean available = waitingForHandler();
                if (available) {
                    urlNettyAsynHandlerMap = serverClientMap.get(servicName);
                    size = urlNettyAsynHandlerMap.size();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("出错", e);
            }
        }
        //获取对应的访问次数
        AtomicInteger count = pollingMap.get(servicName);
        int index = (count.getAndAdd(1) + size) % size;

        Iterator<Map.Entry<URL, NettyAsynHandler>> iterator = urlNettyAsynHandlerMap.entrySet().iterator();
        //取出相应的handler
        NettyAsynHandler nettyAsynHandler = null;
        for (int i = 0; i <= index; i++) {
            nettyAsynHandler = iterator.next().getValue();
        }
        return nettyAsynHandler;
    }

    //等待一定时间，等handler和相应的server建立建立链接
    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void removeURL(URL url){
        List<String> list=new ArrayList<>();
        for (Map.Entry<String,Map<URL,NettyAsynHandler>> map:serverClientMap.entrySet()){
            for (Map.Entry<URL, NettyAsynHandler> urlNettyAsynHandlerEntry : map.getValue().entrySet()) {
               if( urlNettyAsynHandlerEntry.getKey().equals(url)){
                   urlNettyAsynHandlerEntry.getValue().close();
                   list.add(map.getKey()+"@"+urlNettyAsynHandlerEntry.getKey());
               }
            }
        }
        for (String s : list) {
            String[] split = s.split("@");
            serverClientMap.get(split[0]).remove(split[1]);
        }

    }

    //唤醒其他的在该条件等待的线程,相当于释放锁
    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    //添加server，并启动对应的服务器
    public void addServer(Map<String, List<URL>> allURL) {

        for (String s : allURL.keySet()) {
            pollingMap.put(s, new AtomicInteger(0));
            List<URL> urls = allURL.get(s);
            for (URL url : urls) {
                clientBooter.submit(new Runnable() {
                    @Override
                    public void run() {
                        createClient(s, eventLoopGroup, url);
                    }
                });
            }
        }
    }

    //创建客户端,持久化链接
    public void createClient(String serviceName, EventLoopGroup eventLoopGroup, URL url) {
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
                                .addLast(new NettyAsynHandler());

                    }
                }));


        ChannelFuture channelFuture = b.connect(url.getHostname(), url.getPort());

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                //链接成功后的操作，把相应的url地址和客户端链接存入
                if (channelFuture.isSuccess()) {
                    NettyAsynHandler handler = channelFuture.channel().pipeline().get(NettyAsynHandler.class);
                    addConnection(serviceName, url, handler);
                }
            }
        });
    }


    //自定义线程工厂
    static class BooterThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        BooterThreadFactory() {
            group = new ThreadGroup("connectManger");
            group.setDaemon(false);
            group.setMaxPriority(5);
            namePrefix = "clientBooter-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            return t;
        }
    }
    public void stop() {
        isShutDown = true;
        for (Map<URL, NettyAsynHandler> urlNettyAsynHandlerMap : serverClientMap.values()) {
            urlNettyAsynHandlerMap.values().forEach(e->e.close());
        }
        signalAvailableHandler();
        clientBooter.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}
