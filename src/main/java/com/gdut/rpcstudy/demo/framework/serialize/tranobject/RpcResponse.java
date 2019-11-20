package com.gdut.rpcstudy.demo.framework.serialize.tranobject;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:12
 */
@Data

public class RpcResponse  {
    private String requestId;

    private Object result;

    private String error;
}
