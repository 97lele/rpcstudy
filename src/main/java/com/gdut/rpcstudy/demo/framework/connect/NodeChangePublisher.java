package com.gdut.rpcstudy.demo.framework.connect;

import com.gdut.rpcstudy.demo.framework.URL;

/**
 * @author: lele
 * @date: 2019/11/22 下午3:29
 */
public interface NodeChangePublisher {

     void addListener(NodeChangeListener listener) ;

     void removeListener(NodeChangeListener listener) ;

     void notifyListener(int state, String path) ;

}
