package com.zhangxiaojian;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

/**
 * 拦截到的方法作出入参打印
 *
 * @author 张晓键(472694060 @ qq.com)
 * @since 2022/9/20
 */
public class MethodInterceptor {

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable,
                                   @AllArguments Object[] paramArray) throws Exception {
        System.out.println("--------------------------------------------------");
        System.out.println("调用类方法： " + method);
        System.out.println("调用方法： " + method.getName());
        System.out.println("参数个数： " + paramArray.length + "个");
        long start = System.currentTimeMillis();
        try {
            int index = 0;
            for (Object param : paramArray) {
                index++;
                if (param instanceof Integer
                        || param instanceof Double
                        || param instanceof Float
                        || param instanceof BigDecimal
                        || param instanceof Boolean
                        || param instanceof Long) {
                    System.out.println("参数" + index + "：" + param);
                    continue;
                }
                ClassLoader classLoader = param.getClass().getClassLoader();
                if (classLoader == null || classLoader.getClass().getCanonicalName().contains("AppClassLoader") ||
                        classLoader.getClass().getCanonicalName().contains("RestartClassLoader")) {
                    System.out.println("参数" + index + "：" + param);
                } else {
                    String classLoaderName = classLoader.getClass().getCanonicalName();
                    System.out.println("类加载器名称:" + classLoaderName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object result;
        try {
            result = callable.call();
        } finally {
            System.out.println("耗时：" + (System.currentTimeMillis() - start) + "ms");
        }
        try {
            System.out.println("结果：" + (result == null ? "" : result.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------------------");
        return result;
    }
}
