package com.ric.studio.zkdemo.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Richard_yyf
 * @version 1.0 2019/3/27
 */
@Configuration
public class ZkConfiguration {

    @Autowired
    private ZkContext zkContext;

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(zkContext.getZkAddr(), 5000);
    }

    @Bean
    public CuratorFramework curatorZookeeperClient() {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkContext.getZkAddr(), retryPolicy);
        client.start();
        return client;
    }
}
