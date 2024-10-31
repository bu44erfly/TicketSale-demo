package com.sam.sale;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class CuratorConfig {


     static final String ZK_ADDRESS = "127.0.0.1:2181";  // 替换为实际的ZooKeeper地址
     static final String TICKET_PATH = "/tickets";

    public static CuratorFramework createCuratorClient() {

        CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3));

        client.start();
        return client;
    }
}
