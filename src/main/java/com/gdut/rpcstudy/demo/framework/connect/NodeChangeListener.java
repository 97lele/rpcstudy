package com.gdut.rpcstudy.demo.framework.connect;


import com.gdut.rpcstudy.demo.framework.URL;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:26
 */
public interface NodeChangeListener {

    ConnectManager connect=ConnectManager.getInstance();

    //相应的处理
    void change(int state,URL url,String serviceName);




    class AddServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url,String serviceName) {
            if(state==NodeChangePublisher.add){
                System.out.println(Thread.currentThread().getName()+"addNode的listern事件被触发");
                connect.addServerAfter(url,serviceName);
            }
        }
    }

    class ReActiveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangePublisher.reactive){
                System.out.println("reActive的listern事件被触发");
                connect.reAddActiveURL(url,serviceName);
            }
        }
    }

    class InactiveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangePublisher.inactive){
                System.out.println("InActive的listern事件被触发");

                connect.addInactiveURL(url,serviceName);
            }
        }
    }

    class RemoveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangePublisher.remove){
                System.out.println("RemovServer的listern事件被触发");
                connect.removeURL(url,serviceName,true);
            }
        }
    }



}
