package com.gdut.rpcstudy.demo.framework.protocol.http;


/**
 * @author: lele
 * @date: 2019/11/15 下午6:51
 */
@Deprecated
public class HttpServerHandler  {
  /* public void handle(HttpServletRequest req, HttpServletResponse resp){
       try {
           //获取输入流
           ServletInputStream inputStream = req.getInputStream();
           //包装成对象输入流
           ObjectInputStream ois=new ObjectInputStream(inputStream);
           //转换成方法调用参数
           RpcRequest rpcRequest = (RpcRequest) ois.readObject();
           String hostAddress = InetAddress.getLocalHost().getHostName();
           URL url=new URL(hostAddress,8080);
           String implClassName = ZkRegister.get(rpcRequest.getInterfaceName(), url);
           Class implClass=Class.forName(implClassName);
           Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsTypes());
           String result = (String) method.invoke(implClass.newInstance(), rpcRequest.getParams());
           //写回结果
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

   }*/
}
