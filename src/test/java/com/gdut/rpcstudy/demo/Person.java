package com.gdut.rpcstudy.demo;

import com.gdut.rpcstudy.demo.framework.ProxyFactory;
import com.gdut.rpcstudy.demo.consumer.HelloService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author: lele
 * @date: 2019/11/19 下午5:45
 */
@Data
@AllArgsConstructor
public class Person {
    private String name;
    private Integer age;

    public static void main(String[] args) {
        Person[] people = {
                new Person("test1",10),
                new Person("test2",20),
                new Person("test3",30),
                new Person("test4",40),
        };

        TreeMap<Integer,Person> treeMap=new TreeMap();
        Map<Integer,Integer> res=new HashMap<>();
        Map<Integer,Integer> test=new HashMap<>();
        for (Person person : people) {
            int lastWeight=treeMap.size()==0?0:treeMap.lastKey();
            res.put(person.getAge()+lastWeight,person.getAge());
            treeMap.put(person.getAge()+lastWeight,person);
        }
        for (int i = 0; i < 100; i++) {
            int v = (int) (treeMap.lastKey() *  Math.random());
            SortedMap<Integer,Person> map=treeMap.tailMap(v,false);
            Integer o = map.firstKey();
            if(test.containsKey(o)){
                test.put(o,test.get(o)+1);
            }else{
                test.put(o,1);
            }
        }
        for (Map.Entry<Integer, Integer> integerIntegerEntry : res.entrySet()) {
            for (Map.Entry<Integer, Integer> integerEntry : test.entrySet()) {
                if(integerEntry.getKey().equals(integerIntegerEntry.getKey())){
                    System.out.println(integerIntegerEntry.getValue()+"--"+integerEntry.getValue());
                    break;
                }
            }
        }


    }
    public static void test(){
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result = helloService.qq();
        System.out.println(result);
    }
}
