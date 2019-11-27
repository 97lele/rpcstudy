package com.gdut.rpcstudy.demo.framework.client;

import com.gdut.rpcstudy.demo.DemoApplication;
import com.gdut.rpcstudy.demo.framework.connect.ConnectManager;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.stereotype.Component;

/**
 * @author: lele
 * @date: 2019/11/27 下午2:38
 */
@Component
public class ConnectMangerInitlizer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        if(DemoApplication.mode==1){
            ConnectManager.getInstance().init();
        }
    }
}
