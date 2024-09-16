package com.sam.sale;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketBooking implements Runnable {
    private CuratorFramework client;
    private String path;

    private InterProcessMutex lock ;
    private int ticketsAvailable;

    public TicketBooking(CuratorFramework client0,String path0 , int ticket0) {
        this.lock = new InterProcessMutex(client0 ,path0);
        this.ticketsAvailable =  ticket0 ;
    }


    @Override
    public void run() {
        try {
            lock.acquire(5, TimeUnit.SECONDS);
            if (ticketsAvailable > 0) {
                System.out.println(Thread.currentThread().getName() + " bought a ticket.");
                ticketsAvailable --;
                System.out.println("Tickets remaining: " + ticketsAvailable);
            } else {
                System.out.println("Tickets sold out.");
            }

            lock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        CuratorFramework client = CuratorConfig.createCuratorClient();
        String lockPath = "/locks";
        int tickets = 30 ;
        Runnable r =new TicketBooking(client,lockPath,tickets); //共享资源

        for (int i = 0; i < 17; i++) {
            new Thread(r, "User-" + i).start();
        }



//        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 3000, null);
//        String lockPath = "/locks";
//
//        // 如果根节点不存在，则创建
//        if (zooKeeper.exists(lockPath, false) == null) {
//            zooKeeper.create(lockPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        }
//
//        DistributedLock lock = new DistributedLock(zooKeeper, lockPath);
//        int tickets = 50; // 假设总票数为5
//
//        // 模拟多个线程同时抢票
//        for (int i = 0; i < 30; i++) {
//            new Thread(new TicketBooking(lock, tickets), "User-" + i).start();
//        }
//
//        // 关闭连接
//        Thread.sleep(10000); // 确保所有线程都执行完毕
//        zooKeeper.close();
    }
}
