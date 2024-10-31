package com.sam.sale;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketSale {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181"; // ZooKeeper 地址
    private static final String TICKET_PATH = "/tickets"; // 票的路径
    private static final String LOCK_PATH = "/ticket_lock"; // 分布式锁路径
    private static final int TOTAL_TICKETS = 100; // 总票数

    public static void main(String[] args) throws Exception {
        // 创建 ZooKeeper 客户端
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZOOKEEPER_ADDRESS, new ExponentialBackoffRetry(1000, 3));
        client.start();

        // 初始化票数节点
        initializeTicketNode(client);

        // 使用线程池模拟多个用户购票
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) { // 50个用户
            final int userId = i;
            executor.submit(() -> {
                try {
                    buyTicket(client, userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }

    private static void initializeTicketNode(CuratorFramework client) throws Exception {
        // 检查并删除已有的票节点
        if (client.checkExists().forPath(TICKET_PATH) != null) {
            client.delete().forPath(TICKET_PATH);
            System.out.println("Deleted existing ticket node.");
        }

        // 创建新的票节点并初始化票数
        client.create().withMode(CreateMode.PERSISTENT).forPath(TICKET_PATH, String.valueOf(TOTAL_TICKETS).getBytes());
        System.out.println("Initialized ticket node with " + TOTAL_TICKETS + " tickets.");
    }

    private static void buyTicket(CuratorFramework client, int userId) throws Exception {
        // 创建分布式锁
        InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);

        try {
            // 获取锁
            lock.acquire();

            // 获取当前票数
            byte[] data = client.getData().forPath(TICKET_PATH);
            int remainingTickets = Integer.parseInt(new String(data));

            if (remainingTickets > 0) {
                // 减少票数
                client.setData().forPath(TICKET_PATH, String.valueOf(remainingTickets - 1).getBytes());
                System.out.println("User " + userId + " bought a ticket! Remaining tickets: " + (remainingTickets - 1));
            } else {
                System.out.println("User " + userId + " tried to buy a ticket, but none are left!");
            }
        } finally {
            // 释放锁
            lock.release();
        }
    }
}
