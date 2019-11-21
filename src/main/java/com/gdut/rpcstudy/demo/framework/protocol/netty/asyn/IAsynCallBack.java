package com.gdut.rpcstudy.demo.framework.protocol.netty.asyn;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;

/**
 * @author: lele
 * @date: 2019/11/21 下午6:51
 * 回调接口
 */
public interface IAsynCallBack {
    //成功时候执行
    void success(Object response);
    //失败时候执行
    void error(RuntimeException exception);

}
