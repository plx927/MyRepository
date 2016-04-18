package com.panlingxiao.concurrency.sync;

import java.util.concurrent.CountDownLatch;

/**
 * Created by panlingxiao on 2016/4/16.
 * 测试Object的wait方法
 */
public class WaitTest {

    private Object object = new Object();

    //t1
    public void sync() throws InterruptedException{
        synchronized (this){
            this.wait();
            System.out.println("I'm invoked!");
        }
    }

    //t2
    public void bind(){
        synchronized (this){
            System.out.println("bind");
            notifyAll();
        }
    }

    /**
     * IllegalMonitorStateException
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        final WaitTest wt = new WaitTest();

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread t1 = new Thread(new Runnable() {
            public void run() {
                try {
                    countDownLatch.countDown();
                    wt.sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"t1");

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                wt.bind();
            }
        },"t2");

        t1.start();
        countDownLatch.await();
        t2.start();

    }
}
