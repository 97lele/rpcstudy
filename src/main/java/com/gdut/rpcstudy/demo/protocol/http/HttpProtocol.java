package com.gdut.rpcstudy.demo.protocol.http;

import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.Protocol;
import com.gdut.rpcstudy.demo.framework.URL;

/**
 * @author lulu
 * @Date 2019/11/15 23:31
 * http协议
 */
public class HttpProtocol implements Protocol {
    @Override
    public void start(URL url) {
        HttpServer server=new HttpServer();
        server.start(url.getHostname(),url.getPort());
    }

    @Override
    public String send(URL url, Invocation invocation) {
        HttpClient client=new HttpClient();
        String res = client.post(url.getHostname(), url.getPort(), invocation);
        return res;
    }
}
