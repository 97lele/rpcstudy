package com.gdut.rpcstudy.demo.framework.serialize.tranobject;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:01
 * 封装调用方所想调用的远程方法信息
 */
@Data
@AllArgsConstructor
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -7789662944864163267L;
    private String requestId;

    private String interfaceName;

    private String methodName;
    private Object[] params;
    //防止重载
    private Class[] paramsTypes;
}
