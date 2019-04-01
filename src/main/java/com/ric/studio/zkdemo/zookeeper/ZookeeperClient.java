package com.ric.studio.zkdemo.zookeeper;

import java.util.List;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/28
 */
public interface ZookeeperClient {

    /**
     * 创建client
     * @param path
     * @param ephemeral
     */
    void create(String path, boolean ephemeral);

    /**
     * 删除client
     * @param path
     */
    void delete(String path);

    /**
     * 获得子节点集合
     * @param path
     * @return
     */
    List<String> getChildren(String path);

    /**
     * 向zookeeper的该节点发起订阅，获得该节点所有
     * @param path
     * @param listener
     * @return
     */
    List<String> addChildListener(String path, ChildListener listener);

    /**
     * 移除该节点的子节点监听器
     * @param path
     * @param listener
     */
    void removeChildListener(String path, ChildListener listener);

}
