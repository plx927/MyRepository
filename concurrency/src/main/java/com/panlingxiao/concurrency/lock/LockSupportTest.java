package com.panlingxiao.concurrency.lock;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by panlingxiao on 2016/5/1.
 */
public class LockSupportTest {

    public static void main(String[] args) {
        MyThread myThread = new MyThread();
        myThread.start();

        myThread.interrupt();

    }

    static class MyThread extends Thread{
        @Override
        public void run() {
            LockSupport.park();
            System.out.println("");
        }
    }
}
