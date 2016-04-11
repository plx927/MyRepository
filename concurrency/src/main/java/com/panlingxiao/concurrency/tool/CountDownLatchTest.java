package com.panlingxiao.concurrency.tool;

import java.util.concurrent.CountDownLatch;

/**
 * Created by panlingxiao on 2016/4/12.
 * <p/>
 * CountDownLatch使用以及分析
 * <p/>
 * CountDownLatch中的count值只能减，不能被重置。
 * <p/>
 * <p/>
 * CoutDownLatch的应用场景
 */
public class CountDownLatchTest {


    public static void main(String[] args) throws InterruptedException {
        new Driver().drive();

    }



    static class Worker implements Runnable {

        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;

        Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
                doneSignal.countDown();
            } catch (InterruptedException ex) {
            } // return;
        }

        private void doWork() {
            System.out.println(Thread.currentThread().getName()+" 执行任务！");
        }
    }

    static class Driver {

        final int N = Runtime.getRuntime().availableProcessors();

        /*
         * 第一个CountDownLatch作为开始信号的通知,是让所有的工作线程处于等待。
         * 知道当前的Driver准备就绪，才让这些工作开始执行它们的任务
         *
         */
        CountDownLatch startSignal = new CountDownLatch(1);

        /*
         * 第二个CountDownLatch作为一个结束的信号通过，让Drvier等待，
         * 知道所有的Worker工作完毕了才让它完成收尾的工作。
         */
        CountDownLatch doneSignal = new CountDownLatch(N);


        public void drive() throws InterruptedException {
            for (int i = 0; i < N; ++i) {
                // create and start threads
                new Thread(new Worker(startSignal, doneSignal),"工作线程"+i).start();
            }

            doSomethingReady();

            //让所有的工作线程开始执行它们的任务
            startSignal.countDown();

            //等待所有的工作线程执行完毕才能继续执行
            doneSignal.await();

            doSomethingFinish();
        }

        /**
         * Driver处理开始的准备工作
         */
        private  void doSomethingReady() {
            System.out.println("准备就绪");

        }

        /**
         * 处理一些收尾的任务
         */
        private  void doSomethingFinish() {
            System.out.println("扫尾工作完成");
        }
    }

}
