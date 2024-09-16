

package com.sam.sale;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

/**
 *
 * 自定义的zookeeper分布式锁
 */
public class DistributedLock {
    private ZooKeeper zooKeeper;
    private String lockPath;
    private String currentNode;
    private String watchedNode;

    public DistributedLock(ZooKeeper zooKeeper, String lockPath) {
        this.zooKeeper = zooKeeper;
        this.lockPath = lockPath;
    }

    public void acquireLock() throws Exception {
        // 创建临时顺序节点
        currentNode = zooKeeper.create(lockPath + "/lock_", new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        // 获取该路径下的所有子节点
        List<String> children = zooKeeper.getChildren(lockPath, false);
        Collections.sort(children);

        String nodeName = currentNode.substring(currentNode.lastIndexOf("/") + 1);

        int index = children.indexOf(nodeName);
        if (index == 0) {
            // 当前节点是最小节点，获得锁
            System.out.println("Lock acquired: " + currentNode);
        } else {
            // 监听比自己小的节点
            watchedNode = children.get(index - 1);
            Stat stat = zooKeeper.exists(lockPath + "/" + watchedNode, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    try {
                        acquireLock();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            if (stat == null) {
                acquireLock();
            }
        }
    }

    public void releaseLock() throws Exception {
        // 释放锁，删除节点
        zooKeeper.delete(currentNode, -1);
        System.out.println("Lock released: " + currentNode);
    }
}
