package com.panlingxiao.concurrency.threadpool;

import java.util.concurrent.*;

/**
 * Created by panlingxiao on 2016/4/13.
 */
public class ThreadPoolExecutorTest {

    public static void main(String[] args) {
        /*
         *  创建时给定LinkedBlockingQueue的大小,对于线程工厂也可以进行重写。
         *  可以在线程工厂中也缓存一部分
         */
//        int capacity = 1000;
//        ExecutorService cachedThreadPool = new ThreadPoolExecutor(
//                0,
//                Runtime.getRuntime().availableProcessors(),
//                60,
//                TimeUnit.SECONDS,
//                new LinkedBlockingQueue<Runnable>(capacity),
//                Executors.defaultThreadFactory());
//
//        for (int i = 0; i < 10000; i++) {
//            final int j = i;
//            cachedThreadPool.execute(new Runnable() {
//                public void run() {
//                    try {
//                        System.out.println(Thread.currentThread().getName() + "执行任务 " + j);
//                        Thread.sleep((long)(Math.random()*1000));
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//        }



        int capacity = 1000;
        ExecutorService cachedThreadPool = new ThreadPoolExecutor(
                0,
                Runtime.getRuntime().availableProcessors(),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(capacity),
                Executors.defaultThreadFactory());

        for (int i = 0; i < 10000; i++) {
            final int j = i;
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.println(Thread.currentThread().getName() + "执行任务 " + j);
                        Thread.sleep((long) (10000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }
}
