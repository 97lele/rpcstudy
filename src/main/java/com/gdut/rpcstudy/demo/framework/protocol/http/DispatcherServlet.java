package com.gdut.rpcstudy.demo.framework.protocol.http;

import javax.servlet.http.HttpServlet;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:51
 * 相当于拦截器的功能，请求都要经过这个servlet处理
 */
@Deprecated
public class DispatcherServlet extends HttpServlet {
    /*@Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new HttpServerHandler().handle(req,resp);
    }*/
}
