package com.gdut.rpcstudy.demo.protocol.http;

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:44
 * 构造嵌入式tomcat服务器
 */
public class HttpServer {
    public void start(String hostname, Integer port) {

        Tomcat tomcat = new Tomcat();
        //获取server实例
        Server server = tomcat.getServer();
        Service service = server.findService("Tomcat");

        //连接器
        Connector connector = new Connector();
        connector.setPort(port);

        Engine engine = new StandardEngine();
        engine.setDefaultHost(hostname);
        //host
        Host host = new StandardHost();
        host.setName(hostname);
        String contextPath = "";
        //上下文
        Context context = new StandardContext();
        context.setPath(contextPath);
        context.addLifecycleListener(new Tomcat.FixContextListener());

        host.addChild(context);
        engine.addChild(host);

        service.setContainer(engine);
        service.addConnector(connector);
        //添加servlet，匹配所有路径
        tomcat.addServlet(contextPath, "dispathcer", new DispatcherServlet());
        context.addServletMappingDecoded("/*","dispathcer");
        try {
            tomcat.start();
            tomcat.getServer().await();

        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}
