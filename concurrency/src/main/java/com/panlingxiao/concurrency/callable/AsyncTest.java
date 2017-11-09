package com.panlingxiao.concurrency.callable;

import java.util.concurrent.*;

/**
 * Created by panlingxiao on 2016/4/11.
 * <p/>
 * 通过Callable实现一个异步处理的功能
 */
public class AsyncTest<T> implements  Runnable{

    static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws InterruptedException {
        final AsyncTest<Integer> aysnc = new AsyncTest<Integer>(new MyCallable(1,2),new MyCallback());
        aysnc.call();
        System.out.println("执行代码");
    }

    private Callable<T> task;
    private Callback<T> callback;
    private CountDownLatch countDownLatch;

    public AsyncTest(Callable<T> task, Callback<T> callback) {
        this.task = task;
        this.callback = callback;
    }

    public AsyncTest call(){
        new Thread(this).start();
        return this;
    }

    public void run() {
        //通过线程池提交一个任务
        Future<T> future = executor.submit(task);
        try {
            callback.callBackHandle(future.get());
            if(countDownLatch != null) {
                countDownLatch.countDown();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步执行
     * @throws InterruptedException
     */
    public void sync() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

    interface Callback<T> {
        void callBackHandle(T t);
    }

    static class MyCallable implements Callable<java.lang.Integer> {

        private final int  a;
        private final int  b;

        public MyCallable(Integer a,Integer b){
            this.a = a;
            this.b = b;
        }

        public java.lang.Integer call() throws Exception {
            return a + b;
        }
    }

    static  class MyCallback implements  Callback<Integer>{
        public void callBackHandle(Integer t) {
            System.out.println(String.format("计算结果为:%d",t));
        }
    }


}
