package com.panlingxiao.concurrency.sync;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by panlingxiao on 2016/6/15.
 */
public class VolatileTest {

    static volatile boolean flag = true;
    static volatile int k = 0;
    static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
//        new Thread(){
//            @Override
//            public void run() {
//                while (flag){
//                    System.out.println("a");
//                }
//            }
//        }.start();
//
//        Thread.sleep(100);
//        flag = false;
        Thread[] threads = new Thread[100];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {

                    for (int i = 0; i < 1000; i++) {
                        counter.incrementAndGet();
                    }
                }
            };
            threads[i].start();
        }

        for(Thread thread : threads){
            thread.join();
        }


        System.out.println(counter.get());
    }

}
