

package com.zhangxiaojian;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 动态变更配置（每隔5秒读取配置文件）
 *
 * @author 张晓键(472694060 @ qq.com)
 * @since 2022/9/20
 */
public class DynamicInterceptor {
    public static void load(String configPath, Instrumentation inst) {
        System.out.println("配置文件绝对路径:" + configPath);
        Properties properties = new Properties();
        InputStream in = null;
        ByteBuddyAgent.install();
        List<String> tempHotKeyList = new LinkedList<>();
        while (true) {
            try {
                if (configPath == null || !(new File(configPath)).exists()) {
                    System.out.println("配置文件不存在");
                    return;
                }
                // 使用ClassLoader加载properties配置文件生成对应的输入流
                in = new FileInputStream(configPath);
                // 使用properties对象加载输入流
                properties.load(in);
                //获取key对应的value值
                String hotKey = properties.getProperty("hotKey");
                if (hotKey == null || "".equals(hotKey.trim())) {
                    return;
                }
                String[] hostKeyArray = hotKey.split(",");
                for (String item : hostKeyArray) {
                    if (!item.contains(":")) {
                        continue;
                    }
                    if (!AgentContext.contains(item)) {
                        tempHotKeyList.add(item);
                        AgentContext.addHotKey(item);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (tempHotKeyList.size() > 0) {
                Class<?>[] loadedClasses = inst.getAllLoadedClasses();
                for (String item : tempHotKeyList) {
                    String[] itemSplit = item.split(":");
                    String className = itemSplit[0];
                    String methodName = itemSplit[1];
                    Class warnUpObject = null;
                    for (Class<?> clazz : loadedClasses) {
                        if (clazz.getName().equals(className)) {
                            System.out.println("热点类已加载,类名=" + className);
                            warnUpObject = clazz;
                            System.out.println("重新加载类,类名=" + className);
                            break;
                        }
                    }
                    new AgentBuilder
                            //采用ByteBuddy作为默认的Agent实例
                            .Default()
                            .warmUp(warnUpObject)
                            //拦截匹配方式：类以com.zhangxiaojian开始（其实就是com.zhangxiaojian包下的所有类）
                            .type(ElementMatchers.named(className))
                            //拦截到的类由transformer处理
                            .transform(transformer(methodName))
                            .with(listener(className))
                            //安装到 Instrumentation
                            .installOnByteBuddyAgent();

                    System.out.println("添加热点：" + item);
                }
                System.out.println("添加" + tempHotKeyList.size() + "个");
                tempHotKeyList.clear();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static AgentBuilder.Listener listener(String className) {
        return new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
                if (typeName.equals(className)) {
                    System.out.println("onDiscovery typeName=" + typeName);
                    System.out.println("onDiscovery classLoader=" + classLoader);
                    System.out.println("onDiscovery module=" + module);
                    System.out.println("onDiscovery loaded=" + loaded);
                }
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
//                System.out.println("onTransformation typeDescription=" + typeDescription);
//                System.out.println("onTransformation classLoader=" + classLoader);
//                System.out.println("onTransformation module=" + module);
//                System.out.println("onTransformation loaded=" + loaded);
//                System.out.println("onTransformation dynamicType=" + dynamicType);
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
//                System.out.println("onIgnored typeDescription=" + typeDescription);
//                System.out.println("onIgnored classLoader=" + classLoader);
//                System.out.println("onIgnored module=" + module);
//                System.out.println("onIgnored loaded=" + loaded);
            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
//                System.out.println("onError typeName=" + typeName);
//                System.out.println("onError classLoader=" + classLoader);
//                System.out.println("onError module=" + module);
//                System.out.println("onError loaded=" + loaded);
//                System.out.println("onError throwable=" + throwable);
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
//                System.out.println("onComplete typeName=" + typeName);
//                System.out.println("onComplete classLoader=" + classLoader);
//                System.out.println("onComplete module=" + module);
//                System.out.println("onComplete loaded=" + loaded);
//                System.out.println("onComplete loaded=" + loaded);
            }
        };
    }

    private static AgentBuilder.Transformer transformer(String methodName) {
        //动态构建操作，根据transformer规则执行拦截操作
        return new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    // 匹配上的具体的类型描述
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader,
                                                    JavaModule javaModule) {
                //构建拦截规则
                return builder
                        //method()指定哪些方法需要被拦截，ElementMatchers.any()表示拦截所有方法
                        .method(ElementMatchers.named(methodName))
                        //intercept()指定拦截上述方法的拦截器
                        .intercept(MethodDelegation.to(MethodInterceptor.class));
            }
        };
    }
}
