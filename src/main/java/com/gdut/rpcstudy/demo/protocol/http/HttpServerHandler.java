package com.gdut.rpcstudy.demo.protocol.http;


import com.gdut.rpcstudy.demo.framework.Invocation;
import com.gdut.rpcstudy.demo.framework.URL;
import com.gdut.rpcstudy.demo.register.MapRegister;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:51
 */
public class HttpServerHandler  {
   public void handle(HttpServletRequest req, HttpServletResponse resp){

       try {
           ServletInputStream inputStream = req.getInputStream();
           ObjectInputStream ois=new ObjectInputStream(inputStream);
           Invocation invocation= (Invocation) ois.readObject();
           URL url=new URL("127.0.0.1",8080);
           Class implClass=MapRegister.get(invocation.getInterfaceName(),url);

           Method method = implClass.getMethod(invocation.getMethodName(), invocation.getParamsTypes());
           String result = (String) method.invoke(implClass.newInstance(), invocation.getParams());
           IOUtils.write(result,resp.getOutputStream());
       } catch (IOException e) {
           e.printStackTrace();
       } catch (ClassNotFoundException e) {
           e.printStackTrace();
       } catch (NoSuchMethodException e) {
           e.printStackTrace();
       } catch (IllegalAccessException e) {
           e.printStackTrace();
       } catch (InstantiationException e) {
           e.printStackTrace();
       } catch (InvocationTargetException e) {
           e.printStackTrace();
       }

   }
}
