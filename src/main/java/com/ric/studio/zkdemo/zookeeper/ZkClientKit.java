package com.ric.studio.zkdemo.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Zk curator
 * @author Richard_yyf
 * @version 1.0 2019/3/27
 */
@Component
public class ZkClientKit implements ZookeeperClient{

    @Autowired
    private ZkClient client;

    /**
     * 客户端监听器集合
     */
    private ConcurrentMap<String, ConcurrentMap<ChildListener, IZkChildListener>> childListeners = new ConcurrentHashMap<>();


    @Override
    public void create(String path, boolean ephemeral) {
        // 如果不是临时节点
        if (!ephemeral) {
            // 判断该客户端是否存在
            if (checkExists(path)) {
                return;
            }
        }
        // 获得/的位置
        int i = path.lastIndexOf('/');
        if (i > 0) {
            // 创建客户端
            create(path.substring(0, i), false);
        }
        // 如果是临时节点
        if (ephemeral) {
            // 创建临时节点
            createEphemeral(path);
        } else {
            // 递归创建节点
            createPersistent(path);
        }
    }



    @Override
    public void delete(String path) {
        try {
            // 删除节点
            client.delete(path);
        } catch (ZkNoNodeException e) {
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            // 获得子节点
            return client.getChildren(path);
        } catch (ZkNoNodeException e) {
            return null;
        }
    }

    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        // 获得子节点的监听器集合
        ConcurrentMap<ChildListener, IZkChildListener> listeners = childListeners.get(path);
        // 如果为空，则创建一个，并且加入集合
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<>());
            listeners = childListeners.get(path);
        }
        IZkChildListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    private IZkChildListener createTargetChildListener(String path, ChildListener listener) {
        return new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds)
                    throws Exception {
                listener.childChanged(parentPath, currentChilds);
            }
        };
    }

    private List<String> addTargetChildListener(String path, IZkChildListener listener) {
        return client.subscribeChildChanges(path, listener);
    }

    @Override
    public void removeChildListener(String path, ChildListener listener) {
        client.unsubscribeChildChanges(path, (IZkChildListener) listener);
    }

    /**
     * 节点是否存在
     * @param path
     * @return
     */
    public boolean checkExists(String path) {
        try {
            // 查看是否存在该节点
            return client.exists(path);
        } catch (Throwable t) {
        }
        return false;
    }

    /**
     * 创建临时节点
     * @param path
     */
    public void createEphemeral(String path) {
        try {
            // 创建临时节点
            client.createEphemeral(path);
        } catch (ZkNodeExistsException e) {
        }
    }

    /**
     * 创建持久节点
     * @param path
     */
    public void createPersistent(String path) {
        try {
            // 递归创建节点
            client.createPersistent(path);
        } catch (ZkNodeExistsException e) {
        }
    }

    public void addChildDataListener(String path, IZkDataListener listener) {
        try {
            // 递归创建节点
            client.subscribeDataChanges(path, listener);
        } catch (ZkNodeExistsException e) {
        }
    }

    public void removeChildDataListener(String path, IZkDataListener listener) {
        try {
            // 递归创建节点
            client.unsubscribeDataChanges(path, listener);
        } catch (ZkNodeExistsException e) {
        }
    }

    public void addData(String path, String data) {
        try {
            client.writeData(path, data);
        } catch (ZkNodeExistsException e) {
        }

    }

    public String getData(String path) {
        try {
            return client.readData(path);
        } catch (ZkNodeExistsException e) {
        }
        return null;
    }

}
