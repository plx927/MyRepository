package com.panlingxiao.concurrency.synchronier;

import java.util.concurrent.CountDownLatch;

/**
 * Created by panlingxiao on 2016/4/30.
 */
public class CountDownLatchTest2 {


    static final CountDownLatch cdl = new CountDownLatch(1);
    public static void main(String[] args) throws InterruptedException{
        Thread t1 = new MyThread();
        t1.start();
        Thread t2 = new MyThread();
        t2.start();

        //cdl.await();
        //cdl.countDown();
        cdl.countDown();
    }

    static class MyThread extends Thread{
        public void run() {
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}
