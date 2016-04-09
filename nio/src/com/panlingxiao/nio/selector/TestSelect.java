package com.panlingxiao.nio.selector;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;

/**
 * 测试Selector的select方法
 */
public class TestSelect {

    static Selector selector;
    static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws  Exception{

        selector = Selector.open();

        Thread t1 = new Thread(){
            @Override
            public void run() {
                try {
                    countDownLatch.countDown();
                    //让t1阻塞在select方法上
                    selector.select();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        t1.start();
        //通过countDownLatch来确保线程t1已经执行
        countDownLatch.await();

        //通过wakeUp可以让select方法返回
        //selector.wakeup();

        //中断t1线程,也可以让select方法返回
        t1.interrupt();
    }
}
