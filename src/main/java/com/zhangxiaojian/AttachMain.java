
package com.zhangxiaojian;


import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * attach为jvm进程添加探针
 *
 * @author 张晓键(472694060 @ qq.com)
 * @since 2022/9/20
 */
public class AttachMain {
    /**
     * attach添加探针
     *
     * @param args 参数
     *             1)如果只有一个参数则为进程id,配置文件默认为探针jar包同级目录的config.properties
     *             2)如果有两个参数则第一个为配置文件绝对路径，第二个参数为进程id
     */
    public static void main(String[] args) {
        try {
            String params = null;
            String processId = null;
            if (args.length == 1) {
                processId = args[0];
            } else if (args.length == 2) {
                params = args[0];
                processId = args[1];
            }
            //获取当前系统中所有 运行中的 虚拟机
            List<VirtualMachineDescriptor> vmList = VirtualMachine.list();
            if (vmList.size() == 0) {
                System.out.println("没有正在执行的jvm进程");
                return;
            }
            String path = getCurrentJarPath();
            String agentPath = path + File.separator + "CaptureMethodAgent.jar";
            for (VirtualMachineDescriptor vm : vmList) {
                if (vm.id().equals(processId)) {
                    System.out.println("process:" + vm.displayName());
                    VirtualMachine virtualMachine = VirtualMachine.attach(vm);
                    virtualMachine.loadAgent(agentPath, params);
                    virtualMachine.detach();
                }
            }
        } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentJarPath() {
        try {
            String path = AttachMain.class.getProtectionDomain().getCodeSource().getLocation().getFile();
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
