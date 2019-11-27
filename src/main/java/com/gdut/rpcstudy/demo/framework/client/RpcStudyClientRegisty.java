package com.gdut.rpcstudy.demo.framework.client;

import com.gdut.rpcstudy.demo.DemoApplication;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: lele
 * @date: 2019/11/20 下午3:06
 * 第一个接口获取注册bean能力，第二个接口获取类加载器,仿feignregister写法
 */

public class RpcStudyClientRegisty implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware{


    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {


        if (DemoApplication.mode == 1) {
            //获取指定路径中注解bean定义扫描器
            ClassPathScanningCandidateComponentProvider scanner = getScanner();
            //获取扫描的包，通过enable那个注解的属性
            Set<String> basePackages = getBasePackages(importingClassMetadata);
            //添加过滤规则，属于rpcstudyclient的加入，excludeFilter则是排除
            scanner.addIncludeFilter(new AnnotationTypeFilter(RpcStudyClient.class));

            Set<BeanDefinition> candidateBeans = new HashSet<>();
            //获取符合条件的bean
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
                candidateBeans.addAll(candidateComponents);
            }
            //spring中用BeanDefintion来表示bean，这里判断bean类型是否合适，合适就注册
            for (BeanDefinition candidateBean : candidateBeans) {
                //如果bean还没有注册
                if (!registry.containsBeanDefinition(candidateBean.getBeanClassName())) {
                    //判读是否含有注解
                    if (candidateBean instanceof AnnotatedBeanDefinition) {
                        //存储该类信息的bean,methodMetadata(方法)，AnnotationMetadata(里面也包括methodMetadata,可以获取注解，类信息等等)
                        AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) candidateBean;
                        //获取bean的类信息
                        AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                        //判断其否为接口
                        Assert.isTrue(annotationMetadata.isInterface(), "@RpcStudeyClient注解只能用在接口上");
                        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(RpcStudyClient.class.getCanonicalName());

                        this.registerRpcClient(registry, annotationMetadata, attributes);
                    }
                }
            }
        }

    }

    //注册bean
    private void registerRpcClient(BeanDefinitionRegistry registry,
                                   AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {

        //获取bean类名
        String className = annotationMetadata.getClassName();
        //使用自定义的对象工厂定制化生成bean
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(RpcStudyClientFactoryBean.class);
        //设置根据类型的注入方式
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        definition.addPropertyValue("type", className);
        String name = attributes.get("name") == null ? "" : (String) (attributes.get("name"));

        String alias = name + "RpcStudyClient";
        //获取bean基类
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        //防止其他有实现，设置此实现为首要
        beanDefinition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                new String[]{alias});
        //注册bean
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }


    //复写bean扫描的判断
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false
        ) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                //存放注解相关信息，具备了class、注解的信息
                AnnotationMetadata metadata = beanDefinition.getMetadata();
                //是否是独立能创建对象的，比如class、内部类、静态内部类
                if (metadata.isIndependent()) {
                    //用于过滤注解为@RpcClient的注解
                    if (metadata.isInterface() &&
                            metadata.getInterfaceNames().length == 1 &&
                            Annotation.class.getName().equals(metadata.getInterfaceNames()[0])) {
                        try {
                            Class<?> target = ClassUtils.forName(metadata.getClassName(),
                                    RpcStudyClientRegisty.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error(
                                    "Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);
                        }
                    }
                    return true;
                }
                return false;

            }
        };
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    //获取需要扫描的包位置
    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableRpcStudyClient.class.getCanonicalName());
        String[] scanPackages = (String[]) attributes.get("basePackages");
        Set<String> basePackages = new HashSet<>();

        if (scanPackages.length > 0) {
            //扫描指定包
            for (String pkg : scanPackages) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
        } else {
            //扫描主入口所在的包
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

}
