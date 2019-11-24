package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.protocol.netty.asyn.NettyAsynHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:24
 * 获取链接机制,轮询、随机、权重
 */
public interface FetchPolicy {
    Random RANDOM = new Random();
    int random = 1;
    int polling = 2;
    int weight = 3;
    int bestRequest = 4;
    //策略类
    Map<Integer, FetchPolicy> policyMap = new HashMap<>();
    static Map<Integer, FetchPolicy> getPolicyMap() {
        policyMap.put(random, new RandomFetch());
        policyMap.put(polling, new PollingFetch());
        policyMap.put(weight, new WeightFetch());
        policyMap.put(bestRequest, new BestRequestFetch());
        return policyMap;
    }

    NettyAsynHandler choose(String serviceName, List<NettyAsynHandler> handlers);


    class WeightFetch implements FetchPolicy {
        @Override
        public NettyAsynHandler choose(String serviceName, List<NettyAsynHandler> handlers) {
            int length = handlers.size();
            //总权重
            int totalWeight = 0;
            //是否权重一致
            boolean sameWeight = true;
            //先把所有权重加起来，并且判断权重是否一致
            for (int i = 0; i < length; i++) {
                int weight = handlers.get(i).getWeight();
                totalWeight += weight;
                if (sameWeight && i > 0
                        && weight != handlers.get(i - 1).getWeight()) {
                    sameWeight = false;
                }
            }
            //不断减去对应权重所在的区间
            if (totalWeight > 0 && !sameWeight) {
                int offset = RANDOM.nextInt(totalWeight);
                for (int i = 0; i < length; i++) {
                    offset -= handlers.get(i).getWeight();
                    if (offset < 0) {
                        return handlers.get(i);
                    }
                }
            }
            // 如果权重都一样，则轮询返回
            return FetchPolicy.getPolicyMap().get(polling).choose(serviceName,handlers);
        }
    }

    /**
     * 主要通过NettyAsynHandler的requestCount属性挑取最小请求的handler进行返回
     */
    class BestRequestFetch implements FetchPolicy {

        @Override
        public NettyAsynHandler choose(String serviceName, List<NettyAsynHandler> handlers) {
            int minRequest = Integer.MAX_VALUE;
            NettyAsynHandler res = null;
            for (NettyAsynHandler handler : handlers) {
                if (handler.getRequestCount().get() < minRequest) {
                    res = handler;
                }
            }

            if(res==null){
                // 如果找不到，则轮询返回
                return FetchPolicy.getPolicyMap().get(polling).choose(serviceName,handlers);
            }
            return res;
        }
    }

    /**
     * 记录每个服务对应的请求次数，并返回对应的handler
     */
    class PollingFetch implements FetchPolicy {
        private static Map<String, AtomicInteger> pollingMap = new ConcurrentHashMap<>();

        @Override
        public NettyAsynHandler choose(String serviceName, List<NettyAsynHandler> handlers) {
            if (pollingMap.get(serviceName) == null) {
                pollingMap.put(serviceName, new AtomicInteger(0));
            }
            int next = pollingMap.get(serviceName).getAndIncrement();
            int index = RANDOM.nextInt(next);
            return handlers.get(index);
        }
    }

    /**
     * 随机
     */
    class RandomFetch implements FetchPolicy {

        @Override
        public NettyAsynHandler choose(String serviceName, List<NettyAsynHandler> handlers) {

            int index = RANDOM.nextInt(handlers.size());
            //取出相应的handler
            NettyAsynHandler nettyAsynHandler = null;
            for (int i = 0; i < index; i++) {
                nettyAsynHandler = handlers.get(i);
            }
            return nettyAsynHandler;
        }
    }

}
