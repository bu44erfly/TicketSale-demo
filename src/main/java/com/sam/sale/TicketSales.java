package com.sam.sale;

/**
 * 单机模式
 */
class Ticket {
    private int totalTickets; // 总票数

    public Ticket(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    // 买票的方法
    public synchronized boolean buyTicket() {
        if (totalTickets > 0) {
            totalTickets--;
            System.out.println(Thread.currentThread().getName() + " bought a ticket. Remaining tickets: " + totalTickets);
            return true;
        } else {
            System.out.println(Thread.currentThread().getName() + " tried to buy a ticket, but none are left.");
            return false;
        }
    }
}

public class TicketSales {
    public static void main(String[] args) {
        Ticket ticket = new Ticket(30); // 初始化10张票

        // 创建多个用户线程


        // 启动10个线程模拟10个用户
        Thread[] users = new Thread[5];
        for (int i = 0; i < users.length; i++) {
            Runnable user = () -> {
                while (true) {
                    if (!ticket.buyTicket()) {
                        break; // 如果买票失败（票已卖完），则退出循环
                    }
                    try {
                        Thread.sleep(100); // 模拟购票间隔
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            users[i] = new Thread(user, "User-" + (i + 1));
            users[i].start();
        }

        // 等待所有线程完成
        for (Thread userThread : users) {
            try {
                userThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All tickets have been sold out.");
    }
}
