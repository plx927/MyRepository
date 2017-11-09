package com.panlingxiao.concurrency.aqs;

import java.util.concurrent.CountDownLatch;

/**
 * Created by panlingxiao on 2016/5/5.
 */
public class AQSTest {

    static final BooleanLatch cdl = new BooleanLatch();
    public static void main(String[] args) throws InterruptedException{
        Thread t1 = new MyThread();
        t1.start();
        Thread t2 = new MyThread();
        t2.start();

        //cdl.await();
        //cdl.countDown();
        cdl.signal();
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
