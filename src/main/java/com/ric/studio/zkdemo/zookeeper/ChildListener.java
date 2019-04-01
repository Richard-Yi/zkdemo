package com.ric.studio.zkdemo.zookeeper;

import java.util.List;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/28
 */
public interface ChildListener {

    /**
     * 子节点修改
     * @param path
     * @param children
     */
    void childChanged(String path, List<String> children);

}
