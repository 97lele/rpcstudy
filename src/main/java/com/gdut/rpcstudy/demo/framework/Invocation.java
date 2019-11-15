package com.gdut.rpcstudy.demo.framework;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:01
 */
@Data
@AllArgsConstructor
public class Invocation implements Serializable {

    private static final long serialVersionUID = -7789662944864163267L;
    private String interfaceName;

    private String methodName;
    private Object[] params;
    //防止重载
    private Class[] paramsTypes;


}
