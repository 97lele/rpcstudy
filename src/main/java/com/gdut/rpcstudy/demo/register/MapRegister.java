package com.gdut.rpcstudy.demo.register;

import com.gdut.rpcstudy.demo.framework.URL;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:56
 *
 */
public class MapRegister {


    //{服务名：{URL:实现类}}
    private static Map<String, Map<URL, Class>> REGISTER = new HashMap<>();

    //一个接口对应多个map,每个map只有一个key/value，key为可用的url(服务地址),value(具体的实现类)
    public static void register(String interfaceName, URL url, Class implClass) {
        Map<URL, Class> map = new HashMap<>();
        map.put(url,implClass);
        REGISTER.put(interfaceName,map);
        saveFile();
    }


    public static URL random(String interfaceName){
        REGISTER=getFile();
        return REGISTER.get(interfaceName).keySet().iterator().next();
    }

    public static Class get(String interfaceName, URL url){

        REGISTER=getFile();
        return REGISTER.get(interfaceName).get(url);
    }

public static Map<String,Map<URL,Class>> getFile(){
    FileInputStream fileInputStream= null;
    try {
        fileInputStream = new FileInputStream(System.getProperty("user.dir")+"/"+"temp.txt");
        ObjectInputStream in=new ObjectInputStream(fileInputStream);
        Object o = in.readObject();
        return (Map<String, Map<URL, Class>>) o;
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }
    return null;
}
    private static void saveFile(){
        FileOutputStream fileOutputStream= null;
        try {
            fileOutputStream = new FileOutputStream(System.getProperty("user.dir")+"/"+"temp.txt");
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(REGISTER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
