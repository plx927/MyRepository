package com.panlingxiao.concurrency.threadpool;

import java.util.concurrent.*;

/**
 * Created by panlingxiao on 2016/4/13.
 */
public class CachedThreadPoolTest {

    public static void main(String[] args) throws InterruptedException{

        //创建带缓存功能的线程池
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        for(int i = 0;i < 2;i++) {
            final int j = i;
            //先提交一个任务
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    System.out.println("任务:"+j+" 执行");
                }
            });
            Thread.sleep(1);
        }



    }
}
