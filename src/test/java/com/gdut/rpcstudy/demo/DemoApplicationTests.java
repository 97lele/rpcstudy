package com.gdut.rpcstudy.demo;

import com.gdut.rpcstudy.demo.consumer.HelloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private HelloService helloService;

    @Test
    void contextLoads() throws InterruptedException {

        int threadCount = 10;
        int requestCount = 100;
        CountDownLatch countDownLatch=new CountDownLatch(threadCount*requestCount);
        long start = System.currentTimeMillis();
        ExecutorService service = new ThreadPoolExecutor(threadCount, threadCount, 6000, TimeUnit.SECONDS, new LinkedBlockingDeque<>()
        );
        for (int i = 0; i < threadCount; i++) {
            service.submit(() -> {
                for (int q = 0; q < requestCount; q++) {
                    helloService.sayHello("test");
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        long end=System.currentTimeMillis();
        System.out.printf("耗时:%dms,qps(ms)%d%n",end-start,threadCount*requestCount/(end-start));
    }

}
