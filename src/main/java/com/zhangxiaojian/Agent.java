package com.zhangxiaojian;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * 探针入口
 *
 * @author 张晓键(472694060@qq.com)
 * @since 2022/9/20
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain探针接入");
        agentMount(agentArgs, inst);
    }

    private static void agentMount(String agentArgs, Instrumentation inst) {
        String path = getCurrentJarPath();
        if (agentArgs == null) {
            agentArgs = path + File.separator + "config.properties";
        }
        String configPath = agentArgs;
        Thread t = new Thread(() -> DynamicInterceptor.load(configPath, inst));
        t.start();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain探针接入");
        agentMount(agentArgs, inst);
    }

    public static String getCurrentJarPath() {
        try {
            String path = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            path = java.net.URLDecoder.decode(path, "UTF-8");
            File file = new File(path);
            if (file.isFile()) {
                return new File(file.getParent()).getAbsolutePath();
            } else {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
