package com.gdut.rpcstudy.demo.framework.connect;


import com.gdut.rpcstudy.demo.framework.URL;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:26
 */
public interface NodeChangeListener {

    ConnectManager connect=ConnectManager.getInstance();

    void change(int state,URL url,String serviceName);

    int inactive=0;
    int remove=1;
    int add=2;
    int reactive=3;


    class AddServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url,String serviceName) {
            if(state==NodeChangeListener.add){
                System.out.println(Thread.currentThread().getName()+"addNode的listern事件被触发");
                connect.addServerAfter(url,serviceName);
            }
        }
    }

    class ReActiveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangeListener.reactive){
                System.out.println("reActive的listern事件被触发");
                connect.reAddActiveURL(url,serviceName);
            }
        }
    }

    class InactiveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangeListener.inactive){
                System.out.println("InActive的listern事件被触发");

                connect.addInactiveURL(url,serviceName);
            }
        }
    }

    class RemoveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangeListener.remove){
                System.out.println("RemovServer的listern事件被触发");
                connect.removeURL(url,serviceName,true);
            }
        }
    }



}
