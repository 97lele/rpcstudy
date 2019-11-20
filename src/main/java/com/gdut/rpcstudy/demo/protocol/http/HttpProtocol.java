package com.gdut.rpcstudy.demo.protocol.http;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;

/**
 * @author lulu
 * @Date 2019/11/15 23:31
 * http协议
 */
@Deprecated
public class HttpProtocol implements Protocol {
    @Override
    public void start(URL url) {
        HttpServer server=new HttpServer();
        server.start(url.getHostname(),url.getPort());
    }

    @Override
    public RpcResponse send(URL url, RpcRequest rpcRequest) {
        HttpClient client=new HttpClient();
        RpcResponse res = client.post(url.getHostname(), url.getPort(), rpcRequest);
        return res;
    }
}
