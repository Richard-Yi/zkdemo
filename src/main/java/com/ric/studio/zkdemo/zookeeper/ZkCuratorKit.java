package com.ric.studio.zkdemo.zookeeper;



import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/27
 */
@Component
public class ZkCuratorKit implements ZookeeperClient{


    @Autowired
    private CuratorFramework client;

    /**
     * 客户端监听器集合
     */
    private ConcurrentMap<String, ConcurrentMap<ChildListener, CuratorWatcher>> childListeners = new ConcurrentHashMap<>();

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
            client.delete().forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        // 获得子节点的监听器集合
        ConcurrentMap<ChildListener, CuratorWatcher> listeners = childListeners.get(path);
        // 如果为空，则创建一个，并且加入集合
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<>());
            listeners = childListeners.get(path);
        }
        CuratorWatcher targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    private List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void addTargetDataListener(String path, CuratorWatcher listener) {
        try {
            client.getData().usingWatcher(listener).forPath(path);
        } catch (Exception e) {
        }
    }

    private CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }


    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, CuratorWatcher> listeners = childListeners.get(path);
        if (listeners != null) {
            CuratorWatcher targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    private void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
    }


    public boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void createEphemeral(String path) {
        try {
            // 创建临时节点
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createPersistent(String path) {
        try {
            // 递归创建节点
           client.create().withMode(CreateMode.PERSISTENT).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * curator watcher 实现类
     */
    private class CuratorWatcherImpl implements CuratorWatcher {

        private volatile ChildListener listener;

        public CuratorWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (listener != null) {
                String path = event.getPath() == null ? "" : event.getPath();
                listener.childChanged(path,
                        // if path is null, curator using watcher will throw NullPointerException.
                        // if client connect or disconnect to server, zookeeper will queue
                        // watched event(Watcher.Event.EventType.None, .., path = null).
                        StringUtils.isNotEmpty(path)
                                ? client.getChildren().usingWatcher(this).forPath(path)
                                : Collections.<String>emptyList());
            }
        }
    }
}
