package com.gdut.rpcstudy.demo;

import com.gdut.rpcstudy.demo.framework.ProxyFactory;
import com.gdut.rpcstudy.demo.consumer.HelloService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:45
 */
@Data
@AllArgsConstructor
public class Person {
    private String name;
    private Integer age;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i=0;i<200;i++){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(new Random().nextInt(3));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    test();
                }
            });
        }
        executorService.shutdown();

    }
    public static void test(){
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result = helloService.qq();
        System.out.println(result);
    }
}
