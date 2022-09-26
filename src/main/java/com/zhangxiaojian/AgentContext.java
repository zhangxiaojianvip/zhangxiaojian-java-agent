

package com.zhangxiaojian;

import java.util.LinkedList;
import java.util.List;

/**
 * 探针上下文变量
 *
 * @author 张晓键(472694060 @ qq.com)
 * @since 2022/9/20
 */
public class AgentContext {
    private static final List<String> HOTKEY_LIST = new LinkedList<>();

    public static void addHotKey(String hotKey) {
        if (HOTKEY_LIST.contains(hotKey)) {
            return;
        }
        HOTKEY_LIST.add(hotKey);
    }

    public static boolean contains(String hotKey) {
        return HOTKEY_LIST.contains(hotKey);
    }
}
