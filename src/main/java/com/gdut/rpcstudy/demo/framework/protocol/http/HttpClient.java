package com.gdut.rpcstudy.demo.framework.protocol.http;

import com.gdut.rpcstudy.demo.framework.serialize.serializer.JsonSerializer;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: lele
 * @date: 2019/11/15 下午7:12
 * 使用post方法携带对象进行请求
 */
@Deprecated
public class HttpClient {
    public RpcResponse post(String hostname, Integer port, RpcRequest rpcRequest) {
        try {
            URL url = new URL("http", hostname, port, "/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(rpcRequest);
            oos.flush();
            oos.close();
            InputStream inputStream = urlConnection.getInputStream();
            String result = IOUtils.toString(inputStream);
            RpcResponse deserialize = JsonSerializer.getInstance().deserialize(result.getBytes(), RpcResponse.class);
            return deserialize;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }
}
