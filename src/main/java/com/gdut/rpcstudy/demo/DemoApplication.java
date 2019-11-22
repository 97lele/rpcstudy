package com.gdut.rpcstudy.demo;

import com.gdut.rpcstudy.demo.framework.client.EnableRpcStudyClient;
import com.gdut.rpcstudy.demo.utils.ZkUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
@EnableRpcStudyClient(basePackages = {"com.gdut.rpcstudy.demo"})
public class DemoApplication {
    //server为0，client为1
    public static Integer mode = 1;

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(DemoApplication.class);
        //server模式时去掉web容器
        if (mode == 0) {
            application.setWebApplicationType(WebApplicationType.NONE);
        }
        application.run(args);
    }

}
