package com.panlingxiao.concurrency.lock;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by panlingxiao on 2016/5/5.
 */
public class LockSupportTest2 {


    static  Object blocker = new Object();

    public static void main(String[] args) throws InterruptedException{

        new Thread(){
            @Override
            public void run() {
                LockSupport.park(blocker);
            }
        }.start();

        Thread.sleep(1000);

        blocker = null;

    }


}
