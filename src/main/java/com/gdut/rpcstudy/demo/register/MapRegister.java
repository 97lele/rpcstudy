package com.gdut.rpcstudy.demo.register;

import com.gdut.rpcstudy.demo.framework.URL;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: lele
 * @date: 2019/11/15 下午6:56
 */
public class MapRegister {

    //{服务名：{URL:实现类}}
    private static Map<String, Map<URL, Class>> REGISTER = new HashMap<>();

    public static void register(String interfaceName, URL url, Class implClass) {
        Map<URL, Class> map = new HashMap<>();
        map.put(url,implClass);
        REGISTER.put(interfaceName,map);
    }

    public static Class get(String interfaceName,URL url){
        return REGISTER.get(interfaceName).get(url);
    }

}
