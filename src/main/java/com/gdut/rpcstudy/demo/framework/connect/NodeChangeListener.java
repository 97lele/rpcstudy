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


    class AddServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url,String serviceName) {
            if(state==NodeChangeListener.add){
                connect.addServerAfter(url,serviceName);
            }
        }
    }

    class InactiveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangeListener.inactive){
                connect.addInactiveURL(url,serviceName);
            }
        }
    }

    class RemoveServer implements NodeChangeListener{

        @Override
        public void change(int state, URL url, String serviceName) {
            if(state==NodeChangeListener.remove){
                connect.removeURL(url,serviceName);
            }
        }
    }



}
